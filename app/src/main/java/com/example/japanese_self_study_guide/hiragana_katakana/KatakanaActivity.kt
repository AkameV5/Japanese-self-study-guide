package com.example.japanese_self_study_guide.hiragana_katakana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.japanese_self_study_guide.hiragana_katakana.ui.KatakanaNavGraph
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme

class KatakanaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dailyMode = intent.getBooleanExtra("daily_mode", false)
        val dailyIds  = intent.getIntArrayExtra("daily_katakana_ids")

        setContent {
            JapaneseSelfStudyGuideTheme {
                Surface {
                    KatakanaNavGraph(
                        onExit    = { finish() },
                        dailyMode = dailyMode,
                        dailyIds  = dailyIds?.toList()
                    )
                }
            }
        }
    }
}