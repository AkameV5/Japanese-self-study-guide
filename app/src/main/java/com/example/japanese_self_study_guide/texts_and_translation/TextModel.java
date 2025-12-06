package com.example.japanese_self_study_guide.texts_and_translation;

import java.util.List;

public class TextModel {
    private int id;
    private String title;
    private String author;
    private String difficultyLevel;
    private List<String> sentences;

    public TextModel() {}

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public List<String> getSentences() { return sentences; }
}
