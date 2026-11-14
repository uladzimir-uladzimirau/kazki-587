package by.kazki.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import by.kazki.app.AppConstants
import by.kazki.app.BuildConfig
import by.kazki.app.ui.theme.Kazki

private val privacy = listOf(
    "Каротка" to "KAZKI не збірае пра вас і вашых дзяцей ніякіх даных. У праграме няма рэкламы, аналітыкі, трэкінгу і акаўнтаў.",
    "Што захоўваецца на прыладзе" to "Абраныя казкі, месца, дзе вы спыніліся, і налады плэера захоўваюцца толькі на гэтай прыладзе. Яны знікнуць, калі выдаліць праграму.",
    "Пакупкі" to "У праграме няма пакупак і падпісак. Усё бясплатна, мы не просім ніякіх плацёжных даных.",
    "Знешнія спасылкі" to "Telegram, пошта і «Падзяліцца казкай» адкрываюцца толькі пасля праверкі для дарослых.",
    "Інтэрнэт" to "Праграма загружае з нашага сервера каталог казак, вокладкі і аўдыя. Мы не захоўваем, хто і што слухае.",
    "Кантакт" to "Пытанні пра прыватнасць: ${AppConstants.CONTACT_EMAIL}",
)

private val about = listOf(
    "KAZKI — казкі на ноч" to "Беларускія народныя і аўтарскія казкі ў аўдыя для дзяцей ад 2 да 8 гадоў. Спакойныя галасы, павольны рытм і таймер сну, каб засынаць было лёгка.",
    "Раздзелы" to "Звярыныя, чароўныя, міфалагічныя, забаўныя і аўтарскія казкі. Усе казкі і калыханка — бясплатна.",
    "Падтрымка" to "Мы не зарабляем на дзецях: праект існуе дзякуючы падтрымцы слухачоў. Без рэкламы, назаўсёды.",
    "Сувязь" to AppConstants.CONTACT_EMAIL,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(kind: String, onBack: () -> Unit) {
    val isPrivacy = kind == "privacy"
    Scaffold(
        containerColor = Kazki.Night,
        topBar = {
            TopAppBar(
                title = { Text(if (isPrivacy) "Палітыка прыватнасці" else "Пра праграму", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Kazki.Night,
                    titleContentColor = Kazki.Cream,
                    navigationIconContentColor = Kazki.Cream,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .navigationBarsPadding(),
        ) {
            (if (isPrivacy) privacy else about).forEach { (title, body) ->
                Spacer(Modifier.height(18.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = Kazki.Gold)
                Spacer(Modifier.height(6.dp))
                Text(body, style = MaterialTheme.typography.bodyLarge, color = Kazki.Cream)
            }
            Spacer(Modifier.height(28.dp))
            if (!isPrivacy) {
                Text("Версія ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = Kazki.CreamMuted)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
