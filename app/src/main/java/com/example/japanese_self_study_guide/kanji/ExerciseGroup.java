package com.example.japanese_self_study_guide.kanji;

public class ExerciseGroup {
    private String title;
    private int startId;
    private int endId;
    private int limit;

    public ExerciseGroup(String title, int startId, int endId, int limit) {
        this.title = title;
        this.startId = startId;
        this.endId = endId;
        this.limit = limit;
    }

    public String getTitle() { return title; }
    public int getStartId() { return startId; }
    public int getEndId() { return endId; }
    public int getLimit() { return limit; }
}
