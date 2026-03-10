package com.example.japanese_self_study_guide.audio.view_model

import android.util.Log
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
        repository.getAudioList()
            .addOnSuccessListener { audioList ->
                Log.d("AudioVM", "Loaded ${audioList.size} audio items")
                repository.getLearnedAudioIds()
                    .addOnSuccessListener { learnedIds ->
                        _uiState.value = AudioListUiState(
                            isLoading = false,
                            audioList = audioList,
                            learnedIds = learnedIds
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e("AudioVM", "getLearnedIds error: ${e.message}")
                        _uiState.value = AudioListUiState(
                            isLoading = false,
                            audioList = audioList
                        )
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AudioVM", "getAudioList error: ${e.message}")
                _uiState.value = AudioListUiState(isLoading = false, error = e.message)
            }
    }
}