package com.example.auth

import com.example.data.model.UserProfile
import com.example.storage.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult {
    data class Success(val profile: UserProfile) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthManager(private val preferencesManager: PreferencesManager) {

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun login(identifier: String, password: String, rememberMe: Boolean = true): AuthResult = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        if (cleanId.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Please enter your username/email and password.")
        }

        // Secure token generation for session
        val token = UUID.randomUUID().toString()
        val displayName = cleanId.substringBefore("@").replaceFirstChar { it.uppercase() }

        val profile = UserProfile(
            id = "user_${cleanId.hashCode()}",
            name = displayName,
            username = cleanId.substringBefore("@"),
            email = if (cleanId.contains("@")) cleanId else "$cleanId@zayntv.app",
            phone = if (cleanId.all { it.isDigit() || it == '+' }) cleanId else "",
            sessionToken = token,
            isLoggedIn = true,
            rememberMe = rememberMe
        )

        preferencesManager.saveUserProfile(profile)
        AuthResult.Success(profile)
    }

    suspend fun signUp(name: String, username: String, email: String, phone: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        if (name.isBlank() || username.isBlank() || password.length < 4) {
            return@withContext AuthResult.Error("Please provide a valid name, username, and password (min 4 characters).")
        }

        val token = UUID.randomUUID().toString()
        val profile = UserProfile(
            id = "user_${UUID.randomUUID()}",
            name = name.trim(),
            username = username.trim(),
            email = email.trim(),
            phone = phone.trim(),
            sessionToken = token,
            isLoggedIn = true,
            rememberMe = true
        )

        preferencesManager.saveUserProfile(profile)
        AuthResult.Success(profile)
    }

    suspend fun updateProfile(name: String, email: String, phone: String, currentProfile: UserProfile): AuthResult = withContext(Dispatchers.IO) {
        val updated = currentProfile.copy(
            name = name.trim().ifEmpty { currentProfile.name },
            email = email.trim().ifEmpty { currentProfile.email },
            phone = phone.trim().ifEmpty { currentProfile.phone }
        )
        preferencesManager.saveUserProfile(updated)
        AuthResult.Success(updated)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        preferencesManager.clearUserSession()
    }
}
