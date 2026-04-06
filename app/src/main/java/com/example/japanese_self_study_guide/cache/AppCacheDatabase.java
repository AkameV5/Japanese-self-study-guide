package com.example.japanese_self_study_guide.cache;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

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

@Database(
        entities = {
                TextModel.class,
                TranslationModel.class,
                ExerciseModel.class,
                GrammarRule.class,
                GrammarExercise.class,
                AudioModel.class,
                AudioExerciseModel.class,
                HiraganaItem.class,
                HiraganaExerciseModel.class,
                KatakanaItem.class,
                KatakanaExerciseModel.class,
                KanjiModel.class,
                KanjiExerciseModel.class,
                Word.class
        },
        version = 3,
        exportSchema = false
)
@TypeConverters(ListConverters.class)
public abstract class AppCacheDatabase extends RoomDatabase {
    public abstract ContentDao contentDao();
}
