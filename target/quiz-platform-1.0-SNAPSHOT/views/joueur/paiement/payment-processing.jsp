<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.services.CourseService" %>

<%
    String reference = request.getParameter("reference");
    Integer courseId = (Integer) request.getAttribute("courseId");
    
    if (reference == null || reference.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }
    
    if (courseId == null) {
        courseId = 0;
    }
    
    String courseTitle = CourseService.getCourseTitle(courseId);
    
    String extraHead = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/payment-processing.css\">";
    request.setAttribute("extraHead", extraHead);
%>

<%@ include file="../../header.jsp" %>

<div class="processing-container">
    
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
            <i class="fas fa-spinner fa-pulse"></i><span>Paiement en cours</span>
        </span>
    </nav>
    
    <!-- Zone de chargement -->
    <div class="processing-card">
        <div class="processing-icon">
            <div class="spinner"></div>
            <i class="fas fa-mobile-alt"></i>
        </div>
        
        <h1>En attente du paiement</h1>
        <p class="processing-subtitle">
            Veuillez confirmer le paiement sur votre téléphone via Mobile Money
        </p>
        
        <div class="processing-details">
            <div class="detail-item">
                <span class="detail-label">Cours</span>
                <span class="detail-value"><%= courseTitle %></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Référence</span>
                <span class="detail-value"><%= reference %></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Statut</span>
                <span class="detail-value status-pending">
                    <i class="fas fa-clock"></i> En attente
                </span>
            </div>
        </div>
        
        <!-- Barre de progression -->
        <div class="progress-bar">
            <div class="progress-fill" id="progressFill"></div>
        </div>
        
        <p class="processing-timeout">
            <i class="fas fa-hourglass-half"></i>
            Temps restant : <span id="countdown">05:00</span>
        </p>
        
        <div class="processing-actions">
            <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= courseId %>" class="btn-cancel">
                <i class="fas fa-times"></i> Annuler
            </a>
            <button type="button" class="btn-refresh" id="refreshBtn" style="display: none;">
                <i class="fas fa-sync-alt"></i> Vérifier à nouveau
            </button>
        </div>
        
        <div class="processing-help">
            <i class="fas fa-question-circle"></i>
            <span>Vous n'avez pas reçu la notification ? Vérifiez que votre numéro est correct.</span>
        </div>
    </div>
    
    <!-- Message d'erreur -->
    <div class="error-message" id="errorMessage" style="display: none;">
        <i class="fas fa-exclamation-triangle"></i>
        <div>
            <strong>Problème de connexion</strong>
            <p>Impossible de vérifier le statut du paiement. Vérifiez votre connexion.</p>
        </div>
    </div>
    
    <!-- Variables pour JS -->
    <input type="hidden" id="referenceValue" value="<%= reference %>">
    <input type="hidden" id="courseIdValue" value="<%= courseId %>">
</div>

<script src="${pageContext.request.contextPath}/views/assets/js/payment-processing.js"></script>

<%@ include file="../../footer.jsp" %>