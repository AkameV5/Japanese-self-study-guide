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

public class HiraganaRepository {

    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();
    private final ContentDao dao = DB.getLocalDatabase().contentDao();
    private final FirebaseContentSync sync = new FirebaseContentSync();

    public Task<List<HiraganaItem>> getSymbols() {
        return CacheTaskRunner.call(dao::getAllHiragana)
                .continueWithTask(task -> {
                    List<HiraganaItem> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncHiragana();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncHiragana();
                });
    }

    public Task<List<HiraganaItem>> getSymbolsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        return CacheTaskRunner.call(() -> dao.getHiraganaByIds(ids))
                .continueWithTask(task -> {
                    List<HiraganaItem> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncHiragana();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncHiragana().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getHiraganaByIds(ids))
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
                    List<Long> learned = (List<Long>) task.getResult().get("hiraganaLearned");
                    if (learned == null) return ids;
                    for (Long l : learned) ids.add(l.intValue());
                    return ids;
                });
    }

    public Task<List<HiraganaExerciseModel>> getExercises(List<Integer> hiraganaIds) {
        if (hiraganaIds == null || hiraganaIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        return CacheTaskRunner.call(() -> dao.getHiraganaExercisesByIds(hiraganaIds))
                .continueWithTask(task -> {
                    List<HiraganaExerciseModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncHiraganaExercises();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncHiraganaExercises().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getHiraganaExercisesByIds(hiraganaIds))
                    );
                });
    }

    public Task<List<HiraganaExerciseModel>> getAllExercises() {
        return CacheTaskRunner.call(dao::getAllHiraganaExercises)
                .continueWithTask(task -> {
                    List<HiraganaExerciseModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncHiraganaExercises();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncHiraganaExercises();
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
