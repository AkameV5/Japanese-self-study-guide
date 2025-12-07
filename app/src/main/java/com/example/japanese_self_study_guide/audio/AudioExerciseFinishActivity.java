package com.example.japanese_self_study_guide.audio;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;

public class AudioExerciseFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_exercise_finish);

        Button btn = findViewById(R.id.finishBackBtn);
        btn.setOnClickListener(v -> finish());
    }
}
