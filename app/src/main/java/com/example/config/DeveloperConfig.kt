package com.example.config

/**
 * Central Developer and Branding Configuration for ZaynTV.
 * All social URLs and developer credentials are maintained exclusively here.
 */
object DeveloperConfig {
    const val APP_NAME: String = "ZaynTV"
    const val APP_VERSION: String = "1.0.1"
    const val DEVELOPER_NAME: String = "Samim Mandal"
    const val DEVELOPER_HANDLE: String = "@thesamimmandal"

    // Social Media Profiles
    const val FACEBOOK_URL: String = "https://www.facebook.com/thesamimmandal"
    const val INSTAGRAM_URL: String = "https://www.instagram.com/thesamimmandal"
    const val X_URL: String = "https://x.com/thesamimmandal"
    const val YOUTUBE_URL: String = "https://www.youtube.com/@thesamimmandal"
    const val TELEGRAM_URL: String = "https://t.me/thesamimmandal"
    const val GITHUB_URL: String = "https://github.com/thesamimmandal"

    // Optional email - empty string hides the email action as requested
    const val DEVELOPER_EMAIL: String = ""

    // Developer Test Stream URL for pipeline verification (Legal public open-source test stream)
    const val TEST_STREAM_URL: String = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
}
