package com.example.japanese_self_study_guide.kanji;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.main_profile.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
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

    private Map<Integer, Integer> totalPerKanji = new HashMap<>();
    private Map<Integer, Integer> correctPerKanji = new HashMap<>();

    private int totalCorrectAnswers = 0;
    private int totalQuestionsAnswered = 0;

    private boolean dailyMode = false;
    private int dailyLimit = 20;


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

        dailyMode = getIntent().getBooleanExtra("daily_mode", false);

        btnCheck.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> nextExercise());

        loadExercises();
    }

    private void loadExercises() {
        if (!dailyMode) {
            loadExercisesClassic();
            return;
        }
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("Progress")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Long> learned = (List<Long>) doc.get("kanjiLearned");
                    if (learned == null) learned = new ArrayList<>();
                    loadExercisesDaily(learned);
                });
    }

    private void loadExercisesClassic() {
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

    private void loadExercisesDaily(List<Long> learnedKanji) {
        db.collection("KanjiExercises")
                .whereGreaterThanOrEqualTo("id_kanji", startId)
                .whereLessThanOrEqualTo("id_kanji", endId)
                .get()
                .addOnSuccessListener(query -> {
                    exercises.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        KanjiExerciseModel ex = doc.toObject(KanjiExerciseModel.class);
                        if (ex == null) continue;
                        if (learnedKanji.contains((long) ex.getId_kanji()))
                            continue; // пропускаем уже выученные
                        exercises.add(ex);
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
            finishKanjiExercise();
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

        int kanjiId = currentEx.getId_kanji();
        totalPerKanji.put(
                kanjiId,
                totalPerKanji.getOrDefault(kanjiId, 0) + 1
        );

        totalQuestionsAnswered++;

        if (isCorrect) {
            tvExplanation.setText("Правильно ✅\n" + currentEx.getExplanation());
            correctPerKanji.put(
                    kanjiId,
                    correctPerKanji.getOrDefault(kanjiId, 0) + 1
            );
            totalCorrectAnswers++;
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

    private void finishKanjiExercise() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Progress")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    List<Long> learned =
                            (List<Long>) doc.get("kanjiLearned");

                    if (learned == null) learned = new ArrayList<>();

                    int learnedNow = 0;
                    int totalKanji = totalPerKanji.size();

                    for (Integer kanjiId : totalPerKanji.keySet()) {

                        if (learned.contains(kanjiId.longValue())) {
                            learnedNow++;
                            continue;
                        }

                        int total = totalPerKanji.get(kanjiId);
                        int correct = correctPerKanji.getOrDefault(kanjiId, 0);

                        float percent = (correct * 100f) / total;

                        if (percent >= 70f) {
                            learnedNow++;
                            db.collection("Progress")
                                    .document(uid)
                                    .update(
                                            "kanjiLearned",
                                            FieldValue.arrayUnion(kanjiId),
                                            "kanjiDone",
                                            FieldValue.increment(1)
                                    );
                            MainActivity.removeDailyRecommendation("kanji", kanjiId);
                        }
                    }

                    Toast.makeText(this,
                            "Упражнения по кандзи завершены!",
                            Toast.LENGTH_SHORT).show();

                    final int finalLearnedNow = learnedNow;
                    final int finalTotalKanji = totalKanji;

                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        android.content.Intent intent = new android.content.Intent(
                                KanjiExercisesActivity.this,
                                KanjiExerciseFinishActivity.class
                        );
                        intent.putExtra("correct", totalCorrectAnswers);
                        intent.putExtra("total", totalQuestionsAnswered);
                        intent.putExtra("learned", finalLearnedNow);
                        intent.putExtra("totalKanji", finalTotalKanji);
                        startActivity(intent);
                        finish();
                    }, 1500);
                });
    }
}