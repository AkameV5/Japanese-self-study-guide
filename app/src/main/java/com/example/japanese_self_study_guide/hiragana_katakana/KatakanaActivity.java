package com.example.japanese_self_study_guide.hiragana_katakana;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class KatakanaActivity extends AppCompatActivity {

    private RecyclerView recyclerKatakana, recyclerYouon;
    private HiraganaAdapter adapterKatakana, adapterYouon;
    private final List<HiraganaItem> katakanaList = new ArrayList<>();
    private final List<HiraganaItem> youonList = new ArrayList<>();

    private FirebaseFirestore db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_katakana);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        recyclerKatakana = findViewById(R.id.recyclerKatakana);
        recyclerYouon = findViewById(R.id.recyclerYouonKata);

        recyclerKatakana.setLayoutManager(new GridLayoutManager(this, 5));
        recyclerYouon.setLayoutManager(new GridLayoutManager(this, 3));

        recyclerKatakana.setHasFixedSize(true);
        recyclerYouon.setHasFixedSize(true);
        recyclerKatakana.setNestedScrollingEnabled(false);
        recyclerYouon.setNestedScrollingEnabled(false);
        recyclerKatakana.setItemAnimator(null);
        recyclerYouon.setItemAnimator(null);

        adapterKatakana = new HiraganaAdapter(katakanaList, this::showSymbolDialog);
        adapterYouon = new HiraganaAdapter(youonList, this::showSymbolDialog);
        recyclerKatakana.setAdapter(adapterKatakana);
        recyclerYouon.setAdapter(adapterYouon);

        findViewById(R.id.btnExercises).setOnClickListener(v -> {
            Intent intent = new Intent(this, KatakanaGroupsActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        loadFromFirebase(); // загружаем символы

        ProgressManager.getProgressDoc(userId).addOnSuccessListener(doc -> {
            List<Long> learned = (List<Long>) doc.get("katakanaLearned");
            if (learned == null) return;
            List<Integer> ids = new ArrayList<>();
            for (Long l : learned) ids.add(l.intValue());

            handler.post(() -> {
                adapterKatakana.setLearnedIds(ids);
                adapterYouon.setLearnedIds(ids);
            });
        });

    }

    private void loadFromFirebase() {
        db.collection("Katakana")
                .orderBy("id")
                .get()
                .addOnSuccessListener(query -> executor.execute(() -> {
                    List<HiraganaItem> tempKata = new ArrayList<>();
                    List<HiraganaItem> tempYouon = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : query) {
                        String symbol = doc.getString("symbol");
                        String romaji = doc.getString("romanji");
                        String imageUrl = doc.getString("imageUrl");
                        Long id = doc.getLong("id");
                        if (id == null) continue;

                        HiraganaItem item = new HiraganaItem(symbol, romaji, imageUrl, id.intValue());
                        if (id < 72) tempKata.add(item);
                        else tempYouon.add(item);
                    }

                    Collections.sort(tempKata, Comparator.comparingInt(HiraganaItem::getId));
                    Collections.sort(tempYouon, Comparator.comparingInt(HiraganaItem::getId));
                    insertKatakanaGaps(tempKata);

                    handler.post(() -> {
                        katakanaList.clear();
                        youonList.clear();
                        katakanaList.addAll(tempKata);
                        youonList.addAll(tempYouon);
                        adapterKatakana.notifyDataSetChanged();
                        adapterYouon.notifyDataSetChanged();
                    });
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void insertKatakanaGaps(List<HiraganaItem> list) {
        List<HiraganaItem> newList = new ArrayList<>();
        for (HiraganaItem item : list) {
            newList.add(item);

            if (item.getId() == 61) newList.add(new HiraganaItem("", "", null, -1)); // после ya
            if (item.getId() == 62) newList.add(new HiraganaItem("", "", null, -1)); // после yu
            if (item.getId() == 69) { // после wa
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

}
