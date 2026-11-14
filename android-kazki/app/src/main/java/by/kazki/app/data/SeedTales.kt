package by.kazki.app.data

/** Offline fallback; the live catalogue comes from the backend `tales` table.
 *  Narration is not recorded yet: audioUrl stays null and the UI marks these tales "Хутка". */
object SeedTales {

    private fun t(i: Int, id: String, title: String, category: String, description: String) =
        Tale(id, title, category, null, null, 0, true, description, i * 10)

    val all: List<Tale> = listOf(
        t(1, "muzyka", "Музыка-чарадзей", "charounyja", "Філасофская казка пра дзівосную сілу музыкі і яе супрацьстаянне злу ды ліху."),
        t(2, "andrej", "Андрэй за ўсіх мудрэй", "charounyja", "Чарадзейная казка пра чалавечую мудрасць і іспыты жыцця."),
        t(3, "kot", "Як кот звяроў напалохаў", "zviarynyja", "Іранічная казка пра хітрасць, знаходлівасць і прыткі розум."),
        t(4, "sapciucha", "Шаптуха", "zabaunyja", "Сацыяльна-бытавая казка пра тое, што на ўсякага хітрага знойдзецца яшчэ хітрэйшы."),
        t(5, "alionka", "Алёнка", "charounyja", "Чарадзейная казка пра жаночую смеласць ды рашучасць."),
        t(6, "barsuk", "Чаму барсук і лісіца ў норах жывуць", "mifalagicnyja", "Казка пра тое, чаму звяры такія розныя і што з імі здарылася на самой справе."),
    )
}
