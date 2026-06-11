<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.learnpath.models.Theme" %>

<%-- ============================================================
     FICHIER : views/admin/admin-themes.jsp
     RÔLE    : Page d'administration des thèmes de cours
              - Tableau paginé avec filtres et recherche
              - Statistiques globales
              - Actions : Créer, Modifier, Supprimer
              - Indicateurs visuels (icône, couleur, cours associés)
              - Design professionnel complet
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    List<Map<String, Object>> themesAvecStats = (List<Map<String, Object>>) request.getAttribute("themes");
    Integer totalThemes = (Integer) request.getAttribute("totalThemes");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");
    
    // Paramètres de pagination et filtres
    String recherche = (String) request.getAttribute("recherche");
    String filtreOrdre = (String) request.getAttribute("filtreOrdre");
    String tri = (String) request.getAttribute("tri");
    Integer pageActuelle = (Integer) request.getAttribute("page");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Integer totalThemesFiltres = (Integer) request.getAttribute("totalThemesFiltres");
    String urlPagination = (String) request.getAttribute("urlPagination");
    
    // Valeurs par défaut
    if (themesAvecStats == null) themesAvecStats = new java.util.ArrayList<Map<String, Object>>();
    if (totalThemes == null) totalThemes = 0;
    if (recherche == null) recherche = "";
    if (filtreOrdre == null) filtreOrdre = "tous";
    if (tri == null) tri = "ordre";
    if (pageActuelle == null) pageActuelle = 1;
    if (totalPages == null) totalPages = 1;
    if (totalThemesFiltres == null) totalThemesFiltres = 0;
    if (urlPagination == null) urlPagination = "/admin/themes?";
    
    // Calculs pour les stats
    int totalCoursAssocies = 0;
    if (themesAvecStats != null) {
        for (Map<String, Object> data : themesAvecStats) {
            totalCoursAssocies += (Integer) data.get("nbCours");
        }
    }
    
    // Titre de la page
    request.setAttribute("pageTitle", "Gestion des thèmes - Administration - LearnPath");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-themes.css\">";
    request.setAttribute("extraHead", adminCSS);
 %>  
    
<%@ include file="../header.jsp" %>

<%-- ============================================================
     CONTENEUR PRINCIPAL
     ============================================================ --%>
<div class="admin-themes-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-tags"></i>
            <span>Thèmes</span>
        </span>
    </nav>

    <%-- ============================================================
         MESSAGES FLASH
         ============================================================ --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes" data-timeout="6000">
            <i class="fas fa-check-circle"></i>
            <span><%= messageSucces %></span>
        </div>
    <% } %>
    
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ============================================================
         EN-TÊTE AVEC STATISTIQUES
         ============================================================ --%>
    <div class="admin-themes-header">
        
        <%-- Partie gauche : Titre et description --%>
        <div class="admin-header-left">
            <div class="admin-header-icon">
                <i class="fas fa-tags"></i>
            </div>
            <div class="admin-header-text">
                <h1>Gestion des thèmes</h1>
                <p>Configurez les thèmes utilisés pour organiser les cours sur la plateforme</p>
            </div>
        </div>
        
        <%-- Partie droite : Statistiques rapides + Bouton --%>
        <div class="admin-header-right">
            
            <%-- Mini-statistiques --%>
            <div class="header-stats-group">
                <div class="header-stat-item">
                    <span class="header-stat-nombre"><%= totalThemes %></span>
                    <span class="header-stat-label">Thèmes</span>
                </div>
                <div class="header-stat-divider"></div>
                <div class="header-stat-item">
                    <span class="header-stat-nombre"><%= totalCoursAssocies %></span>
                    <span class="header-stat-label">Cours liés</span>
                </div>
            </div>
            
            <%-- Bouton Créer --%>
            <a href="${pageContext.request.contextPath}/admin/themes/creer" 
               class="btn-ajouter-theme">
                <i class="fas fa-plus-circle"></i>
                <span>Ajouter un thème</span>
            </a>
            
        </div>
        
    </div>

        <%-- ============================================================
         BARRE D'OUTILS : RECHERCHE + FILTRES + TRI
         ============================================================ --%>
    <div class="admin-toolbar">
        <form action="${pageContext.request.contextPath}/admin/themes" method="GET" 
              class="toolbar-form" id="formFiltres">
            
            <%-- Champ de recherche --%>
            <div class="recherche-input-wrapper">
                <i class="fas fa-search"></i>
                <input 
                    type="text" 
                    name="recherche" 
                    value="<%= recherche %>" 
                    placeholder="Rechercher un thème par nom ou description..." 
                    class="recherche-input"
                    id="rechercheInput"
                >
                <% if (recherche != null && !recherche.isEmpty()) { %>
                    <a href="${pageContext.request.contextPath}/admin/themes" 
                       class="recherche-clear-btn"
                       title="Effacer la recherche">
                        <i class="fas fa-times"></i>
                    </a>
                <% } %>
            </div>
            
            <%-- Filtre par cours associés --%>
            <select name="ordre" id="filtreOrdre" class="toolbar-select">
                <option value="tous" <%= "tous".equals(filtreOrdre) ? "selected" : "" %>>
                    Tous les thèmes
                </option>
                <option value="avec-cours" <%= "avec-cours".equals(filtreOrdre) ? "selected" : "" %>>
                    Avec cours associés
                </option>
                <option value="sans-cours" <%= "sans-cours".equals(filtreOrdre) ? "selected" : "" %>>
                    Sans cours associés
                </option>
            </select>
            
            <%-- Tri --%>
            <select name="tri" id="filtreTri" class="toolbar-select">
                <option value="ordre" <%= "ordre".equals(tri) ? "selected" : "" %>>
                    Tri : Ordre d'affichage
                </option>
                <option value="nom" <%= "nom".equals(tri) ? "selected" : "" %>>
                    Tri : Nom (A-Z)
                </option>
                <option value="nom-desc" <%= "nom-desc".equals(tri) ? "selected" : "" %>>
                    Tri : Nom (Z-A)
                </option>
                <option value="cours" <%= "cours".equals(tri) ? "selected" : "" %>>
                    Tri : Nombre de cours
                </option>
                <option value="recent" <%= "recent".equals(tri) ? "selected" : "" %>>
                    Tri : Plus récent
                </option>
                <option value="ancien" <%= "ancien".equals(tri) ? "selected" : "" %>>
                    Tri : Plus ancien
                </option>
            </select>
            
            <%-- Bouton réinitialiser les filtres --%>
            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreOrdre) || !"ordre".equals(tri)) { %>
                <a href="${pageContext.request.contextPath}/admin/themes" 
                   class="btn-reset-filtres" 
                   title="Réinitialiser tous les filtres">
                    <i class="fas fa-undo"></i>
                    <span>Réinitialiser</span>
                </a>
            <% } %>
            
        </form>
    </div>

    <%-- ============================================================
         GUIDE CONTEXTUEL (affiché seulement si aucun thème)
         ============================================================ --%>
    <% if (themesAvecStats == null || themesAvecStats.isEmpty()) { %>
        <div class="admin-guide">
            <div class="guide-icon">
                <i class="fas fa-lightbulb"></i>
            </div>
            <div class="guide-content">
                <strong>À quoi servent les thèmes ?</strong>
                <p>
                    Les thèmes permettent d'organiser les cours par catégories (ex: Développement Web, Java, Python...). 
                    Chaque cours que vous créerez sera rattaché à un thème. 
                    Configurez vos thèmes avant de créer des cours pour une meilleure organisation.
                </p>
            </div>
        </div>
    <% } %>

        <%-- ============================================================
         COMPTEUR DE RÉSULTATS
         ============================================================ --%>
    <div class="resultats-info">
        <div class="resultats-count">
            <i class="fas fa-list"></i>
            <span>
                <strong><%= totalThemesFiltres %></strong> 
                thème<%= totalThemesFiltres > 1 ? "s" : "" %> trouvé<%= totalThemesFiltres > 1 ? "s" : "" %>
            </span>
            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreOrdre)) { %>
                <span class="resultats-filtered-badge">
                    <i class="fas fa-filter"></i> Filtré
                </span>
            <% } %>
        </div>
    </div>

    <%-- ============================================================
         TABLEAU DES THÈMES
         ============================================================ --%>
    <div class="admin-tableau-section">
        
        <% if (themesAvecStats != null && !themesAvecStats.isEmpty()) { %>
            
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th class="th-id">
                                <span>ID</span>
                            </th>
                            <th class="th-theme">
                                <i class="fas fa-tag"></i>
                                <span>Thème</span>
                            </th>
                            <th class="th-description">
                                <i class="fas fa-align-left"></i>
                                <span>Description</span>
                            </th>
                            <th class="th-center th-icone">
                                <i class="fas fa-icons"></i>
                                <span>Icône</span>
                            </th>
                            <th class="th-center th-couleur">
                                <i class="fas fa-palette"></i>
                                <span>Couleur</span>
                            </th>
                            <th class="th-center th-ordre">
                                <i class="fas fa-sort-numeric-down"></i>
                                <span>Ordre</span>
                            </th>
                            <th class="th-center th-cours">
                                <i class="fas fa-book"></i>
                                <span>Cours</span>
                            </th>
                            <th class="th-date">
                                <i class="fas fa-calendar-alt"></i>
                                <span>Création</span>
                            </th>
                            <th class="th-action">
                                <span>Actions</span>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("fr", "FR"));
                            int indexLigne = 1;
                            
                            for (Map<String, Object> data : themesAvecStats) {
                                Theme theme = (Theme) data.get("theme");
                                int nbCours = (Integer) data.get("nbCours");
                                
                                // Tronquer la description si trop longue
                                String description = theme.getDescription();
                                String descriptionCourte = "";
                                if (description != null && !description.isEmpty()) {
                                    descriptionCourte = description.length() > 80 
                                        ? description.substring(0, 80) + "..." 
                                        : description;
                                }
                                
                                // Formater la date
                                String dateCreation = theme.getDateCreation() != null 
                                    ? sdf.format(theme.getDateCreation()) 
                                    : "N/A";
                                
                                // Classes pour les badges
                                String badgeCoursClass = nbCours > 0 ? "badge-cours-actif" : "badge-cours-zero";
                                String badgeCoursIcone = nbCours > 0 ? "fa-check-circle" : "fa-circle";
                        %>
                            <tr class="theme-row" data-theme-id="<%= theme.getId() %>">
                                
                                <%-- ID --%>
                                <td class="td-id">
                                    <span class="cell-id">#<%= theme.getId() %></span>
                                </td>
                                
                                <%-- Nom du thème --%>
                                <td class="td-theme">
                                    <div class="theme-identity">
                                        <span class="theme-mini-icon" 
                                              style="background: <%= theme.getCouleur() %>;">
                                            <i class="<%= theme.getIcone() %>"></i>
                                        </span>
                                        <div class="theme-name-group">
                                            <span class="theme-nom"><%= theme.getNom() %></span>
                                        </div>
                                    </div>
                                </td>
                                
                                <%-- Description --%>
                                <td class="td-description">
                                    <% if (descriptionCourte != null && !descriptionCourte.isEmpty()) { %>
                                        <span class="cell-description" 
                                              title="<%= description != null ? description.replace("\"", "&quot;") : "" %>">
                                            <%= descriptionCourte %>
                                        </span>
                                    <% } else { %>
                                        <span class="cell-empty">—</span>
                                    <% } %>
                                </td>
                                
                                <%-- Icône --%>
                                <td class="td-center td-icone">
                                    <div class="icone-display">
                                        <span class="icone-badge" 
                                              style="background: <%= theme.getCouleur() %>20; color: <%= theme.getCouleur() %>;">
                                            <i class="<%= theme.getIcone() %>"></i>
                                        </span>
                                        <code class="icone-code"><%= theme.getIcone() %></code>
                                    </div>
                                </td>
                                
                                <%-- Couleur --%>
                                <td class="td-center td-couleur">
                                    <div class="couleur-display">
                                        <span class="couleur-swatch" 
                                              style="background: <%= theme.getCouleur() %>;"
                                              title="<%= theme.getCouleur() %>">
                                        </span>
                                        <code class="couleur-code"><%= theme.getCouleur() %></code>
                                    </div>
                                </td>
                                
                                <%-- Ordre --%>
                                <td class="td-center td-ordre">
                                    <span class="cell-ordre">
                                        <span class="ordre-number"><%= theme.getOrdre() %></span>
                                    </span>
                                </td>
                                
                                <%-- Nombre de cours --%>
                                <td class="td-center td-cours">
                                    <span class="badge-cours <%= badgeCoursClass %>">
                                        <i class="fas <%= badgeCoursIcone %>"></i>
                                        <span><%= nbCours %></span>
                                    </span>
                                </td>
                                
                                <%-- Date de création --%>
                                <td class="td-date">
                                    <span class="cell-date">
                                        <i class="far fa-calendar-alt"></i>
                                        <%= dateCreation %>
                                    </span>
                                </td>
                                
                                <%-- Actions --%>
                                <td class="td-actions">
                                    <div class="actions-group">
                                        
                                        <%-- Bouton Modifier --%>
                                        <a href="${pageContext.request.contextPath}/admin/themes/modifier?id=<%= theme.getId() %>" 
                                           class="btn-action-icon btn-modifier" 
                                           title="Modifier ce thème"
                                           data-tooltip="Modifier">
                                            <i class="fas fa-pen"></i>
                                        </a>
                                        
                                        <%-- Bouton Supprimer --%>
                                        <form action="${pageContext.request.contextPath}/admin/themes/supprimer" 
                                              method="POST" 
                                              class="form-action-inline"
                                              onsubmit="return confirmerSuppression(event, '<%= theme.getNom().replace("'", "\\'") %>', <%= nbCours %>)">
                                            <input type="hidden" name="id" value="<%= theme.getId() %>">
                                            <button type="submit" 
                                                    class="btn-action-icon btn-supprimer" 
                                                    title="Supprimer ce thème"
                                                    data-tooltip="Supprimer"
                                                    <%= nbCours > 0 ? "disabled style='opacity: 0.4; cursor: not-allowed;'" : "" %>>
                                                <i class="fas fa-trash-alt"></i>
                                            </button>
                                        </form>
                                        
                                    </div>
                                </td>
                                
                            </tr>
                        <%
                                indexLigne++;
                            }
                        %>
                    </tbody>
                </table>
            </div>
            
            <%-- ============================================================
                 PAGINATION
                 ============================================================ --%>
            <% if (totalPages > 1) { %>
                <div class="pagination-bar">
                    
                    <%-- Bouton Première page --%>
                    <% if (pageActuelle > 1) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=1" 
                           class="page-link page-first" 
                           title="Première page">
                            <i class="fas fa-angle-double-left"></i>
                        </a>
                    <% } else { %>
                        <span class="page-link page-first disabled">
                            <i class="fas fa-angle-double-left"></i>
                        </span>
                    <% } %>
                    
                    <%-- Bouton Page précédente --%>
                    <% if (pageActuelle > 1) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle - 1 %>" 
                           class="page-link page-prev" 
                           title="Page précédente">
                            <i class="fas fa-angle-left"></i>
                        </a>
                    <% } else { %>
                        <span class="page-link page-prev disabled">
                            <i class="fas fa-angle-left"></i>
                        </span>
                    <% } %>
                    
                    <%-- Numéros de pages --%>
                    <div class="page-numbers">
                        <%
                            int debutPage = Math.max(1, pageActuelle - 2);
                            int finPage = Math.min(totalPages, pageActuelle + 2);
                            
                            // Toujours afficher la page 1
                            if (debutPage > 1) {
                        %>
                            <a href="${pageContext.request.contextPath}<%= urlPagination %>page=1" 
                               class="page-link page-number">1</a>
                            <% if (debutPage > 2) { %>
                                <span class="page-ellipsis">…</span>
                            <% } %>
                        <%
                            }
                            
                            // Pages du milieu
                            for (int i = debutPage; i <= finPage; i++) {
                                boolean isActive = (i == pageActuelle);
                        %>
                            <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= i %>" 
                               class="page-link page-number <%= isActive ? "active" : "" %>"
                               <%= isActive ? "aria-current=\"page\"" : "" %>>
                                <%= i %>
                            </a>
                        <%
                            }
                            
                            // Toujours afficher la dernière page
                            if (finPage < totalPages) {
                                if (finPage < totalPages - 1) {
                        %>
                                <span class="page-ellipsis">…</span>
                        <%
                                }
                        %>
                            <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= totalPages %>" 
                               class="page-link page-number"><%= totalPages %></a>
                        <%
                            }
                        %>
                    </div>
                    
                    <%-- Bouton Page suivante --%>
                    <% if (pageActuelle < totalPages) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle + 1 %>" 
                           class="page-link page-next" 
                           title="Page suivante">
                            <i class="fas fa-angle-right"></i>
                        </a>
                    <% } else { %>
                        <span class="page-link page-next disabled">
                            <i class="fas fa-angle-right"></i>
                        </span>
                    <% } %>
                    
                    <%-- Bouton Dernière page --%>
                    <% if (pageActuelle < totalPages) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= totalPages %>" 
                           class="page-link page-last" 
                           title="Dernière page">
                            <i class="fas fa-angle-double-right"></i>
                        </a>
                    <% } else { %>
                        <span class="page-link page-last disabled">
                            <i class="fas fa-angle-double-right"></i>
                        </span>
                    <% } %>
                    
                    <%-- Info pagination --%>
                    <span class="page-info">
                        Page <strong><%= pageActuelle %></strong> sur <strong><%= totalPages %></strong>
                        · <%= totalThemesFiltres %> résultat<%= totalThemesFiltres > 1 ? "s" : "" %>
                    </span>
                    
                </div>
            <% } %>
            
        <% } else { %>
            
            <%-- ============================================================
                 ÉTAT VIDE
                 ============================================================ --%>
            <div class="empty-state">
                <div class="empty-icon">
                    <i class="fas fa-tags"></i>
                </div>
                
                <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreOrdre)) { %>
                    <%-- Aucun résultat avec les filtres actuels --%>
                    <h3>Aucun thème trouvé</h3>
                    <p>
                        Aucun thème ne correspond à vos critères de recherche.
                        Essayez de modifier vos filtres ou votre terme de recherche.
                    </p>
                    <div class="empty-actions">
                        <a href="${pageContext.request.contextPath}/admin/themes" 
                           class="btn-reset-empty">
                            <i class="fas fa-undo"></i>
                            Réinitialiser les filtres
                        </a>
                    </div>
                <% } else { %>
                    <%-- Aucun thème configuré --%>
                    <h3>Aucun thème configuré</h3>
                    <p>
                        Vous n'avez pas encore créé de thème de cours. 
                        Les thèmes permettent d'organiser vos cours par catégories 
                        (Développement Web, Java, Python, etc.).
                    </p>
                    <div class="empty-actions">
                        <a href="${pageContext.request.contextPath}/admin/themes/creer" 
                           class="btn-primary-empty">
                            <i class="fas fa-plus-circle"></i>
                            Créer votre premier thème
                        </a>
                    </div>
                <% } %>
            </div>
            
        <% } %>
        
    </div>

    <%-- ============================================================
         SECTION INFORMATIONS COMPLÉMENTAIRES
         ============================================================ --%>
    <div class="admin-info-cards">
        
        <%-- Carte info 1 : Hiérarchie --%>
        <div class="info-card">
            <div class="info-card-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-sitemap"></i>
            </div>
            <div class="info-card-content">
                <h4>Hiérarchie du contenu</h4>
                <p>
                    <strong>Thème</strong> → Cours → Modules (1-15) → Chapitres → Leçons → Ressources
                </p>
            </div>
        </div>
        
        <%-- Carte info 2 : Icônes --%>
        <div class="info-card">
            <div class="info-card-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-icons"></i>
            </div>
            <div class="info-card-content">
                <h4>Icônes Font Awesome</h4>
                <p>
                    Utilisez les classes Font Awesome pour les icônes. 
                    Ex: <code>fas fa-code</code>, <code>fab fa-python</code>
                </p>
            </div>
        </div>
        
        <%-- Carte info 3 : Suppression --%>
        <div class="info-card">
            <div class="info-card-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-shield-alt"></i>
            </div>
            <div class="info-card-content">
                <h4>Protection des données</h4>
                <p>
                    Un thème ne peut pas être supprimé s'il a des cours associés.
                    Réassignez ou supprimez d'abord les cours liés.
                </p>
            </div>
        </div>
        
    </div>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i>
            Retour au dashboard
        </a>
    </div>

</div>

<script>
    /**
     * Confirmation avant suppression d'un thème
     */
    function confirmerSuppression(event, nomTheme, nbCours) {
        event.preventDefault();
        
        if (nbCours > 0) {
            alert('Impossible de supprimer \u00AB ' + nomTheme + ' \u00BB.\n\n' + 
                  nbCours + ' cours y sont associes. Reassignez-les d\'abord.');
            return false;
        }
        
        if (confirm('⚠️ Suppression definitive\n\n' +
                    'Theme : \u00AB ' + nomTheme + ' \u00BB\n\n' +
                    'Cette action est irreversible. Confirmez-vous la suppression ?')) {
            event.target.closest('form').submit();
        }
        
        return false;
    }
    
    /**
     * Gestion des filtres, tri et recherche - SANS soumission automatique intempestive
     */
    document.addEventListener('DOMContentLoaded', function() {
        var form = document.getElementById('formFiltres');
        if (!form) return;
        
        // ==========================================
        // FILTRE PAR COURS ASSOCIÉS
        // ==========================================
        var filtreOrdre = document.getElementById('filtreOrdre');
        if (filtreOrdre) {
            filtreOrdre.addEventListener('change', function() {
                form.submit();
            });
        }
        
        // ==========================================
        // TRI
        // ==========================================
        var filtreTri = document.getElementById('filtreTri');
        if (filtreTri) {
            filtreTri.addEventListener('change', function() {
                form.submit();
            });
        }
        
        // ==========================================
        // RECHERCHE (soumission après 600ms d'inactivité)
        // ==========================================
        var rechercheInput = document.getElementById('rechercheInput');
        if (rechercheInput) {
            var rechercheTimeout;
            
            rechercheInput.addEventListener('input', function() {
                clearTimeout(rechercheTimeout);
                var valeurActuelle = this.value;
                
                rechercheTimeout = setTimeout(function() {
                    if (valeurActuelle !== rechercheInput.dataset.previousValue) {
                        rechercheInput.dataset.previousValue = valeurActuelle;
                        form.submit();
                    }
                }, 600);
            });
            
            // Entrée = soumission immédiate
            rechercheInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    clearTimeout(rechercheTimeout);
                    if (this.value !== rechercheInput.dataset.previousValue) {
                        rechercheInput.dataset.previousValue = this.value;
                        form.submit();
                    }
                }
            });
            
            // Initialiser la valeur précédente
            rechercheInput.dataset.previousValue = rechercheInput.value;
        }
    });
</script>
 <%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-themes.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>

