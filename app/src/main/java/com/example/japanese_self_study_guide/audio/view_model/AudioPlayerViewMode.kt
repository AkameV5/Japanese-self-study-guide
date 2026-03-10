package com.example.japanese_self_study_guide.audio.view_model

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.audio.model.AudioCacheManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class AudioPlayerUiState(
    val isLoading: Boolean = true,
    val isPrepared: Boolean = false,
    val isPlaying: Boolean = false,
    val duration: Int = 0,
    val currentPosition: Int = 0,
    val speed: Float = 1.0f,
    val showExercisesButton: Boolean = false,
    val error: String? = null
)

class AudioPlayerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AudioPlayerUiState())
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private var player: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val seekRunnable = object : Runnable {
        override fun run() {
            player?.let {
                if (it.isPlaying) {
                    _uiState.value = _uiState.value.copy(currentPosition = it.currentPosition)
                    handler.postDelayed(this, 300)
                }
            }
        }
    }

    fun initPlayer(context: Context, audioId: Int, url: String) {
        AudioCacheManager.preload(context, audioId, url, object : AudioCacheManager.Callback {
            override fun onReady(file: File) {
                setupPlayer { mp -> mp.setDataSource(file.absolutePath) }
            }

            override fun onError() {
                setupPlayer { mp -> mp.setDataSource(url) }
            }
        })
    }

    private fun setupPlayer(dataSourceSetter: (MediaPlayer) -> Unit) {
        try {
            val mp = MediaPlayer()
            dataSourceSetter(mp)

            mp.setOnPreparedListener {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isPrepared = true,
                    duration = mp.duration
                )
            }
            mp.setOnCompletionListener {
                _uiState.value = _uiState.value.copy(
                    isPlaying = false,
                    showExercisesButton = true
                )
                handler.removeCallbacks(seekRunnable)
            }
            mp.prepareAsync()
            player = mp
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    fun togglePlayPause() {
        val mp = player ?: return
        if (!_uiState.value.isPrepared) return

        if (_uiState.value.isPlaying) {
            mp.pause()
            handler.removeCallbacks(seekRunnable)
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            mp.start()
            handler.post(seekRunnable)
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }

    fun seekTo(position: Int) {
        player?.seekTo(position)
        _uiState.value = _uiState.value.copy(currentPosition = position)
    }

    fun changeSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            player?.let { mp ->
                mp.playbackParams = PlaybackParams().setSpeed(speed)
                if (!_uiState.value.isPlaying) mp.pause()
                _uiState.value = _uiState.value.copy(speed = speed)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(seekRunnable)
        player?.release()
        player = null
    }
}