package xyz.xfqlittlefan.chou

import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.xfqlittlefan.chou.ui.components.Edit
import xyz.xfqlittlefan.chou.ui.components.Main

class ActivityViewModel : ViewModel() {
    private val homeScrollState = ScrollState(0)
    private var editingScrollState = LazyListState()
    var currentScrollState: Any = homeScrollState

    private val offset
        get() = when (currentScrollState) {
            is ScrollState -> (currentScrollState as ScrollState).value
            is LazyListState -> (currentScrollState as LazyListState).firstVisibleItemScrollOffset
            else -> 0
        }
    val fraction
        get() = if (offset > 0) 1f else 0f

    var list by mutableStateOf(listOf<Item>())
        private set

    @OptIn(ExperimentalMaterial3Api::class)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var state by mutableStateOf(0)

    var current by mutableStateOf(0)

    var isEditing: Int? by mutableStateOf(null)

    val screenList = listOf(
        Screen("home", R.string.chou_page, Icons.Default.Home, homeScrollState) { Main(this, homeScrollState) },
        Screen("editing", R.string.editing_page, Icons.Default.Settings, editingScrollState) { Edit(this, editingScrollState) }
    )

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
        editingScrollState = LazyListState()
        state = 0
        current = 0
    }

    class Item {
        var value by mutableStateOf("")
    }

    class Screen(val route: String, @StringRes val resId: Int, val icon: ImageVector, val state: Any, val component: @Composable () -> Unit)
}
