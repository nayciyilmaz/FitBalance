package com.example.fitbalance.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.R
import com.example.fitbalance.data.SignUpUiState
import com.example.fitbalance.data.UserData
import com.example.fitbalance.data.ValidationErrors
import com.example.fitbalance.data.WeightEntry
import com.example.fitbalance.repository.AuthRepository
import com.example.fitbalance.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var inputName by mutableStateOf("")
        private set

    var inputSurname by mutableStateOf("")
        private set

    var inputMail by mutableStateOf("")
        private set

    var inputPassword by mutableStateOf("")
        private set

    var inputHeight by mutableStateOf("")
        private set

    var inputWeight by mutableStateOf("")
        private set

    var inputAge by mutableStateOf("")
        private set

    var inputGender by mutableStateOf("")
        private set

    var inputGoal by mutableStateOf("")
        private set

    var uiState by mutableStateOf(SignUpUiState())
        private set

    fun updateName(name: String) {
        inputName = name
        clearFieldError { it.copy(nameError = null) }
    }

    fun updateSurname(surname: String) {
        inputSurname = surname
        clearFieldError { it.copy(surnameError = null) }
    }

    fun updateMail(mail: String) {
        inputMail = mail
        clearFieldError { it.copy(emailError = null) }
    }

    fun updatePassword(password: String) {
        inputPassword = password
        clearFieldError { it.copy(passwordError = null) }
    }

    fun updateHeight(height: String) {
        inputHeight = height
        clearFieldError { it.copy(heightError = null) }
    }

    fun updateWeight(weight: String) {
        inputWeight = weight
        clearFieldError { it.copy(weightError = null) }
    }

    fun updateAge(age: String) {
        inputAge = age
        clearFieldError { it.copy(ageError = null) }
    }

    fun updateGender(gender: String) {
        inputGender = gender
        clearFieldError { it.copy(genderError = null) }
    }

    fun updateGoal(goal: String) {
        inputGoal = goal
        clearFieldError { it.copy(goalError = null) }
    }

    private fun clearFieldError(update: (ValidationErrors) -> ValidationErrors) {
        if (uiState.errorMessage != null) {
            uiState = uiState.copy(errorMessage = null)
        }
        uiState = uiState.copy(validationErrors = update(uiState.validationErrors))
    }

    fun signUp(onSuccess: () -> Unit) {
        val validationErrors = validateInputs()
        if (validationErrors.hasErrors()) {
            uiState = uiState.copy(validationErrors = validationErrors)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                validationErrors = ValidationErrors()
            )

            val initialWeightEntry = WeightEntry(
                weight = inputWeight.toDouble(),
                date = System.currentTimeMillis()
            )

            val userData = UserData(
                name = inputName.trim(),
                surname = inputSurname.trim(),
                email = inputMail.trim(),
                height = inputHeight.toDouble(),
                weightHistory = listOf(initialWeightEntry),
                age = inputAge.toInt(),
                gender = inputGender,
                goal = inputGoal
            )

            when (val result = authRepository.signUp(inputMail.trim(), inputPassword, userData)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null,
                        validationErrors = ValidationErrors()
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    val validationErrors = mapFirebaseErrorToValidation(result.message)
                    uiState = uiState.copy(
                        isLoading = false,
                        validationErrors = validationErrors
                    )
                }
            }
        }
    }

    private fun mapFirebaseErrorToValidation(errorMessage: String?): ValidationErrors {
        return when {
            errorMessage?.contains("email-already-in-use", ignoreCase = true) == true ||
                    errorMessage?.contains("email address is already", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true) == true -> {
                ValidationErrors(
                    emailError = context.getString(R.string.error_email_already_in_use)
                )
            }

            errorMessage?.contains("weak-password", ignoreCase = true) == true ||
                    errorMessage?.contains("password should be at least", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_WEAK_PASSWORD", ignoreCase = true) == true -> {
                ValidationErrors(
                    passwordError = context.getString(R.string.error_weak_password)
                )
            }

            errorMessage?.contains("invalid-email", ignoreCase = true) == true ||
                    errorMessage?.contains("badly formatted", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_INVALID_EMAIL", ignoreCase = true) == true -> {
                ValidationErrors(
                    emailError = context.getString(R.string.error_email_invalid_firebase)
                )
            }

            errorMessage?.contains("user-disabled", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_USER_DISABLED", ignoreCase = true) == true -> {
                ValidationErrors(
                    emailError = context.getString(R.string.error_user_disabled)
                )
            }

            errorMessage?.contains("too-many-requests", ignoreCase = true) == true ||
                    errorMessage?.contains("too many", ignoreCase = true) == true -> {
                ValidationErrors(
                    emailError = context.getString(R.string.error_too_many_requests),
                    passwordError = context.getString(R.string.error_too_many_requests)
                )
            }

            errorMessage?.contains("operation-not-allowed", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_OPERATION_NOT_ALLOWED", ignoreCase = true) == true -> {
                ValidationErrors(
                    emailError = context.getString(R.string.error_operation_not_allowed),
                    passwordError = context.getString(R.string.error_operation_not_allowed)
                )
            }

            errorMessage?.contains("network", ignoreCase = true) == true ||
                    errorMessage?.contains("connection", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_NETWORK_REQUEST_FAILED", ignoreCase = true) == true -> {
                ValidationErrors(
                    emailError = context.getString(R.string.error_network_request_failed),
                    passwordError = context.getString(R.string.error_network_request_failed)
                )
            }

            else -> {
                ValidationErrors(
                    emailError = context.getString(R.string.error_signup_failed).format(errorMessage ?: ""),
                    passwordError = context.getString(R.string.error_signup_failed).format(errorMessage ?: "")
                )
            }
        }
    }

    private fun validateInputs(): ValidationErrors {
        var errors = ValidationErrors()

        if (inputName.trim().isEmpty()) {
            errors = errors.copy(nameError = context.getString(R.string.error_name_empty))
        } else if (inputName.trim().length < 2) {
            errors = errors.copy(nameError = context.getString(R.string.error_name_min_length))
        } else if (!inputName.trim().all { it.isLetter() || it.isWhitespace() }) {
            errors = errors.copy(nameError = context.getString(R.string.error_name_letters_only))
        }

        if (inputSurname.trim().isEmpty()) {
            errors = errors.copy(surnameError = context.getString(R.string.error_surname_empty))
        } else if (inputSurname.trim().length < 2) {
            errors = errors.copy(surnameError = context.getString(R.string.error_surname_min_length))
        } else if (!inputSurname.trim().all { it.isLetter() || it.isWhitespace() }) {
            errors = errors.copy(surnameError = context.getString(R.string.error_surname_letters_only))
        }

        if (inputMail.trim().isEmpty()) {
            errors = errors.copy(emailError = context.getString(R.string.error_email_empty))
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(inputMail.trim()).matches()) {
            errors = errors.copy(emailError = context.getString(R.string.error_email_invalid))
        }

        if (inputPassword.isEmpty()) {
            errors = errors.copy(passwordError = context.getString(R.string.error_password_empty))
        } else if (inputPassword.length < 6) {
            errors = errors.copy(passwordError = context.getString(R.string.error_password_min_length))
        } else if (!inputPassword.any { it.isDigit() }) {
            errors = errors.copy(passwordError = context.getString(R.string.error_password_needs_digit))
        } else if (!inputPassword.any { it.isLetter() }) {
            errors = errors.copy(passwordError = context.getString(R.string.error_password_needs_letter))
        }

        if (inputHeight.trim().isEmpty()) {
            errors = errors.copy(heightError = context.getString(R.string.error_height_empty))
        } else {
            val height = inputHeight.toDoubleOrNull()
            if (height == null) {
                errors = errors.copy(heightError = context.getString(R.string.error_height_invalid))
            } else if (height < 50 || height > 300) {
                errors = errors.copy(heightError = context.getString(R.string.error_height_range))
            }
        }

        if (inputWeight.trim().isEmpty()) {
            errors = errors.copy(weightError = context.getString(R.string.error_weight_empty))
        } else {
            val weight = inputWeight.toDoubleOrNull()
            if (weight == null) {
                errors = errors.copy(weightError = context.getString(R.string.error_weight_invalid))
            } else if (weight < 20 || weight > 500) {
                errors = errors.copy(weightError = context.getString(R.string.error_weight_range))
            }
        }

        if (inputAge.trim().isEmpty()) {
            errors = errors.copy(ageError = context.getString(R.string.error_age_empty))
        } else {
            val age = inputAge.toIntOrNull()
            if (age == null) {
                errors = errors.copy(ageError = context.getString(R.string.error_age_invalid))
            } else if (age < 10 || age > 120) {
                errors = errors.copy(ageError = context.getString(R.string.error_age_range))
            }
        }

        if (inputGender.isEmpty()) {
            errors = errors.copy(genderError = context.getString(R.string.error_gender_empty))
        }

        if (inputGoal.isEmpty()) {
            errors = errors.copy(goalError = context.getString(R.string.error_goal_empty))
        }

        return errors
    }
}

private fun ValidationErrors.hasErrors(): Boolean {
    return nameError != null || surnameError != null || emailError != null ||
            passwordError != null || heightError != null || weightError != null ||
            ageError != null || genderError != null || goalError != null
}