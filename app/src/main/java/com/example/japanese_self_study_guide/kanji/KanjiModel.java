package com.example.japanese_self_study_guide.kanji;

import java.util.List;

public class KanjiModel {
    private double  id;
    private String kanji;
    private String meaning;
    private List<String> onYomi;
    private List<String> kunYomi;
    private int jlpt;
    private String category;

    public KanjiModel() {}

    public double getId() { return id; }
    public String getKanji() { return kanji; }
    public String getMeaning() { return meaning; }
    public List<String> getOnYomi() { return onYomi; }
    public List<String> getKunYomi() { return kunYomi; }
    public int getJlpt() { return jlpt; }
    public String getCategory() { return category; }
}
