package com.example.japanese_self_study_guide.texts_and_translation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "texts")
public class TextModel {
    @PrimaryKey
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

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setSentences(List<String> sentences) { this.sentences = sentences; }
}
