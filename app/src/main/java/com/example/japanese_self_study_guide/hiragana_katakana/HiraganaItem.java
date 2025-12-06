package com.example.japanese_self_study_guide.hiragana_katakana;

public class HiraganaItem {
    private String symbol;
    private String romaji;
    private String imageUrl;
    private int id;

    public HiraganaItem() {}

    public HiraganaItem(String symbol, String romaji, String imageUrl, int id) {
        this.symbol = symbol;
        this.romaji = romaji;
        this.imageUrl = imageUrl;
        this.id = id;
    }

    public String getSymbol() { return symbol; }
    public String getRomaji() { return romaji; }
    public String getImageUrl() { return imageUrl; }
    public int getId() { return id; }
}
