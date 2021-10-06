package xyz.xfqlittlefan.chou.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Dialog(
    title: String,
    buttons: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.wrapContentSize(), shape = RoundedCornerShape(10.dp)) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .weight(weight = 1f, fill = false)
                ) { content() }
                Spacer(modifier = Modifier.height(24.dp))
                if (buttons != null) {
                    Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                        ButtonsRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 12.dp, content = buttons)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun Dialog(
    title: String,
    onDismissRequest: () -> Unit = { },
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Dialog(
        title = title,
        buttons = if (onConfirm != null || onDismiss != null) {
            {
                if (onDismiss != null) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                }
                if (onConfirm != null) {
                    TextButton(onClick = onConfirm) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            }
        } else null,
        onDismissRequest = onDismissRequest,
        content = content
    )
}

@Composable
private fun ButtonsRow(
    mainAxisSpacing: Dp,
    crossAxisSpacing: Dp,
    content: @Composable () -> Unit
) {
    Layout(content) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        val childConstraints = Constraints(maxWidth = constraints.maxWidth)

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
                    placeable.width <= constraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                crossAxisSpace += crossAxisSpacing.roundToPx()
            }
            sequences += currentSequence.toList()
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

            currentSequence.clear()
            currentMainAxisSize = 0
            currentCrossAxisSize = 0
        }

        for (measurable in measurables) {
            // Ask the child for its preferred size.
            val placeable = measurable.measure(childConstraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacing.roundToPx()
            }
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) startNewSequence()

        val mainAxisLayoutSize = if (constraints.maxWidth != Constraints.Infinity) {
            constraints.maxWidth
        } else {
            max(mainAxisSpace, constraints.minWidth)
        }
        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

        layout(mainAxisLayoutSize, crossAxisLayoutSize) {
            sequences.forEachIndexed { i, placeables ->
                val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                    placeables[j].width +
                            if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
                }
                val arrangement = Arrangement.Bottom
                // Handle vertical direction
                val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
                with(arrangement) {
                    arrange(mainAxisLayoutSize, childrenMainAxisSizes, mainAxisPositions)
                }
                placeables.forEachIndexed { j, placeable ->
                    placeable.place(
                        x = mainAxisPositions[j],
                        y = crossAxisPositions[i]
                    )
                }
            }
        }
    }
}
