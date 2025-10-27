package com.example.fitbalance.data

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.PropertyName

data class UserData(
    val uid: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val age: Int = 0,
    val gender: String = "",
    val goal: String = ""
)

data class SignUpUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val validationErrors: ValidationErrors = ValidationErrors()
)

data class ValidationErrors(
    val nameError: String? = null,
    val surnameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val heightError: String? = null,
    val weightError: String? = null,
    val ageError: String? = null,
    val genderError: String? = null,
    val goalError: String? = null
)

data class SignInUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val validationErrors: SignInValidationErrors = SignInValidationErrors()
)

data class SignInValidationErrors(
    val emailError: String? = null,
    val passwordError: String? = null
)

data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

data class MealPlan(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val breakfast: Meal? = null,
    val lunch: Meal? = null,
    val dinner: Meal? = null,
    val totalCalories: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Meal(
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("items") @set:PropertyName("items")
    var items: List<MealItem> = emptyList(),

    @get:PropertyName("totalCalories") @set:PropertyName("totalCalories")
    var totalCalories: Int = 0,

    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,

    @get:PropertyName("completedAt") @set:PropertyName("completedAt")
    var completedAt: Long? = null
) {
    constructor() : this("", emptyList(), 0, false, null)
}

data class MealItem(
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("calories") @set:PropertyName("calories")
    var calories: Int = 0
) {
    constructor() : this("", 0)
}

data class MealGenerationRequest(
    val height: Double,
    val weight: Double,
    val age: Int,
    val gender: String,
    val goal: String,
    val previousMeals: List<String> = emptyList()
)