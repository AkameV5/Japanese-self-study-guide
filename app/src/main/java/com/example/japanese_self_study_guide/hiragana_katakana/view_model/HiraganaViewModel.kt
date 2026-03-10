package com.example.japanese_self_study_guide.hiragana_katakana.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaItem
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HiraganaListUiState(
    val isLoading: Boolean = true,
    val symbols: List<HiraganaItem> = emptyList(),
    val learnedIds: List<Int> = emptyList(),
    val error: String? = null
)

class HiraganaViewModel(
    private val repository: HiraganaRepository = HiraganaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HiraganaListUiState())
    val uiState: StateFlow<HiraganaListUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        repository.symbols
            .addOnSuccessListener { symbols ->
                _uiState.value = _uiState.value.copy(isLoading = false, symbols = symbols)
                repository.learnedIds
                    .addOnSuccessListener { ids ->
                        _uiState.value = _uiState.value.copy(learnedIds = ids)
                    }
            }
            .addOnFailureListener { e ->
                _uiState.value = HiraganaListUiState(isLoading = false, error = e.message)
            }
    }
}