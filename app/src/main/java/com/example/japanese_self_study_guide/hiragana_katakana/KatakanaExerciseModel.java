package com.example.japanese_self_study_guide.hiragana_katakana;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "katakana_exercises")
public class KatakanaExerciseModel {

    @PrimaryKey
    private int exerciseId;
    private int katakanaId;
    private String type;
    private String question;
    private String correctAnswer;
    private List<String> options;
    private String explanation;

    public KatakanaExerciseModel() {}

    @Ignore
    public KatakanaExerciseModel(int exerciseId, int katakanaId, String type,
                                 String question, String correctAnswer,
                                 List<String> options, String explanation) {
        this.exerciseId = exerciseId;
        this.katakanaId = katakanaId;
        this.type = type;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.explanation = explanation;
    }

    public int getExerciseId() { return exerciseId; }
    public int getKatakanaId() { return katakanaId; }

    public String getType() { return type; }
    public String getQuestion() { return question; }
    public String getCorrectAnswer() { return correctAnswer; }
    public List<String> getOptions() { return options; }
    public String getExplanation() { return explanation; }

    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    public void setKatakanaId(int katakanaId) { this.katakanaId = katakanaId; }
    public void setType(String type) { this.type = type; }
    public void setQuestion(String question) { this.question = question; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}

