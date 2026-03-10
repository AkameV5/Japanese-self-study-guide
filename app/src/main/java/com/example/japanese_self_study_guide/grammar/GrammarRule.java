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

    public int getId()           { return id; }
    public String getStructure() { return structure   != null ? structure   : ""; }
    public String getExplanation(){ return explanation != null ? explanation : ""; }
    public String getExample()   { return example     != null ? example     : ""; }
    public String getTranslation(){ return translation != null ? translation : ""; }

    public void setId(int id)                       { this.id = id; }
    public void setStructure(String s)              { this.structure = s; }
    public void setExplanation(String e)            { this.explanation = e; }
    public void setExample(String e)                { this.example = e; }
    public void setTranslation(String t)            { this.translation = t; }
}