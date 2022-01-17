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

class AnimatedFloatValue(initialValue: Float, private val coroutineScope: CoroutineScope) {
    private var useAnimatedValue by mutableStateOf(false)
    private var _value by mutableStateOf(initialValue)
    private var _animatedValue = Animatable(initialValue)
    var value
        get() = if (useAnimatedValue) _animatedValue.value else _value
        set(value) {
            useAnimatedValue = false
            _value = value
        }

    fun animatedTo(value: Float) {
        _animatedValue = Animatable(_value)
        useAnimatedValue = true
        coroutineScope.launch { _animatedValue.animateTo(value) }
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): Float = value

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: Float) {
        this.value = value
    }
}