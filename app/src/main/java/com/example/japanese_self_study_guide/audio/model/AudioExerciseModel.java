package com.example.japanese_self_study_guide.audio.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "audio_exercises")
public class AudioExerciseModel {
    @PrimaryKey
    private int id;
    private int audioId;
    private String question;
    private List<String> options;
    private int correctIndex;
    private String hint;

    public AudioExerciseModel() {}

    public int getId() { return id; }
    public int getAudioId() { return audioId; }
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public String getHint() { return hint; }

    public void setId(int id) { this.id = id; }
    public void setAudioId(int audioId) { this.audioId = audioId; }
    public void setQuestion(String question) { this.question = question; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
    public void setHint(String hint) { this.hint = hint; }
}
