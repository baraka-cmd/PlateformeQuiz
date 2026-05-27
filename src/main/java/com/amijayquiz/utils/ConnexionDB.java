package com.amijayquiz.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionDB {
    
    // Paramètres de connexion à la base de données
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "amijayquiz_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    // URL de connexion JDBC pour MySQL
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    
    // Objet connexion unique (pattern Singleton simple)
    private static Connection connexion = null;
    
    /**
     * Méthode qui retourne une connexion à la base de données
     * Si la connexion n'existe pas ou est fermée, elle en crée une nouvelle
     */
    public static Connection getConnexion() {
        if (connexion == null) {
            try {
                // Chargement du driver JDBC MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Établissement de la connexion
                connexion = DriverManager.getConnection(URL, USER, PASSWORD);
                
                System.out.println("Connexion à la base de données établie avec succès !");
                
            } catch (ClassNotFoundException e) {
                System.err.println("Erreur : Driver MySQL introuvable !");
                System.err.println("Message : " + e.getMessage());
                e.printStackTrace();
                
            } catch (SQLException e) {
                System.err.println("Erreur : Impossible de se connecter à la base de données !");
                System.err.println("Message : " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return connexion;
    }
    
    /**
     * Méthode pour fermer la connexion
     */
    public static void fermerConnexion() {
        if (connexion != null) {
            try {
                connexion.close();
                connexion = null;
                System.out.println("Connexion à la base de données fermée avec succès !");
                
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion !");
                System.err.println("Message : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Méthode pour tester si la connexion est active
     */
    public static boolean testerConnexion() {
        try {
            Connection test = getConnexion();
            if (test != null && !test.isClosed()) {
                System.out.println("Test de connexion : RÉUSSI");
                return true;
            } else {
                System.out.println("Test de connexion : ÉCHEC (connexion null ou fermée)");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Test de connexion : ÉCHEC");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}