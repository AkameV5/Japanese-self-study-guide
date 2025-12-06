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

        loadExercise();

        btnCheck.setOnClickListener(v -> {
            if (currentExercise == null) return;
            String user = etAnswer.getText().toString().trim();
            if (user.isEmpty()) {
                etAnswer.setError("Введите ответ");
                return;
            }
            boolean correct = user.equalsIgnoreCase(currentExercise.getCorrectAnswer().trim());
            tvExplanation.setVisibility(View.VISIBLE);
            tvExplanation.setText((correct ? "Правильно!\n\n" : "Неправильно.\n\n")
                    + "Правильный ответ: " + currentExercise.getCorrectAnswer() + "\n\n"
                    + "Пояснение: " + (currentExercise.getExplanation() == null ? "-" : currentExercise.getExplanation()));
            // Блокируем ввод
            etAnswer.setEnabled(false);
            btnCheck.setEnabled(false);

            // (опционально) — сохранение результата в Firestore в коллекцию Progress
            // saveProgress(correct);
        });
    }

    private void loadExercise() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // поиск по полю id (если у тебя numeric id stored)
        db.collection("Exercises_text")
                .whereEqualTo("id", exerciseId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        currentExercise = query.getDocuments().get(0).toObject(ExerciseModel.class);
                        tvQuestion.setText(currentExercise.getQuestion());
                        tvExplanation.setVisibility(View.GONE);
                    } else {
                        tvQuestion.setText("Упражнение не найдено.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ExerciseDetail", "Ошибка загрузки упражнения", e);
                    tvQuestion.setText("Ошибка загрузки.");
                });
    }

}
