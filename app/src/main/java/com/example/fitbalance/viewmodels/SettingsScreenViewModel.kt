package com.example.fitbalance.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.R
import com.example.fitbalance.data.UserData
import com.example.fitbalance.data.WeightEntry
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var userData by mutableStateOf<UserData?>(null)
        private set

    var inputName by mutableStateOf("")
    var inputSurname by mutableStateOf("")
    var inputAge by mutableStateOf("")
    var inputHeight by mutableStateOf("")
    var inputWeight by mutableStateOf("")
    var inputGoal by mutableStateOf("")
    var inputCurrentPassword by mutableStateOf("")
    var inputNewPassword by mutableStateOf("")
    var inputNewPasswordConfirm by mutableStateOf("")

    var nameError by mutableStateOf<String?>(null)
        private set
    var surnameError by mutableStateOf<String?>(null)
        private set
    var ageError by mutableStateOf<String?>(null)
        private set
    var heightError by mutableStateOf<String?>(null)
        private set
    var weightError by mutableStateOf<String?>(null)
        private set
    var goalError by mutableStateOf<String?>(null)
        private set
    var currentPasswordError by mutableStateOf<String?>(null)
        private set
    var newPasswordError by mutableStateOf<String?>(null)
        private set
    var newPasswordConfirmError by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var currentPasswordVisible by mutableStateOf(false)
    var newPasswordVisible by mutableStateOf(false)
    var confirmPasswordVisible by mutableStateOf(false)

    var personalInfoExpanded by mutableStateOf(false)
    var physicalInfoExpanded by mutableStateOf(false)
    var passwordExpanded by mutableStateOf(false)

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: return@launch

            try {
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                userData = userDoc.toObject(UserData::class.java)
                userData?.let { user ->
                    inputName = user.name
                    inputSurname = user.surname
                    inputAge = user.age.toString()
                    inputHeight = user.height.toInt().toString()
                    inputWeight = user.currentWeight.toInt().toString()
                    inputGoal = user.goal
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateName(name: String) {
        inputName = name
        nameError = null
    }

    fun updateSurname(surname: String) {
        inputSurname = surname
        surnameError = null
    }

    fun updateAge(age: String) {
        inputAge = age
        ageError = null
    }

    fun updateHeight(height: String) {
        inputHeight = height
        heightError = null
    }

    fun updateWeight(weight: String) {
        inputWeight = weight
        weightError = null
    }

    fun updateGoal(goal: String) {
        inputGoal = goal
        goalError = null
    }

    fun updateCurrentPassword(password: String) {
        inputCurrentPassword = password
        currentPasswordError = null
    }

    fun updateNewPassword(password: String) {
        inputNewPassword = password
        newPasswordError = null
    }

    fun updateNewPasswordConfirm(password: String) {
        inputNewPasswordConfirm = password
        newPasswordConfirmError = null
    }

    fun toggleCurrentPasswordVisibility() {
        currentPasswordVisible = !currentPasswordVisible
    }

    fun toggleNewPasswordVisibility() {
        newPasswordVisible = !newPasswordVisible
    }

    fun toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible
    }

    fun togglePersonalInfoExpanded() {
        personalInfoExpanded = !personalInfoExpanded
    }

    fun togglePhysicalInfoExpanded() {
        physicalInfoExpanded = !physicalInfoExpanded
    }

    fun togglePasswordExpanded() {
        passwordExpanded = !passwordExpanded
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!validateInputs()) return@launch

            isLoading = true

            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                isLoading = false
                return@launch
            }

            try {
                val currentUserData = userData ?: return@launch

                val weightChanged = inputWeight.toDouble() != currentUserData.currentWeight
                val newWeightHistory = if (weightChanged) {
                    currentUserData.weightHistory + WeightEntry(
                        weight = inputWeight.toDouble(),
                        date = System.currentTimeMillis()
                    )
                } else {
                    currentUserData.weightHistory
                }

                val updatedUserData = currentUserData.copy(
                    name = inputName.trim(),
                    surname = inputSurname.trim(),
                    age = inputAge.toInt(),
                    height = inputHeight.toDouble(),
                    weightHistory = newWeightHistory,
                    goal = inputGoal
                )

                firestore.collection("users")
                    .document(userId)
                    .set(updatedUserData)
                    .await()

                if (inputCurrentPassword.isNotEmpty() &&
                    inputNewPassword.isNotEmpty() &&
                    inputNewPasswordConfirm.isNotEmpty()) {
                    updatePassword()
                }

                isLoading = false
                onSuccess()
            } catch (e: Exception) {
                isLoading = false
                e.printStackTrace()
            }
        }
    }

    private suspend fun updatePassword() {
        try {
            val user = firebaseAuth.currentUser ?: return
            val credential = EmailAuthProvider.getCredential(
                user.email ?: "",
                inputCurrentPassword
            )

            user.reauthenticate(credential).await()
            user.updatePassword(inputNewPassword).await()

            inputCurrentPassword = ""
            inputNewPassword = ""
            inputNewPasswordConfirm = ""
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            currentPasswordError = "Mevcut şifre hatalı"
        } catch (e: Exception) {
            currentPasswordError = "Şifre güncellenemedi: ${e.message}"
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (inputName.trim().isEmpty()) {
            nameError = context.getString(R.string.error_name_empty)
            isValid = false
        } else if (inputName.trim().length < 2) {
            nameError = context.getString(R.string.error_name_min_length)
            isValid = false
        } else if (!inputName.trim().all { it.isLetter() || it.isWhitespace() }) {
            nameError = context.getString(R.string.error_name_letters_only)
            isValid = false
        }

        if (inputSurname.trim().isEmpty()) {
            surnameError = context.getString(R.string.error_surname_empty)
            isValid = false
        } else if (inputSurname.trim().length < 2) {
            surnameError = context.getString(R.string.error_surname_min_length)
            isValid = false
        } else if (!inputSurname.trim().all { it.isLetter() || it.isWhitespace() }) {
            surnameError = context.getString(R.string.error_surname_letters_only)
            isValid = false
        }

        if (inputAge.trim().isEmpty()) {
            ageError = context.getString(R.string.error_age_empty)
            isValid = false
        } else {
            val age = inputAge.toIntOrNull()
            if (age == null) {
                ageError = context.getString(R.string.error_age_invalid)
                isValid = false
            } else if (age < 10 || age > 120) {
                ageError = context.getString(R.string.error_age_range)
                isValid = false
            }
        }

        if (inputHeight.trim().isEmpty()) {
            heightError = context.getString(R.string.error_height_empty)
            isValid = false
        } else {
            val height = inputHeight.toDoubleOrNull()
            if (height == null) {
                heightError = context.getString(R.string.error_height_invalid)
                isValid = false
            } else if (height < 50 || height > 300) {
                heightError = context.getString(R.string.error_height_range)
                isValid = false
            }
        }

        if (inputWeight.trim().isEmpty()) {
            weightError = context.getString(R.string.error_weight_empty)
            isValid = false
        } else {
            val weight = inputWeight.toDoubleOrNull()
            if (weight == null) {
                weightError = context.getString(R.string.error_weight_invalid)
                isValid = false
            } else if (weight < 20 || weight > 500) {
                weightError = context.getString(R.string.error_weight_range)
                isValid = false
            }
        }

        if (inputGoal.isEmpty()) {
            goalError = context.getString(R.string.error_goal_empty)
            isValid = false
        }

        val isPasswordFieldsFilled = inputCurrentPassword.isNotEmpty() ||
                inputNewPassword.isNotEmpty() ||
                inputNewPasswordConfirm.isNotEmpty()

        if (isPasswordFieldsFilled) {
            if (inputCurrentPassword.isEmpty()) {
                currentPasswordError = context.getString(R.string.error_password_empty)
                isValid = false
            }

            if (inputNewPassword.isEmpty()) {
                newPasswordError = context.getString(R.string.error_password_empty)
                isValid = false
            } else if (inputNewPassword.length < 6) {
                newPasswordError = context.getString(R.string.error_password_min_length)
                isValid = false
            } else if (!inputNewPassword.any { it.isDigit() }) {
                newPasswordError = context.getString(R.string.error_password_needs_digit)
                isValid = false
            } else if (!inputNewPassword.any { it.isLetter() }) {
                newPasswordError = context.getString(R.string.error_password_needs_letter)
                isValid = false
            }

            if (inputNewPasswordConfirm.isEmpty()) {
                newPasswordConfirmError = context.getString(R.string.error_password_empty)
                isValid = false
            } else if (inputNewPassword != inputNewPasswordConfirm) {
                newPasswordConfirmError = "Şifreler eşleşmiyor"
                isValid = false
            }
        }

        return isValid
    }

    fun refreshUserData() {
        loadUserData()
    }
}