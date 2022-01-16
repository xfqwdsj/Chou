package xyz.xfqlittlefan.chou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.cutoutPadding
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import xyz.xfqlittlefan.chou.ui.components.ChouAppBar
import xyz.xfqlittlefan.chou.ui.components.ChouNavigationBar
import xyz.xfqlittlefan.chou.ui.theme.ChouTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ActivityViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val systemUiController = rememberSystemUiController()

            SideEffect {
                systemUiController.setNavigationBarColor(color = Color.Transparent)
            }

            ChouTheme {
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    val navController = rememberNavController()

                    Scaffold(
                        topBar = {
                            ChouAppBar(
                                title = { Text(stringResource(id = R.string.app_name)) },
                                modifier = Modifier
                                    .systemBarsPadding(bottom = false)
                                    .cutoutPadding(top = false, bottom = false),
                                actions = {
                                    AnimatedVisibility(
                                        visible = viewModel.appState == 2,
                                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                                    ) {
                                        IconButton(onClick = { viewModel.resetState() }) {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = stringResource(id = R.string.reset)
                                            )
                                        }
                                    }
                                },
                                scrollBehavior = viewModel.scrollBehavior,
                                fraction = viewModel.fraction
                            )
                        },
                        bottomBar = {
                            AnimatedVisibility(
                                visible = viewModel.globalScreen == null,
                                enter = expandVertically(expandFrom = Alignment.Top),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top)
                            ) {
                                ChouNavigationBar(
                                    modifier = Modifier
                                        .systemBarsPadding(top = false)
                                        .imePadding()
                                        .cutoutPadding(top = false, bottom = false)
                                ) {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentDestination = navBackStackEntry?.destination
                                    viewModel.screenList.forEach { item ->
                                        NavigationBarItem(
                                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                            onClick = {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = { Icon(imageVector = item.icon, contentDescription = stringResource(id = item.resId)) },
                                            enabled = viewModel.appState == 0,
                                            label = { Text(stringResource(id = item.resId)) }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        LaunchedEffect(viewModel.globalScreen) {
                            viewModel.globalScreen?.let {
                                navController.navigate(it) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }

                        NavHost(navController = navController, startDestination = viewModel.screenList[0].route, modifier = Modifier.padding(innerPadding)) {
                            viewModel.screenList.forEach { item ->
                                composable(item.route) {
                                    SideEffect {
                                        viewModel.currentScrollState = item.state
                                    }

                                    item.component {
                                        navController.navigate(it) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
