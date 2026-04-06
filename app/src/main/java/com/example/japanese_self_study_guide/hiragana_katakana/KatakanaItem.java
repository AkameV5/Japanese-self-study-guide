package com.example.japanese_self_study_guide.hiragana_katakana;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "katakana_items")
public class KatakanaItem {
    @PrimaryKey
    private int id;
    private String symbol;
    private String romaji;
    private String imageUrl;

    public KatakanaItem() {}

    @Ignore
    public KatakanaItem(int id, String symbol, String romaji, String imageUrl) {
        this.id = id;
        this.symbol = symbol;
        this.romaji = romaji;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getRomaji() { return romaji; }
    public String getImageUrl() { return imageUrl; }

    public void setId(int id) { this.id = id; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setRomaji(String romaji) { this.romaji = romaji; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
