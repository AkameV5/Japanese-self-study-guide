package com.example.japanese_self_study_guide.audio.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.audio.view_model.AnswerState
import com.example.japanese_self_study_guide.audio.view_model.AudioExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioExerciseScreen(
    audioId: Int,
    onBack: () -> Unit,
    onFinished: (correct: Int, total: Int) -> Unit,
    viewModel: AudioExerciseViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(audioId) { viewModel.init(context, audioId) }
    LaunchedEffect(state.isFinished) { if (state.isFinished) onFinished(state.correct, state.total) }
    LaunchedEffect(state.failedLowScore) { if (state.failedLowScore) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exercise_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.audio_back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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

                state.exercises.isNotEmpty() -> {
                    val exercise = state.exercises[state.currentIndex]
                    val shuffledOptions = remember(state.currentIndex) { exercise.options.shuffled() }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = (state.currentIndex + 1).toFloat() / state.exercises.size,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${state.currentIndex + 1} / ${state.exercises.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = exercise.question,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        shuffledOptions.forEach { option ->
                            AnswerButton(
                                text = option,
                                state = state.answerState,
                                isSelected = option == state.selectedOption,
                                isCorrect = option == state.correctOption,
                                onClick = { viewModel.selectAnswer(option) }
                            )
                        }

                        if (state.showHint) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = "💡 ${exercise.hint}",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        if (state.answerState != AnswerState.NONE) {
                            Text(
                                text = if (state.answerState == AnswerState.CORRECT)
                                    stringResource(R.string.exercise_correct)
                                else
                                    stringResource(R.string.exercise_wrong),
                                color = if (state.answerState == AnswerState.CORRECT)
                                    Color(0xFF388E3C) else Color(0xFFD32F2F),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.useHint() },
                                enabled = !state.hintDisabled && state.answerState == AnswerState.NONE,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (state.hintDisabled)
                                        stringResource(R.string.exercise_hint_used)
                                    else
                                        stringResource(R.string.exercise_hint),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (state.answerState != AnswerState.NONE) {
                                Button(
                                    onClick = { viewModel.next() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        if (state.currentIndex == state.exercises.size - 1)
                                            stringResource(R.string.exercise_finish)
                                        else
                                            stringResource(R.string.exercise_next)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    state: AnswerState,
    isSelected: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val answered = state != AnswerState.NONE

    val borderColor by animateColorAsState(
        targetValue = when {
            answered && isCorrect -> Color(0xFF388E3C)
            answered && isSelected && !isCorrect -> Color(0xFFD32F2F)
            else -> MaterialTheme.colorScheme.outline
        },
        label = "borderColor"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = !answered,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}