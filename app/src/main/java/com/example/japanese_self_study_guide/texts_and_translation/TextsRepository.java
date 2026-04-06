package com.example.japanese_self_study_guide.texts_and_translation;

import com.example.japanese_self_study_guide.DB;
import com.example.japanese_self_study_guide.cache.CacheTaskRunner;
import com.example.japanese_self_study_guide.cache.ContentDao;
import com.example.japanese_self_study_guide.cache.FirebaseContentSync;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class TextsRepository {

    private final ContentDao dao = DB.getLocalDatabase().contentDao();
    private final FirebaseContentSync sync = new FirebaseContentSync();

    public Task<List<TextModel>> getTexts() {
        return CacheTaskRunner.call(dao::getAllTexts)
                .continueWithTask(task -> {
                    List<TextModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncTexts();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncTexts();
                });
    }

    public Task<TextModel> getTextById(int textId) {
        return CacheTaskRunner.call(() -> dao.getTextById(textId))
                .continueWithTask(task -> {
                    TextModel cached = task.isSuccessful() ? task.getResult() : null;
                    if (cached != null) {
                        sync.syncTexts();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncTexts().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getTextById(textId))
                    );
                });
    }

    public Task<TranslationModel> getTranslationByTextId(int textId) {
        return CacheTaskRunner.call(() -> dao.getTranslationByTextId(textId))
                .continueWithTask(task -> {
                    TranslationModel cached = task.isSuccessful() ? task.getResult() : null;
                    if (cached != null) {
                        sync.syncTranslations();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncTranslations().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getTranslationByTextId(textId))
                    );
                });
    }

    public Task<List<ExerciseModel>> getExercisesByTextId(int textId) {
        return CacheTaskRunner.call(() -> dao.getTextExercisesByTextId(textId))
                .continueWithTask(task -> {
                    List<ExerciseModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncTextExercises();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncTextExercises().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getTextExercisesByTextId(textId))
                    );
                });
    }
}
