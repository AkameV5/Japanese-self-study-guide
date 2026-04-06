package com.example.japanese_self_study_guide.cache;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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

import java.util.List;

@Dao
public interface ContentDao {

    @Query("SELECT * FROM texts ORDER BY id")
    List<TextModel> getAllTexts();

    @Query("SELECT * FROM texts WHERE id = :textId LIMIT 1")
    TextModel getTextById(int textId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTexts(List<TextModel> items);

    @Query("DELETE FROM texts")
    void clearTexts();

    @Query("SELECT * FROM text_translations WHERE textId = :textId LIMIT 1")
    TranslationModel getTranslationByTextId(int textId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTranslations(List<TranslationModel> items);

    @Query("DELETE FROM text_translations")
    void clearTranslations();

    @Query("SELECT * FROM text_exercises WHERE textId = :textId ORDER BY id")
    List<ExerciseModel> getTextExercisesByTextId(int textId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTextExercises(List<ExerciseModel> items);

    @Query("DELETE FROM text_exercises")
    void clearTextExercises();

    @Query("SELECT * FROM grammar_rules ORDER BY id")
    List<GrammarRule> getAllGrammarRules();

    @Query("SELECT * FROM grammar_rules WHERE id = :grammarId LIMIT 1")
    GrammarRule getGrammarRuleById(int grammarId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGrammarRules(List<GrammarRule> items);

    @Query("DELETE FROM grammar_rules")
    void clearGrammarRules();

    @Query("SELECT * FROM grammar_exercises WHERE id_grammar = :grammarId ORDER BY id")
    List<GrammarExercise> getGrammarExercisesByGrammarId(int grammarId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGrammarExercises(List<GrammarExercise> items);

    @Query("DELETE FROM grammar_exercises")
    void clearGrammarExercises();

    @Query("SELECT * FROM audio_items ORDER BY id")
    List<AudioModel> getAllAudio();

    @Query("SELECT * FROM audio_items WHERE id = :audioId LIMIT 1")
    AudioModel getAudioById(int audioId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAudio(List<AudioModel> items);

    @Query("DELETE FROM audio_items")
    void clearAudio();

    @Query("SELECT * FROM audio_exercises WHERE audioId = :audioId ORDER BY id")
    List<AudioExerciseModel> getAudioExercisesByAudioId(int audioId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAudioExercises(List<AudioExerciseModel> items);

    @Query("DELETE FROM audio_exercises")
    void clearAudioExercises();

    @Query("SELECT * FROM hiragana_items ORDER BY id")
    List<HiraganaItem> getAllHiragana();

    @Query("SELECT * FROM hiragana_items WHERE id IN (:ids) ORDER BY id")
    List<HiraganaItem> getHiraganaByIds(List<Integer> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHiragana(List<HiraganaItem> items);

    @Query("DELETE FROM hiragana_items")
    void clearHiragana();

    @Query("SELECT * FROM hiragana_exercises WHERE hiraganaId IN (:ids) ORDER BY exerciseId")
    List<HiraganaExerciseModel> getHiraganaExercisesByIds(List<Integer> ids);

    @Query("SELECT * FROM hiragana_exercises ORDER BY exerciseId")
    List<HiraganaExerciseModel> getAllHiraganaExercises();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHiraganaExercises(List<HiraganaExerciseModel> items);

    @Query("DELETE FROM hiragana_exercises")
    void clearHiraganaExercises();

    @Query("SELECT * FROM katakana_items ORDER BY id")
    List<KatakanaItem> getAllKatakana();

    @Query("SELECT * FROM katakana_items WHERE id IN (:ids) ORDER BY id")
    List<KatakanaItem> getKatakanaByIds(List<Integer> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKatakana(List<KatakanaItem> items);

    @Query("DELETE FROM katakana_items")
    void clearKatakana();

    @Query("SELECT * FROM katakana_exercises WHERE katakanaId IN (:ids) ORDER BY exerciseId")
    List<KatakanaExerciseModel> getKatakanaExercisesByIds(List<Integer> ids);

    @Query("SELECT * FROM katakana_exercises ORDER BY exerciseId")
    List<KatakanaExerciseModel> getAllKatakanaExercises();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKatakanaExercises(List<KatakanaExerciseModel> items);

    @Query("DELETE FROM katakana_exercises")
    void clearKatakanaExercises();

    @Query("SELECT * FROM kanji_items ORDER BY id")
    List<KanjiModel> getAllKanji();

    @Query("SELECT * FROM kanji_items WHERE id IN (:ids) ORDER BY id")
    List<KanjiModel> getKanjiByIds(List<Double> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKanji(List<KanjiModel> items);

    @Query("DELETE FROM kanji_items")
    void clearKanji();

    @Query("SELECT * FROM kanji_exercises WHERE id_kanji >= :startId AND id_kanji <= :endId ORDER BY id")
    List<KanjiExerciseModel> getKanjiExercisesInRange(int startId, int endId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKanjiExercises(List<KanjiExerciseModel> items);

    @Query("DELETE FROM kanji_exercises")
    void clearKanjiExercises();

    @Query("SELECT * FROM words ORDER BY reading, word")
    List<Word> getAllWords();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWords(List<Word> items);

    @Query("DELETE FROM words")
    void clearWords();
}
