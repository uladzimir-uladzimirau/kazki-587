package app.rork.vs5cyeao6lm3069le5zsh

import android.app.Application
import app.rork.vs5cyeao6lm3069le5zsh.data.CatalogRepository
import app.rork.vs5cyeao6lm3069le5zsh.data.LocalStore
import app.rork.vs5cyeao6lm3069le5zsh.playback.PlaybackManager

class KazkiApp : Application() {
    lateinit var store: LocalStore
        private set
    lateinit var catalog: CatalogRepository
        private set
    lateinit var playback: PlaybackManager
        private set

    override fun onCreate() {
        super.onCreate()
        store = LocalStore(this)
        catalog = CatalogRepository(store)
        playback = PlaybackManager(this, store, catalog)
        catalog.refresh()
    }
}
