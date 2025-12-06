package com.example.japanese_self_study_guide;

import android.app.Application;
import android.util.Log;

import com.example.japanese_self_study_guide.dictionary.Word;
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
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class DB extends Application {

    // ✅ флажки — включай при необходимости
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

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // Предзагрузка хираганы в кэш
        db.collection("Hiragana")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("DB", "🔥 Хирагана предзагружена в кэш Firestore");
                    } else {
                        Log.w("DB", "Ошибка предзагрузки: ", task.getException());
                    }
                });

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


    }

    // ✅ Универсальный метод чтения JSON из /res/raw
    private String readJsonFromRaw(int rawId) throws Exception {
        InputStream is = getResources().openRawResource(rawId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString("UTF-8");
    }

    // ✅ Загрузка текстов (оригинал)
    private void uploadTexts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.text10); // texts.json в /res/raw
            Gson gson = new Gson();

            // Преобразуем JSON в Map<id, TextModel>
            Type type = new TypeToken<Map<String, TextModel>>() {}.getType();
            Map<String, TextModel> data = gson.fromJson(json, type);

            for (Map.Entry<String, TextModel> entry : data.entrySet()) {
                db.collection("Texts")
                        .document(entry.getKey())
                        .set(entry.getValue())
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_TEXTS", "✅ Добавлен текст: " + entry.getKey()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_TEXTS", "❌ Ошибка при добавлении", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_TEXTS", "❌ Ошибка чтения JSON", e);
        }
    }

    // ✅ Загрузка переводов
    private void uploadTranslations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.translations_text10); // translations_texts.json
            Gson gson = new Gson();

            // Преобразуем JSON в Map<id, TranslationModel>
            Type type = new TypeToken<Map<String, TranslationModel>>() {}.getType();
            Map<String, TranslationModel> data = gson.fromJson(json, type);

            for (Map.Entry<String, TranslationModel> entry : data.entrySet()) {
                db.collection("Translations_texts")
                        .document(entry.getKey())
                        .set(entry.getValue())
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_TRANSL", "✅ Добавлен перевод: " + entry.getKey()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_TRANSL", "❌ Ошибка при добавлении", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_TRANSL", "❌ Ошибка чтения JSON", e);
        }
    }

    // ✅ Твоя прежняя функция для кандзи
    private void uploadNewKanji() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.kanji_361_428);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<KanjiModel>>() {}.getType();
            List<KanjiModel> newKanji = gson.fromJson(json, listType);

            for (KanjiModel k : newKanji) {
                db.collection("Kanji")
                        .document(String.valueOf(k.getId()))
                        .set(k)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_KANJI", "✅ Добавлено: " + k.getId()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_KANJI", "❌ Ошибка", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_KANJI", "❌ Ошибка чтения JSON", e);
        }
    }

    // ✅ Загрузка словаря в Firestore
    private void uploadWords() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.words); // words.json в /res/raw
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Word>>() {}.getType();
            List<Word> words = gson.fromJson(json, listType);

            for (Word w : words) {
                db.collection("Words")
                        .document(String.valueOf(w.getId()))
                        .set(w)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_WORDS", "✅ Добавлено слово: " + w.getWord()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_WORDS", "❌ Ошибка при добавлении слова", e));
            }
        } catch (Exception e) {
            Log.e("UPLOAD_WORDS", "❌ Ошибка чтения JSON", e);
        }
    }

    private void uploadGrammar() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.grammar); // grammar.json в /res/raw
            Gson gson = new Gson();

            Type listType = new TypeToken<List<com.example.japanese_self_study_guide.grammar.GrammarRule>>() {}.getType();
            List<com.example.japanese_self_study_guide.grammar.GrammarRule> grammarList =
                    gson.fromJson(json, listType);

            for (com.example.japanese_self_study_guide.grammar.GrammarRule g : grammarList) {
                db.collection("Grammar")
                        .document(String.valueOf(g.getId()))
                        .set(g)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_GRAMMAR", "✅ Добавлена грамматика ID: " + g.getId()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_GRAMMAR", "❌ Ошибка при добавлении", e));
            }

        } catch (Exception e) {
            Log.e("UPLOAD_GRAMMAR", "❌ Ошибка чтения JSON", e);
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
                                Log.d("UPLOAD_HIRAGANA_EX", "✅ Добавлено упражнение: " + entry.getKey()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_HIRAGANA_EX", "❌ Ошибка", e));
            }

        } catch (Exception e) {
            Log.e("UPLOAD_HIRAGANA_EX", "❌ Ошибка чтения JSON", e);
        }
    }
    private void uploadKatakanaExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            // Катакана JSON в /res/raw/katakana_exercises.json
            String json = readJsonFromRaw(R.raw.katakana_exercises);
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, KatakanaExerciseModel>>() {}.getType();
            Map<String, KatakanaExerciseModel> data = gson.fromJson(json, type);

            for (Map.Entry<String, KatakanaExerciseModel> entry : data.entrySet()) {
                db.collection("KatakanaExercises")
                        .document(entry.getKey())
                        .set(entry.getValue())
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_KATAKANA_EX", "✅ Добавлено упражнение: " + entry.getKey()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_KATAKANA_EX", "❌ Ошибка при добавлении", e));
            }

        } catch (Exception e) {
            Log.e("UPLOAD_KATAKANA_EX", "❌ Ошибка чтения JSON", e);
        }
    }
    private void uploadKanjiExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.kanji_exercises); // kanji_exercises.json
            Gson gson = new Gson();

            Type type = new TypeToken<List<KanjiExerciseModel>>() {}.getType();
            List<KanjiExerciseModel> data = gson.fromJson(json, type);

            for (KanjiExerciseModel ex : data) {
                db.collection("KanjiExercises")
                        .document(String.valueOf(ex.getId()))
                        .set(ex)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_KANJI_EX", "✅ Добавлено упражнение: " + ex.getId()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_KANJI_EX", "❌ Ошибка", e));
            }

        } catch (Exception e) {
            Log.e("UPLOAD_KANJI_EX", "❌ Ошибка чтения JSON", e);
        }
    }

    private void uploadGrammarExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.grammar_exercises); // grammar_exercises.json
            Gson gson = new Gson();

            Type type = new TypeToken<List<com.example.japanese_self_study_guide.grammar.GrammarExerciseModel>>() {}.getType();
            List<com.example.japanese_self_study_guide.grammar.GrammarExerciseModel> exercises =
                    gson.fromJson(json, type);

            for (com.example.japanese_self_study_guide.grammar.GrammarExerciseModel ex : exercises) {
                db.collection("GrammarExercises")
                        .document(String.valueOf(ex.getId()))
                        .set(ex)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_GRAMMAR_EX", "✅ Добавлено упражнение ID: " + ex.getId()))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_GRAMMAR_EX", "❌ Ошибка при добавлении", e));
            }

        } catch (Exception e) {
            Log.e("UPLOAD_GRAMMAR_EX", "❌ Ошибка чтения JSON", e);
        }
    }

    private void uploadTextsExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            String json = readJsonFromRaw(R.raw.texts_exercises); // твой файл в /res/raw
            Gson gson = new Gson();

            // JSON → List<TextExerciseModel>
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> wrapper = gson.fromJson(json, type);

            List<Map<String, Object>> exercises = (List<Map<String, Object>>) wrapper.get("text_exercises");

            for (Map<String, Object> ex : exercises) {
                String id = String.valueOf(ex.get("id"));

                db.collection("TextsExercises")
                        .document(id)
                        .set(ex)
                        .addOnSuccessListener(a ->
                                Log.d("UPLOAD_TEXTS_EX", "✅ Добавлено упражнение ID: " + id))
                        .addOnFailureListener(e ->
                                Log.e("UPLOAD_TEXTS_EX", "❌ Ошибка загрузки: " + id, e));
            }

        } catch (Exception e) {
            Log.e("UPLOAD_TEXTS_EX", "❌ Ошибка чтения JSON", e);
        }
    }

}
