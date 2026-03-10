package com.example.japanese_self_study_guide.audio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.japanese_self_study_guide.audio.ui.AudioNavGraph
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme

class AudioActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioId          = intent.getIntExtra("audioId", -1)
        val audioUrl         = intent.getStringExtra("audio_url") ?: ""
        val audioName        = intent.getStringExtra("audio_name") ?: ""
        val audioDescription = intent.getStringExtra("audio_description") ?: ""
        val dailyMode        = intent.getBooleanExtra("daily_mode", false)

        if (dailyMode && audioId != -1) {
            pendingDailyAudioId          = audioId
            pendingDailyAudioUrl         = audioUrl
            pendingDailyAudioName        = audioName
            pendingDailyAudioDescription = audioDescription
        } else {
            pendingDailyAudioId = null
        }

        setContent {
            JapaneseSelfStudyGuideTheme {
                Surface {
                    AudioNavGraph(
                        onExit = { finish() },
                        dailyMode = dailyMode && audioId != -1
                    )
                }
            }
        }
    }

    companion object {
        var pendingDailyAudioId: Int?          = null
        var pendingDailyAudioUrl: String       = ""
        var pendingDailyAudioName: String      = ""
        var pendingDailyAudioDescription: String = ""
    }
}