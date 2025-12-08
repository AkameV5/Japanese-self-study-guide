package com.example.japanese_self_study_guide.main_profile;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class TotalManager {

    private static final String PREF_NAME = "TotalsCache";
    private static final long CACHE_TIME = 7L * 24L * 60L * 60L * 1000L; // 7 дней

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String[] COLLECTIONS = {
            "Hiragana",
            "Katakana",
            "Kanji",
            "Grammar",
            "Texts",
            "Audio"
    };

    private static final String[] KEYS = {
            "hiraganaTotal",
            "katakanaTotal",
            "kanjiTotal",
            "grammarTotal",
            "textsTotal",
            "audioTotal"
    };

    // Загружает totals (кэшированный или новый)
    public static void loadTotals(Context context, TotalsCallback callback) {

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong("lastUpdate", 0);

        long now = System.currentTimeMillis();

        // Если прошло меньше недели — берём из кэша
        if (now - lastUpdate < CACHE_TIME) {
            Map<String, Integer> cached = new HashMap<>();
            for (String key : KEYS) {
                cached.put(key, prefs.getInt(key, 0));
            }
            callback.onTotalsLoaded(cached);
            return;
        }

        // Загружаем из Firestore
        Task<QuerySnapshot>[] tasks = new Task[COLLECTIONS.length];
        for (int i = 0; i < COLLECTIONS.length; i++) {
            tasks[i] = db.collection(COLLECTIONS[i]).get();
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {

                    Map<String, Integer> totals = new HashMap<>();

                    for (int i = 0; i < results.size(); i++) {
                        QuerySnapshot snap = (QuerySnapshot) results.get(i);
                        totals.put(KEYS[i], snap.size());
                    }

                    SharedPreferences.Editor ed = prefs.edit();
                    for (String key : KEYS) ed.putInt(key, totals.get(key));
                    ed.putLong("lastUpdate", System.currentTimeMillis());
                    ed.apply();

                    callback.onTotalsLoaded(totals);
                });
    }

    public interface TotalsCallback {
        void onTotalsLoaded(Map<String, Integer> totals);
    }
}
