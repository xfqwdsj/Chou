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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.xfqlittlefan.chou.ActivityViewModel
import xyz.xfqlittlefan.chou.R

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
        Spacer(modifier = Modifier.height(50.dp))
        AnimatedContent(
            targetState = viewModel.list.isNotEmpty(),
            transitionSpec = { fadeIn() with fadeOut() }
        ) { visible ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (visible) {
                    AnimatedContent(
                        targetState = viewModel.state,
                        modifier = Modifier.padding(horizontal = 30.dp),
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) {
                        Text(text = stringResource(id = if (it == 2) R.string.chose_item else R.string.current_item), style = MaterialTheme.typography.titleMedium)
                    }
                    AnimatedContent(
                        targetState = viewModel.current,
                        modifier = Modifier
                            .padding(30.dp)
                            .fillMaxWidth(),
                        transitionSpec = {
                            (fadeIn() + slideInVertically(initialOffsetY = { it }) with
                                    slideOutVertically(targetOffsetY = { -it }) + fadeOut()).using(SizeTransform(clip = false))
                        }
                    ) {
                        Text(
                            text = if (viewModel.list.isEmpty()) "" else viewModel.list[it].value,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    AnimatedVisibility(visible = viewModel.state == 0) {
                        Button(
                            onClick = { viewModel.start() }
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
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun Edit(viewModel: ActivityViewModel, state: LazyListState) {
    Scaffold(
        modifier = Modifier.nestedScroll(viewModel.scrollBehavior.nestedScrollConnection),
        topBar = {
            val background by TopAppBarDefaults.smallTopAppBarColors().containerColor(
                scrollFraction = viewModel.fraction
            )
            Column(modifier = Modifier.background(color = background)) {
                Spacer(modifier = Modifier.height(5.dp))
                AnimatedVisibility(visible = viewModel.isEditing != null) {
                    val requester = FocusRequester()
                    val initValue = viewModel.isEditing?.let { viewModel.list[it].value } ?: ""
                    var value by remember {
                        mutableStateOf(
                            TextFieldValue(
                                text = initValue,
                                selection = TextRange(index = initValue.length)
                            )
                        )
                    }

                    SideEffect {
                        requester.requestFocus()
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape = RoundedCornerShape(size = 10.dp))
                        ) {
                            TextField(
                                value = value,
                                onValueChange = { value = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(requester)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = { viewModel.isEditing = null }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(id = android.R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = {
                            viewModel.list[viewModel.isEditing!!].value = value.text
                            viewModel.isEditing = null
                        }) {
                            Icon(imageVector = Icons.Filled.Done, contentDescription = stringResource(id = android.R.string.ok))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                TextButton(
                    onClick = {
                        viewModel.add(0)
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            state = state
        ) {
            itemsIndexed(items = viewModel.list, key = { _, item -> item.toString() }) { index, item ->
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .fillMaxWidth()
                        .animateItemPlacement(),
                    shape = RoundedCornerShape(10.dp),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedContent(
                            targetState = item.value,
                            modifier = Modifier.weight(1f),
                            transitionSpec = { fadeIn() with fadeOut() }
                        ) {
                            Text(text = it)
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Row {
                            IconButton(
                                onClick = { viewModel.add(index + 1) }
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(id = R.string.add_item))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(
                                onClick = { viewModel.remove(index) }
                            ) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(id = R.string.remove_item))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(
                                onClick = { viewModel.isEditing = index }
                            ) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(id = R.string.edit_item))
                            }
                        }
                    }
                }
            }
        }
    }
}
