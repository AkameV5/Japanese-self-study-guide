package com.example.japanese_self_study_guide.grammar.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.grammar.GrammarRepository
import com.example.japanese_self_study_guide.grammar.GrammarRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GrammarListUiState(
    val isLoading: Boolean = true,
    val rules: List<GrammarRule> = emptyList(),
    val learnedIds: List<Int> = emptyList(),
    val error: String? = null
)

class GrammarViewModel(
    private val repository: GrammarRepository = GrammarRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarListUiState())
    val uiState: StateFlow<GrammarListUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        repository.getRules()
            .addOnSuccessListener { rules ->
                _uiState.value = _uiState.value.copy(isLoading = false, rules = rules)
                repository.getLearnedIds()
                    .addOnSuccessListener { ids ->
                        _uiState.value = _uiState.value.copy(learnedIds = ids)
                    }
            }
            .addOnFailureListener { e ->
                _uiState.value = GrammarListUiState(isLoading = false, error = e.message)
            }
    }
}