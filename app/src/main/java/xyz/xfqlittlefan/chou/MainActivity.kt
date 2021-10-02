package xyz.xfqlittlefan.chou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import xyz.xfqlittlefan.chou.ui.theme.ChouTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ActivityViewModel>()

    @OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val controller = rememberSystemUiController()
            SideEffect {
                controller.setNavigationBarColor(color = androidx.compose.ui.graphics.Color.Transparent)
            }

            ProvideWindowInsets {
                ChouTheme {
                    val cardRadius = animateDpAsState(
                        targetValue = (if (viewModel.sheetFraction == 1f) 0 else 20).dp
                    )
                    val peekHeight = animateDpAsState(
                        targetValue = if (viewModel.state == 0) with(LocalDensity.current) {
                            LocalWindowInsets.current.navigationBars.bottom.toDp() + 100.dp
                        } else 0.dp
                    )

                    BottomSheetScaffold(
                        sheetContent = {
                            val isLight = MaterialTheme.colors.isLight
                            SideEffect {
                                controller.setStatusBarColor(
                                    color = androidx.compose.ui.graphics.Color.Transparent,
                                    darkIcons = isLight && viewModel.sheetFraction >= 0.9f
                                )
                            }

                            with(LocalDensity.current) {
                                val elevationOverlay = LocalElevationOverlay.current
                                val draggableBarAlpha by animateFloatAsState(
                                    targetValue = if (viewModel.dragging) 0.15f else 0.05f,
                                    animationSpec = tween(durationMillis = 500)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((LocalWindowInsets.current.statusBars.top * viewModel.sheetFraction).toDp())
                                )
                                BoxWithConstraints(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val width by animateDpAsState(
                                        targetValue = (constraints.maxWidth * if (viewModel.dragging) 0.25f else 0.15f).toDp()
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .padding(15.dp)
                                            .width(width)
                                            .height(5.dp)
                                            .clip(CircleShape)
                                            .background(color = MaterialTheme.colors.onSurface.copy(alpha = draggableBarAlpha))
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                var dragging by remember { mutableStateOf(false) }
                                val alpha by animateFloatAsState(targetValue = if (dragging) 0.15f else 0.1f)
                                val enabled = !viewModel.visible && viewModel.sheetFraction == 1f
                                val modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color = MaterialTheme.colors.onSurface.copy(alpha = alpha))
                                Box(
                                    modifier = if (enabled) modifier
                                        .draggable(
                                            state = rememberDraggableState { viewModel.offset += it.toDp().value },
                                            orientation = Orientation.Horizontal,
                                            onDragStarted = { dragging = true },
                                            onDragStopped = { dragging = false }
                                        )
                                        .padding(10.dp) else modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = enabled,
                                        transitionSpec = { fadeIn() with fadeOut() }
                                    ) {
                                        CompositionLocalProvider(LocalContentAlpha provides if (it) ContentAlpha.medium else ContentAlpha.disabled) {
                                            Text(
                                                text = stringResource(id = R.string.drag)
                                                        + if (it) "" else stringResource(id = R.string.disabled)
                                            )
                                        }
                                    }
                                }
                                AnimatedContent(
                                    targetState = viewModel.sheetFraction == 1f,
                                    transitionSpec = { fadeIn() with fadeOut() }
                                ) { enabledIt ->
                                    TextButton(
                                        onClick = {
                                            if (viewModel.visible) {
                                                viewModel.clear()
                                            } else {
                                                viewModel.add(viewModel.quantity)
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        enabled = enabledIt,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        AnimatedContent(
                                            targetState = viewModel.visible,
                                            transitionSpec = {
                                                if (targetState) {
                                                    fadeIn() + slideInVertically({ -it }) with
                                                            slideOutVertically({ it }) + fadeOut()
                                                } else {
                                                    fadeIn() + slideInVertically({ it }) with
                                                            slideOutVertically({ -it }) + fadeOut()
                                                }
                                            }
                                        ) {
                                            Row {
                                                Text(
                                                    text = stringResource(id = if (it) R.string.clear_item else R.string.add_item)
                                                )
                                                AnimatedContent(
                                                    targetState = viewModel.quantity,
                                                    transitionSpec = {
                                                        when {
                                                            targetState > initialState -> {
                                                                fadeIn() + slideInVertically({ it }) with
                                                                        slideOutVertically({ -it }) + fadeOut()
                                                            }
                                                            targetState < initialState -> {
                                                                fadeIn() + slideInVertically({ -it }) with
                                                                        slideOutVertically({ it }) + fadeOut()
                                                            }
                                                            else -> fadeIn() with fadeOut()
                                                        }
                                                    }
                                                ) {
                                                    Text(text = it.toString())
                                                }
                                                Text(text = stringResource(id = R.string.count))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(modifier = Modifier.fillMaxSize()) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = viewModel.visible,
                                        modifier = Modifier.fillMaxSize(),
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            state = viewModel.listState,
                                            contentPadding = rememberInsetsPaddingValues(
                                                insets = LocalWindowInsets.current.navigationBars,
                                                applyBottom = true,
                                                additionalTop = 5.dp
                                            )
                                        ) {
                                            items(viewModel.list) { item ->
                                                Card(
                                                    modifier = Modifier
                                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                                        .fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)),
                                                    elevation = 0.dp
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(20.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        AnimatedContent(
                                                            targetState = item.editing,
                                                            modifier = Modifier.weight(1f),
                                                            transitionSpec = { fadeIn() with fadeOut() },
                                                            contentAlignment = Alignment.Center
                                                        ) { editing ->
                                                            Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))) {
                                                                TextField(
                                                                    value = item.editingString,
                                                                    onValueChange = { item.editingString = it },
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    enabled = editing,
                                                                    shape = RectangleShape
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(15.dp))
                                                        AnimatedVisibility(
                                                            visible = !item.editing,
                                                            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                                                            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                                                        ) {
                                                            Row {
                                                                IconButton(
                                                                    onClick = { item.editing = true }
                                                                ) { Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(id = R.string.edit_item)) }
                                                            }
                                                        }
                                                        AnimatedVisibility(
                                                            visible = item.editing,
                                                            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                                                            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
                                                        ) {
                                                            Row {
                                                                IconButton(
                                                                    onClick = { item.editing = false; item.editingString = item.string }
                                                                ) { Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(id = R.string.cancel)) }
                                                                IconButton(
                                                                    onClick = { item.editing = false; item.string = item.editingString }
                                                                ) { Icon(imageVector = Icons.Filled.Done, contentDescription = stringResource(id = R.string.ok)) }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        elevationOverlay?.apply(MaterialTheme.colors.surface, LocalAbsoluteElevation.current + BottomSheetScaffoldDefaults.SheetElevation) ?: MaterialTheme.colors.surface,
                                                        androidx.compose.ui.graphics.Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        scaffoldState = viewModel.scaffoldState,
                        topBar = {
                            TopAppBar(
                                title = { Text(text = stringResource(id = R.string.app_name)) },
                                contentPadding = rememberInsetsPaddingValues(insets = LocalWindowInsets.current.statusBars),
                                actions = {
                                    AnimatedVisibility(
                                        visible = viewModel.state == 2,
                                        enter = fadeIn() + slideInHorizontally({ it }),
                                        exit = fadeOut() + slideOutHorizontally({ it })
                                    ) {
                                        IconButton(onClick = { viewModel.reset() }) {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = stringResource(id = R.string.reset)
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        sheetShape = RoundedCornerShape(topStart = cardRadius.value, topEnd = cardRadius.value),
                        sheetPeekHeight = peekHeight.value
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(it.calculateTopPadding()))
                            AnimatedContent(
                                targetState = viewModel.visible,
                                transitionSpec = { fadeIn() with fadeOut() }
                            ) { visible ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (visible) {
                                        Row(modifier = Modifier.padding(50.dp)) {
                                            AnimatedContent(
                                                targetState = viewModel.state,
                                                transitionSpec = { fadeIn() with fadeOut() }
                                            ) {
                                                Text(text = stringResource(id = if (it == 2) R.string.chose_item else R.string.current_item), style = MaterialTheme.typography.h6)
                                            }
                                            AnimatedContent(
                                                targetState = viewModel.current,
                                                transitionSpec = {
                                                    fadeIn() + slideInVertically({ it }) with
                                                            slideOutVertically({ -it }) + fadeOut()
                                                }
                                            ) {
                                                Text(text = if (viewModel.list.isEmpty()) "" else viewModel.list[it].string, style = MaterialTheme.typography.h6)
                                            }
                                        }
                                        AnimatedVisibility(visible = viewModel.state == 0) {
                                            Button(
                                                onClick = { viewModel.start() }
                                            ) {
                                                Text(text = stringResource(id = R.string.choose))
                                            }
                                        }
                                    } else {
                                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                                            Text(text = stringResource(id = R.string.no_item))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(it.calculateBottomPadding() + 50.dp))
                        }
                    }
                }
            }
        }
    }
}