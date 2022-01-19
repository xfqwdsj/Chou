package xyz.xfqlittlefan.chou

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import xyz.xfqlittlefan.chou.ui.components.Edit
import xyz.xfqlittlefan.chou.ui.components.Main
import kotlin.math.roundToInt

class ActivityViewModel : ViewModel() {
    private val homeScrollState = ScrollState(0)
    private var editScrollState = LazyListState()
    var currentScrollState: Any by mutableStateOf(homeScrollState)
    var globalScreen: String? by mutableStateOf(null)

    private val scrollOffset
        get() = when (currentScrollState) {
            is ScrollState -> (currentScrollState as ScrollState).value
            is LazyListState -> (currentScrollState as LazyListState).firstVisibleItemScrollOffset
            else -> 0
        }
    val scrollFraction
        get() = if (scrollOffset > 0) 1f else 0f

    var itemList by mutableStateOf(listOf<Item>())
        private set

    @OptIn(ExperimentalMaterial3Api::class)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // 0: 空闲  1: 正在选取  2: 已出结果
    var appState by mutableStateOf(0)

    var currentItem by mutableStateOf(0)

    var isEditing by mutableStateOf(false)
    var editingItem by mutableStateOf(-1)
    var editingValue by mutableStateOf(TextFieldValue(text = ""))

    val screenList = listOf(
        Screen("home", R.string.home, Icons.Default.Home, homeScrollState) { padding, navigateTo -> Main(padding, this, "home", homeScrollState, navigateTo) },
        Screen("edit", R.string.edit, Icons.Default.Edit, editScrollState) { padding, navigateTo -> Edit(padding, this, "edit", editScrollState, navigateTo) }
    )

    val itemTypeList = listOf(
        R.string.item_type_0,
        R.string.item_type_1
    )

    suspend fun addItem(position: Int) {
            if (isEditing && position <= editingItem) editingItem++
            itemList = itemList.toMutableList().apply { add(position, Item()) }
        editScrollState.animateScrollToItem(position)
    }

    fun removeItem(position: Int) {
            if (editingItem == position) isEditing = false
            if (isEditing && position < editingItem) editingItem--
            itemList = itemList.toMutableList().apply { removeAt(position) }
    }

    fun editItem(position: Int) {
        isEditing = true
        editingItem = position
        globalScreen = "edit"
        val initValue = itemList[editingItem].value
        editingValue = TextFieldValue(
            text = initValue,
            selection = TextRange(index = initValue.length)
        )
    }

    val cancelEditing =  {
        isEditing = false
        globalScreen = null
    }

    val confirmEditing = {
        cancelEditing()
        itemList[editingItem].value = editingValue.text
    }

    val onTextFieldValueChanged: (TextFieldValue) -> Unit = {
        editingValue = it
    }

    @OptIn(DelicateCoroutinesApi::class)
    val startSelecting: () -> Unit = {
        appState = 1
        globalScreen = "home"
        isEditing = false
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
        globalScreen = null
    }

    class Item {
        // 0: 文字  1: 范围
        var type by mutableStateOf(R.string.item_type_0)
        var value by mutableStateOf("")
    }

    class Screen(val route: String, @StringRes val resId: Int, val icon: ImageVector, val state: Any, val component: @Composable (PaddingValues, (String) -> Unit) -> Unit)
}
