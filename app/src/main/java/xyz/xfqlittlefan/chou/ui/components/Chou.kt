package xyz.xfqlittlefan.chou.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.cutoutPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import xyz.xfqlittlefan.chou.ActivityViewModel
import xyz.xfqlittlefan.chou.R
import xyz.xfqlittlefan.chou.ui.plus

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Main(viewModel: ActivityViewModel, state: ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(50.dp))
        AnimatedContent(
            targetState = viewModel.itemList.isNotEmpty(),
            transitionSpec = { fadeIn() with fadeOut() }
        ) { visible ->
            Column(
                modifier = Modifier
                    .systemBarsPadding(top = false, bottom = false)
                    .cutoutPadding(top = false, bottom = false), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (visible) {
                    AnimatedContent(
                        targetState = viewModel.appState,
                        modifier = Modifier.padding(horizontal = 30.dp),
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) {
                        Text(text = stringResource(id = if (it == 2) R.string.chose_item else R.string.current_item), style = MaterialTheme.typography.titleMedium)
                    }
                    AnimatedContent(
                        targetState = viewModel.currentItem,
                        modifier = Modifier
                            .padding(30.dp)
                            .fillMaxWidth(),
                        transitionSpec = {
                            (fadeIn() + slideInVertically(initialOffsetY = { it }) with
                                    slideOutVertically(targetOffsetY = { -it }) + fadeOut()).using(SizeTransform(clip = false))
                        }
                    ) {
                        Text(
                            text = if (viewModel.itemList.isEmpty()) "" else viewModel.itemList[it].value,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    AnimatedVisibility(visible = viewModel.appState == 0) {
                        Button(
                            onClick = { viewModel.startSelecting() }
                        ) {
                            Text(text = stringResource(id = R.string.choose))
                        }
                    }
                } else {
                    CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = 0.38f)) {
                        Text(text = stringResource(id = R.string.no_item))
                    }
                }
            }
        }
        Spacer(Modifier.height(50.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun Edit(viewModel: ActivityViewModel, state: LazyListState) {
    Scaffold(
        topBar = {
            val background by TopAppBarDefaults.smallTopAppBarColors().containerColor(
                scrollFraction = viewModel.fraction
            )
            Column(
                modifier = Modifier
                    .background(color = background)
                    .systemBarsPadding(top = false, bottom = false)
                    .cutoutPadding(top = false, bottom = false)
            ) {
                AnimatedVisibility(visible = viewModel.editing != null) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                        Spacer(Modifier.height(5.dp))
                        when (viewModel.editing?.let { viewModel.itemList[it].type }) {
                            R.string.item_type_0 -> {
                                val requester = FocusRequester()

                                SideEffect {
                                    requester.requestFocus()
                                }

                                Box(
                                    modifier = Modifier.clip(shape = RoundedCornerShape(size = 10.dp))
                                ) {
                                    val context = LocalContext.current
                                    TextField(
                                        value = viewModel.editingValue,
                                        onValueChange = {
                                            Toast.makeText(context, "${viewModel.editingValue}\n$it", Toast.LENGTH_LONG).show()
                                            viewModel.editingValue = it
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(requester)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ButtonWithIcon(label = stringResource(id = android.R.string.cancel), icon = Icons.Default.Close) {
                                viewModel.editing = null
                            }
                            ButtonWithIcon(label = stringResource(id = android.R.string.ok), icon = Icons.Default.Done) {
                                viewModel.itemList[viewModel.editing!!].value = viewModel.editingValue.text
                                viewModel.editing = null
                            }
                        }
                    }
                }
                Spacer(Modifier.height(5.dp))
                TextButton(
                    onClick = {
                        viewModel.addItem(0)
                    },
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(size = 10.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.add_item)
                    )
                }
            }
        }
    ) { padding ->
        val radius by animateDpAsState(targetValue = if (viewModel.editing != null) 10.dp else 0.dp)
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .let { if (radius > 0.dp) it.blur(radius) else it },
            state = state,
            contentPadding = rememberInsetsPaddingValues(insets = LocalWindowInsets.current.systemBars, applyTop = false, applyBottom = false)
                .plus(rememberInsetsPaddingValues(insets = LocalWindowInsets.current.displayCutout, applyTop = false, applyBottom = false), LocalLayoutDirection.current)
        ) {
            itemsIndexed(items = viewModel.itemList, key = { _, item -> item.toString() }) { index, item ->
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .fillMaxWidth()
                        .animateItemPlacement(),
                    shape = RoundedCornerShape(10.dp),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = 0.38f)) {
                            Text(text = stringResource(id = item.type))
                        }
                        Spacer(Modifier.width(10.dp))
                        AnimatedContent(
                            targetState = item.value,
                            transitionSpec = { fadeIn() with fadeOut() }
                        ) {
                            Text(text = it)
                        }
                        Spacer(Modifier.width(10.dp))
                        FlowRow {
                            ButtonWithIcon(label = stringResource(id = R.string.add_item), icon = Icons.Default.Add) {
                                viewModel.addItem(index + 1)
                            }
                            ButtonWithIcon(label = stringResource(id = R.string.remove_item), icon = Icons.Default.Delete) {
                                viewModel.removeItem(index)
                            }
                            val context = LocalContext.current
                            ButtonWithIcon(label = stringResource(id = R.string.edit_item), icon = Icons.Default.Edit) {
                                viewModel.editing = index
                                val initValue = viewModel.editing?.let { viewModel.itemList[it].value } ?: ""
                                Toast.makeText(context, "${viewModel.editingValue}", Toast.LENGTH_LONG).show()
                                viewModel.editingValue = TextFieldValue(
                                    text = initValue,
                                    selection = TextRange(index = initValue.length)
                                )
                                Toast.makeText(context, "${viewModel.editingValue}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonWithIcon(label: String, icon: ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(5.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label)
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(label)
    }
}