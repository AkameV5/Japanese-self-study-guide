package com.example.japanese_self_study_guide.texts_and_translation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.japanese_self_study_guide.texts_and_translation.ui.TextsNavGraph
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme

class TextsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dailyMode = intent.getBooleanExtra("daily_mode", false)
        val rawTextId = intent.getIntExtra("textId", -1)
        val startTextId = if (dailyMode && rawTextId > 0) rawTextId else -1

        setContent {
            JapaneseSelfStudyGuideTheme {
                TextsNavGraph(
                    startTextId = startTextId,
                    onBack = { finish() }
                )
            }
        }
    }
}