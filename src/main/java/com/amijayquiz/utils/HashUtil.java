package com.amijayquiz.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    
    /**
     * Hashe une chaîne de caractères avec l'algorithme SHA-256
     * 
     * @param motDePasse Le mot de passe en clair à hasher
     * @return Le hash SHA-256 sous forme de chaîne hexadécimale
     */
    public static String hasher(String motDePasse) {
        try {
            // Création de l'instance MessageDigest avec SHA-256
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            
            // Application du hashage sur le mot de passe converti en bytes
            byte[] hashBytes = messageDigest.digest(motDePasse.getBytes());
            
            // Conversion du tableau de bytes en chaîne hexadécimale
            StringBuilder hexString = new StringBuilder();
            
            for (byte hashByte : hashBytes) {
                // Conversion de chaque byte en hexadécimal sur 2 caractères
                String hex = Integer.toHexString(0xff & hashByte);
                
                // Ajout d'un zéro devant si nécessaire pour avoir 2 caractères
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Erreur : Algorithme SHA-256 introuvable !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}