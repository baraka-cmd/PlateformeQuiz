<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.learnpath.models.Cours" %>
<%@ page import="com.learnpath.models.Theme" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours.jsp
     RÔLE    : Page d'administration des cours
              - Tableau paginé avec filtres et recherche
              - Statistiques globales
              - Actions : Voir plan, Modifier, Modules, Chapitres,
                Statistiques, Supprimer/Archiver
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    List<Map<String, Object>> coursAvecStats = (List<Map<String, Object>>) request.getAttribute("coursAvecStats");
    Integer totalCours = (Integer) request.getAttribute("totalCours");
    Integer totalCoursFiltres = (Integer) request.getAttribute("totalCoursFiltres");
    Integer pageActuelle = (Integer) request.getAttribute("page");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    String recherche = (String) request.getAttribute("recherche");
    String filtreTheme = (String) request.getAttribute("filtreTheme");
    String filtreNiveau = (String) request.getAttribute("filtreNiveau");
    String filtreStatut = (String) request.getAttribute("filtreStatut");
    String tri = (String) request.getAttribute("tri");
    List<Theme> themes = (List<Theme>) request.getAttribute("themes");
    Map<String, Object> statsGlobales = (Map<String, Object>) request.getAttribute("statsGlobales");
    String urlPagination = (String) request.getAttribute("urlPagination");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    // Valeurs par défaut
    if (coursAvecStats == null) coursAvecStats = new java.util.ArrayList<>();
    if (totalCours == null) totalCours = 0;
    if (totalCoursFiltres == null) totalCoursFiltres = 0;
    if (pageActuelle == null) pageActuelle = 1;
    if (totalPages == null) totalPages = 1;
    if (recherche == null) recherche = "";
    if (filtreTheme == null) filtreTheme = "tous";
    if (filtreNiveau == null) filtreNiveau = "tous";
    if (filtreStatut == null) filtreStatut = "tous";
    if (tri == null) tri = "id";
    if (themes == null) themes = new java.util.ArrayList<>();
    if (statsGlobales == null) statsGlobales = new java.util.LinkedHashMap<>();
    if (urlPagination == null) urlPagination = "/admin/cours?";

    int totalApprenants = statsGlobales.get("totalApprenants") != null ? (Integer) statsGlobales.get("totalApprenants") : 0;
    int totalCertificats = statsGlobales.get("totalCertificats") != null ? (Integer) statsGlobales.get("totalCertificats") : 0;

    request.setAttribute("pageTitle", "Gestion des cours - Administration - LearnPath");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-cours.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<div class="ac-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </span>
    </nav>

    <%-- ===== MESSAGES FLASH ===== --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes" data-timeout="6000">
            <i class="fas fa-check-circle"></i><span><%= messageSucces %></span>
        </div>
    <% } %>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i><span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== EN-TÊTE AVEC STATISTIQUES ===== --%>
    <div class="ac-header">
        <div class="ac-header-left">
            <div class="ac-header-icon">
                <i class="fas fa-graduation-cap"></i>
            </div>
            <div class="ac-header-text">
                <h1>Gestion des cours</h1>
                <p>Gérez l'ensemble des cours de la plateforme</p>
            </div>
        </div>
        <div class="ac-header-right">
            <div class="ac-header-stats">
                <div class="ac-header-stat">
                    <span class="ac-header-stat-nombre"><%= totalCours %></span>
                    <span class="ac-header-stat-label">Cours</span>
                </div>
                <div class="ac-header-stat-divider"></div>
                <div class="ac-header-stat">
                    <span class="ac-header-stat-nombre"><%= totalApprenants %></span>
                    <span class="ac-header-stat-label">Apprenants</span>
                </div>
                <div class="ac-header-stat-divider"></div>
                <div class="ac-header-stat">
                    <span class="ac-header-stat-nombre"><%= totalCertificats %></span>
                    <span class="ac-header-stat-label">Certificats</span>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/admin/cours/creer" class="ac-btn-ajouter">
                <i class="fas fa-plus-circle"></i><span>Créer un cours</span>
            </a>
        </div>
    </div>

    <%-- ===== BARRE DE STATISTIQUES ===== --%>
    <div class="ac-stats-bar">
        <div class="ac-stat-item">
            <i class="fas fa-book"></i>
            <strong><%= statsGlobales.getOrDefault("total", 0) %></strong> total
        </div>
        <div class="ac-stat-separator"></div>
        <div class="ac-stat-item ac-stat-publie">
            <i class="fas fa-check-circle"></i>
            <strong><%= statsGlobales.getOrDefault("publie", 0) %></strong> publiés
        </div>
        <div class="ac-stat-separator"></div>
        <div class="ac-stat-item ac-stat-brouillon">
            <i class="fas fa-pencil-alt"></i>
            <strong><%= statsGlobales.getOrDefault("brouillon", 0) %></strong> brouillons
        </div>
        <div class="ac-stat-separator"></div>
        <div class="ac-stat-item ac-stat-archive">
            <i class="fas fa-archive"></i>
            <strong><%= statsGlobales.getOrDefault("archive", 0) %></strong> archivés
        </div>
        <div class="ac-stat-separator"></div>
        <div class="ac-stat-item">
            <i class="fas fa-tags"></i>
            <strong><%= statsGlobales.getOrDefault("themes", 0) %></strong> thèmes
        </div>
    </div>

    <%-- ===== BARRE D'OUTILS : RECHERCHE + FILTRES + TRI ===== --%>
    <div class="ac-toolbar">
        <form action="${pageContext.request.contextPath}/admin/cours" method="GET" class="ac-toolbar-form" id="formFiltres">
            
            <%-- Recherche --%>
            <div class="ac-recherche-wrapper">
                <i class="fas fa-search"></i>
                <input type="text" name="recherche" value="<%= recherche %>" 
                       placeholder="Rechercher par titre, description ou thème..." 
                       class="ac-recherche-input" id="rechercheInput">
                <% if (recherche != null && !recherche.isEmpty()) { %>
                    <a href="${pageContext.request.contextPath}/admin/cours" class="ac-recherche-clear" title="Effacer">
                        <i class="fas fa-times"></i>
                    </a>
                <% } %>
            </div>

            <%-- Filtre Thème --%>
            <select name="theme" id="filtreTheme" class="ac-select">
                <option value="tous" <%= "tous".equals(filtreTheme) ? "selected" : "" %>>Tous les thèmes</option>
                <% for (Theme t : themes) { %>
                    <option value="<%= t.getNom() %>" <%= t.getNom().equals(filtreTheme) ? "selected" : "" %>><%= t.getNom() %></option>
                <% } %>
            </select>

            <%-- Filtre Niveau --%>
            <select name="niveau" id="filtreNiveau" class="ac-select">
                <option value="tous" <%= "tous".equals(filtreNiveau) ? "selected" : "" %>>Tous les niveaux</option>
                <option value="elementaire" <%= "elementaire".equals(filtreNiveau) ? "selected" : "" %>>Élémentaire</option>
                <option value="moyen" <%= "moyen".equals(filtreNiveau) ? "selected" : "" %>>Moyen</option>
                <option value="avance" <%= "avance".equals(filtreNiveau) ? "selected" : "" %>>Avancé</option>
            </select>

            <%-- Filtre Statut --%>
            <select name="statut" id="filtreStatut" class="ac-select">
                <option value="tous" <%= "tous".equals(filtreStatut) ? "selected" : "" %>>Tous les statuts</option>
                <option value="publié" <%= "publié".equals(filtreStatut) ? "selected" : "" %>>Publié</option>
                <option value="brouillon" <%= "brouillon".equals(filtreStatut) ? "selected" : "" %>>Brouillon</option>
                <option value="archivé" <%= "archivé".equals(filtreStatut) ? "selected" : "" %>>Archivé</option>
            </select>

            <%-- Tri --%>
            <select name="tri" id="filtreTri" class="ac-select">
                <option value="id" <%= "id".equals(tri) ? "selected" : "" %>>Tri : ID</option>
                <option value="titre" <%= "titre".equals(tri) ? "selected" : "" %>>Tri : Titre A-Z</option>
                <option value="titre-desc" <%= "titre-desc".equals(tri) ? "selected" : "" %>>Tri : Titre Z-A</option>
                <option value="theme" <%= "theme".equals(tri) ? "selected" : "" %>>Tri : Thème</option>
                <option value="niveau" <%= "niveau".equals(tri) ? "selected" : "" %>>Tri : Niveau</option>
                <option value="recent" <%= "recent".equals(tri) ? "selected" : "" %>>Tri : Plus récent</option>
                <option value="ancien" <%= "ancien".equals(tri) ? "selected" : "" %>>Tri : Plus ancien</option>
            </select>

            <%-- Réinitialiser --%>
            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreTheme) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreStatut) || !"id".equals(tri)) { %>
                <a href="${pageContext.request.contextPath}/admin/cours" class="ac-btn-reset">
                    <i class="fas fa-undo"></i><span>Réinitialiser</span>
                </a>
            <% } %>
        </form>
    </div>

    <%-- ===== COMPTEUR DE RÉSULTATS ===== --%>
    <div class="ac-resultats-info">
        <span>
            <i class="fas fa-list"></i>
            <strong><%= totalCoursFiltres %></strong> cours trouvé<%= totalCoursFiltres > 1 ? "s" : "" %>
        </span>
        <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreTheme) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreStatut)) { %>
            <span class="ac-badge-filtre"><i class="fas fa-filter"></i> Filtré</span>
        <% } %>
    </div>


        <%-- ===== TABLEAU DES COURS ===== --%>
<div class="ac-tableau-section">
    <% if (coursAvecStats != null && !coursAvecStats.isEmpty()) { %>
        <div class="ac-table-responsive">
            <table class="ac-table">
                <colgroup>
                    <col style="width: 65px;">   <col style="width: auto;">   <col style="width: 130px;">  <col style="width: 110px;">  <col style="width: 110px;">  <col style="width: 70px;">   <col style="width: 70px;">   <col style="width: 85px;">   <col style="width: 85px;">   <col style="width: 120px;">  <col style="width: 210px;">  </colgroup>
                <thead>
                    <tr>
                        <th class="ac-th-id">ID</th>
                        <th class="ac-th-cours">Cours</th>
                        <th class="ac-th-theme">Thème</th>
                        <th class="ac-th-niveau">Niveau</th>
                        <th class="ac-th-statut">Statut</th>
                        <th class="ac-th-chapitres">Chap.</th>
                        <th class="ac-th-lecons">Leçons</th>
                        <th class="ac-th-apprenants">Appren.</th>
                        <th class="ac-th-certificats">Certif.</th>
                        <th class="ac-th-date">Création</th>
                        <th class="ac-th-actions">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("fr", "FR"));
                        for (Map<String, Object> data : coursAvecStats) {
                            Cours cours = (Cours) data.get("cours");
                            int nbApprenants = (Integer) data.get("nbApprenants");
                            int nbCertificats = (Integer) data.get("nbCertificats");

                            String titreCours = cours.getTitre() != null ? cours.getTitre() : "Sans titre";
                            String themeCours = cours.getTheme() != null ? cours.getTheme() : "N/A";
                            String niveauCours = cours.getNiveau() != null ? cours.getNiveauFormate() : "N/A";
                            String statutCours = cours.getStatut() != null ? cours.getStatut() : "brouillon";
                            String dateCreation = cours.getDateCreation() != null ? sdf.format(cours.getDateCreation()) : "N/A";

                            String statutClass = "";
                            String statutIcone = "";
                            String statutLibelle = "";
                            if ("publié".equalsIgnoreCase(statutCours)) {
                                statutClass = "ac-badge-publie";
                                statutIcone = "fa-check-circle";
                                statutLibelle = "Publié";
                            } else if ("archivé".equalsIgnoreCase(statutCours) || "archive".equalsIgnoreCase(statutCours)) {
                                statutClass = "ac-badge-archive";
                                statutIcone = "fa-archive";
                                statutLibelle = "Archivé";
                            } else {
                                statutClass = "ac-badge-brouillon";
                                statutIcone = "fa-pencil-alt";
                                statutLibelle = "Brouillon";
                            }

                            boolean peutSupprimer = (nbApprenants == 0 && nbCertificats == 0);
                    %>
                    <tr class="ac-row" data-cours-id="<%= cours.getId() %>">
                        <td class="ac-td-id" data-label="ID">
                            <span class="ac-cell-id">#<%= cours.getId() %></span>
                        </td>
                        
                        <td class="ac-td-cours" data-label="Cours">
                            <div class="ac-cours-identite">
                                <span class="ac-cours-icon" style="background: <%= cours.getCouleurTheme() != null ? cours.getCouleurTheme() : "#667eea" %>;">
                                    <i class="<%= cours.getIconeTheme() != null ? cours.getIconeTheme() : "fas fa-book" %>"></i>
                                </span>
                                <div>
                                    <span class="ac-cours-titre"><%= titreCours %></span>
                                    <% if (cours.getDescription() != null && !cours.getDescription().isEmpty()) { %>
                                        <span class="ac-cours-desc">
                                            <%= cours.getDescription().length() > 60 ? cours.getDescription().substring(0, 60) + "..." : cours.getDescription() %>
                                        </span>
                                    <% } %>
                                </div>
                            </div>
                        </td>
                        
                        <td class="ac-td-theme" data-label="Thème">
                            <span class="ac-text-theme"><%= themeCours %></span>
                        </td>
                        
                        <td class="ac-td-niveau" data-label="Niveau">
                            <span class="ac-badge-niveau ac-badge-niveau-<%= cours.getNiveau() %>">
                                <i class="<%= cours.getIconeNiveau() %>"></i> <%= niveauCours %>
                            </span>
                        </td>
                        
                        <td class="ac-td-statut" data-label="Statut">
                            <span class="ac-badge-statut <%= statutClass %>">
                                <i class="fas <%= statutIcone %>"></i> <%= statutLibelle %>
                            </span>
                        </td>
                        
                        <td class="ac-td-chapitres" data-label="Chap.">
                            <strong><%= cours.getNombreChapitres() %></strong>
                        </td>
                        
                        <td class="ac-td-lecons" data-label="Leçons">
                            <strong><%= cours.getNombreLecons() %></strong>
                        </td>
                        
                        <td class="ac-td-apprenants" data-label="Appren.">
                            <% if (nbApprenants > 0) { %>
                                <span class="ac-badge-apprenants"><i class="fas fa-users"></i> <%= nbApprenants %></span>
                            <% } else { %>
                                <span class="ac-text-muted">—</span>
                            <% } %>
                        </td>
                        
                        <td class="ac-td-certificats" data-label="Certif.">
                            <% if (nbCertificats > 0) { %>
                                <span class="ac-badge-certificats"><i class="fas fa-certificate"></i> <%= nbCertificats %></span>
                            <% } else { %>
                                <span class="ac-text-muted">—</span>
                            <% } %>
                        </td>
                        
                        <td class="ac-td-date" data-label="Création">
                            <span class="ac-date-text"><%= dateCreation %></span>
                        </td>
                        
                        <td class="ac-td-actions" data-label="Actions">
                            <div class="ac-actions-group">
                                <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" 
                                   class="ac-btn-icon ac-btn-voir" title="Voir le plan" target="_blank">
                                    <i class="fas fa-eye"></i>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/cours/modifier?id=<%= cours.getId() %>" 
                                   class="ac-btn-icon ac-btn-modifier" title="Modifier">
                                    <i class="fas fa-pen"></i>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/cours/modules?coursId=<%= cours.getId() %>" 
                                   class="ac-btn-icon ac-btn-modules" title="Gérer les modules">
                                    <i class="fas fa-cubes"></i>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= cours.getId() %>" 
                                   class="ac-btn-icon ac-btn-chapitres" title="Gérer les chapitres">
                                    <i class="fas fa-bookmark"></i>
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/cours/statistiques?id=<%= cours.getId() %>" 
                                   class="ac-btn-icon ac-btn-stats" title="Statistiques">
                                    <i class="fas fa-chart-bar"></i>
                                </a>
                                <form action="${pageContext.request.contextPath}/admin/cours/supprimer" method="POST" class="ac-form-inline"
                                      onsubmit="return confirmerSuppression(event, '<%= titreCours.replace("'", "\\'") %>', <%= nbApprenants %>, <%= nbCertificats %>, <%= peutSupprimer %>)">
                                    <input type="hidden" name="id" value="<%= cours.getId() %>">
                                    <button type="submit" class="ac-btn-icon ac-btn-supprimer" title="<%= peutSupprimer ? "Supprimer" : "Archiver (cours avec apprenants)" %>">
                                        <i class="fas <%= peutSupprimer ? "fa-trash-alt" : "fa-archive" %>"></i>
                                    </button>
                                </form>
                            </div>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>

        
        <%-- ===== PAGINATION ===== --%>
        <% if (totalPages > 1) { %>
            <div class="ac-pagination">
                <% if (pageActuelle > 1) { %>
                    <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle - 1 %>" class="ac-page-link" title="Page précédente">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                <% } else { %>
                    <span class="ac-page-link ac-page-disabled"><i class="fas fa-chevron-left"></i></span>
                <% } %>

                <%
                    int debutPage = Math.max(1, pageActuelle - 2);
                    int finPage = Math.min(totalPages, pageActuelle + 2);
                    if (debutPage > 1) {
                %>
                    <a href="${pageContext.request.contextPath}<%= urlPagination %>page=1" class="ac-page-link">1</a>
                    <% if (debutPage > 2) { %><span class="ac-page-ellipsis">…</span><% } %>
                <% } %>

                <% for (int i = debutPage; i <= finPage; i++) { %>
                    <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= i %>" 
                       class="ac-page-link <%= i == pageActuelle ? "ac-page-active" : "" %>"><%= i %></a>
                <% } %>

                <% if (finPage < totalPages) { %>
                    <% if (finPage < totalPages - 1) { %><span class="ac-page-ellipsis">…</span><% } %>
                    <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= totalPages %>" class="ac-page-link"><%= totalPages %></a>
                <% } %>

                <% if (pageActuelle < totalPages) { %>
                    <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle + 1 %>" class="ac-page-link" title="Page suivante">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                <% } else { %>
                    <span class="ac-page-link ac-page-disabled"><i class="fas fa-chevron-right"></i></span>
                <% } %>

                <span class="ac-page-info">Page <strong><%= pageActuelle %></strong>/<strong><%= totalPages %></strong> · <%= totalCoursFiltres %> résultats</span>
            </div>
        <% } %>

    <% } else { %>
        <div class="ac-empty">
            <div class="ac-empty-icon"><i class="fas fa-graduation-cap"></i></div>
            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreTheme) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreStatut)) { %>
                <h3>Aucun cours trouvé</h3>
                <p>Aucun cours ne correspond à vos critères. Modifiez vos filtres.</p>
                <a href="${pageContext.request.contextPath}/admin/cours" class="ac-btn-reset-empty">
                    <i class="fas fa-undo"></i> Réinitialiser les filtres
                </a>
            <% } else { %>
                <h3>Aucun cours configuré</h3>
                <p>Commencez par créer votre premier cours.</p>
                <a href="${pageContext.request.contextPath}/admin/cours/creer" class="ac-btn-primary-empty">
                    <i class="fas fa-plus-circle"></i> Créer un cours
                </a>
            <% } %>
        </div>
    <% } %>
</div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="ac-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="ac-btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard
        </a>
    </div>

</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    function confirmerSuppression(event, titre, nbApprenants, nbCertificats, peutSupprimer) {
        event.preventDefault();
        var form = event.target.closest('form');

        if (peutSupprimer) {
            if (confirm('⚠️ SUPPRESSION DÉFINITIVE\n\nCours : « ' + titre + ' »\n\nCette action est irréversible. Continuer ?')) {
                form.submit();
            }
        } else {
            var message = 'Ce cours ne peut pas être supprimé car il a :\n';
            if (nbApprenants > 0) message += '• ' + nbApprenants + ' apprenant(s)\n';
            if (nbCertificats > 0) message += '• ' + nbCertificats + ' certificat(s)\n';
            message += '\nVoulez-vous l\'archiver à la place ?';
            
            if (confirm(message)) {
                var input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'action';
                input.value = 'archiver';
                form.appendChild(input);
                form.submit();
            }
        }
        return false;
    }

    document.addEventListener('DOMContentLoaded', function() {
        var form = document.getElementById('formFiltres');
        if (!form) return;

        // Filtres et tri : soumission au changement
        ['filtreTheme', 'filtreNiveau', 'filtreStatut', 'filtreTri'].forEach(function(id) {
            var el = document.getElementById(id);
            if (el) el.addEventListener('change', function() { form.submit(); });
        });

        // Recherche avec debounce
        var rechercheInput = document.getElementById('rechercheInput');
        if (rechercheInput) {
            var timeout;
            rechercheInput.dataset.previousValue = rechercheInput.value;
            rechercheInput.addEventListener('input', function() {
                clearTimeout(timeout);
                var val = this.value;
                timeout = setTimeout(function() {
                    if (val !== rechercheInput.dataset.previousValue) {
                        rechercheInput.dataset.previousValue = val;
                        form.submit();
                    }
                }, 600);
            });
            rechercheInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    clearTimeout(timeout);
                    if (this.value !== rechercheInput.dataset.previousValue) {
                        rechercheInput.dataset.previousValue = this.value;
                        form.submit();
                    }
                }
            });
        }
    });
</script>

<%@ include file="../footer-connecte.jsp" %>