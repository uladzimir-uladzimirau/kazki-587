package by.kazki.app

import android.app.Application
import by.kazki.app.data.CatalogRepository
import by.kazki.app.data.LocalStore
import by.kazki.app.playback.PlaybackManager

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
