package app.rork.vs5cyeao6lm3069le5zsh

import android.content.ComponentName
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.rork.vs5cyeao6lm3069le5zsh.playback.PlaybackService
import app.rork.vs5cyeao6lm3069le5zsh.ui.navigation.AppNavigation
import app.rork.vs5cyeao6lm3069le5zsh.ui.theme.AppTheme
import com.google.common.util.concurrent.ListenableFuture

@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        // Binding a controller keeps the MediaSessionService alive for lock-screen controls.
        controllerFuture = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlaybackService::class.java))).buildAsync()
        setContent {
            AppTheme {
                // Exposes Compose test tags as resource IDs for UI automation; invisible to users.
                Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        super.onDestroy()
    }
}
