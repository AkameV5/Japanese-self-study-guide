package com.example.japanese_self_study_guide.texts_and_translation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "text_translations")
public class TranslationModel {
    @PrimaryKey
    private int id;
    private int textId;
    private String translationTitle;
    private String translator;
    private List<String> sentences;

    public TranslationModel() {}

    public int getId() { return id; }
    public int getTextId() { return textId; }
    public String getTranslationTitle() { return translationTitle; }
    public String getTranslator() { return translator; }
    public List<String> getSentences() { return sentences; }

    public void setId(int id) { this.id = id; }
    public void setTextId(int textId) { this.textId = textId; }
    public void setTranslationTitle(String translationTitle) { this.translationTitle = translationTitle; }
    public void setTranslator(String translator) { this.translator = translator; }
    public void setSentences(List<String> sentences) { this.sentences = sentences; }
}
