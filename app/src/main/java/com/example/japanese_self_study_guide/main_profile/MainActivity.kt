package com.example.japanese_self_study_guide.main_profile

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.audio.AudioActivity
import com.example.japanese_self_study_guide.dictionary.DictionaryActivity
import com.example.japanese_self_study_guide.grammar.GrammarActivity
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaActivity
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaActivity
import com.example.japanese_self_study_guide.kanji.KanjiActivity
import com.example.japanese_self_study_guide.login_and_registration.LoginActivity
import com.example.japanese_self_study_guide.texts_and_translation.TextsActivity
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var mAuth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    private val langState = mutableStateOf("ru")

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val lang = prefs.getString("My_Lang", "ru") ?: "ru"
        applyLocale(lang)
        langState.value = lang

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.accentPinkDark)
        }

        mAuth     = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        viewModel.start(lang)
        scheduleDailyReminder()

        setContent {
            val currentLang by langState

            JapaneseSelfStudyGuideTheme {
                MainActivityContent(
                    viewModel    = viewModel,
                    currentLang  = currentLang,
                    onTileClick  = { rec -> openRecommendedLesson(rec) },
                    onNavigate   = { dest -> navigateTo(dest) },
                    onLangChange = { newLang ->
                        prefs.edit().putString("My_Lang", newLang).apply()
                        applyLocale(newLang)
                        langState.value = newLang
                        viewModel.start(newLang)
                        recreate()
                    },
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        val i = Intent(this, LoginActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(i)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        if (user != null) ProgressManager.initProgressIfNeeded(user.uid)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserHeader()

        val savedLang = prefs.getString("My_Lang", "ru") ?: "ru"
        if (langState.value != savedLang) {
            applyLocale(savedLang)
            langState.value = savedLang
            viewModel.start(savedLang)
            recreate()
        }
    }

    private fun applyLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun navigateTo(dest: String) {
        val intent = when (dest) {
            "profile"    -> Intent(this, ProfileActivity::class.java)
            "hiragana"   -> Intent(this, HiraganaActivity::class.java)
            "katakana"   -> Intent(this, KatakanaActivity::class.java)
            "kanji"      -> Intent(this, KanjiActivity::class.java)
            "dictionary" -> Intent(this, DictionaryActivity::class.java)
            "grammar"    -> Intent(this, GrammarActivity::class.java)
            "texts"      -> Intent(this, TextsActivity::class.java)
            "audio"      -> Intent(this, AudioActivity::class.java)
            else         -> return
        }
        startActivity(intent)
    }

    private fun openRecommendedLesson(rec: Map<String, Any>) {
        val type = rec["type"] as? String ?: return
        @Suppress("UNCHECKED_CAST")
        val payload = rec["payload"] as? Map<String, Any> ?: return

        fun extractIds(key: String = "ids"): IntArray? {
            val raw = payload[key] as? List<*> ?: return null
            return IntArray(raw.size) { i ->
                val v = raw[i]
                if (v is Long) v.toInt() else v as Int
            }
        }

        fun longVal(key: String): Int? {
            val v = payload[key] ?: return null
            return if (v is Long) v.toInt() else (v as? Int)
        }

        when (type) {
            "hiragana" -> {
                val intent = Intent(this, HiraganaActivity::class.java)
                extractIds()?.let { intent.putExtra("daily_hiragana_ids", it) }
                intent.putExtra("daily_mode", true); startActivity(intent)
            }
            "katakana" -> {
                val intent = Intent(this, KatakanaActivity::class.java)
                extractIds()?.let { intent.putExtra("daily_katakana_ids", it) }
                intent.putExtra("daily_mode", true); startActivity(intent)
            }
            "kanji" -> {
                val intent = Intent(this, KanjiActivity::class.java)
                extractIds()?.let { intent.putExtra("daily_kanji_ids", it) }
                longVal("startId")?.let { intent.putExtra("daily_start_id", it) }
                longVal("endId")?.let   { intent.putExtra("daily_end_id", it) }
                longVal("limit")?.let   { intent.putExtra("daily_limit", it) }
                intent.putExtra("daily_mode", true); startActivity(intent)
            }
            "grammar" -> {
                val id = longVal("id") ?: return
                startActivity(Intent(this, GrammarActivity::class.java).apply {
                    putExtra("id", id); putExtra("daily_mode", true)
                })
            }
            "text" -> {
                val id = longVal("id") ?: return
                startActivity(Intent(this, TextsActivity::class.java).apply {
                    putExtra("textId", id); putExtra("daily_mode", true)
                })
            }
            "audio" -> {
                val audioId = longVal("id")?.toLong() ?: return
                FirebaseFirestore.getInstance().collection("Audio")
                    .whereEqualTo("id", audioId).get()
                    .addOnSuccessListener { query ->
                        if (!query.isEmpty) {
                            val doc = query.documents[0]
                            val audioPath = doc.getString("audioPath") ?: ""
                            val fullUrl = if (audioPath.isNotEmpty())
                                "https://raw.githubusercontent.com/AkameV5/Japanese-self-study-guide/master/audio/$audioPath"
                            else ""
                            startActivity(Intent(this, AudioActivity::class.java).apply {
                                putExtra("audioId", audioId.toInt())
                                putExtra("audio_url", fullUrl)
                                putExtra("audio_name", doc.getString("name"))
                                putExtra("audio_description", doc.getString("description"))
                                putExtra("daily_mode", true)
                            })
                        }
                    }
            }
        }
    }

    companion object {
        @JvmStatic
        fun removeDailyRecommendation(type: String, id: Int) {
            MainViewModel.removeDailyRecommendation(type, id)
        }
    }

    private fun scheduleDailyReminder() {
        val now    = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        if (now.after(target)) target.add(Calendar.DAY_OF_YEAR, 1)

        val intent = Intent(this, DailyReminderWorker::class.java)
        val pi = PendingIntent.getBroadcast(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.RTC_WAKEUP, target.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
    }
}