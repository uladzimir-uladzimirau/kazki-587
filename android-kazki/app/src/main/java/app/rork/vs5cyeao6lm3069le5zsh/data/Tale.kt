package app.rork.vs5cyeao6lm3069le5zsh.data

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
    val audioUrl: String,
    val durationSec: Int,
    val isFree: Boolean,
    val description: String,
    val sortOrder: Int = 0,
) {
    val categoryEnum: Category? get() = Category.from(category)
    val durationLabel: String get() = "${(durationSec + 30) / 60} хв"
}

@Serializable
data class TalesResponse(val tales: List<Tale>)
