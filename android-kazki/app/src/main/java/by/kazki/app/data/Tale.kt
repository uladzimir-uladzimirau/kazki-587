package by.kazki.app.data

import kotlinx.serialization.Serializable

enum class Category(val key: String, val title: String) {
    Zviarynyja("zviarynyja", "Звярыныя"),
    Charounyja("charounyja", "Чароўныя"),
    Mifalagicnyja("mifalagicnyja", "Міфалагічныя"),
    Zabaunyja("zabaunyja", "Забаўныя"),
    Autarskija("autarskija", "Аўтарскія");

    companion object {
        fun from(key: String): Category? = entries.firstOrNull { it.key == key }
    }
}

@Serializable
data class Tale(
    val id: String,
    val title: String,
    val category: String,
    val coverUrl: String? = null,
    val audioUrl: String? = null,
    val durationSec: Int = 0,
    val isFree: Boolean,
    val description: String,
    val sortOrder: Int = 0,
) {
    val categoryEnum: Category? get() = Category.from(category)

    /** Narration is uploaded to the catalogue later; until then the tale is marked "Хутка". */
    val hasAudio: Boolean get() = !audioUrl.isNullOrEmpty()

    val durationLabel: String
        get() = if (hasAudio) "${(durationSec + 30) / 60} хв" else "Хутка"
}

@Serializable
data class TalesResponse(val tales: List<Tale>)
