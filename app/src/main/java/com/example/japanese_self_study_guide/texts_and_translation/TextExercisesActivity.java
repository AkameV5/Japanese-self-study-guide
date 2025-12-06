package com.example.japanese_self_study_guide.texts_and_translation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TextExercisesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExercisesAdapter adapter;
    private List<ExerciseModel> exercises = new ArrayList<>();
    private int textId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_exercises);

        textId = getIntent().getIntExtra("textId", -1);

        recyclerView = findViewById(R.id.recyclerTextExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExercisesAdapter(exercises);
        recyclerView.setAdapter(adapter);

        loadExercises();
    }

    private void loadExercises() {
        FirebaseFirestore.getInstance()
                .collection("TextsExercises")
                .whereEqualTo("textId", textId)
                .get()
                .addOnSuccessListener(query -> {
                    exercises.clear();
                    for (var doc : query) {
                        ExerciseModel e = doc.toObject(ExerciseModel.class);
                        exercises.add(e);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}

