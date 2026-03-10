package com.example.japanese_self_study_guide.dictionary;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DictionaryRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<List<Word>> getWords() {
        return db.collection("Words")
                .get()
                .continueWith(task -> {
                    List<Word> result = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) return result;
                    for (var doc : task.getResult()) {
                        Word w = doc.toObject(Word.class);
                        if (w != null) result.add(w);
                    }
                    return result;
                });
    }
}