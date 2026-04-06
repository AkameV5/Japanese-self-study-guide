package com.example.japanese_self_study_guide.grammar;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "grammar_exercises")
public class GrammarExercise {
    @PrimaryKey
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

    public void setId(int id) { this.id = id; }
    public void setId_grammar(int id_grammar) { this.id_grammar = id_grammar; }
    public void setTask(String task) { this.task = task; }
    public void setRightAnswer(String rightAnswer) { this.rightAnswer = rightAnswer; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}
