package com.example.japanese_self_study_guide.audio;

public class AudioModel {
    private int id;
    private String name;
    private String description;
    private String difficulty;
    private String url;

    public AudioModel() {}

    public AudioModel(int id, String name, String description, String difficulty, String url) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.url = url;
    }

    public int getId() { return id; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public String getUrl() { return url; }
}
