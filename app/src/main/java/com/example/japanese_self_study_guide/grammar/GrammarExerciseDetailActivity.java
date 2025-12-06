package com.example.japanese_self_study_guide.grammar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.japanese_self_study_guide.R;

public class GrammarExerciseDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_exercise_detail);

        TextView textTask = findViewById(R.id.textTask);
        EditText editAnswer = findViewById(R.id.editAnswer);
        Button btnCheck = findViewById(R.id.btnCheck);
        TextView textResult = findViewById(R.id.textResult);

        String task = getIntent().getStringExtra("task");
        String rightAnswer = getIntent().getStringExtra("rightAnswer");
        String explanation = getIntent().getStringExtra("explanation");

        textTask.setText(task);

        btnCheck.setOnClickListener(v -> {
            String userAnswer = editAnswer.getText().toString().trim();

            if (userAnswer.equals(rightAnswer)) {
                textResult.setText("✔ Правильно!\n\n" + explanation);
            } else {
                textResult.setText("❌ Неправильно.\n\nПравильный ответ: " +
                        rightAnswer + "\n\n" + explanation);
            }
        });
    }
}
