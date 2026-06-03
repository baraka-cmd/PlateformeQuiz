<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- ============================================================
     FICHIER : views/admin/admin-dashboard.jsp
     RÔLE    : Tableau de bord administrateur AmijayQuiz
              - Statistiques globales (9 cartes)
              - Actions rapides
              - Dernieres inscriptions
              - Paiements en attente
     ============================================================ --%>

<%
    // Recuperation des attributs
    Integer totalUtilisateurs = (Integer) request.getAttribute("totalUtilisateurs");
    Integer totalJoueurs = (Integer) request.getAttribute("totalJoueurs");
    Integer totalCours = (Integer) request.getAttribute("totalCours");
    Integer totalQuestions = (Integer) request.getAttribute("totalQuestions");
    Integer totalQuiz = (Integer) request.getAttribute("totalQuiz");
    Integer totalPartiesJouees = (Integer) request.getAttribute("totalPartiesJouees");
    Integer paiementsEnAttente = (Integer) request.getAttribute("paiementsEnAttente");
    Integer paiementsValidesAujourdhui = (Integer) request.getAttribute("paiementsValidesAujourdhui");
    Integer utilisateursPremium = (Integer) request.getAttribute("utilisateursPremium");
    List<Map<String, Object>> dernieresInscriptions = (List<Map<String, Object>>) request.getAttribute("dernieresInscriptions");

    // Valeurs par defaut
    if (totalUtilisateurs == null) totalUtilisateurs = 0;
    if (totalJoueurs == null) totalJoueurs = 0;
    if (totalCours == null) totalCours = 0;
    if (totalQuestions == null) totalQuestions = 0;
    if (totalQuiz == null) totalQuiz = 0;
    if (totalPartiesJouees == null) totalPartiesJouees = 0;
    if (paiementsEnAttente == null) paiementsEnAttente = 0;
    if (paiementsValidesAujourdhui == null) paiementsValidesAujourdhui = 0;
    if (utilisateursPremium == null) utilisateursPremium = 0;
    if (dernieresInscriptions == null) dernieresInscriptions = new java.util.ArrayList<Map<String, Object>>();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new java.util.Locale("fr", "FR"));

    // Message de succes/erreur
    String messageSucces = (String) session.getAttribute("messageSucces");
    String messageErreur = (String) session.getAttribute("messageErreur");
    if (messageSucces != null) session.removeAttribute("messageSucces");
    if (messageErreur != null) session.removeAttribute("messageErreur");

    request.setAttribute("pageTitle", "Dashboard - Administration - AmijayQuiz");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-dashboard.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-dashboard-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <span class="breadcrumb-current">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard Admin</span>
        </span>
    </nav>

    <%-- ============================================================
         MESSAGES FLASH
         ============================================================ --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes">
            <i class="fas fa-check-circle"></i>
            <span><%= messageSucces %></span>
        </div>
    <% } %>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ============================================================
         EN-TETE DE BIENVENUE
         ============================================================ --%>
    <div class="admin-welcome-header">
        <div class="admin-welcome-left">
            <div class="admin-welcome-icon">
                <i class="fas fa-shield-alt"></i>
            </div>
            <div class="admin-welcome-text">
                <h1>Administration AmijayQuiz</h1>
                <p>Vue d'ensemble de la plateforme</p>
            </div>
        </div>
        <div class="admin-welcome-date">
            <i class="far fa-calendar-alt"></i>
            <%= new SimpleDateFormat("EEEE d MMMM yyyy", new java.util.Locale("fr", "FR")).format(new java.util.Date()) %>
        </div>
    </div>

    <%-- ============================================================
         CARTES STATISTIQUES (9 cartes)
         ============================================================ --%>
    <div class="admin-stats-grid">
        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-users"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalUtilisateurs %></span>
                <span class="admin-stat-label">Utilisateurs totaux</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                <i class="fas fa-user-check"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalJoueurs %></span>
                <span class="admin-stat-label">Joueurs actifs</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-crown"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= utilisateursPremium %></span>
                <span class="admin-stat-label">Comptes Premium</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                <i class="fas fa-book"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalCours %></span>
                <span class="admin-stat-label">Cours disponibles</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                <i class="fas fa-question-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalQuestions %></span>
                <span class="admin-stat-label">Questions quiz</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                <i class="fas fa-list-check"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalQuiz %></span>
                <span class="admin-stat-label">Quiz crees</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-play-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalPartiesJouees %></span>
                <span class="admin-stat-label">Parties jouees</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #f5576c, #e0485c);">
                <i class="fas fa-clock"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= paiementsEnAttente %></span>
                <span class="admin-stat-label">Paiements en attente</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #00bcd4, #0097a7);">
                <i class="fas fa-check-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= paiementsValidesAujourdhui %></span>
                <span class="admin-stat-label">Valides aujourd'hui</span>
            </div>
        </div>
    </div>

    <%-- ============================================================
         ACTIONS RAPIDES
         ============================================================ --%>
    <div class="admin-actions-section">
        <h2><i class="fas fa-bolt"></i> Actions rapides</h2>
        <div class="admin-actions-grid">
            <a href="${pageContext.request.contextPath}/admin/paiements" class="admin-action-card">
                <div class="admin-action-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                    <i class="fas fa-money-bill-wave"></i>
                </div>
                <div class="admin-action-text">
                    <strong>Paiements en attente</strong>
                    <span><%= paiementsEnAttente %> demande<%= paiementsEnAttente > 1 ? "s" : "" %></span>
                </div>
                <i class="fas fa-arrow-right admin-action-arrow"></i>
            </a>

            <a href="${pageContext.request.contextPath}/admin/questions" class="admin-action-card">
                <div class="admin-action-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                    <i class="fas fa-question-circle"></i>
                </div>
                <div class="admin-action-text">
                    <strong>Gerer les questions</strong>
                    <span><%= totalQuestions %> questions</span>
                </div>
                <i class="fas fa-arrow-right admin-action-arrow"></i>
            </a>

            <a href="${pageContext.request.contextPath}/admin/quiz" class="admin-action-card">
                <div class="admin-action-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                    <i class="fas fa-list-check"></i>
                </div>
                <div class="admin-action-text">
                    <strong>Gerer les quiz</strong>
                    <span><%= totalQuiz %> quiz</span>
                </div>
                <i class="fas fa-arrow-right admin-action-arrow"></i>
            </a>

            <a href="${pageContext.request.contextPath}/admin/resultats" class="admin-action-card">
                <div class="admin-action-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                    <i class="fas fa-chart-bar"></i>
                </div>
                <div class="admin-action-text">
                    <strong>Voir les resultats</strong>
                    <span><%= totalPartiesJouees %> parties jouees</span>
                </div>
                <i class="fas fa-arrow-right admin-action-arrow"></i>
            </a>
        </div>
    </div>

    <%-- ============================================================
         DEUX COLONNES : INSCRIPTIONS + PAIEMENTS
         ============================================================ --%>
    <div class="admin-two-columns">
        
        <%-- Dernieres inscriptions --%>
        <div class="admin-column">
            <div class="admin-column-header">
                <h3><i class="fas fa-user-plus"></i> Dernieres inscriptions</h3>
            </div>
            <div class="admin-column-body">
                <% if (dernieresInscriptions != null && !dernieresInscriptions.isEmpty()) {
                    for (Map<String, Object> insc : dernieresInscriptions) {
                        String pseudo = (String) insc.get("pseudo");
                        String email = (String) insc.get("email");
                        Timestamp dateIns = (Timestamp) insc.get("date_inscription");
                        String dateFormatee = dateIns != null ? sdf.format(dateIns) : "N/A";
                %>
                    <div class="admin-list-item">
                        <div class="admin-list-avatar">
                            <i class="fas fa-user"></i>
                        </div>
                        <div class="admin-list-info">
                            <strong><%= pseudo %></strong>
                            <span><%= email != null ? email : "N/A" %></span>
                        </div>
                        <span class="admin-list-date"><%= dateFormatee %></span>
                    </div>
                <%  }
                } else { %>
                    <div class="admin-empty">
                        <i class="fas fa-inbox"></i>
                        <p>Aucune inscription recente</p>
                    </div>
                <% } %>
            </div>
        </div>

        <%-- Paiements en attente --%>
        <div class="admin-column">
            <div class="admin-column-header">
                <h3><i class="fas fa-clock"></i> Paiements en attente</h3>
                <a href="${pageContext.request.contextPath}/admin/paiements" class="admin-column-link">
                    Voir tout <i class="fas fa-arrow-right"></i>
                </a>
            </div>
            <div class="admin-column-body">
                <% if (paiementsEnAttente > 0) { %>
                    <div class="admin-alerte-paiement">
                        <div class="admin-alerte-icon">
                            <i class="fas fa-exclamation-circle"></i>
                        </div>
                        <div class="admin-alerte-content">
                            <strong><%= paiementsEnAttente %> demande<%= paiementsEnAttente > 1 ? "s" : "" %> en attente</strong>
                            <p>Ces demandes necessitent votre validation.</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/paiements" class="admin-alerte-btn">
                            Valider
                        </a>
                    </div>
                <% } else { %>
                    <div class="admin-empty">
                        <i class="fas fa-check-circle" style="color: #27ae60;"></i>
                        <p>Toutes les demandes sont traitees</p>
                    </div>
                <% } %>
            </div>
        </div>

    </div>

</div>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-dashboard.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>