package com.example.japanese_self_study_guide.hiragana_katakana;

import com.example.japanese_self_study_guide.DB;
import com.example.japanese_self_study_guide.cache.CacheTaskRunner;
import com.example.japanese_self_study_guide.cache.ContentDao;
import com.example.japanese_self_study_guide.cache.FirebaseContentSync;
import com.example.japanese_self_study_guide.main_profile.ProgressManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class KatakanaRepository {

    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();
    private final ContentDao dao = DB.getLocalDatabase().contentDao();
    private final FirebaseContentSync sync = new FirebaseContentSync();

    public Task<List<HiraganaItem>> getSymbols() {
        return CacheTaskRunner.call(dao::getAllKatakana)
                .continueWithTask(task -> {
                    List<KatakanaItem> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncKatakana();
                        return Tasks.forResult(mapItems(cached));
                    }
                    return sync.syncKatakana().continueWith(task1 -> mapItems(task1.getResult()));
                });
    }

    public Task<List<HiraganaItem>> getSymbolsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        return CacheTaskRunner.call(() -> dao.getKatakanaByIds(ids))
                .continueWithTask(task -> {
                    List<KatakanaItem> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncKatakana();
                        return Tasks.forResult(mapItems(cached));
                    }
                    return sync.syncKatakana().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> mapItems(dao.getKatakanaByIds(ids)))
                    );
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
                    List<Long> learned = (List<Long>) task.getResult().get("katakanaLearned");
                    if (learned == null) return ids;
                    for (Long l : learned) ids.add(l.intValue());
                    return ids;
                });
    }

    public Task<List<KatakanaExerciseModel>> getExercises(List<Integer> katakanaIds) {
        if (katakanaIds == null || katakanaIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        return CacheTaskRunner.call(() -> dao.getKatakanaExercisesByIds(katakanaIds))
                .continueWithTask(task -> {
                    List<KatakanaExerciseModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncKatakanaExercises();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncKatakanaExercises().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getKatakanaExercisesByIds(katakanaIds))
                    );
                });
    }

    public Task<List<KatakanaExerciseModel>> getAllExercises() {
        return CacheTaskRunner.call(dao::getAllKatakanaExercises)
                .continueWithTask(task -> {
                    List<KatakanaExerciseModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncKatakanaExercises();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncKatakanaExercises();
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

        for (Integer katakanaId : totalPerSymbol.keySet()) {
            if (alreadyLearned.contains(katakanaId.longValue())) {
                learnedNow[0]++;
                continue;
            }
            int total   = totalPerSymbol.get(katakanaId);
            int correct = correctPerSymbol.getOrDefault(katakanaId, 0);
            float pct   = (correct * 100f) / total;
            if (pct >= 70f) {
                learnedNow[0]++;
                db.collection("Progress").document(uid)
                        .update(
                                "katakanaLearned", FieldValue.arrayUnion(katakanaId),
                                "katakanaDone",    FieldValue.increment(1)
                        );
            }
        }
        callback.onDone(learnedNow[0], totalSymbols);
    }

    public interface LearnedCallback {
        void onDone(int learnedNow, int totalSymbols);
    }

    private List<HiraganaItem> mapItems(List<KatakanaItem> items) {
        List<HiraganaItem> result = new ArrayList<>();
        if (items == null) return result;
        for (KatakanaItem item : items) {
            result.add(new HiraganaItem(
                    item.getSymbol(),
                    item.getRomaji(),
                    item.getImageUrl(),
                    item.getId()
            ));
        }
        result.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        return result;
    }
}
