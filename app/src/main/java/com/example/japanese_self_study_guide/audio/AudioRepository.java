package com.example.japanese_self_study_guide.audio;

import com.example.japanese_self_study_guide.DB;
import com.example.japanese_self_study_guide.audio.model.AudioExerciseModel;
import com.example.japanese_self_study_guide.audio.model.AudioModel;
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

public class AudioRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final ContentDao dao = DB.getLocalDatabase().contentDao();
    private final FirebaseContentSync sync = new FirebaseContentSync();

    public Task<List<AudioModel>> getAudioList() {
        return CacheTaskRunner.call(dao::getAllAudio)
                .continueWithTask(task -> {
                    List<AudioModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncAudio();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncAudio();
                });
    }

    public Task<AudioModel> getAudioById(int audioId) {
        return CacheTaskRunner.call(() -> dao.getAudioById(audioId))
                .continueWithTask(task -> {
                    AudioModel cached = task.isSuccessful() ? task.getResult() : null;
                    if (cached != null) {
                        sync.syncAudio();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncAudio().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getAudioById(audioId))
                    );
                });
    }

    public Task<List<Integer>> getLearnedAudioIds() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return Tasks.forResult(new ArrayList<>());

        return ProgressManager.getProgressDoc(uid)
                .continueWith(task -> {
                    List<Integer> ids = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return ids;

                    @SuppressWarnings("unchecked")
                    List<Long> learned = (List<Long>) task.getResult().get("audioLearned");
                    if (learned == null) return ids;

                    for (Long l : learned) ids.add(l.intValue());
                    return ids;
                });
    }

    public Task<List<AudioExerciseModel>> getExercises(int audioId) {
        return CacheTaskRunner.call(() -> dao.getAudioExercisesByAudioId(audioId))
                .continueWithTask(task -> {
                    List<AudioExerciseModel> cached = task.isSuccessful() && task.getResult() != null
                            ? task.getResult()
                            : new ArrayList<>();
                    if (!cached.isEmpty()) {
                        sync.syncAudioExercises();
                        return Tasks.forResult(cached);
                    }
                    return sync.syncAudioExercises().continueWithTask(ignored ->
                            CacheTaskRunner.call(() -> dao.getAudioExercisesByAudioId(audioId))
                    );
                });
    }

    public Task<Void> markAudioCompleted(int audioId) {
        String uid = auth.getUid();
        if (uid == null) return Tasks.forResult(null);

        return db.collection("Progress")
                .document(uid)
                .update(
                        "audioDone", FieldValue.increment(1),
                        "audioLearned", FieldValue.arrayUnion(audioId)
                );
    }
}
