package com.learnpath.models;

public class Question {
    
    private int id;
    private String texteQuestion;
    private String proposition1;
    private String proposition2;
    private String proposition3;
    private String proposition4;
    private int bonneReponse;
    
    // Constructeur par défaut
    public Question() {
        this.bonneReponse = 1;
    }
    
    // Constructeur pour création
    public Question(String texteQuestion, String proposition1, String proposition2, 
                    String proposition3, String proposition4, int bonneReponse) {
        this.texteQuestion = texteQuestion;
        this.proposition1 = proposition1;
        this.proposition2 = proposition2;
        this.proposition3 = proposition3;
        this.proposition4 = proposition4;
        this.bonneReponse = bonneReponse;
    }
    
    // Constructeur complet
    public Question(int id, String texteQuestion, String proposition1, String proposition2, 
                    String proposition3, String proposition4, int bonneReponse) {
        this.id = id;
        this.texteQuestion = texteQuestion;
        this.proposition1 = proposition1;
        this.proposition2 = proposition2;
        this.proposition3 = proposition3;
        this.proposition4 = proposition4;
        this.bonneReponse = bonneReponse;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTexteQuestion() {
        return texteQuestion;
    }
    
    public void setTexteQuestion(String texteQuestion) {
        this.texteQuestion = texteQuestion;
    }
    
    public String getProposition1() {
        return proposition1;
    }
    
    public void setProposition1(String proposition1) {
        this.proposition1 = proposition1;
    }
    
    public String getProposition2() {
        return proposition2;
    }
    
    public void setProposition2(String proposition2) {
        this.proposition2 = proposition2;
    }
    
    public String getProposition3() {
        return proposition3;
    }
    
    public void setProposition3(String proposition3) {
        this.proposition3 = proposition3;
    }
    
    public String getProposition4() {
        return proposition4;
    }
    
    public void setProposition4(String proposition4) {
        this.proposition4 = proposition4;
    }
    
    public int getBonneReponse() {
        return bonneReponse;
    }
    
    public void setBonneReponse(int bonneReponse) {
        this.bonneReponse = bonneReponse;
    }
    
    /**
     * Récupère une proposition par son numéro (1 à 4)
     * 
     * @param numero Le numéro de la proposition
     * @return Le texte de la proposition
     */
    public String getProposition(int numero) {
        switch (numero) {
            case 1: return proposition1;
            case 2: return proposition2;
            case 3: return proposition3;
            case 4: return proposition4;
            default: return "";
        }
    }
    
    /**
     * Vérifie si une réponse est correcte
     * 
     * @param reponseJoueur Le numéro de la réponse du joueur
     * @return true si la réponse est correcte
     */
    public boolean estCorrecte(int reponseJoueur) {
        return reponseJoueur == bonneReponse;
    }
    
    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", texte='" + texteQuestion + '\'' +
                ", bonneReponse=" + bonneReponse +
                '}';
    }
}