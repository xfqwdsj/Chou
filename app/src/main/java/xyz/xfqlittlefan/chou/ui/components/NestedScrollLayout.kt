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

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
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
import xyz.xfqlittlefan.chou.ui.AnimatedFloatValue
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
                nestedScrollBehavior.maxHeight = layoutHeight.toFloat()

                val topBarPlaceableList = subcompose(NestedScrollLayoutContent.TopBar, topBar).map {
                    it.measure(looseConstraints)
                }

                val topBarY = AnimatedFloatValue(nestedScrollBehavior.offset, nestedScrollBehavior.coroutineScope)
                val topBarHeight = if (nestedScrollBehavior.scrollMode == MODE_TOP_ONLY) layoutHeight else topBarPlaceableList.maxByOrNull { it.height }?.height ?: 0
                nestedScrollBehavior.offsetLimit = (-topBarHeight).toFloat()

                val bodyY = AnimatedFloatValue(nestedScrollBehavior.blankOffset - nestedScrollBehavior.offsetLimit, nestedScrollBehavior.coroutineScope)
                val bodyContentHeight = layoutHeight - bodyY.value.roundToInt()

                nestedScrollBehavior.onScrollModeChanged = {
                    when (it) {
                        MODE_TOP_ONLY -> {
                            topBarY.animatedTo(0f)
                            bodyY.animatedTo(topBarHeight.toFloat())
                        }
                        MODE_CONTENT_FIRST -> {
                            topBarY.animatedTo(nestedScrollBehavior.offset)
                            bodyY.animatedTo(nestedScrollBehavior.blankOffset - nestedScrollBehavior.offsetLimit)
                        }
                    }
                }

                val bodyContentPlaceableList = subcompose(NestedScrollLayoutContent.MainContent, content).map {
                    it.measure(looseConstraints.copy(maxHeight = bodyContentHeight))
                }

                bodyContentPlaceableList.forEach {
                    it.place(0, bodyY.value.roundToInt())
                }
                topBarPlaceableList.forEach {
                    it.place(0, topBarY.value.roundToInt())
                }
            }
        }
    }
}

class NestedScrollBehavior(internal val coroutineScope: CoroutineScope) {
    var scrollMode
        get() = _scrollMode
        set(value) {
            _scrollMode = value
            onScrollModeChanged(value)
        }
    private var _scrollMode by mutableStateOf(MODE_CONTENT_FIRST)
    internal var onScrollModeChanged: (Int) -> Unit = {}
    val scrollFraction
        get() = if (offsetLimit != 0f) {
            1 - ((offsetLimit - contentOffset).coerceIn(
                minimumValue = offsetLimit,
                maximumValue = 0f
            ) / offsetLimit)
        } else {
            0f
        }
    internal var offset
        get() = _offset.value
        set(value) { _offset.value = value }
    private var _offset = AnimatedFloatValue(0f, coroutineScope)
    internal var blankOffset
        get() = _blankOffset.value
        set(value) { _blankOffset.value = value }
    private var _blankOffset = AnimatedFloatValue(0f, coroutineScope)
    private var contentOffset by mutableStateOf(0f)
    internal var offsetLimit by mutableStateOf(-Float.MAX_VALUE)
    internal var maxHeight by mutableStateOf(Float.MAX_VALUE)
    private var direction by mutableStateOf(0f)
    private var previousOffset by mutableStateOf(0f)
    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            return if (scrollMode == MODE_CONTENT_FIRST) {
                direction += available.y
                offset = (offset + available.y).coerceIn(minimumValue = offsetLimit, maximumValue = 0f)
                val newBlankOffset = (blankOffset + available.y).coerceIn(minimumValue = offsetLimit, maximumValue = 0f)
                val difference = newBlankOffset - blankOffset
                if (available.y < 0f) blankOffset = newBlankOffset
                Log.w("!!!Chou!!!", "1\noffset=$offset\nblankOffset=$blankOffset\nconsumed=${if (difference < 0f) difference else 0f}")
                Offset(x = 0f, y = if (difference < 0f) difference else 0f)
            }
            else {
                available
            }
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            return if (scrollMode == MODE_CONTENT_FIRST) {
                contentOffset += consumed.y
                direction = consumed.y + available.y
                if (offset == 0f || offset == offsetLimit) {
                    if (available.y > 0f) {
                        contentOffset = 0f
                    }
                }
                val newOffset = (blankOffset + available.y).coerceIn(minimumValue = offsetLimit, maximumValue = 0f)
                val difference = newOffset - blankOffset
                if (available.y > 0f) blankOffset = newOffset
                Log.i("!!!Chou!!!", "2\nconsumed=${consumed.y}\navailable=${available.y}\ncommitted=${if (available.y > 0f) difference else 0f}")
                Offset(x = 0f, y = if (available.y > 0f) difference else 0f)
            } else {
                available
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            return if (scrollMode == MODE_CONTENT_FIRST) Velocity.Zero else available
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            return if (scrollMode == MODE_CONTENT_FIRST) {
                animateSetOffset(
                    when {
                        direction > 0 -> 0f
                        direction < 0 -> offsetLimit
                        else -> previousOffset
                    }
                )
                Log.e("!!!Chou!!!", "4\noffset=$offset\nblankOffset=$blankOffset\ndirection=$direction")
                direction = 0f
                Velocity.Zero
            } else {
                available
            }
        }
    }

    private fun animateSetOffset(to: Float) {
        previousOffset = to
        if (offset != offsetLimit && offset != 0f) _offset.animatedTo(to)
        if (blankOffset != offsetLimit && blankOffset != 0f) _blankOffset.animatedTo(to)
    }
}

private const val MODE_TOP_ONLY = 0
private const val MODE_CONTENT_FIRST = 1

private enum class NestedScrollLayoutContent { TopBar, MainContent }