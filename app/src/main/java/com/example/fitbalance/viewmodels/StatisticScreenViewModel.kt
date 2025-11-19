package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.data.MealPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class StatisticScreenViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var selectedDateMealPlan by mutableStateOf<MealPlan?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var visibleMonth by mutableStateOf(YearMonth.now())
        private set

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    fun updateVisibleMonth(month: YearMonth) {
        visibleMonth = month
    }

    fun updateSelectedDate(date: LocalDate) {
        selectedDate = date
    }

    fun loadMealPlanForDate(date: LocalDate) {
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
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val snapshot = firestore.collection("meal_plans")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", dateString)
                    .limit(1)
                    .get()
                    .await()

                if (snapshot.documents.isNotEmpty()) {
                    val doc = snapshot.documents[0]
                    selectedDateMealPlan = doc.toObject(MealPlan::class.java)
                } else {
                    selectedDateMealPlan = null
                }

                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Veriler yüklenemedi: ${e.message}"
                isLoading = false
                e.printStackTrace()
            }
        }
    }

    fun getTotalCaloriesForDate(): Int {
        val mealPlan = selectedDateMealPlan ?: return 0
        var total = 0

        mealPlan.breakfast?.let {
            if (it.isCompleted) total += it.totalCalories
        }

        mealPlan.lunch?.let {
            if (it.isCompleted) total += it.totalCalories
        }

        mealPlan.dinner?.let {
            if (it.isCompleted) total += it.totalCalories
        }

        return total
    }
}