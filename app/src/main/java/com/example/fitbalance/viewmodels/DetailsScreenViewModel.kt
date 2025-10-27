package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.data.Meal
import com.example.fitbalance.data.MealItem
import com.example.fitbalance.repository.MealRepository
import com.example.fitbalance.repository.MealResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsScreenViewModel @Inject constructor(
    private val mealRepository: MealRepository
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