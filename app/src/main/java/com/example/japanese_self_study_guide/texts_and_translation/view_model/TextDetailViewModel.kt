package com.example.japanese_self_study_guide.texts_and_translation.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.texts_and_translation.ExerciseModel
import com.example.japanese_self_study_guide.texts_and_translation.TextsRepository
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
    val currentTitle get() = if (showTranslation) translationTitle.ifBlank { japaneseTitle } else japaneseTitle
    val isLastParagraph get() = currentIndex == currentParagraphs.size - 1 && currentParagraphs.isNotEmpty()
    val canGoPrev get() = currentIndex > 0
    val canGoNext get() = currentIndex < currentParagraphs.size - 1
}

class TextDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<Application>()
    private val repository = TextsRepository()
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
        repository.getTextById(textId)
            .addOnSuccessListener { text ->
                if (text == null) {
                    Log.e("TextDetailVM", "Text not found for id=$textId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = app.getString(R.string.texts_not_found, textId)
                    )
                    return@addOnSuccessListener
                }
                val paragraphs = groupIntoParagraphs(text.sentences ?: emptyList())
                Log.d("TextDetailVM", "Loaded text id=$textId, paragraphs=${paragraphs.size}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    japaneseTitle = text.title ?: "",
                    japaneseParagraphs = paragraphs
                )
            }
            .addOnFailureListener { e ->
                Log.e("TextDetailVM", "Failed to load text", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
    }

    private fun loadTranslation(textId: Int) {
        repository.getTranslationByTextId(textId)
            .addOnSuccessListener { translation ->
                if (translation == null) return@addOnSuccessListener
                val paragraphs = groupIntoParagraphs(translation.sentences ?: emptyList())
                _uiState.value = _uiState.value.copy(
                    translationTitle = translation.translationTitle ?: "",
                    translationParagraphs = paragraphs
                )
            }
    }

    private fun loadExercises(textId: Int) {
        repository.getExercisesByTextId(textId)
            .addOnSuccessListener { list ->
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
        for (sentence in sentences) {
            if (sentence.trim().isEmpty()) {
                if (builder.isNotEmpty()) {
                    paragraphs.add(builder.toString().trim())
                    builder.clear()
                }
            } else {
                builder.append(sentence).append("\n")
            }
        }
        if (builder.isNotEmpty()) paragraphs.add(builder.toString().trim())
        return paragraphs
    }
}
