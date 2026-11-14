package by.kazki.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import by.kazki.app.KazkiApp
import by.kazki.app.data.Tale
import by.kazki.app.ui.components.MiniPlayer
import by.kazki.app.ui.screens.AboutProjectScreen
import by.kazki.app.ui.screens.CatalogScreen
import by.kazki.app.ui.screens.FavoritesScreen
import by.kazki.app.ui.screens.InfoScreen
import by.kazki.app.ui.screens.ParentsScreen
import by.kazki.app.ui.screens.PlayerScreen
import by.kazki.app.ui.theme.Kazki

@Composable
fun rememberKazkiApp(): KazkiApp = LocalContext.current.applicationContext as KazkiApp

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Catalog("catalog", "Казкі", Icons.Rounded.AutoStories),
    Favorites("favorites", "Абранае", Icons.Rounded.Favorite),
    Parents("parents", "Бацькам", Icons.Rounded.FamilyRestroom),
}

private const val FADE = 600

@Composable
fun AppNavigation() {
    val app = rememberKazkiApp()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isTab = Tab.entries.any { it.route == route }

    val tales by app.catalog.tales.collectAsStateWithLifecycle()
    val playback by app.playback.ui.collectAsStateWithLifecycle()
    val current = tales.firstOrNull { it.id == playback.taleId }

    fun openTale(tale: Tale) {
        app.playback.play(tale)
        nav.navigate("player/${tale.id}") { launchSingleTop = true }
    }

    Scaffold(
        containerColor = Kazki.Night,
        bottomBar = {
            AnimatedVisibility(
                visible = isTab,
                enter = fadeIn(tween(FADE)) + slideInVertically(tween(FADE)) { it / 2 },
                exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 },
            ) {
                Column {
                    if (current != null) {
                        MiniPlayer(
                            tale = current,
                            ui = playback,
                            onOpen = { nav.navigate("player/${current.id}") { launchSingleTop = true } },
                            onToggle = { app.playback.togglePlay() },
                        )
                    }
                    KazkiNavigationBar(nav, route)
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Tab.Catalog.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(tween(FADE)) },
            exitTransition = { fadeOut(tween(FADE)) },
            popEnterTransition = { fadeIn(tween(FADE)) },
            popExitTransition = { fadeOut(tween(FADE)) },
        ) {
            composable(Tab.Catalog.route) { CatalogScreen(padding, onTale = ::openTale) }
            composable(Tab.Favorites.route) { FavoritesScreen(padding, onTale = ::openTale) }
            composable(Tab.Parents.route) {
                ParentsScreen(padding, onAboutProject = { nav.navigate("project") }, onInfo = { nav.navigate("info/$it") })
            }
            composable("player/{id}") { backStack ->
                PlayerScreen(
                    taleId = backStack.arguments?.getString("id").orEmpty(),
                    onClose = { nav.popBackStack() },
                )
            }
            composable("project") { AboutProjectScreen(onBack = { nav.popBackStack() }) }
            composable("info/{kind}") { backStack ->
                InfoScreen(kind = backStack.arguments?.getString("kind").orEmpty(), onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun KazkiNavigationBar(nav: NavHostController, route: String?) {
    NavigationBar(containerColor = Kazki.NightDeep, contentColor = Kazki.Cream) {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = route == tab.route,
                modifier = Modifier.testTag("tab-${tab.route}"),
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Kazki.Ink,
                    selectedTextColor = Kazki.Gold,
                    indicatorColor = Kazki.Gold,
                    unselectedIconColor = Kazki.CreamMuted,
                    unselectedTextColor = Kazki.CreamMuted,
                ),
            )
        }
    }
}
