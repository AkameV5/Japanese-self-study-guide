package com.example.japanese_self_study_guide.audio.view_model

import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.audio.AudioRepository
import com.example.japanese_self_study_guide.audio.model.AudioModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioListUiState(
    val isLoading: Boolean = true,
    val audioList: List<AudioModel> = emptyList(),
    val learnedIds: List<Int> = emptyList(),
    val error: String? = null
)

class AudioViewModel(
    private val repository: AudioRepository = AudioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioListUiState())
    val uiState: StateFlow<AudioListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        repository.audioList
            .addOnSuccessListener { audioList ->
                repository.learnedAudioIds
                    .addOnSuccessListener { learnedIds ->
                        _uiState.value = AudioListUiState(
                            isLoading = false,
                            audioList = audioList,
                            learnedIds = learnedIds
                        )
                    }
                    .addOnFailureListener {
                        _uiState.value = AudioListUiState(
                            isLoading = false,
                            audioList = audioList
                        )
                    }
            }
            .addOnFailureListener { e ->
                _uiState.value = AudioListUiState(isLoading = false, error = e.message)
            }
    }
}