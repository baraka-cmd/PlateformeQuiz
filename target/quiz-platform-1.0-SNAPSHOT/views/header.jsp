<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     Fichier : header.jsp
     Rôle    : En-tête commun à toutes les pages de la plateforme
              - Déclare le DOCTYPE, <head>, Google Fonts, Font Awesome
              - Charge les feuilles de style globales
              - Affiche la barre de navigation adaptative (visiteur / joueur / admin)
              - Menu déroulant utilisateur (Profil, Changer MDP, Déconnexion)
              - Ouvre le conteneur principal <div class="main-container">
     ============================================================ --%>

<%
    // Récupération de l'utilisateur connecté depuis la session
    // ATTENTION : ces variables NE doivent PAS être redéclarées dans les pages qui incluent ce header
    // Si une page a besoin de ces variables, elle doit les récupérer via session
    Utilisateur utilisateurConnecte = null;
    String role = null;
    
    if (session != null && session.getAttribute("utilisateur") != null) {
        utilisateurConnecte = (Utilisateur) session.getAttribute("utilisateur");
        role = utilisateurConnecte.getRole();
    }

    // Détermination de la page active pour le lien "actif" dans la navigation
    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    <%-- Titre dynamique : peut être surchargé par les pages qui incluent ce header --%>
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "AmijayQuiz - Plateforme de Quiz et Cours" %></title>
    
    <%-- Meta description pour SEO --%>
    <meta name="description" content="AmijayQuiz - Plateforme de quiz et cours en ligne. Testez vos connaissances, progressez niveau par niveau et obtenez des certificats vérifiables.">
    <meta name="author" content="AmijayQuiz">
    <meta name="theme-color" content="#667eea">

    <%-- ====== FEUILLES DE STYLE EXTERNES ====== --%>
    <%-- Font Awesome 6 (icônes vectorielles) --%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" 
          integrity="sha512-DTOQO9RWCH3ppGqcWaEA1BIZOC6xxalwEsw9c2QQeAIftl+Vegovlnee1c9QX4TctnWMn13TZye+giMm8e2LwA==" 
          crossorigin="anonymous" referrerpolicy="no-referrer" />
    
    <%-- Google Fonts : Poppins (poids 300 à 700) --%>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <%-- Feuille de style globale de la plateforme --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/footer.css">

    <%-- ====== BLOC POUR LES STYLES SPÉCIFIQUES À CHAQUE PAGE ====== --%>
    <%-- Chaque page peut injecter son propre CSS ici via request.setAttribute("extraHead") --%>
    <%= request.getAttribute("extraHead") != null ? request.getAttribute("extraHead") : "" %>
</head>
<body>

<%-- ============================================================
     BARRE DE NAVIGATION PRINCIPALE
     - Sticky en haut de page
     - Logo AmijayQuiz à gauche
     - Liens de navigation au centre
     - Menu utilisateur déroulant à droite
     ============================================================ --%>
<nav class="navbar" role="navigation" aria-label="Navigation principale">
    
    <%-- Logo de la plateforme (retour à l'accueil) --%>
    <a href="${pageContext.request.contextPath}/" class="logo" aria-label="Retour à l'accueil AmijayQuiz">
        <i class="fas fa-brain" aria-hidden="true"></i>
        <span>AmijayQuiz</span>
    </a>
    
    <%-- Liens de navigation --%>
    <ul class="nav-links">
        
        <%-- ============================================================
             MENU VISITEUR (utilisateur non connecté)
             Accueil | Cours | Inscription | Connexion
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
             MENU ADMINISTRATEUR
             Dashboard | Questions | Quiz | Résultats | Cours | Menu Admin
             ============================================================ --%>
        <% } else if ("admin".equals(role)) { %>
            
            <li>
                <a href="${pageContext.request.contextPath}/admin/dashboard"
                   class="<%= uri.contains("/admin/dashboard") ? "active" : "" %>">
                    <i class="fas fa-tachometer-alt" aria-hidden="true"></i>
                    <span>Dashboard</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/admin/questions"
                   class="<%= uri.contains("/admin/questions") ? "active" : "" %>">
                    <i class="fas fa-question-circle" aria-hidden="true"></i>
                    <span>Questions</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/admin/quiz"
                   class="<%= uri.contains("/admin/quiz") ? "active" : "" %>">
                    <i class="fas fa-list-check" aria-hidden="true"></i>
                    <span>Quiz</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/admin/resultats"
                   class="<%= uri.contains("/admin/resultats") ? "active" : "" %>">
                    <i class="fas fa-chart-bar" aria-hidden="true"></i>
                    <span>Résultats</span>
                </a>
            </li>
            
            <%-- Menu utilisateur admin --%>
            <li class="user-menu-container">
                <button class="user-menu-trigger" id="userMenuTrigger" aria-expanded="false" aria-haspopup="true" title="Menu utilisateur">
                    <span class="user-avatar-mini">
                        <i class="fas fa-user-circle"></i>
                    </span>
                    <span class="user-name"><%= utilisateurConnecte.getPseudo() %></span>
                    <i class="fas fa-shield-haltered admin-badge" title="Administrateur"></i>
                    <i class="fas fa-chevron-down menu-arrow"></i>
                </button>
                <div class="user-menu-dropdown" id="userMenuDropdown" role="menu" aria-label="Menu utilisateur">
                    <div class="user-menu-header">
                        <div class="user-menu-avatar">
                            <i class="fas fa-user-circle"></i>
                        </div>
                        <div class="user-menu-info">
                            <strong><%= utilisateurConnecte.getPseudo() %></strong>
                            <span>Administrateur</span>
                        </div>
                    </div>
                    <div class="user-menu-divider"></div>
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
        <% } else { 
            // 🆕 Vérification premium pour le header
            com.amijayquiz.dao.AccessPremiumDAO premiumDAONav = new com.amijayquiz.dao.AccessPremiumDAO();
            boolean estPremiumNav = premiumDAONav.estPremium(utilisateurConnecte.getId());
        %>
            
            <li>
                <a href="${pageContext.request.contextPath}/joueur/dashboard"
                   class="<%= uri.contains("/joueur/dashboard") || uri.contains("/joueur/theme") || uri.contains("/joueur/niveau") ? "active" : "" %>">
                    <i class="fas fa-layer-group" aria-hidden="true"></i>
                    <span>Quiz</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/joueur/cours"
                   class="<%= uri.contains("/joueur/cours") ? "active" : "" %>">
                    <i class="fas fa-graduation-cap" aria-hidden="true"></i>
                    <span>Cours</span>
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/joueur/historique"
                   class="<%= uri.contains("/joueur/historique") ? "active" : "" %>">
                    <i class="fas fa-history" aria-hidden="true"></i>
                    <span>Historique</span>
                </a>
            </li>
            
            <%-- 🆕 BOUTON PREMIUM DANS LA NAVBAR --%>
            <% if (!estPremiumNav) { %>
                <li>
                    <a href="${pageContext.request.contextPath}/paiement" 
                       class="nav-premium-btn">
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
                    <%-- 🆕 BADGE PREMIUM DANS LE MENU --%>
                    <% if (estPremiumNav) { %>
                        <i class="fas fa-crown premium-crown-icon" title="Premium"></i>
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
                            <span>
                                Joueur
                                <% if (estPremiumNav) { %>
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
                    
                    <%-- 🆕 LIEN PREMIUM DANS LE MENU DÉROULANT --%>
                    <% if (!estPremiumNav) { %>
                        <a href="${pageContext.request.contextPath}/paiement" class="user-menu-item user-menu-premium" role="menuitem">
                            <i class="fas fa-crown"></i>
                            <span>Devenir Premium (4$)</span>
                        </a>
                        <div class="user-menu-divider"></div>
                    <% } %>
                    
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

<%-- ============================================================
     CONTENEUR PRINCIPAL
     Toutes les pages incluent leur contenu entre ce div et le footer
     ============================================================ --%>
<main class="main-container" role="main">