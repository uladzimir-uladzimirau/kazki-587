package by.kazki.app.data

/** Offline fallback; the live catalogue comes from the backend `tales` table.
 *  Tales without audioUrl have no narration yet — the UI marks them "Хутка". */
object SeedTales {

    private const val R2 = "https://pub-1a65051c16c1434c80f6e16db46fe497.r2.dev"

    private fun t(i: Int, id: String, title: String, category: String, description: String, audio: String? = null, durationSec: Int = 0) =
        Tale(id, title, category, null, audio?.let { "$R2/$it.m4a" }, durationSec, true, description, i * 10)

    val all: List<Tale> = listOf(
        t(1, "muzyka", "Музыка-чарадзей", "charounyja", "Філасофская казка пра дзівосную сілу музыкі і яе супрацьстаянне злу ды ліху.", "muzyka", 449),
        t(2, "andrej", "Андрэй за ўсіх мудрэй", "charounyja", "Чарадзейная казка пра чалавечую мудрасць і іспыты жыцця.", "andrej", 499),
        t(3, "kot", "Як кот звяроў напалохаў", "zviarynyja", "Іранічная казка пра хітрасць, знаходлівасць і прыткі розум.", "kot", 439),
        t(4, "sapciucha", "Шаптуха", "zabaunyja", "Сацыяльна-бытавая казка пра тое, што на ўсякага хітрага знойдзецца яшчэ хітрэйшы.", "sapciucha", 302),
        t(5, "alionka", "Алёнка", "charounyja", "Чарадзейная казка пра жаночую смеласць ды рашучасць.", "alionka", 646),
        t(6, "barsuk", "Чаму барсук і лісіца ў норах жывуць", "mifalagicnyja", "Казка пра тое, чаму звяры такія розныя і што з імі здарылася на самой справе.", "barsuk", 433),
    )
}
