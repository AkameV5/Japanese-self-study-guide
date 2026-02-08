package com.example.japanese_self_study_guide.kanji;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;

public class KanjiExerciseFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanji_exercise_finish);

        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 0);
        int learned = getIntent().getIntExtra("learned", 0);
        int totalKanji = getIntent().getIntExtra("totalKanji", 0);

        TextView resultText = findViewById(R.id.resultText);
        TextView learnedText = findViewById(R.id.learnedText);

        float percent = (correct * 100f) / total;
        String resultMessage = String.format(
                "Правильных ответов: %d из %d\nРезультат: %.0f%%",
                correct, total, percent
        );

        resultText.setText(resultMessage);

        String learnedMessage = String.format(
                "Изучено кандзи: %d из %d",
                learned, totalKanji
        );
        learnedText.setText(learnedMessage);

        Button btnBackToList = findViewById(R.id.btnBackToList);
        btnBackToList.setOnClickListener(v -> {
            Intent intent = new Intent(
                    KanjiExerciseFinishActivity.this,
                    KanjiActivity.class
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}