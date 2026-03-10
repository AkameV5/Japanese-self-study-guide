package com.example.japanese_self_study_guide.grammar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.grammar.view_model.CheckState
import com.example.japanese_self_study_guide.grammar.view_model.GrammarExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarExerciseScreen(
    grammarId: Int,
    onBack: () -> Unit,
    onFinished: (correct: Int, total: Int) -> Unit,
    viewModel: GrammarExerciseViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(grammarId) { viewModel.init(grammarId) }
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onFinished(state.correct, state.total)
    }
    LaunchedEffect(state.failedLowScore) {
        if (state.failedLowScore) onBack()
    }
    LaunchedEffect(state.isEmpty) {
        if (state.isEmpty) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exercise_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.audio_back),
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
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
                    val ex = state.exercises[state.currentIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
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
                            text = ex.task,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        OutlinedTextField(
                            value = state.answer,
                            onValueChange = { viewModel.onAnswerChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.grammar_answer_hint)) },
                            enabled = state.checkState == CheckState.IDLE,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboard?.hide()
                                viewModel.check()
                            })
                        )

                        // Check button
                        if (state.checkState == CheckState.IDLE) {
                            Button(
                                onClick = {
                                    keyboard?.hide()
                                    viewModel.check()
                                },
                                enabled = state.answer.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(stringResource(R.string.grammar_check_btn),
                                    style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        AnimatedVisibility(
                            visible = state.checkState != CheckState.IDLE,
                            enter = fadeIn()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                val isCorrect = state.checkState == CheckState.CORRECT
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCorrect)
                                            Color(0xFF388E3C).copy(alpha = 0.12f)
                                        else
                                            Color(0xFFD32F2F).copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = state.explanationText,
                                        modifier = Modifier.padding(16.dp),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Button(
                                    onClick = { viewModel.next() },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = if (state.currentIndex == state.exercises.size - 1)
                                            stringResource(R.string.exercise_finish)
                                        else
                                            stringResource(R.string.grammar_next_btn),
                                        style = MaterialTheme.typography.titleMedium
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