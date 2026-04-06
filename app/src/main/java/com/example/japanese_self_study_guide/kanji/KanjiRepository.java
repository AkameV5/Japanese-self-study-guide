package com.example.japanese_self_study_guide.kanji;

import com.example.japanese_self_study_guide.DB;
import com.example.japanese_self_study_guide.cache.CacheTaskRunner;
import com.example.japanese_self_study_guide.cache.ContentDao;
import com.example.japanese_self_study_guide.cache.FirebaseContentSync;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class KanjiRepository {

    private final ContentDao dao = DB.getLocalDatabase().contentDao();
    private final FirebaseContentSync sync = new FirebaseContentSync();

    public Task<List<KanjiModel>> getAllKanji() {
        return CacheTaskRunner.call(dao::getAllKanji)
                .continueWithTask(task -> {
                    List<KanjiModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncKanji();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncKanji();
                });
    }

    public Task<List<KanjiModel>> getKanjiByIds(List<Integer> ids) {
        List<Double> lookupIds = new ArrayList<>();
        for (Integer id : ids) {
            if (id != null) lookupIds.add(id.doubleValue());
        }

        return CacheTaskRunner.call(() -> dao.getKanjiByIds(lookupIds))
                .continueWithTask(task -> {
                    List<KanjiModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncKanji();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncKanji().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getKanjiByIds(lookupIds))
                    );
                });
    }

    public Task<List<KanjiExerciseModel>> getExercisesInRange(int startId, int endId) {
        return CacheTaskRunner.call(() -> dao.getKanjiExercisesInRange(startId, endId))
                .continueWithTask(task -> {
                    List<KanjiExerciseModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncKanjiExercises();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncKanjiExercises().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getKanjiExercisesInRange(startId, endId))
                    );
                });
    }
}
