<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.ExamenModule" %>
<%@ page import="com.amijayquiz.models.QuestionExamen" %>

<%-- ============================================================
     Fichier : cours-resultat-module.jsp
     Rôle    : Page de résultat après l'examen d'un module
              Affiche le score, le pourcentage, succès/échec
              Correction détaillée question par question
              Boutons : réessayer, module suivant, plan du cours
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    ExamenModule examen = (ExamenModule) request.getAttribute("examen");
    List<QuestionExamen> questions = (List<QuestionExamen>) request.getAttribute("questions");
    int[] reponsesJoueur = (int[]) request.getAttribute("reponsesJoueur");
    List<Boolean> corrections = (List<Boolean>) request.getAttribute("corrections");
    Integer score = (Integer) request.getAttribute("score");
    Integer totalQuestions = (Integer) request.getAttribute("totalQuestions");
    Double pourcentage = (Double) request.getAttribute("pourcentage");
    Boolean estReussi = (Boolean) request.getAttribute("estReussi");
    Double seuilReussite = (Double) request.getAttribute("seuilReussite");
    Integer scoreMinimum = (Integer) request.getAttribute("scoreMinimum");
    Integer moduleSuivant = (Integer) request.getAttribute("moduleSuivant");
    Boolean moduleSuivantDebloque = (Boolean) request.getAttribute("moduleSuivantDebloque");

    if (cours == null || examen == null || questions == null || corrections == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    if (score == null) score = 0;
    if (totalQuestions == null) totalQuestions = 0;
    if (pourcentage == null) pourcentage = 0.0;
    if (estReussi == null) estReussi = false;
    if (seuilReussite == null) seuilReussite = 75.0;
    if (scoreMinimum == null) scoreMinimum = 15;
    if (moduleSuivant == null) moduleSuivant = examen.getModuleNumero() + 1;
    if (moduleSuivantDebloque == null) moduleSuivantDebloque = false;

    String pourcentageStr = String.format("%.0f%%", pourcentage);
    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
    
    // Déterminer le niveau de réussite
    String niveauReussiteLibelle;
    String iconeNiveauReussite;
    String couleurNiveauReussite;
    if (pourcentage == 100) {
        niveauReussiteLibelle = "Parfait";
        iconeNiveauReussite = "fas fa-trophy";
        couleurNiveauReussite = "#ffd700";
    } else if (pourcentage >= 90) {
        niveauReussiteLibelle = "Excellent";
        iconeNiveauReussite = "fas fa-star";
        couleurNiveauReussite = "#27ae60";
    } else if (pourcentage >= 80) {
        niveauReussiteLibelle = "Très Bien";
        iconeNiveauReussite = "fas fa-thumbs-up";
        couleurNiveauReussite = "#2980b9";
    } else if (pourcentage >= 75) {
        niveauReussiteLibelle = "Bien";
        iconeNiveauReussite = "fas fa-check-circle";
        couleurNiveauReussite = "#667eea";
    } else if (pourcentage >= 50) {
        niveauReussiteLibelle = "Insuffisant";
        iconeNiveauReussite = "fas fa-exclamation-circle";
        couleurNiveauReussite = "#f39c12";
    } else {
        niveauReussiteLibelle = "À réviser";
        iconeNiveauReussite = "fas fa-book-open";
        couleurNiveauReussite = "#e74c3c";
    }
%>



<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String resultatModuleCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-resultat-module.css\">";
    request.setAttribute("extraHead", resultatModuleCSS);
%>

<%@ include file="../header.jsp" %>

<div class="resultat-module-container">

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
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="breadcrumb-link">
            <i class="fas fa-sitemap"></i><span>Plan</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-chart-simple"></i><span>Résultat <%= examen.getModuleFormate() %></span>
        </span>
    </nav>

    <%-- ============================================================
         CARTE SCORE PRINCIPALE
         ============================================================ --%>
    <div class="score-card <%= estReussi ? "reussi" : "echoue" %>">
        <div class="score-header">
            <%-- Cercle de score --%>
            <div class="score-circle <%= estReussi ? "circle-reussi" : "circle-echoue" %>">
                <span class="score-valeur"><%= pourcentageStr %></span>
                <span class="score-detail"><%= score %> / <%= totalQuestions %></span>
            </div>

            <h1 class="score-titre">
                <% if (estReussi) { %>
                    🎉 Module Réussi !
                <% } else { %>
                    😞 Module Non Réussi
                <% } %>
            </h1>
            <p class="score-module"><%= examen.getModuleFormate() %> - <%= examen.getTitre() %></p>

            <%-- Badge réussite/échec --%>
            <div class="score-badge <%= estReussi ? "badge-reussi" : "badge-echoue" %>">
                <i class="fas <%= estReussi ? "fa-check-circle" : "fa-times-circle" %>"></i>
                <span>
                    <% if (estReussi) { %>
                        Félicitations ! Module validé !
                    <% } else { %>
                        Score insuffisant. Réessayez !
                    <% } %>
                </span>
            </div>
        </div>

        <%-- Statistiques --%>
        <div class="score-stats">
            <div class="stat-item">
                <div class="stat-icon stat-icon-score">
                    <i class="fas fa-star"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-valeur"><%= score %> / <%= totalQuestions %></span>
                    <span class="stat-label">Bonnes réponses</span>
                </div>
            </div>

            <div class="stat-item">
                <div class="stat-icon stat-icon-pourcentage">
                    <i class="fas fa-chart-line"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-valeur"><%= pourcentageStr %></span>
                    <span class="stat-label">Pourcentage</span>
                </div>
            </div>

            <div class="stat-item">
                <div class="stat-icon stat-icon-seuil">
                    <i class="fas fa-flag-checkered"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-valeur"><%= scoreMinimum %>/<%= totalQuestions %></span>
                    <span class="stat-label">Score minimum requis</span>
                </div>
            </div>
        </div>
    </div>

    <%-- ============================================================
         NIVEAU DE RÉUSSITE
         ============================================================ --%>
    <div class="performance-card">
        <div class="performance-icon" style="color: <%= couleurNiveauReussite %>;">
            <i class="<%= iconeNiveauReussite %>"></i>
        </div>
        <h3 style="color: <%= couleurNiveauReussite %>;"><%= niveauReussiteLibelle %></h3>
        <p>
            <% if (estReussi) { %>
                Vous maîtrisez les notions de ce module. 
                <% if (moduleSuivantDebloque) { %>
                    Le <strong><%= examen.getModuleFormate().replace("Module ", "Module ") %> suivant</strong> est maintenant débloqué !
                <% } %>
            <% } else { %>
                Il vous manque <strong><%= scoreMinimum - score %></strong> bonne(s) réponse(s) pour valider ce module. Révisez et réessayez !
            <% } %>
        </p>
    </div>

    <%-- ============================================================
         ALERTE PROGRESSION
         ============================================================ --%>
    <div class="alerte-resultat <%= estReussi ? "alerte-reussi" : "alerte-echoue" %>">
        <div class="alerte-icon">
            <% if (estReussi) { %>
                <i class="fas fa-trophy"></i>
            <% } else { %>
                <i class="fas fa-redo"></i>
            <% } %>
        </div>
        <div class="alerte-content">
            <% if (estReussi) { %>
                <h3><%= examen.getModuleFormate() %> validé !</h3>
                <p>
                    Vous avez obtenu <strong><%= pourcentageStr %></strong>. 
                    Le seuil requis était de <strong><%= String.format("%.0f", seuilReussite) %>%</strong>.
                    <% if (moduleSuivantDebloque) { %>
                        Vous pouvez maintenant accéder au <strong><%= examen.getModuleFormate().replace(examen.getModuleNumero() + "", (examen.getModuleNumero() + 1) + "") %> suivant</strong>.
                    <% } %>
                </p>
            <% } else { %>
                <h3>Score insuffisant</h3>
                <p>
                    Vous avez obtenu <strong><%= pourcentageStr %></strong>. 
                    Le minimum requis est <strong><%= String.format("%.0f", seuilReussite) %>%</strong> 
                    (<%= scoreMinimum %>/<%= totalQuestions %>). Révisez et réessayez !
                </p>
            <% } %>
        </div>
        <div class="alerte-actions">
            <% if (estReussi) { %>
                <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" 
                   class="btn-action btn-plan">
                    <i class="fas fa-sitemap"></i> Retour au plan
                </a>
                <% if (moduleSuivantDebloque && moduleSuivant <= 15) { %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/examen-module?coursId=<%= cours.getId() %>&module=<%= moduleSuivant %>" 
                       class="btn-action btn-module-suivant">
                        <i class="fas fa-arrow-right"></i> Module suivant
                    </a>
                <% } %>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" 
                   class="btn-action btn-reviser">
                    <i class="fas fa-book"></i> Réviser le module
                </a>
                <a href="${pageContext.request.contextPath}/joueur/cours/examen-module?coursId=<%= cours.getId() %>&module=<%= examen.getModuleNumero() %>" 
                   class="btn-action btn-reessayer">
                    <i class="fas fa-redo"></i> Réessayer l'examen
                </a>
            <% } %>
        </div>
    </div>

    <%-- ============================================================
         CORRECTION DÉTAILLÉE
         ============================================================ --%>
    <div class="correction-card">
        <div class="correction-header">
            <i class="fas fa-list-ul"></i>
            <span>Correction détaillée</span>
            <span class="correction-stats">
                <span class="corr-ok"><i class="fas fa-check-circle"></i> <%= score %> correctes</span>
                <span class="corr-sep">|</span>
                <span class="corr-ko"><i class="fas fa-times-circle"></i> <%= totalQuestions - score %> incorrectes</span>
            </span>
        </div>

        <div class="correction-body">
            <% String[] lettres = {"A", "B", "C", "D"};
               for (int i = 0; i < questions.size(); i++) {
                   QuestionExamen q = questions.get(i);
                   boolean estCorrecte = corrections.get(i);
                   int reponseJoueur = reponsesJoueur != null ? reponsesJoueur[i] : 0;
            %>
                <div class="correction-item <%= estCorrecte ? "correct" : "incorrect" %>">
                    <div class="correction-item-header">
                        <span class="correction-numero">Question <%= i + 1 %></span>
                        <span class="correction-badge <%= estCorrecte ? "badge-ok" : "badge-ko" %>">
                            <i class="fas <%= estCorrecte ? "fa-check-circle" : "fa-times-circle" %>"></i>
                            <%= estCorrecte ? "Correct" : "Incorrect" %>
                        </span>
                    </div>
                    <div class="correction-item-body">
                        <p class="correction-question"><%= q.getQuestion() %></p>

                        <% if (estCorrecte) { %>
                            <div class="reponse-detail reponse-ok">
                                <i class="fas fa-check-circle"></i>
                                Votre réponse : <strong><%= reponseJoueur > 0 ? lettres[reponseJoueur - 1] + ". " + q.getProposition(reponseJoueur) : "Aucune réponse" %></strong>
                            </div>
                        <% } else { %>
                            <div class="reponse-detail reponse-ko">
                                <i class="fas fa-times-circle"></i>
                                Votre réponse : <strong><%= reponseJoueur > 0 ? lettres[reponseJoueur - 1] + ". " + q.getProposition(reponseJoueur) : "Aucune réponse" %></strong>
                            </div>
                            <div class="reponse-detail reponse-correcte">
                                <i class="fas fa-check-circle"></i>
                                Bonne réponse : <strong><%= q.getLettreBonneReponse() %>. <%= q.getTexteBonneReponse() %></strong>
                            </div>
                        <% } %>
                    </div>
                </div>
            <% } %>
        </div>
    </div>

    <%-- ============================================================
         BOUTONS D'ACTION
         ============================================================ --%>
    <div class="actions-group">
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" 
           class="btn-action-bottom btn-plan-retour">
            <i class="fas fa-sitemap"></i> Plan du cours
        </a>
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" 
           class="btn-action-bottom btn-niveau">
            <i class="fas fa-layer-group"></i> Niveau <%= cours.getNiveauFormate() %>
        </a>
        <a href="${pageContext.request.contextPath}/joueur/cours" 
           class="btn-action-bottom btn-tous-cours">
            <i class="fas fa-graduation-cap"></i> Tous les cours
        </a>
    </div>

</div>

<%-- ============================================================
     SCRIPTS
     ============================================================ --%>
<%
    String resultatModuleJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-resultat-module.js\"></script>";
    request.setAttribute("extraScripts", resultatModuleJS);
%>

<%@ include file="../footer-connecte.jsp" %>