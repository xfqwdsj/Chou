/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.xfqlittlefan.chou.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Suppress("ModifierParameter")
@Composable
fun ChouDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandIn(animationSpec = spring()),
    exit: ExitTransition = shrinkOut(animationSpec = spring()) + fadeOut(),
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) {
    var showPopup by remember { mutableStateOf(expanded) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) showPopup = true else visible = false
    }

    if (showPopup) {
        Popup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = DropdownMenuPositionProvider(offset, LocalDensity.current),
            properties = properties
        ) {
            LaunchedEffect(Unit) {
                visible = true
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                tonalElevation = MenuElevation,
                shadowElevation = MenuElevation
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = enter,
                    exit = exit
                ) {
                    DisposableEffect(Unit) {
                        onDispose {
                            showPopup = false
                        }
                    }

                    Column(
                        modifier = modifier
                            .padding(vertical = DropdownMenuVerticalPadding)
                            .width(IntrinsicSize.Max)
                            .verticalScroll(rememberScrollState()),
                        content = content
                    )
                }
            }
        }
    }
}

private val MenuElevation = 8.dp
internal val MenuVerticalMargin = 48.dp
internal val DropdownMenuVerticalPadding = 8.dp

@Immutable
internal data class DropdownMenuPositionProvider(
    val contentOffset: DpOffset,
    val density: Density
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val verticalMargin = with(density) { MenuVerticalMargin.roundToPx() }
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        val toRight = anchorBounds.left + contentOffsetX
        val toLeft = anchorBounds.right - contentOffsetX - popupContentSize.width
        val toDisplayRight = windowSize.width - popupContentSize.width
        val toDisplayLeft = 0
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                toRight,
                toLeft,
                if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
            )
        } else {
            sequenceOf(
                toLeft,
                toRight,
                if (anchorBounds.right <= windowSize.width) toDisplayLeft else toDisplayRight
            )
        }.firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: toLeft

        val toBottom = maxOf(anchorBounds.bottom + contentOffsetY, verticalMargin)
        val toTop = anchorBounds.top - contentOffsetY - popupContentSize.height
        val toCenter = anchorBounds.top - popupContentSize.height / 2
        val toDisplayBottom = windowSize.height - popupContentSize.height - verticalMargin
        val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
            it >= verticalMargin &&
                    it + popupContentSize.height <= windowSize.height - verticalMargin
        } ?: toTop

        return IntOffset(x, y)
    }
}
