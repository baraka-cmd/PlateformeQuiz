<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.ProgressionCours" %>
<%@ page import="java.text.DecimalFormat" %>

<%-- ============================================================
     Fichier : cours-niveau.jsp
     Rôle    : Page niveau d'un thème de cours
              Affiche les cours/langages disponibles
              pour un thème et un niveau donnés
              Design épuré, senior, essentiel
     Version améliorée avec progression et métriques avancées
     ============================================================ --%>

<%
    // Récupération des attributs
    String nomTheme = (String) request.getAttribute("nomTheme");
    String nomNiveauFormate = (String) request.getAttribute("nomNiveauFormate");
    String iconeNiveau = (String) request.getAttribute("iconeNiveau");
    String couleurNiveau = (String) request.getAttribute("couleurNiveau");
    List<Map<String, Object>> coursAvecProgression = (List<Map<String, Object>>) request.getAttribute("coursAvecProgression");
    
    // Données supplémentaires pour l'expérience senior
    Integer totalCours = (Integer) request.getAttribute("totalCours");
    Integer totalCoursTermines = (Integer) request.getAttribute("totalCoursTermines");
    Double progressionGlobale = (Double) request.getAttribute("progressionGlobale");
    String tempsEstime = (String) request.getAttribute("tempsEstime");
    String niveauRecommandation = (String) request.getAttribute("niveauRecommandation");

    // Initialisation des valeurs par défaut
    if (nomTheme == null) nomTheme = "Thème";
    if (nomNiveauFormate == null) nomNiveauFormate = "Niveau";
    if (iconeNiveau == null) iconeNiveau = "fas fa-layer-group";
    if (couleurNiveau == null) couleurNiveau = "#667eea";
    if (totalCours == null) totalCours = 0;
    if (totalCoursTermines == null) totalCoursTermines = 0;
    if (progressionGlobale == null) progressionGlobale = 0.0;
    if (tempsEstime == null) tempsEstime = "Non estimé";
    if (niveauRecommandation == null) niveauRecommandation = "Débutant";

    String themeEncode = java.net.URLEncoder.encode(nomTheme, "UTF-8");
    DecimalFormat df = new DecimalFormat("#.#");
%>

<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String coursNiveauCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-niveau.css\">";
    request.setAttribute("extraHead", coursNiveauCSS);
%>

<%@ include file="../header.jsp" %>

<div class="cours-niveau-container">

    <%-- ===== BREADCRUMB ===== --%>
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
            <span><%= nomTheme %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="<%= iconeNiveau %>"></i><span><%= nomNiveauFormate %></span>
        </span>
    </nav>

    <%-- ===== EN-TÊTE AVEC MÉTRIQUES ===== --%>
    <div class="niveau-header">
        <div class="niveau-header-main">
            <span class="niveau-badge" style="background: <%= couleurNiveau %>20; color: <%= couleurNiveau %>;">
                <i class="<%= iconeNiveau %>"></i> <%= nomNiveauFormate %>
            </span>
            <h1><%= nomTheme %></h1>
            <p class="niveau-description">
                Maîtrisez les fondamentaux du <%= nomTheme %> niveau <%= nomNiveauFormate.toLowerCase() %>.
                Des cours structurés pour une progression efficace.
            </p>
        </div>
        
        <%-- Carte de progression globale --%>
        <div class="niveau-stats-card">
            <div class="stat-item">
                <span class="stat-value"><%= totalCours %></span>
                <span class="stat-label">Cours disponibles</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
                <span class="stat-value"><%= totalCoursTermines %></span>
                <span class="stat-label">Cours terminés</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
                <span class="stat-value"><%= df.format(progressionGlobale) %>%</span>
                <span class="stat-label">Progression</span>
                <div class="stat-progress">
                    <div class="stat-progress-fill" style="width: <%= progressionGlobale %>%; background: <%= couleurNiveau %>;"></div>
                </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
                <span class="stat-value"><%= tempsEstime %></span>
                <span class="stat-label">Temps estimé</span>
            </div>
        </div>
    </div>

    <%-- ===== FILTRES ET TRI ===== --%>
    <div class="cours-filters">
        <div class="filter-group">
            <button class="filter-btn active" data-filter="all">Tous les cours</button>
            <button class="filter-btn" data-filter="in-progress">En cours</button>
            <button class="filter-btn" data-filter="completed">Terminés</button>
            <button class="filter-btn" data-filter="not-started">Non commencés</button>
        </div>
        <div class="sort-group">
            <label>
                <i class="fas fa-sort"></i>
                <select id="sortSelect" class="sort-select">
                    <option value="default">Par défaut</option>
                    <option value="title-asc">Titre A → Z</option>
                    <option value="title-desc">Titre Z → A</option>
                    <option value="progress-asc">Progression croissante</option>
                    <option value="progress-desc">Progression décroissante</option>
                </select>
            </label>
        </div>
    </div>

    <%-- ===== GRILLE DES COURS ===== --%>
    <div class="cours-grid" id="coursGrid">
        <%
    if (coursAvecProgression != null && !coursAvecProgression.isEmpty()) {
        for (Map<String, Object> coursData : coursAvecProgression) {
            Cours cours = (Cours) coursData.get("cours");
            if (cours == null) continue;
            
            String iconeCours = cours.getIconeTheme();
            String couleurCours = cours.getCouleurTheme();
            
            // Valeurs sécurisées avec null check
            int nbChapitres = 0;
            int nbLecons = 0;
            int leconsTerminees = 0;
            int pourcentageProgression = 0;
            
            if (coursData.get("nbChapitres") != null) {
                nbChapitres = (Integer) coursData.get("nbChapitres");
            }
            if (coursData.get("nbLecons") != null) {
                nbLecons = (Integer) coursData.get("nbLecons");
            }
            if (coursData.get("leconsTerminees") != null) {
                leconsTerminees = (Integer) coursData.get("leconsTerminees");
            }
            if (coursData.get("pourcentageProgression") != null) {
                pourcentageProgression = (Integer) coursData.get("pourcentageProgression");
            }
            
            boolean estTermine = pourcentageProgression == 100;
            boolean estCommence = leconsTerminees > 0 && !estTermine;
            
            String statutClass = "";
            String statutLabel = "";
            if (estTermine) {
                statutClass = "completed";
                statutLabel = "Terminé";
            } else if (estCommence) {
                statutClass = "in-progress";
                statutLabel = "En cours";
            } else {
                statutClass = "not-started";
                statutLabel = "Non commencé";
            }
%>
            <div class="cours-card <%= statutClass %>" data-title="<%= cours.getTitre().toLowerCase() %>" 
                 data-progress="<%= pourcentageProgression %>" data-status="<%= statutClass %>">
                
                <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="cours-card-link">
                    
                    <div class="cours-card-icon" style="background: <%= couleurCours %>;">
                        <i class="<%= iconeCours %>"></i>
                    </div>
                    
                    <div class="cours-card-info">
                        <div class="cours-card-header">
                            <h3><%= cours.getTitre() %></h3>
                            <span class="cours-status-badge status-<%= statutClass %>">
                                <i class="fas <%= estTermine ? "fa-check-circle" : (estCommence ? "fa-play-circle" : "fa-clock") %>"></i>
                                <%= statutLabel %>
                            </span>
                        </div>
                        
                        <p class="cours-card-description">
                            <%= cours.getDescription() != null && !cours.getDescription().isEmpty() ? 
                                (cours.getDescription().length() > 100 ? cours.getDescription().substring(0, 100) + "..." : cours.getDescription()) : 
                                "Découvrez ce cours complet sur " + cours.getTitre() + "." %>
                        </p>
                        
                        <div class="cours-card-meta">
                            <span><i class="fas fa-book"></i> <%= nbChapitres %> chapitre<%= nbChapitres > 1 ? "s" : "" %></span>
                            <span><i class="fas fa-file-alt"></i> <%= nbLecons %> leçon<%= nbLecons > 1 ? "s" : "" %></span>
                            <span><i class="fas fa-clock"></i> ~<%= Math.max(1, (int) Math.ceil(nbLecons * 0.5)) %>h</span>
                        </div>
                        
                        <% if (pourcentageProgression > 0) { %>
                            <div class="cours-progress">
                                <div class="cours-progress-bar">
                                    <div class="cours-progress-fill" style="width: <%= pourcentageProgression %>%; background: <%= couleurCours %>;"></div>
                                </div>
                                <span class="cours-progress-text"><%= pourcentageProgression %>% complété</span>
                            </div>
                        <% } %>
                    </div>
                    
                    <div class="cours-card-action">
                        <i class="fas fa-arrow-right"></i>
                    </div>
                    
                </a>
            </div>
        <%  }
        } else { %>
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>Aucun cours disponible pour ce niveau pour le moment.</p>
                <p class="empty-state-sub">De nouveaux cours arrivent régulièrement. Revenez bientôt !</p>
                <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="btn-retour">
                    <i class="fas fa-arrow-left"></i> Retour au thème
                </a>
            </div>
        <% } %>
    </div>

    <%-- ===== SECTION RECOMMANDATION ===== --%>
    <div class="recommandation-section">
        <div class="recommandation-card">
            <div class="recommandation-icon">
                <i class="fas fa-lightbulb"></i>
            </div>
            <div class="recommandation-content">
                <h4>Recommandation pour vous</h4>
                <p>
                    Niveau <strong><%= niveauRecommandation %></strong> : 
                    <% if (progressionGlobale == 0) { %>
                        Commencez par le premier cours de cette liste pour construire des bases solides.
                    <% } else if (progressionGlobale < 50) { %>
                        Excellente initiative ! Continuez sur votre lancée, vous progressez bien.
                    <% } else if (progressionGlobale < 100) { %>
                        Vous êtes à <%= df.format(progressionGlobale) %>% de compléter ce niveau. Encore un effort !
                    <% } else { %>
                        Félicitations ! Vous avez complété tous les cours de ce niveau. Passez au niveau supérieur !
                    <% } %>
                </p>
            </div>
        </div>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="back-section">
        <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="btn-back">
            <i class="fas fa-arrow-left"></i> Retour au thème <%= nomTheme %>
        </a>
    </div>

</div>

<script>
// Filtrage et tri des cours
document.addEventListener('DOMContentLoaded', function() {
    const filterBtns = document.querySelectorAll('.filter-btn');
    const sortSelect = document.getElementById('sortSelect');
    const coursGrid = document.getElementById('coursGrid');
    const coursCards = Array.from(document.querySelectorAll('.cours-card'));
    
    let currentFilter = 'all';
    let currentSort = 'default';
    
    function filterAndSort() {
        let filteredCards = coursCards.filter(card => {
            if (currentFilter === 'all') return true;
            return card.dataset.status === currentFilter;
        });
        
        if (currentSort !== 'default') {
            filteredCards.sort((a, b) => {
                switch(currentSort) {
                    case 'title-asc':
                        return a.dataset.title.localeCompare(b.dataset.title);
                    case 'title-desc':
                        return b.dataset.title.localeCompare(a.dataset.title);
                    case 'progress-asc':
                        return (parseInt(a.dataset.progress) || 0) - (parseInt(b.dataset.progress) || 0);
                    case 'progress-desc':
                        return (parseInt(b.dataset.progress) || 0) - (parseInt(a.dataset.progress) || 0);
                    default:
                        return 0;
                }
            });
        }
        
        coursCards.forEach(card => card.style.display = 'none');
        filteredCards.forEach(card => card.style.display = 'block');
        
        if (filteredCards.length === 0) {
            let emptyMsg = document.getElementById('filterEmptyMessage');
            if (!emptyMsg) {
                emptyMsg = document.createElement('div');
                emptyMsg.id = 'filterEmptyMessage';
                emptyMsg.className = 'empty-state';
                emptyMsg.innerHTML = '<i class="fas fa-filter"></i><p>Aucun cours ne correspond à ce filtre.</p>';
                coursGrid.appendChild(emptyMsg);
            } else {
                emptyMsg.style.display = 'flex';
            }
        } else {
            const emptyMsg = document.getElementById('filterEmptyMessage');
            if (emptyMsg) emptyMsg.style.display = 'none';
        }
    }
    
    filterBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            filterBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            currentFilter = this.dataset.filter;
            filterAndSort();
        });
    });
    
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            currentSort = this.value;
            filterAndSort();
        });
    }
});
</script>

<%@ include file="../footer.jsp" %>