package com.example.japanese_self_study_guide.kanji

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.japanese_self_study_guide.kanji.KanjiRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class KanjiSortOption { BY_ID, BY_CATEGORY }

data class KanjiUiState(
    val isLoading: Boolean = true,
    val allKanji: List<KanjiModel> = emptyList(),
    val displayedKanji: List<KanjiModel> = emptyList(),
    val learnedIds: Set<Int> = emptySet(),
    val query: String = "",
    val jlptFilter: Int = 0,
    val sortOption: KanjiSortOption = KanjiSortOption.BY_ID,
    val selectedCategory: String? = null,
    val availableCategories: List<String> = emptyList(),
    val error: String? = null
)

class KanjiViewModel(
    private val repository: KanjiRepository = KanjiRepository()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(KanjiUiState())
    val uiState: StateFlow<KanjiUiState> = _uiState.asStateFlow()
    fun initialize(dailyMode: Boolean, dailyIds: IntArray?) {
        if (dailyMode && dailyIds != null && dailyIds.isNotEmpty()) {
            loadDailyKanji(dailyIds)
        } else {
            loadAll()
        }
    }

    private fun loadAll() {
        repository.getAllKanji()
            .addOnSuccessListener { items ->
                val list = items.sortedBy { model -> model.id }

                val categories = list
                    .mapNotNull { model -> model.category?.takeIf { it.isNotBlank() } }
                    .distinct()
                    .sorted()

                Log.d("KanjiVM", "Loaded ${list.size} kanji, ${categories.size} categories")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allKanji = list,
                    displayedKanji = list,
                    availableCategories = categories
                )
                loadLearnedIds()
            }
            .addOnFailureListener { e ->
                Log.e("KanjiVM", "Load error: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
    }

    private fun loadLearnedIds() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        db.collection("Progress").document(uid).get()
            .addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                val learned = (doc.get("kanjiLearned") as? List<Long>)
                    ?.map { l -> l.toInt() }?.toSet() ?: emptySet()
                _uiState.value = _uiState.value.copy(learnedIds = learned)
            }
    }

    fun loadDailyKanji(ids: IntArray) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        repository.getKanjiByIds(ids.toList())
            .addOnSuccessListener { items ->
                val sorted = items.sortedBy { model -> model.id }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allKanji = sorted,
                    displayedKanji = sorted
                )
                loadLearnedIds()
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
    }

    fun onQueryChange(q: String) {
        val s = _uiState.value
        _uiState.value = s.copy(
            query = q,
            displayedKanji = applyAll(s.allKanji, q, s.jlptFilter, s.selectedCategory, s.sortOption)
        )
    }

    fun onJlptFilter(level: Int) {
        val s = _uiState.value
        _uiState.value = s.copy(
            jlptFilter = level,
            displayedKanji = applyAll(s.allKanji, s.query, level, s.selectedCategory, s.sortOption)
        )
    }

    fun onSortChange(option: KanjiSortOption) {
        val s = _uiState.value
        val category = if (option == KanjiSortOption.BY_CATEGORY) s.selectedCategory else null
        _uiState.value = s.copy(
            sortOption = option,
            selectedCategory = category,
            displayedKanji = applyAll(s.allKanji, s.query, s.jlptFilter, category, option)
        )
    }

    fun onCategorySelect(category: String?) {
        val s = _uiState.value
        _uiState.value = s.copy(
            selectedCategory = category,
            displayedKanji = applyAll(s.allKanji, s.query, s.jlptFilter, category, s.sortOption)
        )
    }

    private fun applyAll(
        list: List<KanjiModel>,
        query: String,
        jlpt: Int,
        category: String?,
        sort: KanjiSortOption
    ): List<KanjiModel> {
        var r = list
        if (query.isNotBlank()) {
            val q = query.lowercase()
            r = r.filter { model ->
                (model.kanji?.contains(q) == true) ||
                        (model.meaning?.lowercase()?.contains(q) == true) ||
                        (model.category?.lowercase()?.contains(q) == true)
            }
        }
        if (jlpt > 0) r = r.filter { model -> model.jlpt == jlpt }
        if (category != null) r = r.filter { model -> model.category == category }
        r = when (sort) {
            KanjiSortOption.BY_ID -> r.sortedBy { model -> model.id }
            KanjiSortOption.BY_CATEGORY -> r.sortedWith(
                compareBy({ model -> model.category ?: "" }, { model -> model.id })
            )
        }
        return r
    }

    fun randomKanji(): KanjiModel? {
        val list = _uiState.value.displayedKanji
        return if (list.isNotEmpty()) list.random() else null
    }
}
