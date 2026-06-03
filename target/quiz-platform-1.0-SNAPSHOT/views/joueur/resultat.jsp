<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Quiz" %>
<%@ page import="com.amijayquiz.models.Question" %>
<%@ page import="java.util.List" %>

<%-- ============================================================
     Fichier : resultat.jsp
     Rôle    : Page de résultat après avoir terminé un quiz
              - Cercle de score animé avec pourcentage
              - Statistiques (bonnes réponses, temps, pourcentage)
              - Alerte de réussite ou d'échec avec actions
              - Message de niveau de performance (Parfait, Excellent...)
              - Détail question par question (correction complète)
              - Boutons d'action (dashboard, historique, suite)
              - Breadcrumb de navigation
     ============================================================ --%>

<%
    // Récupération des attributs depuis le servlet
    Quiz quiz = (Quiz) request.getAttribute("quiz");
    List<Question> listeQuestions = (List<Question>) request.getAttribute("listeQuestions");
    int[] reponsesJoueur = (int[]) request.getAttribute("reponsesJoueur");
    List<Boolean> corrections = (List<Boolean>) request.getAttribute("corrections");
    Integer score = (Integer) request.getAttribute("score");
    Integer totalQuestions = (Integer) request.getAttribute("totalQuestions");
    Double pourcentage = (Double) request.getAttribute("pourcentage");
    Integer tempsRealiseSecondes = (Integer) request.getAttribute("tempsRealiseSecondes");
    String niveauReussite = (String) request.getAttribute("niveauReussite");
    String iconeNiveauReussite = (String) request.getAttribute("iconeNiveau");
    String couleurNiveauReussite = (String) request.getAttribute("couleurNiveau");
    
    // Attributs de progression
    Boolean partieReussie = (Boolean) request.getAttribute("partieReussie");
    Integer seuilReussite = (Integer) request.getAttribute("seuilReussite");
    Boolean niveauTermine = (Boolean) request.getAttribute("niveauTermine");
    String niveauSuperieur = (String) request.getAttribute("niveauSuperieur");
    Quiz quizSuivant = (Quiz) request.getAttribute("quizSuivant");
    Quiz premierQuizNiveauSuperieur = (Quiz) request.getAttribute("premierQuizNiveauSuperieur");
    String categorieQuiz = (String) request.getAttribute("categorieQuiz");
    Integer partiesReussiesNiveau = (Integer) request.getAttribute("partiesReussiesNiveau");
    Integer totalPartiesNiveau = (Integer) request.getAttribute("totalPartiesNiveau");
    
    // Sécurité
    if (quiz == null || listeQuestions == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
        return;
    }
    
    // Formatages
    int minutes = tempsRealiseSecondes / 60;
    int secondes = tempsRealiseSecondes % 60;
    String tempsFormate = minutes + " min " + secondes + " s";
    String pourcentageStr = String.format("%.0f%%", pourcentage);
    
    // Niveau supérieur formaté
    String niveauSuperieurFormate = "";
    if ("moyen".equals(niveauSuperieur)) {
        niveauSuperieurFormate = "Moyen";
    } else if ("avance".equals(niveauSuperieur)) {
        niveauSuperieurFormate = "Avancé";
    }
    
    // URLs encodées pour les liens
    String categorieEncodee = categorieQuiz != null ? java.net.URLEncoder.encode(categorieQuiz, "UTF-8") : "";
    String niveauEncode = quiz.getNiveau() != null ? java.net.URLEncoder.encode(quiz.getNiveau(), "UTF-8") : "";
    
    // Message de performance
    String messagePerformance = "";
    if (pourcentage == 100) {
        messagePerformance = "Incroyable ! Un sans-faute ! Vous maîtrisez parfaitement ce sujet.";
    } else if (pourcentage >= 80) {
        messagePerformance = "Excellent travail ! Vous maîtrisez très bien ce sujet.";
    } else if (pourcentage >= 60) {
        messagePerformance = "Bon travail ! Continuez à vous entraîner pour progresser.";
    } else if (pourcentage >= 40) {
        messagePerformance = "Pas mal, mais vous pouvez encore vous améliorer. Réessayez !";
    } else if (pourcentage >= 20) {
        messagePerformance = "Quelques révisions sont nécessaires. Ne baissez pas les bras !";
    } else {
        messagePerformance = "Beaucoup de révisions sont nécessaires. Concentrez-vous sur vos points faibles.";
    }
    
    // Titre de la page
    request.setAttribute("pageTitle", "Résultat - " + quiz.getTitre() + " - AmijayQuiz");
%>



<%-- ============================================================
     STYLES SPÉCIFIQUES
     ============================================================ --%>
<%
    String resultatCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/resultat.css\">";
    request.setAttribute("extraHead", resultatCSS);
%>

<%@ include file="../header.jsp" %>

<div class="resultat-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i> Dashboard
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/theme?categorie=<%= categorieEncodee %>" class="breadcrumb-link">
            <i class="fas fa-layer-group"></i> <%= categorieQuiz %>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/niveau?categorie=<%= categorieEncodee %>&niveau=<%= niveauEncode %>" class="breadcrumb-link">
            <i class="<%= quiz.getIconeNiveau() %>"></i> <%= quiz.getNiveauFormate() %>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-chart-simple"></i> Résultat
        </span>
    </nav>

    <%-- ============================================================
         CARTE SCORE PRINCIPAL
         ============================================================ --%>
    <div class="score-card">
        <div class="score-header">
            <%-- Cercle de score animé --%>
            <div class="score-circle <%= partieReussie != null && partieReussie ? "score-reussi" : "score-echoue" %>">
                <span class="score-valeur"><%= pourcentageStr %></span>
                <span class="score-total"><%= score %> / <%= totalQuestions %></span>
            </div>
            
            <h2 class="score-titre">Quiz terminé !</h2>
            <p class="score-quiz-nom"><%= quiz.getTitre() %></p>
            
            <%-- Badge réussite/échec --%>
            <div class="score-badge <%= partieReussie != null && partieReussie ? "badge-reussi" : "badge-echoue" %>">
                <i class="fas <%= partieReussie != null && partieReussie ? "fa-check-circle" : "fa-times-circle" %>"></i>
                <span><%= partieReussie != null && partieReussie ? "Partie réussie !" : "Partie non réussie" %></span>
            </div>
        </div>
        
        <%-- Statistiques --%>
        <div class="score-stats">
            <div class="stat-item">
                <div class="stat-icon stat-icon-correct">
                    <i class="fas fa-check-circle"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-valeur"><%= score %></span>
                    <span class="stat-label">Bonnes réponses</span>
                </div>
            </div>
            
            <div class="stat-item">
                <div class="stat-icon stat-icon-time">
                    <i class="fas fa-clock"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-valeur"><%= tempsFormate %></span>
                    <span class="stat-label">Temps réalisé</span>
                </div>
            </div>
            
            <div class="stat-item">
                <div class="stat-icon stat-icon-percent">
                    <i class="fas fa-chart-line"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-valeur"><%= pourcentageStr %></span>
                    <span class="stat-label">Pourcentage</span>
                </div>
            </div>
        </div>
    </div>

    <%-- ============================================================
         ALERTE PROGRESSION (RÉUSSITE OU ÉCHEC)
         ============================================================ --%>
    <% if (partieReussie != null && partieReussie) { %>
        <div class="alerte-progression alerte-reussie">
            <div class="alerte-icon-wrapper">
                <i class="fas fa-trophy"></i>
            </div>
            <div class="alerte-content">
                <h3>🎉 Partie réussie !</h3>
                <p>
                    Vous avez obtenu <strong><%= score %>/<%= totalQuestions %></strong> 
                    (seuil minimum : <strong><%= seuilReussite %>/<%= totalQuestions %></strong>).
                    <br>Progression du niveau : <strong><%= partiesReussiesNiveau %>/<%= totalPartiesNiveau %> parties</strong>.
                </p>
            </div>
            <div class="alerte-actions">
                <% if (niveauTermine != null && niveauTermine && premierQuizNiveauSuperieur != null) { %>
                    <a href="${pageContext.request.contextPath}/joueur/niveau?categorie=<%= categorieEncodee %>&niveau=<%= java.net.URLEncoder.encode(niveauSuperieur, "UTF-8") %>" 
                       class="btn-progression btn-niveau-superieur">
                        <i class="fas fa-level-up-alt"></i>
                        Niveau <%= niveauSuperieurFormate %> débloqué !
                    </a>
                <% } else if (quizSuivant != null) { %>
                    <a href="${pageContext.request.contextPath}/joueur/demarrer-quiz?id=<%= quizSuivant.getId() %>" 
                       class="btn-progression btn-suite">
                        <i class="fas fa-arrow-right"></i>
                        Partie suivante
                    </a>
                <% } else { %>
                    <div class="alerte-trophee">
                        <i class="fas fa-trophy"></i>
                        <span>Toutes les parties de ce niveau sont terminées !</span>
                    </div>
                <% } %>
            </div>
        </div>
    <% } else { %>
        <div class="alerte-progression alerte-echec">
            <div class="alerte-icon-wrapper">
                <i class="fas fa-redo"></i>
            </div>
            <div class="alerte-content">
                <h3>Partie non réussie</h3>
                <p>
                    Vous avez obtenu <strong><%= score %>/<%= totalQuestions %></strong>.
                    Le minimum requis est <strong><%= seuilReussite %>/<%= totalQuestions %></strong>.
                    <br>Ne vous découragez pas, révisez et réessayez !
                </p>
            </div>
            <div class="alerte-actions">
                <a href="${pageContext.request.contextPath}/joueur/demarrer-quiz?id=<%= quiz.getId() %>" 
                   class="btn-progression btn-reessayer">
                    <i class="fas fa-redo"></i>
                    Réessayer cette partie
                </a>
            </div>
        </div>
    <% } %>

    <%-- ============================================================
         CARTE NIVEAU DE RÉUSSITE
         ============================================================ --%>
    <div class="performance-card">
        <div class="performance-icon" style="color: <%= couleurNiveauReussite %>;">
            <i class="<%= iconeNiveauReussite %>"></i>
        </div>
        <h3 style="color: <%= couleurNiveauReussite %>;"><%= niveauReussite %></h3>
        <p><%= messagePerformance %></p>
    </div>

    <%-- ============================================================
         DÉTAIL QUESTION PAR QUESTION (CORRECTION)
         ============================================================ --%>
    <div class="detail-card">
        <div class="detail-header">
            <i class="fas fa-list-ul"></i>
            <span>Détail de vos réponses</span>
            <span class="detail-badge">
                <span class="detail-correct"><%= score %> correctes</span>
                <span class="detail-separator">|</span>
                <span class="detail-incorrect"><%= totalQuestions - score %> incorrectes</span>
            </span>
        </div>
        
        <div class="detail-body">
            <% 
                String[] lettres = {"A", "B", "C", "D"};
                for (int i = 0; i < listeQuestions.size(); i++) { 
                    Question q = listeQuestions.get(i);
                    int reponseJoueur = reponsesJoueur[i];
                    boolean estCorrect = corrections.get(i);
            %>
                <div class="question-resultat <%= estCorrect ? "qr-correct" : "qr-incorrect" %>">
                    <div class="qr-header">
                        <span class="qr-numero">Question <%= i + 1 %></span>
                        <span class="qr-badge <%= estCorrect ? "qr-badge-correct" : "qr-badge-incorrect" %>">
                            <i class="fas <%= estCorrect ? "fa-check-circle" : "fa-times-circle" %>"></i>
                            <%= estCorrect ? "Correct" : "Incorrect" %>
                        </span>
                    </div>
                    <div class="qr-body">
                        <p class="qr-texte"><%= q.getTexteQuestion() %></p>
                        
                        <% if (estCorrect) { %>
                            <div class="qr-reponse qr-joueur-correct">
                                <i class="fas fa-check-circle"></i>
                                <span>Votre réponse : <strong><%= lettres[reponseJoueur - 1] %>. <%= q.getProposition(reponseJoueur) %></strong></span>
                            </div>
                        <% } else { %>
                            <div class="qr-reponse qr-joueur-incorrect">
                                <i class="fas fa-times-circle"></i>
                                <span>Votre réponse : <strong><%= lettres[reponseJoueur - 1] %>. <%= q.getProposition(reponseJoueur) %></strong></span>
                            </div>
                            <div class="qr-reponse qr-bonne-reponse">
                                <i class="fas fa-check-circle"></i>
                                <span>Bonne réponse : <strong><%= lettres[q.getBonneReponse() - 1] %>. <%= q.getProposition(q.getBonneReponse()) %></strong></span>
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
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-action btn-action-primary">
            <i class="fas fa-tachometer-alt"></i>
            <span>Retour au dashboard</span>
        </a>
        <a href="${pageContext.request.contextPath}/joueur/niveau?categorie=<%= categorieEncodee %>&niveau=<%= niveauEncode %>" class="btn-action btn-action-secondary">
            <i class="fas fa-layer-group"></i>
            <span>Voir le niveau <%= quiz.getNiveauFormate() %></span>
        </a>
        <a href="${pageContext.request.contextPath}/joueur/historique" class="btn-action btn-action-outline">
            <i class="fas fa-history"></i>
            <span>Voir l'historique</span>
        </a>
    </div>

</div>

<%-- ============================================================
     SCRIPTS SPÉCIFIQUES
     ============================================================ --%>
<%
    String resultatJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/resultat.js\"></script>";
    request.setAttribute("extraScripts", resultatJS);
%>

<%@ include file="../footer-connecte.jsp" %>