package com.example.japanese_self_study_guide.hiragana_katakana;

import java.util.List;

public class HiraganaExerciseModel {

    private int exerciseId;
    private int hiraganaId;
    private String type; // choose / write / reverse_write
    private String question;
    private String correctAnswer;
    private List<String> options;
    private String explanation;

    public HiraganaExerciseModel() {}

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
}
