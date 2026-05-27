<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.services.CourseService" %>

<%
    // Récupération des attributs
    Cours cours = (Cours) request.getAttribute("cours");
    int courseId = cours != null ? cours.getId() : 0;
    String courseTitle = cours != null ? cours.getTitre() : "";
    double coursePrice = CourseService.getCoursePrice(courseId);
    String courseIcone = cours != null ? cours.getIconeTheme() : "fas fa-book";
    String courseCouleur = cours != null ? cours.getCouleurTheme() : "#667eea";

    if (cours == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    String extraHead = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/checkout.css\">";
    request.setAttribute("extraHead", extraHead);
%>

<%@ include file="../../header.jsp" %>

<div class="checkout-container">

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
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= courseId %>" class="breadcrumb-link">
            <i class="fas fa-sitemap"></i><span>Plan</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-credit-card"></i><span>Paiement</span>
        </span>
    </nav>

    <!-- En-tête -->
    <div class="checkout-header">
        <h1>Finaliser l'achat</h1>
        <p>Complétez votre paiement pour accéder au cours</p>
    </div>

    <!-- Layout 2 colonnes -->
    <div class="checkout-layout">
        
        <!-- Colonne gauche : Résumé du cours -->
        <div class="checkout-summary">
            <div class="summary-card">
                <div class="summary-icon" style="background: <%= courseCouleur %>20; color: <%= courseCouleur %>;">
                    <i class="<%= courseIcone %>"></i>
                </div>
                <h3><%= courseTitle %></h3>
                <div class="summary-price">
                    <span class="price-label">Prix</span>
                    <span class="price-value"><%= String.format("%.2f", coursePrice) %> USD</span>
                </div>
                <div class="summary-divider"></div>
                <div class="summary-total">
                    <span>Total à payer</span>
                    <strong><%= String.format("%.2f", coursePrice) %> USD</strong>
                </div>
                <div class="summary-features">
                    <div class="feature"><i class="fas fa-check-circle"></i> Accès à vie</div>
                    <div class="feature"><i class="fas fa-check-circle"></i> Tous les chapitres</div>
                    <div class="feature"><i class="fas fa-check-circle"></i> Certificat inclus</div>
                </div>
            </div>
        </div>
        
        <!-- Colonne droite : Formulaire de paiement -->
        <div class="checkout-form">
            <div class="form-card">
                <h3>Informations de paiement</h3>
                <p class="form-subtitle">Paiement sécurisé via PawaPay (Mobile Money)</p>
                
                <form id="paymentForm" action="${pageContext.request.contextPath}/joueur/paiement/creer" method="POST">
                    <input type="hidden" name="courseId" value="<%= courseId %>">
                    
                    <div class="form-group">
                        <label for="phoneNumber">
                            <i class="fas fa-phone-alt"></i>
                            Numéro Mobile Money
                        </label>
                        <input type="tel" 
                               id="phoneNumber" 
                               name="phoneNumber" 
                               placeholder="ex: 243XXXXXXXXX ou +243XXXXXXXXX"
                               required>
                        <small class="form-hint">Format : 243XXXXXXXXX ou +243XXXXXXXXX (Orange Money, M-Pesa, Airtel Money)</small>
                    </div>
                    
                    <div class="form-group payment-methods">
                        <label>Méthode de paiement acceptée</label>
                        <div class="methods-grid">
                            <div class="method-item">
                                <i class="fas fa-mobile-alt"></i>
                                <span>Orange Money</span>
                            </div>
                            <div class="method-item">
                                <i class="fas fa-mobile-alt"></i>
                                <span>M-Pesa</span>
                            </div>
                            <div class="method-item">
                                <i class="fas fa-mobile-alt"></i>
                                <span>Airtel Money</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-actions">
                        <button type="submit" class="btn-payer" id="submitBtn">
                            <i class="fas fa-lock"></i>
                            Payer <%= String.format("%.2f", coursePrice) %> USD
                        </button>
                        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= courseId %>" class="btn-annuler">
                            <i class="fas fa-times"></i>
                            Annuler
                        </a>
                    </div>
                </form>
                
                <div class="security-badge">
                    <i class="fas fa-shield-alt"></i>
                    <span>Paiement 100% sécurisé</span>
                </div>
            </div>
        </div>
        
    </div>
</div>

<script src="${pageContext.request.contextPath}/views/assets/js/checkout.js"></script>

<%@ include file="../../footer.jsp" %>