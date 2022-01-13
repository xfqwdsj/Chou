package xyz.xfqlittlefan.chou.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ChouAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.smallTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
            scrollState: Any
) {
    val offset = when (scrollState) {
        is ScrollState -> scrollState.value
        is LazyListState -> scrollState.firstVisibleItemScrollOffset
        else -> 0
    }
    val fraction = if (offset > 0) 1f else 0f
    val backgroundColor by colors.containerColor(
        scrollFraction = fraction
    )
    val foregroundColors = TopAppBarDefaults.smallTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent
    )
    Box(modifier = Modifier.background(backgroundColor)) {
        SmallTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = foregroundColors,
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
fun ChouNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = 3.dp,
    content: @Composable RowScope.() -> Unit
) {
    Box(modifier = Modifier.background(containerColor)) {
        NavigationBar(
            modifier = modifier,
            containerColor = Color.Transparent,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            content = content
        )
    }
}