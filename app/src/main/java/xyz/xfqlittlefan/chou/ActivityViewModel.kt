package xyz.xfqlittlefan.chou

import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.xfqlittlefan.chou.ui.components.Edit
import xyz.xfqlittlefan.chou.ui.components.Main

class ActivityViewModel : ViewModel() {
    private val homeScrollState = ScrollState(0)
    private var editScrollState = LazyListState()
    var currentScrollState: Any by mutableStateOf(homeScrollState)

    private val offset
        get() = when (currentScrollState) {
            is ScrollState -> (currentScrollState as ScrollState).value
            is LazyListState -> (currentScrollState as LazyListState).firstVisibleItemScrollOffset
            else -> 0
        }
    val fraction
        get() = if (offset > 0) 1f else 0f

    var itemList by mutableStateOf(listOf<Item>())
        private set

    @OptIn(ExperimentalMaterial3Api::class)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // 0: 空闲  1: 正在选取  2: 已出结果
    var appState by mutableStateOf(0)

    var currentItem by mutableStateOf(0)

    var editing: Int? by mutableStateOf(null)
    var editingValue by mutableStateOf(TextFieldValue(text = ""))

    val screenList = listOf(
        Screen("home", R.string.home, Icons.Default.Home, homeScrollState) { Main(this, homeScrollState) },
        Screen("edit", R.string.edit, Icons.Default.Edit, editScrollState) { Edit(this, editScrollState) }
    )

    @OptIn(DelicateCoroutinesApi::class)
    fun addItem(position: Int) {
        GlobalScope.launch {
            if (editing != null && position <= editing ?: -1) editing = (editing ?: -1) + 1
            itemList = itemList.toMutableList().apply { add(position, Item()) }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun removeItem(position: Int) {
        GlobalScope.launch {
            if (editing == position) editing = null
            if (editing != null && position < editing ?: 0) editing = (editing ?: 1) - 1
            itemList = itemList.toMutableList().apply { removeAt(position) }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startSelecting() {
        appState = 1
        editing = null
        val time = ((4 - (10 / (itemList.size + 2.5))) * 1000).toLong()
        val job = GlobalScope.launch {
            while (true) {
                if (itemList.size >= 6) {
                    currentItem = (itemList.indices).random()
                } else {
                    if (currentItem + 1 == itemList.size) currentItem = 0 else currentItem++
                }
                delay(150)
            }
        }
        GlobalScope.launch {
            delay(time)
            job.cancel()
            currentItem = itemList.indices.random()
            appState = 2
        }
    }

    fun resetState() {
        appState = 0
        currentItem = 0
    }

    class Item {
        // 0: 文字  1: 范围
        var type by mutableStateOf(R.string.item_type_0)
        var value by mutableStateOf("")
    }

    class Screen(val route: String, @StringRes val resId: Int, val icon: ImageVector, val state: Any, val component: @Composable () -> Unit)
}
