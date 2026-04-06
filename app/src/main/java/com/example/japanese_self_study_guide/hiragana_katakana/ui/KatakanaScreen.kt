package com.example.japanese_self_study_guide.hiragana_katakana.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
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
                    val mainRows = ceil(main.size / 5.0).toInt()
                    val youonRows = ceil(youon.size / 3.0).toInt()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                stringResource(R.string.katakana_main_group),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        item {
                            FixedGrid(
                                items = main,
                                columns = 5,
                                cellDp = SymbolCellSize,
                                learnedIds = state.learnedIds,
                                rows = mainRows,
                                onClick = { dialogItem = it }
                            )
                        }
                        item {
                            Text(
                                stringResource(R.string.hiragana_youon_group),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        item {
                            FixedGrid(
                                items = youon,
                                columns = 3,
                                cellDp = SymbolCellSize,
                                learnedIds = state.learnedIds,
                                rows = youonRows,
                                onClick = { dialogItem = it }
                            )
                        }
                        item {
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = onGoToGroups,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    stringResource(R.string.hiragana_exercises_btn),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(Modifier.height(10.dp))
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
    val rows = ceil(symbols.size / 5.0).toInt().coerceAtLeast(1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.katakana_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FixedGrid(
                            items = symbols,
                            columns = 5,
                            cellDp = SymbolCellSize,
                            learnedIds = emptyList(),
                            rows = rows,
                            onClick = { dialogItem = it }
                        )
                    }
                    item {
                        Button(
                            onClick = onStartExercises,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                stringResource(R.string.hiragana_start_daily_btn),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    dialogItem?.let { SymbolZoomDialog(it) { dialogItem = null } }
}
