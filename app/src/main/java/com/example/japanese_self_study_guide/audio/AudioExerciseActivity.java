package com.example.japanese_self_study_guide.audio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;
import com.google.android.material.button.MaterialButton;
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

        // создаём radio buttons
        for (String opt : options) {
            RadioButton rb = new RadioButton(this);
            rb.setText(opt);
            rb.setTextSize(16f);
            rb.setPadding(8, 16, 8, 16);
            rb.setTextColor(getResources().getColor(R.color.textDark));
            rb.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.textDark)));
            optionsContainer.addView(rb);
        }

        nextBtn.setText("Ответить");
        nextBtn.setOnClickListener(v -> checkAnswer(m));

        hintBtn.setOnClickListener(v -> showHint(m));
    }

    private void checkAnswer(AudioExerciseModel m) {
        String selected = null;

        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            RadioButton rb = (RadioButton) optionsContainer.getChildAt(i);
            if (rb.isChecked()) {
                selected = rb.getText().toString();
                break;
            }
        }

        if (selected == null) return;

        String correct = m.getOptions().get(m.getCorrectIndex());

        if (selected.equals(correct)) {
            resultText.setText("✔ Правильно!");
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
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
        Intent i = new Intent(this, AudioExerciseFinishActivity.class);
        startActivity(i);
        finish();
    }
}
