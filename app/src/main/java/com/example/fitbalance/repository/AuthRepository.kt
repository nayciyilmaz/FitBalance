package com.example.fitbalance.repository

import com.example.fitbalance.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun signUp(
        email: String,
        password: String,
        userData: UserData
    ): AuthResult {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return AuthResult.Error("Kullanıcı ID'si alınamadı")

            val userDataWithUid = userData.copy(uid = userId)
            firestore.collection("users")
                .document(userId)
                .set(userDataWithUid)
                .await()

            AuthResult.Success
        } catch (e: FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Bu e-posta adresi zaten kullanımda"
                "ERROR_INVALID_EMAIL" -> "Geçersiz e-posta adresi"
                "ERROR_WEAK_PASSWORD" -> "Şifre çok zayıf (en az 6 karakter olmalı)"
                "ERROR_NETWORK_REQUEST_FAILED" -> "İnternet bağlantısı hatası"
                "ERROR_USER_DISABLED" -> "Bu hesap devre dışı bırakılmış"
                "ERROR_TOO_MANY_REQUESTS" -> "Çok fazla deneme yapıldı. Lütfen daha sonra tekrar deneyin"
                "ERROR_OPERATION_NOT_ALLOWED" -> "Bu işlem şu anda kullanılamıyor"
                else -> "Kayıt işlemi başarısız: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            AuthResult.Error("Beklenmeyen bir hata oluştu: ${e.message}")
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
                "ERROR_INVALID_EMAIL" -> "Geçersiz e-posta adresi"
                "ERROR_WRONG_PASSWORD" -> "Hatalı şifre"
                "ERROR_USER_NOT_FOUND" -> "Bu e-posta ile kayıtlı kullanıcı bulunamadı"
                "ERROR_USER_DISABLED" -> "Bu hesap devre dışı bırakılmış"
                "ERROR_TOO_MANY_REQUESTS" -> "Çok fazla başarısız deneme. Lütfen daha sonra tekrar deneyin"
                "ERROR_NETWORK_REQUEST_FAILED" -> "İnternet bağlantısı hatası"
                "ERROR_INVALID_CREDENTIAL" -> "E-posta veya şifre hatalı"
                else -> "Giriş başarısız: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            AuthResult.Error("Beklenmeyen bir hata oluştu: ${e.message}")
        }
    }
}

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}