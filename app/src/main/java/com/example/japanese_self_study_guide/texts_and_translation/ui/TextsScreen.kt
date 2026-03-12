package com.example.japanese_self_study_guide.texts_and_translation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.texts_and_translation.TextModel
import com.example.japanese_self_study_guide.texts_and_translation.view_model.TextsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextsScreen(
    onBack: () -> Unit,
    onTextClick: (textId: Int) -> Unit
) {
    val vm: TextsViewModel = viewModel()
    val state by vm.uiState.collectAsState()

    val jlptLevels = listOf("Все уровни", "N5", "N4", "N3", "N2", "N1")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.texts_title),
                        color = MaterialTheme.colorScheme.onPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null,
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.texts_search_hint)) },
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

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(jlptLevels) { level ->
                    FilterChip(
                        selected = state.jlptFilter == level,
                        onClick = { vm.onJlptFilter(level) },
                        label = { Text(level, fontSize = 13.sp) }
                    )
                }
            }

            Text(
                text = stringResource(R.string.texts_count, state.displayedTexts.size),
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
                state.displayedTexts.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(stringResource(R.string.dict_no_results),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.displayedTexts, key = { it.id }) { text ->
                        TextCard(
                            text = text,
                            isLearned = state.learnedIds.contains(text.id),
                            onClick = { onTextClick(text.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextCard(text: TextModel, isLearned: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text.title ?: "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.texts_level, text.difficultyLevel ?: "?"), fontSize = 13.sp) }
                )
            }
            if (isLearned) {
                Icon(
                    painter = painterResource(R.drawable.sakura_learned),
                    contentDescription = stringResource(R.string.kanji_learned),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}