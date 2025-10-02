package com.example.fitbalance.data

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