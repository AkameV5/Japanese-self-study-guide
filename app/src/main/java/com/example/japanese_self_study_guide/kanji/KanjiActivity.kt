package com.example.japanese_self_study_guide.kanji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.japanese_self_study_guide.kanji.ui.KanjiNavGraph
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme

class KanjiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dailyMode  = intent.getBooleanExtra("daily_mode", false)
        val dailyIds   = intent.getIntArrayExtra("daily_kanji_ids")
        val dailyStart = intent.getIntExtra("daily_start_id", 0)
        val dailyEnd   = intent.getIntExtra("daily_end_id", 0)
        val dailyLimit = intent.getIntExtra("daily_limit", 999)

        setContent {
            JapaneseSelfStudyGuideTheme {
                Surface {
                    KanjiNavGraph(
                        onExit       = { finish() },
                        dailyMode    = dailyMode,
                        dailyIds     = dailyIds,
                        dailyStartId = dailyStart,
                        dailyEndId   = dailyEnd,
                        dailyLimit   = dailyLimit
                    )
                }
            }
        }
    }
}