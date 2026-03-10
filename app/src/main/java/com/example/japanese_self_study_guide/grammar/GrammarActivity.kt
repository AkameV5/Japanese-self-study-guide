package com.example.japanese_self_study_guide.grammar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.japanese_self_study_guide.grammar.ui.GrammarNavGraph
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme

class GrammarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dailyMode = intent.getBooleanExtra("daily_mode", false)
        val grammarId = intent.getIntExtra("id", -1)

        setContent {
            JapaneseSelfStudyGuideTheme {
                Surface {
                    GrammarNavGraph(
                        onExit = { finish() },
                        startGrammarId  = if (dailyMode && grammarId != -1) grammarId else null
                    )
                }
            }
        }
    }
}