package com.example.ui.parental

import com.example.storage.PreferencesManager
import kotlinx.coroutines.flow.first

class ParentalControlManager(private val preferencesManager: PreferencesManager) {

    private var isSessionUnlocked: Boolean = false

    suspend fun verifyPin(enteredPin: String): Boolean {
        val settings = preferencesManager.userSettingsFlow.first()
        if (!settings.parentalEnabled || settings.parentalPin.isBlank()) {
            return true
        }
        val isCorrect = settings.parentalPin == enteredPin.trim()
        if (isCorrect) {
            isSessionUnlocked = true
        }
        return isCorrect
    }

    suspend fun isChannelLocked(channelId: String, categoryName: String): Boolean {
        if (isSessionUnlocked) return false
        val settings = preferencesManager.userSettingsFlow.first()
        if (!settings.parentalEnabled) return false

        val lockedChannels = preferencesManager.lockedChannelsFlow.first()
        val lockedCategories = preferencesManager.lockedCategoriesFlow.first()

        if (lockedChannels.contains(channelId)) return true

        // Also check if category is sensitive or locked
        if (lockedCategories.contains(categoryName)) return true
        if (categoryName.contains("adult", ignoreCase = true) || categoryName.contains("xxx", ignoreCase = true)) {
            return true
        }

        return false
    }

    fun lockSession() {
        isSessionUnlocked = false
    }
}
