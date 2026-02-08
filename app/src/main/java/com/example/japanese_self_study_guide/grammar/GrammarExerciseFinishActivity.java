package com.example.japanese_self_study_guide.grammar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;

public class GrammarExerciseFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_exercise_finish);

        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 0);

        TextView resultText = findViewById(R.id.resultText);

        float percent = (correct * 100f) / total;
        String resultMessage = String.format(
                "Правильных ответов: %d из %d\nРезультат: %.0f%%",
                correct, total, percent
        );

        resultText.setText(resultMessage);

        Button btnBackToList = findViewById(R.id.btnBackToList);
        btnBackToList.setOnClickListener(v -> {
            Intent intent = new Intent(
                    GrammarExerciseFinishActivity.this,
                    GrammarActivity.class
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}