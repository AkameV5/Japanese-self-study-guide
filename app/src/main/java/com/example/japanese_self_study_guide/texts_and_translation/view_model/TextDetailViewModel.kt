package com.example.japanese_self_study_guide.texts_and_translation.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.texts_and_translation.ExerciseModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TextDetailUiState(
    val isLoading: Boolean = true,
    val japaneseTitle: String = "",
    val translationTitle: String = "",
    val japaneseParagraphs: List<String> = emptyList(),
    val translationParagraphs: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val showTranslation: Boolean = false,
    val exercises: List<ExerciseModel> = emptyList(),
    val error: String? = null
) {
    val currentParagraphs get() = if (showTranslation) translationParagraphs else japaneseParagraphs
    val currentText get() = currentParagraphs.getOrNull(currentIndex) ?: ""
    val currentTitle get() = if (showTranslation)
        translationTitle.ifBlank { "Перевод" } else japaneseTitle
    val isLastParagraph get() = currentIndex == currentParagraphs.size - 1 && currentParagraphs.isNotEmpty()
    val canGoPrev get() = currentIndex > 0
    val canGoNext get() = currentIndex < currentParagraphs.size - 1
}

class TextDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(TextDetailUiState())
    val uiState: StateFlow<TextDetailUiState> = _uiState.asStateFlow()

    private var loadedTextId: Int = -1

    fun load(textId: Int) {
        if (loadedTextId == textId) return
        loadedTextId = textId
        _uiState.value = TextDetailUiState(isLoading = true)
        loadText(textId)
        loadTranslation(textId)
        loadExercises(textId)
    }

    private fun loadText(textId: Int) {
        db.collection("Texts")
            .get()
            .addOnSuccessListener { query ->
                val doc = query.documents.firstOrNull { doc ->
                    val id = doc.getLong("id") ?: doc.get("id")?.toString()?.toLongOrNull()
                    id?.toInt() == textId
                }
                if (doc == null) {
                    Log.e("TextDetailVM", "Text not found for id=$textId")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Текст не найден (id=$textId)")
                    return@addOnSuccessListener
                }
                val title = doc.getString("title") ?: ""
                @Suppress("UNCHECKED_CAST")
                val sentences = doc.get("sentences") as? List<String> ?: emptyList()
                val paragraphs = groupIntoParagraphs(sentences)
                Log.d("TextDetailVM", "Loaded text id=$textId, paragraphs=${paragraphs.size}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    japaneseTitle = title,
                    japaneseParagraphs = paragraphs
                )
            }
            .addOnFailureListener { e ->
                Log.e("TextDetailVM", "Failed to load text", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
    }

    private fun loadTranslation(textId: Int) {
        db.collection("Translations_texts")
            .get()
            .addOnSuccessListener { query ->
                val doc = query.documents.firstOrNull { doc ->
                    val id = doc.getLong("textId") ?: doc.get("textId")?.toString()?.toLongOrNull()
                    id?.toInt() == textId
                } ?: return@addOnSuccessListener

                val title = doc.getString("translationTitle") ?: ""
                @Suppress("UNCHECKED_CAST")
                val sentences = doc.get("sentences") as? List<String> ?: emptyList()
                val paragraphs = groupIntoParagraphs(sentences)
                _uiState.value = _uiState.value.copy(
                    translationTitle = title,
                    translationParagraphs = paragraphs
                )
            }
    }

    private fun loadExercises(textId: Int) {
        db.collection("TextsExercises")
            .get()
            .addOnSuccessListener { query ->
                val list = query.documents
                    .filter { doc ->
                        val id = doc.getLong("textId") ?: doc.get("textId")?.toString()?.toLongOrNull()
                        id?.toInt() == textId
                    }
                    .mapNotNull { it.toObject(ExerciseModel::class.java) }
                _uiState.value = _uiState.value.copy(exercises = list)
            }
    }

    fun toggleLanguage(showTranslation: Boolean) {
        val s = _uiState.value
        val targetList = if (showTranslation) s.translationParagraphs else s.japaneseParagraphs
        val clampedIndex = s.currentIndex.coerceAtMost((targetList.size - 1).coerceAtLeast(0))
        _uiState.value = s.copy(showTranslation = showTranslation, currentIndex = clampedIndex)
    }

    fun goNext() {
        val s = _uiState.value
        if (s.canGoNext) _uiState.value = s.copy(currentIndex = s.currentIndex + 1)
    }

    fun goPrev() {
        val s = _uiState.value
        if (s.canGoPrev) _uiState.value = s.copy(currentIndex = s.currentIndex - 1)
    }

    private fun groupIntoParagraphs(sentences: List<String>): List<String> {
        val paragraphs = mutableListOf<String>()
        val builder = StringBuilder()
        for (s in sentences) {
            if (s.trim().isEmpty()) {
                if (builder.isNotEmpty()) {
                    paragraphs.add(builder.toString().trim())
                    builder.clear()
                }
            } else {
                builder.append(s).append("\n")
            }
        }
        if (builder.isNotEmpty()) paragraphs.add(builder.toString().trim())
        return paragraphs
    }
}