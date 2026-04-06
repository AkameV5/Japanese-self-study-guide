package com.example.japanese_self_study_guide.dictionary;

import com.example.japanese_self_study_guide.DB;
import com.example.japanese_self_study_guide.cache.CacheTaskRunner;
import com.example.japanese_self_study_guide.cache.ContentDao;
import com.example.japanese_self_study_guide.cache.FirebaseContentSync;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class DictionaryRepository {

    private final ContentDao dao = DB.getLocalDatabase().contentDao();
    private final FirebaseContentSync sync = new FirebaseContentSync();

    public Task<List<Word>> getWords() {
        return CacheTaskRunner.call(dao::getAllWords)
                .continueWithTask(task -> {
                    List<Word> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncWords();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncWords();
                });
    }
}
