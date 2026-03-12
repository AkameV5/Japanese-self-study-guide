package com.example.japanese_self_study_guide.hiragana_katakana.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaItem
import com.example.japanese_self_study_guide.hiragana_katakana.view_model.KatakanaViewModel
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KatakanaScreen(
    onBack: () -> Unit,
    onGoToGroups: () -> Unit,
    viewModel: KatakanaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var dialogItem by remember { mutableStateOf<HiraganaItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.katakana_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                state.error != null -> Text(
                    text = stringResource(R.string.audio_error, state.error!!),
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                else -> {
                    val (main, youon) = remember(state.symbols) {
                        insertGaps(state.symbols.filter { it.id < 72 }) to
                                state.symbols.filter { it.id >= 72 }
                    }
                    val cellDp = 64.dp
                    val mainRows  = ceil(main.size / 5.0).toInt()
                    val youonRows = ceil(youon.size / 3.0).toInt()
                    val gap = 4.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(stringResource(R.string.katakana_main_group),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier.fillMaxWidth()
                                    .height(cellDp * mainRows + gap * (mainRows - 1)),
                                userScrollEnabled = false,
                                horizontalArrangement = Arrangement.spacedBy(gap),
                                verticalArrangement = Arrangement.spacedBy(gap)
                            ) {
                                items(main, key = { it.id }) { item ->
                                    SymbolCell(item, item.id in state.learnedIds, cellDp) { dialogItem = item }
                                }
                            }
                        }
                        item {
                            Text(stringResource(R.string.hiragana_youon_group),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        }
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxWidth()
                                    .height(cellDp * youonRows + gap * (youonRows - 1)),
                                userScrollEnabled = false,
                                horizontalArrangement = Arrangement.spacedBy(gap),
                                verticalArrangement = Arrangement.spacedBy(gap)
                            ) {
                                items(youon, key = { it.id }) { item ->
                                    SymbolCell(item, item.id in state.learnedIds, cellDp) { dialogItem = item }
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = onGoToGroups,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(R.string.hiragana_exercises_btn),
                                    style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    dialogItem?.let { SymbolZoomDialog(it) { dialogItem = null } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KatakanaDailyScreen(
    symbols: List<HiraganaItem>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onStartExercises: () -> Unit
) {
    var dialogItem by remember { mutableStateOf<HiraganaItem?>(null) }
    val cellDp = 64.dp
    val rows = ceil(symbols.size / 5.0).toInt().coerceAtLeast(1)
    val gap = 4.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.katakana_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier.fillMaxWidth()
                                .height(cellDp * rows + gap * (rows - 1)),
                            userScrollEnabled = false,
                            horizontalArrangement = Arrangement.spacedBy(gap),
                            verticalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            items(symbols, key = { it.id }) { item ->
                                SymbolCell(item, false, cellDp) { dialogItem = item }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = onStartExercises,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.hiragana_start_daily_btn),
                                style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    dialogItem?.let { SymbolZoomDialog(it) { dialogItem = null } }
}