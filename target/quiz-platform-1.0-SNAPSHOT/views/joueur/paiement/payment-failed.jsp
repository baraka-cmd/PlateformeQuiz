<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Payment" %>
<%@ page import="com.amijayquiz.services.CourseService" %>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    Payment payment = (Payment) request.getAttribute("payment");
    String errorMessage = (String) request.getAttribute("errorMessage");
    
    int courseId = cours != null ? cours.getId() : 0;
    String courseTitle = cours != null ? cours.getTitre() : "";
    String courseIcone = cours != null ? cours.getIconeTheme() : "fas fa-book";
    String courseCouleur = cours != null ? cours.getCouleurTheme() : "#667eea";
    
    if (errorMessage == null) errorMessage = "Une erreur est survenue lors du traitement de votre paiement.";
    
    String extraHead = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/payment-failed.css\">";
    request.setAttribute("extraHead", extraHead);
%>

<%@ include file="../../header.jsp" %>

<div class="failed-container">
    
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
            <i class="fas fa-times-circle"></i><span>Échec du paiement</span>
        </span>
    </nav>
    
    <!-- Carte d'échec -->
    <div class="failed-card">
        <div class="failed-icon">
            <i class="fas fa-times-circle"></i>
        </div>
        
        <h1>Paiement échoué</h1>
        <p class="failed-subtitle">
            Nous n'avons pas pu traiter votre paiement.
        </p>
        
        <!-- Message d'erreur -->
        <div class="error-box">
            <i class="fas fa-exclamation-triangle"></i>
            <span><%= errorMessage %></span>
        </div>
        
        <!-- Récapitulatif -->
        <div class="recap-card">
            <div class="recap-icon" style="background: <%= courseCouleur %>20; color: <%= courseCouleur %>;">
                <i class="<%= courseIcone %>"></i>
            </div>
            <div class="recap-info">
                <h3><%= courseTitle %></h3>
                <% if (payment != null) { %>
                <div class="recap-details">
                    <span class="recap-label">Référence</span>
                    <span class="recap-value"><%= payment.getReference() %></span>
                </div>
                <div class="recap-details">
                    <span class="recap-label">Montant</span>
                    <span class="recap-value"><%= String.format("%.2f", payment.getAmount()) %> USD</span>
                </div>
                <% } %>
            </div>
        </div>
        
        <!-- Causes possibles -->
        <div class="causes-section">
            <h3><i class="fas fa-question-circle"></i> Causes possibles</h3>
            <ul class="causes-list">
                <li><i class="fas fa-ban"></i> Solde insuffisant sur votre compte Mobile Money</li>
                <li><i class="fas fa-ban"></i> Numéro de téléphone incorrect</li>
                <li><i class="fas fa-ban"></i> Problème de connexion avec le service de paiement</li>
                <li><i class="fas fa-ban"></i> Délai de paiement expiré</li>
                <li><i class="fas fa-ban"></i> Transaction annulée par l'utilisateur</li>
            </ul>
        </div>
        
        <!-- Solutions -->
        <div class="solutions-section">
            <h3><i class="fas fa-lightbulb"></i> Que faire ?</h3>
            <div class="solutions-grid">
                <div class="solution-item">
                    <span class="solution-number">1</span>
                    <span>Vérifiez votre solde Mobile Money</span>
                </div>
                <div class="solution-item">
                    <span class="solution-number">2</span>
                    <span>Confirmez votre numéro de téléphone</span>
                </div>
                <div class="solution-item">
                    <span class="solution-number">3</span>
                    <span>Réessayez dans quelques minutes</span>
                </div>
                <div class="solution-item">
                    <span class="solution-number">4</span>
                    <span>Contactez le support si le problème persiste</span>
                </div>
            </div>
        </div>
        
        <!-- Boutons d'action -->
        <div class="failed-actions">
            <a href="${pageContext.request.contextPath}/joueur/paiement/checkout?courseId=<%= courseId %>" class="btn-retry">
                <i class="fas fa-redo-alt"></i> Réessayer le paiement
            </a>
            <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= courseId %>" class="btn-cancel">
                <i class="fas fa-arrow-left"></i> Retour au cours
            </a>
        </div>
        
        <!-- Support -->
        <div class="support-box">
            <i class="fas fa-headset"></i>
            <div>
                <strong>Besoin d'aide ?</strong>
                <p>Contactez notre support : <a href="mailto:support@amijayquiz.com">support@amijayquiz.com</a></p>
            </div>
        </div>
    </div>
    
    <!-- Footer sécurisé -->
    <div class="secure-footer">
        <i class="fas fa-shield-alt"></i>
        <span>Transaction sécurisée par PawaPay</span>
    </div>
</div>

<script src="${pageContext.request.contextPath}/views/assets/js/payment-failed.js"></script>

<%@ include file="../../footer.jsp" %>