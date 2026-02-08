package com.example.japanese_self_study_guide.texts_and_translation;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.japanese_self_study_guide.R;
import com.google.android.material.button.MaterialButton;

public class ExerciseFinishedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_exercise_finished);

        int textId = getIntent().getIntExtra("textId", -1);
        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 0);

        TextView resultText = findViewById(R.id.resultText);

        float percent = total > 0 ? (correct * 100f) / total : 0;
        String resultMessage = String.format(
                "Правильных ответов: %d из %d\nРезультат: %.0f%%",
                correct, total, percent
        );

        resultText.setText(resultMessage);

        MaterialButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(this, TextDetailActivity.class);
            i.putExtra("textId", textId);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }
}