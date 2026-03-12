package com.example.japanese_self_study_guide.main_profile

import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaGroupProvider
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaGroupProvider
import com.example.japanese_self_study_guide.kanji.ExerciseGroup
import com.example.japanese_self_study_guide.kanji.GroupsProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TileStatus { PENDING, DONE }

data class DailyTile(
    val type: String,
    val title: String,
    val subtitle: String,
    val status: TileStatus = TileStatus.PENDING,
    val rec: Map<String, Any>
)

data class MainUiState(
    val isLoading: Boolean = true,
    val tiles: List<DailyTile> = emptyList(),
    val allDone: Boolean = false,
    val username: String = "",
    val userEmail: String = "",
    val avatarBase64: String? = null
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var dailyListener: ListenerRegistration? = null

    private val typeNames = mapOf(
        "ru" to mapOf(
            "hiragana" to "Хирагана",
            "katakana" to "Катакана",
            "kanji"    to "Кандзи",
            "grammar"  to "Грамматика",
            "text"     to "Тексты",
            "audio"    to "Аудио"
        ),
        "ja" to mapOf(
            "hiragana" to "ひらがな",
            "katakana" to "カタカナ",
            "kanji"    to "漢字",
            "grammar"  to "文法",
            "text"     to "テキスト",
            "audio"    to "音声"
        )
    )

    fun start(lang: String = "ru") {
        loadUserHeader()
        listenDailyRecommendations(lang)
    }

    fun loadUserHeader() {
        val user = auth.currentUser ?: return
        db.collection("Users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                _uiState.value = _uiState.value.copy(
                    username    = doc.getString("username") ?: "",
                    userEmail   = doc.getString("email") ?: user.email ?: "",
                    avatarBase64 = doc.getString("profilePhoto")
                )
            }
    }

    fun listenDailyRecommendations(lang: String = "ru") {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val ref = db.collection("Daily").document(uid)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        dailyListener?.remove()
        dailyListener = ref.addSnapshotListener { doc, e ->
            if (e != null) { _uiState.value = _uiState.value.copy(isLoading = false); return@addSnapshotListener }

            if (doc == null || !doc.exists() || doc.getString("date") != today) {
                generateRecommendations(ref, today, uid, lang)
                return@addSnapshotListener
            }

            @Suppress("UNCHECKED_CAST")
            val list = doc.get("recommendations") as? List<Map<String, Any>> ?: emptyList()
            showTiles(list, lang)
        }
    }

    private fun generateRecommendations(ref: DocumentReference, today: String, uid: String, lang: String) {
        db.collection("Progress").document(uid).get().addOnSuccessListener { doc ->

            @Suppress("UNCHECKED_CAST")
            fun learnedList(field: String) = (doc.get(field) as? List<Long>) ?: emptyList()

            val hiraganaLearned = learnedList("hiraganaLearned")
            val katakanaLearned = learnedList("katakanaLearned")
            val kanjiLearned    = learnedList("kanjiLearned")
            val grammarLearned  = learnedList("grammarLearned")
            val textsLearned    = learnedList("textsLearned")
            val audioLearned    = learnedList("audioLearned")

            val newList = mutableListOf<Map<String, Any>>()

            pickNextFromGroups(HiraganaGroupProvider.GROUPS_ALL, hiraganaLearned, 5).takeIf { it.isNotEmpty() }?.let {
                newList += makeRecMap("hiragana", mapOf("ids" to it))
            }
            pickNextFromGroups(KatakanaGroupProvider.GROUPS_ALL, katakanaLearned, 5).takeIf { it.isNotEmpty() }?.let {
                newList += makeRecMap("katakana", mapOf("ids" to it))
            }
            pickNextKanjiIds(kanjiLearned).takeIf { it.isNotEmpty() }?.let { ids ->
                val group = findGroupForIds(ids)
                if (group != null) {
                    newList += makeRecMap("kanji", mapOf(
                        "ids" to ids, "startId" to group.startId,
                        "endId" to group.endId, "limit" to group.limit
                    ))
                }
            }
            val grammarId = pickNextSingleId(grammarLearned, "Grammar")

            pickNextTextIdByLevel(textsLearned) { textResult ->
                if (textResult != null) {
                    newList += makeRecMap("text", mapOf(
                        "id"   to textResult.first.toInt(),
                        "name" to textResult.second
                    ))
                }
                val audioId = pickNextSingleId(audioLearned, "Audio")

                var pendingFetches = 0
                val afterFetches = {
                    val data = mapOf("date" to today, "recommendations" to newList)
                    ref.set(data)
                }

                if (grammarId != null) {
                    pendingFetches++
                    db.collection("Grammar").whereEqualTo("id", grammarId).limit(1).get()
                        .addOnCompleteListener { task ->
                            val name = task.result?.documents?.firstOrNull()?.getString("structure") ?: grammarId.toString()
                            newList += makeRecMap("grammar", mapOf("id" to grammarId.toInt(), "name" to name))
                            pendingFetches--
                            if (pendingFetches == 0) afterFetches()
                        }
                }
                if (audioId != null) {
                    pendingFetches++
                    db.collection("Audio").whereEqualTo("id", audioId).limit(1).get()
                        .addOnCompleteListener { task ->
                            val name = task.result?.documents?.firstOrNull()?.getString("name") ?: audioId.toString()
                            newList += makeRecMap("audio", mapOf("id" to audioId.toInt(), "name" to name))
                            pendingFetches--
                            if (pendingFetches == 0) afterFetches()
                        }
                }
                if (pendingFetches == 0) afterFetches()
            }
        }
    }

    private fun showTiles(list: List<Map<String, Any>>, lang: String) {
        val names = typeNames[lang] ?: typeNames["ru"]!!

        if (list.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = false, tiles = emptyList(), allDone = true)
            return
        }

        val tiles = list.map { rec ->
            val type = rec["type"] as? String ?: ""
            @Suppress("UNCHECKED_CAST")
            val meta = rec["meta"] as? Map<String, Any> ?: emptyMap()
            val subtitle = buildSubtitle(type, meta, lang)
            DailyTile(
                type     = type,
                title    = names[type] ?: type,
                subtitle = subtitle,
                status   = if (rec["done"] == true) TileStatus.DONE else TileStatus.PENDING,
                rec      = rec
            )
        }
        val allDone = tiles.all { it.status == TileStatus.DONE }
        _uiState.value = _uiState.value.copy(isLoading = false, tiles = tiles, allDone = allDone)
    }

    private fun buildSubtitle(type: String, meta: Map<String, Any>, lang: String): String {
        val isJa = lang == "ja"
        return when (type) {
            "hiragana" -> {
                val ids = (meta["ids"] as? List<*>)?.joinToString("  ") { "・" } ?: ""
                if (isJa) "今日の記号 ID: ${meta["ids_str"] ?: meta["count"]}"
                else "Символы: ${meta["ids_str"] ?: "${meta["count"]} шт."}"
            }
            "katakana" -> {
                if (isJa) "今日の記号 ID: ${meta["ids_str"] ?: meta["count"]}"
                else "Символы: ${meta["ids_str"] ?: "${meta["count"]} шт."}"
            }
            "kanji" -> {
                if (isJa) "今日の漢字 ${meta["count"]}字 (ID ${meta["startId"]}〜${meta["endId"]})"
                else "Кандзи: ${meta["count"]} шт. (ID ${meta["startId"]}–${meta["endId"]})"
            }
            "grammar"  -> meta["name"]?.toString()
                ?: if (isJa) "新しい文法を学ぶ" else "Изучить новую грамматику"
            "text"     -> meta["name"]?.toString()
                ?: if (isJa) "新しいテキストを読む" else "Прочитать новый текст"
            "audio"    -> meta["name"]?.toString()
                ?: if (isJa) "新しい音声を聴く" else "Прослушать аудио"
            else       -> ""
        }
    }

    companion object {
        @JvmStatic
        fun removeDailyRecommendation(type: String, id: Int) {
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("Daily").document(user.uid)

            db.runTransaction { tx ->
                val doc: DocumentSnapshot = tx.get(ref)
                if (!doc.exists()) return@runTransaction null

                @Suppress("UNCHECKED_CAST")
                val list = doc.get("recommendations") as? List<Map<String, Any>> ?: return@runTransaction null

                val newList = list.map { rec ->
                    if (rec["type"] != type) return@map rec

                    @Suppress("UNCHECKED_CAST")
                    val payload = rec["payload"] as? Map<String, Any> ?: return@map rec

                    val matches = when (type) {
                        "hiragana", "katakana", "kanji" -> {
                            @Suppress("UNCHECKED_CAST")
                            val ids = (payload["ids"] as? List<Long>) ?: emptyList()
                            ids.contains(id.toLong())
                        }
                        else -> {
                            val v = payload["id"]
                            val recId = when (v) { is Long -> v; is Int -> v.toLong(); else -> -1L }
                            recId == id.toLong()
                        }
                    }

                    if (matches) rec.toMutableMap().also { it["done"] = true }
                    else rec
                }
                tx.update(ref, "recommendations", newList)
                null
            }
        }
    }

    private fun pickNextFromGroups(groups: Array<IntArray>, learned: List<Long>, n: Int): List<Int> {
        val learnedSet = learned.map { it.toInt() }.toHashSet()
        for (group in groups) {
            val notLearned = group.filter { it !in learnedSet }
            if (notLearned.isNotEmpty()) return notLearned.take(n)
        }
        return emptyList()
    }

    private fun pickNextKanjiIds(learned: List<Long>): List<Int> {
        val learnedSet = learned.map { it.toInt() }.toHashSet()
        for (g in GroupsProvider.getGroups()) {
            val res = mutableListOf<Int>()
            for (id in g.startId..g.endId) {
                if (id !in learnedSet) {
                    res += id
                    if (res.size >= g.limit) return res
                }
            }
            if (res.isNotEmpty()) return res
        }
        return emptyList()
    }

    private fun findGroupForIds(ids: List<Int>): ExerciseGroup? {
        val first = ids.firstOrNull() ?: return null
        return GroupsProvider.getGroups().find { first >= it.startId && first <= it.endId }
    }

    private fun pickNextSingleId(learned: List<Long>, collection: String): Long? {
        val learnedSet = learned.toHashSet()
        for (id in 1L..500L) {
            if (id !in learnedSet) return id
        }
        return null
    }

    private fun pickNextTextIdByLevel(learned: List<Long>, callback: (Pair<Long, String>?) -> Unit) {
        db.collection("Texts").get()
            .addOnSuccessListener { query ->
                var bestId: Long? = null
                var bestName = ""
                var bestLevel = 999
                for (doc in query) {
                    val id = doc.getLong("id") ?: continue
                    if (id <= 0 || id in learned) continue
                    val lvl = parseLevel(doc.getString("difficultyLevel"))
                    if (lvl < bestLevel) {
                        bestLevel = lvl
                        bestId = id
                        bestName = doc.getString("title") ?: id.toString()
                    }
                }
                callback(bestId?.let { it to bestName })
            }
            .addOnFailureListener { callback(null) }
    }

    private fun parseLevel(raw: String?): Int {
        val s = raw?.trim()?.uppercase() ?: return 999
        return when {
            s.contains("N5") || s == "5" -> 1
            s.contains("N4") || s == "4" -> 2
            s.contains("N3") || s == "3" -> 3
            s.contains("N2") || s == "2" -> 4
            s.contains("N1") || s == "1" -> 5
            else -> 999
        }
    }

    private fun makeRecMap(type: String, payload: Map<String, Any>): Map<String, Any> {
        val meta = mutableMapOf<String, Any>()
        (payload["ids"] as? List<*>)?.let { ids ->
            meta["count"] = ids.size
            meta["ids_str"] = ids.take(5).joinToString(", ")
        }
        payload["id"]?.let { meta["id"] = it }
        payload["startId"]?.let { meta["startId"] = it }
        payload["endId"]?.let   { meta["endId"]   = it }
        payload["name"]?.let    { meta["name"]    = it }
        return mapOf("type" to type, "payload" to payload, "meta" to meta)
    }

    override fun onCleared() {
        super.onCleared()
        dailyListener?.remove()
    }
}