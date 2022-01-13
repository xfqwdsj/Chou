package xyz.xfqlittlefan.chou.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection

fun PaddingValues.plus(another: PaddingValues, layoutDirection: LayoutDirection): PaddingValues {
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection) + another.calculateStartPadding(layoutDirection),
        top = this.calculateTopPadding() + another.calculateTopPadding(),
        end = this.calculateEndPadding(layoutDirection) + another.calculateEndPadding(layoutDirection),
        bottom = this.calculateBottomPadding() + another.calculateBottomPadding()
    )
}