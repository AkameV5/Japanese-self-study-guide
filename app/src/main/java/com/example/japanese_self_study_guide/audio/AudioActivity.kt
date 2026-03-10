package com.example.japanese_self_study_guide.audio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.japanese_self_study_guide.audio.ui.AudioNavGraph
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme

class AudioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioId = intent.getIntExtra("audioId", -1)
        val audioUrl = intent.getStringExtra("audio_url") ?: ""
        val audioName = intent.getStringExtra("audio_name") ?: ""
        val audioDescription = intent.getStringExtra("audio_description") ?: ""
        val dailyMode = intent.getBooleanExtra("daily_mode", false)

        setContent {
            JapaneseSelfStudyGuideTheme {
                Surface {
                    AudioNavGraph(
                        onExit = { finish() },
                        startAudioId = if (dailyMode && audioId != -1) audioId else null,
                        startAudioUrl = audioUrl,
                        startAudioName = audioName,
                        startAudioDescription = audioDescription
                    )
                }
            }
        }
    }
}