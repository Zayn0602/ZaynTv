package com.example.remote

import android.view.KeyEvent

enum class RemoteAction {
    CHANNEL_NEXT,
    CHANNEL_PREV,
    TOGGLE_CHANNEL_LIST,
    SHOW_INFO,
    SHOW_MENU,
    SHOW_NUMERIC_KEYPAD,
    SHOW_EPG,
    PLAY_PAUSE,
    OPEN_SETTINGS,
    NONE
}

object RemoteKeyHandler {
    /**
     * Maps physical Android TV / Fire TV remote keycodes to ZaynTV actions
     * specifically when in Fullscreen Live TV mode.
     */
    fun handleKeyDown(keyCode: Int, event: KeyEvent, isOverlayOpen: Boolean): RemoteAction {
        // If an overlay (keypad, channel list, menu) is open, standard DPAD navigation applies
        if (isOverlayOpen) {
            return RemoteAction.NONE
        }

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> RemoteAction.CHANNEL_NEXT
            KeyEvent.KEYCODE_DPAD_UP -> RemoteAction.CHANNEL_PREV
            KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_PAGE_UP -> RemoteAction.CHANNEL_NEXT
            KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_PAGE_DOWN -> RemoteAction.CHANNEL_PREV
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> RemoteAction.TOGGLE_CHANNEL_LIST
            KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_I -> RemoteAction.SHOW_INFO
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_M -> RemoteAction.SHOW_MENU
            KeyEvent.KEYCODE_GUIDE, KeyEvent.KEYCODE_G -> RemoteAction.SHOW_EPG
            KeyEvent.KEYCODE_SETTINGS, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_HELP -> RemoteAction.OPEN_SETTINGS
            // Channel shortcuts or colored buttons
            KeyEvent.KEYCODE_PROG_RED, KeyEvent.KEYCODE_F1, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_C -> {
                RemoteAction.SHOW_NUMERIC_KEYPAD
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> RemoteAction.PLAY_PAUSE
            KeyEvent.KEYCODE_MEDIA_PLAY -> RemoteAction.PLAY_PAUSE
            KeyEvent.KEYCODE_MEDIA_PAUSE -> RemoteAction.PLAY_PAUSE
            // Volume keys are untouched
            else -> RemoteAction.NONE
        }
    }
}
