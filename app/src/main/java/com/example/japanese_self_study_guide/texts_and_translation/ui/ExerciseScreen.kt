package com.example.japanese_self_study_guide.texts_and_translation.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.texts_and_translation.ExerciseModel
import com.example.japanese_self_study_guide.texts_and_translation.view_model.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    textId: Int,
    exercises: List<ExerciseModel>,
    onBack: () -> Unit,
    onFinished: (correct: Int, total: Int) -> Unit,
    onFailedLowScore: () -> Unit
) {
    val vm: ExerciseViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("hints", android.content.Context.MODE_PRIVATE)
    }

    LaunchedEffect(Unit) {
        vm.load(exercises, prefs, textId)
    }

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished(state.correct, state.total)
    }

    LaunchedEffect(state.failedLowScore) {
        if (state.failedLowScore) onFailedLowScore()
    }

    val ex = state.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(state.counter, color = MaterialTheme.colorScheme.onPrimary)
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
        if (ex == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinearProgressIndicator(
                progress = { state.progress },
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
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            state.shuffledOptions.forEachIndexed { index, option ->
                val isSelected = state.resultText != null && index == state.shuffledCorrectIndex
                OutlinedButton(
                    onClick = { if (state.resultText == null) vm.checkAnswer(index) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = when {
                            state.resultText == null -> Color.Transparent
                            index == state.shuffledCorrectIndex -> Color(0xFFE8F5E9)
                            else -> Color.Transparent
                        }
                    )
                ) {
                    Text(option, fontSize = 16.sp)
                }
            }

            AnimatedVisibility(state.resultText != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
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

            AnimatedVisibility(state.hintText != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        state.hintText ?: "",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { vm.useHint(prefs, textId) },
                    enabled = !state.hintUsed && state.resultText == null,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(
                            if (state.hintUsed) R.string.exercise_hint_used
                            else R.string.exercise_hint
                        ),
                        fontSize = 14.sp
                    )
                }

                if (state.resultText != null) {
                    Button(
                        onClick = { vm.next(prefs, textId) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.exercise_next),
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}