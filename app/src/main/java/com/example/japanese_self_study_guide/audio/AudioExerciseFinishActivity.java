package com.example.japanese_self_study_guide.audio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;

public class AudioExerciseFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_exercise_finish);
        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 0);
        TextView resultText = findViewById(R.id.resultText);

        float percent = (correct * 100f) / total;
        String resultMessage = String.format(
                "Правильных ответов: %d из %d\nРезультат: %.0f%%",
                correct, total, percent
        );

        resultText.setText(resultMessage);

        Button btn = findViewById(R.id.finishBackBtn);
        btn.setOnClickListener(v -> finish());
    }
}