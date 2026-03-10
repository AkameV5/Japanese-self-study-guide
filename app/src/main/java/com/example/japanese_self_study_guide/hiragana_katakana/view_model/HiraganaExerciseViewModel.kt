package com.example.japanese_self_study_guide.hiragana_katakana.view_model

import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaExerciseModel
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaRepository
import com.example.japanese_self_study_guide.main_profile.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HiraganaCheckState { IDLE, CORRECT, WRONG }

data class HiraganaExerciseUiState(
    val isLoading: Boolean = true,
    val exercises: List<HiraganaExerciseModel> = emptyList(),
    val currentIndex: Int = 0,
    val answer: String = "",
    val checkState: HiraganaCheckState = HiraganaCheckState.IDLE,
    val explanationText: String = "",
    val totalCorrect: Int = 0,
    val totalAnswered: Int = 0,
    val isFinished: Boolean = false,
    val learnedNow: Int = 0,
    val totalSymbols: Int = 0,
    val isEmpty: Boolean = false,
    val error: String? = null
)

class HiraganaExerciseViewModel(
    private val repository: HiraganaRepository = HiraganaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HiraganaExerciseUiState())
    val uiState: StateFlow<HiraganaExerciseUiState> = _uiState.asStateFlow()

    private val totalPerSymbol   = mutableMapOf<Int, Int>()
    private val correctPerSymbol = mutableMapOf<Int, Int>()

    private var initialized = false
    private var dailyIds: List<Int> = emptyList()

    fun init(groupIds: List<Int>, dailyMode: Boolean, limit: Int = 0, allRandom: Boolean = false) {
        if (initialized) return
        initialized = true
        this.dailyIds = if (dailyMode) groupIds else emptyList()
        load(groupIds, dailyMode, limit, allRandom)
    }

    private fun load(groupIds: List<Int>, dailyMode: Boolean, limit: Int, allRandom: Boolean) {
        val task = if (allRandom) repository.allExercises else repository.getExercises(groupIds)

        task.addOnSuccessListener { list ->
            var result = list.shuffled()
            if (allRandom && result.size > 50) result = result.take(50)
            if (limit > 0 && result.size > limit) result = result.take(limit)

            if (result.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, isEmpty = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, exercises = result)
            }
        }.addOnFailureListener { e ->
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    fun onAnswerChange(text: String) {
        _uiState.value = _uiState.value.copy(answer = text)
    }

    fun check() {
        val state = _uiState.value
        if (state.checkState != HiraganaCheckState.IDLE) return
        val ex = state.exercises.getOrNull(state.currentIndex) ?: return

        val user    = state.answer.trim().lowercase()
        val correct = ex.correctAnswer.trim().lowercase()
        val isOk    = user == correct

        val sid = ex.hiraganaId
        totalPerSymbol[sid]   = (totalPerSymbol[sid] ?: 0) + 1
        if (isOk) correctPerSymbol[sid] = (correctPerSymbol[sid] ?: 0) + 1

        val explanation = if (isOk)
            "✅ Правильно!\n\n${ex.explanation}"
        else
            "❌ Неверно\nПравильный ответ: ${ex.correctAnswer}\n\n${ex.explanation}"

        _uiState.value = state.copy(
            checkState      = if (isOk) HiraganaCheckState.CORRECT else HiraganaCheckState.WRONG,
            explanationText = explanation,
            totalCorrect    = if (isOk) state.totalCorrect + 1 else state.totalCorrect,
            totalAnswered   = state.totalAnswered + 1
        )
    }

    fun next() {
        val state = _uiState.value
        val next  = state.currentIndex + 1
        if (next < state.exercises.size) {
            _uiState.value = state.copy(
                currentIndex    = next,
                answer          = "",
                checkState      = HiraganaCheckState.IDLE,
                explanationText = ""
            )
        } else {
            finish()
        }
    }

    private fun finish() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Progress").document(uid).get()
            .addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                val alreadyLearned = (doc.get("hiraganaLearned") as? List<Long>) ?: emptyList()

                repository.markLearnedIfPassed(
                    totalPerSymbol, correctPerSymbol, alreadyLearned
                ) { learnedNow, totalSymbols ->

                    if (learnedNow == totalSymbols) {
                        for (id in totalPerSymbol.keys) {
                            MainActivity.removeDailyRecommendation("hiragana", id)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isFinished   = true,
                        learnedNow   = learnedNow,
                        totalSymbols = totalSymbols
                    )
                }
            }
    }
}