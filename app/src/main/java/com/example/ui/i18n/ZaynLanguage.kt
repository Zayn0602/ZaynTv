package com.example.ui.i18n

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String
)

object ZaynLanguageManager {
    val supportedLanguages = listOf(
        LanguageOption("system", "System Default", "System Default"),
        LanguageOption("en", "English", "English"),
        LanguageOption("bn", "Bengali", "বাংলা"),
        LanguageOption("hi", "Hindi", "हिन्दी"),
        LanguageOption("ta", "Tamil", "தமிழ்"),
        LanguageOption("te", "Telugu", "తెలుగు"),
        LanguageOption("kn", "Kannada", "ಕನ್ನಡ"),
        LanguageOption("ml", "Malayalam", "മലയാളം"),
        LanguageOption("mr", "Marathi", "मराठी"),
        LanguageOption("gu", "Gujarati", "ગુજરાતી"),
        LanguageOption("pa", "Punjabi", "ਪੰਜਾਬੀ"),
        LanguageOption("or", "Odia", "ଓଡ଼ିଆ"),
        LanguageOption("as", "Assamese", "অসমীয়া"),
        LanguageOption("ur", "Urdu", "اردو"),
        LanguageOption("ne", "Nepali", "नेपाली")
    )

    fun getString(key: String, langCode: String = "en"): String {
        val lang = if (langCode == "system") "en" else langCode
        return when (key) {
            "live_tv" -> when (lang) {
                "bn" -> "লাইভ টিভি"
                "hi" -> "लाइव टीवी"
                "ta" -> "லைவ் டிவி"
                "te" -> "లైవ్ టీవీ"
                "kn" -> "ಲೈವ್ ಟಿವಿ"
                "ml" -> "ലൈവ് ടിവി"
                "mr" -> "थेट टीव्ही"
                "gu" -> "લાઇવ ટીવી"
                "pa" -> "ਲਾਈਵ ਟੀਵੀ"
                "ur" -> "لائیو ٹی وی"
                else -> "Live TV"
            }
            "channels" -> when (lang) {
                "bn" -> "চ্যানেলসমূহ"
                "hi" -> "चैनल"
                "ta" -> "சேனல்கள்"
                "te" -> "ఛానెల్స్"
                "kn" -> "ಚಾನೆಲ್‌ಗಳು"
                "ml" -> "ചാനലുകൾ"
                "mr" -> "वाहिन्या"
                "gu" -> "ચેનલો"
                "pa" -> "ਚੈਨਲ"
                "ur" -> "چینلز"
                else -> "Channels"
            }
            "categories" -> when (lang) {
                "bn" -> "বিভাগসমূহ"
                "hi" -> "श्रेणियाँ"
                "ta" -> "வகைகள்"
                "te" -> "వర్గాలు"
                "kn" -> "ವರ್ಗಗಳು"
                "ml" -> "വിഭാഗങ്ങൾ"
                "mr" -> "श्रेण्या"
                "gu" -> "શ્રેણીઓ"
                "pa" -> "ਸ਼੍ਰੇਣੀਆਂ"
                "ur" -> "زمرے"
                else -> "Categories"
            }
            "providers" -> when (lang) {
                "bn" -> "প্রোভাইডার"
                "hi" -> "प्रदाता"
                "ta" -> "வழங்குநர்கள்"
                "te" -> "ప్రొవైడర్లు"
                "ur" -> "فراہم کنندگان"
                else -> "Providers"
            }
            "settings" -> when (lang) {
                "bn" -> "সেটিংস"
                "hi" -> "सेटिंग्स"
                "ta" -> "அமைப்புகள்"
                "te" -> "సెట్టింగ్‌లు"
                "kn" -> "ಸೆಟ್ಟಿಂಗ್‌ಗಳು"
                "ml" -> "ക്രമീകരണങ്ങൾ"
                "mr" -> "सेटिंग्ज"
                "gu" -> "સેટિંગ્સ"
                "pa" -> "ਸੈਟਿੰਗਾਂ"
                "ur" -> "ترتیبات"
                else -> "Settings"
            }
            "search" -> when (lang) {
                "bn" -> "অনুসন্ধান"
                "hi" -> "खोजें"
                "ta" -> "தேடு"
                "te" -> "శోధించండి"
                "kn" -> "ಹುಡುಕಿ"
                "ml" -> "തിരയുക"
                "mr" -> "शोधा"
                "gu" -> "શોધો"
                "pa" -> "ਖੋਜੋ"
                "ur" -> "تلاش کریں"
                else -> "Search"
            }
            "favorites" -> when (lang) {
                "bn" -> "প্রিয় চ্যানেল"
                "hi" -> "पसंदीदा"
                "ta" -> "விருப்பமானவை"
                "te" -> "ఇష్టమైనవి"
                "kn" -> "ಮೆಚ್ಚಿನವುಗಳು"
                "ml" -> "പ്രിയപ്പെട്ടവ"
                "ur" -> "پسندیدہ"
                else -> "Favorites"
            }
            "stream_unavailable" -> when (lang) {
                "bn" -> "স্ট্রিমটি অনুপলব্ধ"
                "hi" -> "स्ट्रीम अनुपलब्ध है"
                "ur" -> "سٹریم دستیاب نہیں ہے"
                else -> "Stream unavailable"
            }
            "retry" -> when (lang) {
                "bn" -> "আবার চেষ্টা করুন"
                "hi" -> "पुनः प्रयास करें"
                "ur" -> "دوبارہ کوشش کریں"
                else -> "Retry"
            }
            "watermark" -> when (lang) {
                "bn" -> "ভিডিও লোগো / ওয়াটারমার্ক"
                "hi" -> "वीडियो वॉटरमार्क"
                else -> "Video Watermark"
            }
            else -> key
        }
    }
}
