package com.example.japanese_self_study_guide.kanji;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "kanji_items")
public class KanjiModel {
    @PrimaryKey
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

    public void setId(double id) { this.id = id; }
    public void setKanji(String kanji) { this.kanji = kanji; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public void setOnYomi(List<String> onYomi) { this.onYomi = onYomi; }
    public void setKunYomi(List<String> kunYomi) { this.kunYomi = kunYomi; }
    public void setJlpt(int jlpt) { this.jlpt = jlpt; }
    public void setCategory(String category) { this.category = category; }
}
