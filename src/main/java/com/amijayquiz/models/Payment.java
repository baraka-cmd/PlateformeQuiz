package com.amijayquiz.models;

import java.util.Date;

/**
 * Modèle représentant un paiement
 */
public class Payment {
    
    private int id;
    private int userId;
    private int courseId;
    private String reference;
    private String transactionId;
    private double amount;
    private String currency;
    private PaymentStatus status;
    private String phoneNumber;
    private Date createdAt;
    private Date updatedAt;
    
    // Constructeur par défaut
    public Payment() {}
    
    // Constructeur pour création de paiement
    public Payment(int userId, int courseId, String reference, double amount, String currency, String phoneNumber) {
        this.userId = userId;
        this.courseId = courseId;
        this.reference = reference;
        this.amount = amount;
        this.currency = currency;
        this.phoneNumber = phoneNumber;
        this.status = PaymentStatus.PENDING;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Méthodes utilitaires
    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }
    
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
    
    public String getFormattedAmount() {
        return String.format("%.2f %s", amount, currency);
    }
}