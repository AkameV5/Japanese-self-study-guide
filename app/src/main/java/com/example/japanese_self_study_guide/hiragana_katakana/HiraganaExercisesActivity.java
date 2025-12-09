package com.example.japanese_self_study_guide.hiragana_katakana;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiraganaExercisesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<HiraganaExerciseModel> exercises = new ArrayList<>();

    private TextView tvQuestion, tvExplanation, tvProgress;
    private LinearLayout layoutOptions;
    private EditText etAnswer;
    private Button btnCheck, btnNext;
    private int index = 0;
    private HiraganaExerciseModel currentEx;
    private final Map<Integer, Integer> totalPerSymbol = new HashMap<>();
    private final Map<Integer, Integer> correctPerSymbol = new HashMap<>();


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

        btnCheck.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> nextExercise());

        boolean allRandom = getIntent().getBooleanExtra("all_random_mode", false);
        if (allRandom) {
            loadAllRandom();
            return;
        }

        boolean dailyMode = getIntent().getBooleanExtra("daily_mode", false);
        int dailyLimit = getIntent().getIntExtra("daily_limit", 0);
        int[] dailyIds = getIntent().getIntArrayExtra("daily_hiragana_ids");
        if (dailyMode && dailyIds != null) {
            loadDailyGroup(dailyIds, dailyLimit);
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

    private void showExercise() {
        if (index >= exercises.size()) {

            // ✅ ГЛАВНОЕ — считаем прогресс
            finishExercise();

            // ✅ дальше — визуальное завершение
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
        etAnswer.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnCheck.setVisibility(View.VISIBLE);
        etAnswer.setText("");

        switch (currentEx.getType()) {
            case "choose":
                showChoose();
                break;

            case "write":
            case "reverse_write":
                showWrite();
                break;
        }
    }


    // --- MULTIPLE CHOICE ---
    private void showChoose() {
        layoutOptions.setVisibility(View.VISIBLE);

        for (String opt : currentEx.getOptions()) {
            Button b = new Button(this);
            b.setText(opt);
            b.setTextSize(18f);
            b.setPadding(10, 10, 10, 10);
            b.setOnClickListener(v -> etAnswer.setText(opt));
            layoutOptions.addView(b);
        }

        etAnswer.setVisibility(View.VISIBLE); // показываем выбранный ответ
    }

    // --- WRITE ANSWER ---
    private void showWrite() {
        layoutOptions.setVisibility(View.GONE);
        etAnswer.setVisibility(View.VISIBLE);
    }

    private void checkAnswer() {

        String user = etAnswer.getText().toString().trim();
        String correct = currentEx.getCorrectAnswer().trim();

        int hiraganaId = currentEx.getHiraganaId();

        // ✅ увеличиваем общее число вопросов по символу
        totalPerSymbol.put(
                hiraganaId,
                totalPerSymbol.getOrDefault(hiraganaId, 0) + 1
        );

        // ✅ если ответ правильный
        if (user.equals(correct)) {

            correctPerSymbol.put(
                    hiraganaId,
                    correctPerSymbol.getOrDefault(hiraganaId, 0) + 1
            );

            tvExplanation.setText("Правильно! 🎉\n" + currentEx.getExplanation());

        } else {
            tvExplanation.setText(
                    "Неверно ❌\nПравильный ответ: " + correct +
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

    private void loadGroup(int[] groupIds) {

        List<Integer> ids = new ArrayList<>();
        for (int id : groupIds) ids.add(id);

        db.collection("HiraganaExercises")
                .whereIn("hiraganaId", ids)
                .get()
                .addOnSuccessListener(query -> {
                    exercises.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        HiraganaExerciseModel ex = doc.toObject(HiraganaExerciseModel.class);
                        if (ex != null) exercises.add(ex);
                    }

                    Collections.shuffle(exercises);
                    index = 0;
                    showExercise();
                });
    }

    private void loadAllRandom() {
        db.collection("HiraganaExercises")
                .get()
                .addOnSuccessListener(query -> {

                    exercises.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        HiraganaExerciseModel ex = doc.toObject(HiraganaExerciseModel.class);
                        if (ex != null) exercises.add(ex);
                    }

                    Collections.shuffle(exercises);

                    if (exercises.size() > 50)
                        exercises = exercises.subList(0, 50);

                    index = 0;
                    showExercise();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                );
    }

    private void finishExercise() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Progress")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    List<Long> learned =
                            (List<Long>) doc.get("hiraganaLearned");
                    if (learned == null) learned = new ArrayList<>();

                    for (Integer hiraganaId : totalPerSymbol.keySet()) {

                        if (learned.contains(hiraganaId.longValue()))
                            continue;

                        int total = totalPerSymbol.get(hiraganaId);
                        int correct = correctPerSymbol.getOrDefault(hiraganaId, 0);
                        float percent = (correct * 100f) / total;

                        if (percent >= 70f) {
                            db.collection("Progress")
                                    .document(uid)
                                    .update(
                                            "hiraganaLearned",
                                            FieldValue.arrayUnion(hiraganaId),
                                            "hiraganaDone",
                                            FieldValue.increment(1)
                                    );
                        }
                    }

                    Toast.makeText(
                            HiraganaExercisesActivity.this,
                            "Упражнение завершено!",
                            Toast.LENGTH_SHORT
                    ).show();
                    new Handler(Looper.getMainLooper()).postDelayed(
                            this::goBackToHiraganaList,
                            2000
                    );

                });
    }
    private void goBackToHiraganaList() {
        Intent intent = new Intent(
                HiraganaExercisesActivity.this,
                HiraganaActivity.class
        );

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void loadDailyGroup(int[] idsArr, int limit) {
        List<Integer> ids = new ArrayList<>();
        for (int i : idsArr) ids.add(i);

        db.collection("HiraganaExercises")
                .whereIn("hiraganaId", ids)
                .get()
                .addOnSuccessListener(query -> {
                    exercises.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        HiraganaExerciseModel ex = doc.toObject(HiraganaExerciseModel.class);
                        if (ex != null) exercises.add(ex);
                    }
                    Collections.shuffle(exercises);
                    if (limit > 0 && exercises.size() > limit) {
                        exercises = exercises.subList(0, limit);
                    }
                    index = 0;
                    showExercise();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                );
    }



}
