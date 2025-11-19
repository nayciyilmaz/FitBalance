package com.example.fitbalance.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.R
import com.example.fitbalance.data.SignInUiState
import com.example.fitbalance.data.SignInValidationErrors
import com.example.fitbalance.repository.AuthRepository
import com.example.fitbalance.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
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
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                validationErrors = SignInValidationErrors()
            )

            when (val result = authRepository.signIn(inputMail.trim(), inputPassword)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null,
                        validationErrors = SignInValidationErrors()
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

    private fun mapFirebaseErrorToValidation(errorMessage: String?): SignInValidationErrors {
        return when {
            errorMessage?.contains("user-not-found", ignoreCase = true) == true ||
                    errorMessage?.contains("no user record", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_USER_NOT_FOUND", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_user_not_found)
                )
            }

            errorMessage?.contains("wrong-password", ignoreCase = true) == true ||
                    errorMessage?.contains("password is invalid", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_WRONG_PASSWORD", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    passwordError = context.getString(R.string.error_wrong_password)
                )
            }

            errorMessage?.contains("user-disabled", ignoreCase = true) == true ||
                    errorMessage?.contains("account has been disabled", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_USER_DISABLED", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_user_disabled)
                )
            }

            errorMessage?.contains("too-many-requests", ignoreCase = true) == true ||
                    errorMessage?.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) == true ||
                    errorMessage?.contains("too many", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_too_many_requests),
                    passwordError = context.getString(R.string.error_too_many_requests)
                )
            }

            errorMessage?.contains("network", ignoreCase = true) == true ||
                    errorMessage?.contains("connection", ignoreCase = true) == true ||
                    errorMessage?.contains("ERROR_NETWORK_REQUEST_FAILED", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_network_request_failed),
                    passwordError = context.getString(R.string.error_network_request_failed)
                )
            }

            errorMessage?.contains("invalid-credential", ignoreCase = true) == true ||
                    errorMessage?.contains("invalid credential", ignoreCase = true) == true ||
                    errorMessage?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_invalid_credential),
                    passwordError = context.getString(R.string.error_invalid_credential)
                )
            }

            else -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_sign_in_failed),
                    passwordError = context.getString(R.string.error_sign_in_failed)
                )
            }
        }
    }

    private fun validateInputs(): SignInValidationErrors {
        var errors = SignInValidationErrors()

        if (inputMail.trim().isEmpty()) {
            errors = errors.copy(emailError = context.getString(R.string.error_email_empty))
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(inputMail.trim()).matches()) {
            errors = errors.copy(emailError = context.getString(R.string.error_email_invalid))
        }

        if (inputPassword.isEmpty()) {
            errors = errors.copy(passwordError = context.getString(R.string.error_password_empty))
        } else if (inputPassword.length < 6) {
            errors = errors.copy(passwordError = context.getString(R.string.error_password_min_length))
        }

        return errors
    }
}

private fun SignInValidationErrors.hasErrors(): Boolean {
    return emailError != null || passwordError != null
}