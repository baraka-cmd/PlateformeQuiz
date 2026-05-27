package com.amijayquiz.services;

import com.amijayquiz.config.PawaPayConfig;
import com.amijayquiz.dao.PaymentDAO;
import com.amijayquiz.dao.CourseAccessDAO;
import com.amijayquiz.models.Payment;
import com.amijayquiz.models.PaymentStatus;
import com.amijayquiz.utils.ReferenceGenerator;
import com.google.gson.JsonObject;

public class PaymentService {

    public static Payment initiatePayment(int userId, int courseId, double amount, String phoneNumber) throws Exception {
        String reference = ReferenceGenerator.generateCoursePaymentReference(courseId, userId);
        
        Payment payment = new Payment(userId, courseId, reference, amount, "USD", phoneNumber);
        
        boolean saved = PaymentDAO.save(payment);
        if (!saved) {
            throw new Exception("Impossible de sauvegarder le paiement");
        }
        
        JsonObject pawapayResponse = PawaPayService.createPayment(
            reference, amount, phoneNumber, PawaPayConfig.getWebhookUrl()
        );
        
        int httpCode = pawapayResponse.get("httpCode").getAsInt();
        if (httpCode < 200 || httpCode >= 300) {
            PaymentDAO.updateStatusOnly(reference, PaymentStatus.FAILED);
            throw new Exception("Erreur PawaPay: " + pawapayResponse.toString());
        }
        
        return payment;
    }
    
    public static boolean processPaymentConfirmation(String reference, String transactionId, PaymentStatus status) {
        Payment payment = PaymentDAO.findByReference(reference);
        if (payment == null) {
            return false;
        }
        
        if (payment.isSuccess()) {
            return true;
        }
        
        boolean updated = PaymentDAO.updateStatus(reference, status, transactionId);
        if (!updated) {
            return false;
        }
        
        if (status.isSuccess()) {
            return CourseAccessDAO.grantAccess(payment.getUserId(), payment.getCourseId());
        }
        
        return true;
    }
    
    public static PaymentStatus checkPaymentStatus(String reference) throws Exception {
        Payment payment = PaymentDAO.findByReference(reference);
        if (payment == null) {
            return null;
        }
        
        if (!payment.isPending()) {
            return payment.getStatus();
        }
        
        JsonObject response = PawaPayService.getPaymentStatus(reference);
        int httpCode = response.get("httpCode").getAsInt();
        
        if (httpCode >= 200 && httpCode < 300) {
            String statusFromApi = response.get("status").getAsString();
            String transactionId = response.has("transactionId") ? response.get("transactionId").getAsString() : "";
            
            PaymentStatus newStatus = PaymentStatus.fromCode(statusFromApi);
            
            if (!payment.getStatus().equals(newStatus)) {
                PaymentDAO.updateStatus(reference, newStatus, transactionId);
                
                if (newStatus.isSuccess()) {
                    CourseAccessDAO.grantAccess(payment.getUserId(), payment.getCourseId());
                }
            }
            
            return newStatus;
        }
        
        return payment.getStatus();
    }
    
    public static boolean canAccessCourse(int userId, int courseId) {
        return CourseAccessDAO.hasAccess(userId, courseId);
    }
}