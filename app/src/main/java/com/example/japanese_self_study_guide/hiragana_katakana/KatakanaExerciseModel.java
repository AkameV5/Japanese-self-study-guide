package com.example.japanese_self_study_guide.hiragana_katakana;

import java.util.List;

public class KatakanaExerciseModel {

    private int exerciseId;
    private int katakanaId;
    private String type;
    private String question;
    private String correctAnswer;
    private List<String> options;
    private String explanation;

    public KatakanaExerciseModel() {}

    public int getExerciseId() { return exerciseId; }
    public int getKatakanaId() { return katakanaId; }

    public String getType() { return type; }
    public String getQuestion() { return question; }
    public String getCorrectAnswer() { return correctAnswer; }
    public List<String> getOptions() { return options; }
    public String getExplanation() { return explanation; }
}

