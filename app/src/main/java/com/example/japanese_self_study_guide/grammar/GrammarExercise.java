package com.example.japanese_self_study_guide.grammar;

public class GrammarExercise {
    private int id;
    private int id_grammar;
    private String task;
    private String rightAnswer;
    private String explanation;
    private int difficulty;

    public GrammarExercise() {}

    public GrammarExercise(int id, int id_grammar, String task,
                           String rightAnswer, String explanation, int difficulty) {
        this.id = id;
        this.id_grammar = id_grammar;
        this.task = task;
        this.rightAnswer = rightAnswer;
        this.explanation = explanation;
        this.difficulty = difficulty;
    }

    public int getId() { return id; }
    public int getId_grammar() { return id_grammar; }
    public String getTask() { return task; }
    public String getRightAnswer() { return rightAnswer; }
    public String getExplanation() { return explanation; }
    public int getDifficulty() { return difficulty; }
}

