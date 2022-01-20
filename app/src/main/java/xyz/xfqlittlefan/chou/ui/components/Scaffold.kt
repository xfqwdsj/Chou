package xyz.xfqlittlefan.chou.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.*

@ExperimentalMaterial3Api
@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    content: ScaffoldScope.() -> Unit
) {
    Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
        SubcomposeLayout { constraints ->
            val scope = ScaffoldScope(
                floatingActionButtonPosition,
                layoutDirection,
                constraints,
                { toDp() },
                { roundToPx() },
                { slotId, content -> subcompose(slotId, content) }
            )
            scope.content()
            layout(scope.layoutWidth, scope.layoutHeight, placementBlock = scope.block)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ScaffoldScope @OptIn(ExperimentalMaterial3Api::class) constructor(
    private val fabPosition: FabPosition,
    private val layoutDirection: LayoutDirection,
    private val constraints: Constraints,
    private val toDp: Int.() -> Dp,
    private val roundToPx: Dp.() -> Int,
    private val subcompose: (Any?, (@Composable () -> Unit)) -> List<Measurable>
) {
    internal val layoutWidth = constraints.maxWidth
    internal val layoutHeight = constraints.maxHeight

    private val components = mutableListOf<Pair<ScaffoldLayoutContent, Any>>()

    fun topBar(content: @Composable () -> Unit) {
        components += Pair(ScaffoldLayoutContent.TopBar, content)
    }

    fun bottomBar(content: @Composable () -> Unit) {
        components += Pair(ScaffoldLayoutContent.BottomBar, content)
    }

    fun content(content: @Composable (PaddingValues) -> Unit) {
        components += Pair(ScaffoldLayoutContent.MainContent, content)
    }

    fun snackbar(content: @Composable () -> Unit) {
        components += Pair(ScaffoldLayoutContent.Snackbar, content)
    }

    fun fab(content: @Composable () -> Unit) {
        components += Pair(ScaffoldLayoutContent.Fab, content)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    internal val block: Placeable.PlacementScope.() -> Unit = {
            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
            var topBar: @Composable () -> Unit = {}
            var bottomBar: @Composable () -> Unit = {}
            var content: @Composable (PaddingValues) -> Unit = {}
            var snackbar: @Composable () -> Unit = {}
            var fab: @Composable () -> Unit = {}

            components.forEach {
                when (it.first) {
                    ScaffoldLayoutContent.TopBar -> topBar = it.second as @Composable () -> Unit
                    ScaffoldLayoutContent.BottomBar -> bottomBar = it.second as @Composable () -> Unit
                    ScaffoldLayoutContent.MainContent -> content = it.second as @Composable (PaddingValues) -> Unit
                    ScaffoldLayoutContent.Snackbar -> snackbar = it.second as @Composable () -> Unit
                    ScaffoldLayoutContent.Fab -> fab = it.second as @Composable () -> Unit
                }
            }

            val topBarPlaceables = subcompose(ScaffoldLayoutContent.TopBar, topBar).map {
                it.measure(looseConstraints)
            }

            val topBarHeight = topBarPlaceables.maxByOrNull { it.height }?.height ?: 0

            val snackbarPlaceables = subcompose(ScaffoldLayoutContent.Snackbar, snackbar).map {
                it.measure(looseConstraints)
            }

            val snackbarHeight = snackbarPlaceables.maxByOrNull { it.height }?.height ?: 0
            val snackbarWidth = snackbarPlaceables.maxByOrNull { it.width }?.width ?: 0

            val fabPlaceables =
                subcompose(ScaffoldLayoutContent.Fab, fab).mapNotNull { measurable ->
                    measurable.measure(looseConstraints).takeIf { it.height != 0 && it.width != 0 }
                }

            val fabPlacement = if (fabPlaceables.isNotEmpty()) {
                val fabWidth = fabPlaceables.maxByOrNull { it.width }!!.width
                val fabHeight = fabPlaceables.maxByOrNull { it.height }!!.height
                val fabLeftOffset = if (fabPosition == FabPosition.End) {
                    if (layoutDirection == LayoutDirection.Ltr) {
                        layoutWidth - FabSpacing.roundToPx() - fabWidth
                    } else {
                        FabSpacing.roundToPx()
                    }
                } else {
                    (layoutWidth - fabWidth) / 2
                }

                FabPlacement(
                    left = fabLeftOffset,
                    width = fabWidth,
                    height = fabHeight
                )
            } else {
                null
            }

            val bottomBarPlaceables = subcompose(ScaffoldLayoutContent.BottomBar) {
                CompositionLocalProvider(
                    LocalFabPlacement provides fabPlacement,
                    content = bottomBar
                )
            }.map { it.measure(looseConstraints) }

            val bottomBarHeight = bottomBarPlaceables.maxByOrNull { it.height }?.height ?: 0
            val fabOffsetFromBottom = fabPlacement?.let {
                if (bottomBarHeight == 0) {
                    it.height + FabSpacing.roundToPx()
                } else {
                    bottomBarHeight + it.height + FabSpacing.roundToPx()
                }
            }

            val snackbarOffsetFromBottom = if (snackbarHeight != 0) {
                snackbarHeight + (fabOffsetFromBottom ?: bottomBarHeight)
            } else {
                0
            }

            val bodyContentPlaceables = subcompose(ScaffoldLayoutContent.MainContent) {
                val innerPadding = PaddingValues(top = topBarHeight.toDp(), bottom = bottomBarHeight.toDp())
                content(innerPadding)
            }.map { it.measure(looseConstraints) }

            components.forEach { pair ->
                when (pair.first) {
                    ScaffoldLayoutContent.TopBar -> topBarPlaceables.forEach { it.place(0, 0) }
                    ScaffoldLayoutContent.BottomBar -> bottomBarPlaceables.forEach { it.place(0, layoutHeight - bottomBarHeight) }
                    ScaffoldLayoutContent.MainContent -> bodyContentPlaceables.forEach { it.place(0, 0) }
                    ScaffoldLayoutContent.Snackbar -> snackbarPlaceables.forEach {
                        it.place(
                            (layoutWidth - snackbarWidth) / 2,
                            layoutHeight - snackbarOffsetFromBottom
                        )
                    }
                    ScaffoldLayoutContent.Fab -> fabPlacement?.let { placement ->
                        fabPlaceables.forEach {
                            it.place(placement.left, layoutHeight - fabOffsetFromBottom!!)
                        }
                    }
                }
            }
    }
}

@Immutable
internal class FabPlacement(
    val left: Int,
    val width: Int,
    val height: Int
)

internal val LocalFabPlacement = staticCompositionLocalOf<FabPlacement?> { null }

private val FabSpacing = 16.dp

private enum class ScaffoldLayoutContent { TopBar, BottomBar, MainContent, Snackbar, Fab }