package com.example.japanese_self_study_guide.grammar;

public class GrammarRule {
    private int id;
    private String structure;
    private String explanation;
    private String example;
    private String translation;

    public GrammarRule() {}

    public GrammarRule(int id, String structure, String explanation,
                       String example, String translation) {
        this.id = id;
        this.structure = structure;
        this.explanation = explanation;
        this.example = example;
        this.translation = translation;
    }

    public int getId() { return id; }
    public String getStructure() { return structure; }
    public String getExplanation() { return explanation; }
    public String getExample() { return example; }
    public String getTranslation() { return translation; }

    public void setId(int id) { this.id = id; }
    public void setStructure(String structure) { this.structure = structure; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setExample(String example) { this.example = example; }
    public void setTranslation(String translation) { this.translation = translation; }
}

