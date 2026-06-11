package com.learnpath.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Générateur de références uniques pour les transactions
 */
public class ReferenceGenerator {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * Génère une référence unique au format: COURSE_{courseId}_USER_{userId}_{timestamp}_{uuid}
     */
    public static String generateCoursePaymentReference(int courseId, int userId) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("COURSE_%d_USER_%d_%s_%s", courseId, userId, timestamp, shortUuid);
    }
    
    /**
     * Génère une référence unique générique
     */
    public static String generateGenericReference() {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String uuid = UUID.randomUUID().toString();
        return String.format("PAY_%s_%s", timestamp, uuid.substring(0, 13));
    }
    
    /**
     * Extrait l'ID du cours depuis la référence
     */
    public static int extractCourseIdFromReference(String reference) {
        try {
            String[] parts = reference.split("_");
            for (int i = 0; i < parts.length; i++) {
                if ("COURSE".equals(parts[i]) && i + 1 < parts.length) {
                    return Integer.parseInt(parts[i + 1]);
                }
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }
    
    /**
     * Extrait l'ID utilisateur depuis la référence
     */
    public static int extractUserIdFromReference(String reference) {
        try {
            String[] parts = reference.split("_");
            for (int i = 0; i < parts.length; i++) {
                if ("USER".equals(parts[i]) && i + 1 < parts.length) {
                    return Integer.parseInt(parts[i + 1]);
                }
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }
    
    /**
     * Valide le format d'une référence
     */
    public static boolean isValidReference(String reference) {
        if (reference == null || reference.isEmpty()) {
            return false;
        }
        // Format: COURSE_X_USER_Y_timestamp_uuid
        return reference.matches("^COURSE_\\d+_USER_\\d+_\\d{14}_[a-f0-9]{8}$");
    }
}