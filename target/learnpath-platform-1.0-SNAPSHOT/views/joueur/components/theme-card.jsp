<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%-- ============================================================
     Composant : theme-card.jsp
     Rôle    : Carte d'un thème sur le dashboard joueur
              - Icône et nom du thème
              - Progression globale (barre + pourcentage)
              - Compteur niveaux terminés et parties réussies
              - Mini-badges des 3 niveaux (terminé/en cours/bloqué)
              - Niveau actif en cours
              - Bouton "Voir le parcours"
              - Entièrement cliquable pour accéder au détail
     ============================================================ --%>

<%
    // Récupération des attributs passés par la page parente
    String nomTheme = (String) request.getAttribute("nomTheme");
    String iconeTheme = (String) request.getAttribute("iconeTheme");
    String couleurTheme = (String) request.getAttribute("couleurTheme");
    Integer totalParties = (Integer) request.getAttribute("totalParties");
    Integer partiesReussies = (Integer) request.getAttribute("partiesReussies");
    Integer pourcentageProgression = (Integer) request.getAttribute("pourcentageProgression");
    List<Map<String, Object>> niveaux = (List<Map<String, Object>>) request.getAttribute("niveaux");
    String niveauActif = (String) request.getAttribute("niveauActif");

    // Valeurs par défaut sécurisées
    if (totalParties == null) totalParties = 0;
    if (partiesReussies == null) partiesReussies = 0;
    if (pourcentageProgression == null) pourcentageProgression = 0;
    if (niveauActif == null) niveauActif = "Élémentaire";
    if (nomTheme == null) nomTheme = "Thème";
    if (iconeTheme == null) iconeTheme = "fas fa-book";
    if (couleurTheme == null) couleurTheme = "#667eea";

    // Calculer le nombre de niveaux terminés
    int niveauxTerminesCount = 0;
    if (niveaux != null) {
        for (Map<String, Object> niv : niveaux) {
            Boolean termine = (Boolean) niv.get("termine");
            if (termine != null && termine) {
                niveauxTerminesCount++;
            }
        }
    }

    // URL encodée pour le lien
    String urlTheme = request.getContextPath() + "/joueur/theme?categorie=" 
                    + java.net.URLEncoder.encode(nomTheme, "UTF-8");
    
    // Label du niveau actif
    String niveauActifLabel = (niveauxTerminesCount == 3) ? "Terminé" : niveauActif;
    boolean estTermine = (niveauxTerminesCount == 3);
%>

<a href="<%= urlTheme %>" class="theme-card-link" 
   aria-label="Thème <%= nomTheme %> - <%= niveauxTerminesCount %>/3 niveaux terminés - <%= pourcentageProgression %>% de maîtrise">
    
    <div class="theme-card">
        
        <%-- En-tête : icône + infos --%>
        <div class="theme-card-header">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, <%= couleurTheme %>, <%= couleurTheme %>dd);">
                <i class="<%= iconeTheme %>"></i>
            </div>
            <div class="theme-card-info">
                <h3 class="theme-card-nom"><%= nomTheme %></h3>
                <div class="theme-card-meta">
                    <span class="meta-item">
                        <i class="fas fa-layer-group"></i>
                        <strong><%= niveauxTerminesCount %></strong>/3 niveaux
                    </span>
                    <span class="meta-separator">•</span>
                    <span class="meta-item">
                        <i class="fas fa-check-circle"></i>
                        <strong><%= partiesReussies %></strong>/<%= totalParties %> parties
                    </span>
                </div>
            </div>
            <%-- Badge "Terminé" si tous les niveaux sont complétés --%>
            <% if (estTermine) { %>
                <div class="theme-badge-termine">
                    <i class="fas fa-trophy"></i>
                </div>
            <% } %>
        </div>

        <%-- Corps : barre de progression + mini-badges --%>
        <div class="theme-card-body">
            <%-- Barre de progression --%>
            <div class="theme-progress">
                <div class="progress-label">
                    <span>Maîtrise du thème</span>
                    <span class="progress-value"><%= pourcentageProgression %>%</span>
                </div>
                <div class="progress-track">
                    <div class="progress-fill" 
                         style="width: <%= pourcentageProgression %>%; background: linear-gradient(90deg, <%= couleurTheme %>, <%= couleurTheme %>cc);">
                    </div>
                </div>
            </div>

            <%-- Mini-badges des 3 niveaux --%>
            <div class="theme-niveaux-badges">
                <% if (niveaux != null) {
                    for (Map<String, Object> niv : niveaux) {
                        String nomFormate = (String) niv.get("nomFormate");
                        Boolean termine = (Boolean) niv.get("termine");
                        Boolean debloque = (Boolean) niv.get("debloque");

                        if (termine != null && termine) { %>
                            <span class="mini-badge badge-termine">
                                <i class="fas fa-check-circle"></i>
                                <span><%= nomFormate %></span>
                            </span>
                        <% } else if (debloque != null && debloque) { %>
                            <span class="mini-badge badge-encours">
                                <i class="fas fa-play-circle"></i>
                                <span><%= nomFormate %></span>
                            </span>
                        <% } else { %>
                            <span class="mini-badge badge-bloque">
                                <i class="fas fa-lock"></i>
                                <span><%= nomFormate %></span>
                            </span>
                        <% }
                    }
                } %>
            </div>
        </div>

        <%-- Pied : niveau en cours + bouton --%>
        <div class="theme-card-footer">
            <div class="footer-niveau-actif">
                <i class="fas fa-circle" style="color: <%= (niveauxTerminesCount == 3) ? "#27ae60" : "#667eea" %>;"></i>
                <span>
                    <% if (niveauxTerminesCount == 3) { %>
                        🎉 Thème terminé
                    <% } else { %>
                        Niveau en cours : <strong><%= niveauActifLabel %></strong>
                    <% } %>
                </span>
            </div>
            <span class="btn-voir-parcours">
                <i class="fas fa-eye"></i>
                <span>Voir le parcours</span>
            </span>
        </div>

    </div>
    
</a>