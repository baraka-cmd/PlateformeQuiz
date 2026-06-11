<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.learnpath.models.Quiz" %>

<%-- ============================================================
     Composant : niveau-card.jsp
     Rôle    : Carte d'un niveau (élémentaire/moyen/avancé)
              - Icône et nom du niveau
              - Badge de statut (terminé/en cours/bloqué)
              - Barre de progression avec pourcentage
              - Compteur de parties réussies
              - Bouton d'action contextuel
              - Message pour niveau bloqué
              - Entièrement cliquable si débloqué
     ============================================================ --%>

<%
    // Récupération des attributs passés par la page parente
    String nomCategorie = (String) request.getAttribute("nomCategorie");
    String nomNiveau = (String) request.getAttribute("nomNiveau");
    String nomFormate = (String) request.getAttribute("nomFormate");
    String iconeNiveau = (String) request.getAttribute("iconeNiveau");
    String couleurNiveau = (String) request.getAttribute("couleurNiveau");
    Integer totalParties = (Integer) request.getAttribute("totalParties");
    Integer partiesReussies = (Integer) request.getAttribute("partiesReussies");
    Integer pourcentageProgression = (Integer) request.getAttribute("pourcentageProgression");
    Boolean debloque = (Boolean) request.getAttribute("debloque");
    Boolean termine = (Boolean) request.getAttribute("termine");
    Quiz prochainQuiz = (Quiz) request.getAttribute("prochainQuiz");

    // Valeurs par défaut sécurisées
    if (totalParties == null) totalParties = 0;
    if (partiesReussies == null) partiesReussies = 0;
    if (pourcentageProgression == null) pourcentageProgression = 0;
    if (debloque == null) debloque = false;
    if (termine == null) termine = false;
    if (nomFormate == null) nomFormate = "Niveau";
    if (iconeNiveau == null) iconeNiveau = "fas fa-circle";
    if (couleurNiveau == null) couleurNiveau = "#888888";

    // Détermination du statut et des propriétés d'affichage
    String classeStatut;
    String iconeStatut;
    String couleurStatut;
    String texteBouton;
    String classeBouton;
    String iconeBouton;
    boolean estBloque = false;
    String messageBloque = "";
    String urlCible = "#";

    if (termine) {
        classeStatut = "niveau-termine";
        iconeStatut = "fas fa-check-circle";
        couleurStatut = "#27ae60";
        texteBouton = "Voir le détail";
        classeBouton = "btn-reussi";
        iconeBouton = "fas fa-eye";
        urlCible = request.getContextPath() + "/joueur/niveau?categorie=" 
                 + java.net.URLEncoder.encode(nomCategorie, "UTF-8") 
                 + "&niveau=" + nomNiveau;
    } else if (debloque) {
        classeStatut = "niveau-debloque";
        iconeStatut = "fas fa-play-circle";
        couleurStatut = "#667eea";
        texteBouton = "Accéder au niveau";
        classeBouton = "btn-continuer";
        iconeBouton = "fas fa-arrow-right";
        urlCible = request.getContextPath() + "/joueur/niveau?categorie=" 
                 + java.net.URLEncoder.encode(nomCategorie, "UTF-8") 
                 + "&niveau=" + nomNiveau;
    } else {
        classeStatut = "niveau-bloque";
        iconeStatut = "fas fa-lock";
        couleurStatut = "#bbb";
        texteBouton = "Niveau bloqué";
        classeBouton = "btn-bloque";
        iconeBouton = "fas fa-lock";
        estBloque = true;
        
        // Message personnalisé selon le niveau
        if ("moyen".equals(nomNiveau)) {
            messageBloque = "Terminez le niveau Élémentaire pour débloquer";
        } else if ("avance".equals(nomNiveau)) {
            messageBloque = "Terminez le niveau Moyen pour débloquer";
        } else {
            messageBloque = "Terminez le niveau précédent pour débloquer";
        }
    }
%>

<%-- Lien englobant si le niveau est accessible --%>
<% if (!estBloque) { %>
    <a href="<%= urlCible %>" class="niveau-card-link" aria-label="<%= nomFormate %> - <%= termine ? "Terminé" : "En cours" %>">
<% } %>

<div class="niveau-card <%= classeStatut %>" <%= estBloque ? "aria-disabled=\"true\"" : "" %>>
    
    <%-- Badge de statut en haut à droite --%>
    <div class="niveau-statut-badge" style="color: <%= couleurStatut %>;">
        <i class="<%= iconeStatut %>"></i>
        <% if (termine) { %>
            <span>Terminé</span>
        <% } else if (debloque) { %>
            <span>En cours</span>
        <% } else { %>
            <span>Bloqué</span>
        <% } %>
    </div>

    <%-- Icône du niveau --%>
    <div class="niveau-icon-wrapper" style="background: linear-gradient(135deg, <%= couleurNiveau %>22, <%= couleurNiveau %>11);">
        <i class="<%= iconeNiveau %>" style="color: <%= couleurNiveau %>;"></i>
    </div>

    <%-- Nom du niveau --%>
    <h3 class="niveau-nom" style="color: <%= couleurNiveau %>;"><%= nomFormate %></h3>

    <%-- Barre de progression --%>
    <div class="niveau-progress">
        <div class="progress-header">
            <span class="progress-label">Progression</span>
            <span class="progress-value"><%= pourcentageProgression %>%</span>
        </div>
        <div class="progress-track">
            <div class="progress-fill" style="width: <%= pourcentageProgression %>%; background: <%= couleurNiveau %>;"></div>
        </div>
        <div class="progress-detail">
            <i class="fas fa-check-circle"></i>
            <span><strong><%= partiesReussies %></strong> / <%= totalParties %> parties réussies</span>
        </div>
    </div>

    <%-- Bouton d'action --%>
    <div class="niveau-action">
        <% if (!estBloque) { %>
            <span class="btn-niveau <%= classeBouton %>">
                <i class="<%= iconeBouton %>"></i>
                <span><%= texteBouton %></span>
            </span>
        <% } else { %>
            <span class="btn-niveau btn-bloque" aria-disabled="true">
                <i class="fas fa-lock"></i>
                <span><%= texteBouton %></span>
            </span>
            <p class="niveau-message-bloque">
                <i class="fas fa-info-circle"></i>
                <%= messageBloque %>
            </p>
        <% } %>
    </div>

</div>

<% if (!estBloque) { %>
    </a>
<% } %>