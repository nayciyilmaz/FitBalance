package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var userData by mutableStateOf<UserData?>(null)
        private set

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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        firebaseAuth.signOut()
        onSuccess()
    }

    fun refreshUserData() {
        loadUserData()
    }
}