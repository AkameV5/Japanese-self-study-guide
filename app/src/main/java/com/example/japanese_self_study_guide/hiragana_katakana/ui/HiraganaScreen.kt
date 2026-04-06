package com.example.japanese_self_study_guide.hiragana_katakana.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaItem
import com.example.japanese_self_study_guide.hiragana_katakana.view_model.HiraganaViewModel
import kotlin.math.ceil

private val SymbolGridGap = 8.dp
val SymbolCellSize = 76.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiraganaScreen(
    onBack: () -> Unit,
    onGoToGroups: () -> Unit,
    viewModel: HiraganaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var dialogItem by remember { mutableStateOf<HiraganaItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hiragana_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.audio_back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
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
                                text = stringResource(R.string.hiragana_main_group),
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
                                text = stringResource(R.string.hiragana_youon_group),
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
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
fun HiraganaDailyScreen(
    dailyIds: List<Int>,
    symbols: List<HiraganaItem>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onStartExercises: () -> Unit
) {
    var dialogItem by remember { mutableStateOf<HiraganaItem?>(null) }
    val cellDp = SymbolCellSize
    val rows = ceil(symbols.size / 5.0).toInt().coerceAtLeast(1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hiragana_title), color = MaterialTheme.colorScheme.onPrimary) },
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
                            cellDp = cellDp,
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

@Composable
fun FixedGrid(
    items: List<HiraganaItem>,
    columns: Int,
    cellDp: Dp,
    learnedIds: List<Int>,
    rows: Int,
    onClick: (HiraganaItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth()
            .height(cellDp * rows + SymbolGridGap * (rows - 1)),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(SymbolGridGap),
        verticalArrangement = Arrangement.spacedBy(SymbolGridGap)
    ) {
        items(items, key = { it.id }) { item ->
            SymbolCell(
                item = item,
                isLearned = item.id in learnedIds,
                cellSize = cellDp,
                onClick = { onClick(item) }
            )
        }
    }
}

@Composable
fun SymbolCell(item: HiraganaItem, isLearned: Boolean, cellSize: Dp, onClick: () -> Unit) {
    if (item.id < 0) {
        Box(Modifier.size(cellSize))
        return
    }

    val accent = if (isLearned) {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFFE57D7D)
    }
    val gradient = if (isLearned) {
        listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            MaterialTheme.colorScheme.surface
        )
    } else {
        listOf(
            accent.copy(alpha = 0.20f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier.size(cellSize),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradient))
                .padding(horizontal = 4.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.symbol ?: "",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.romaji ?: "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            if (isLearned) {
                Icon(
                    painter = painterResource(R.drawable.sakura_learned),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
fun SymbolZoomDialog(item: HiraganaItem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.symbol ?: "",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = item.romaji ?: "",
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun insertGaps(list: List<HiraganaItem>): List<HiraganaItem> {
    val result = mutableListOf<HiraganaItem>()
    var gapCounter = -1
    for (item in list) {
        result.add(item)
        if (item.id == 61) result.add(HiraganaItem("", "", null, gapCounter--))
        if (item.id == 62) result.add(HiraganaItem("", "", null, gapCounter--))
        if (item.id == 69) repeat(3) { result.add(HiraganaItem("", "", null, gapCounter--)) }
    }
    return result
}
