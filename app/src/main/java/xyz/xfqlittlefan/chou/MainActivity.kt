package xyz.xfqlittlefan.chou

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
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
import xyz.xfqlittlefan.chou.ui.theme.ChouTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ActivityViewModel>()

    @OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            ProvideWindowInsets {
                ChouTheme {
                    BottomSheetScaffold(
                        sheetContent = {
                            val elevationOverlay = LocalElevationOverlay.current
                            val topSpacerColor by animateColorAsState(
                                targetValue = if ((viewModel.sheetProgress.to == BottomSheetValue.Expanded
                                            && viewModel.sheetProgress.fraction == 1f)
                                    || (viewModel.sheetProgress.to == BottomSheetValue.Collapsed
                                            && viewModel.sheetProgress.fraction == 0f)
                                ) {
                                    MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
                                } else {
                                    elevationOverlay?.apply(MaterialTheme.colors.surface, LocalAbsoluteElevation.current + BottomSheetScaffoldDefaults.SheetElevation) ?: MaterialTheme.colors.surface
                                },
                                animationSpec = tween(durationMillis = 700)
                            )
                            val draggableBarAlpha by animateFloatAsState(
                                targetValue = if (viewModel.dragging) 0.15f else 0.05f,
                                animationSpec = tween(durationMillis = 500)
                            )
                            Spacer(modifier = Modifier
                                .fillMaxWidth().height(
                                    with(LocalDensity.current) {
                                        when (viewModel.sheetProgress.to) {
                                            BottomSheetValue.Collapsed -> LocalWindowInsets.current.statusBars.top - LocalWindowInsets.current.statusBars.top * viewModel.sheetProgress.fraction
                                            BottomSheetValue.Expanded -> LocalWindowInsets.current.statusBars.top * viewModel.sheetProgress.fraction
                                            else -> 0f
                                        }.toDp()
                                    }
                                ).background(color = topSpacerColor))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                    Spacer(
                                        modifier = Modifier
                                            .padding(15.dp).width(60.dp).height(5.dp).clip(CircleShape)
                                            .background(color = MaterialTheme.colors.onSurface.copy(alpha = draggableBarAlpha))
                                    )
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth().height(5.dp)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    topSpacerColor,
                                                    androidx.compose.ui.graphics.Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            with(LocalDensity.current) {
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))) {
                                    var dragging by remember { mutableStateOf(false) }
                                    val alpha by animateFloatAsState(targetValue = if (dragging) 0.15f else 0.1f)
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = !viewModel.visible,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth()
                                                //.clip(RoundedCornerShape(10.dp))
                                                .background(color = MaterialTheme.colors.onSurface.copy(alpha = alpha))
                                                .draggable(
                                                    state = rememberDraggableState {
                                                        if (!viewModel.visible) viewModel.offset += it.toDp().value
                                                    }, orientation = Orientation.Horizontal,
                                                    onDragStarted = { dragging = true },
                                                    onDragStopped = { dragging = false }
                                                ).padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                Text(text = stringResource(id = R.string.drag))
                                            }
                                        }
                                    }
                                }
                            }
                            TextButton(
                                onClick = {
                                    if (viewModel.visible) {
                                        viewModel.clear()
                                    } else {
                                        viewModel.add((viewModel.offset / 50).roundToInt())
                                    }
                                },
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
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
                                            text = stringResource(id = if (it) R.string.reset else R.string.add_item)
                                        )
                                        AnimatedContent(
                                            targetState = (viewModel.offset / 50).roundToInt(),
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
                                            LaunchedEffect(Unit) {
                                                item.state.targetState = true
                                            }
                                            androidx.compose.animation.AnimatedVisibility(
                                                visibleState = item.state,
                                                modifier = Modifier.fillMaxWidth(),
                                                enter = expandVertically(),
                                                exit = shrinkVertically()
                                            ) {
                                                Card(
                                                    modifier = Modifier
                                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                                        .fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)),
                                                    elevation = 0.dp
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(20.dp),
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
                                }
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth().height(5.dp)
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
                        },
                        modifier = Modifier.fillMaxSize(),
                        scaffoldState = viewModel.scaffoldState,
                        topBar = {
                            TopAppBar(
                                title = { Text(text = stringResource(id = R.string.app_name)) },
                                contentPadding = rememberInsetsPaddingValues(insets = LocalWindowInsets.current.statusBars)
                            )
                        },
                        sheetShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                        sheetPeekHeight = with(LocalDensity.current) {
                            LocalWindowInsets.current.navigationBars.bottom.toDp() + 70.dp
                        }
                    ) {

                    }
                }
            }
        }
    }
}