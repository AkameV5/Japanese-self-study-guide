package com.example.japanese_self_study_guide.audio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AudioExerciseActivity extends AppCompatActivity {

    private int audioId;
    private ArrayList<AudioExerciseModel> list = new ArrayList<>();
    private FirebaseFirestore db;

    private int index = 0;

    private TextView counterText, questionText, resultText;
    private LinearLayout optionsContainer;
    private MaterialButton hintBtn, nextBtn;

    private SharedPreferences prefs;
    private String hintKey;

    private int total = 0;
    private int correct = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_exercise);

        audioId = getIntent().getIntExtra("audioId", -1);

        db = FirebaseFirestore.getInstance();

        counterText = findViewById(R.id.audioCounter);
        questionText = findViewById(R.id.audioQuestionText);
        resultText = findViewById(R.id.audioResultText);

        optionsContainer = findViewById(R.id.optionsContainer);
        hintBtn = findViewById(R.id.audioHintBtn);
        nextBtn = findViewById(R.id.nextBtn);

        prefs = getSharedPreferences("hintPrefs", MODE_PRIVATE);
        hintKey = "hint_used_audio_" + audioId;

        boolean dailyMode = getIntent().getBooleanExtra("daily_mode", false);
        int dailyAudioId = getIntent().getIntExtra("daily_audio_id", -1);

        if (dailyMode && dailyAudioId != -1) {
            audioId = dailyAudioId;
        }

        checkHintAvailability();

        loadExercises();
    }

    private void checkHintAvailability() {
        long lastUse = prefs.getLong(hintKey, 0);
        long oneDay = 24 * 60 * 60 * 1000L;

        if (System.currentTimeMillis() - lastUse < oneDay) {
            hintBtn.setEnabled(false); // подсказку можно нажать только раз в день
        }
    }

    private void loadExercises() {
        db.collection("AudioExercises")
                .whereEqualTo("audioId", audioId)
                .orderBy("id")
                .get()
                .addOnSuccessListener(q -> {
                    for (var d : q.getDocuments()) {
                        list.add(d.toObject(AudioExerciseModel.class));
                    }

                    Collections.shuffle(list); // перемешиваем задания

                    showExercise();
                });
    }

    private void showExercise() {
        AudioExerciseModel m = list.get(index);

        counterText.setText((index + 1) + "/" + list.size());
        questionText.setText(m.getQuestion());

        resultText.setVisibility(TextView.GONE);
        optionsContainer.removeAllViews();

        List<String> options = new ArrayList<>(m.getOptions());
        Collections.shuffle(options); // перемешиваем варианты

        optionsContainer.removeAllViews();

        for (String opt : options) {
            MaterialButton btn = new MaterialButton(this, null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);

            btn.setText(opt);
            btn.setTextSize(16f);
            btn.setAllCaps(false);
            btn.setPadding(8, 16, 8, 16);

            btn.setStrokeWidth(2);
            btn.setStrokeColor(ColorStateList.valueOf(
                    getResources().getColor(R.color.textDark)
            ));
            btn.setTextColor(getResources().getColor(R.color.textDark));

            optionsContainer.addView(btn);

            btn.setOnClickListener(v -> checkAnswer(opt, m, btn));
        }
    }

    private void checkAnswer(String selected, AudioExerciseModel m, MaterialButton selectedBtn) {

        total++;

        String correctAnswer = m.getOptions().get(m.getCorrectIndex());

        // отключаем все кнопки
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            optionsContainer.getChildAt(i).setEnabled(false);
        }

        if (selected.equals(correctAnswer)) {
            correct++;

            selectedBtn.setStrokeColor(ColorStateList.valueOf(
                    getResources().getColor(android.R.color.holo_green_dark)
            ));

            resultText.setText("✔ Правильно!");
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        } else {
            selectedBtn.setStrokeColor(ColorStateList.valueOf(
                    getResources().getColor(android.R.color.holo_red_dark)
            ));

            // подсветить правильный вариант
            for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                MaterialButton b = (MaterialButton) optionsContainer.getChildAt(i);
                if (b.getText().toString().equals(correctAnswer)) {
                    b.setStrokeColor(ColorStateList.valueOf(
                            getResources().getColor(android.R.color.holo_green_dark)
                    ));
                }
            }

            resultText.setText("✖ Неправильно!");
            resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        resultText.setVisibility(TextView.VISIBLE);

        nextBtn.setText(index == list.size() - 1 ? "Завершить" : "Следующее");
        nextBtn.setOnClickListener(v -> next());
    }


    private void showHint(AudioExerciseModel m) {
        resultText.setText(m.getHint());
        resultText.setTextColor(getResources().getColor(R.color.textDark));
        resultText.setVisibility(TextView.VISIBLE);

        hintBtn.setEnabled(false);

        prefs.edit().putLong(hintKey, System.currentTimeMillis()).apply();
    }

    private void next() {
        if (index < list.size() - 1) {
            index++;
            showExercise();
        } else {
            finishExercises();
        }
    }

    private void finishExercises() {

        float percent = (correct * 100f) / total;

        // если меньше 70% — не засчитываем
        if (percent < 70f) {
            Toast.makeText(
                    this,
                    "Результат ниже 70%. Попробуйте ещё раз.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Увеличиваем общее количество выполненных
            db.collection("Progress")
                    .document(uid)
                    .update(
                            "audioDone",
                            FieldValue.increment(1),
                            "audioLearned",
                            FieldValue.arrayUnion(audioId)
                    );
        }

        // показываем уведомление
        Toast.makeText(
                this,
                "Упражнение завершено!",
                Toast.LENGTH_SHORT
        ).show();

        // задержка перед переходом
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent i = new Intent(this, AudioExerciseFinishActivity.class);
            startActivity(i);
            finish();
        }, 1500);
    }

}
