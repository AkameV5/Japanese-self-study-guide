package com.example.japanese_self_study_guide.hiragana_katakana;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class KatakanaGroupsActivity extends AppCompatActivity {

    private LinearLayout layoutGroups;
    private Map<Integer, String> symbolMap = new HashMap<>();

    private int[][] GROUPS_5 = {
            {1,2,3,4,5}, {6,7,8,9,10}, {11,12,13,14,15},
            {16,17,18,19,20}, {21,22,23,24,25}, {26,27,28,29,30},
            {31,32,33,34,35}, {36,37,38,39,40}, {41,42,43,44,45},
            {46,47,48,49,50}, {51,52,53,54,55}, {56,57,58,59,60},
            {61,62,63}, {64,65,66,67,68}, {69,70,71}
    };

    private int[][] GROUPS_3 = {
            {72,73,74}, {75,76,77}, {78,79,80}, {81,82,83},
            {84,85,86}, {87,88,89}, {90,91,92}, {93,94,95},
            {96,97,98}, {99,100,101}, {102,103,104}, {105,106,107}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groups_hiragana); // можно сделать свой layout

        layoutGroups = findViewById(R.id.layoutGroups);

        loadSymbolsAndBuildGroups();
    }

    private void loadSymbolsAndBuildGroups() {
        FirebaseFirestore.getInstance()
                .collection("Katakana")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Integer id = doc.getLong("id").intValue();
                        String symbol = doc.getString("symbol");
                        symbolMap.put(id, symbol);
                    }
                    createGroupButtons();
                });
    }

    private void createGroupButtons() {

        addTitle("Основные группы:");

        for (int[] group : GROUPS_5) {
            Button b = makeButton(getGroupLabel(group));
            b.setOnClickListener(v -> openExercises(group));
            layoutGroups.addView(b);
        }

        addTitle("Ёоны:");

        for (int[] group : GROUPS_3) {
            Button b = makeButton(getGroupLabel(group));
            b.setOnClickListener(v -> openExercises(group));
            layoutGroups.addView(b);
        }

        addTitle("Режим тренировки:");

        Button all = makeButton("Все символы (случайные)");
        all.setOnClickListener(v -> openRandomAll());
        layoutGroups.addView(all);
    }

    private void addTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(20f);
        tv.setTextColor(getColor(R.color.accentPink));
        tv.setPadding(0, 24, 0, 12);
        layoutGroups.addView(tv);
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setBackgroundResource(R.drawable.group_button);
        b.setTextSize(18f);
        b.setAllCaps(false);
        b.setTextColor(getColor(R.color.textDark));
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        b.setLayoutParams(params);
        return b;
    }

    private String getGroupLabel(int[] ids) {
        StringBuilder sb = new StringBuilder();

        for (int id : ids) {
            String sym = symbolMap.get(id);
            if (sym != null) sb.append(sym).append("・");
        }

        if (sb.length() > 0)
            sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    private void openExercises(int[] group) {
        Intent intent = new Intent(this, KatakanaExercisesActivity.class);
        intent.putExtra("group_ids", group);
        startActivity(intent);
    }

    private void openRandomAll() {
        Intent intent = new Intent(this, HiraganaExercisesActivity.class);
        intent.putExtra("all_random_mode", true);
        startActivity(intent);
    }

}
