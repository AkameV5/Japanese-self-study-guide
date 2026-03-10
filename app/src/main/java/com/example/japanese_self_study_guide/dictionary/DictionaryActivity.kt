package com.example.japanese_self_study_guide.dictionary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.japanese_self_study_guide.dictionary.ui.DictionaryScreen
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme

class DictionaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JapaneseSelfStudyGuideTheme {
                Surface {
                    DictionaryScreen(onBack = { finish() })
                }
            }
        }
    }
}