package com.example.japanese_self_study_guide.kanji;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.japanese_self_study_guide.R;
import com.google.android.material.button.MaterialButton;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class KanjiActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private KanjiAdapter adapter;
    private SearchView searchView;
    private Spinner spinnerSort, spinnerCategory, spinnerJlpt;
    private MaterialButton buttonRandom;
    private TextView textKanjiCount;

    private List<KanjiModel> kanjiList = new ArrayList<>();
    private List<KanjiModel> currentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanji);

        recyclerView = findViewById(R.id.recyclerViewKanji);
        searchView = findViewById(R.id.searchViewKanji);
        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerJlpt = findViewById(R.id.spinnerJlpt);
        buttonRandom = findViewById(R.id.buttonRandom);
        textKanjiCount = findViewById(R.id.textKanjiCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KanjiAdapter(currentList);
        adapter.setOnItemClickListener(this::showKanjiDialog);
        recyclerView.setAdapter(adapter);


        setupSortSpinner();
        setupJlptSpinner();
        loadKanjiFromFirestore();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard();
                searchView.clearFocus();
                filterInActivity(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    hideKeyboard();
                    searchView.clearFocus();
                    currentList.clear();
                    currentList.addAll(kanjiList);
                    adapter.updateList(currentList);
                    updateKanjiCount();
                } else {
                    filterInActivity(newText);
                }
                return true;
            }
        });


        buttonRandom.setOnClickListener(v -> showRandomKanji());

        MaterialButton buttonExercise = findViewById(R.id.buttonExercise);

        buttonExercise.setOnClickListener(v -> {
            startActivity(new Intent(KanjiActivity.this, KanjiExerciseGroupsActivity.class));
        });

    }

    private void setupSortSpinner() {
        String[] options = {"Сортировать по...", "Категория"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        setupCategorySpinner();
                        spinnerCategory.setVisibility(View.VISIBLE);
                        break;
                    default: // По ID (по умолчанию)
                        spinnerCategory.setVisibility(View.GONE);
                        sortById();
                        break;
                }
                updateKanjiCount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        for (KanjiModel k : kanjiList) {
            if (k.getCategory() != null && !categories.contains(k.getCategory())) {
                categories.add(k.getCategory());
            }
        }
        Collections.sort(categories);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                applyCombinedFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadKanjiFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Kanji").get()
                .addOnSuccessListener(query -> {
                    kanjiList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        KanjiModel kanji = doc.toObject(KanjiModel.class);
                        kanjiList.add(kanji);
                    }
                    // По умолчанию сортируем по id и показываем всё
                    sortById();
                    Log.d("KanjiActivity", "✅ Загрузили " + kanjiList.size() + " кандзи");
                    updateKanjiCount();
                })
                .addOnFailureListener(e -> {
                    Log.e("KanjiActivity", "❌ Ошибка загрузки", e);
                    updateKanjiCount();
                });
    }

    private void sortById() {
        Collections.sort(kanjiList, Comparator.comparingDouble(KanjiModel::getId));
        currentList.clear();
        currentList.addAll(kanjiList);
        adapter.updateList(currentList);
        updateKanjiCount();
    }

    private void setupJlptSpinner() {
        String[] levels = {"Все уровни", "N1", "N2", "N3", "N4", "N5"};
        ArrayAdapter<String> jlptAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levels);
        jlptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJlpt.setAdapter(jlptAdapter);

        spinnerJlpt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                applyCombinedFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyCombinedFilters() {
        String selectedCategory = null;
        if (spinnerCategory.getVisibility() == View.VISIBLE && spinnerCategory.getSelectedItem() != null) {
            selectedCategory = spinnerCategory.getSelectedItem().toString();
        }

        int jlptFilter = 0; // 0 = все уровни
        int pos = spinnerJlpt.getSelectedItemPosition();
        if (pos > 0) {
            jlptFilter = pos; // pos=1 → N1 → jlpt=1, pos=5 → N5 → jlpt=5
        }

        List<KanjiModel> filtered = new ArrayList<>();
        for (KanjiModel k : kanjiList) {
            boolean categoryMatch = (selectedCategory == null || selectedCategory.equals(k.getCategory()));
            boolean jlptMatch = (jlptFilter == 0 || k.getJlpt() == jlptFilter);

            if (categoryMatch && jlptMatch) {
                filtered.add(k);
            }
        }

        currentList.clear();
        currentList.addAll(filtered);
        adapter.updateList(currentList);
        updateKanjiCount();
    }

    private void filterInActivity(String text) {
        List<KanjiModel> filtered = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            // пустой запрос — показываем либо весь список, либо применённую сортировку/категорию
            // Чтобы сохранить текущий режим сортировки: если spinnerCategory виден и выбран — оставляем текущList
            if (spinnerCategory.getVisibility() == View.VISIBLE && spinnerCategory.getSelectedItem() != null) {
                // category spinner уже обновляет currentList сам, поэтому просто показываем currentList как есть
                // (ничего не делаем — currentList уже держит выбранную категорию)
                // Но если нужно — можно реконструировать фильтр по категории
            } else {
                // показываем весь kanjiList (с учётом текущей сортировки)
                currentList.clear();
                currentList.addAll(kanjiList);
                adapter.updateList(currentList);
            }
        } else {
            String query = text.toLowerCase().trim();
            for (KanjiModel k : kanjiList) {
                boolean matches = false;
                if (k.getKanji() != null && k.getKanji().toLowerCase().contains(query)) {
                    matches = true;
                }
                if (!matches && k.getMeaning() != null && k.getMeaning().toLowerCase().contains(query)) {
                    matches = true;
                }
                if (!matches && k.getCategory() != null && k.getCategory().toLowerCase().contains(query)) {
                    matches = true;
                }
                if (!matches) {
                    if (k.getOnYomi() != null) {
                        for (String s : k.getOnYomi()) {
                            if (s != null && s.toLowerCase().contains(query)) {
                                matches = true;
                                break;
                            }
                        }
                    }
                }
                if (!matches) {
                    if (k.getKunYomi() != null) {
                        for (String s : k.getKunYomi()) {
                            if (s != null && s.toLowerCase().contains(query)) {
                                matches = true;
                                break;
                            }
                        }
                    }
                }

                if (matches) filtered.add(k);
            }
            currentList.clear();
            currentList.addAll(filtered);
            adapter.updateList(currentList);
        }

        updateKanjiCount();
    }

    private void updateKanjiCount() {
        // Можно показать "Найдено: X / Y"
        String text = "Показано: " + currentList.size() + " / " + kanjiList.size() + " кандзи";
        textKanjiCount.setText(text);
    }

    private void showRandomKanji() {
        if (currentList.isEmpty()) return;

        Random random = new Random();
        KanjiModel randomKanji = currentList.get(random.nextInt(currentList.size()));

        String message = "Значение: " + safeString(randomKanji.getMeaning()) + "\n\n" +
                "Онъёми: " + String.join(", ", safeList(randomKanji.getOnYomi())) + "\n" +
                "Кунъёми: " + String.join(", ", safeList(randomKanji.getKunYomi())) + "\n\n" +
                "JLPT: N" + randomKanji.getJlpt() + "\n" +
                "Категория: " + safeString(randomKanji.getCategory());

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        TextView title = new TextView(this);
        title.setText(randomKanji.getKanji());
        title.setTextSize(48);
        title.setPadding(20, 40, 20, 20);
        title.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        title.setTextColor(0xFF000000);

        dialog.setCustomTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("Ок", null);
        dialog.show();
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View v = this.getCurrentFocus();
            if (v == null) v = new View(this);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    private List<String> safeList(List<String> list) {
        if (list == null) return new ArrayList<>();
        return list;
    }

    private void showKanjiDialog(KanjiModel kanji) {

        String message = "Значение: " + safeString(kanji.getMeaning()) + "\n\n" +
                "Онъёми: " + String.join(", ", safeList(kanji.getOnYomi())) + "\n" +
                "Кунъёми: " + String.join(", ", safeList(kanji.getKunYomi())) + "\n\n" +
                "JLPT: N" + kanji.getJlpt() + "\n" +
                "Категория: " + safeString(kanji.getCategory());

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        TextView title = new TextView(this);
        title.setText(kanji.getKanji());
        title.setTextSize(56);        // 💥 ещё больше чем рандом-кнопка
        title.setPadding(20, 40, 20, 20);
        title.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        title.setTextColor(0xFF000000);

        dialog.setCustomTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("Ок", null);
        dialog.show();
    }

}
