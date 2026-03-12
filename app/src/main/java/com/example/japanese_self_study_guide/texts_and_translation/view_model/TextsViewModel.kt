package com.example.japanese_self_study_guide.texts_and_translation.view_model

import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.texts_and_translation.TextModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TextsUiState(
    val isLoading: Boolean = true,
    val allTexts: List<TextModel> = emptyList(),
    val displayedTexts: List<TextModel> = emptyList(),
    val learnedIds: Set<Int> = emptySet(),
    val query: String = "",
    val jlptFilter: String = "Все уровни",
    val error: String? = null
)

class TextsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(TextsUiState())
    val uiState: StateFlow<TextsUiState> = _uiState.asStateFlow()

    init {
        loadTexts()
        loadLearnedIds()
    }

    private fun loadTexts() {
        db.collection("Texts").get()
            .addOnSuccessListener { query ->
                val list = query.documents
                    .mapNotNull { doc -> doc.toObject(TextModel::class.java) }
                    .sortedBy { it.id }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allTexts = list,
                    displayedTexts = list
                )
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
    }

    private fun loadLearnedIds() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        db.collection("Progress").document(uid).get()
            .addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                val learned = (doc.get("textsLearned") as? List<Long>)
                    ?.map { it.toInt() }?.toSet() ?: emptySet()
                _uiState.value = _uiState.value.copy(learnedIds = learned)
            }
    }

    fun onQueryChange(q: String) {
        val s = _uiState.value
        _uiState.value = s.copy(
            query = q,
            displayedTexts = applyFilters(s.allTexts, q, s.jlptFilter)
        )
    }

    fun onJlptFilter(level: String) {
        val s = _uiState.value
        _uiState.value = s.copy(
            jlptFilter = level,
            displayedTexts = applyFilters(s.allTexts, s.query, level)
        )
    }

    private fun applyFilters(list: List<TextModel>, query: String, jlpt: String): List<TextModel> {
        var r = list
        if (query.isNotBlank())
            r = r.filter { it.title?.lowercase()?.contains(query.lowercase()) == true }
        if (jlpt != "Все уровни")
            r = r.filter { "N${it.difficultyLevel}" == jlpt }
        return r
    }
}