package com.example.japanese_self_study_guide.audio.model;

public class AudioModel {
    private int id;
    private String name;
    private String description;
    private String difficulty;
    private String url;
    private String audioPath;

    public AudioModel() {}

    public AudioModel(int id, String name, String description, String difficulty, String audioPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.audioPath = audioPath;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDifficulty() {
        return difficulty != null ? difficulty : "";
    }
    public String getUrl() { return url; }
    public String getAudioPath() { return audioPath; }

    public void setUrl(String url) { this.url = url; }
}