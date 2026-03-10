package com.example.japanese_self_study_guide.grammar;

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

    public Task<List<GrammarRule>> getRules() {
        return db.collection("Grammar")
                .get()
                .continueWith(task -> {
                    List<GrammarRule> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;
                    for (var doc : task.getResult()) {
                        GrammarRule r = doc.toObject(GrammarRule.class);
                        if (r != null) result.add(r);
                    }
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
                    List<Long> learned = (List<Long>) task.getResult().get("grammarLearned");
                    if (learned == null) return ids;
                    for (Long l : learned) ids.add(l.intValue());
                    return ids;
                });
    }

    public Task<List<GrammarExercise>> getExercises(int grammarId) {
        return db.collection("GrammarExercises")
                .whereEqualTo("id_grammar", grammarId)
                .get()
                .continueWith(task -> {
                    List<GrammarExercise> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;
                    for (var doc : task.getResult()) {
                        GrammarExercise ex = doc.toObject(GrammarExercise.class);
                        if (ex != null) result.add(ex);
                    }
                    return result;
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