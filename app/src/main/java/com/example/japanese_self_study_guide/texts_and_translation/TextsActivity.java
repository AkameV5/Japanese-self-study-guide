package com.example.japanese_self_study_guide.texts_and_translation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextsAdapter adapter;
    private SearchView searchView;
    private android.widget.Spinner spinnerJlpt;
    private List<TextModel> allTexts = new ArrayList<>();
    private List<TextModel> filteredTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texts);

        recyclerView = findViewById(R.id.recyclerViewTexts);
        searchView = findViewById(R.id.searchViewTexts);
        spinnerJlpt = findViewById(R.id.spinnerJlptTexts);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TextsAdapter(filteredTexts, this::openTextDetail);
        recyclerView.setAdapter(adapter);

        loadTexts();
        setupSearch();
        setupJlptFilter();

        boolean dailyMode = getIntent().getBooleanExtra("daily_mode", false);
        int dailyId = getIntent().getIntExtra("daily_text_id", -1);

        if (dailyMode && dailyId != -1) {
            openTextByDailyId(dailyId);
            return;
        }


    }

    private void loadTexts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Texts").get()
                .addOnSuccessListener(query -> {
                    allTexts.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        TextModel text = doc.toObject(TextModel.class);
                        allTexts.add(text);
                    }
                    filteredTexts.clear();
                    filteredTexts.addAll(allTexts);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("TextsActivity", "Ошибка загрузки текстов", e));
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void setupJlptFilter() {
        String[] levels = {"Все уровни", "N1", "N2", "N3", "N4", "N5"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levels);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJlpt.setAdapter(adapterSpinner);

        spinnerJlpt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
                filter(searchView.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filter(String query) {
        String selectedLevel = spinnerJlpt.getSelectedItem().toString();
        filteredTexts.clear();

        for (TextModel t : allTexts) {
            boolean matchQuery = t.getTitle().toLowerCase().contains(query.toLowerCase());
            boolean matchLevel = selectedLevel.equals("Все уровни")
                    || ("N" + t.getDifficultyLevel()).equals(selectedLevel);

            if (matchQuery && matchLevel) {
                filteredTexts.add(t);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openTextDetail(TextModel text) {
        Intent intent = new Intent(this, TextDetailActivity.class);
        intent.putExtra("textId", text.getId());
        startActivity(intent);
    }
    private void openTextByDailyId(int id) {
        for (TextModel t : allTexts) {
            if (t.getId() == id) {
                Intent intent = new Intent(this, TextDetailActivity.class);
                intent.putExtra("textId", id);
                startActivity(intent);
                finish();
                return;
            }
        }
    }



}
