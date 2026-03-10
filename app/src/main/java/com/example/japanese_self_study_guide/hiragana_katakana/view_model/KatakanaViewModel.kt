package com.example.japanese_self_study_guide.hiragana_katakana.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaItem
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class KatakanaListUiState(
    val isLoading: Boolean = true,
    val symbols: List<HiraganaItem> = emptyList(),
    val learnedIds: List<Int> = emptyList(),
    val error: String? = null
)

class KatakanaViewModel(
    private val repository: KatakanaRepository = KatakanaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(KatakanaListUiState())
    val uiState: StateFlow<KatakanaListUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        repository.symbols
            .addOnSuccessListener { symbols ->
                Log.d("KatakanaVM", "Loaded ${symbols.size} symbols")
                _uiState.value = _uiState.value.copy(isLoading = false, symbols = symbols)
                repository.learnedIds
                    .addOnSuccessListener { ids ->
                        _uiState.value = _uiState.value.copy(learnedIds = ids)
                    }
                    .addOnFailureListener { e ->
                        Log.e("KatakanaVM", "getLearnedIds error: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("KatakanaVM", "getSymbols error: ${e.message}")
                _uiState.value = KatakanaListUiState(isLoading = false, error = e.message)
            }
    }
}