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
    private final java.util.Map<Integer, Integer> totalPerSymbol = new java.util.HashMap<>();
    private final java.util.Map<Integer, Integer> correctPerSymbol = new java.util.HashMap<>();
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
            finishExercise();
            tvQuestion.setText("Все упражнения завершены!");
            layoutOptions.removeAllViews();
            etAnswer.setVisibility(View.GONE);
            btnCheck.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
            return;
        }

        currentEx = exercises.get(index);

        int katakanaId = currentEx.getKatakanaId();
        totalPerSymbol.put(
                katakanaId,
                totalPerSymbol.getOrDefault(katakanaId, 0) + 1
        );

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
            int katakanaId = currentEx.getKatakanaId();
            correctPerSymbol.put(
                    katakanaId,
                    correctPerSymbol.getOrDefault(katakanaId, 0) + 1
            );
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

    private void finishExercise() {

        String uid = com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .getUid();

        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Progress")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    List<Long> learned =
                            (List<Long>) doc.get("katakanaLearned");

                    if (learned == null) learned = new ArrayList<>();

                    for (Integer katakanaId : totalPerSymbol.keySet()) {

                        if (learned.contains(katakanaId.longValue()))
                            continue;

                        int total = totalPerSymbol.get(katakanaId);
                        int correct = correctPerSymbol.getOrDefault(katakanaId, 0);

                        float percent = (correct * 100f) / total;

                        if (percent >= 70f) {
                            db.collection("Progress")
                                    .document(uid)
                                    .update(
                                            "katakanaLearned",
                                            com.google.firebase.firestore.FieldValue.arrayUnion(katakanaId),
                                            "katakanaDone",
                                            com.google.firebase.firestore.FieldValue.increment(1)
                                    );
                        }
                    }

                    Toast.makeText(
                            this,
                            "Упражнение завершено!",
                            Toast.LENGTH_LONG
                    ).show();
                    android.content.Intent intent =
                            new android.content.Intent(
                                    KatakanaExercisesActivity.this,
                                    KatakanaActivity.class
                            );

                    intent.setFlags(
                            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );
                    startActivity(intent);
                    finish();
                });
    }

}
