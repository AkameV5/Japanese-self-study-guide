package com.example.japanese_self_study_guide.dictionary;

import java.util.List;

public class Word {
    public int id;  // обязательно public или private + геттер
    public List<Integer> idKanji;
    public String word;
    public String reading;
    public String category;
    public String translation;

    public Word() {} // нужен для Firestore

    // Геттеры
    public int getId() { return id; }
    public String getWord() { return word; }
    public String getReading() { return reading; }
    public String getCategory() { return category; }
    public String getTranslation() { return translation; }
}
