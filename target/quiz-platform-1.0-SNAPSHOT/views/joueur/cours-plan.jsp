<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>
<%@ page import="com.amijayquiz.models.Certificat" %>

<%-- ============================================================
     Fichier : cours-plan.jsp
     Rôle    : Plan détaillé d'un cours
              Affiche l'arborescence complète du cours :
              tous les chapitres avec leurs leçons visibles,
              comme sur les plateformes officielles (OpenClassrooms,
              Udemy, Coursera, Codecademy, W3Schools)
     ============================================================ --%>

<%
    // Récupération des attributs depuis le servlet
    Cours cours = (Cours) request.getAttribute("cours");
    List<Map<String, Object>> chapitresAvecProgression = (List<Map<String, Object>>) request.getAttribute("chapitresAvecProgression");
    Integer totalChapitres = (Integer) request.getAttribute("totalChapitres");
    Integer chapitresTermines = (Integer) request.getAttribute("chapitresTermines");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    Integer totalLeconsTerminees = (Integer) request.getAttribute("totalLeconsTerminees");
    Integer totalExemplesCode = (Integer) request.getAttribute("totalExemplesCode");
    String tempsLectureTotal = (String) request.getAttribute("tempsLectureTotal");
    Integer pourcentageGlobal = (Integer) request.getAttribute("pourcentageGlobal");
    Boolean coursTermine = (Boolean) request.getAttribute("coursTermine");
    Boolean examenAccessible = (Boolean) request.getAttribute("examenAccessible");
    Boolean dejaCertifie = (Boolean) request.getAttribute("dejaCertifie");
    Certificat certificatExistant = (Certificat) request.getAttribute("certificatExistant");
    Integer prochainChapitreId = (Integer) request.getAttribute("prochainChapitreId");
    String prochainChapitreTitre = (String) request.getAttribute("prochainChapitreTitre");
    String messageMotivation = (String) request.getAttribute("messageMotivation");
    String iconeMotivation = (String) request.getAttribute("iconeMotivation");

    if (cours == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    // Valeurs par défaut
    if (totalChapitres == null) totalChapitres = 0;
    if (chapitresTermines == null) chapitresTermines = 0;
    if (totalLecons == null) totalLecons = 0;
    if (totalLeconsTerminees == null) totalLeconsTerminees = 0;
    if (totalExemplesCode == null) totalExemplesCode = 0;
    if (tempsLectureTotal == null) tempsLectureTotal = "N/A";
    if (pourcentageGlobal == null) pourcentageGlobal = 0;
    if (coursTermine == null) coursTermine = false;
    if (examenAccessible == null) examenAccessible = false;
    if (dejaCertifie == null) dejaCertifie = false;
    if (messageMotivation == null) messageMotivation = "Commencez dès maintenant votre apprentissage par la lecture et la pratique !";
    if (iconeMotivation == null) iconeMotivation = "fas fa-rocket";

    // URLs encodées
    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
%>



<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String coursPlanCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-plan.css\">";
    request.setAttribute("extraHead", coursPlanCSS);
%>

<%@ include file="../header.jsp" %>

<div class="cours-plan-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="breadcrumb-link">
            <span><%= cours.getTheme() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" class="breadcrumb-link">
            <span><%= cours.getNiveauFormate() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-sitemap"></i><span>Plan du cours</span>
        </span>
    </nav>

    <%-- ============================================================
         EN-TÊTE DU COURS
         ============================================================ --%>
    <header class="plan-header">
        <div class="plan-header-top">
            <div class="plan-header-icon" style="background: linear-gradient(135deg, <%= cours.getCouleurTheme() %>, <%= cours.getCouleurTheme() %>dd);">
                <i class="<%= cours.getIconeTheme() %>"></i>
            </div>
            <div class="plan-header-info">
                <div class="plan-header-badges">
                    <span class="plan-badge-theme" style="background: <%= cours.getCouleurTheme() %>20; color: <%= cours.getCouleurTheme() %>;">
                        <i class="<%= cours.getIconeTheme() %>"></i> <%= cours.getTheme() %>
                    </span>
                    <span class="plan-badge-niveau" style="background: <%= cours.getCouleurNiveau() %>20; color: <%= cours.getCouleurNiveau() %>;">
                        <i class="<%= cours.getIconeNiveau() %>"></i> <%= cours.getNiveauFormate() %>
                    </span>
                </div>
                <h1><%= cours.getTitre() %></h1>
                <div class="plan-header-meta">
                    <span class="plan-meta-item">
                        <i class="fas fa-book-reader"></i> <%= tempsLectureTotal %> de lecture
                    </span>
                    <span class="plan-meta-separator">•</span>
                    <span class="plan-meta-item">
                        <i class="fas fa-list-ul"></i> <%= totalChapitres %> chapitres
                    </span>
                    <span class="plan-meta-separator">•</span>
                    <span class="plan-meta-item">
                        <i class="fas fa-file-alt"></i> <%= totalLecons %> leçons
                    </span>
                    <span class="plan-meta-separator">•</span>
                    <span class="plan-meta-item">
                        <i class="fas fa-code"></i> <%= totalExemplesCode %> exemples de code
                    </span>
                    <span class="plan-meta-separator">•</span>
                    <span class="plan-meta-item">
                        <i class="<%= cours.getIconeNiveau() %>"></i> <%= cours.getNiveauFormate() %>
                    </span>
                </div>
            </div>
            <div class="plan-header-statut">
                <% if (dejaCertifie) { %>
                    <div class="plan-statut-badge certifie">
                        <i class="fas fa-crown"></i>
                        <span>Certifié</span>
                    </div>
                <% } else if (coursTermine) { %>
                    <div class="plan-statut-badge termine">
                        <i class="fas fa-check-circle"></i>
                        <span>Cours terminé</span>
                    </div>
                <% } else if (pourcentageGlobal > 0) { %>
                    <div class="plan-statut-badge en-cours">
                        <i class="fas fa-spinner"></i>
                        <span>En cours</span>
                    </div>
                <% } else { %>
                    <div class="plan-statut-badge a-debuter">
                        <i class="fas fa-play-circle"></i>
                        <span>À débuter</span>
                    </div>
                <% } %>
            </div>
        </div>

        <%-- Barre de progression globale --%>
        <div class="plan-progress-global">
            <div class="plan-progress-header">
                <span><i class="fas fa-chart-simple"></i> Progression globale du cours</span>
                <span><strong><%= pourcentageGlobal %>%</strong></span>
            </div>
            <div class="plan-progress-bar">
                <div class="plan-progress-fill" style="width: <%= pourcentageGlobal %>%; background: linear-gradient(90deg, <%= cours.getCouleurTheme() %>, <%= cours.getCouleurTheme() %>cc);"></div>
            </div>
            <div class="plan-progress-detail">
                <span><i class="fas fa-check-circle"></i> <%= totalLeconsTerminees %> / <%= totalLecons %> leçons terminées</span>
                <span><i class="fas fa-layer-group"></i> <%= chapitresTermines %> / <%= totalChapitres %> chapitres terminés</span>
            </div>
        </div>

        <%-- Message de motivation --%>
        <div class="plan-motivation" style="border-left-color: <%= cours.getCouleurTheme() %>;">
            <i class="<%= iconeMotivation %>" style="color: <%= cours.getCouleurTheme() %>;"></i>
            <p><%= messageMotivation %></p>
            <% if (prochainChapitreId != null && prochainChapitreTitre != null) { %>
                <a href="${pageContext.request.contextPath}/joueur/cours/chapitre?coursId=<%= cours.getId() %>&chapitreId=<%= prochainChapitreId %>" 
                   class="plan-motivation-btn" style="background: <%= cours.getCouleurTheme() %>;">
                    <i class="fas fa-play"></i> Continuer : <%= prochainChapitreTitre %>
                </a>
            <% } else if (examenAccessible) { %>
                <a href="${pageContext.request.contextPath}/joueur/cours/examen?coursId=<%= cours.getId() %>" 
                   class="plan-motivation-btn" style="background: #f39c12;">
                    <i class="fas fa-trophy"></i> Passer l'examen final
                </a>
            <% } else if (dejaCertifie) { %>
                <a href="${pageContext.request.contextPath}/joueur/cours/certificat?coursId=<%= cours.getId() %>" 
                   class="plan-motivation-btn" style="background: #9b59b6;">
                    <i class="fas fa-certificate"></i> Voir mon certificat
                </a>
            <% } %>
        </div>
    </header>

    <%-- ============================================================
         DESCRIPTION ET OBJECTIFS DU COURS
         ============================================================ --%>
    <div class="plan-description-section">
        <% if (cours.getDescription() != null && !cours.getDescription().isEmpty()) { %>
            <div class="plan-description-box">
                <h3><i class="fas fa-info-circle"></i> Description du cours</h3>
                <p><%= cours.getDescription() %></p>
            </div>
        <% } %>

        <div class="plan-details-grid">
            <% if (cours.getObjectifs() != null && !cours.getObjectifs().isEmpty()) { %>
                <div class="plan-detail-card">
                    <h4><i class="fas fa-bullseye"></i> Objectifs pédagogiques</h4>
                    <p><%= cours.getObjectifs() %></p>
                </div>
            <% } %>

            <% if (cours.getPrerequis() != null && !cours.getPrerequis().isEmpty()) { %>
                <div class="plan-detail-card">
                    <h4><i class="fas fa-clipboard-list"></i> Prérequis</h4>
                    <p><%= cours.getPrerequis() %></p>
                </div>
            <% } %>

            <div class="plan-detail-card">
                <h4><i class="fas fa-chart-bar"></i> Statistiques du cours</h4>
                <div class="plan-stats-grid">
                    <div class="plan-stat-mini">
                        <i class="fas fa-book-reader"></i>
                        <strong><%= tempsLectureTotal %></strong>
                        <span>de lecture</span>
                    </div>
                    <div class="plan-stat-mini">
                        <i class="fas fa-code"></i>
                        <strong><%= totalExemplesCode %></strong>
                        <span>exemples de code</span>
                    </div>
                    <div class="plan-stat-mini">
                        <i class="fas fa-file-alt"></i>
                        <strong><%= totalLecons %></strong>
                        <span>leçons</span>
                    </div>
                    <div class="plan-stat-mini">
                        <i class="fas fa-trophy"></i>
                        <strong>80%</strong>
                        <span>pour réussir</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%-- ============================================================
         ARBORESCENCE : PLAN DU COURS
         ============================================================ --%>
    <section class="plan-arbrescence">
        <div class="section-header">
            <h2>
                <i class="fas fa-sitemap"></i>
                Plan du cours
            </h2>
            <span class="badge-count" style="background: <%= cours.getCouleurTheme() %>;">
                <i class="fas fa-list-ul"></i>
                <%= totalChapitres %> chapitre<%= totalChapitres > 1 ? "s" : "" %>
            </span>
        </div>

        <div class="arbrescence">
            <% if (chapitresAvecProgression != null && !chapitresAvecProgression.isEmpty()) {
                for (int i = 0; i < chapitresAvecProgression.size(); i++) {
                    Map<String, Object> chapData = chapitresAvecProgression.get(i);
                    Chapitre chapitre = (Chapitre) chapData.get("chapitre");
                    int ordre = (Integer) chapData.get("ordre");
                    boolean termine = (Boolean) chapData.get("termine");
                    boolean accessible = (Boolean) chapData.get("accessible");
                    String statut = (String) chapData.get("statut");
                    String iconeStatut = (String) chapData.get("iconeStatut");
                    String couleurStatut = (String) chapData.get("couleurStatut");
                    int nbLecons = (Integer) chapData.get("nbLecons");
                    int leconsTerminees = (Integer) chapData.get("leconsTerminees");
                    int pourcentageChapitre = (Integer) chapData.get("pourcentageChapitre");
                    List<Map<String, Object>> lecons = (List<Map<String, Object>>) chapData.get("lecons");
                    boolean estDernier = (i == chapitresAvecProgression.size() - 1);
                    
                    // Compter les exemples de code dans ce chapitre
                    int exemplesCodeChapitre = 0;
                    if (lecons != null) {
                        for (Map<String, Object> lData : lecons) {
                            Lecon l = (Lecon) lData.get("lecon");
                            if (l.hasCodeSource()) exemplesCodeChapitre++;
                        }
                    }
            %>
                <%-- ===== CHAPITRE ===== --%>
                <div class="chapitre-node <%= termine ? "termine" : (accessible ? "actif" : "verrouille") %>" id="chapitre-<%= ordre %>">
                    
                    <%-- Connecteur vertical --%>
                    <div class="chapitre-connector">
                        <div class="connector-dot <%= termine ? "done" : (accessible ? "active" : "locked") %>" 
                             style="<%= accessible || termine ? "background: " + couleurStatut + "; border-color: " + couleurStatut + ";" : "" %>">
                            <% if (termine) { %>
                                <i class="fas fa-check"></i>
                            <% } else if (accessible) { %>
                                <span><%= ordre %></span>
                            <% } else { %>
                                <i class="fas fa-lock"></i>
                            <% } %>
                        </div>
                        <% if (!estDernier) { %>
                            <div class="connector-line <%= termine ? "done" : "" %>"></div>
                        <% } %>
                    </div>

                    <%-- Contenu du chapitre --%>
                    <div class="chapitre-content">
                        <div class="chapitre-header">
                            <div class="chapitre-header-left">
                                <h3>
                                    <span class="chapitre-numero">Chapitre <%= ordre %></span>
                                    <%= chapitre.getTitre() %>
                                </h3>
                                <div class="chapitre-meta">
                                    <span class="chapitre-meta-item">
                                        <i class="fas fa-file-alt"></i> <%= nbLecons %> leçon<%= nbLecons > 1 ? "s" : "" %>
                                    </span>
                                    <% if (exemplesCodeChapitre > 0) { %>
                                        <span class="chapitre-meta-separator">•</span>
                                        <span class="chapitre-meta-item">
                                            <i class="fas fa-code"></i> <%= exemplesCodeChapitre %> exemple<%= exemplesCodeChapitre > 1 ? "s" : "" %> de code
                                        </span>
                                    <% } %>
                                    <% if (pourcentageChapitre > 0) { %>
                                        <span class="chapitre-meta-separator">•</span>
                                        <span class="chapitre-meta-item">
                                            <i class="fas fa-check-circle"></i> <%= leconsTerminees %>/<%= nbLecons %> terminées
                                        </span>
                                    <% } %>
                                </div>
                            </div>
                            <div class="chapitre-header-right">
                                <span class="chapitre-statut-badge" style="color: <%= couleurStatut %>; background: <%= couleurStatut %>15;">
                                    <i class="<%= iconeStatut %>"></i> <%= statut %>
                                </span>
                                <% if (accessible) { %>
                                    <a href="${pageContext.request.contextPath}/joueur/cours/chapitre?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>" 
                                       class="btn-acceder-chapitre" style="background: <%= cours.getCouleurTheme() %>;">
                                        <i class="fas fa-eye"></i> Accéder
                                    </a>
                                <% } %>
                            </div>
                        </div>

                        <%-- Description du chapitre --%>
                        <% if (chapitre.getDescription() != null && !chapitre.getDescription().isEmpty()) { %>
                            <p class="chapitre-description"><%= chapitre.getDescription() %></p>
                        <% } %>

                        <%-- Barre de progression du chapitre --%>
                        <% if (nbLecons > 0) { %>
                            <div class="chapitre-mini-progress">
                                <div class="chapitre-mini-bar">
                                    <div class="chapitre-mini-fill" style="width: <%= pourcentageChapitre %>%; background: <%= couleurStatut %>;"></div>
                                </div>
                                <span class="chapitre-mini-pourcentage"><%= pourcentageChapitre %>%</span>
                            </div>
                        <% } %>

                        <%-- Liste des leçons --%>
                        <% if (lecons != null && !lecons.isEmpty()) { %>
                            <ul class="lecons-liste">
                                <% for (int j = 0; j < lecons.size(); j++) {
                                    Map<String, Object> leconData = lecons.get(j);
                                    Lecon lecon = (Lecon) leconData.get("lecon");
                                    boolean leconTerminee = (Boolean) leconData.get("terminee");
                                    boolean leconAccessible = (Boolean) leconData.get("accessible");
                                    int ordreLecon = (Integer) leconData.get("ordre");
                                %>
                                    <li class="lecon-item <%= leconTerminee ? "terminee" : (!leconAccessible ? "verrouillee" : "") %>">
                                        <span class="lecon-dot">
                                            <% if (leconTerminee) { %>
                                                <i class="fas fa-check-circle" style="color: #27ae60;"></i>
                                            <% } else if (leconAccessible) { %>
                                                <i class="fas fa-circle" style="color: <%= cours.getCouleurTheme() %>; font-size: 8px;"></i>
                                            <% } else { %>
                                                <i class="fas fa-lock" style="color: #ccc; font-size: 10px;"></i>
                                            <% } %>
                                        </span>
                                        <span class="lecon-ordre"><%= ordre %>.<%= ordreLecon %></span>
                                        <span class="lecon-titre"><%= lecon.getTitre() %></span>
                                        <span class="lecon-badges">
                                            <% if (lecon.hasCodeSource()) { %>
                                                <span class="lecon-badge code" title="Exemple de code inclus">
                                                    <i class="fas fa-code"></i> Code
                                                </span>
                                            <% } %>
                                            <span class="lecon-badge lecture" title="Temps de lecture : <%= lecon.getTempsLectureFormate() %>">
                                                <i class="fas fa-book-reader"></i> <%= lecon.getTempsLectureFormate() %>
                                            </span>
                                            <% if (lecon.isGratuit()) { %>
                                                <span class="lecon-badge gratuit">Gratuit</span>
                                            <% } else { %>
                                                <span class="lecon-badge premium">Premium</span>
                                            <% } %>
                                        </span>
                                        <% if (leconAccessible && !leconTerminee) { %>
                                            <a href="${pageContext.request.contextPath}/joueur/cours/lecon?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>&leconId=<%= lecon.getId() %>" 
                                               class="lecon-lien" title="Accéder à la leçon">
                                                <i class="fas fa-arrow-right"></i>
                                            </a>
                                        <% } %>
                                    </li>
                                <% } %>
                            </ul>
                        <% } %>
                    </div>
                </div>
            <%  }
            } else { %>
                <div class="empty-state">
                    <div class="empty-icon"><i class="fas fa-inbox"></i></div>
                    <h3>Aucun chapitre trouvé</h3>
                    <p>Le plan de ce cours n'est pas encore disponible.</p>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         SECTION EXAMEN FINAL
         ============================================================ --%>
    <section class="examen-final-section">
        <div class="examen-final-card <%= dejaCertifie ? "certifie" : (examenAccessible ? "accessible" : "verrouille") %>">
            
            <%-- Icône --%>
            <div class="examen-final-icon">
                <% if (dejaCertifie) { %>
                    <i class="fas fa-crown"></i>
                <% } else if (examenAccessible) { %>
                    <i class="fas fa-trophy"></i>
                <% } else { %>
                    <i class="fas fa-lock"></i>
                <% } %>
            </div>

            <%-- Contenu --%>
            <div class="examen-final-content">
                <h3>
                    <% if (dejaCertifie) { %>
                        🎉 Certificat déjà obtenu !
                    <% } else if (examenAccessible) { %>
                        🏁 Examen Final
                    <% } else { %>
                        🔒 Examen Final
                    <% } %>
                </h3>
                <p>
                    <% if (dejaCertifie) { %>
                        Vous avez déjà réussi l'examen final de ce cours avec une mention 
                        <strong style="color: <%= certificatExistant.getCouleurMention() %>;">
                            <%= certificatExistant.getMention() %>
                        </strong>.
                    <% } else if (examenAccessible) { %>
                        Tous les chapitres sont terminés ! Vous pouvez maintenant passer l'examen final 
                        pour valider vos connaissances et obtenir votre <strong>certificat vérifiable</strong>.
                        <br>Score minimum requis : <strong>80%</strong>.
                    <% } else { %>
                        Terminez tous les chapitres du cours pour débloquer l'examen final.
                        <br>Progression actuelle : <strong><%= pourcentageGlobal %>%</strong> 
                        (il reste <strong><%= totalChapitres - chapitresTermines %></strong> chapitre<%= (totalChapitres - chapitresTermines) > 1 ? "s" : "" %>).
                    <% } %>
                </p>
            </div>

            <%-- Action --%>
            <div class="examen-final-action">
                <% if (dejaCertifie) { %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/certificat?coursId=<%= cours.getId() %>" 
                       class="btn-examen btn-voir-certificat">
                        <i class="fas fa-certificate"></i> Voir mon certificat
                    </a>
                <% } else if (examenAccessible) { %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/examen?coursId=<%= cours.getId() %>" 
                       class="btn-examen btn-passer-examen">
                        <i class="fas fa-play-circle"></i> Passer l'examen
                    </a>
                <% } else { %>
                    <span class="btn-examen btn-examen-verrouille" aria-disabled="true">
                        <i class="fas fa-lock"></i> Examen verrouillé
                    </span>
                <% } %>
            </div>

        </div>
    </section>

    <%-- ============================================================
         BOUTONS DE NAVIGATION
         ============================================================ --%>
    <div class="plan-navigation">
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" 
           class="btn-nav-plan btn-retour-niveau">
            <i class="fas fa-arrow-left"></i>
            <span>Retour au niveau <%= cours.getNiveauFormate() %></span>
        </a>

        <% if (prochainChapitreId != null && prochainChapitreTitre != null) { %>
            <a href="${pageContext.request.contextPath}/joueur/cours/chapitre?coursId=<%= cours.getId() %>&chapitreId=<%= prochainChapitreId %>" 
               class="btn-nav-plan btn-continuer">
                <span>Continuer le cours</span>
                <i class="fas fa-arrow-right"></i>
            </a>
        <% } else if (examenAccessible) { %>
            <a href="${pageContext.request.contextPath}/joueur/cours/examen?coursId=<%= cours.getId() %>" 
               class="btn-nav-plan btn-examen-final">
                <span>Passer l'examen final</span>
                <i class="fas fa-arrow-right"></i>
            </a>
        <% } %>
    </div>

</div>

<%-- ===== CHARGEMENT DES SCRIPTS ===== --%>
<%
    String coursPlanJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-plan.js\"></script>";
    request.setAttribute("extraScripts", coursPlanJS);
%>

<%@ include file="../footer.jsp" %>