package com.example.japanese_self_study_guide.hiragana_katakana;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.main_profile.ProgressManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HiraganaActivity extends AppCompatActivity {

    private RecyclerView recyclerHiragana, recyclerYouon;
    private HiraganaAdapter adapterHiragana, adapterYouon;
    private final List<HiraganaItem> hiraganaList = new ArrayList<>();
    private final List<HiraganaItem> youonList = new ArrayList<>();

    private FirebaseFirestore db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hiragana);

        recyclerHiragana = findViewById(R.id.recyclerHiragana);
        recyclerYouon = findViewById(R.id.recyclerYouon);

        recyclerHiragana.setLayoutManager(new GridLayoutManager(this, 5));
        recyclerYouon.setLayoutManager(new GridLayoutManager(this, 3));

        recyclerHiragana.setHasFixedSize(true);
        recyclerYouon.setHasFixedSize(true);
        recyclerHiragana.setNestedScrollingEnabled(false);
        recyclerYouon.setNestedScrollingEnabled(false);
        recyclerHiragana.setItemAnimator(null);
        recyclerYouon.setItemAnimator(null);

        adapterHiragana = new HiraganaAdapter(hiraganaList, this::showSymbolDialog);
        adapterYouon = new HiraganaAdapter(youonList, this::showSymbolDialog);
        recyclerHiragana.setAdapter(adapterHiragana);
        recyclerYouon.setAdapter(adapterYouon);

        findViewById(R.id.btnExercises).setOnClickListener(v -> {
            Intent intent = new Intent(this, HiraganaGroupsActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();

        boolean dailyMode = getIntent().getBooleanExtra("daily_mode", false);
        int[] dailyIds = getIntent().getIntArrayExtra("daily_hiragana_ids");

        TextView tvHiraTitle = findViewById(R.id.tvHiraganaTitle);
        TextView tvYouonTitle = findViewById(R.id.tvYouonTitle);

        if (dailyMode && dailyIds != null) {

            findViewById(R.id.btnExercises).setVisibility(View.GONE);

            tvHiraTitle.setVisibility(View.GONE);
            tvYouonTitle.setVisibility(View.GONE);

            recyclerYouon.setVisibility(View.GONE);

            findViewById(R.id.btnDaily).setVisibility(View.VISIBLE);

            loadDailySymbols(dailyIds);

            findViewById(R.id.btnDaily).setOnClickListener(v -> {
                Intent ex = new Intent(this, HiraganaExercisesActivity.class);
                ex.putExtra("daily_mode", true);
                ex.putExtra("daily_hiragana_ids", dailyIds);
                startActivity(ex);
            });

            return;
        }

        loadFromFirebase();

        ProgressManager.getProgressDoc(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addOnSuccessListener(doc -> {
                    List<Long> learned = (List<Long>) doc.get("hiraganaLearned");
                    if (learned == null) return;

                    List<Integer> ids = new ArrayList<>();
                    for (Long l : learned) ids.add(l.intValue());

                    handler.post(() -> {
                        adapterHiragana.setLearnedIds(ids);
                        adapterYouon.setLearnedIds(ids);
                    });
                });

    }

    private void loadFromFirebase() {
        db.collection("Hiragana")
                .orderBy("id")
                .get()
                .addOnSuccessListener(query -> executor.execute(() -> {
                    List<HiraganaItem> tempHira = new ArrayList<>();
                    List<HiraganaItem> tempYouon = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : query) {
                        String symbol = doc.getString("symbol");
                        String romaji = doc.getString("romanji");
                        String imageUrl = doc.getString("imageUrl");
                        Long id = doc.getLong("id");
                        if (id == null) continue;

                        HiraganaItem item = new HiraganaItem(symbol, romaji, imageUrl, id.intValue());
                        if (id < 72) tempHira.add(item);
                        else tempYouon.add(item);
                    }

                    Collections.sort(tempHira, Comparator.comparingInt(HiraganaItem::getId));
                    Collections.sort(tempYouon, Comparator.comparingInt(HiraganaItem::getId));
                    insertHiraganaGaps(tempHira);

                    handler.post(() -> {
                        hiraganaList.clear();
                        youonList.clear();
                        hiraganaList.addAll(tempHira);
                        youonList.addAll(tempYouon);
                        adapterHiragana.notifyDataSetChanged();
                        adapterYouon.notifyDataSetChanged();
                    });
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void insertHiraganaGaps(List<HiraganaItem> list) {
        List<HiraganaItem> newList = new ArrayList<>();
        for (HiraganaItem item : list) {
            newList.add(item);

            if (item.getId() == 61) newList.add(new HiraganaItem("", "", null, -1));
            if (item.getId() == 62) newList.add(new HiraganaItem("", "", null, -1));
            if (item.getId() == 69) {
                newList.add(new HiraganaItem("", "", null, -1));
                newList.add(new HiraganaItem("", "", null, -1));
                newList.add(new HiraganaItem("", "", null, -1));
            }
        }
        list.clear();
        list.addAll(newList);
    }

    private void showSymbolDialog(HiraganaItem item) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_symbol_zoom);

        TextView tvSymbolBig = dialog.findViewById(R.id.tvSymbolBig);
        TextView tvRomajiBig = dialog.findViewById(R.id.tvRomajiBig);

        tvSymbolBig.setText(item.getSymbol());
        tvRomajiBig.setText(item.getRomaji());

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void loadDailySymbols(int[] idsArr) {
        List<Integer> ids = new ArrayList<>();
        for (int i : idsArr) ids.add(i);

        db.collection("Hiragana")
                .whereIn("id", ids)
                .get()
                .addOnSuccessListener(query -> executor.execute(() -> {
                    List<HiraganaItem> temp = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : query) {
                        String symbol = doc.getString("symbol");
                        String romaji = doc.getString("romanji");
                        String imageUrl = doc.getString("imageUrl");
                        Long id = doc.getLong("id");
                        if (id == null) continue;
                        temp.add(new HiraganaItem(symbol, romaji, imageUrl, id.intValue()));
                    }
                    Collections.sort(temp, Comparator.comparingInt(HiraganaItem::getId));

                    handler.post(() -> {
                        hiraganaList.clear();
                        youonList.clear();

                        hiraganaList.addAll(temp);
                        youonList.clear();

                        adapterHiragana.notifyDataSetChanged();
                        adapterYouon.notifyDataSetChanged();
                    });
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}
