package com.example.japanese_self_study_guide.texts_and_translation;

import java.io.Serializable;
import java.util.List;

public class ExerciseModel implements Serializable {
    private int id;
    private int textId;
    private String type;
    private String question;
    private List<String> options;
    private int correctIndex;
    private String hint;

    public ExerciseModel() {}

    public int getId() { return id; }
    public int getTextId() { return textId; }
    public String getType() { return type; }
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public String getHint() { return hint; }

    public void setId(int id) { this.id = id; }
    public void setTextId(int textId) { this.textId = textId; }
    public void setType(String type) { this.type = type; }
    public void setQuestion(String question) { this.question = question; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
    public void setHint(String hint) { this.hint = hint; }
}
