package com.example.japanese_self_study_guide.kanji.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.kanji.KanjiModel
import com.example.japanese_self_study_guide.kanji.KanjiSortOption
import com.example.japanese_self_study_guide.kanji.KanjiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiScreen(
    dailyMode: Boolean = false,
    dailyIds: IntArray? = null,
    onBack: () -> Unit,
    onGoToExercises: (startId: Int, endId: Int, limit: Int) -> Unit,
    onGoToExerciseGroups: () -> Unit
) {
    val vm: KanjiViewModel = viewModel()
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.initialize(dailyMode, dailyIds)
    }

    var randomKanji by remember { mutableStateOf<KanjiModel?>(null) }
    var detailKanji by remember { mutableStateOf<KanjiModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (dailyMode) R.string.kanji_daily_title else R.string.kanji_title),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (!dailyMode) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.kanji_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { vm.onQueryChange("") }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.sortOption == KanjiSortOption.BY_ID,
                        onClick = { vm.onSortChange(KanjiSortOption.BY_ID) },
                        label = { Text(stringResource(R.string.kanji_sort_by_id)) }
                    )
                    FilterChip(
                        selected = state.sortOption == KanjiSortOption.BY_CATEGORY,
                        onClick = { vm.onSortChange(KanjiSortOption.BY_CATEGORY) },
                        label = { Text(stringResource(R.string.kanji_sort_by_category)) }
                    )
                }

                AnimatedVisibility(state.sortOption == KanjiSortOption.BY_CATEGORY) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedCategory == null,
                                onClick = { vm.onCategorySelect(null) },
                                label = { Text(stringResource(R.string.kanji_filter_all), fontSize = 13.sp) }
                            )
                        }
                        items(state.availableCategories) { cat ->
                            FilterChip(
                                selected = state.selectedCategory == cat,
                                onClick = { vm.onCategorySelect(cat) },
                                label = { Text(cat, fontSize = 13.sp) }
                            )
                        }
                    }
                }

                JlptFilterRow(selected = state.jlptFilter, onSelect = vm::onJlptFilter)
            }

            Text(
                text = if (dailyMode)
                    stringResource(R.string.kanji_count_daily, state.displayedKanji.size)
                else
                    stringResource(R.string.kanji_count_shown, state.displayedKanji.size, state.allKanji.size),
                modifier = Modifier.padding(start = 14.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(stringResource(R.string.audio_error, state.error!!),
                        color = MaterialTheme.colorScheme.error)
                }
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    if (state.sortOption == KanjiSortOption.BY_CATEGORY && state.selectedCategory == null) {
                        val grouped = state.displayedKanji.groupBy { it.category ?: "" }
                        grouped.forEach { (category, kanjis) ->
                            item(key = "header_$category") {
                                Text(
                                    text = category.ifBlank { stringResource(R.string.kanji_no_category) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(kanjis, key = { it.id }) { kanji ->
                                KanjiCard(
                                    kanji = kanji,
                                    isLearned = state.learnedIds.contains(kanji.id.toInt()),
                                    onClick = { detailKanji = kanji }
                                )
                            }
                        }
                    } else {
                        items(state.displayedKanji, key = { it.id }) { kanji ->
                            KanjiCard(
                                kanji = kanji,
                                isLearned = state.learnedIds.contains(kanji.id.toInt()),
                                onClick = { detailKanji = kanji }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (dailyMode) onGoToExercises(0, 0, 0)
                        else onGoToExerciseGroups()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        stringResource(if (dailyMode) R.string.kanji_daily_exercises_btn else R.string.kanji_exercises_btn),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (!dailyMode) {
                    SmallFloatingActionButton(
                        onClick = { randomKanji = vm.randomKanji() },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Outlined.Casino, stringResource(R.string.kanji_random_btn))
                    }
                }
            }
        }
    }

    randomKanji?.let { k -> KanjiDialog(kanji = k, onDismiss = { randomKanji = null }) }
    detailKanji?.let { k -> KanjiDialog(kanji = k, onDismiss = { detailKanji = null }) }
}

@Composable
private fun JlptFilterRow(selected: Int, onSelect: (Int) -> Unit) {
    val allLabel = stringResource(R.string.kanji_filter_all)
    val levels = listOf(0 to allLabel, 5 to "N5", 4 to "N4", 3 to "N3", 2 to "N2", 1 to "N1")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(levels) { (level, label) ->
            FilterChip(
                selected = selected == level,
                onClick = { onSelect(level) },
                label = { Text(label, fontSize = 13.sp) }
            )
        }
    }
}

@Composable
private fun KanjiCard(kanji: KanjiModel, isLearned: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = kanji.kanji ?: "",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(60.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(kanji.meaning ?: "", fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                if (!kanji.onYomi.isNullOrEmpty()) {
                    Text(
                        stringResource(R.string.kanji_onyomi_label, kanji.onYomi.joinToString(", ")),
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!kanji.kunYomi.isNullOrEmpty()) {
                    Text(
                        stringResource(R.string.kanji_kunyomi_label, kanji.kunYomi.joinToString(", ")),
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    AssistChip(onClick = {},
                        label = { Text(stringResource(R.string.kanji_jlpt_label, kanji.jlpt), fontSize = 12.sp) })
                    if (!kanji.category.isNullOrBlank()) {
                        AssistChip(onClick = {}, label = { Text(kanji.category!!, fontSize = 12.sp) })
                    }
                }
            }
            if (isLearned) {
                Icon(
                    painter = painterResource(id = R.drawable.sakura_learned),
                    contentDescription = stringResource(R.string.kanji_learned),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun KanjiDialog(kanji: KanjiModel, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(kanji.kanji ?: "", fontSize = 72.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(kanji.meaning ?: "", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                if (!kanji.onYomi.isNullOrEmpty())
                    Text(stringResource(R.string.kanji_dialog_onyomi, kanji.onYomi.joinToString(", ")),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!kanji.kunYomi.isNullOrEmpty())
                    Text(stringResource(R.string.kanji_dialog_kunyomi, kanji.kunYomi.joinToString(", ")),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {},
                        label = { Text(stringResource(R.string.kanji_dialog_jlpt, kanji.jlpt)) })
                    if (!kanji.category.isNullOrBlank())
                        AssistChip(onClick = {}, label = { Text(kanji.category!!) })
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.kanji_dialog_close))
                }
            }
        }
    }
}