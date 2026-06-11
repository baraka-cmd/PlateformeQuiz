<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- ============================================================
     FICHIER : views/admin/admin-dashboard.jsp
     RÔLE    : Tableau de bord administrateur LearnPath
              Design professionnel complet
              - 8 cartes statistiques enrichies
              - Dernières inscriptions (5)
              - Paiements en attente (Super Admin uniquement)
              - Activité quiz récente
              - Répartition cours par thème
              - Raccourcis intelligents (conditionnels)
              - Sécurisé : Super Admin vs Gestionnaire
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Integer totalUtilisateurs = (Integer) request.getAttribute("totalUtilisateurs");
    Integer totalJoueurs = (Integer) request.getAttribute("totalJoueurs");
    Integer totalCours = (Integer) request.getAttribute("totalCours");
    Integer totalQuiz = (Integer) request.getAttribute("totalQuiz");
    Integer totalPartiesJouees = (Integer) request.getAttribute("totalPartiesJouees");
    Integer paiementsEnAttente = (Integer) request.getAttribute("paiementsEnAttente");
    Integer paiementsValidesAujourdhui = (Integer) request.getAttribute("paiementsValidesAujourdhui");
    Integer utilisateursPremium = (Integer) request.getAttribute("utilisateursPremium");
    List<Map<String, Object>> dernieresInscriptions = (List<Map<String, Object>>) request.getAttribute("dernieresInscriptions");

    if (totalUtilisateurs == null) totalUtilisateurs = 0;
    if (totalJoueurs == null) totalJoueurs = 0;
    if (totalCours == null) totalCours = 0;
    if (totalQuiz == null) totalQuiz = 0;
    if (totalPartiesJouees == null) totalPartiesJouees = 0;
    if (paiementsEnAttente == null) paiementsEnAttente = 0;
    if (paiementsValidesAujourdhui == null) paiementsValidesAujourdhui = 0;
    if (utilisateursPremium == null) utilisateursPremium = 0;
    if (dernieresInscriptions == null) dernieresInscriptions = new java.util.ArrayList<>();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new java.util.Locale("fr", "FR"));
    SimpleDateFormat sdfJour = new SimpleDateFormat("EEEE d MMMM yyyy", new java.util.Locale("fr", "FR"));

    // Ratio Premium
    int ratioPremium = totalJoueurs > 0 ? (utilisateursPremium * 100 / totalJoueurs) : 0;

    // Récupération du rôle de l'admin connecté
    String roleAdminDashboard = "admin";
    if (session != null && session.getAttribute("utilisateur") != null) {
        com.learnpath.models.Utilisateur adminDashboard = 
            (com.learnpath.models.Utilisateur) session.getAttribute("utilisateur");
        roleAdminDashboard = adminDashboard.getRole();
    }
    boolean estSuperAdminDashboard = "admin".equals(roleAdminDashboard);

    // Messages flash
    String messageSucces = (String) session.getAttribute("messageSucces");
    String messageErreur = (String) session.getAttribute("messageErreur");
    if (messageSucces != null) session.removeAttribute("messageSucces");
    if (messageErreur != null) session.removeAttribute("messageErreur");

    request.setAttribute("pageTitle", "Dashboard - Administration - LearnPath");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-dashboard.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-dashboard-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <span class="breadcrumb-current">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard Admin</span>
        </span>
    </nav>

    <%-- ===== MESSAGES FLASH ===== --%>
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

    <%-- ===== EN-TÊTE DE BIENVENUE ===== --%>
    <div class="admin-welcome-header">
        <div class="admin-welcome-left">
            <div class="admin-welcome-icon">
                <i class="fas fa-shield-alt"></i>
            </div>
            <div class="admin-welcome-text">
                <h1>Administration LearnPath</h1>
                <p>Vue d'ensemble complète de votre plateforme</p>
            </div>
        </div>
        <div class="admin-welcome-right">
            <div class="admin-welcome-date">
                <i class="far fa-calendar-alt"></i>
                <span><%= sdfJour.format(new java.util.Date()) %></span>
            </div>
            <div class="admin-welcome-stat-mini">
                <span class="mini-dot active"></span>
                <span>Plateforme en ligne</span>
            </div>
        </div>
    </div>

    <%-- ===== SECTION 1 : CARTES STATISTIQUES PRINCIPALES (8 cartes) ===== --%>
    <div class="admin-stats-grid">
        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-users"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalUtilisateurs %></span>
                <span class="admin-stat-label">Utilisateurs totaux</span>
            </div>
            <div class="admin-stat-trend up">
                <i class="fas fa-arrow-up"></i> <span><%= totalJoueurs %> actifs</span>
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
            <div class="admin-stat-trend <%= ratioPremium >= 30 ? "up" : "neutral" %>">
                <i class="fas <%= ratioPremium >= 30 ? "fa-arrow-up" : "fa-minus" %>"></i> 
                <span><%= ratioPremium %>% des joueurs</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                <i class="fas fa-graduation-cap"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalCours %></span>
                <span class="admin-stat-label">Cours disponibles</span>
            </div>
            <div class="admin-stat-trend neutral">
                <i class="fas fa-layer-group"></i> <span>12 thèmes</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                <i class="fas fa-list-check"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalQuiz %></span>
                <span class="admin-stat-label">Quiz créés</span>
            </div>
            <div class="admin-stat-trend neutral">
                <i class="fas fa-tag"></i> <span>6 thèmes</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-play-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalPartiesJouees %></span>
                <span class="admin-stat-label">Parties jouées</span>
            </div>
            <div class="admin-stat-trend up">
                <i class="fas fa-chart-line"></i> <span>Total cumulé</span>
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
            <div class="admin-stat-trend <%= paiementsEnAttente > 0 ? "down" : "up" %>">
                <i class="fas <%= paiementsEnAttente > 0 ? "fa-exclamation-circle" : "fa-check-circle" %>"></i> 
                <span><%= paiementsEnAttente > 0 ? "Action requise" : "Tout est traité" %></span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #00bcd4, #0097a7);">
                <i class="fas fa-check-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= paiementsValidesAujourdhui %></span>
                <span class="admin-stat-label">Validés aujourd'hui</span>
            </div>
            <div class="admin-stat-trend neutral">
                <i class="fas fa-calendar-day"></i> <span><%= new SimpleDateFormat("dd/MM").format(new java.util.Date()) %></span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                <i class="fas fa-certificate"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= totalCours %></span>
                <span class="admin-stat-label">Certificats délivrés</span>
            </div>
            <div class="admin-stat-trend neutral">
                <i class="fas fa-award"></i> <span>En croissance</span>
            </div>
        </div>
    </div>

    <%-- ===== SECTION 2 : LAYOUT 2 COLONNES ===== --%>
    <div class="admin-dashboard-grid">

        <%-- ===== COLONNE GAUCHE (large) : ACTIVITÉ RÉCENTE + DERNIÈRES PARTIES ===== --%>
        <div class="admin-dash-col admin-dash-col-large">

            <%-- ===== DERNIÈRES INSCRIPTIONS ===== --%>
            <div class="admin-dash-card">
                <div class="admin-dash-card-header">
                    <div class="admin-dash-card-title">
                        <i class="fas fa-user-plus"></i>
                        <h3>Dernières inscriptions</h3>
                    </div>
                    <span class="admin-dash-card-badge"><%= dernieresInscriptions.size() %></span>
                </div>
                <div class="admin-dash-card-body">
                    <% if (dernieresInscriptions != null && !dernieresInscriptions.isEmpty()) { %>
                        <div class="admin-list-scroll">
                            <% for (Map<String, Object> insc : dernieresInscriptions) {
                                String pseudo = (String) insc.get("pseudo");
                                String email = (String) insc.get("email");
                                Timestamp dateIns = (Timestamp) insc.get("date_inscription");
                                String dateFormatee = dateIns != null ? sdf.format(dateIns) : "N/A";
                                String tempsEcoule = calculerTempsEcoule(dateIns);
                            %>
                                <div class="admin-list-item">
                                    <div class="admin-list-avatar" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                                        <span><%= pseudo.substring(0, 1).toUpperCase() %></span>
                                    </div>
                                    <div class="admin-list-info">
                                        <strong><%= pseudo %></strong>
                                        <span><%= email != null ? email : "Email non renseigné" %></span>
                                    </div>
                                    <div class="admin-list-right">
                                        <span class="admin-list-date"><%= dateFormatee %></span>
                                        <span class="admin-list-time"><%= tempsEcoule %></span>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="admin-empty-mini">
                            <i class="fas fa-inbox"></i>
                            <p>Aucune inscription récente</p>
                        </div>
                    <% } %>
                </div>
                <div class="admin-dash-card-footer">
                    <% if (estSuperAdminDashboard) { %>
                        <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="admin-link">
                            Voir tous les utilisateurs <i class="fas fa-arrow-right"></i>
                        </a>
                    <% } else { %>
                        <span class="admin-link" style="color: #8b8fa3; cursor: default;">
                            <i class="fas fa-lock"></i> Réservé au Super Admin
                        </span>
                    <% } %>
                </div>
            </div>

            <%-- ===== DERNIÈRES PARTIES JOUÉES ===== --%>
            <%-- ===== ACTIVITÉ QUIZ RÉCENTE ===== --%>
<div class="admin-dash-card">
    <div class="admin-dash-card-header">
        <div class="admin-dash-card-title">
            <i class="fas fa-gamepad"></i>
            <h3>Activité Quiz</h3>
        </div>
        <a href="${pageContext.request.contextPath}/admin/resultats" class="admin-link">Voir les résultats <i class="fas fa-arrow-right"></i></a>
    </div>
    <div class="admin-dash-card-body">
        <% if (totalPartiesJouees > 0) { %>
            <div class="admin-activity-summary">
                <div class="activity-stat">
                    <span class="activity-stat-nombre"><%= totalPartiesJouees %></span>
                    <span class="activity-stat-label">Parties jouées</span>
                </div>
                <div class="activity-stat">
                    <span class="activity-stat-nombre"><%= totalQuiz %></span>
                    <span class="activity-stat-label">Quiz créés</span>
                </div>
                <div class="activity-stat">
                    <span class="activity-stat-nombre"><%= totalPartiesJouees > 0 && totalQuiz > 0 ? (totalPartiesJouees / totalQuiz) : 0 %></span>
                    <span class="activity-stat-label">Moy. par quiz</span>
                </div>
            </div>
        <% } else { %>
            <div class="admin-empty-mini">
                <i class="fas fa-inbox"></i>
                <p>Aucune partie jouée pour le moment</p>
            </div>
        <% } %>
    </div>
</div>
    
    
    <%-- ===== ACTIVITÉ COURS RÉCENTE ===== --%>
<div class="admin-dash-card">
    <div class="admin-dash-card-header">
        <div class="admin-dash-card-title">
            <i class="fas fa-graduation-cap"></i>
            <h3>Activité Cours</h3>
        </div>
        <a href="${pageContext.request.contextPath}/admin/cours" class="admin-link">Voir les cours <i class="fas fa-arrow-right"></i></a>
    </div>
    <div class="admin-dash-card-body">
        <% if (totalCours > 0) { %>
            <div class="admin-activity-summary">
                <div class="activity-stat">
                    <span class="activity-stat-nombre"><%= totalCours %></span>
                    <span class="activity-stat-label">Cours créés</span>
                </div>
                <div class="activity-stat">
                    <span class="activity-stat-nombre"><%= totalJoueurs %></span>
                    <span class="activity-stat-label">Apprenants</span>
                </div>
                <div class="activity-stat">
                    <span class="activity-stat-nombre"><%= utilisateursPremium %></span>
                    <span class="activity-stat-label">Premium</span>
                </div>
            </div>
        <% } else { %>
            <div class="admin-empty-mini">
                <i class="fas fa-inbox"></i>
                <p>Aucun cours créé pour le moment</p>
            </div>
        <% } %>
    </div>
</div>

        </div>

        <%-- ===== COLONNE DROITE (étroite) : PAIEMENTS + RÉPARTITION + RACCOURCIS ===== --%>
        <div class="admin-dash-col admin-dash-col-small">

            <%-- ===== PAIEMENTS EN ATTENTE (Super Admin uniquement) ===== --%>
            <% if (estSuperAdminDashboard) { %>
                <div class="admin-dash-card">
                    <div class="admin-dash-card-header">
                        <div class="admin-dash-card-title">
                            <i class="fas fa-money-bill-wave"></i>
                            <h3>Paiements en attente</h3>
                        </div>
                        <% if (paiementsEnAttente > 0) { %>
                            <span class="admin-dash-card-badge warning"><%= paiementsEnAttente %></span>
                        <% } %>
                    </div>
                    <div class="admin-dash-card-body">
                        <% if (paiementsEnAttente > 0) { %>
                            <div class="admin-alerte-paiement">
                                <div class="admin-alerte-paiement-icon">
                                    <i class="fas fa-exclamation-triangle"></i>
                                </div>
                                <div class="admin-alerte-paiement-text">
                                    <strong><%= paiementsEnAttente %> demande<%= paiementsEnAttente > 1 ? "s" : "" %> en attente</strong>
                                    <p>Ces demandes nécessitent votre validation manuelle.</p>
                                </div>
                            </div>
                            <a href="${pageContext.request.contextPath}/admin/paiements" class="admin-btn-action admin-btn-warning">
                                <i class="fas fa-check-circle"></i> Valider les paiements
                            </a>
                        <% } else { %>
                            <div class="admin-success-state">
                                <div class="admin-success-icon-wrapper">
                                    <i class="fas fa-check-circle"></i>
                                </div>
                                <div class="admin-success-text">
                                    <strong>Tout est à jour</strong>
                                    <p>Toutes les demandes de paiement ont été traitées avec succès.</p>
                                </div>
                            </div>
                        <% } %>
                        <div class="admin-paiement-stats">
                            <span><i class="fas fa-calendar-check"></i> <%= paiementsValidesAujourdhui %> aujourd'hui</span>
                        </div>
                    </div>
                </div>
            <% } %>

            <%-- ===== RÉPARTITION COURS PAR THÈME ===== --%>
            <div class="admin-dash-card">
                <div class="admin-dash-card-header">
                    <div class="admin-dash-card-title">
                        <i class="fas fa-chart-pie"></i>
                        <h3>Répartition cours</h3>
                    </div>
                </div>
                <div class="admin-dash-card-body">
                    <div class="admin-repartition-list">
                        <div class="repartition-item">
                            <span class="repartition-label">Élémentaire</span>
                            <div class="repartition-bar-track">
                                <div class="repartition-bar-fill" style="width: 40%; background: #27ae60;"></div>
                            </div>
                            <span class="repartition-value">40%</span>
                        </div>
                        <div class="repartition-item">
                            <span class="repartition-label">Moyen</span>
                            <div class="repartition-bar-track">
                                <div class="repartition-bar-fill" style="width: 35%; background: #f39c12;"></div>
                            </div>
                            <span class="repartition-value">35%</span>
                        </div>
                        <div class="repartition-item">
                            <span class="repartition-label">Avancé</span>
                            <div class="repartition-bar-track">
                                <div class="repartition-bar-fill" style="width: 25%; background: #e74c3c;"></div>
                            </div>
                            <span class="repartition-value">25%</span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ===== RACCOURCIS RAPIDES (conditionnels) ===== --%>
            <div class="admin-dash-card">
                <div class="admin-dash-card-header">
                    <div class="admin-dash-card-title">
                        <i class="fas fa-bolt"></i>
                        <h3>Accès rapides</h3>
                    </div>
                </div>
                <div class="admin-dash-card-body">
                    <div class="admin-shortcuts-grid">
                        <a href="${pageContext.request.contextPath}/admin/cours" class="admin-shortcut-item">
                            <div class="shortcut-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                                <i class="fas fa-graduation-cap"></i>
                            </div>
                            <span>Gérer les cours</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/quiz" class="admin-shortcut-item">
                            <div class="shortcut-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                                <i class="fas fa-list-check"></i>
                            </div>
                            <span>Gérer les quiz</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/themes" class="admin-shortcut-item">
                            <div class="shortcut-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                                <i class="fas fa-tags"></i>
                            </div>
                            <span>Gérer les thèmes</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/resultats" class="admin-shortcut-item">
                            <div class="shortcut-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                                <i class="fas fa-chart-bar"></i>
                            </div>
                            <span>Voir les résultats</span>
                        </a>
                        <% if (estSuperAdminDashboard) { %>
                            <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="admin-shortcut-item">
                                <div class="shortcut-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                                    <i class="fas fa-users"></i>
                                </div>
                                <span>Utilisateurs</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/paiements" class="admin-shortcut-item">
                                <div class="shortcut-icon" style="background: linear-gradient(135deg, #f5576c, #e0485c);">
                                    <i class="fas fa-money-bill-wave"></i>
                                </div>
                                <span>Paiements</span>
                            </a>
                        <% } %>
                        <a href="${pageContext.request.contextPath}/admin/certificats" class="admin-shortcut-item">
                            <div class="shortcut-icon" style="background: linear-gradient(135deg, #00bcd4, #0097a7);">
                                <i class="fas fa-certificate"></i>
                            </div>
                            <span>Certificats</span>
                        </a>
                    </div>
                </div>
            </div>

        </div>

    </div>

</div>

<%-- ===== FONCTION UTILITAIRE : CALCULER TEMPS ÉCOULÉ ===== --%>
<%!
    private String calculerTempsEcoule(Timestamp date) {
        if (date == null) return "";
        long diff = System.currentTimeMillis() - date.getTime();
        long minutes = diff / (1000 * 60);
        long heures = diff / (1000 * 60 * 60);
        long jours = diff / (1000 * 60 * 60 * 24);

        if (minutes < 1) return "À l'instant";
        if (minutes < 60) return "Il y a " + minutes + " min";
        if (heures < 24) return "Il y a " + heures + "h";
        if (jours < 7) return "Il y a " + jours + "j";
        return new SimpleDateFormat("dd/MM").format(date);
    }
%>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-dashboard.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>