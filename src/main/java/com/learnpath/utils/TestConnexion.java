package com.learnpath.utils;

public class TestConnexion {
    
    public static void main(String[] args) {
        
        System.out.println("==========================================");
        System.out.println("  TEST DE CONNEXION À LA BASE DE DONNÉES");
        System.out.println("==========================================");
        System.out.println("");
        
        // Étape 1 : Tester la connexion
        System.out.println("Étape 1 : Tentative de connexion...");
        boolean statut = ConnexionDB.testerConnexion();
        
        System.out.println("");
        
        // Étape 2 : Afficher le résultat final
        if (statut) {
            System.out.println("==========================================");
            System.out.println("  CONNEXION ÉTABLIE AVEC SUCCÈS !");
            System.out.println("  La base de données 'LearnPath_db'");
            System.out.println("  est accessible.");
            System.out.println("==========================================");
        } else {
            System.out.println("==========================================");
            System.out.println("  ÉCHEC DE CONNEXION !");
            System.out.println("  Vérifiez que :");
            System.out.println("  1. XAMPP est démarré (Apache + MySQL)");
            System.out.println("  2. La base 'LearnPath_db' existe");
            System.out.println("  3. Les paramètres USER/PASSWORD sont corrects");
            System.out.println("==========================================");
        }
        
        System.out.println("");
        
        // Étape 3 : Fermer la connexion proprement
        System.out.println("Étape 2 : Fermeture de la connexion...");
        ConnexionDB.fermerConnexion();
        
        System.out.println("");
        System.out.println("Test terminé.");
    }
}