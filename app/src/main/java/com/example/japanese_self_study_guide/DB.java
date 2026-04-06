package com.example.japanese_self_study_guide;

import android.app.Application;
import android.util.Log;

import androidx.room.Room;

import com.example.japanese_self_study_guide.audio.AudioRepository;
import com.example.japanese_self_study_guide.audio.model.AudioCacheManager;
import com.example.japanese_self_study_guide.audio.model.AudioModel;
import com.example.japanese_self_study_guide.cache.AppCacheDatabase;
import com.example.japanese_self_study_guide.cache.FirebaseContentSync;
import com.example.japanese_self_study_guide.dictionary.Word;
import com.example.japanese_self_study_guide.grammar.GrammarExercise;
import com.example.japanese_self_study_guide.grammar.GrammarRule;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaExerciseModel;
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaExerciseModel;
import com.example.japanese_self_study_guide.kanji.KanjiExerciseModel;
import com.example.japanese_self_study_guide.kanji.KanjiModel;
import com.example.japanese_self_study_guide.texts_and_translation.TextModel;
import com.example.japanese_self_study_guide.texts_and_translation.TranslationModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class DB extends Application {
    private static final String AUDIO_BASE_URL =
            "https://raw.githubusercontent.com/AkameV5/Japanese-self-study-guide/master/audio/";

    private static DB instance;
    private static AppCacheDatabase localDatabase;

    // Enable these only when reseeding Firestore from bundled JSON is needed.
    private static final boolean UPLOAD_NEW_KANJI = false;
    private static final boolean UPLOAD_TEXTS = false;
    private static final boolean UPLOAD_TRANSLATIONS = false;
    private static final boolean UPLOAD_GRAMMAR = false;
    private static final boolean UPLOAD_WORDS = false;
    private static final boolean UPLOAD_KATAKANA_EXERCISES = false;
    private static final boolean UPLOAD_HIRAGANA_EXERCISES = false;
    private static final boolean UPLOAD_KANJI_EXERCISES = false;
    private static final boolean UPLOAD_GRAMMAR_EXERCISES = false;
    private static final boolean UPLOAD_TEXTS_EXERCISES = false;
    private static final boolean UPLOAD_AUDIO_EXERCISES = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        localDatabase = Room.databaseBuilder(
                        getApplicationContext(),
                        AppCacheDatabase.class,
                        "firebase_content_cache.db"
                )
                .fallbackToDestructiveMigration()
                .build();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        preloadRoomCache();
        preloadAllAudio();

        if (UPLOAD_NEW_KANJI) {
            uploadNewKanji();
        }

        if (UPLOAD_TEXTS) {
            uploadTexts();
        }

        if (UPLOAD_TRANSLATIONS) {
            uploadTranslations();
        }

        if (UPLOAD_WORDS) {
            uploadWords();
        }

        if (UPLOAD_GRAMMAR) {
            uploadGrammar();
        }

        if (UPLOAD_HIRAGANA_EXERCISES) {
            uploadHiraganaExercises();
        }

        if (UPLOAD_KATAKANA_EXERCISES) {
            uploadKatakanaExercises();
        }

        if (UPLOAD_KANJI_EXERCISES) {
            uploadKanjiExercises();
        }

        if (UPLOAD_GRAMMAR_EXERCISES) {
            uploadGrammarExercises();
        }

        if (UPLOAD_TEXTS_EXERCISES) {
            uploadTextsExercises();
        }

        if (UPLOAD_AUDIO_EXERCISES) {
            uploadAudioExercises();
        }
    }

    public static DB getInstance() {
        return instance;
    }

    public static AppCacheDatabase getLocalDatabase() {
        return localDatabase;
    }

    private void preloadRoomCache() {
        FirebaseContentSync sync = new FirebaseContentSync();
        sync.syncTexts().addOnFailureListener(e -> sync.logWarmFailure("Texts warmup failed", e));
        sync.syncTranslations().addOnFailureListener(e -> sync.logWarmFailure("Translations warmup failed", e));
        sync.syncTextExercises().addOnFailureListener(e -> sync.logWarmFailure("Text exercises warmup failed", e));
        sync.syncGrammarRules().addOnFailureListener(e -> sync.logWarmFailure("Grammar warmup failed", e));
        sync.syncGrammarExercises().addOnFailureListener(e -> sync.logWarmFailure("Grammar exercises warmup failed", e));
        sync.syncAudio().addOnFailureListener(e -> sync.logWarmFailure("Audio warmup failed", e));
        sync.syncAudioExercises().addOnFailureListener(e -> sync.logWarmFailure("Audio exercises warmup failed", e));
        sync.syncHiragana().addOnFailureListener(e -> sync.logWarmFailure("Hiragana warmup failed", e));
        sync.syncHiraganaExercises().addOnFailureListener(e -> sync.logWarmFailure("Hiragana exercises warmup failed", e));
        sync.syncKatakana().addOnFailureListener(e -> sync.logWarmFailure("Katakana warmup failed", e));
        sync.syncKatakanaExercises().addOnFailureListener(e -> sync.logWarmFailure("Katakana exercises warmup failed", e));
        sync.syncKanji().addOnFailureListener(e -> sync.logWarmFailure("Kanji warmup failed", e));
        sync.syncKanjiExercises().addOnFailureListener(e -> sync.logWarmFailure("Kanji exercises warmup failed", e));
        sync.syncWords().addOnFailureListener(e -> sync.logWarmFailure("Words warmup failed", e));
    }

    private String readJsonFromRaw(int rawId) throws Exception {
        InputStream is = getResources().openRawResource(rawId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString("UTF-8");
    }

    private void uploadTexts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.text10);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, TextModel>>() {}.getType();
            Map<String, TextModel> data = gson.fromJson(json, type);

            for (Map.Entry<String, TextModel> entry : data.entrySet()) {
                db.collection("Texts")
                        .document(entry.getKey())
                        .set(entry.getValue())
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_TEXTS", "Added text: " + entry.getKey()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_TEXTS", "Failed to upload text", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_TEXTS", "Failed to read texts JSON", e);
        }
    }

    private void uploadTranslations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.translations_text10);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, TranslationModel>>() {}.getType();
            Map<String, TranslationModel> data = gson.fromJson(json, type);

            for (Map.Entry<String, TranslationModel> entry : data.entrySet()) {
                db.collection("Translations_texts")
                        .document(entry.getKey())
                        .set(entry.getValue())
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_TRANSL", "Added translation: " + entry.getKey()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_TRANSL", "Failed to upload translation", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_TRANSL", "Failed to read translations JSON", e);
        }
    }

    private void uploadNewKanji() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.kanji_361_428);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<KanjiModel>>() {}.getType();
            List<KanjiModel> newKanji = gson.fromJson(json, listType);

            for (KanjiModel kanji : newKanji) {
                db.collection("Kanji")
                        .document(String.valueOf(kanji.getId()))
                        .set(kanji)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_KANJI", "Added kanji: " + kanji.getId()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_KANJI", "Failed to upload kanji", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_KANJI", "Failed to read kanji JSON", e);
        }
    }

    private void uploadWords() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.words);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Word>>() {}.getType();
            List<Word> words = gson.fromJson(json, listType);

            for (Word word : words) {
                db.collection("Words")
                        .document(String.valueOf(word.getId()))
                        .set(word)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_WORDS", "Added word: " + word.getWord()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_WORDS", "Failed to upload word", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_WORDS", "Failed to read words JSON", e);
        }
    }

    private void uploadGrammar() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.grammar);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<GrammarRule>>() {}.getType();
            List<GrammarRule> grammarList = gson.fromJson(json, listType);

            for (GrammarRule rule : grammarList) {
                db.collection("Grammar")
                        .document(String.valueOf(rule.getId()))
                        .set(rule)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_GRAMMAR", "Added grammar rule: " + rule.getId()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_GRAMMAR", "Failed to upload grammar rule", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_GRAMMAR", "Failed to read grammar JSON", e);
        }
    }

    private void uploadHiraganaExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.hiragana_exercises);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, HiraganaExerciseModel>>() {}.getType();
            Map<String, HiraganaExerciseModel> data = gson.fromJson(json, type);

            for (Map.Entry<String, HiraganaExerciseModel> entry : data.entrySet()) {
                db.collection("HiraganaExercises")
                        .document(entry.getKey())
                        .set(entry.getValue())
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_HIRAGANA_EX", "Added exercise: " + entry.getKey()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_HIRAGANA_EX", "Failed to upload exercise", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_HIRAGANA_EX", "Failed to read hiragana exercises JSON", e);
        }
    }

    private void uploadKatakanaExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.katakana_exercises);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, KatakanaExerciseModel>>() {}.getType();
            Map<String, KatakanaExerciseModel> data = gson.fromJson(json, type);

            for (Map.Entry<String, KatakanaExerciseModel> entry : data.entrySet()) {
                db.collection("KatakanaExercises")
                        .document(entry.getKey())
                        .set(entry.getValue())
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_KATAKANA_EX", "Added exercise: " + entry.getKey()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_KATAKANA_EX", "Failed to upload exercise", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_KATAKANA_EX", "Failed to read katakana exercises JSON", e);
        }
    }

    private void uploadKanjiExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.kanji_exercises);
            Gson gson = new Gson();
            Type type = new TypeToken<List<KanjiExerciseModel>>() {}.getType();
            List<KanjiExerciseModel> data = gson.fromJson(json, type);

            for (KanjiExerciseModel exercise : data) {
                db.collection("KanjiExercises")
                        .document(String.valueOf(exercise.getId()))
                        .set(exercise)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_KANJI_EX", "Added exercise: " + exercise.getId()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_KANJI_EX", "Failed to upload exercise", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_KANJI_EX", "Failed to read kanji exercises JSON", e);
        }
    }

    private void uploadGrammarExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.grammar_exercises);
            Gson gson = new Gson();
            Type type = new TypeToken<List<GrammarExercise>>() {}.getType();
            List<GrammarExercise> exercises = gson.fromJson(json, type);

            for (GrammarExercise exercise : exercises) {
                db.collection("GrammarExercises")
                        .document(String.valueOf(exercise.getId()))
                        .set(exercise)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_GRAMMAR_EX", "Added exercise: " + exercise.getId()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_GRAMMAR_EX", "Failed to upload exercise", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_GRAMMAR_EX", "Failed to read grammar exercises JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void uploadTextsExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.texts_exercises);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> wrapper = gson.fromJson(json, type);
            List<Map<String, Object>> exercises = (List<Map<String, Object>>) wrapper.get("text_exercises");

            for (Map<String, Object> exercise : exercises) {
                String id = String.valueOf(exercise.get("id"));

                db.collection("TextsExercises")
                        .document(id)
                        .set(exercise)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_TEXTS_EX", "Added exercise: " + id))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_TEXTS_EX", "Failed to upload exercise: " + id, e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_TEXTS_EX", "Failed to read text exercises JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void uploadAudioExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.audio_exercises);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> wrapper = gson.fromJson(json, type);
            List<Map<String, Object>> exercises =
                    (List<Map<String, Object>>) wrapper.get("audio_exercises");

            for (Map<String, Object> exercise : exercises) {
                String id = String.valueOf(exercise.get("id"));

                db.collection("AudioExercises")
                        .document(id)
                        .set(exercise)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_AUDIO_EX", "Added exercise: " + id))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_AUDIO_EX", "Failed to upload exercise: " + id, e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_AUDIO_EX", "Failed to read audio exercises JSON", e);
        }
    }

    private void preloadAllAudio() {
        new AudioRepository()
                .getAudioList()
                .addOnSuccessListener(audioList -> {
                    for (AudioModel audio : audioList) {
                        int audioId = audio.getId();
                        String url = audio.getUrl();
                        if ((url == null || url.isEmpty()) && audio.getAudioPath() != null) {
                            url = AUDIO_BASE_URL + audio.getAudioPath();
                        }
                        if (url == null || url.isEmpty()) {
                            continue;
                        }

                        AudioCacheManager.preload(
                                this,
                                audioId,
                                url,
                                new AudioCacheManager.Callback() {
                                    @Override
                                    public void onReady(File file) {
                                        Log.d("AUDIO_PRELOAD", "Preloaded audio: " + audioId);
                                    }

                                    @Override
                                    public void onError() {
                                        Log.e("AUDIO_PRELOAD", "Failed to preload audio: " + audioId);
                                    }
                                }
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("AUDIO_PRELOAD", "Failed to load audio list for preloading", e));
    }
}
