<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="java.text.DecimalFormat" %>

<%
    // Récupération des attributs
    String nomTheme = (String) request.getAttribute("nomTheme");
    String iconeTheme = (String) request.getAttribute("iconeTheme");
    String couleurTheme = (String) request.getAttribute("couleurTheme");
    Integer totalCours = (Integer) request.getAttribute("totalCours");
    Integer totalCoursTermines = (Integer) request.getAttribute("totalCoursTermines");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    Integer totalLeconsTerminees = (Integer) request.getAttribute("totalLeconsTerminees");
    Integer pourcentageProgression = (Integer) request.getAttribute("pourcentageProgression");
    List<Map<String, Object>> niveaux = (List<Map<String, Object>>) request.getAttribute("niveaux");
    String niveauActif = (String) request.getAttribute("niveauActif");
    
    String descriptionTheme = (String) request.getAttribute("descriptionTheme");
    Integer tempsEstimeTotal = (Integer) request.getAttribute("tempsEstimeTotal");
    String niveauRecommandation = (String) request.getAttribute("niveauRecommandation");
    String dernierCoursVisite = (String) request.getAttribute("dernierCoursVisite");
    String dernierCoursUrl = (String) request.getAttribute("dernierCoursUrl");
    List<Map<String, Object>> coursPopulaires = (List<Map<String, Object>>) request.getAttribute("coursPopulaires");

    if (nomTheme == null) nomTheme = "Thème";
    if (iconeTheme == null) iconeTheme = "fas fa-book";
    if (couleurTheme == null) couleurTheme = "#667eea";
    if (totalCours == null) totalCours = 0;
    if (totalCoursTermines == null) totalCoursTermines = 0;
    if (totalLecons == null) totalLecons = 0;
    if (totalLeconsTerminees == null) totalLeconsTerminees = 0;
    if (pourcentageProgression == null) pourcentageProgression = 0;
    if (niveauActif == null) niveauActif = "elementaire";
    if (descriptionTheme == null) descriptionTheme = "Maîtrisez les fondamentaux et concepts avancés de " + nomTheme + " à travers une progression structurée et des exercices pratiques.";
    if (tempsEstimeTotal == null) tempsEstimeTotal = 40;
    if (niveauRecommandation == null) niveauRecommandation = "elementaire";
    
    DecimalFormat df = new DecimalFormat("#.#");
%>

<%
    String coursThemeCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-theme.css\">";
    request.setAttribute("extraHead", coursThemeCSS);
%>

<%@ include file="../header.jsp" %>

<div class="cours-theme-container">

    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i>
            <span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="<%= iconeTheme %>"></i>
            <span><%= nomTheme %></span>
        </span>
    </nav>

    <div class="theme-banner" style="background: linear-gradient(135deg, <%= couleurTheme %>, <%= couleurTheme %>dd);">
        <div class="theme-banner-bg"></div>
        
        <div class="theme-banner-icon">
            <i class="<%= iconeTheme %>"></i>
        </div>
        
        <div class="theme-banner-content">
            <h1><%= nomTheme %></h1>
            <p><%= descriptionTheme %></p>
            
            <div class="theme-banner-stats">
                <div class="banner-stat-item">
                    <i class="fas fa-book"></i>
                    <div>
                        <strong><%= totalCours %></strong>
                        <span>cours</span>
                    </div>
                </div>
                <div class="banner-stat-item">
                    <i class="fas fa-list-ol"></i>
                    <div>
                        <strong><%= totalLecons %></strong>
                        <span>leçons</span>
                    </div>
                </div>
                <div class="banner-stat-item">
                    <i class="fas fa-layer-group"></i>
                    <div>
                        <strong>3</strong>
                        <span>niveaux</span>
                    </div>
                </div>
                <div class="banner-stat-item">
                    <i class="fas fa-clock"></i>
                    <div>
                        <strong><%= tempsEstimeTotal %>h</strong>
                        <span>estimées</span>
                    </div>
                </div>
            </div>
            
            <div class="banner-progress">
                <div class="banner-progress-header">
                    <span>Progression du thème</span>
                    <span><strong><%= pourcentageProgression %>%</strong></span>
                </div>
                <div class="banner-progress-bar">
                    <div class="banner-progress-fill" style="width: <%= pourcentageProgression %>%; background: rgba(255,255,255,0.4);"></div>
                </div>
            </div>
        </div>
        
        <div class="theme-banner-status">
            <% if (pourcentageProgression >= 100 && totalCours > 0) { %>
                <div class="status-badge completed">
                    <i class="fas fa-trophy"></i>
                    <span>Thème terminé</span>
                </div>
            <% } else if (pourcentageProgression > 0) { %>
                <div class="status-badge in-progress">
                    <i class="fas fa-chart-line"></i>
                    <span><%= pourcentageProgression %>%</span>
                </div>
            <% } else { %>
                <div class="status-badge not-started">
                    <i class="fas fa-play-circle"></i>
                    <span>À commencer</span>
                </div>
            <% } %>
        </div>
    </div>

    <% if (dernierCoursVisite != null && !dernierCoursVisite.isEmpty()) { %>
        <div class="continue-card" style="border-left-color: <%= couleurTheme %>;">
            <div class="continue-icon">
                <i class="fas fa-history" style="color: <%= couleurTheme %>;"></i>
            </div>
            <div class="continue-content">
                <span class="continue-label">Continuer votre apprentissage</span>
                <h4><%= dernierCoursVisite %></h4>
                <p>Reprenez où vous vous étiez arrêté</p>
            </div>
            <a href="<%= dernierCoursUrl != null ? dernierCoursUrl : "#" %>" class="continue-btn" style="background: <%= couleurTheme %>;">
                <i class="fas fa-play"></i> Continuer
            </a>
        </div>
    <% } %>

    <div class="section-header">
        <h2>Niveaux disponibles</h2>
        <p>Choisissez votre niveau et progressez à votre rythme</p>
    </div>

    <div class="niveaux-grid">
        <% if (niveaux != null && !niveaux.isEmpty()) {
            for (Map<String, Object> niveau : niveaux) {
                String nomNiveau = (String) niveau.get("nom");
                
                String nomFormate = "";
                if (nomNiveau != null) {
                    if ("elementaire".equalsIgnoreCase(nomNiveau)) {
                        nomFormate = "Élémentaire";
                    } else if ("moyen".equalsIgnoreCase(nomNiveau)) {
                        nomFormate = "Moyen";
                    } else if ("avance".equalsIgnoreCase(nomNiveau)) {
                        nomFormate = "Avancé";
                    } else {
                        nomFormate = nomNiveau;
                    }
                }
                
                String iconeNiveau = (String) niveau.get("icone");
                String couleurNiveau = (String) niveau.get("couleur");
                int nbCoursNiveau = niveau.get("nbCours") != null ? (Integer) niveau.get("nbCours") : 0;
                int nbLeconsNiveau = niveau.get("nbLecons") != null ? (Integer) niveau.get("nbLecons") : 0;
                int nbLeconsTermineesNiveau = niveau.get("nbLeconsTerminees") != null ? (Integer) niveau.get("nbLeconsTerminees") : 0;
                int pourcentageNiveau = niveau.get("pourcentage") != null ? (Integer) niveau.get("pourcentage") : 0;
                boolean debloque = niveau.get("debloque") != null ? (Boolean) niveau.get("debloque") : false;
                boolean termine = niveau.get("termine") != null ? (Boolean) niveau.get("termine") : false;
                
                // Récupérer la liste des titres de cours
                java.util.List<String> titresCours = (java.util.List<String>) niveau.get("titresCours");
                
                String urlNiveau = request.getContextPath() + "/joueur/cours/niveau?theme=" 
                    + java.net.URLEncoder.encode(nomTheme, "UTF-8") 
                    + "&niveau=" + nomNiveau;
                
                boolean isRecommended = niveauRecommandation != null && niveauRecommandation.equalsIgnoreCase(nomNiveau);
        %>
            <div class="niveau-card-wrapper">
                <% if (debloque) { %>
                    <a href="<%= urlNiveau %>" class="niveau-card-link">
                <% } %>
                
                <div class="niveau-card <%= termine ? "termine" : (debloque ? "debloque" : "bloque") %>">
                    
                    <% if (isRecommended && !termine && debloque) { %>
                        <div class="niveau-recommended-badge">
                            <i class="fas fa-star"></i> Recommandé
                        </div>
                    <% } %>
                    
                    <div class="niveau-card-header">
                        <div class="niveau-icon" style="background: <%= couleurNiveau %>20; color: <%= couleurNiveau %>;">
                            <i class="<%= iconeNiveau %>"></i>
                        </div>
                        <div class="niveau-info">
                            <h3><%= nomFormate %></h3>
                            <div class="niveau-stats">
                                <span><i class="fas fa-book"></i> <%= nbCoursNiveau %> cours</span>
                                <span class="sep">•</span>
                                <span><i class="fas fa-list-ol"></i> <%= nbLeconsNiveau %> leçons</span>
                            </div>
                            
                            <%-- ===== LISTE DES TITRES DE COURS ===== --%>
                            <% if (titresCours != null && !titresCours.isEmpty()) { %>
                                <div class="niveau-cours-list">
                                    <% for (String titreCours : titresCours) { %>
                                        <span class="niveau-cours-item">
                                            <i class="fas fa-chevron-right"></i> <%= titreCours %>
                                        </span>
                                    <% } %>
                                </div>
                            <% } %>
                        </div>
                        <div class="niveau-status-icon">
                            <% if (termine) { %>
                                <i class="fas fa-check-circle" style="color: #27ae60;"></i>
                            <% } else if (debloque) { %>
                                <i class="fas fa-arrow-right" style="color: <%= couleurNiveau %>;"></i>
                            <% } else { %>
                                <i class="fas fa-lock" style="color: #ccc;"></i>
                            <% } %>
                        </div>
                    </div>
                    
                    <div class="niveau-description">
                        <% if ("elementaire".equalsIgnoreCase(nomNiveau)) { %>
                            Les bases essentielles pour débuter. Aucun prérequis nécessaire.
                        <% } else if ("moyen".equalsIgnoreCase(nomNiveau)) { %>
                            Approfondissez vos connaissances avec des concepts intermédiaires.
                        <% } else { %>
                            Maîtrisez les sujets complexes et avancés du domaine.
                        <% } %>
                    </div>
                    
                    <div class="niveau-progress">
                        <div class="niveau-progress-header">
                            <span><i class="fas fa-check-circle"></i> <%= nbLeconsTermineesNiveau %>/<%= nbLeconsNiveau %> leçons</span>
                            <span><strong><%= pourcentageNiveau %>%</strong></span>
                        </div>
                        <div class="niveau-progress-bar">
                            <div class="niveau-progress-fill" style="width: <%= pourcentageNiveau %>%; background: <%= couleurNiveau %>;"></div>
                        </div>
                    </div>
                    
                    <div class="niveau-card-footer">
                        <% if (termine) { %>
                            <span class="niveau-footer-text termine">
                                <i class="fas fa-trophy"></i> Niveau maîtrisé
                            </span>
                            <span class="niveau-footer-btn voir" style="background: <%= couleurNiveau %>20; color: <%= couleurNiveau %>;">
                                <i class="fas fa-eye"></i> Revoir
                            </span>
                        <% } else if (debloque) { %>
                            <span class="niveau-footer-text en-cours">
                                <% if (pourcentageNiveau > 0) { %>
                                    <i class="fas fa-chart-line"></i> <%= pourcentageNiveau %>% complété
                                <% } else { %>
                                    <i class="fas fa-play-circle"></i> Prêt à commencer
                                <% } %>
                            </span>
                            <span class="niveau-footer-btn commencer" style="background: <%= couleurNiveau %>;">
                                <i class="fas fa-arrow-right"></i> Accéder
                            </span>
                        <% } else { %>
                            <span class="niveau-footer-text bloque">
                                <i class="fas fa-lock"></i> Complétez le niveau précédent
                            </span>
                            <span class="niveau-footer-btn bloque">
                                <i class="fas fa-lock"></i> Bloqué
                            </span>
                        <% } %>
                    </div>
                    
                </div>
                
                <% if (debloque) { %>
                    </a>
                <% } %>
            </div>
        <%  }
        } else { %>
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>Aucun niveau disponible pour ce thème.</p>
            </div>
        <% } %>
    </div>

    <% if (coursPopulaires != null && !coursPopulaires.isEmpty()) { %>
        <div class="section-header">
            <h2><i class="fas fa-fire"></i> Cours populaires</h2>
            <p>Les plus suivis par notre communauté</p>
        </div>
        
        <div class="cours-populaires-grid">
            <% for (Map<String, Object> coursPop : coursPopulaires) { 
                String titre = (String) coursPop.get("titre");
                String icone = (String) coursPop.get("icone");
                String couleur = (String) coursPop.get("couleur");
                String url = (String) coursPop.get("url");
                int nbEtudiants = coursPop.get("nbEtudiants") != null ? (Integer) coursPop.get("nbEtudiants") : 0;
            %>
                <a href="<%= url %>" class="cours-populaire-card">
                    <div class="populaire-icon" style="background: <%= couleur %>20; color: <%= couleur %>;">
                        <i class="<%= icone %>"></i>
                    </div>
                    <div class="populaire-info">
                        <h4><%= titre %></h4>
                        <span><i class="fas fa-users"></i> <%= nbEtudiants %> étudiants</span>
                    </div>
                    <i class="fas fa-arrow-right populaire-arrow" style="color: <%= couleur %>;"></i>
                </a>
            <% } %>
        </div>
    <% } %>

    <div class="recommandation-section">
        <div class="recommandation-card" style="background: linear-gradient(135deg, <%= couleurTheme %>10, <%= couleurTheme %>05); border-color: <%= couleurTheme %>20;">
            <div class="recommandation-icon" style="background: <%= couleurTheme %>;">
                <i class="fas fa-brain"></i>
            </div>
            <div class="recommandation-content">
                <h4>Conseil personnalisé</h4>
                <p>
                    <% if (pourcentageProgression == 0) { %>
                        Commencez par le niveau <strong>Élémentaire</strong> pour construire des bases solides.
                    <% } else if (pourcentageProgression < 33) { %>
                        Vous progressez bien ! <strong><%= df.format(pourcentageProgression) %>%</strong> du thème est déjà complété.
                    <% } else if (pourcentageProgression < 66) { %>
                        Excellente progression ! Vous êtes à <strong><%= df.format(pourcentageProgression) %>%</strong>.
                    <% } else if (pourcentageProgression < 100) { %>
                        Vous êtes presque au bout ! <strong><%= df.format(pourcentageProgression) %>%</strong> complété.
                    <% } else { %>
                        Félicitations ! Vous maîtrisez désormais <strong><%= nomTheme %></strong>.
                    <% } %>
                </p>
            </div>
        </div>
    </div>

    <div class="back-section">
        <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-back">
            <i class="fas fa-arrow-left"></i>
            <span>Retour aux thèmes</span>
        </a>
    </div>

</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    var cards = document.querySelectorAll('.niveau-card');
    cards.forEach(function(card, index) {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        setTimeout(function() {
            card.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
});
</script>

<%@ include file="../footer-connecte.jsp" %>