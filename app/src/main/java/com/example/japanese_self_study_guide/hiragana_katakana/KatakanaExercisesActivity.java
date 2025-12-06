package com.example.japanese_self_study_guide.hiragana_katakana;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KatakanaExercisesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<KatakanaExerciseModel> exercises = new ArrayList<>();

    private TextView tvQuestion, tvExplanation, tvProgress;
    private LinearLayout layoutOptions;
    private EditText etAnswer;
    private Button btnCheck, btnNext;

    private int index = 0;
    private KatakanaExerciseModel currentEx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hiragana_exercise);

        db = FirebaseFirestore.getInstance();

        tvQuestion = findViewById(R.id.tvQuestion);
        tvExplanation = findViewById(R.id.tvExplanation);
        tvProgress = findViewById(R.id.tvProgress);

        layoutOptions = findViewById(R.id.layoutOptions);
        etAnswer = findViewById(R.id.etAnswer);

        btnCheck = findViewById(R.id.btnCheck);
        btnNext = findViewById(R.id.btnNext);

        btnCheck.setOnClickListener(v -> check());
        btnNext.setOnClickListener(v -> next());

        boolean allRandom = getIntent().getBooleanExtra("all_random_mode", false);
        if (allRandom) {
            loadAllRandom();
            return;
        }


        int[] groupIds = getIntent().getIntArrayExtra("group_ids");
        if (groupIds == null) {
            Toast.makeText(this, "Ошибка: группа не выбрана", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadGroup(groupIds);
    }

    private void loadGroup(int[] idsArr) {
        List<Integer> ids = new ArrayList<>();
        for (int i : idsArr) ids.add(i);

        db.collection("KatakanaExercises")
                .whereIn("katakanaId", ids)
                .get()
                .addOnSuccessListener(query -> {
                    exercises.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        KatakanaExerciseModel ex = doc.toObject(KatakanaExerciseModel.class);
                        if (ex != null) exercises.add(ex);
                    }

                    Collections.shuffle(exercises);
                    index = 0;
                    show();
                });
    }

    private void show() {
        if (index >= exercises.size()) {
            tvQuestion.setText("Все упражнения завершены!");
            layoutOptions.removeAllViews();
            etAnswer.setVisibility(View.GONE);
            btnCheck.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
            return;
        }

        currentEx = exercises.get(index);

        tvQuestion.setText(currentEx.getQuestion());
        tvExplanation.setText("");
        tvProgress.setText((index + 1) + " / " + exercises.size());

        layoutOptions.removeAllViews();
        etAnswer.setText("");

        switch (currentEx.getType()) {
            case "choose": showChoose(); break;
            default: showWrite(); break;
        }
    }

    private void showChoose() {
        layoutOptions.setVisibility(View.VISIBLE);
        etAnswer.setVisibility(View.VISIBLE);

        for (String opt : currentEx.getOptions()) {
            Button b = new Button(this);
            b.setText(opt);
            b.setOnClickListener(v -> etAnswer.setText(opt));
            layoutOptions.addView(b);
        }
    }

    private void showWrite() {
        layoutOptions.setVisibility(View.GONE);
        etAnswer.setVisibility(View.VISIBLE);
    }

    private void check() {
        String user = etAnswer.getText().toString().trim();
        String correct = currentEx.getCorrectAnswer().trim();

        if (user.equals(correct)) {
            tvExplanation.setText("Правильно! 🎉\n" + currentEx.getExplanation());
        } else {
            tvExplanation.setText("Неверно ❌\nПравильный ответ: " + correct +
                    "\n\n" + currentEx.getExplanation());
        }

        btnNext.setVisibility(View.VISIBLE);
        btnCheck.setVisibility(View.GONE);
    }

    private void next() {
        index++;
        btnCheck.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
        show();
    }

    private void loadAllRandom() {
        db.collection("KatakanaExercises")
                .get()
                .addOnSuccessListener(query -> {

                    exercises.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        KatakanaExerciseModel ex = doc.toObject(KatakanaExerciseModel.class);
                        if (ex != null) exercises.add(ex);
                    }

                    Collections.shuffle(exercises);

                    if (exercises.size() > 50)
                        exercises = exercises.subList(0, 50);
                    index = 0;
                    show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                );
    }

}
