// ============================================================
// FICHIER : src/main/java/com/LearnPath/models/QuestionExamen.java
// RÔLE    : Modèle représentant une question d'examen de module
//           Question à choix multiples (A, B, C, D)
//           avec une seule bonne réponse
// ============================================================

package com.learnpath.models;

/**
 * QuestionExamen - Question d'un examen de module
 * 
 * Chaque question est à choix multiples avec 4 propositions
 * (A, B, C, D) et une seule bonne réponse.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class QuestionExamen {

    // ===== ATTRIBUTS =====
    private int id;
    private int examenId;
    private int numero;
    private String question;
    private String proposition1;
    private String proposition2;
    private String proposition3;
    private String proposition4;
    private int bonneReponse;

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public QuestionExamen() {
        this.bonneReponse = 1;
    }

    /**
     * Constructeur pour création d'une question
     * 
     * @param examenId      ID de l'examen
     * @param numero        Numéro de la question dans l'examen
     * @param question      Texte de la question
     * @param proposition1  Proposition A
     * @param proposition2  Proposition B
     * @param proposition3  Proposition C
     * @param proposition4  Proposition D
     * @param bonneReponse  Numéro de la bonne réponse (1 à 4)
     */
    public QuestionExamen(int examenId, int numero, String question,
                          String proposition1, String proposition2,
                          String proposition3, String proposition4,
                          int bonneReponse) {
        this.examenId = examenId;
        this.numero = numero;
        this.question = question;
        this.proposition1 = proposition1;
        this.proposition2 = proposition2;
        this.proposition3 = proposition3;
        this.proposition4 = proposition4;
        this.bonneReponse = (bonneReponse >= 1 && bonneReponse <= 4) ? bonneReponse : 1;
    }

    /**
     * Constructeur complet avec ID
     * 
     * @param id            ID de la question
     * @param examenId      ID de l'examen
     * @param numero        Numéro de la question
     * @param question      Texte de la question
     * @param proposition1  Proposition A
     * @param proposition2  Proposition B
     * @param proposition3  Proposition C
     * @param proposition4  Proposition D
     * @param bonneReponse  Numéro de la bonne réponse (1 à 4)
     */
    public QuestionExamen(int id, int examenId, int numero, String question,
                          String proposition1, String proposition2,
                          String proposition3, String proposition4,
                          int bonneReponse) {
        this.id = id;
        this.examenId = examenId;
        this.numero = numero;
        this.question = question;
        this.proposition1 = proposition1;
        this.proposition2 = proposition2;
        this.proposition3 = proposition3;
        this.proposition4 = proposition4;
        this.bonneReponse = (bonneReponse >= 1 && bonneReponse <= 4) ? bonneReponse : 1;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamenId() {
        return examenId;
    }

    public void setExamenId(int examenId) {
        this.examenId = examenId;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
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
        if (bonneReponse >= 1 && bonneReponse <= 4) {
            this.bonneReponse = bonneReponse;
        }
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Récupère une proposition par son numéro (1 à 4)
     * 
     * @param numero Le numéro de la proposition (1=A, 2=B, 3=C, 4=D)
     * @return Le texte de la proposition, ou "" si numéro invalide
     */
    public String getProposition(int numero) {
        switch (numero) {
            case 1: return proposition1 != null ? proposition1 : "";
            case 2: return proposition2 != null ? proposition2 : "";
            case 3: return proposition3 != null ? proposition3 : "";
            case 4: return proposition4 != null ? proposition4 : "";
            default: return "";
        }
    }

    /**
     * Vérifie si une réponse est correcte
     * 
     * @param reponseJoueur Le numéro de la réponse du joueur (1 à 4)
     * @return true si la réponse est correcte
     */
    public boolean estCorrecte(int reponseJoueur) {
        return reponseJoueur == bonneReponse;
    }

    /**
     * Retourne la lettre de la bonne réponse (A, B, C ou D)
     * 
     * @return La lettre correspondant à la bonne réponse
     */
    public String getLettreBonneReponse() {
        switch (bonneReponse) {
            case 1: return "A";
            case 2: return "B";
            case 3: return "C";
            case 4: return "D";
            default: return "?";
        }
    }

    /**
     * Retourne la lettre correspondant à un numéro de réponse
     * 
     * @param numero Numéro de la réponse (1 à 4)
     * @return La lettre (A, B, C, D) ou "?" si invalide
     */
    public static String getLettre(int numero) {
        switch (numero) {
            case 1: return "A";
            case 2: return "B";
            case 3: return "C";
            case 4: return "D";
            default: return "?";
        }
    }

    /**
     * Retourne le texte de la bonne réponse
     * 
     * @return Le texte de la proposition correcte
     */
    public String getTexteBonneReponse() {
        return getProposition(bonneReponse);
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "QuestionExamen{" +
                "id=" + id +
                ", examenId=" + examenId +
                ", numero=" + numero +
                ", question='" + question + '\'' +
                ", bonneReponse=" + getLettreBonneReponse() +
                '}';
    }
}