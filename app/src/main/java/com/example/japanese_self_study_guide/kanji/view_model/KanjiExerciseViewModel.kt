package com.example.japanese_self_study_guide.kanji.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.kanji.KanjiExerciseModel
import com.example.japanese_self_study_guide.main_profile.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class KanjiExerciseUiState(
    val isLoading: Boolean = true,
    val exercises: List<KanjiExerciseModel> = emptyList(),
    val index: Int = 0,
    val answer: String = "",
    val selectedOption: String? = null,
    val resultText: String? = null,
    val isCorrect: Boolean = false,
    val totalCorrect: Int = 0,
    val totalAnswered: Int = 0,
    val finished: Boolean = false,
    val learnedCount: Int = 0,
    val totalKanjiCount: Int = 0
)

class KanjiExerciseViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(KanjiExerciseUiState())
    val uiState: StateFlow<KanjiExerciseUiState> = _uiState.asStateFlow()

    private val totalPerKanji = mutableMapOf<Int, Int>()
    private val correctPerKanji = mutableMapOf<Int, Int>()

    fun load(startId: Int, endId: Int, limit: Int, dailyMode: Boolean) {
        if (!_uiState.value.isLoading && _uiState.value.exercises.isNotEmpty()) return

        val uid = FirebaseAuth.getInstance().uid
        if (dailyMode && uid != null) {
            db.collection("Progress").document(uid).get()
                .addOnSuccessListener { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val learned = (doc.get("kanjiLearned") as? List<Long>) ?: emptyList()
                    fetchExercises(startId, endId, limit, learned)
                }
        } else {
            fetchExercises(startId, endId, limit, emptyList())
        }
    }

    private fun fetchExercises(startId: Int, endId: Int, limit: Int, learnedKanji: List<Long>) {
        db.collection("KanjiExercises")
            .whereGreaterThanOrEqualTo("id_kanji", startId)
            .whereLessThanOrEqualTo("id_kanji", endId)
            .get()
            .addOnSuccessListener { query ->
                var list = query.documents
                    .mapNotNull { it.toObject(KanjiExerciseModel::class.java) }
                    .filter { !learnedKanji.contains(it.id_kanji.toLong()) }
                    .shuffled()
                if (list.size > limit) list = list.subList(0, limit)
                Log.d("KanjiExVM", "Loaded ${list.size} exercises")
                _uiState.value = _uiState.value.copy(isLoading = false, exercises = list)
            }
            .addOnFailureListener { e ->
                Log.e("KanjiExVM", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
    }

    fun onAnswerChange(text: String) {
        _uiState.value = _uiState.value.copy(answer = text)
    }

    fun selectOption(opt: String) {
        _uiState.value = _uiState.value.copy(selectedOption = opt, answer = opt)
    }

    fun check() {
        val state = _uiState.value
        val ex = state.exercises.getOrNull(state.index) ?: return
        val userAnswer = state.answer.trim()
        val correct = ex.answer?.any { it != null && userAnswer == it.trim() } == true

        val kanjiId = ex.id_kanji
        totalPerKanji[kanjiId] = (totalPerKanji[kanjiId] ?: 0) + 1
        if (correct) correctPerKanji[kanjiId] = (correctPerKanji[kanjiId] ?: 0) + 1

        val resultText = if (correct) {
            "Правильно ✅\n${ex.explanation ?: ""}"
        } else {
            "Ошибка ❌\nПравильный ответ: ${ex.answer?.joinToString(", ")}\n\n${ex.explanation ?: ""}"
        }

        _uiState.value = state.copy(
            resultText = resultText,
            isCorrect = correct,
            totalCorrect = state.totalCorrect + if (correct) 1 else 0,
            totalAnswered = state.totalAnswered + 1
        )
    }

    fun next() {
        val state = _uiState.value
        val nextIndex = state.index + 1

        if (nextIndex >= state.exercises.size) {
            finishAndSave()
            return
        }

        _uiState.value = state.copy(
            index = nextIndex,
            answer = "",
            selectedOption = null,
            resultText = null,
            isCorrect = false
        )
    }

    private fun finishAndSave() {
        val uid = FirebaseAuth.getInstance().uid ?: run {
            _uiState.value = _uiState.value.copy(finished = true)
            return
        }

        db.collection("Progress").document(uid).get()
            .addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                val alreadyLearned = (doc.get("kanjiLearned") as? List<Long>) ?: emptyList()
                var learnedNow = 0
                val totalKanji = totalPerKanji.size

                for ((kanjiId, total) in totalPerKanji) {
                    if (alreadyLearned.contains(kanjiId.toLong())) {
                        learnedNow++
                        continue
                    }
                    val correct = correctPerKanji[kanjiId] ?: 0
                    val percent = correct * 100f / total
                    if (percent >= 70f) {
                        learnedNow++
                        db.collection("Progress").document(uid).update(
                            "kanjiLearned", FieldValue.arrayUnion(kanjiId),
                            "kanjiDone", FieldValue.increment(1)
                        )
                        MainActivity.removeDailyRecommendation("kanji", kanjiId)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    finished = true,
                    learnedCount = learnedNow,
                    totalKanjiCount = totalKanji
                )
            }
    }
}