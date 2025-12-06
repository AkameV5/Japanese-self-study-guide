package com.example.japanese_self_study_guide.texts_and_translation;

import java.util.List;

public class TranslationModel {
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
}
