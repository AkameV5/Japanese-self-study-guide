package com.example.japanese_self_study_guide.audio;

import java.util.List;

public class AudioExerciseModel {
    private int id;
    private int audioId;
    private String question;
    private List<String> options;
    private int correctIndex;
    private String hint;

    public AudioExerciseModel() {} // Firebase needs empty constructor

    public int getId() { return id; }
    public int getAudioId() { return audioId; }
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public String getHint() { return hint; }
}
