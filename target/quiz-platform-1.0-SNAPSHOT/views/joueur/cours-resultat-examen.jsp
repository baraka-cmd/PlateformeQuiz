<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Question" %>
<%@ page import="com.amijayquiz.models.Certificat" %>

<%-- ============================================================
     Fichier : cours-resultat-examen.jsp
     Rôle    : Page de résultat après l'examen final
              Affiche le score, la mention, la correction
              et le certificat si réussi
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    List<Question> questions = (List<Question>) request.getAttribute("questions");
    List<Boolean> corrections = (List<Boolean>) request.getAttribute("corrections");
    Integer score = (Integer) request.getAttribute("score");
    Integer totalQuestions = (Integer) request.getAttribute("totalQuestions");
    Double pourcentage = (Double) request.getAttribute("pourcentage");
    Boolean estReussi = (Boolean) request.getAttribute("estReussi");
    Double seuilReussite = (Double) request.getAttribute("seuilReussite");
    Certificat certificat = (Certificat) request.getAttribute("certificat");

    if (cours == null || questions == null || corrections == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    if (score == null) score = 0;
    if (totalQuestions == null) totalQuestions = 0;
    if (pourcentage == null) pourcentage = 0.0;
    if (estReussi == null) estReussi = false;
    if (seuilReussite == null) seuilReussite = 80.0;

    String pourcentageStr = String.format("%.0f%%", pourcentage);
    String mention = certificat != null ? certificat.getMention() : "";
    String iconeMention = certificat != null ? certificat.getIconeMention() : "fas fa-medal";
    String couleurMention = certificat != null ? certificat.getCouleurMention() : "#888888";

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
%>



<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String resultatExamenCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-resultat-examen.css\">";
    request.setAttribute("extraHead", resultatExamenCSS);
%>

<%@ include file="../header.jsp" %>

<div class="resultat-examen-container">

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
            <i class="fas fa-chart-simple"></i><span>Résultat</span>
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
                    🎉 Examen Réussi !
                <% } else { %>
                    😞 Examen Non Réussi
                <% } %>
            </h1>
            <p class="score-cours"><%= cours.getTitre() %></p>

            <%-- Badge réussite/échec --%>
            <div class="score-badge <%= estReussi ? "badge-reussi" : "badge-echoue" %>">
                <i class="fas <%= estReussi ? "fa-check-circle" : "fa-times-circle" %>"></i>
                <span><%= estReussi ? "Félicitations !" : "Ne baissez pas les bras !" %></span>
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
                    <span class="stat-valeur"><%= String.format("%.0f", seuilReussite) %>%</span>
                    <span class="stat-label">Seuil requis</span>
                </div>
            </div>
        </div>
    </div>

    <%-- ============================================================
         MENTION (SI RÉUSSI)
         ============================================================ --%>
    <% if (estReussi && certificat != null) { %>
        <div class="mention-card">
            <div class="mention-icon" style="color: <%= couleurMention %>;">
                <i class="<%= iconeMention %>"></i>
            </div>
            <h2 style="color: <%= couleurMention %>;">Mention : <%= mention %></h2>
            <p class="mention-code">
                <i class="fas fa-qrcode"></i> 
                Code de vérification : <strong><%= certificat.getCodeVerification() %></strong>
            </p>
        </div>
    <% } %>

    <%-- ============================================================
         ALERTE RÉUSSITE/ÉCHEC
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
                <h3>Certificat obtenu !</h3>
                <p>
                    Votre certificat est disponible avec le code <strong><%= certificat.getCodeVerification() %></strong>.
                    Vous pouvez le consulter, le télécharger et le partager sur vos réseaux professionnels.
                </p>
            <% } else { %>
                <h3>Pas assez de bonnes réponses</h3>
                <p>
                    Vous avez obtenu <strong><%= pourcentageStr %></strong>. 
                    Le minimum requis est <strong><%= String.format("%.0f", seuilReussite) %>%</strong>. 
                    Révisez les chapitres et réessayez !
                </p>
            <% } %>
        </div>
        <div class="alerte-actions">
            <% if (estReussi && certificat != null) { %>
                <a href="${pageContext.request.contextPath}/joueur/cours/certificat?coursId=<%= cours.getId() %>" class="btn-action btn-certificat">
                    <i class="fas fa-certificate"></i> Voir mon certificat
                </a>
                <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="btn-action btn-plan">
                    <i class="fas fa-sitemap"></i> Retour au plan
                </a>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="btn-action btn-reviser">
                    <i class="fas fa-book"></i> Réviser les chapitres
                </a>
                <a href="${pageContext.request.contextPath}/joueur/cours/examen?coursId=<%= cours.getId() %>" class="btn-action btn-reessayer">
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
                   Question q = questions.get(i);
                   boolean estCorrecte = corrections.get(i);
                   
                   String reponseParam = request.getParameter("reponse_" + i);
                   int reponseJoueur = 0;
                   if (reponseParam != null && !reponseParam.isEmpty()) {
                       try { reponseJoueur = Integer.parseInt(reponseParam); } 
                       catch (NumberFormatException e) { reponseJoueur = 0; }
                   }
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
                        <p class="correction-question"><%= q.getTexteQuestion() %></p>

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
                                Bonne réponse : <strong><%= lettres[q.getBonneReponse() - 1] %>. <%= q.getProposition(q.getBonneReponse()) %></strong>
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
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="btn-action-bottom btn-plan">
            <i class="fas fa-sitemap"></i> Plan du cours
        </a>
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" class="btn-action-bottom btn-niveau">
            <i class="fas fa-layer-group"></i> Niveau <%= cours.getNiveauFormate() %>
        </a>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-action-bottom btn-tous-cours">
            <i class="fas fa-graduation-cap"></i> Tous les cours
        </a>
        <% if (estReussi && certificat != null) { %>
            <a href="${pageContext.request.contextPath}/joueur/cours/certificat?coursId=<%= cours.getId() %>" class="btn-action-bottom btn-certificat-bottom">
                <i class="fas fa-certificate"></i> Mon certificat
            </a>
        <% } %>
    </div>

</div>

<%-- ===== CHARGEMENT DES SCRIPTS ===== --%>
<%
    String resultatExamenJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-resultat-examen.js\"></script>";
    request.setAttribute("extraScripts", resultatExamenJS);
%>

<%@ include file="../footer-connecte.jsp" %>