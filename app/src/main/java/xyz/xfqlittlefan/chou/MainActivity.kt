package xyz.xfqlittlefan.chou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import xyz.xfqlittlefan.chou.ui.theme.ChouTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ActivityViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val controller = rememberSystemUiController()

            //SideEffect {
            //    controller.setNavigationBarColor(color = Color.Transparent)
            //}

            val main: @Composable () -> Unit = {
                Text(stringResource(R.string.app_name))
            }

            val setting: @Composable () -> Unit = {

            }

            ChouTheme {
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    with(LocalDensity.current) {
                        val navController = rememberNavController()
                        val items = listOf(
                            Pair(Pair(R.string.chou_page, Icons.Default.Home), main),
                            Pair(Pair(R.string.setting_page, Icons.Default.Settings), setting)
                        )

                        Scaffold(
                            topBar = {
                                SmallTopAppBar(
                                    title = { Text(stringResource(id = R.string.app_name)) },
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
                                    })
                            },
                            bottomBar = {
                                NavigationBar {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentDestination = navBackStackEntry?.destination
                                    items.forEach { item ->
                                        NavigationBarItem(
                                            icon = { Icon(imageVector = item.first.second, contentDescription = stringResource(id = item.first.first)) },
                                            label = { Text(stringResource(id = item.first.first)) },
                                            selected = currentDestination?.hierarchy?.any { it.route == item.first.first.toString() } == true,
                                            onClick = {
                                                navController.navigate(item.first.first.toString()) {
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
                            NavHost(navController = navController, startDestination = items[0].first.first.toString(), modifier = Modifier.padding(innerPadding)) {
                                items.forEach { item ->
                                    composable(item.first.first.toString()) { item.second() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
