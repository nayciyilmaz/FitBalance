package com.example.fitbalance.viewmodels

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.data.Meal
import com.example.fitbalance.data.MealGenerationRequest
import com.example.fitbalance.data.MealItem
import com.example.fitbalance.data.UserData
import com.example.fitbalance.repository.GeminiMealResult
import com.example.fitbalance.repository.GeminiRepository
import com.example.fitbalance.repository.MealRepository
import com.example.fitbalance.repository.MealResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DetailsScreenViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val geminiRepository: GeminiRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var isEditing by mutableStateOf(false)
        private set

    var editableMealItems by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var currentMealPlanId by mutableStateOf("")
        private set

    var currentMealType by mutableStateOf("")
        private set

    var canChangeToday by mutableStateOf(true)
        private set

    fun startEditing(items: List<Pair<String, Int>>, mealPlanId: String, mealType: String) {
        isEditing = true
        currentMealPlanId = mealPlanId
        currentMealType = mealType
        editableMealItems = items.map { it.first to it.second.toString() }
    }

    fun cancelEditing() {
        isEditing = false
        editableMealItems = emptyList()
        currentMealPlanId = ""
        currentMealType = ""
    }

    fun confirmEditing(onSuccess: (Meal) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val mealItems = editableMealItems.mapNotNull { (name, calories) ->
                val calorieValue = calories.toIntOrNull()
                if (name.isNotBlank() && calorieValue != null && calorieValue > 0) {
                    MealItem(name = name, calories = calorieValue)
                } else null
            }

            if (mealItems.isEmpty()) {
                errorMessage = "En az bir yiyecek eklemelisiniz"
                isLoading = false
                return@launch
            }

            val updatedMeal = Meal(
                name = "",
                items = mealItems,
                totalCalories = mealItems.sumOf { it.calories },
                isCompleted = false
            )

            when (val result = mealRepository.updateMeal(currentMealPlanId, currentMealType, updatedMeal)) {
                is MealResult.Success -> {
                    isEditing = false
                    editableMealItems = emptyList()
                    isLoading = false
                    result.mealPlan?.let { plan ->
                        val meal = when (currentMealType) {
                            "breakfast" -> plan.breakfast
                            "lunch" -> plan.lunch
                            "dinner" -> plan.dinner
                            else -> null
                        }
                        meal?.let { onSuccess(it) }
                    }
                }
                is MealResult.Error -> {
                    errorMessage = result.message
                    isLoading = false
                }
            }
        }
    }

    fun changeMeal(mealPlanId: String, mealType: String, onSuccess: (Meal) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                errorMessage = "Kullanıcı bulunamadı"
                isLoading = false
                return@launch
            }

            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val userData = userDoc.toObject(UserData::class.java)

                if (userData == null) {
                    errorMessage = "Kullanıcı verileri bulunamadı"
                    isLoading = false
                    return@launch
                }

                val previousMealNames = mealRepository.getMealHistory(mealType, 30)

                val request = MealGenerationRequest(
                    height = userData.height,
                    weight = userData.currentWeight,
                    age = userData.age,
                    gender = userData.gender,
                    goal = userData.goal,
                    previousMeals = emptyList()
                )

                when (val result = geminiRepository.generateSingleMeal(request, mealType, previousMealNames)) {
                    is GeminiMealResult.Success -> {
                        val newMeal = when (mealType) {
                            "breakfast" -> result.breakfast
                            "lunch" -> result.lunch
                            "dinner" -> result.dinner
                            else -> null
                        }

                        if (newMeal != null && newMeal.items.isNotEmpty()) {
                            when (val changeResult = mealRepository.changeMeal(mealPlanId, mealType, newMeal)) {
                                is MealResult.Success -> {
                                    canChangeToday = false
                                    isLoading = false
                                    onSuccess(newMeal)
                                }
                                is MealResult.Error -> {
                                    errorMessage = changeResult.message
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "Öğün oluşturulamadı"
                            isLoading = false
                        }
                    }
                    is GeminiMealResult.Error -> {
                        errorMessage = result.message
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Bir hata oluştu: ${e.message}"
                isLoading = false
            }
        }
    }

    fun checkCanChangeToday(mealPlanId: String, mealType: String) {
        viewModelScope.launch {
            try {
                val docRef = firestore.collection("meal_plans").document(mealPlanId)
                val snapshot = docRef.get().await()
                val mealPlan = snapshot.toObject(com.example.fitbalance.data.MealPlan::class.java)

                mealPlan?.let {
                    canChangeToday = mealRepository.canChangeMealToday(it, mealType)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun shareMeal(mealTitle: String, mealItems: List<Pair<String, Int>>) {
        val itemsText = mealItems.joinToString("\n") { (name, calories) ->
            "• $name - $calories kcal"
        }
        val totalCalories = mealItems.sumOf { it.second }

        val summary = """
            $mealTitle
            
            $itemsText
            
            Toplam Kalori: $totalCalories kcal
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, mealTitle)
            putExtra(Intent.EXTRA_TEXT, summary)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(
            Intent.createChooser(intent, "Öğünü Paylaş").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    fun updateItemName(index: Int, newName: String) {
        editableMealItems = editableMealItems.toMutableList().apply {
            this[index] = this[index].copy(first = newName)
        }
    }

    fun updateItemCalories(index: Int, newCalories: String) {
        editableMealItems = editableMealItems.toMutableList().apply {
            this[index] = this[index].copy(second = newCalories)
        }
    }

    fun removeItem(index: Int) {
        editableMealItems = editableMealItems.toMutableList().apply {
            removeAt(index)
        }
    }

    fun addNewItem() {
        editableMealItems = editableMealItems.toMutableList().apply {
            add("" to "")
        }
    }

    fun getTotalCalories(): Int {
        return editableMealItems.sumOf {
            it.second.toIntOrNull() ?: 0
        }
    }
}