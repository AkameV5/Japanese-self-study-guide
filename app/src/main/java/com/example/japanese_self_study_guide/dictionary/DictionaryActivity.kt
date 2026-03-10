package com.example.japanese_self_study_guide.dictionary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.text.Collator;
import java.util.Locale;


public class DictionaryActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private DictionaryAdapter adapter;
    private TextView textWordCount;
    private Spinner spinnerSort;

    private List<Word> allWords = new ArrayList<>();
    private List<Word> filteredWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        searchView = findViewById(R.id.searchViewDictionary);
        recyclerView = findViewById(R.id.recyclerViewDictionary);
        textWordCount = findViewById(R.id.textWordCount);
        spinnerSort = findViewById(R.id.spinnerSortDictionary);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DictionaryAdapter(filteredWords);
        recyclerView.setAdapter(adapter);

        setupSpinner();
        loadWords();
        setupSearch();


    }

    private void setupSpinner() {
        String[] options = {"По алфавиту", "По категории", "По длине слова"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortWords(options[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int wordGroup(Word w) {
        String s = safe(w.getWord());
        if (s.isEmpty()) return 2;

        char c = s.charAt(0);

        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        if (block == Character.UnicodeBlock.HIRAGANA
                || block == Character.UnicodeBlock.KATAKANA
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
            return 0;
        }
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            return 1;
        }
        return 2;
    }

    private String sortKey(Word w) {
        String key = safe(w.getReading());
        if (key.isEmpty()) {
            key = safe(w.getWord());
        }
        return key;
    }

    private void loadWords() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference wordsRef = db.collection("Words");

        wordsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allWords.clear();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Word w = doc.toObject(Word.class);
                        allWords.add(w);
                    }
                    filteredWords.clear();
                    filteredWords.addAll(allWords);
                    sortWords("По алфавиту");
                    adapter.notifyDataSetChanged();
                    updateCount();
                } else {
                    Log.e("Firestore", "Ошибка при загрузке слов", task.getException());
                }
            }
        });
    }


    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterWords(newText);
                return true;
            }
        });
    }

    private void filterWords(String query) {
        filteredWords.clear();
        String lower = query.toLowerCase();
        for (Word w : allWords) {
            if (w.getWord().contains(lower)
                    || w.getReading().contains(lower)
                    || w.getTranslation().toLowerCase().contains(lower)
                    || w.getCategory().toLowerCase().contains(lower)) {
                filteredWords.add(w);
            }
        }
        adapter.notifyDataSetChanged();
        updateCount();
    }

    private void sortWords(String option) {

        Collator collator = Collator.getInstance(Locale.JAPANESE);
        collator.setStrength(Collator.PRIMARY);

        switch (option) {

            case "По алфавиту":
                Collections.sort(filteredWords, (w1, w2) -> {

                    int g1 = wordGroup(w1);
                    int g2 = wordGroup(w2);
                    if (g1 != g2) return Integer.compare(g1, g2);
                    return collator.compare(
                            sortKey(w1),
                            sortKey(w2)
                    );
                });
                break;


            case "По категории":
                Collections.sort(filteredWords, (w1, w2) -> {
                    int cat = collator.compare(
                            safe(w1.getCategory()),
                            safe(w2.getCategory())
                    );
                    if (cat != 0) return cat;

                    return collator.compare(
                            safe(w1.getReading()),
                            safe(w2.getReading())
                    );
                });
                break;

            case "По длине слова":
                Collections.sort(filteredWords,
                        Comparator.comparingInt(w -> safe(w.getWord()).length()));
                break;
        }

        adapter.notifyDataSetChanged();
    }


    private void updateCount() {
        textWordCount.setText("Показано: " + filteredWords.size() + " слов");
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

}

