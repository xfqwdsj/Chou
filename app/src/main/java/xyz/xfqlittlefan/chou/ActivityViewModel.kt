package xyz.xfqlittlefan.chou

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val sheetFraction
        get() = when (sheetState.progress.to) {
            BottomSheetValue.Expanded -> sheetState.progress.fraction
            BottomSheetValue.Collapsed -> 1f - sheetState.progress.fraction
            else -> 0f
        }

    @OptIn(ExperimentalMaterialApi::class)
    val dragging: Boolean
        get() = sheetState.progress.fraction < 1f

    var visible by mutableStateOf(false)

    var listState = LazyListState()

    var list by mutableStateOf(listOf<Item>())
        private set

    var state by mutableStateOf(0)

    var current by mutableStateOf(0)

    var editing: Int? by mutableStateOf(null)

    @OptIn(DelicateCoroutinesApi::class)
    fun add(position: Int) {
        GlobalScope.launch {
            list = list.toMutableList().apply { add(position, Item()) }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun remove(position: Int) {
        GlobalScope.launch {
            list = list.toMutableList().apply { removeAt(position) }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        state = 1
        val time = (list.size * (500..700).random()).toLong()
        val job = GlobalScope.launch {
            while (true) {
                if (current + 1 == list.size) current = 0 else current++
                delay(150)
            }
        }
        GlobalScope.launch {
            delay(time)
            job.cancel()
            current = list.indices.random()
            state = 2
        }
    }

    fun reset() {
        listState = LazyListState()
        state = 0
        current = 0
    }

    class Item {
        var value by mutableStateOf("")
        var visible by mutableStateOf(false)
    }
}
