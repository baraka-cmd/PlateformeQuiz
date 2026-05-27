package com.amijayquiz.dao;

import com.amijayquiz.models.Payment;
import com.amijayquiz.models.PaymentStatus;
import com.amijayquiz.utils.ConnexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public static boolean save(Payment payment) {
        String sql = "INSERT INTO payments (user_id, course_id, reference, amount, currency, phone_number, status, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payment.getUserId());
            stmt.setInt(2, payment.getCourseId());
            stmt.setString(3, payment.getReference());
            stmt.setDouble(4, payment.getAmount());
            stmt.setString(5, payment.getCurrency());
            stmt.setString(6, payment.getPhoneNumber());
            stmt.setString(7, payment.getStatus().getCode());
            stmt.setTimestamp(8, new Timestamp(payment.getCreatedAt().getTime()));
            stmt.setTimestamp(9, new Timestamp(payment.getUpdatedAt().getTime()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setId(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateStatus(String reference, PaymentStatus status, String transactionId) {
        String sql = "UPDATE payments SET status = ?, transaction_id = ?, updated_at = ? WHERE reference = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.getCode());
            stmt.setString(2, transactionId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, reference);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateStatusOnly(String reference, PaymentStatus status) {
        String sql = "UPDATE payments SET status = ?, updated_at = ? WHERE reference = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.getCode());
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, reference);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Payment findByReference(String reference) {
        String sql = "SELECT * FROM payments WHERE reference = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reference);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Payment> findByUserId(int userId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public static boolean hasUserPaidForCourse(int userId, int courseId) {
        String sql = "SELECT COUNT(*) FROM payments WHERE user_id = ? AND course_id = ? AND status = 'SUCCESS'";

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

    public static Payment findLastPaymentByUserAndCourse(int userId, int courseId) {
        String sql = "SELECT * FROM payments WHERE user_id = ? AND course_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getUserIdByReference(String reference) {
        String sql = "SELECT user_id FROM payments WHERE reference = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reference);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getCourseIdByReference(String reference) {
        String sql = "SELECT course_id FROM payments WHERE reference = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reference);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("course_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id"));
        payment.setUserId(rs.getInt("user_id"));
        payment.setCourseId(rs.getInt("course_id"));
        payment.setReference(rs.getString("reference"));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setCurrency(rs.getString("currency"));
        payment.setPhoneNumber(rs.getString("phone_number"));
        payment.setStatus(PaymentStatus.fromCode(rs.getString("status")));
        payment.setCreatedAt(rs.getTimestamp("created_at"));
        payment.setUpdatedAt(rs.getTimestamp("updated_at"));
        return payment;
    }

    public static int countSuccessfulPayments() {
        String sql = "SELECT COUNT(*) FROM payments WHERE status = 'SUCCESS'";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getTotalRevenue() {
        String sql = "SELECT SUM(amount) FROM payments WHERE status = 'SUCCESS'";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}