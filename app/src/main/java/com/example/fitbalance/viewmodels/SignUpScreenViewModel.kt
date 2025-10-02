package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.data.SignUpUiState
import com.example.fitbalance.data.UserData
import com.example.fitbalance.data.ValidationErrors
import com.example.fitbalance.repository.AuthRepository
import com.example.fitbalance.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val userData = UserData(
                name = inputName.trim(),
                surname = inputSurname.trim(),
                email = inputMail.trim(),
                height = inputHeight.toDouble(),
                weight = inputWeight.toDouble(),
                age = inputAge.toInt(),
                gender = inputGender,
                goal = inputGoal
            )

            when (val result = authRepository.signUp(inputMail.trim(), inputPassword, userData)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun validateInputs(): ValidationErrors {
        var errors = ValidationErrors()

        if (inputName.trim().isEmpty()) {
            errors = errors.copy(nameError = "İsim boş bırakılamaz")
        } else if (inputName.trim().length < 2) {
            errors = errors.copy(nameError = "İsim en az 2 karakter olmalı")
        } else if (!inputName.trim().all { it.isLetter() || it.isWhitespace() }) {
            errors = errors.copy(nameError = "İsim sadece harf içermelidir")
        }

        if (inputSurname.trim().isEmpty()) {
            errors = errors.copy(surnameError = "Soyisim boş bırakılamaz")
        } else if (inputSurname.trim().length < 2) {
            errors = errors.copy(surnameError = "Soyisim en az 2 karakter olmalı")
        } else if (!inputSurname.trim().all { it.isLetter() || it.isWhitespace() }) {
            errors = errors.copy(surnameError = "Soyisim sadece harf içermelidir")
        }

        if (inputMail.trim().isEmpty()) {
            errors = errors.copy(emailError = "E-posta boş bırakılamaz")
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(inputMail.trim()).matches()) {
            errors = errors.copy(emailError = "Geçerli bir e-posta adresi girin")
        }

        if (inputPassword.isEmpty()) {
            errors = errors.copy(passwordError = "Şifre boş bırakılamaz")
        } else if (inputPassword.length < 6) {
            errors = errors.copy(passwordError = "Şifre en az 6 karakter olmalı")
        } else if (!inputPassword.any { it.isDigit() }) {
            errors = errors.copy(passwordError = "Şifre en az bir rakam içermelidir")
        } else if (!inputPassword.any { it.isLetter() }) {
            errors = errors.copy(passwordError = "Şifre en az bir harf içermelidir")
        }

        if (inputHeight.trim().isEmpty()) {
            errors = errors.copy(heightError = "Boy boş bırakılamaz")
        } else {
            val height = inputHeight.toDoubleOrNull()
            if (height == null) {
                errors = errors.copy(heightError = "Geçerli bir sayı girin")
            } else if (height < 50 || height > 300) {
                errors = errors.copy(heightError = "Boy 50-300 cm arasında olmalı")
            }
        }

        if (inputWeight.trim().isEmpty()) {
            errors = errors.copy(weightError = "Kilo boş bırakılamaz")
        } else {
            val weight = inputWeight.toDoubleOrNull()
            if (weight == null) {
                errors = errors.copy(weightError = "Geçerli bir sayı girin")
            } else if (weight < 20 || weight > 500) {
                errors = errors.copy(weightError = "Kilo 20-500 kg arasında olmalı")
            }
        }

        if (inputAge.trim().isEmpty()) {
            errors = errors.copy(ageError = "Yaş boş bırakılamaz")
        } else {
            val age = inputAge.toIntOrNull()
            if (age == null) {
                errors = errors.copy(ageError = "Geçerli bir sayı girin")
            } else if (age < 10 || age > 120) {
                errors = errors.copy(ageError = "Yaş 10-120 arasında olmalı")
            }
        }

        if (inputGender.isEmpty()) {
            errors = errors.copy(genderError = "Cinsiyet seçiniz")
        }

        if (inputGoal.isEmpty()) {
            errors = errors.copy(goalError = "Hedef seçiniz")
        }

        return errors
    }
}

private fun ValidationErrors.hasErrors(): Boolean {
    return nameError != null || surnameError != null || emailError != null ||
            passwordError != null || heightError != null || weightError != null ||
            ageError != null || genderError != null || goalError != null
}