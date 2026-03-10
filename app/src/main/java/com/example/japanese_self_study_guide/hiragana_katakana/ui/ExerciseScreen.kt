package com.example.japanese_self_study_guide.hiragana_katakana.ui

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
import com.example.japanese_self_study_guide.hiragana_katakana.view_model.*

@Composable
fun HiraganaExerciseScreen(
    groupIds: List<Int>,
    dailyMode: Boolean = false,
    dailyLimit: Int = 0,
    allRandom: Boolean = false,
    onBack: () -> Unit,
    onFinished: (correct: Int, total: Int, learnedNow: Int, totalSymbols: Int) -> Unit,
    viewModel: HiraganaExerciseViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.init(groupIds, dailyMode, dailyLimit, allRandom) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onFinished(state.totalCorrect, state.totalAnswered, state.learnedNow, state.totalSymbols)
    }
    LaunchedEffect(state.isEmpty) { if (state.isEmpty) onBack() }

    ExerciseScreenContent(
        isLoading       = state.isLoading,
        error           = state.error,
        exercises       = state.exercises.map { ExerciseItem(it.question, it.correctAnswer, it.options, it.explanation) },
        currentIndex    = state.currentIndex,
        answer          = state.answer,
        checkState      = when (state.checkState) {
            HiraganaCheckState.IDLE    -> CheckStateUi.IDLE
            HiraganaCheckState.CORRECT -> CheckStateUi.CORRECT
            HiraganaCheckState.WRONG   -> CheckStateUi.WRONG
        },
        explanationText = state.explanationText,
        onAnswerChange  = viewModel::onAnswerChange,
        onCheck         = viewModel::check,
        onNext          = viewModel::next,
        onBack          = onBack
    )
}

@Composable
fun KatakanaExerciseScreen(
    groupIds: List<Int>,
    dailyMode: Boolean = false,
    dailyLimit: Int = 0,
    allRandom: Boolean = false,
    onBack: () -> Unit,
    onFinished: (correct: Int, total: Int, learnedNow: Int, totalSymbols: Int) -> Unit,
    viewModel: KatakanaExerciseViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.init(groupIds, dailyMode, dailyLimit, allRandom) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onFinished(state.totalCorrect, state.totalAnswered, state.learnedNow, state.totalSymbols)
    }
    LaunchedEffect(state.isEmpty) { if (state.isEmpty) onBack() }

    ExerciseScreenContent(
        isLoading       = state.isLoading,
        error           = state.error,
        exercises       = state.exercises.map { ExerciseItem(it.question, it.correctAnswer, it.options, it.explanation) },
        currentIndex    = state.currentIndex,
        answer          = state.answer,
        checkState      = when (state.checkState) {
            KatakanaCheckState.IDLE    -> CheckStateUi.IDLE
            KatakanaCheckState.CORRECT -> CheckStateUi.CORRECT
            KatakanaCheckState.WRONG   -> CheckStateUi.WRONG
        },
        explanationText = state.explanationText,
        onAnswerChange  = viewModel::onAnswerChange,
        onCheck         = viewModel::check,
        onNext          = viewModel::next,
        onBack          = onBack
    )
}

data class ExerciseItem(
    val question: String,
    val correctAnswer: String,
    val options: List<String>?,
    val explanation: String?
)

enum class CheckStateUi { IDLE, CORRECT, WRONG }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseScreenContent(
    isLoading: Boolean,
    error: String?,
    exercises: List<ExerciseItem>,
    currentIndex: Int,
    answer: String,
    checkState: CheckStateUi,
    explanationText: String,
    onAnswerChange: (String) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exercise_title), color = MaterialTheme.colorScheme.onPrimary) },
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
                isLoading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                error != null -> Text(
                    text = stringResource(R.string.audio_error, error),
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                exercises.isNotEmpty() -> {
                    val ex = exercises[currentIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = (currentIndex + 1).toFloat() / exercises.size,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${currentIndex + 1} / ${exercises.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ex.question,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (!ex.options.isNullOrEmpty() && checkState == CheckStateUi.IDLE) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ex.options.forEach { opt ->
                                    OutlinedButton(
                                        onClick = { onAnswerChange(opt) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(opt, fontSize = 18.sp)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = answer,
                            onValueChange = { v ->
                                // блокируем кириллицу
                                if (v.none { it in 'А'..'я' || it == 'ё' || it == 'Ё' })
                                    onAnswerChange(v)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.grammar_answer_hint)) },
                            enabled = checkState == CheckStateUi.IDLE,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboard?.hide(); onCheck()
                            })
                        )

                        if (checkState == CheckStateUi.IDLE) {
                            Button(
                                onClick = { keyboard?.hide(); onCheck() },
                                enabled = answer.isNotBlank(),
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
                            visible = checkState != CheckStateUi.IDLE,
                            enter = fadeIn()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                val isCorrect = checkState == CheckStateUi.CORRECT
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
                                        text = explanationText,
                                        modifier = Modifier.padding(16.dp),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Button(
                                    onClick = onNext,
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = if (currentIndex == exercises.size - 1)
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