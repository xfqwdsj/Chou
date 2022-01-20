package xyz.xfqlittlefan.chou.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

fun PaddingValues.plus(another: PaddingValues, layoutDirection: LayoutDirection): PaddingValues {
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection) + another.calculateStartPadding(layoutDirection),
        top = this.calculateTopPadding() + another.calculateTopPadding(),
        end = this.calculateEndPadding(layoutDirection) + another.calculateEndPadding(layoutDirection),
        bottom = this.calculateBottomPadding() + another.calculateBottomPadding()
    )
}