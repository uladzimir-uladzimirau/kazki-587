package app.rork.vs5cyeao6lm3069le5zsh.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AfterTale { Stop, Lullaby, Repeat }

/** Anonymous on-device state only: favourites, listening progress, preferences. */
class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("kazki", Context.MODE_PRIVATE)

    private val _favorites = MutableStateFlow(prefs.getStringSet(KEY_FAVORITES, emptySet())!!.toSet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _progress = MutableStateFlow(readAllProgress())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    private val _lastTaleId = MutableStateFlow(prefs.getString(KEY_LAST_TALE, null))
    /** The tale opened most recently, kept across launches for the «Працягнуць» card. */
    val lastTaleId: StateFlow<String?> = _lastTaleId.asStateFlow()

    fun setLastTale(id: String) {
        if (_lastTaleId.value == id) return
        _lastTaleId.value = id
        prefs.edit { putString(KEY_LAST_TALE, id) }
    }

    fun toggleFavorite(id: String) {
        val next = _favorites.value.toMutableSet().apply { if (!add(id)) remove(id) }
        _favorites.value = next
        prefs.edit { putStringSet(KEY_FAVORITES, next) }
    }

    fun positionMs(id: String): Long = prefs.getLong("pos_$id", 0L)

    fun savePosition(id: String, positionMs: Long, durationMs: Long) {
        prefs.edit {
            putLong("pos_$id", positionMs)
            if (durationMs > 0) putFloat("frac_$id", (positionMs.toFloat() / durationMs).coerceIn(0f, 1f))
            else remove("frac_$id")
        }
        _progress.value = _progress.value.toMutableMap().apply {
            if (durationMs > 0 && positionMs > 0) put(id, (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)) else remove(id)
        }
    }

    var afterTale: AfterTale
        get() = runCatching { AfterTale.valueOf(prefs.getString(KEY_AFTER, AfterTale.Stop.name)!!) }.getOrDefault(AfterTale.Stop)
        set(value) = prefs.edit { putString(KEY_AFTER, value.name) }

    var cachedCatalog: String?
        get() = prefs.getString(KEY_CATALOG, null)
        set(value) = prefs.edit { putString(KEY_CATALOG, value) }

    private fun readAllProgress(): Map<String, Float> =
        prefs.all.filterKeys { it.startsWith("frac_") }
            .mapNotNull { (k, v) -> (v as? Float)?.takeIf { it > 0f }?.let { k.removePrefix("frac_") to it } }
            .toMap()

    private companion object {
        const val KEY_FAVORITES = "favorites"
        const val KEY_AFTER = "after_tale"
        const val KEY_CATALOG = "catalog_json"
        const val KEY_LAST_TALE = "last_tale_id"
    }
}
