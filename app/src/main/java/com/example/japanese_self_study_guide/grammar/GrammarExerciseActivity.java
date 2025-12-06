package com.example.japanese_self_study_guide.grammar;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GrammarExerciseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GrammarExerciseAdapter adapter;
    private List<GrammarExercise> exercises = new ArrayList<>();
    private int grammarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_exercise);

        grammarId = getIntent().getIntExtra("id_grammar", -1);

        recyclerView = findViewById(R.id.recyclerExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GrammarExerciseAdapter(exercises, exercise -> openExercise(exercise));
        recyclerView.setAdapter(adapter);

        loadExercises();
    }

    private void loadExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("GrammarExercises")
                .whereEqualTo("id_grammar", grammarId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        exercises.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            GrammarExercise ex = doc.toObject(GrammarExercise.class);
                            exercises.add(ex);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void openExercise(GrammarExercise ex) {
        Intent intent = new Intent(this, GrammarExerciseDetailActivity.class);
        intent.putExtra("task", ex.getTask());
        intent.putExtra("rightAnswer", ex.getRightAnswer());
        intent.putExtra("explanation", ex.getExplanation());
        intent.putExtra("difficulty", ex.getDifficulty());
        startActivity(intent);
    }

}

