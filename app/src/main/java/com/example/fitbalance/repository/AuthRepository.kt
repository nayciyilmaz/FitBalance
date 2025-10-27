package com.example.fitbalance.repository

import android.content.Context
import com.example.fitbalance.R
import com.example.fitbalance.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    suspend fun signUp(
        email: String,
        password: String,
        userData: UserData
    ): AuthResult {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
                ?: return AuthResult.Error(context.getString(R.string.error_user_id_not_found))

            val userDataWithUid = userData.copy(uid = userId)
            firestore.collection("users")
                .document(userId)
                .set(userDataWithUid)
                .await()

            AuthResult.Success
        } catch (e: FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    context.getString(R.string.error_email_already_in_use)
                "ERROR_INVALID_EMAIL" ->
                    context.getString(R.string.error_email_invalid_firebase)
                "ERROR_WEAK_PASSWORD" ->
                    context.getString(R.string.error_weak_password)
                "ERROR_NETWORK_REQUEST_FAILED" ->
                    context.getString(R.string.error_network_request_failed)
                "ERROR_USER_DISABLED" ->
                    context.getString(R.string.error_user_disabled)
                "ERROR_TOO_MANY_REQUESTS" ->
                    context.getString(R.string.error_too_many_requests)
                "ERROR_OPERATION_NOT_ALLOWED" ->
                    context.getString(R.string.error_operation_not_allowed)
                else ->
                    context.getString(R.string.error_signup_failed, e.message)
            }
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            AuthResult.Error(context.getString(R.string.error_unexpected, e.message))
        }
    }

    suspend fun signIn(
        email: String,
        password: String
    ): AuthResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" ->
                    context.getString(R.string.error_email_invalid_firebase)
                "ERROR_WRONG_PASSWORD" ->
                    context.getString(R.string.error_wrong_password)
                "ERROR_USER_NOT_FOUND" ->
                    context.getString(R.string.error_user_not_found)
                "ERROR_USER_DISABLED" ->
                    context.getString(R.string.error_user_disabled)
                "ERROR_TOO_MANY_REQUESTS" ->
                    context.getString(R.string.error_too_many_failed_attempts)
                "ERROR_NETWORK_REQUEST_FAILED" ->
                    context.getString(R.string.error_network_request_failed)
                "ERROR_INVALID_CREDENTIAL" ->
                    context.getString(R.string.error_invalid_credential)
                else ->
                    context.getString(R.string.error_signin_failed, e.message)
            }
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            AuthResult.Error(context.getString(R.string.error_unexpected, e.message))
        }
    }
}

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}