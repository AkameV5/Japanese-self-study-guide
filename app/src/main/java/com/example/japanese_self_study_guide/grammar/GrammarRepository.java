package com.example.japanese_self_study_guide.grammar;

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

public class GrammarRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final ContentDao dao = DB.getLocalDatabase().contentDao();
    private final FirebaseContentSync sync = new FirebaseContentSync();

    public Task<List<GrammarRule>> getRules() {
        return CacheTaskRunner.call(dao::getAllGrammarRules)
                .continueWithTask(task -> {
                    List<GrammarRule> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncGrammarRules();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncGrammarRules();
                });
    }

    public Task<GrammarRule> getRuleById(int grammarId) {
        return CacheTaskRunner.call(() -> dao.getGrammarRuleById(grammarId))
                .continueWithTask(task -> {
                    GrammarRule cached = task.isSuccessful() ? task.getResult() : null;
                    if (cached != null) {
                        sync.syncGrammarRules();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncGrammarRules().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getGrammarRuleById(grammarId))
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
                    List<Long> learned = (List<Long>) task.getResult().get("grammarLearned");
                    if (learned == null) return ids;
                    for (Long l : learned) ids.add(l.intValue());
                    return ids;
                });
    }

    public Task<List<GrammarExercise>> getExercises(int grammarId) {
        return CacheTaskRunner.call(() -> dao.getGrammarExercisesByGrammarId(grammarId))
                .continueWithTask(task -> {
                    List<GrammarExercise> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncGrammarExercises();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncGrammarExercises().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getGrammarExercisesByGrammarId(grammarId))
                    );
                });
    }

    public Task<Void> markGrammarCompleted(int grammarId) {
        String uid = auth.getUid();
        if (uid == null) return Tasks.forResult(null);
        return db.collection("Progress")
                .document(uid)
                .update(
                        "grammarLearned", FieldValue.arrayUnion(grammarId),
                        "grammarDone",    FieldValue.increment(1)
                );
    }
}
