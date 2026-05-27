<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Payment" %>
<%@ page import="com.amijayquiz.services.CourseService" %>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    Payment payment = (Payment) request.getAttribute("payment");
    
    int courseId = cours != null ? cours.getId() : 0;
    String courseTitle = cours != null ? cours.getTitre() : "";
    String courseIcone = cours != null ? cours.getIconeTheme() : "fas fa-book";
    String courseCouleur = cours != null ? cours.getCouleurTheme() : "#667eea";
    
    if (cours == null || payment == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }
    
    String extraHead = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/payment-success.css\">";
    request.setAttribute("extraHead", extraHead);
%>

<%@ include file="../../header.jsp" %>

<div class="success-container">
    
    <!-- Breadcrumb -->
    <nav class="breadcrumb">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-check-circle"></i><span>Confirmation</span>
        </span>
    </nav>
    
    <!-- Carte de succès -->
    <div class="success-card">
        <div class="success-icon">
            <i class="fas fa-check-circle"></i>
        </div>
        
        <h1>Paiement réussi !</h1>
        <p class="success-subtitle">
            Félicitations ! Vous avez maintenant accès au cours.
        </p>
        
        <!-- Récapitulatif -->
        <div class="recap-card">
            <div class="recap-icon" style="background: <%= courseCouleur %>20; color: <%= courseCouleur %>;">
                <i class="<%= courseIcone %>"></i>
            </div>
            <div class="recap-info">
                <h3><%= courseTitle %></h3>
                <div class="recap-details">
                    <span class="recap-label">Référence transaction</span>
                    <span class="recap-value"><%= payment.getReference() %></span>
                </div>
                <div class="recap-details">
                    <span class="recap-label">Montant payé</span>
                    <span class="recap-value"><%= String.format("%.2f", payment.getAmount()) %> USD</span>
                </div>
                <div class="recap-details">
                    <span class="recap-label">Date</span>
                    <span class="recap-value"><%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(payment.getUpdatedAt()) %></span>
                </div>
            </div>
        </div>
        
        <!-- Ce que vous avez débloqué -->
        <div class="unlocked-section">
            <h3><i class="fas fa-gift"></i> Ce que vous avez débloqué</h3>
            <div class="unlocked-grid">
                <div class="unlocked-item">
                    <i class="fas fa-infinity"></i>
                    <span>Accès à vie</span>
                </div>
                <div class="unlocked-item">
                    <i class="fas fa-file-alt"></i>
                    <span>Tous les chapitres</span>
                </div>
                <div class="unlocked-item">
                    <i class="fas fa-quiz"></i>
                    <span>Quiz et examens</span>
                </div>
                <div class="unlocked-item">
                    <i class="fas fa-certificate"></i>
                    <span>Certificat inclus</span>
                </div>
            </div>
        </div>
        
        <!-- Boutons d'action -->
        <div class="success-actions">
            <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= courseId %>" class="btn-access">
                <i class="fas fa-play-circle"></i> Accéder au cours
            </a>
            <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-dashboard">
                <i class="fas fa-tachometer-alt"></i> Retour au tableau de bord
            </a>
        </div>
        
        <!-- Message supplémentaire -->
        <div class="info-message">
            <i class="fas fa-envelope"></i>
            <span>Un email de confirmation vous a été envoyé avec tous les détails.</span>
        </div>
    </div>
    
    <!-- Footer sécurisé -->
    <div class="secure-footer">
        <i class="fas fa-lock"></i>
        <span>Transaction sécurisée par PawaPay</span>
    </div>
</div>

<script src="${pageContext.request.contextPath}/views/assets/js/payment-success.js"></script>

<%@ include file="../../footer.jsp" %>