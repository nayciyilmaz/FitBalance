package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.data.SignInUiState
import com.example.fitbalance.data.SignInValidationErrors
import com.example.fitbalance.repository.AuthRepository
import com.example.fitbalance.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var inputMail by mutableStateOf("")
        private set

    var inputPassword by mutableStateOf("")
        private set

    var uiState by mutableStateOf(SignInUiState())
        private set

    fun updateMail(mail: String) {
        inputMail = mail
        if (uiState.errorMessage != null) {
            uiState = uiState.copy(errorMessage = null)
        }
        uiState = uiState.copy(
            validationErrors = uiState.validationErrors.copy(emailError = null)
        )
    }

    fun updatePassword(password: String) {
        inputPassword = password
        if (uiState.errorMessage != null) {
            uiState = uiState.copy(errorMessage = null)
        }
        uiState = uiState.copy(
            validationErrors = uiState.validationErrors.copy(passwordError = null)
        )
    }

    fun signIn(onSuccess: () -> Unit) {
        val validationErrors = validateInputs()
        if (validationErrors.hasErrors()) {
            uiState = uiState.copy(validationErrors = validationErrors)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            when (val result = authRepository.signIn(inputMail.trim(), inputPassword)) {
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

    private fun validateInputs(): SignInValidationErrors {
        var errors = SignInValidationErrors()

        if (inputMail.trim().isEmpty()) {
            errors = errors.copy(emailError = "E-posta boş bırakılamaz")
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(inputMail.trim()).matches()) {
            errors = errors.copy(emailError = "Geçerli bir e-posta adresi girin")
        }

        if (inputPassword.isEmpty()) {
            errors = errors.copy(passwordError = "Şifre boş bırakılamaz")
        } else if (inputPassword.length < 6) {
            errors = errors.copy(passwordError = "Şifre en az 6 karakter olmalı")
        }

        return errors
    }
}

private fun SignInValidationErrors.hasErrors(): Boolean {
    return emailError != null || passwordError != null
}