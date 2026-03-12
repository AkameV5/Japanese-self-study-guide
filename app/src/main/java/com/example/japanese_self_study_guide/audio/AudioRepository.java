package com.example.japanese_self_study_guide.audio;

import com.example.japanese_self_study_guide.audio.model.AudioExerciseModel;
import com.example.japanese_self_study_guide.audio.model.AudioModel;
import com.example.japanese_self_study_guide.main_profile.ProgressManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AudioRepository {

    private static final String AUDIO_BASE_URL =
            "https://raw.githubusercontent.com/AkameV5/Japanese-self-study-guide/master/audio/";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public Task<List<AudioModel>> getAudioList() {
        return db.collection("Audio")
                .get()
                .continueWith(task -> {
                    List<AudioModel> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;

                    for (var doc : task.getResult()) {
                        AudioModel audio = doc.toObject(AudioModel.class);
                        if (audio.getAudioPath() != null) {
                            audio.setUrl(AUDIO_BASE_URL + audio.getAudioPath());
                        }
                        result.add(audio);
                    }
                    return result;
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
        // Use long to match Firestore storage type; no orderBy to avoid composite index requirement
        return db.collection("AudioExercises")
                .whereEqualTo("audioId", (long) audioId)
                .get()
                .continueWith(task -> {
                    List<AudioExerciseModel> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;

                    for (var doc : task.getResult().getDocuments()) {
                        AudioExerciseModel m = doc.toObject(AudioExerciseModel.class);
                        if (m != null) result.add(m);
                    }
                    // Sort client-side instead of relying on Firestore index
                    result.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
                    return result;
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