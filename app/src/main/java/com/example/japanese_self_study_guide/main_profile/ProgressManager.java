package com.example.japanese_self_study_guide.main_profile;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.HashMap;
import java.util.Map;

public class ProgressManager {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void initProgressIfNeeded(String userId) {
        DocumentReference ref = db.collection("Progress").document(userId);

        ref.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {

                Map<String, Object> data = new HashMap<>();

                data.put("hiraganaDone", 0);
                data.put("katakanaDone", 0);
                data.put("kanjiDone", 0);
                data.put("grammarDone", 0);
                data.put("textsDone", 0);
                data.put("audioDone", 0);

                data.put("hiraganaLearned", new java.util.ArrayList<>());
                data.put("katakanaLearned", new java.util.ArrayList<>());
                data.put("kanjiLearned", new java.util.ArrayList<>());
                data.put("grammarLearned", new java.util.ArrayList<>());
                data.put("textsLearned", new java.util.ArrayList<>());
                data.put("audioLearned", new java.util.ArrayList<>());

                data.put("createdAt", FieldValue.serverTimestamp());

                ref.set(data);
            }
        });
    }

    // ✅ 2. УВЕЛИЧЕНИЕ ПРОГРЕССА
    public static Task<Void> incrementProgress(String userId, String field, long amount) {
        DocumentReference ref = db.collection("Progress").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put(field, FieldValue.increment(amount));
        updates.put("lastUpdated", FieldValue.serverTimestamp());
        return ref.set(updates, com.google.firebase.firestore.SetOptions.merge());
    }

    // ✅ 3. УСТАНОВКА ЗНАЧЕНИЯ
    public static Task<Void> setProgressValue(String userId, String field, long value) {
        DocumentReference ref = db.collection("Progress").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put(field, value);
        updates.put("lastUpdated", FieldValue.serverTimestamp());
        return ref.set(updates, com.google.firebase.firestore.SetOptions.merge());
    }

    // ✅ 4. ЧТЕНИЕ
    public static Task<com.google.firebase.firestore.DocumentSnapshot> getProgressDoc(String userId) {
        return db.collection("Progress").document(userId).get();
    }
}
