package com.example.fitbalance.repository

import com.example.fitbalance.data.Meal
import com.example.fitbalance.data.MealPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    suspend fun saveMealPlan(mealPlan: MealPlan): MealResult {
        return try {
            val userId = currentUserId ?: return MealResult.Error("Kullanıcı bulunamadı")

            val mealPlanWithUser = mealPlan.copy(
                userId = userId,
                id = if (mealPlan.id.isEmpty()) firestore.collection("meal_plans").document().id else mealPlan.id
            )

            firestore.collection("meal_plans")
                .document(mealPlanWithUser.id)
                .set(mealPlanWithUser)
                .await()

            MealResult.Success(mealPlanWithUser)
        } catch (e: Exception) {
            MealResult.Error(e.message ?: "Öğün kaydedilemedi")
        }
    }

    suspend fun getTodayMealPlan(): MealResult {
        return try {
            val userId = currentUserId ?: return MealResult.Error("Kullanıcı bulunamadı")
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val snapshot = firestore.collection("meal_plans")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", today)
                .limit(1)
                .get(com.google.firebase.firestore.Source.SERVER)
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents[0]
                val mealPlan = doc.toObject(MealPlan::class.java)

                MealResult.Success(mealPlan)
            } else {
                MealResult.Error("Bugün için öğün planı bulunamadı")
            }
        } catch (e: Exception) {
            MealResult.Error(e.message ?: "Öğün planı alınamadı")
        }
    }

    suspend fun changeMeal(
        mealPlanId: String,
        mealType: String,
        newMeal: Meal
    ): MealResult {
        return try {
            val docRef = firestore.collection("meal_plans").document(mealPlanId)
            val snapshot = docRef.get().await()
            val mealPlan = snapshot.toObject(MealPlan::class.java)
                ?: return MealResult.Error("Öğün planı bulunamadı")

            val currentTime = System.currentTimeMillis()
            val updatedChangeHistory = when (mealType) {
                "breakfast" -> mealPlan.changeHistory.copy(breakfast = currentTime)
                "lunch" -> mealPlan.changeHistory.copy(lunch = currentTime)
                "dinner" -> mealPlan.changeHistory.copy(dinner = currentTime)
                else -> mealPlan.changeHistory
            }

            val updatedMealPlan = when (mealType) {
                "breakfast" -> mealPlan.copy(breakfast = newMeal, changeHistory = updatedChangeHistory)
                "lunch" -> mealPlan.copy(lunch = newMeal, changeHistory = updatedChangeHistory)
                "dinner" -> mealPlan.copy(dinner = newMeal, changeHistory = updatedChangeHistory)
                else -> return MealResult.Error("Geçersiz öğün tipi")
            }

            val newTotalCalories = (updatedMealPlan.breakfast?.totalCalories ?: 0) +
                    (updatedMealPlan.lunch?.totalCalories ?: 0) +
                    (updatedMealPlan.dinner?.totalCalories ?: 0)

            val finalMealPlan = updatedMealPlan.copy(totalCalories = newTotalCalories)

            docRef.set(finalMealPlan).await()
            MealResult.Success(finalMealPlan)
        } catch (e: Exception) {
            MealResult.Error(e.message ?: "Öğün değiştirilemedi")
        }
    }

    suspend fun getMealHistory(mealType: String, limit: Int = 30): List<String> {
        return try {
            val userId = currentUserId ?: return emptyList()

            val snapshot = firestore.collection("meal_plans")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val mealItems = mutableListOf<String>()

            snapshot.documents.forEach { doc ->
                val mealPlan = doc.toObject(MealPlan::class.java)
                when (mealType) {
                    "breakfast" -> mealPlan?.breakfast?.items?.forEach { mealItems.add(it.name) }
                    "lunch" -> mealPlan?.lunch?.items?.forEach { mealItems.add(it.name) }
                    "dinner" -> mealPlan?.dinner?.items?.forEach { mealItems.add(it.name) }
                }
            }

            mealItems.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun canChangeMealToday(mealPlan: MealPlan, mealType: String): Boolean {
        val changeTimestamp = when (mealType) {
            "breakfast" -> mealPlan.changeHistory.breakfast
            "lunch" -> mealPlan.changeHistory.lunch
            "dinner" -> mealPlan.changeHistory.dinner
            else -> 0L
        }

        if (changeTimestamp == 0L) return true

        val changeDate = java.time.Instant.ofEpochMilli(changeTimestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()

        val today = java.time.LocalDate.now()

        return changeDate.isBefore(today)
    }
    suspend fun getPreviousMeals(limit: Int = 30): List<String> {
        return try {
            val userId = currentUserId ?: return emptyList()

            val snapshot = firestore.collection("meal_plans")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val allMealItems = mutableListOf<String>()

            snapshot.documents.forEach { doc ->
                val mealPlan = doc.toObject(MealPlan::class.java)
                mealPlan?.breakfast?.items?.forEach { allMealItems.add(it.name) }
                mealPlan?.lunch?.items?.forEach { allMealItems.add(it.name) }
                mealPlan?.dinner?.items?.forEach { allMealItems.add(it.name) }
            }

            allMealItems.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markMealAsCompleted(mealPlanId: String, mealType: String, isCompleted: Boolean): MealResult {
        return try {
            val docRef = firestore.collection("meal_plans").document(mealPlanId)

            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                return MealResult.Error("Öğün planı bulunamadı")
            }

            val currentPlan = snapshot.toObject(MealPlan::class.java)
                ?: return MealResult.Error("Plan parse edilemedi")

            val updatedPlan = when (mealType) {
                "breakfast" -> {
                    val newBreakfast = currentPlan.breakfast?.copy(
                        isCompleted = isCompleted,
                        completedAt = if (isCompleted) System.currentTimeMillis() else null
                    )
                    currentPlan.copy(breakfast = newBreakfast)
                }
                "lunch" -> {
                    val newLunch = currentPlan.lunch?.copy(
                        isCompleted = isCompleted,
                        completedAt = if (isCompleted) System.currentTimeMillis() else null
                    )
                    currentPlan.copy(lunch = newLunch)
                }
                "dinner" -> {
                    val newDinner = currentPlan.dinner?.copy(
                        isCompleted = isCompleted,
                        completedAt = if (isCompleted) System.currentTimeMillis() else null
                    )
                    currentPlan.copy(dinner = newDinner)
                }
                else -> {
                    return MealResult.Error("Geçersiz öğün tipi")
                }
            }
            docRef.set(updatedPlan).await()
            kotlinx.coroutines.delay(500)
            val verifySnapshot = docRef.get().await()
            val verifiedPlan = verifySnapshot.toObject(MealPlan::class.java)
            MealResult.Success(verifiedPlan)
        } catch (e: Exception) {
            e.printStackTrace()
            MealResult.Error(e.message ?: "Öğün durumu güncellenemedi")
        }
    }

    suspend fun updateMeal(mealPlanId: String, mealType: String, updatedMeal: com.example.fitbalance.data.Meal): MealResult {
        return try {
            val userId = currentUserId ?: return MealResult.Error("Kullanıcı bulunamadı")

            val docRef = firestore.collection("meal_plans").document(mealPlanId)
            val snapshot = docRef.get().await()
            val mealPlan = snapshot.toObject(MealPlan::class.java)
                ?: return MealResult.Error("Öğün planı bulunamadı")

            val updatedMealPlan = when (mealType) {
                "breakfast" -> mealPlan.copy(breakfast = updatedMeal)
                "lunch" -> mealPlan.copy(lunch = updatedMeal)
                "dinner" -> mealPlan.copy(dinner = updatedMeal)
                else -> return MealResult.Error("Geçersiz öğün tipi")
            }

            val newTotalCalories = (updatedMealPlan.breakfast?.totalCalories ?: 0) +
                    (updatedMealPlan.lunch?.totalCalories ?: 0) +
                    (updatedMealPlan.dinner?.totalCalories ?: 0)

            val finalMealPlan = updatedMealPlan.copy(totalCalories = newTotalCalories)

            docRef.set(finalMealPlan).await()
            MealResult.Success(finalMealPlan)
        } catch (e: Exception) {
            MealResult.Error(e.message ?: "Öğün güncellenemedi")
        }
    }
}

sealed class MealResult {
    data class Success(val mealPlan: MealPlan?) : MealResult()
    data class Error(val message: String) : MealResult()
}