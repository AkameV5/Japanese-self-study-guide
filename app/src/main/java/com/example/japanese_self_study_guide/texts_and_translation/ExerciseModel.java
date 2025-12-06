package com.example.japanese_self_study_guide.texts_and_translation;

public class ExerciseModel {
    private int id;
    private int id_texta;
    private String question;
    private String correctAnswer;
    private String explanation;
    private int difficulty;

    public ExerciseModel() {}

    public int getId() { return id; }
    public int getId_texta() { return id_texta; }
    public String getQuestion() { return question; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
    public int getDifficulty() { return difficulty; }
}
