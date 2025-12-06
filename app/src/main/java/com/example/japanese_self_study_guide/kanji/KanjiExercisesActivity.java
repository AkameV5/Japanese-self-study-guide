package com.example.japanese_self_study_guide.kanji;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.japanese_self_study_guide.R;
import com.google.firebase.firestore.*;
import java.util.*;

public class KanjiExercisesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<KanjiExerciseModel> exercises = new ArrayList<>();

    private TextView tvQuestion, tvExplanation, tvProgress;
    private LinearLayout layoutOptions;
    private EditText etAnswer;
    private Button btnCheck, btnNext;

    private int index = 0;
    private KanjiExerciseModel currentEx;
    private int startId, endId, limit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanji_exercises);

        db = FirebaseFirestore.getInstance();

        tvQuestion = findViewById(R.id.tvQuestion);
        tvExplanation = findViewById(R.id.tvExplanation);
        tvProgress = findViewById(R.id.tvProgress);
        layoutOptions = findViewById(R.id.layoutOptions);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheck = findViewById(R.id.btnCheck);
        btnNext = findViewById(R.id.btnNext);

        startId = getIntent().getIntExtra("startId", 0);
        endId = getIntent().getIntExtra("endId", 0);
        limit = getIntent().getIntExtra("limit", 40);

        btnCheck.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> nextExercise());

        loadExercises();
    }

    private void loadExercises() {
        db.collection("KanjiExercises")
                .whereGreaterThanOrEqualTo("id_kanji", startId)
                .whereLessThanOrEqualTo("id_kanji", endId)
                .get()
                .addOnSuccessListener(query -> {
                    exercises.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        KanjiExerciseModel ex = doc.toObject(KanjiExerciseModel.class);
                        if (ex != null) exercises.add(ex);
                    }

                    Collections.shuffle(exercises);

                    if (exercises.size() > limit)
                        exercises = exercises.subList(0, limit);

                    index = 0;
                    showExercise();
                });
    }

    private void showExercise() {
        if (index >= exercises.size()) {
            tvQuestion.setText("Упражнения завершены! 🎉");
            layoutOptions.removeAllViews();
            etAnswer.setVisibility(View.GONE);
            btnCheck.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
            return;
        }

        currentEx = exercises.get(index);

        tvProgress.setText((index + 1) + " / " + exercises.size());
        tvQuestion.setText(currentEx.getQuestion());
        tvExplanation.setText("");
        layoutOptions.removeAllViews();
        etAnswer.setText("");

        etAnswer.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnCheck.setVisibility(View.VISIBLE);

        if (currentEx.getOptions() != null && !currentEx.getOptions().isEmpty())
            showChoose();
        else
            showWrite();
    }

    private void showChoose() {
        layoutOptions.setVisibility(View.VISIBLE);
        etAnswer.setVisibility(View.VISIBLE);

        for (String opt : currentEx.getOptions()) {
            Button b = new Button(this);
            b.setText(opt);
            b.setAllCaps(false);
            b.setTextSize(18f);
            b.setPadding(10, 10, 10, 10);
            b.setOnClickListener(v -> etAnswer.setText(opt));
            layoutOptions.addView(b);
        }
    }

    private void showWrite() {
        layoutOptions.setVisibility(View.GONE);
        etAnswer.setVisibility(View.VISIBLE);
    }

    private void checkAnswer() {
        String user = etAnswer.getText().toString().trim();
        List<String> correctAnswers = currentEx.getAnswer();
        boolean isCorrect = false;

        if (correctAnswers != null) {
            for (String ans : correctAnswers) {
                if (ans != null && user.equals(ans.trim())) {
                    isCorrect = true;
                    break;
                }
            }
        }

        if (isCorrect) {
            tvExplanation.setText("Правильно ✅\n" + currentEx.getExplanation());
        } else {
            tvExplanation.setText(
                    "Ошибка ❌\nПравильный ответ: " + Arrays.toString(correctAnswers.toArray()) +
                            "\n\n" + currentEx.getExplanation()
            );
        }

        btnNext.setVisibility(View.VISIBLE);
        btnCheck.setVisibility(View.GONE);
    }


    private void nextExercise() {
        index++;
        showExercise();
    }
}
