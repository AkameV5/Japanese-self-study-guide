package com.example.japanese_self_study_guide.grammar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.main_profile.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrammarExerciseActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<GrammarExercise> exercises = new ArrayList<>();

    private TextView tvTask, tvExplanation, tvProgress;
    private EditText etAnswer;
    private Button btnCheck, btnNext;

    private int index = 0;
    private GrammarExercise currentEx;

    private int total = 0;
    private int correct = 0;
    private int grammarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_exercise_run);

        db = FirebaseFirestore.getInstance();

        tvTask = findViewById(R.id.tvTask);
        tvExplanation = findViewById(R.id.tvExplanation);
        tvProgress = findViewById(R.id.tvProgress);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheck = findViewById(R.id.btnCheck);
        btnNext = findViewById(R.id.btnNext);

        grammarId = getIntent().getIntExtra("id_grammar", -1);

        btnCheck.setOnClickListener(v -> check());
        btnNext.setOnClickListener(v -> next());

        loadExercises();
    }

    private void loadExercises() {

        db.collection("GrammarExercises")
                .whereEqualTo("id_grammar", grammarId)
                .get()
                .addOnSuccessListener(query -> {

                    exercises.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        GrammarExercise ex = doc.toObject(GrammarExercise.class);
                        exercises.add(ex);
                    }

                    if (exercises.isEmpty()) {
                        Toast.makeText(this,
                                "Упражнений нет",
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    index = 0;
                    show();
                });
    }

    private void show() {
        if (index >= exercises.size()) {
            finishGrammar();
            return;
        }

        currentEx = exercises.get(index);

        tvProgress.setText((index + 1) + " / " + exercises.size());
        tvTask.setText(currentEx.getTask());
        tvExplanation.setText("");
        etAnswer.setText("");

        btnCheck.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }

    private void check() {
        total++;

        String user = etAnswer.getText().toString().trim();
        boolean isCorrect =
                user.equalsIgnoreCase(currentEx.getRightAnswer().trim());

        if (isCorrect) {
            correct++;
            tvExplanation.setText("✅ Правильно!\n\n" +
                    currentEx.getExplanation());
        } else {
            tvExplanation.setText("❌ Неверно\nПравильный ответ: " +
                    currentEx.getRightAnswer() +
                    "\n\n" + currentEx.getExplanation());
        }

        btnCheck.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void next() {
        index++;
        show();
    }

    private void finishGrammar() {

        float percent = (correct * 100f) / total;

        if (percent < 70f) {
            Toast.makeText(this,
                    "Пройдено менее чем на 70%",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Progress")
                .document(uid)
                .update(
                        "grammarLearned",
                        FieldValue.arrayUnion(grammarId),
                        "grammarDone",
                        FieldValue.increment(1)
                );
        MainActivity.removeDailyRecommendation("grammar", grammarId);

        Toast.makeText(this,
                "Грамматика изучена 🎉",
                Toast.LENGTH_LONG).show();

        finish();
    }

}
