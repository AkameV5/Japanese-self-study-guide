package com.example.japanese_self_study_guide.kanji;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class KanjiExerciseGroupsActivity extends AppCompatActivity {

    private LinearLayout layoutGroups;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private HashMap<Integer, KanjiModel> kanjiMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanji_exercise_groups);

        layoutGroups = findViewById(R.id.layoutKanjiGroups);

        loadKanjiFromFirestore(() -> {
            createGroupButtons();
        });
    }

    private void loadKanjiFromFirestore(Runnable onLoaded) {
        db.collection("Kanji")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        KanjiModel k = doc.toObject(KanjiModel.class);
                        int id = (int) k.getId();
                        kanjiMap.put(id, k);
                    }
                    onLoaded.run();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    onLoaded.run();
                });
    }

    private void createGroupButtons() {
        List<ExerciseGroup> groups = GroupsProvider.getGroups();
        for (ExerciseGroup group : groups) {
            addGroupButton(group);
        }
    }

    private void addGroupButton(ExerciseGroup group) {
        Button btn = new Button(this);

        boolean isReview = group.getTitle().contains("Общее")
                || group.getTitle().contains("Окончание");

        if (isReview) {
            btn.setText(group.getTitle());
        } else {
            btn.setText(getPreviewKanji(group));
        }

        btn.setTextSize(22f);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setAllCaps(false);
        btn.setPadding(20, 20, 20, 20);

        btn.setBackgroundResource(R.drawable.group_button);
        btn.setTextColor(getColor(R.color.textDark));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 14);

        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> openExercises(group));

        layoutGroups.addView(btn);
    }
    private List<KanjiModel> getKanjiForGroup(ExerciseGroup group) {
        List<KanjiModel> list = new ArrayList<>();
        for (int id = group.getStartId(); id <= group.getEndId(); id++) {
            if (kanjiMap.containsKey(id)) {
                list.add(kanjiMap.get(id));
            }
        }
        return list;
    }

    private String getPreviewKanji(ExerciseGroup group) {
        List<KanjiModel> list = getKanjiForGroup(group);
        if (list.isEmpty()) return group.getTitle();

        Collections.shuffle(list);
        int count = Math.min(5, list.size());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(list.get(i).getKanji()).append("  ");
        }
        return sb.toString();
    }

    private void openExercises(ExerciseGroup group) {
        Intent intent = new Intent(this, KanjiExercisesActivity.class);
        intent.putExtra("startId", group.getStartId());
        intent.putExtra("endId", group.getEndId());
        intent.putExtra("limit", group.getLimit());
        startActivity(intent);
    }
}
