package by.kazki.app

object AppConstants {
    /** KAZKI API preview address (development builds). */
    private const val API_PREVIEW_URL = "https://vs5cyeao6lm3069le5zsh-kazki-api.rork.live"

    /** KAZKI API published address (store builds). Publish the backend before shipping. */
    private const val API_PUBLISHED_URL = "https://kazki.rork.app"

    val apiBaseUrl: String = if (BuildConfig.DEBUG) API_PREVIEW_URL else API_PUBLISHED_URL

    const val CONTACT_EMAIL = "u.uldzmr@gmail.com"
    const val TELEGRAM_URL = "https://t.me/kazki_by"
    const val SHARE_URL = "https://play.google.com/store/apps/details?id=by.kazki.app"
}
