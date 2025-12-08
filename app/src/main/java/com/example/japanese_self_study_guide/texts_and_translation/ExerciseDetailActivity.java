package com.example.japanese_self_study_guide.texts_and_translation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.main_profile.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ExerciseDetailActivity extends AppCompatActivity {

    private ArrayList<ExerciseModel> allExercises;
    private ExerciseModel currentExercise;

    private int currentIndex;
    private int textId;

    private TextView tvQuestion, tvExplanation, tvCounter;
    private LinearLayout optionsContainer;
    private MaterialButton btnHint, btnNext;

    private List<String> shuffledOptions;
    private int shuffledCorrectIndex;

    private int total = 0;
    private int correct = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_exercise_detail);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvExplanation = findViewById(R.id.tvExplanation);
        tvCounter = findViewById(R.id.tvCounter);
        optionsContainer = findViewById(R.id.optionsContainer);

        btnHint = findViewById(R.id.btnHint);
        btnNext = findViewById(R.id.btnNext);

        allExercises = (ArrayList<ExerciseModel>) getIntent().getSerializableExtra("allExercises");
        currentIndex = getIntent().getIntExtra("currentIndex", 0);
        textId = getIntent().getIntExtra("textId", -1);

        if (allExercises == null || allExercises.isEmpty()) {
            tvQuestion.setText("Ошибка загрузки упражнений");
            return;
        }

        loadExerciseUI();
    }

    private void loadExerciseUI() {

        currentExercise = allExercises.get(currentIndex);
        tvCounter.setText((currentIndex + 1) + " / " + allExercises.size());

        tvExplanation.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        shuffledOptions = new ArrayList<>(currentExercise.getOptions());
        Collections.shuffle(shuffledOptions);

        shuffledCorrectIndex = shuffledOptions.indexOf(
                currentExercise.getOptions().get(currentExercise.getCorrectIndex())
        );

        // Вопрос
        tvQuestion.setText(currentExercise.getQuestion());

        // Варианты
        optionsContainer.removeAllViews();
        for (int i = 0; i < shuffledOptions.size(); i++) {

            MaterialButton btn = new MaterialButton(this);
            btn.setText(shuffledOptions.get(i));

            btn.setBackgroundTintList(getColorStateList(R.color.accentPink));
            btn.setTextColor(getColor(R.color.textDark));

            int index = i;

            btn.setOnClickListener(v -> checkAnswer(index));

            optionsContainer.addView(btn);
        }
        setupHint();
    }

    private void checkAnswer(int chosenIndex) {
        total++;
        boolean isCorrect = chosenIndex == shuffledCorrectIndex;
        if (isCorrect) correct++;

        tvExplanation.setVisibility(View.VISIBLE);
        tvExplanation.setText(
                (isCorrect ? "Правильно!\n\n" : "Неправильно!\n\n") +
                        "Правильный ответ: " + shuffledOptions.get(shuffledCorrectIndex)
        );
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            optionsContainer.getChildAt(i).setEnabled(false);
        }

        btnNext.setVisibility(View.VISIBLE);
        btnNext.setOnClickListener(v -> nextExercise());
    }

    private void nextExercise() {

        if (currentIndex + 1 < allExercises.size()) {
            currentIndex++;
            loadExerciseUI();
        } else {
            finishExercises();
        }
    }
    private void setupHint() {
        if (!isHintAvailable()) {
            btnHint.setEnabled(false);
            btnHint.setText("Подсказка использована");
        } else {
            btnHint.setEnabled(true);
            btnHint.setText("Подсказка");
        }

        btnHint.setOnClickListener(v -> {
            tvExplanation.setVisibility(View.VISIBLE);
            tvExplanation.setText("Подсказка:\n\n" + currentExercise.getHint());
            saveHintUsed();

            btnHint.setEnabled(false);
            btnHint.setText("Подсказка использована");
        });
    }


    private boolean isHintAvailable() {
        SharedPreferences prefs = getSharedPreferences("hints", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return !today.equals(prefs.getString("HINT_USED_" + textId, ""));
    }

    private void saveHintUsed() {
        SharedPreferences prefs = getSharedPreferences("hints", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("HINT_USED_" + textId, today).apply();
    }

    private void finishExercises() {

        float percent = (correct * 100f) / total;
        if (percent < 70f) {
            Toast.makeText(this,
                    "Результат ниже 70%. Попробуйте ещё раз.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            ProgressManager.incrementProgress(uid, "textsDone", 1);

            FirebaseFirestore.getInstance()
                    .collection("Progress")
                    .document(uid)
                    .update(
                            "textsLearned",
                            FieldValue.arrayUnion(textId)
                    );
        }
        Toast.makeText(
                ExerciseDetailActivity.this,
                "Упражнение завершено!",
                Toast.LENGTH_SHORT
        ).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent i = new Intent(this, ExerciseFinishedActivity.class);
            i.putExtra("textId", textId);
            startActivity(i);
            finish();
        }, 1500); // можешь поставить 2000 как в хирагане
    }


}
