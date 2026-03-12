package com.example.japanese_self_study_guide.texts_and_translation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.texts_and_translation.view_model.TextDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDetailScreen(
    textId: Int,
    vm: TextDetailViewModel = viewModel(),
    onBack: () -> Unit,
    onGoToExercises: (textId: Int) -> Unit
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(textId) {
        vm.load(textId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.currentTitle.ifBlank { stringResource(R.string.texts_title) },
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp
                    )
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
        when {
            state.error != null ->
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            state.isLoading || state.japaneseParagraphs.isEmpty() ->
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasTranslation = state.translationParagraphs.isNotEmpty()

                    FilterChip(
                        selected = !state.showTranslation,
                        onClick = { vm.toggleLanguage(false) },
                        label = { Text("日本語") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = state.showTranslation,
                        onClick = { if (hasTranslation) vm.toggleLanguage(true) },
                        enabled = hasTranslation,
                        label = { Text(stringResource(R.string.texts_translation)) }
                    )
                }

                if (state.currentParagraphs.isNotEmpty()) Text(
                    stringResource(R.string.texts_paragraph,
                        state.currentIndex + 1, state.currentParagraphs.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = state.currentText,
                            fontSize = 17.sp,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { vm.goPrev() },
                        enabled = state.canGoPrev,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.texts_prev))
                    }
                    OutlinedButton(
                        onClick = { vm.goNext() },
                        enabled = state.canGoNext,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.texts_next))
                    }
                }

                if (state.isLastParagraph && state.exercises.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onGoToExercises(textId) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.texts_go_to_exercises),
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}