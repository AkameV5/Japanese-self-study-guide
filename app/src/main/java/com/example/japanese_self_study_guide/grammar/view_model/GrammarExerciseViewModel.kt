package com.example.japanese_self_study_guide.grammar.view_model

import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.grammar.GrammarExercise
import com.example.japanese_self_study_guide.grammar.GrammarRepository
import com.example.japanese_self_study_guide.main_profile.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CheckState { IDLE, CORRECT, WRONG }

data class GrammarExerciseUiState(
    val isLoading: Boolean = true,
    val exercises: List<GrammarExercise> = emptyList(),
    val currentIndex: Int = 0,
    val answer: String = "",
    val checkState: CheckState = CheckState.IDLE,
    val explanationText: String = "",
    val correct: Int = 0,
    val total: Int = 0,
    val isFinished: Boolean = false,
    val failedLowScore: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null
)

class GrammarExerciseViewModel(
    private val repository: GrammarRepository = GrammarRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarExerciseUiState())
    val uiState: StateFlow<GrammarExerciseUiState> = _uiState.asStateFlow()

    private var grammarId: Int = -1

    fun init(grammarId: Int) {
        if (this.grammarId == grammarId) return
        this.grammarId = grammarId
        load()
    }

    private fun load() {
        repository.getExercises(grammarId)
            .addOnSuccessListener { list ->
                if (list.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isEmpty = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        exercises = list.shuffled()
                    )
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
    }

    fun onAnswerChange(text: String) {
        _uiState.value = _uiState.value.copy(answer = text)
    }

    fun check() {
        val state = _uiState.value
        if (state.checkState != CheckState.IDLE) return
        val ex = state.exercises.getOrNull(state.currentIndex) ?: return

        val isCorrect = state.answer.trim().equals(ex.rightAnswer.trim(), ignoreCase = true)
        val explanationText = if (isCorrect) {
            "✅ Правильно!\n\n${ex.explanation}"
        } else {
            "❌ Неверно\nПравильный ответ: ${ex.rightAnswer}\n\n${ex.explanation}"
        }

        _uiState.value = state.copy(
            checkState = if (isCorrect) CheckState.CORRECT else CheckState.WRONG,
            explanationText = explanationText,
            correct = if (isCorrect) state.correct + 1 else state.correct,
            total = state.total + 1
        )
    }

    fun next() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1

        if (nextIndex < state.exercises.size) {
            _uiState.value = state.copy(
                currentIndex = nextIndex,
                answer = "",
                checkState = CheckState.IDLE,
                explanationText = ""
            )
        } else {
            finish()
        }
    }

    private fun finish() {
        val state = _uiState.value
        val percent = if (state.total > 0) (state.correct * 100f) / state.total else 0f

        if (percent < 70f) {
            _uiState.value = state.copy(failedLowScore = true)
            return
        }

        repository.markGrammarCompleted(grammarId)
        MainActivity.removeDailyRecommendation("grammar", grammarId)
        _uiState.value = state.copy(isFinished = true)
    }
}