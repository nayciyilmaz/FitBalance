package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.data.Meal
import com.example.fitbalance.data.MealGenerationRequest
import com.example.fitbalance.data.MealPlan
import com.example.fitbalance.data.UserData
import com.example.fitbalance.repository.GeminiMealResult
import com.example.fitbalance.repository.GeminiRepository
import com.example.fitbalance.repository.MealRepository
import com.example.fitbalance.repository.MealResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val mealRepository: MealRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var currentDate by mutableStateOf("")
        private set

    var caloriesBurned by mutableIntStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var breakfast by mutableStateOf<Meal?>(null)
        private set

    var lunch by mutableStateOf<Meal?>(null)
        private set

    var dinner by mutableStateOf<Meal?>(null)
        private set

    var currentMealPlanId by mutableStateOf("")
        private set

    var refreshTrigger by mutableIntStateOf(0)
        private set

    val visibleBreakfast: Meal?
        get() = breakfast?.takeIf { !it.isCompleted }

    val visibleLunch: Meal?
        get() = lunch?.takeIf { !it.isCompleted }

    val visibleDinner: Meal?
        get() = dinner?.takeIf { !it.isCompleted }

    val hasAnyVisibleMeal: Boolean
        get() = visibleBreakfast != null || visibleLunch != null || visibleDinner != null

    val hasAnyMealDefined: Boolean
        get() = breakfast != null || lunch != null || dinner != null

    val shouldShowAllMealsCompleted: Boolean
        get() = hasAnyMealDefined && !hasAnyVisibleMeal

    init {
        updateCurrentDate()
        loadTodayMealPlan()
    }

    private fun updateCurrentDate() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM EEEE", Locale("tr"))
        currentDate = today.format(formatter)
    }

    fun loadTodayMealPlan() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            when (val result = mealRepository.getTodayMealPlan()) {
                is MealResult.Success -> {
                    result.mealPlan?.let { plan ->
                        currentMealPlanId = plan.id
                        breakfast = plan.breakfast
                        lunch = plan.lunch
                        dinner = plan.dinner
                        calculateTotalCalories()
                        isLoading = false
                    } ?: run {
                        generateNewMealPlan()
                    }
                }

                is MealResult.Error -> {
                    generateNewMealPlan()
                }
            }
        }
    }
    fun generateNewMealPlan() {
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

                val previousMeals = mealRepository.getPreviousMeals(30)

                val request = MealGenerationRequest(
                    height = userData.height,
                    weight = userData.currentWeight,
                    age = userData.age,
                    gender = userData.gender,
                    goal = userData.goal,
                    previousMeals = previousMeals
                )

                when (val result = geminiRepository.generateMealPlan(request)) {
                    is GeminiMealResult.Success -> {
                        breakfast = result.breakfast
                        lunch = result.lunch
                        dinner = result.dinner

                        val totalCalories = (breakfast?.totalCalories ?: 0) +
                                (lunch?.totalCalories ?: 0) +
                                (dinner?.totalCalories ?: 0)

                        val mealPlan = MealPlan(
                            userId = userId,
                            date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            breakfast = result.breakfast,
                            lunch = result.lunch,
                            dinner = result.dinner,
                            totalCalories = totalCalories
                        )

                        when (val saveResult = mealRepository.saveMealPlan(mealPlan)) {
                            is MealResult.Success -> {
                                currentMealPlanId = saveResult.mealPlan?.id ?: ""
                                calculateTotalCalories()
                                isLoading = false
                            }

                            is MealResult.Error -> {
                                errorMessage = saveResult.message
                                isLoading = false
                            }
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

    suspend fun markMealStatusSync(mealType: String, isCompleted: Boolean) {
        val result = mealRepository.markMealAsCompleted(currentMealPlanId, mealType, isCompleted)

        when (result) {
            is MealResult.Success -> {
                result.mealPlan?.let { plan ->
                    breakfast = plan.breakfast
                    lunch = plan.lunch
                    dinner = plan.dinner

                    calculateTotalCalories()
                    refreshTrigger++
                }
            }

            is MealResult.Error -> {
                errorMessage = result.message
            }
        }
    }

    fun updateMeal(mealType: String, updatedMeal: Meal) {
        when (mealType) {
            "breakfast" -> breakfast = updatedMeal
            "lunch" -> lunch = updatedMeal
            "dinner" -> dinner = updatedMeal
        }
        calculateTotalCalories()
        refreshTrigger++
    }

    private fun calculateTotalCalories() {
        var total = 0

        breakfast?.let { meal ->
            if (meal.isCompleted) {
                total += meal.totalCalories
            }
        }

        lunch?.let { meal ->
            if (meal.isCompleted) {
                total += meal.totalCalories
            }
        }

        dinner?.let { meal ->
            if (meal.isCompleted) {
                total += meal.totalCalories
            }
        }

        caloriesBurned = total
    }
}