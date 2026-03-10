package com.example.japanese_self_study_guide.grammar;

public class GrammarExercise {
    private int id;
    private int id_grammar;
    private String task;
    private String rightAnswer;
    private String explanation;
    private int difficulty;

    public GrammarExercise() {}

    public int getId()            { return id; }
    public int getId_grammar() { return id_grammar; }
    public String getTask()       { return task        != null ? task        : ""; }
    public String getRightAnswer(){ return rightAnswer  != null ? rightAnswer  : ""; }
    public String getExplanation(){ return explanation  != null ? explanation  : ""; }
    public int getDifficulty()    { return difficulty; }
}