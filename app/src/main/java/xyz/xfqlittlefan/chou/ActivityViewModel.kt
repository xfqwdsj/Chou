package xyz.xfqlittlefan.chou

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ActivityViewModel : ViewModel() {
    @OptIn(ExperimentalMaterialApi::class)
    val scaffoldState = BottomSheetScaffoldState(
        DrawerState(DrawerValue.Closed),
        BottomSheetState(BottomSheetValue.Collapsed),
        SnackbarHostState()
    )

    @OptIn(ExperimentalMaterialApi::class)
    private val sheetState
        get() = scaffoldState.bottomSheetState

    @OptIn(ExperimentalMaterialApi::class)
    val sheetProgress
        get() = sheetState.progress

    @OptIn(ExperimentalMaterialApi::class)
    val dragging: Boolean
        get() = sheetState.progress.fraction < 1f

    var visible by mutableStateOf(false)

    val listState = LazyListState()

    var list by mutableStateOf(listOf<Item>())
        private set

    private var _offset by mutableStateOf(0f)

    var offset
        get() = _offset
        set(value) {
            if (value >= 0f && (value / 50).roundToInt() <= 20) {
                _offset = value
            }
        }

    var state by mutableStateOf(0)

    @OptIn(DelicateCoroutinesApi::class)
    fun add(quantity: Int) {
        GlobalScope.launch {
            list = List(quantity) { Item() }
            visible = true
        }
    }

    fun clear() {
        visible = false
    }

    class Item {
        var string by mutableStateOf("")
        var editingString by mutableStateOf("")
        var editing by mutableStateOf(true)
    }
}