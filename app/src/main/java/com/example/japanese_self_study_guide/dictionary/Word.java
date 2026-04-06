package com.example.japanese_self_study_guide.dictionary;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "words")
public class Word {
    @PrimaryKey
    public int id;
    public List<Integer> idKanji;
    public String word;
    public String reading;
    public String category;
    public String translation;

    public Word() {}
    public int getId() { return id; }
    public String getWord()        { return word        != null ? word        : ""; }
    public String getReading()     { return reading     != null ? reading     : ""; }
    public String getCategory()    { return category    != null ? category    : ""; }
    public String getTranslation() { return translation != null ? translation : ""; }
}
