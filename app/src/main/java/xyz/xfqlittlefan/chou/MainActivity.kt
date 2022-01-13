package xyz.xfqlittlefan.chou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import xyz.xfqlittlefan.chou.ui.componets.ChouAppBar
import xyz.xfqlittlefan.chou.ui.componets.ChouNavigationBar
import xyz.xfqlittlefan.chou.ui.theme.ChouTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ActivityViewModel>()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val systemUiController = rememberSystemUiController()

            SideEffect {
                systemUiController.setNavigationBarColor(color = Color.Transparent)
            }

            @Composable
            fun Main() {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(50.dp))
                    AnimatedContent(
                        targetState = viewModel.list.isNotEmpty(),
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) { visible ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (visible) {
                                AnimatedContent(
                                    targetState = viewModel.state,
                                    modifier = Modifier.padding(horizontal = 30.dp),
                                    transitionSpec = { fadeIn() with fadeOut() }
                                ) {
                                    Text(text = stringResource(id = if (it == 2) R.string.chose_item else R.string.current_item), style = MaterialTheme.typography.titleMedium)
                                }
                                AnimatedContent(
                                    targetState = viewModel.current,
                                    modifier = Modifier
                                        .padding(30.dp)
                                        .fillMaxWidth(),
                                    transitionSpec = {
                                        (fadeIn() + slideInVertically(initialOffsetY = { it }) with
                                                slideOutVertically(targetOffsetY = { -it }) + fadeOut()).using(SizeTransform(clip = false))
                                    }
                                ) {
                                    Text(
                                        text = if (viewModel.list.isEmpty()) "" else viewModel.list[it].value,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                }
                                AnimatedVisibility(visible = viewModel.state == 0) {
                                    Button(
                                        onClick = { viewModel.start() }
                                    ) {
                                        Text(text = stringResource(id = R.string.choose))
                                    }
                                }
                            } else {
                                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)) {
                                    Text(text = stringResource(id = R.string.no_item))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }

            @Composable
            fun Edit() {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = viewModel.listState
                        ) {
                            itemsIndexed(items = viewModel.list, key = { _, item -> item.toString() }) { index, item ->
                                Surface(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                        .fillMaxWidth()
                                        .animateItemPlacement(),
                                    shape = RoundedCornerShape(10.dp),
                                    tonalElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AnimatedContent(
                                            targetState = item.value,
                                            modifier = Modifier.weight(1f),
                                            transitionSpec = { fadeIn() with fadeOut() }
                                        ) {
                                            Text(text = it)
                                        }
                                        Spacer(modifier = Modifier.width(20.dp))
                                        Row {
                                            IconButton(
                                                onClick = { viewModel.add(index + 1) }
                                            ) {
                                                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(id = R.string.add_item))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            IconButton(
                                                onClick = { viewModel.remove(index) }
                                            ) {
                                                Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(id = R.string.remove_item))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            IconButton(
                                                onClick = { viewModel.editing = index }
                                            ) {
                                                Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(id = R.string.edit_item))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Composable
            fun Editor() {
                Column {
                    AnimatedVisibility(visible = viewModel.editing != null) {
                        val requester = FocusRequester()
                        val initValue = viewModel.editing?.let { viewModel.list[it].value } ?: ""
                        var value by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    text = initValue,
                                    selection = TextRange(index = initValue.length)
                                )
                            )
                        }

                        SideEffect {
                            requester.requestFocus()
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape = RoundedCornerShape(size = 10.dp))
                            ) {
                                TextField(
                                    value = value,
                                    onValueChange = { value = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(requester)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(onClick = { viewModel.editing = null }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(id = android.R.string.cancel))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(onClick = {
                                viewModel.list[viewModel.editing!!].value = value.text
                                viewModel.editing = null
                            }) {
                                Icon(imageVector = Icons.Filled.Done, contentDescription = stringResource(id = android.R.string.ok))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    TextButton(
                        onClick = {
                            viewModel.add(0)
                        },
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(size = 10.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.add_item)
                        )
                    }
                }
            }

            ChouTheme {
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    val navController = rememberNavController()
                    val items = listOf<Pair<Pair<Int, ImageVector>, Pair<@Composable () -> Unit, @Composable () -> Unit>>>(
                        Pair(Pair(R.string.chou_page, Icons.Default.Home), Pair(@Composable { Main() }, @Composable { })),
                        Pair(Pair(R.string.edit_page, Icons.Default.Settings), Pair(@Composable { Edit() }, @Composable { Editor() }))
                    )

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
                                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black), //TODO: 移除此行
                                scrollBehavior = viewModel.scrollBehavior
                            ) {
                                NavHost(navController = navController, startDestination = items[0].first.first.toString()) {
                                    items.forEach { item ->
                                        composable(item.first.first.toString()) { item.second.second() }
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            ChouNavigationBar(modifier = Modifier.navigationBarsPadding()) {
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
                                composable(item.first.first.toString()) { item.second.first() }
                            }
                        }
                    }
                }
            }
        }
    }
}
