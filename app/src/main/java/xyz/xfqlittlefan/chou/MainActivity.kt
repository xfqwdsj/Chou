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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
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
                        modifier = Modifier.nestedScroll(viewModel.scrollBehavior.nestedScrollConnection),
                        topBar = {
                            ChouAppBar(
                                title = { Text(stringResource(id = R.string.app_name)) },
                                modifier = Modifier.statusBarsPadding(),
                                actions = {
                                    AnimatedVisibility(
                                        visible = viewModel.state == 2,
                                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                                    ) {
                                        IconButton(onClick = { viewModel.reset() }) {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = stringResource(id = R.string.reset)
                                            )
                                        }
                                    }
                                },
                                scrollBehavior = viewModel.scrollBehavior,
                                scrollState = viewModel.currentScrollState
                            )
                        },
                        bottomBar = {
                            ChouNavigationBar(modifier = Modifier.navigationBarsPadding()) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                viewModel.screenList.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(imageVector = item.icon, contentDescription = stringResource(id = item.resId)) },
                                        label = { Text(stringResource(id = item.resId)) },
                                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        onClick = {
                                            viewModel.currentScrollState = item.state
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(navController = navController, startDestination = viewModel.screenList[0].route, modifier = Modifier.padding(innerPadding)) {
                            viewModel.screenList.forEach { item ->
                                composable(item.route) { item.component() }
                            }
                        }
                    }
                }
            }
        }
    }
}
