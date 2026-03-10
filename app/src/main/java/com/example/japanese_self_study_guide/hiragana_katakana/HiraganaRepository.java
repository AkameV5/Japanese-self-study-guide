package com.example.japanese_self_study_guide.hiragana_katakana;

import com.example.japanese_self_study_guide.main_profile.ProgressManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HiraganaRepository {

    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();

    public Task<List<HiraganaItem>> getSymbols() {
        return db.collection("Hiragana")
                .orderBy("id")
                .get()
                .continueWith(task -> {
                    List<HiraganaItem> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;
                    for (var doc : task.getResult()) {
                        Long id = doc.getLong("id");
                        if (id == null) continue;
                        result.add(new HiraganaItem(
                                doc.getString("symbol"),
                                doc.getString("romanji"),
                                doc.getString("imageUrl"),
                                id.intValue()
                        ));
                    }
                    return result;
                });
    }

    public Task<List<HiraganaItem>> getSymbolsByIds(List<Integer> ids) {
        return db.collection("Hiragana")
                .whereIn("id", ids)
                .get()
                .continueWith(task -> {
                    List<HiraganaItem> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;
                    for (var doc : task.getResult()) {
                        Long id = doc.getLong("id");
                        if (id == null) continue;
                        result.add(new HiraganaItem(
                                doc.getString("symbol"),
                                doc.getString("romanji"),
                                doc.getString("imageUrl"),
                                id.intValue()
                        ));
                    }
                    result.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
                    return result;
                });
    }

    public Task<List<Integer>> getLearnedIds() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return Tasks.forResult(new ArrayList<>());

        return ProgressManager.getProgressDoc(uid)
                .continueWith(task -> {
                    List<Integer> ids = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return ids;
                    @SuppressWarnings("unchecked")
                    List<Long> learned = (List<Long>) task.getResult().get("hiraganaLearned");
                    if (learned == null) return ids;
                    for (Long l : learned) ids.add(l.intValue());
                    return ids;
                });
    }

    public Task<List<HiraganaExerciseModel>> getExercises(List<Integer> hiraganaIds) {
        return db.collection("HiraganaExercises")
                .whereIn("hiraganaId", hiraganaIds)
                .get()
                .continueWith(task -> {
                    List<HiraganaExerciseModel> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;
                    for (var doc : task.getResult()) {
                        HiraganaExerciseModel ex = doc.toObject(HiraganaExerciseModel.class);
                        if (ex != null) result.add(ex);
                    }
                    return result;
                });
    }

    public Task<List<HiraganaExerciseModel>> getAllExercises() {
        return db.collection("HiraganaExercises")
                .get()
                .continueWith(task -> {
                    List<HiraganaExerciseModel> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;
                    for (var doc : task.getResult()) {
                        HiraganaExerciseModel ex = doc.toObject(HiraganaExerciseModel.class);
                        if (ex != null) result.add(ex);
                    }
                    return result;
                });
    }

    public void markLearnedIfPassed(
            java.util.Map<Integer, Integer> totalPerSymbol,
            java.util.Map<Integer, Integer> correctPerSymbol,
            List<Long> alreadyLearned,
            LearnedCallback callback
    ) {
        String uid = auth.getUid();
        if (uid == null) { callback.onDone(0, totalPerSymbol.size()); return; }

        int[] learnedNow = {0};
        int totalSymbols = totalPerSymbol.size();

        for (Integer hiraganaId : totalPerSymbol.keySet()) {
            if (alreadyLearned.contains(hiraganaId.longValue())) {
                learnedNow[0]++;
                continue;
            }
            int total   = totalPerSymbol.get(hiraganaId);
            int correct = correctPerSymbol.getOrDefault(hiraganaId, 0);
            float pct   = (correct * 100f) / total;
            if (pct >= 70f) {
                learnedNow[0]++;
                db.collection("Progress").document(uid)
                        .update(
                                "hiraganaLearned", FieldValue.arrayUnion(hiraganaId),
                                "hiraganaDone",    FieldValue.increment(1)
                        );
            }
        }
        callback.onDone(learnedNow[0], totalSymbols);
    }

    public interface LearnedCallback {
        void onDone(int learnedNow, int totalSymbols);
    }
}