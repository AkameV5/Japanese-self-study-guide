package com.example.japanese_self_study_guide.audio.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "audio_items")
public class AudioModel {
    @PrimaryKey
    private int id;
    private String name;
    private String description;
    private String difficulty;
    private String url;
    private String audioPath;

    public AudioModel() {}

    @Ignore
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

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setUrl(String url) { this.url = url; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
}
