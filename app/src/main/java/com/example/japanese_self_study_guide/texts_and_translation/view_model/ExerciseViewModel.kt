package com.example.japanese_self_study_guide.texts_and_translation.view_model

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.main_profile.MainActivity
import com.example.japanese_self_study_guide.main_profile.ProgressManager
import com.example.japanese_self_study_guide.texts_and_translation.ExerciseModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

data class ExerciseUiState(
    val exercises: List<ExerciseModel> = emptyList(),
    val currentIndex: Int = 0,
    val shuffledOptions: List<String> = emptyList(),
    val shuffledCorrectIndex: Int = -1,
    val resultText: String? = null,
    val isCorrect: Boolean = false,
    val hintText: String? = null,
    val hintUsed: Boolean = false,
    val total: Int = 0,
    val correct: Int = 0,
    val finished: Boolean = false,
    val failedLowScore: Boolean = false
) {
    val current get() = exercises.getOrNull(currentIndex)
    val progress get() = if (exercises.isEmpty()) 0f else (currentIndex + 1f) / exercises.size
    val counter get() = "${currentIndex + 1} / ${exercises.size}"
}

class ExerciseViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    fun load(exercises: List<ExerciseModel>, prefs: SharedPreferences, textId: Int) {
        if (_uiState.value.exercises.isNotEmpty()) return
        _uiState.value = _uiState.value.copy(exercises = exercises)
        setupQuestion(prefs, textId)
    }

    private fun setupQuestion(prefs: SharedPreferences, textId: Int) {
        val ex = _uiState.value.current ?: return
        val shuffled = ex.options.toMutableList()
        Collections.shuffle(shuffled)
        val correctIdx = shuffled.indexOf(ex.options[ex.correctIndex])
        val hintUsed = isHintUsed(prefs, textId)
        _uiState.value = _uiState.value.copy(
            shuffledOptions = shuffled,
            shuffledCorrectIndex = correctIdx,
            resultText = null,
            isCorrect = false,
            hintText = null,
            hintUsed = hintUsed
        )
    }

    fun checkAnswer(chosenIndex: Int) {
        val s = _uiState.value
        val correct = chosenIndex == s.shuffledCorrectIndex
        val correctAnswer = s.shuffledOptions.getOrNull(s.shuffledCorrectIndex) ?: ""
        val resultText = if (correct)
            "Правильно!\n\nПравильный ответ: $correctAnswer"
        else
            "Неправильно!\n\nПравильный ответ: $correctAnswer"

        _uiState.value = s.copy(
            resultText = resultText,
            isCorrect = correct,
            total = s.total + 1,
            correct = s.correct + if (correct) 1 else 0
        )
    }

    fun useHint(prefs: SharedPreferences, textId: Int) {
        val ex = _uiState.value.current ?: return
        saveHintUsed(prefs, textId)
        _uiState.value = _uiState.value.copy(
            hintText = "Подсказка:\n\n${ex.hint}",
            hintUsed = true
        )
    }

    fun next(prefs: SharedPreferences, textId: Int) {
        val s = _uiState.value
        val nextIndex = s.currentIndex + 1
        if (nextIndex < s.exercises.size) {
            _uiState.value = s.copy(currentIndex = nextIndex)
            setupQuestion(prefs, textId)
        } else {
            finishExercises(textId)
        }
    }

    private fun finishExercises(textId: Int) {
        val s = _uiState.value
        val percent = if (s.total > 0) s.correct * 100f / s.total else 0f
        if (percent < 70f) {
            _uiState.value = s.copy(failedLowScore = true)
            return
        }
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            ProgressManager.incrementProgress(uid, "textsDone", 1)
            db.collection("Progress").document(uid)
                .update("textsLearned", FieldValue.arrayUnion(textId))
            MainActivity.removeDailyRecommendation("text", textId)
        }
        _uiState.value = s.copy(finished = true)
    }

    private fun isHintUsed(prefs: SharedPreferences, textId: Int): Boolean {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        return today == prefs.getString("HINT_USED_$textId", "")
    }

    private fun saveHintUsed(prefs: SharedPreferences, textId: Int) {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        prefs.edit().putString("HINT_USED_$textId", today).apply()
    }
}