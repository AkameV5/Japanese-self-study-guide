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
                        // НЕ трогаем w.word, просто добавляем объект
                        allWords.add(w);
                    }
                    filteredWords.clear();
                    filteredWords.addAll(allWords);
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
        switch (option) {
            case "По алфавиту":
                Collections.sort(filteredWords, Comparator.comparing(Word::getWord));
                break;
            case "По категории":
                Collections.sort(filteredWords, Comparator.comparing(Word::getCategory));
                break;
            case "По длине слова":
                Collections.sort(filteredWords, Comparator.comparingInt(w -> w.getWord().length()));
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void updateCount() {
        textWordCount.setText("Показано: " + filteredWords.size() + " слов");
    }
}

