<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Quiz" %>

<%-- ============================================================
     Composant : partie-card.jsp
     Rôle    : Carte d'une partie dans la grille du niveau
              - Icône de statut (réussie / à jouer / verrouillée)
              - Numéro de la partie
              - Nombre de questions
              - Badge "À jouer" si c'est la prochaine partie
              - Bouton "Jouer" si accessible
              - Badge "Réussi" si déjà complétée
              - Entièrement cliquable si débloquée
     ============================================================ --%>

<%
    // Récupération des attributs passés par la page parente
    Quiz quiz = (Quiz) request.getAttribute("quiz");
    Boolean reussie = (Boolean) request.getAttribute("reussie");
    Boolean estProchaine = (Boolean) request.getAttribute("estProchaine");
    Integer ordrePartie = (Integer) request.getAttribute("ordrePartie");

    // Valeurs par défaut sécurisées
    if (reussie == null) reussie = false;
    if (estProchaine == null) estProchaine = false;
    if (ordrePartie == null) ordrePartie = 1;

    // Détermination du statut et des propriétés d'affichage
    String classeStatut;
    String iconeStatut;
    String couleurIcone;
    String texteAction;
    boolean estAccessible = false;

    if (reussie) {
        classeStatut = "partie-reussie";
        iconeStatut = "fas fa-check-circle";
        couleurIcone = "#27ae60";
        texteAction = "Réussi";
        estAccessible = true;
    } else if (estProchaine) {
        classeStatut = "partie-prochaine";
        iconeStatut = "fas fa-play-circle";
        couleurIcone = "#667eea";
        texteAction = "Jouer";
        estAccessible = true;
    } else {
        classeStatut = "partie-verrouillee";
        iconeStatut = "fas fa-lock";
        couleurIcone = "#bbb";
        texteAction = "Bloqué";
        estAccessible = false;
    }

    // URL pour démarrer le quiz
    String urlDemarrer = request.getContextPath() + "/joueur/demarrer-quiz?id=" + quiz.getId();
    
    // Nombre de questions
    int nbQuestions = quiz.getNombreQuestions();
    String questionsLabel = nbQuestions + " question" + (nbQuestions > 1 ? "s" : "");
%>

<%-- Lien englobant si la partie est accessible --%>
<% if (estAccessible && !reussie) { %>
    <a href="<%= urlDemarrer %>" class="partie-card-link" 
       aria-label="Partie <%= ordrePartie %> - <%= reussie ? "Réussie" : "À jouer" %>">
<% } else if (reussie) { %>
    <a href="<%= urlDemarrer %>" class="partie-card-link" 
       aria-label="Partie <%= ordrePartie %> - Réussie - Rejouer">
<% } %>

<div class="partie-card <%= classeStatut %>" 
     <%= !estAccessible ? "aria-disabled=\"true\"" : "" %>
     data-partie="<%= ordrePartie %>"
     data-statut="<%= reussie ? "reussie" : (estProchaine ? "prochaine" : "verrouillee") %>">
    
    <%-- Badge "À jouer" pour la prochaine partie --%>
    <% if (estProchaine) { %>
        <div class="badge-a-jouer">
            <i class="fas fa-bolt"></i>
            <span>À jouer</span>
        </div>
    <% } %>

    <%-- Badge "Réussi" pour les parties complétées --%>
    <% if (reussie) { %>
        <div class="badge-reussi-partie">
            <i class="fas fa-check"></i>
            <span>Réussi</span>
        </div>
    <% } %>

    <%-- Icône de statut --%>
    <div class="partie-statut" style="color: <%= couleurIcone %>;" aria-hidden="true">
        <i class="<%= iconeStatut %>"></i>
    </div>

    <%-- Numéro de la partie --%>
    <div class="partie-numero">
        <span class="partie-numero-texte">Partie <%= ordrePartie %></span>
    </div>

    <%-- Nombre de questions --%>
    <div class="partie-questions">
        <i class="fas fa-question-circle"></i>
        <span><%= questionsLabel %></span>
    </div>

    <%-- Action selon le statut --%>
    <div class="partie-action">
        <% if (reussie) { %>
            <span class="badge-reussi">
                <i class="fas fa-check"></i>
                <span><%= texteAction %></span>
            </span>
            <span class="badge-rejouer-hint">
                <i class="fas fa-redo"></i> Rejouer
            </span>
        <% } else if (estProchaine) { %>
            <span class="btn-jouer-partie">
                <i class="fas fa-play"></i>
                <span><%= texteAction %></span>
            </span>
        <% } else { %>
            <span class="badge-verrouille">
                <i class="fas fa-lock"></i>
                <span><%= texteAction %></span>
            </span>
        <% } %>
    </div>

</div>

<% if ((estAccessible && !reussie) || reussie) { %>
    </a>
<% } %>