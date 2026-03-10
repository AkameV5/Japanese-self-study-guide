package com.example.japanese_self_study_guide.dictionary

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.Collator
import java.util.Locale

enum class SortOption { ALPHABETICAL, BY_CATEGORY, BY_LENGTH }

data class DictionaryUiState(
    val isLoading: Boolean = true,
    val allWords: List<Word> = emptyList(),
    val displayedWords: List<Word> = emptyList(),
    val query: String = "",
    val sortOption: SortOption = SortOption.ALPHABETICAL,
    val error: String? = null
)

class DictionaryViewModel(
    private val repository: DictionaryRepository = DictionaryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    private val collator: Collator = Collator.getInstance(Locale.JAPANESE).apply {
        strength = Collator.PRIMARY
    }

    init {
        loadWords()
    }

    private fun loadWords() {
        repository.words
            .addOnSuccessListener { words ->
                val sorted = applySort(words, SortOption.ALPHABETICAL)
                _uiState.value = DictionaryUiState(
                    isLoading = false,
                    allWords = words,
                    displayedWords = sorted
                )
            }
            .addOnFailureListener { e ->
                _uiState.value = DictionaryUiState(isLoading = false, error = e.message)
            }
    }

    fun onQueryChange(query: String) {
        val state = _uiState.value
        val filtered = filterWords(state.allWords, query)
        val sorted = applySort(filtered, state.sortOption)
        _uiState.value = state.copy(query = query, displayedWords = sorted)
    }

    fun onSortChange(option: SortOption) {
        val state = _uiState.value
        val sorted = applySort(state.displayedWords, option)
        _uiState.value = state.copy(sortOption = option, displayedWords = sorted)
    }

    private fun filterWords(words: List<Word>, query: String): List<Word> {
        if (query.isBlank()) return words
        val lower = query.lowercase()
        return words.filter { w ->
            w.word.contains(lower) ||
                    w.reading.contains(lower) ||
                    w.translation.lowercase().contains(lower) ||
                    w.category.lowercase().contains(lower)
        }
    }

    private fun applySort(words: List<Word>, option: SortOption): List<Word> =
        when (option) {
            SortOption.ALPHABETICAL -> words.sortedWith(
                compareBy({ wordGroup(it) }, { collator.getCollationKey(sortKey(it)) })
            )
            SortOption.BY_CATEGORY -> words.sortedWith(
                Comparator { w1, w2 ->
                    val cat = collator.compare(w1.category, w2.category)
                    if (cat != 0) cat else collator.compare(w1.reading, w2.reading)
                }
            )
            SortOption.BY_LENGTH -> words.sortedBy { it.word.length }
        }

    private fun wordGroup(w: Word): Int {
        val c = w.word.firstOrNull() ?: return 2
        val block = Character.UnicodeBlock.of(c)
        if (block == Character.UnicodeBlock.HIRAGANA ||
            block == Character.UnicodeBlock.KATAKANA ||
            block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
            block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) return 0
        if (c in 'A'..'Z' || c in 'a'..'z') return 1
        return 2
    }

    private fun sortKey(w: Word): String = w.reading.ifBlank { w.word }
}