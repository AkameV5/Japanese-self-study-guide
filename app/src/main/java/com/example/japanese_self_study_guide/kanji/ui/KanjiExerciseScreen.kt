package com.example.japanese_self_study_guide.kanji.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.kanji.view_model.KanjiExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiExerciseScreen(
    startId: Int,
    endId: Int,
    limit: Int,
    dailyMode: Boolean = false,
    onBack: () -> Unit,
    onFinished: (correct: Int, total: Int, learned: Int, totalKanji: Int) -> Unit
) {
    val vm: KanjiExerciseViewModel = viewModel()
    val state by vm.uiState.collectAsState()

    LaunchedEffect(startId, endId) {
        vm.load(startId, endId, limit, dailyMode)
    }

    LaunchedEffect(state.finished) {
        if (state.finished) {
            onFinished(state.totalCorrect, state.totalAnswered, state.learnedCount, state.totalKanjiCount)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.kanji_exercise_progress, state.index + 1, state.exercises.size),
                        color = MaterialTheme.colorScheme.onPrimary
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
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            state.exercises.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(stringResource(R.string.kanji_exercise_title))
            }
            else -> {
                val ex = state.exercises.getOrNull(state.index) ?: return@Scaffold

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (state.index + 1f) / state.exercises.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            ex.question ?: "",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!ex.options.isNullOrEmpty()) {
                        ex.options.forEach { opt ->
                            OutlinedButton(
                                onClick = { vm.selectOption(opt) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (state.selectedOption == opt)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                            ) {
                                Text(opt, fontSize = 16.sp)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = state.answer,
                            onValueChange = vm::onAnswerChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.kanji_exercise_answer_hint)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    AnimatedVisibility(state.resultText != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.isCorrect)
                                    Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            )
                        ) {
                            Text(
                                state.resultText ?: "",
                                modifier = Modifier.padding(14.dp),
                                fontSize = 15.sp,
                                color = if (state.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    if (state.resultText == null) {
                        Button(
                            onClick = { vm.check() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.kanji_exercise_check),
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = { vm.next() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.kanji_exercise_next),
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}