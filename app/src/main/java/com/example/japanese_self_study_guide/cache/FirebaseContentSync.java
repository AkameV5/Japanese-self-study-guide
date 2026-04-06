package com.example.japanese_self_study_guide.cache;

import android.util.Log;

import com.example.japanese_self_study_guide.DB;
import com.example.japanese_self_study_guide.audio.model.AudioExerciseModel;
import com.example.japanese_self_study_guide.audio.model.AudioModel;
import com.example.japanese_self_study_guide.dictionary.Word;
import com.example.japanese_self_study_guide.grammar.GrammarExercise;
import com.example.japanese_self_study_guide.grammar.GrammarRule;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaExerciseModel;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaItem;
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaExerciseModel;
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaItem;
import com.example.japanese_self_study_guide.kanji.KanjiExerciseModel;
import com.example.japanese_self_study_guide.kanji.KanjiModel;
import com.example.japanese_self_study_guide.texts_and_translation.ExerciseModel;
import com.example.japanese_self_study_guide.texts_and_translation.TextModel;
import com.example.japanese_self_study_guide.texts_and_translation.TranslationModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FirebaseContentSync {

    private static final String TAG = "FirebaseContentSync";
    private static final String AUDIO_BASE_URL =
            "https://raw.githubusercontent.com/AkameV5/Japanese-self-study-guide/master/audio/";

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final ContentDao dao = DB.getLocalDatabase().contentDao();

    public void warmAll() {
        syncTexts();
        syncTranslations();
        syncTextExercises();
        syncGrammarRules();
        syncGrammarExercises();
        syncAudio();
        syncAudioExercises();
        syncHiragana();
        syncHiraganaExercises();
        syncKatakana();
        syncKatakanaExercises();
        syncKanji();
        syncKanjiExercises();
        syncWords();
    }

    public Task<List<TextModel>> syncTexts() {
        return firestore.collection("Texts")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Texts sync failed");
                    }

                    List<TextModel> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        TextModel item = doc.toObject(TextModel.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearTexts();
                        dao.insertTexts(items);
                        return items;
                    });
                });
    }

    public Task<List<TranslationModel>> syncTranslations() {
        return firestore.collection("Translations_texts")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Translations sync failed");
                    }

                    List<TranslationModel> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        TranslationModel item = doc.toObject(TranslationModel.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearTranslations();
                        dao.insertTranslations(items);
                        return items;
                    });
                });
    }

    public Task<List<ExerciseModel>> syncTextExercises() {
        return firestore.collection("TextsExercises")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Text exercises sync failed");
                    }

                    List<ExerciseModel> items = new ArrayList<>();
                    for (var doc : task.getResult().getDocuments()) {
                        ExerciseModel item = doc.toObject(ExerciseModel.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearTextExercises();
                        dao.insertTextExercises(items);
                        return items;
                    });
                });
    }

    public Task<List<GrammarRule>> syncGrammarRules() {
        return firestore.collection("Grammar")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Grammar sync failed");
                    }

                    List<GrammarRule> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        GrammarRule item = doc.toObject(GrammarRule.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearGrammarRules();
                        dao.insertGrammarRules(items);
                        return items;
                    });
                });
    }

    public Task<List<GrammarExercise>> syncGrammarExercises() {
        return firestore.collection("GrammarExercises")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Grammar exercises sync failed");
                    }

                    List<GrammarExercise> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        GrammarExercise item = doc.toObject(GrammarExercise.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearGrammarExercises();
                        dao.insertGrammarExercises(items);
                        return items;
                    });
                });
    }

    public Task<List<AudioModel>> syncAudio() {
        return firestore.collection("Audio")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Audio sync failed");
                    }

                    List<AudioModel> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        AudioModel item = doc.toObject(AudioModel.class);
                        if (item == null) continue;
                        if (item.getAudioPath() != null && !item.getAudioPath().isEmpty()) {
                            item.setUrl(AUDIO_BASE_URL + item.getAudioPath());
                        }
                        items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearAudio();
                        dao.insertAudio(items);
                        return items;
                    });
                });
    }

    public Task<List<AudioExerciseModel>> syncAudioExercises() {
        return firestore.collection("AudioExercises")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Audio exercises sync failed");
                    }

                    List<AudioExerciseModel> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        AudioExerciseModel item = doc.toObject(AudioExerciseModel.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearAudioExercises();
                        dao.insertAudioExercises(items);
                        return items;
                    });
                });
    }

    public Task<List<HiraganaItem>> syncHiragana() {
        return firestore.collection("Hiragana")
                .orderBy("id")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Hiragana sync failed");
                    }

                    List<HiraganaItem> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        Long id = doc.getLong("id");
                        if (id == null) continue;
                        HiraganaItem item = new HiraganaItem();
                        item.setId(id.intValue());
                        item.setSymbol(doc.getString("symbol"));
                        item.setRomaji(doc.getString("romanji"));
                        item.setImageUrl(doc.getString("imageUrl"));
                        items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearHiragana();
                        dao.insertHiragana(items);
                        return items;
                    });
                });
    }

    public Task<List<HiraganaExerciseModel>> syncHiraganaExercises() {
        return firestore.collection("HiraganaExercises")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Hiragana exercises sync failed");
                    }

                    List<HiraganaExerciseModel> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        HiraganaExerciseModel item = doc.toObject(HiraganaExerciseModel.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearHiraganaExercises();
                        dao.insertHiraganaExercises(items);
                        return items;
                    });
                });
    }

    public Task<List<KatakanaItem>> syncKatakana() {
        return firestore.collection("Katakana")
                .orderBy("id")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Katakana sync failed");
                    }

                    List<KatakanaItem> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        Long id = doc.getLong("id");
                        if (id == null) continue;
                        KatakanaItem item = new KatakanaItem();
                        item.setId(id.intValue());
                        item.setSymbol(doc.getString("symbol"));
                        item.setRomaji(doc.getString("romanji"));
                        item.setImageUrl(doc.getString("imageUrl"));
                        items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearKatakana();
                        dao.insertKatakana(items);
                        return items;
                    });
                });
    }

    public Task<List<KatakanaExerciseModel>> syncKatakanaExercises() {
        return firestore.collection("KatakanaExercises")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Katakana exercises sync failed");
                    }

                    List<KatakanaExerciseModel> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        KatakanaExerciseModel item = doc.toObject(KatakanaExerciseModel.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearKatakanaExercises();
                        dao.insertKatakanaExercises(items);
                        return items;
                    });
                });
    }

    public Task<List<KanjiModel>> syncKanji() {
        return firestore.collection("Kanji")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Kanji sync failed");
                    }

                    List<KanjiModel> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        KanjiModel item = doc.toObject(KanjiModel.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearKanji();
                        dao.insertKanji(items);
                        return items;
                    });
                });
    }

    public Task<List<KanjiExerciseModel>> syncKanjiExercises() {
        return firestore.collection("KanjiExercises")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Kanji exercises sync failed");
                    }

                    List<KanjiExerciseModel> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        KanjiExerciseModel item = doc.toObject(KanjiExerciseModel.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearKanjiExercises();
                        dao.insertKanjiExercises(items);
                        return items;
                    });
                });
    }

    public Task<List<Word>> syncWords() {
        return firestore.collection("Words")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw failure(task.getException(), "Words sync failed");
                    }

                    List<Word> items = new ArrayList<>();
                    for (var doc : task.getResult()) {
                        Word item = doc.toObject(Word.class);
                        if (item != null) items.add(item);
                    }

                    return CacheTaskRunner.call(() -> {
                        dao.clearWords();
                        dao.insertWords(items);
                        return items;
                    });
                });
    }

    public void logWarmFailure(String label, Exception exception) {
        Log.w(TAG, label, exception);
    }

    private Exception failure(Exception original, String fallbackMessage) {
        return original != null ? original : new IllegalStateException(fallbackMessage);
    }
}
