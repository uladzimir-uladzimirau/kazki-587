package by.kazki.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import by.kazki.app.AppConstants
import by.kazki.app.R
import by.kazki.app.ui.components.ParentGateDialog
import by.kazki.app.ui.components.ParentGateState
import by.kazki.app.ui.components.OrnamentBand
import by.kazki.app.ui.theme.Kazki

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutProjectScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var gate by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun openTelegram() {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, AppConstants.TELEGRAM_URL.toUri()))
        } catch (_: ActivityNotFoundException) {
            error = "Не атрымалася адкрыць. Наш адрас: ${AppConstants.TELEGRAM_URL}"
        }
    }

    Scaffold(
        containerColor = Kazki.Night,
        topBar = {
            TopAppBar(
                title = { Text("Пра праект", style = MaterialTheme.typography.titleLarge) },
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
            Spacer(Modifier.height(8.dp))
            Image(
                painterResource(R.drawable.moon_art),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.6f).clip(RoundedCornerShape(28.dp)),
            )
            Spacer(Modifier.height(18.dp))
            OrnamentBand(Modifier.fillMaxWidth().height(28.dp))
            Spacer(Modifier.height(18.dp))

            Text("KAZKI", style = MaterialTheme.typography.displaySmall, color = Kazki.Cream)
            Spacer(Modifier.height(10.dp))
            Text(
                "KAZKI — бясплатныя беларускія народныя казкі для дзяцей. Мы не зарабляем на дзецях — праект існуе дзякуючы падтрымцы слухачоў.",
                style = MaterialTheme.typography.bodyLarge,
                color = Kazki.Cream,
            )

            Spacer(Modifier.height(20.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Kazki.Surface).padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Favorite, null, tint = Kazki.Red, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Як можна дапамагчы", style = MaterialTheme.typography.titleMedium, color = Kazki.Gold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Раскажыце пра KAZKI сябрам, падзяліцеся казкай з плэера ці напішыце нам у Telegram, якія казкі запісаць наступнымі.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Kazki.CreamMuted,
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (ParentGateState.isOpen) openTelegram() else gate = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Kazki.Gold, contentColor = Kazki.Ink),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Наш Telegram", style = MaterialTheme.typography.titleSmall)
            }
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Kazki.Gold)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Без рэкламы, без пакупак і без збору даных.",
                style = MaterialTheme.typography.bodySmall,
                color = Kazki.CreamMuted,
            )
            Spacer(Modifier.height(28.dp))
        }
    }

    if (gate) {
        ParentGateDialog(
            onDismiss = { gate = false },
            onUnlocked = {
                gate = false
                openTelegram()
            },
        )
    }
}
