package com.example.japanese_self_study_guide.audio.view_model

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.audio.AudioRepository
import com.example.japanese_self_study_guide.audio.model.AudioExerciseModel
import com.example.japanese_self_study_guide.main_profile.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AnswerState { NONE, CORRECT, WRONG }

data class AudioExerciseUiState(
    val isLoading: Boolean = true,
    val exercises: List<AudioExerciseModel> = emptyList(),
    val currentIndex: Int = 0,
    val answerState: AnswerState = AnswerState.NONE,
    val selectedOption: String? = null,
    val correctOption: String? = null,
    val showHint: Boolean = false,
    val hintDisabled: Boolean = false,
    val correct: Int = 0,
    val total: Int = 0,
    val isFinished: Boolean = false,
    val failedLowScore: Boolean = false,
    val error: String? = null
)

class AudioExerciseViewModel(
    private val repository: AudioRepository = AudioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioExerciseUiState())
    val uiState: StateFlow<AudioExerciseUiState> = _uiState.asStateFlow()

    private var audioId: Int = -1
    private lateinit var prefs: SharedPreferences

    fun init(context: Context, audioId: Int) {
        this.audioId = audioId
        prefs = context.getSharedPreferences("hintPrefs", Context.MODE_PRIVATE)

        val hintKey = "hint_used_audio_$audioId"
        val hintDisabled = System.currentTimeMillis() - prefs.getLong(hintKey, 0) < 86_400_000L
        _uiState.value = _uiState.value.copy(hintDisabled = hintDisabled)

        repository.getExercises(audioId)
            .addOnSuccessListener { exercises ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exercises = exercises.shuffled()
                )
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
    }

    fun selectAnswer(selected: String) {
        val state = _uiState.value
        if (state.answerState != AnswerState.NONE) return

        val exercise = state.exercises.getOrNull(state.currentIndex) ?: return
        val correct = exercise.options[exercise.correctIndex]
        val isCorrect = selected == correct

        _uiState.value = state.copy(
            answerState = if (isCorrect) AnswerState.CORRECT else AnswerState.WRONG,
            selectedOption = selected,
            correctOption = correct,
            correct = if (isCorrect) state.correct + 1 else state.correct,
            total = state.total + 1
        )
    }

    fun useHint() {
        prefs.edit().putLong("hint_used_audio_$audioId", System.currentTimeMillis()).apply()
        _uiState.value = _uiState.value.copy(showHint = true, hintDisabled = true)
    }

    fun next() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1

        if (nextIndex < state.exercises.size) {
            _uiState.value = state.copy(
                currentIndex = nextIndex,
                answerState = AnswerState.NONE,
                selectedOption = null,
                correctOption = null,
                showHint = false
            )
        } else {
            finishExercise()
        }
    }

    private fun finishExercise() {
        val state = _uiState.value
        val percent = (state.correct * 100f) / state.total

        if (percent < 70f) {
            _uiState.value = state.copy(failedLowScore = true)
            return
        }

        repository.markAudioCompleted(audioId)
        MainActivity.removeDailyRecommendation("audio", audioId)
        _uiState.value = state.copy(isFinished = true)
    }
}