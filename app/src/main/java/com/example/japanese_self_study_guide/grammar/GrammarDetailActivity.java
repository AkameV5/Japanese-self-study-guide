package com.example.japanese_self_study_guide.grammar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.japanese_self_study_guide.R;

public class GrammarDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_detail);

        TextView textStructure = findViewById(R.id.textDetailStructure);
        TextView textExplanation = findViewById(R.id.textDetailExplanation);
        CardView btnExercises = findViewById(R.id.buttonExercises);
        TextView textExample = findViewById(R.id.textDetailExample);
        TextView textTranslation = findViewById(R.id.textDetailTranslation);

        String structure = getIntent().getStringExtra("structure");
        String explanation = getIntent().getStringExtra("explanation");
        String example = getIntent().getStringExtra("example");
        String translation = getIntent().getStringExtra("translation");


        textStructure.setText(structure);
        textExplanation.setText(explanation);
        textExample.setText(example);
        textTranslation.setText(translation);

        btnExercises.setOnClickListener(v -> {
            Intent intent = new Intent(this, GrammarExerciseActivity.class);
            intent.putExtra("id_grammar", getIntent().getIntExtra("id", -1));
            startActivity(intent);
        });
    }
}

