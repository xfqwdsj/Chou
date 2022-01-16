/*
 * Copyright 2021 The Android Open Source Project
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

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import xyz.xfqlittlefan.chou.ui.round
import kotlin.math.roundToInt

@Composable
fun NestedScrollLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    nestedScrollBehavior: NestedScrollBehavior,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit
) {
    Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
        SubcomposeLayout { constraints ->
            val layoutWidth = constraints.maxWidth
            val layoutHeight = constraints.maxHeight

            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            layout(layoutWidth, layoutHeight) {
                val topBarPlaceableList = subcompose(NestedScrollLayoutContent.TopBar, topBar).map {
                    it.measure(looseConstraints)
                }

                val topBarHeight = topBarPlaceableList.maxByOrNull { it.height }?.height ?: 0
                nestedScrollBehavior.offsetLimit = (-topBarHeight).toFloat()

                val bodyContentHeight = layoutHeight - topBarHeight - nestedScrollBehavior.offset.roundToInt()

                val bodyContentPlaceableList = subcompose(NestedScrollLayoutContent.MainContent, content).map {
                    it.measure(looseConstraints.copy(maxHeight = bodyContentHeight))
                }

                bodyContentPlaceableList.forEach {
                    it.place(0, topBarHeight + nestedScrollBehavior.offset.roundToInt())
                }
                topBarPlaceableList.forEach {
                    it.place(0, 0 + nestedScrollBehavior.offset.roundToInt())
                }
            }
        }
    }
}

class NestedScrollBehavior(private val coroutineScope: CoroutineScope) {
    var scrollMode by mutableStateOf(MODE_CONTENT_FIRST)
    internal var offsetLimit by mutableStateOf(-Float.MAX_VALUE)
    var offset
        get() = _offset.value
        set(value) {
            coroutineScope.launch { _offset.snapTo(value) }
        }
    private val _offset = Animatable(0f)
    var contentOffset
        get() = _contentOffset.value
        set(value) {
            coroutineScope.launch { _contentOffset.snapTo(value) }
        }
    private val _contentOffset = Animatable(0f)
    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val newOffset = (offset + available.y)
            val coerced = newOffset.coerceIn(minimumValue = offsetLimit, maximumValue = 0f)
            return if (newOffset == coerced) {
                offset = coerced
                available.copy(x = 0f)
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            contentOffset += consumed.y
            if (offset == 0f || offset == offsetLimit) {
                if (consumed.y == 0f && available.y > 0f) {
                    contentOffset = 0f
                }
            }
            offset = (offset + consumed.y).coerceIn(
                minimumValue = offsetLimit,
                maximumValue = 0f
            )
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            animateSetOffset(round(offset, offsetLimit, 0f))
            return Velocity.Zero
        }
    }

    private fun animateSetOffset(to: Float) {
        coroutineScope.launch { _offset.animateTo(to) }
    }
}

private const val MODE_TOP_FIRST = 0
private const val MODE_CONTENT_FIRST = 1

private enum class NestedScrollLayoutContent { TopBar, MainContent }