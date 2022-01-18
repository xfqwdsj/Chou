package xyz.xfqlittlefan.chou.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.*
import xyz.xfqlittlefan.chou.ActivityViewModel
import xyz.xfqlittlefan.chou.R
import xyz.xfqlittlefan.chou.ui.plus

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Main(viewModel: ActivityViewModel, route: String, state: ScrollState, navigateTo: (String) -> Unit) {
    SideEffect {
        viewModel.globalScreen?.let { if (it != route) navigateTo(it) }
    }

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
                        Text(text = stringResource(id = if (it == 2) R.string.selected_item else R.string.current_item), style = MaterialTheme.typography.titleMedium)
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
                            onClick = viewModel.startSelecting
                        ) {
                            Text(text = stringResource(id = R.string.start))
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun Edit(viewModel: ActivityViewModel, route: String, state: LazyListState, navigateTo: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val behavior = remember { NestedScrollBehavior(coroutineScope) }

    SideEffect {
        viewModel.globalScreen?.let { if (it != route) navigateTo(it) }
    }

    Box {
        NestedScrollLayout(
            modifier = Modifier.nestedScroll(behavior.connection),
            topBar = {
                val topBarBackground by TopAppBarDefaults.smallTopAppBarColors().containerColor(
                    scrollFraction = viewModel.fraction
                )

                AnimatedVisibility(visible = !viewModel.isEditing) {
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .background(color = topBarBackground)
                            .padding(10.dp)
                            .systemBarsPadding(top = false, bottom = false)
                            .cutoutPadding(top = false, bottom = false)
                    ) {
                        TextButton(
                            onClick = { viewModel.addItem(0) },
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
            },
            nestedScrollBehavior = behavior
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(Modifier.height(20.dp))
                            CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = 0.38f)) {
                                AnimatedContent(
                                    targetState = "${index + 1}. ${stringResource(id = item.type)}",
                                    transitionSpec = { fadeIn() with fadeOut() }
                                ) {
                                    Text(text = it, modifier = Modifier.padding(horizontal = 20.dp))
                                }
                            }
                            Spacer(Modifier.height(5.dp))
                            AnimatedContent(
                                targetState = item.value,
                                transitionSpec = { fadeIn() with fadeOut() }
                            ) {
                                Text(text = it, modifier = Modifier.padding(horizontal = 20.dp))
                            }
                            Spacer(Modifier.height(10.dp))
                            FlowRow(modifier = Modifier.padding(horizontal = 10.dp)) {
                                ButtonWithIconAndLabel(label = stringResource(id = R.string.add_item), icon = Icons.Default.Add) {
                                    viewModel.addItem(index + 1)
                                }
                                ButtonWithIconAndLabel(label = stringResource(id = R.string.remove_item), icon = Icons.Default.Delete) {
                                    viewModel.removeItem(index)
                                }
                                ButtonWithIconAndLabel(label = stringResource(id = R.string.edit_item), icon = Icons.Default.Edit) {
                                    viewModel.editItem(index)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        Surface {
            AnimatedVisibility(
                visible = viewModel.isEditing,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val menuText = stringResource(viewModel.itemList[viewModel.editingItem].type)
                    var isShowingMenu by remember { mutableStateOf(false) }

                    ButtonWithLabelAndIcon(label = menuText, icon = Icons.Default.MoreVert, contentDescription = "$menuText/${stringResource(R.string.click_to_select)}") {
                        isShowingMenu = true
                    }
                    Box {
                        ChouDropdownMenu(expanded = isShowingMenu, onDismissRequest = { isShowingMenu = false }) {
                            viewModel.itemTypeList.forEach { type ->
                                DropdownMenuItem(
                                    onClick = {
                                        isShowingMenu = false
                                        viewModel.itemList[viewModel.editingItem].type = type
                                    }
                                ) {
                                    Text(stringResource(type))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    AnimatedContent(
                        targetState = viewModel.itemList[viewModel.editingItem].type,
                        modifier = Modifier.weight(1f, fill = false),
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) { type ->
                        when (type) {
                            R.string.item_type_0 -> {
                                val requester = FocusRequester()

                                SideEffect {
                                    requester.requestFocus()
                                }

                                Box(
                                    modifier = Modifier.clip(shape = RoundedCornerShape(size = 10.dp))
                                ) {
                                    TextField(
                                        value = viewModel.editingValue,
                                        onValueChange = {
                                            viewModel.editingValue = it
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(requester)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(start = false, end = false)
                    ) {
                        ButtonWithIconAndLabel(
                            label = stringResource(id = android.R.string.cancel),
                            icon = Icons.Default.Close,
                            onClick = viewModel.cancelEditing
                        )
                        ButtonWithIconAndLabel(
                            label = stringResource(id = android.R.string.ok),
                            icon = Icons.Default.Done,
                            onClick = viewModel.confirmEditing
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ButtonWithIconAndLabel(label: String, icon: ImageVector, contentDescription: String? = null, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(5.dp)
    ) {
        AnimatedContent(
            targetState = icon,
            transitionSpec = { fadeIn() with fadeOut() }
        ) {
            Icon(imageVector = it, contentDescription = contentDescription ?: label)
        }
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        AnimatedContent(
            targetState = label,
            transitionSpec = { fadeIn() with fadeOut() }
        ) {
            Text(it)
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ButtonWithLabelAndIcon(label: String, icon: ImageVector, contentDescription: String? = null, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(5.dp)
    ) {
        AnimatedContent(
            targetState = label,
            transitionSpec = { fadeIn() with fadeOut() }
        ) {
            Text(it)
        }
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        AnimatedContent(
            targetState = icon,
            transitionSpec = { fadeIn() with fadeOut() }
        ) {
            Icon(imageVector = it, contentDescription = contentDescription ?: label)
        }
    }
}