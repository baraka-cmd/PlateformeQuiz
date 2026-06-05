<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     FICHIER : views/header.jsp
     RÔLE    : En-tête commun à toutes les pages de la plateforme
              - Déclare le DOCTYPE, <head>, Google Fonts, Font Awesome
              - Charge les feuilles de style globales
              - Affiche la barre de navigation adaptative
              - Badge Premium dans la navbar et le menu déroulant
              - Ouvre le conteneur principal
              - Menu admin complet : Dashboard | Cours | Quiz | Questions | Thèmes | Certificats | Résultats | Paiements
     ============================================================ --%>

<%
    Utilisateur utilisateurConnecte = null;
    String roleHeader = null;
    
    if (session != null && session.getAttribute("utilisateur") != null) {
        utilisateurConnecte = (Utilisateur) session.getAttribute("utilisateur");
        roleHeader = utilisateurConnecte.getRole();
    }

    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();

    // Vérification premium pour le header
    boolean estPremiumHeader = false;
    if (utilisateurConnecte != null && "joueur".equals(roleHeader)) {
        com.amijayquiz.dao.AccessPremiumDAO premiumDAOH = new com.amijayquiz.dao.AccessPremiumDAO();
        estPremiumHeader = premiumDAOH.estPremium(utilisateurConnecte.getId());
    }
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "AmijayQuiz - Plateforme de Quiz et Cours" %></title>
    
    <meta name="description" content="AmijayQuiz - Plateforme de quiz et cours en ligne. Testez vos connaissances, progressez niveau par niveau et obtenez des certificats vérifiables.">
    <meta name="author" content="AmijayQuiz">
    <meta name="theme-color" content="#667eea">

    <%-- Font Awesome 6 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/fontawesome/fontawesome-free-7.2.0-web/css/all.min.css">
    
    <%-- Google Fonts : Poppins --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/fonts.css">
    
    <%-- Styles globaux --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/footer.css">

    <%-- Styles spécifiques à chaque page --%>
    <%= request.getAttribute("extraHead") != null ? request.getAttribute("extraHead") : "" %>
</head>
<body>

<%-- ============================================================
     BARRE DE NAVIGATION PRINCIPALE
     ============================================================ --%>
<nav class="navbar" role="navigation" aria-label="Navigation principale">
    
    <%-- Logo --%>
    <a href="${pageContext.request.contextPath}/" class="logo" aria-label="Retour à l'accueil AmijayQuiz">
        <i class="fas fa-brain" aria-hidden="true"></i>
        <span>AmijayQuiz</span>
    </a>
    
    <%-- Liens de navigation --%>
    <ul class="nav-links">
        
        <%-- ============================================================
             MENU VISITEUR (non connecté)
             ============================================================ --%>
        <% if (utilisateurConnecte == null) { %>
            
            <li>
                <a href="${pageContext.request.contextPath}/" 
                   class="<%= uri.equals(contextPath + "/") || uri.endsWith("/index.jsp") ? "active" : "" %>">
                    <i class="fas fa-home" aria-hidden="true"></i>
                    <span>Accueil</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/inscription"
                   class="<%= uri.contains("/inscription") ? "active" : "" %>">
                    <i class="fas fa-user-plus" aria-hidden="true"></i>
                    <span>Inscription</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/connexion" class="btn-connexion">
                    <i class="fas fa-sign-in-alt" aria-hidden="true"></i>
                    <span>Connexion</span>
                </a>
            </li>
            
        <%-- ============================================================
     MENU ADMINISTRATEUR / GESTIONNAIRE
     Dashboard | Cours | Quiz | Thèmes | Résultats | [Utilisateurs] | [Paiements] | Profil
     [ ] = Super Admin uniquement
     ============================================================ --%>
<% } else if ("admin".equals(roleHeader) || "gestionnaire".equals(roleHeader)) { %>
    
    <li>
        <a href="${pageContext.request.contextPath}/admin/dashboard"
           class="<%= uri.contains("/admin/dashboard") ? "active" : "" %>">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
    </li>
    
    <li>
        <a href="${pageContext.request.contextPath}/admin/cours"
           class="<%= uri.contains("/admin/cours") || uri.contains("/admin/certificats") ? "active" : "" %>">
            <i class="fas fa-graduation-cap"></i>
            <span>Cours</span>
        </a>
    </li>
    
    <li>
        <a href="${pageContext.request.contextPath}/admin/quiz"
           class="<%= uri.contains("/admin/quiz") && !uri.contains("/admin/questions") ? "active" : "" %>">
            <i class="fas fa-list-check"></i>
            <span>Quiz</span>
        </a>
    </li>
    
    <li>
        <a href="${pageContext.request.contextPath}/admin/themes"
           class="<%= uri.contains("/admin/themes") ? "active" : "" %>">
            <i class="fas fa-tags"></i>
            <span>Thèmes</span>
        </a>
    </li>
    
    <li>
        <a href="${pageContext.request.contextPath}/admin/resultats"
           class="<%= uri.contains("/admin/resultats") ? "active" : "" %>">
            <i class="fas fa-chart-bar"></i>
            <span>Résultats</span>
        </a>
    </li>
    
    <%-- ===== LIENS RÉSERVÉS AU SUPER ADMIN ===== --%>
    <% if ("admin".equals(roleHeader)) { %>
        <li>
            <a href="${pageContext.request.contextPath}/admin/utilisateurs"
               class="<%= uri.contains("/admin/utilisateurs") ? "active" : "" %>">
                <i class="fas fa-users"></i>
                <span>Utilisateurs</span>
            </a>
        </li>
        
        <li>
            <a href="${pageContext.request.contextPath}/admin/paiements"
               class="<%= uri.contains("/admin/paiements") ? "active" : "" %>">
                <i class="fas fa-money-bill-wave"></i>
                <span>Paiements</span>
            </a>
        </li>
    <% } %>
    
    <%-- Menu utilisateur --%>
    <li class="user-menu-container">
        <button class="user-menu-trigger" id="userMenuTrigger" aria-expanded="false" aria-haspopup="true" title="Menu utilisateur">
            <span class="user-avatar-mini">
                <i class="fas fa-user-circle"></i>
            </span>
            <span class="user-name"><%= utilisateurConnecte.getPseudo() %></span>
            <% if ("admin".equals(roleHeader)) { %>
                <i class="fas fa-shield-alt admin-badge" title="Super Administrateur"></i>
            <% } else { %>
                <i class="fas fa-user-cog admin-badge" title="Gestionnaire" style="color: #f39c12;"></i>
            <% } %>
            <i class="fas fa-chevron-down menu-arrow"></i>
        </button>
        <div class="user-menu-dropdown" id="userMenuDropdown" role="menu" aria-label="Menu utilisateur">
            <div class="user-menu-header">
                <div class="user-menu-avatar">
                    <i class="fas fa-user-circle"></i>
                </div>
                <div class="user-menu-info">
                    <strong><%= utilisateurConnecte.getPseudo() %></strong>
                    <span class="menu-admin-badge">
                        <% if ("admin".equals(roleHeader)) { %>
                            <i class="fas fa-shield-alt"></i> Super Administrateur
                        <% } else { %>
                            <i class="fas fa-user-cog"></i> Gestionnaire
                        <% } %>
                    </span>
                </div>
            </div>
            <div class="user-menu-divider"></div>
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="user-menu-item" role="menuitem">
                <i class="fas fa-tachometer-alt"></i>
                <span>Dashboard</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours" class="user-menu-item" role="menuitem">
                <i class="fas fa-graduation-cap"></i>
                <span>Gérer les cours</span>
            </a>
            <% if ("admin".equals(roleHeader)) { %>
                <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="user-menu-item" role="menuitem">
                    <i class="fas fa-users"></i>
                    <span>Gérer les utilisateurs</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/paiements" class="user-menu-item" role="menuitem">
                    <i class="fas fa-money-bill-wave"></i>
                    <span>Paiements</span>
                </a>
            <% } %>
            <a href="${pageContext.request.contextPath}/admin/certificats" class="user-menu-item" role="menuitem">
                <i class="fas fa-certificate"></i>
                <span>Certificats</span>
            </a>
            <a href="${pageContext.request.contextPath}/changer-mot-de-passe" class="user-menu-item" role="menuitem">
                <i class="fas fa-key"></i>
                <span>Changer mon mot de passe</span>
            </a>
            <div class="user-menu-divider"></div>
            <a href="${pageContext.request.contextPath}/deconnexion" class="user-menu-item user-menu-danger" role="menuitem">
                <i class="fas fa-sign-out-alt"></i>
                <span>Déconnexion</span>
            </a>
        </div>
    </li>
            
        <%-- ============================================================
             MENU JOUEUR
             Quiz | Cours | Historique | Premium | Menu Joueur
             ============================================================ --%>
        <% } else { %>
            
            <li>
                <a href="${pageContext.request.contextPath}/joueur/dashboard"
                   class="<%= uri.contains("/joueur/dashboard") || uri.contains("/joueur/theme") || uri.contains("/joueur/niveau") ? "active" : "" %>">
                    <i class="fas fa-layer-group"></i>
                    <span>Quiz</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/joueur/cours"
                   class="<%= uri.contains("/joueur/cours") ? "active" : "" %>">
                    <i class="fas fa-graduation-cap"></i>
                    <span>Cours</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/joueur/historique"
                   class="<%= uri.contains("/joueur/historique") ? "active" : "" %>">
                    <i class="fas fa-history"></i>
                    <span>Historique</span>
                </a>
            </li>
            
            <%-- BOUTON PREMIUM DANS LA NAVBAR --%>
            <% if (!estPremiumHeader) { %>
                <li>
                    <a href="${pageContext.request.contextPath}/paiement" class="nav-premium-btn">
                        <i class="fas fa-crown"></i>
                        <span>Premium</span>
                    </a>
                </li>
            <% } %>
            
            <%-- Menu utilisateur joueur --%>
            <li class="user-menu-container">
                <button class="user-menu-trigger" id="userMenuTrigger" aria-expanded="false" aria-haspopup="true" title="Menu utilisateur">
                    <span class="user-avatar-mini">
                        <i class="fas fa-user-circle"></i>
                    </span>
                    <span class="user-name"><%= utilisateurConnecte.getPseudo() %></span>
                    <%-- COURONNE PREMIUM --%>
                    <% if (estPremiumHeader) { %>
                        <i class="fas fa-crown premium-crown-icon" title="Premium actif"></i>
                    <% } %>
                    <i class="fas fa-chevron-down menu-arrow"></i>
                </button>
                <div class="user-menu-dropdown" id="userMenuDropdown" role="menu">
                    <div class="user-menu-header">
                        <div class="user-menu-avatar">
                            <i class="fas fa-user-circle"></i>
                        </div>
                        <div class="user-menu-info">
                            <strong><%= utilisateurConnecte.getPseudo() %></strong>
                            <span>
                                Joueur
                                <% if (estPremiumHeader) { %>
                                    <span class="menu-premium-badge">
                                        <i class="fas fa-crown"></i> Premium
                                    </span>
                                <% } else { %>
                                    <span class="menu-gratuit-badge">Gratuit</span>
                                <% } %>
                            </span>
                        </div>
                    </div>
                    <div class="user-menu-divider"></div>
                    
                    <%-- LIEN PREMIUM SI NON PREMIUM --%>
                    <% if (!estPremiumHeader) { %>
                        <a href="${pageContext.request.contextPath}/paiement" class="user-menu-item user-menu-premium" role="menuitem">
                            <i class="fas fa-crown"></i>
                            <span>Devenir Premium (4$)</span>
                        </a>
                        <div class="user-menu-divider"></div>
                    <% } %>
                    
                    <a href="${pageContext.request.contextPath}/joueur/dashboard" class="user-menu-item" role="menuitem">
                        <i class="fas fa-tachometer-alt"></i>
                        <span>Mon Dashboard</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/joueur/cours" class="user-menu-item" role="menuitem">
                        <i class="fas fa-graduation-cap"></i>
                        <span>Mes Cours</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/joueur/historique" class="user-menu-item" role="menuitem">
                        <i class="fas fa-history"></i>
                        <span>Mon Historique</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/changer-mot-de-passe" class="user-menu-item" role="menuitem">
                        <i class="fas fa-key"></i>
                        <span>Changer mon mot de passe</span>
                    </a>
                    <div class="user-menu-divider"></div>
                    <a href="${pageContext.request.contextPath}/deconnexion" class="user-menu-item user-menu-danger" role="menuitem">
                        <i class="fas fa-sign-out-alt"></i>
                        <span>Déconnexion</span>
                    </a>
                </div>
            </li>
            
        <% } %>
    </ul>
</nav>

<%-- Conteneur principal --%>
<main class="main-container" role="main">