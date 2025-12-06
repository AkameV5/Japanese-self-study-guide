package com.example.japanese_self_study_guide.texts_and_translation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.example.japanese_self_study_guide.R;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ExerciseDetailActivity extends AppCompatActivity {

    private TextView tvQuestion, tvExplanation;
    private EditText etAnswer;
    private MaterialButton btnCheck;
    private int exerciseId;
    private int textId;
    private ExerciseModel currentExercise;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvExplanation = findViewById(R.id.tvExplanation);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheck = findViewById(R.id.btnCheck);



        exerciseId = getIntent().getIntExtra("exerciseId", -1);
        textId = getIntent().getIntExtra("textId", -1);
        Log.d("CHECK", "exerciseId = " + exerciseId + " | textId = " + textId);
        loadExercise();

        btnCheck.setOnClickListener(v -> {
            if (currentExercise == null) return;
            String user = etAnswer.getText().toString().trim();
            if (user.isEmpty()) {
                etAnswer.setError("Введите ответ");
                return;
            }
            String correctAnswer = currentExercise.getOptions().get(currentExercise.getCorrectIndex());

            boolean correct = user.equalsIgnoreCase(correctAnswer.trim());

            tvExplanation.setVisibility(View.VISIBLE);
            tvExplanation.setText(
                    (correct ? "Правильно!\n\n" : "Неправильно.\n\n") +
                            "Правильный ответ: " + correctAnswer + "\n\n" +
                            "Подсказка: " + (currentExercise.getHint() == null ? "-" : currentExercise.getHint())
            );
            etAnswer.setEnabled(false);
            btnCheck.setEnabled(false);

            // (опционально) — сохранение результата в Firestore в коллекцию Progress
            // saveProgress(correct);
        });
    }

    private void loadExercise() {
        Log.d("CHECK", "Loading exercise with id=" + exerciseId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // поиск по полю id (если у тебя numeric id stored)
        db.collection("TextsExercises")
                .whereEqualTo("id", exerciseId)
                .get()
                .addOnSuccessListener(query -> {
                    Log.d("CHECK", "Firestore returned: " + query.size());
                    if (!query.isEmpty()) {
                        Log.d("CHECK", "Document data = " + query.getDocuments().get(0).getData());
                        currentExercise = query.getDocuments().get(0).toObject(ExerciseModel.class);
                        tvQuestion.setText(currentExercise.getQuestion());
                        tvExplanation.setVisibility(View.GONE);
                    } else {
                        tvQuestion.setText("Упражнение не найдено.");
                    }
                })
                .addOnFailureListener(e -> Log.e("CHECK", "Firestore ERROR", e));
    }

}
