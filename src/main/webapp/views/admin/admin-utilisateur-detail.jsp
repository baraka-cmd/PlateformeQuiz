<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.amijayquiz.models.Utilisateur" %>
<%@ page import="com.amijayquiz.models.Partie" %>
<%@ page import="com.amijayquiz.models.Certificat" %>

<%-- ============================================================
     FICHIER : views/admin/admin-utilisateur-detail.jsp
     RÔLE    : Page de détail complet d'un utilisateur
              - Informations personnelles
              - Statut Premium
              - Statistiques Quiz et Cours
              - Dernières parties et certificats
              - Actions : Activer/Désactiver, Changer rôle, Reset MDP
     ============================================================ --%>

<%
    Utilisateur utilisateur = (Utilisateur) request.getAttribute("utilisateur");
    Boolean estPremium = (Boolean) request.getAttribute("estPremium");
    Map<String, Object> infosPremium = (Map<String, Object>) request.getAttribute("infosPremium");
    Map<String, Object> statsQuiz = (Map<String, Object>) request.getAttribute("statsQuiz");
    Map<String, Object> statsCours = (Map<String, Object>) request.getAttribute("statsCours");
    List<Partie> dernieresParties = (List<Partie>) request.getAttribute("dernieresParties");
    List<Certificat> certificats = (List<Certificat>) request.getAttribute("certificats");
    Boolean peutActiver = (Boolean) request.getAttribute("peutActiver");
    Boolean peutDesactiver = (Boolean) request.getAttribute("peutDesactiver");
    Boolean peutPromouvoir = (Boolean) request.getAttribute("peutPromouvoir");
    Boolean peutRetrograder = (Boolean) request.getAttribute("peutRetrograder");
    Boolean peutReinitialiserMDP = (Boolean) request.getAttribute("peutReinitialiserMDP");
    Integer adminId = (Integer) request.getAttribute("adminId");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (utilisateur == null) {
        response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
        return;
    }

    if (estPremium == null) estPremium = false;
    if (statsQuiz == null) statsQuiz = new java.util.LinkedHashMap<>();
    if (statsCours == null) statsCours = new java.util.LinkedHashMap<>();
    if (dernieresParties == null) dernieresParties = new java.util.ArrayList<>();
    if (certificats == null) certificats = new java.util.ArrayList<>();
    if (peutActiver == null) peutActiver = false;
    if (peutDesactiver == null) peutDesactiver = false;
    if (peutPromouvoir == null) peutPromouvoir = false;
    if (peutRetrograder == null) peutRetrograder = false;
    if (peutReinitialiserMDP == null) peutReinitialiserMDP = false;
    if (adminId == null) adminId = 0;

    String pseudo = utilisateur.getPseudo() != null ? utilisateur.getPseudo() : "N/A";
    String email = utilisateur.getEmail() != null ? utilisateur.getEmail() : "Non renseigné";
    String role = utilisateur.getRole() != null ? utilisateur.getRole() : "joueur";
    boolean actif = utilisateur.isActif();
    int utilisateurId = utilisateur.getId();
    boolean estMoi = utilisateurId == adminId;

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("fr", "FR"));
    SimpleDateFormat sdfComplet = new SimpleDateFormat("dd MMM yyyy HH:mm", new java.util.Locale("fr", "FR"));
    String dateInscription = utilisateur.getDateInscription() != null ? sdfComplet.format(utilisateur.getDateInscription()) : "N/A";

    request.setAttribute("pageTitle", "Détail - " + pseudo + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-utilisateurs.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="au-detail-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="breadcrumb-link">
            <i class="fas fa-users"></i><span>Utilisateurs</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-user"></i><span><%= pseudo %></span>
        </span>
    </nav>

    <%-- ===== MESSAGES FLASH ===== --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes" data-timeout="6000">
            <i class="fas fa-check-circle"></i><span><%= messageSucces %></span>
        </div>
    <% } %>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i><span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== EN-TÊTE PROFIL ===== --%>
    <div class="au-profil-header">
        <div class="au-profil-avatar" style="background: linear-gradient(135deg, #667eea, #764ba2);">
            <span><%= pseudo.substring(0, 1).toUpperCase() %></span>
        </div>
        <div class="au-profil-info">
            <h1>
                <%= pseudo %>
                <% if (estMoi) { %>
                    <span class="au-badge-moi">Vous</span>
                <% } %>
            </h1>
            <div class="au-profil-meta">
                <span class="au-meta-item">
                    <i class="fas fa-envelope"></i> <%= email %>
                </span>
                <span class="au-meta-sep">•</span>
                <span class="au-meta-item">
                    <i class="fas fa-calendar-alt"></i> Inscrit le <%= dateInscription %>
                </span>
                <span class="au-meta-sep">•</span>
                <span class="au-meta-item">
                    <i class="fas fa-id-badge"></i> ID #<%= utilisateurId %>
                </span>
            </div>
            <div class="au-profil-badges">
                <span class="au-badge-role <%= "admin".equals(role) ? "au-role-admin" : "au-role-joueur" %>">
                    <i class="fas <%= "admin".equals(role) ? "fa-shield-alt" : "fa-user" %>"></i>
                    <%= "admin".equals(role) ? "Administrateur" : "Joueur" %>
                </span>
                <span class="au-badge-statut <%= actif ? "au-statut-actif" : "au-statut-inactif" %>">
                    <i class="fas <%= actif ? "fa-check-circle" : "fa-ban" %>"></i>
                    <%= actif ? "Actif" : "Inactif" %>
                </span>
                <span class="au-badge-premium <%= estPremium ? "au-premium-actif" : "au-premium-inactif" %>">
                    <i class="fas <%= estPremium ? "fa-crown" : "fa-user" %>"></i>
                    <%= estPremium ? "Premium" : "Gratuit" %>
                </span>
            </div>
        </div>
        <div class="au-profil-actions">
            <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="au-btn-outline">
                <i class="fas fa-arrow-left"></i><span>Retour</span>
            </a>
            <% if (!estMoi) { %>
                <% if (peutActiver) { %>
                    <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline">
                        <input type="hidden" name="action" value="activer">
                        <input type="hidden" name="id" value="<%= utilisateurId %>">
                        <button type="submit" class="au-btn au-btn-activer">
                            <i class="fas fa-check-circle"></i><span>Activer le compte</span>
                        </button>
                    </form>
                <% } %>
                <% if (peutDesactiver) { %>
                    <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline"
                          onsubmit="return confirm('Désactiver le compte de <%= pseudo.replace("'", "\\'") %> ?')">
                        <input type="hidden" name="action" value="desactiver">
                        <input type="hidden" name="id" value="<%= utilisateurId %>">
                        <button type="submit" class="au-btn au-btn-desactiver">
                            <i class="fas fa-ban"></i><span>Désactiver le compte</span>
                        </button>
                    </form>
                <% } %>
                <% if (peutPromouvoir) { %>
                    <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline"
                          onsubmit="return confirm('Promouvoir <%= pseudo.replace("'", "\\'") %> en Administrateur ?')">
                        <input type="hidden" name="action" value="promouvoir">
                        <input type="hidden" name="id" value="<%= utilisateurId %>">
                        <button type="submit" class="au-btn au-btn-promouvoir">
                            <i class="fas fa-arrow-up"></i><span>Promouvoir Admin</span>
                        </button>
                    </form>
                <% } %>
                <% if (peutRetrograder) { %>
                    <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline"
                          onsubmit="return confirm('Rétrograder <%= pseudo.replace("'", "\\'") %> en Joueur ?')">
                        <input type="hidden" name="action" value="retrograder">
                        <input type="hidden" name="id" value="<%= utilisateurId %>">
                        <button type="submit" class="au-btn au-btn-retrograder">
                            <i class="fas fa-arrow-down"></i><span>Rétrograder Joueur</span>
                        </button>
                    </form>
                <% } %>
                <% if (peutReinitialiserMDP) { %>
                    <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline"
                          onsubmit="return confirm('Réinitialiser le mot de passe de <%= pseudo.replace("'", "\\'") %> ?\n\nUn nouveau mot de passe sera envoyé à <%= email %>.')">
                        <input type="hidden" name="action" value="reset-mdp">
                        <input type="hidden" name="id" value="<%= utilisateurId %>">
                        <button type="submit" class="au-btn au-btn-reset-mdp">
                            <i class="fas fa-key"></i><span>Réinitialiser MDP</span>
                        </button>
                    </form>
                <% } %>
            <% } %>
        </div>
    </div>

    <%-- ===== LAYOUT 2 COLONNES ===== --%>
    <div class="au-detail-layout">

        <%-- ===== COLONNE GAUCHE : STATISTIQUES ===== --%>
        <div class="au-detail-col au-detail-col-gauche">

            <%-- ===== STATUT PREMIUM ===== --%>
            <div class="au-detail-card">
                <div class="au-detail-card-header">
                    <div class="au-detail-card-title">
                        <i class="fas fa-crown"></i>
                        <h3>Statut Premium</h3>
                    </div>
                </div>
                <div class="au-detail-card-body">
                    <% if (estPremium && infosPremium != null) { %>
                        <div class="au-premium-actif-card">
                            <div class="au-premium-icon">
                                <i class="fas fa-crown"></i>
                            </div>
                            <div class="au-premium-info">
                                <strong>Premium actif</strong>
                                <span>
                                    Activé le <%= infosPremium.get("dateActivation") != null ? 
                                        sdf.format((java.sql.Timestamp) infosPremium.get("dateActivation")) : "N/A" %>
                                </span>
                                <span>
                                    Référence : <%= infosPremium.get("referencePaiement") != null ? 
                                        infosPremium.get("referencePaiement") : "N/A" %>
                                </span>
                                <span>
                                    Montant : <%= infosPremium.get("montantPaye") != null ? 
                                        String.format("%.2f", infosPremium.get("montantPaye")) : "0.00" %> 
                                    <%= infosPremium.get("devise") != null ? infosPremium.get("devise") : "USD" %>
                                </span>
                            </div>
                        </div>
                    <% } else { %>
                        <div class="au-premium-inactif-card">
                            <i class="fas fa-user"></i>
                            <p>Compte gratuit</p>
                            <span>Cet utilisateur n'a pas encore activé le Premium.</span>
                        </div>
                    <% } %>
                </div>
            </div>

            <%-- ===== STATISTIQUES QUIZ ===== --%>
            <div class="au-detail-card">
                <div class="au-detail-card-header">
                    <div class="au-detail-card-title">
                        <i class="fas fa-layer-group"></i>
                        <h3>Statistiques Quiz</h3>
                    </div>
                </div>
                <div class="au-detail-card-body">
                    <div class="au-stats-grid-detail">
                        <div class="au-stat-item-detail">
                            <span class="au-stat-nombre-detail"><%= statsQuiz.getOrDefault("totalParties", 0) %></span>
                            <span class="au-stat-label-detail">Parties jouées</span>
                        </div>
                        <div class="au-stat-item-detail">
                            <span class="au-stat-nombre-detail"><%= String.format("%.0f", (Double) statsQuiz.getOrDefault("tauxReussite", 0.0)) %>%</span>
                            <span class="au-stat-label-detail">Taux réussite</span>
                        </div>
                        <div class="au-stat-item-detail">
                            <span class="au-stat-nombre-detail"><%= String.format("%.0f", (Double) statsQuiz.getOrDefault("meilleurScore", 0.0)) %>%</span>
                            <span class="au-stat-label-detail">Meilleur score</span>
                        </div>
                        <div class="au-stat-item-detail">
                            <span class="au-stat-nombre-detail"><%= statsQuiz.getOrDefault("partiesReussies", 0) %></span>
                            <span class="au-stat-label-detail">Parties réussies</span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ===== STATISTIQUES COURS ===== --%>
            <div class="au-detail-card">
                <div class="au-detail-card-header">
                    <div class="au-detail-card-title">
                        <i class="fas fa-graduation-cap"></i>
                        <h3>Statistiques Cours</h3>
                    </div>
                </div>
                <div class="au-detail-card-body">
                    <div class="au-stats-grid-detail">
                        <div class="au-stat-item-detail">
                            <span class="au-stat-nombre-detail"><%= statsCours.getOrDefault("totalCertificats", 0) %></span>
                            <span class="au-stat-label-detail">Certificats</span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ===== DERNIERS CERTIFICATS ===== --%>
            <% if (certificats != null && !certificats.isEmpty()) { %>
                <div class="au-detail-card">
                    <div class="au-detail-card-header">
                        <div class="au-detail-card-title">
                            <i class="fas fa-certificate"></i>
                            <h3>Derniers certificats</h3>
                        </div>
                    </div>
                    <div class="au-detail-card-body">
                        <div class="au-liste-mini">
                            <% for (Certificat cert : certificats) { %>
                                <div class="au-liste-item">
                                    <div class="au-liste-icon" style="background: <%= cert.getCouleurMention() %>;">
                                        <i class="<%= cert.getIconeMention() %>"></i>
                                    </div>
                                    <div class="au-liste-info">
                                        <strong><%= cert.getMention() %></strong>
                                        <span><%= cert.getPourcentage() %> • <%= cert.getDateFormatee() %></span>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
            <% } %>

        </div>

        <%-- ===== COLONNE DROITE : DERNIÈRES PARTIES ===== --%>
        <div class="au-detail-col au-detail-col-droite">

            <div class="au-detail-card">
                <div class="au-detail-card-header">
                    <div class="au-detail-card-title">
                        <i class="fas fa-history"></i>
                        <h3>Dernières parties</h3>
                    </div>
                    <span class="au-detail-card-badge"><%= dernieresParties.size() %></span>
                </div>
                <div class="au-detail-card-body">
                    <% if (dernieresParties != null && !dernieresParties.isEmpty()) { %>
                        <div class="au-liste-mini">
                            <% for (Partie partie : dernieresParties) {
                                double pourcentage = partie.getPourcentage();
                                String badgeClass = pourcentage >= 80 ? "au-score-excellent" : 
                                                   (pourcentage >= 60 ? "au-score-bon" : 
                                                   (pourcentage >= 40 ? "au-score-moyen" : "au-score-faible"));
                            %>
                                <div class="au-liste-item">
                                    <div class="au-liste-icon" style="background: #667eea;">
                                        <i class="fas fa-question-circle"></i>
                                    </div>
                                    <div class="au-liste-info">
                                        <strong><%= partie.getTitreQuiz() != null ? partie.getTitreQuiz() : "Quiz" %></strong>
                                        <span>
                                            <%= partie.getScore() %>/<%= partie.getTotalQuestions() %>
                                            • <%= partie.getTempsFormate() %>
                                            • <%= sdf.format(partie.getDatePartie()) %>
                                        </span>
                                    </div>
                                    <span class="au-badge-score <%= badgeClass %>"><%= String.format("%.0f%%", pourcentage) %></span>
                                </div>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="au-empty-mini">
                            <i class="fas fa-inbox"></i>
                            <p>Aucune partie jouée</p>
                        </div>
                    <% } %>
                </div>
            </div>

        </div>

    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="au-back">
        <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="au-btn-retour">
            <i class="fas fa-arrow-left"></i> Retour à la liste des utilisateurs
        </a>
    </div>

</div>

<%@ include file="../footer-connecte.jsp" %>