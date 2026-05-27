package com.amijayquiz.services;

import com.amijayquiz.dao.CoursDAO;

/**
 * Service pour la gestion des cours
 */
public class CourseService {

    /**
     * Récupère le prix d'un cours
     * À adapter selon ta logique métier
     */
    public static double getCoursePrice(int courseId) {
        // TODO: Récupérer le prix depuis la base de données
        // Pour l'instant, prix par défaut
        switch (courseId) {
            case 1: return 29.99;
            case 2: return 49.99;
            case 3: return 39.99;
            default: return 19.99;
        }
    }
    
    /**
     * Vérifie si un cours est payant
     */
    public static boolean isPaidCourse(int courseId) {
        // TODO: Vérifier dans la base si le cours est payant
        return getCoursePrice(courseId) > 0;
    }
    
    /**
     * Récupère le titre d'un cours
     */
    public static String getCourseTitle(int courseId) {
        // TODO: Récupérer depuis la base
        CoursDAO dao = new CoursDAO();
        com.amijayquiz.models.Cours cours = dao.trouverParId(courseId);
        return cours != null ? cours.getTitre() : "Cours";
    }
}