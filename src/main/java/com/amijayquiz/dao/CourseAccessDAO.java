package com.amijayquiz.dao;

import com.amijayquiz.utils.ConnexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseAccessDAO {

    /**
     * Donne l'accès à un cours à un utilisateur après paiement réussi
     */
    public static boolean grantAccess(int userId, int courseId) {
        // Vérifier si l'accès existe déjà
        if (hasAccess(userId, courseId)) {
            return true;
        }
        
        String sql = "INSERT INTO user_course_access (user_id, course_id, granted_at, expires_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setNull(4, Types.TIMESTAMP);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Vérifie si un utilisateur a accès à un cours
     */
    public static boolean hasAccess(int userId, int courseId) {
        String sql = "SELECT COUNT(*) FROM user_course_access WHERE user_id = ? AND course_id = ? AND (expires_at IS NULL OR expires_at > NOW())";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupère tous les cours auxquels un utilisateur a accès
     */
    public static List<Integer> getAccessibleCourseIds(int userId) {
        List<Integer> courseIds = new ArrayList<>();
        String sql = "SELECT course_id FROM user_course_access WHERE user_id = ? AND (expires_at IS NULL OR expires_at > NOW())";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courseIds.add(rs.getInt("course_id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courseIds;
    }

    /**
     * Révoque l'accès à un cours
     */
    public static boolean revokeAccess(int userId, int courseId) {
        String sql = "DELETE FROM user_course_access WHERE user_id = ? AND course_id = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Compte le nombre total d'accès pour un cours
     */
    public static int countAccessByCourse(int courseId) {
        String sql = "SELECT COUNT(*) FROM user_course_access WHERE course_id = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}