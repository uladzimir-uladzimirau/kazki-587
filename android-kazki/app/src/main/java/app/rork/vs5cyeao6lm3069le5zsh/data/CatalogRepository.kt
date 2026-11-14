package app.rork.vs5cyeao6lm3069le5zsh.data

import app.rork.vs5cyeao6lm3069le5zsh.AppConstants
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/** Catalogue from the backend, cached on device, with the bundled seed as offline fallback. */
class CatalogRepository(private val store: LocalStore) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Android) {
        engine { connectTimeout = 10_000; socketTimeout = 15_000 }
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _tales = MutableStateFlow(readCache() ?: SeedTales.all)
    val tales: StateFlow<List<Tale>> = _tales.asStateFlow()

    fun refresh() {
        scope.launch {
            runCatching {
                val response = client.get("${AppConstants.apiBaseUrl}/tales")
                if (!response.status.isSuccess()) return@runCatching
                val body = response.bodyAsText()
                val parsed = json.decodeFromString<TalesResponse>(body).tales
                if (parsed.isNotEmpty()) {
                    _tales.value = parsed.sortedBy { it.sortOrder }
                    store.cachedCatalog = body
                }
            }
        }
    }

    fun byId(id: String): Tale? = _tales.value.firstOrNull { it.id == id }

    private fun readCache(): List<Tale>? = store.cachedCatalog?.let { cached ->
        runCatching { json.decodeFromString<TalesResponse>(cached).tales.sortedBy { it.sortOrder } }
            .getOrNull()?.takeIf { it.isNotEmpty() }
    }
}
