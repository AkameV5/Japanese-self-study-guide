package com.example.japanese_self_study_guide.kanji;

import java.util.List;

public class KanjiExerciseModel {

    private int id;
    private int id_kanji;
    private String question;
    private List<String> options;
    private List<String> answer;
    private String explanation;
    private int difficulty;

    // ✅ Пустой конструктор — обязателен для Firestore & Gson
    public KanjiExerciseModel() {}

    public KanjiExerciseModel(int id, int id_kanji, String question,
                              List<String> options, List<String> answer,
                              String explanation, int difficulty) {
        this.id = id;
        this.id_kanji = id_kanji;
        this.question = question;
        this.options = options;
        this.answer = answer;
        this.explanation = explanation;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public int getId_kanji() {
        return id_kanji;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public List<String> getAnswer() {
        return answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId_kanji(int id_kanji) {
        this.id_kanji = id_kanji;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
