package by.kazki.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import by.kazki.app.AppConstants
import by.kazki.app.ui.components.ParentGateContent
import by.kazki.app.ui.components.ParentGateState
import by.kazki.app.ui.theme.Kazki

@Composable
fun ParentsScreen(contentPadding: PaddingValues, onAboutProject: () -> Unit, onInfo: (String) -> Unit) {
    var unlocked by remember { mutableStateOf(ParentGateState.isOpen) }
    AnimatedContent(
        targetState = unlocked,
        transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(400)) },
        label = "gate",
    ) { open ->
        if (!open) {
            Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
                ParentGateContent(onUnlocked = { unlocked = true })
            }
        } else {
            ParentsContent(contentPadding, onAboutProject, onInfo, onLock = {
                ParentGateState.lock()
                unlocked = false
            })
        }
    }
}

@Composable
private fun ParentsContent(contentPadding: PaddingValues, onAboutProject: () -> Unit, onInfo: (String) -> Unit, onLock: () -> Unit) {
    val context = LocalContext.current
    var restoreMessage by remember { mutableStateOf<String?>(null) }

    fun open(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            restoreMessage = "Не атрымалася адкрыць. Напішыце нам: ${AppConstants.CONTACT_EMAIL}"
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = contentPadding.calculateBottomPadding() + 24.dp),
    ) {
        Row(Modifier.padding(top = 24.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Бацькам", style = MaterialTheme.typography.headlineLarge, color = Kazki.Cream, modifier = Modifier.weight(1f))
            TextButton(onClick = onLock) {
                Icon(Icons.Rounded.Lock, null, tint = Kazki.CreamMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Зачыніць", color = Kazki.CreamMuted)
            }
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Kazki.Surface).padding(22.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Favorite, null, tint = Kazki.Red)
                Spacer(Modifier.width(10.dp))
                Text("Усё бясплатна", style = MaterialTheme.typography.titleLarge, color = Kazki.Cream)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Усе казкі і калыханка адкрытыя для ўсіх. Мы не зарабляем на дзецях.",
                style = MaterialTheme.typography.bodyMedium,
                color = Kazki.CreamMuted,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAboutProject,
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("parents-about"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Kazki.Gold, contentColor = Kazki.Ink),
            ) { Text("Пра праект", style = MaterialTheme.typography.titleSmall) }
            restoreMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Kazki.Gold)
            }
        }

        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Kazki.Surface)) {
            Row_(Icons.Rounded.AutoAwesome, "Пра праект", "Telegram і наша гісторыя") { onAboutProject() }
            HorizontalDivider(color = Kazki.Outline.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 20.dp))
            Row_(Icons.Rounded.Shield, "Палітыка прыватнасці") { onInfo("privacy") }
            HorizontalDivider(color = Kazki.Outline.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 20.dp))
            Row_(Icons.Rounded.Info, "Пра праграму") { onInfo("about") }
            HorizontalDivider(color = Kazki.Outline.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 20.dp))
            Row_(Icons.Rounded.Mail, "Напісаць нам", AppConstants.CONTACT_EMAIL) {
                open(
                    Intent(Intent.ACTION_SENDTO, "mailto:${AppConstants.CONTACT_EMAIL}".toUri())
                        .putExtra(Intent.EXTRA_SUBJECT, "KAZKI"),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Без рэкламы, без акаўнтаў і без збору даных. Абранае і прагрэс захоўваюцца толькі на гэтай прыладзе.",
            style = MaterialTheme.typography.bodySmall,
            color = Kazki.CreamMuted,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun Row_(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = subtitle?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
        leadingContent = { Icon(icon, null, tint = Kazki.Gold) },
        trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Kazki.CreamMuted) },
        colors = ListItemDefaults.colors(
            containerColor = Kazki.Surface,
            headlineColor = Kazki.Cream,
            supportingColor = Kazki.CreamMuted,
        ),
        modifier = Modifier.padding(horizontal = 4.dp).clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
    )
}
