package com.example.japanese_self_study_guide.hiragana_katakana;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "hiragana_exercises")
public class HiraganaExerciseModel {

    @PrimaryKey
    private int exerciseId;
    private int hiraganaId;
    private String type;
    private String question;
    private String correctAnswer;
    private List<String> options;
    private String explanation;

    public HiraganaExerciseModel() {}

    @Ignore
    public HiraganaExerciseModel(int exerciseId, int hiraganaId, String type,
                                 String question, String correctAnswer, 
                                 List<String> options, String explanation) {
        this.exerciseId = exerciseId;
        this.hiraganaId = hiraganaId;
        this.type = type;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.explanation = explanation;
    }

    public int getExerciseId() { return exerciseId; }
    public int getHiraganaId() { return hiraganaId; }
    public String getType() { return type; }
    public String getQuestion() { return question; }
    public String getCorrectAnswer() { return correctAnswer; }
    public List<String> getOptions() { return options; }
    public String getExplanation() { return explanation; }

    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    public void setHiraganaId(int hiraganaId) { this.hiraganaId = hiraganaId; }
    public void setType(String type) { this.type = type; }
    public void setQuestion(String question) { this.question = question; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
