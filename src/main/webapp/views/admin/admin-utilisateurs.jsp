<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     FICHIER : views/admin/admin-utilisateurs.jsp
     RÔLE    : Page d'administration des utilisateurs
              - Tableau paginé avec filtres et recherche
              - Statistiques globales (6 cartes)
              - Actions : Voir détail, Activer/Désactiver, Changer rôle
              - Badges : Rôle, Statut, Premium
              - Design professionnel complet
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    List<Map<String, Object>> utilisateursAvecStats = (List<Map<String, Object>>) request.getAttribute("utilisateurs");
    Integer totalUtilisateurs = (Integer) request.getAttribute("totalUtilisateurs");
    Integer pageActuelle = (Integer) request.getAttribute("page");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    String recherche = (String) request.getAttribute("recherche");
    String filtreRole = (String) request.getAttribute("filtreRole");
    String filtreStatut = (String) request.getAttribute("filtreStatut");
    String filtrePremium = (String) request.getAttribute("filtrePremium");
    String tri = (String) request.getAttribute("tri");
    String ordre = (String) request.getAttribute("ordre");
    Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats");
    String urlPagination = (String) request.getAttribute("urlPagination");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");
    Integer adminId = (Integer) request.getAttribute("adminId");

    // Valeurs par défaut
    if (utilisateursAvecStats == null) utilisateursAvecStats = new java.util.ArrayList<>();
    if (totalUtilisateurs == null) totalUtilisateurs = 0;
    if (pageActuelle == null) pageActuelle = 1;
    if (totalPages == null) totalPages = 1;
    if (recherche == null) recherche = "";
    if (filtreRole == null) filtreRole = "tous";
    if (filtreStatut == null) filtreStatut = "tous";
    if (filtrePremium == null) filtrePremium = "tous";
    if (tri == null) tri = "id";
    if (ordre == null) ordre = "DESC";
    if (stats == null) stats = new java.util.LinkedHashMap<>();
    if (urlPagination == null) urlPagination = "/admin/utilisateurs?";
    if (adminId == null) adminId = 0;

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new java.util.Locale("fr", "FR"));
    SimpleDateFormat sdfComplet = new SimpleDateFormat("dd MMM yyyy HH:mm", new java.util.Locale("fr", "FR"));

    request.setAttribute("pageTitle", "Gestion des utilisateurs - Administration - AmijayQuiz");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-utilisateurs.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-utilisateurs.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<div class="au-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-users"></i><span>Utilisateurs</span>
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
    <div class="au-header">
        <div class="au-header-left">
            <div class="au-header-icon">
                <i class="fas fa-users"></i>
            </div>
            <div class="au-header-text">
                <h1>Gestion des utilisateurs</h1>
                <p><%= totalUtilisateurs %> utilisateur<%= totalUtilisateurs > 1 ? "s" : "" %> inscrit<%= totalUtilisateurs > 1 ? "s" : "" %></p>
            </div>
        </div>
    </div>

    <%-- ===== CARTES STATISTIQUES ===== --%>
    <div class="au-stats-grid">
        <div class="au-stat-card">
            <div class="au-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-users"></i>
            </div>
            <div class="au-stat-content">
                <span class="au-stat-nombre"><%= stats.getOrDefault("total", 0) %></span>
                <span class="au-stat-label">Total</span>
            </div>
        </div>
        <div class="au-stat-card">
            <div class="au-stat-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-shield-alt"></i>
            </div>
            <div class="au-stat-content">
                <span class="au-stat-nombre"><%= stats.getOrDefault("admins", 0) %></span>
                <span class="au-stat-label">Administrateurs</span>
            </div>
        </div>
        <div class="au-stat-card">
            <div class="au-stat-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                <i class="fas fa-user-check"></i>
            </div>
            <div class="au-stat-content">
                <span class="au-stat-nombre"><%= stats.getOrDefault("actifs", 0) %></span>
                <span class="au-stat-label">Actifs</span>
            </div>
        </div>
        <div class="au-stat-card">
            <div class="au-stat-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-user-slash"></i>
            </div>
            <div class="au-stat-content">
                <span class="au-stat-nombre"><%= stats.getOrDefault("inactifs", 0) %></span>
                <span class="au-stat-label">Inactifs</span>
            </div>
        </div>
        <div class="au-stat-card">
            <div class="au-stat-icon" style="background: linear-gradient(135deg, #ffd700, #f39c12);">
                <i class="fas fa-crown"></i>
            </div>
            <div class="au-stat-content">
                <span class="au-stat-nombre"><%= stats.getOrDefault("premium", 0) %></span>
                <span class="au-stat-label">Premium</span>
            </div>
        </div>
        <div class="au-stat-card">
            <div class="au-stat-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                <i class="fas fa-user-graduate"></i>
            </div>
            <div class="au-stat-content">
                <span class="au-stat-nombre"><%= stats.getOrDefault("joueurs", 0) %></span>
                <span class="au-stat-label">Joueurs</span>
            </div>
        </div>
    </div>

    <%-- ===== BARRE D'OUTILS : RECHERCHE + FILTRES + TRI ===== --%>
    <div class="au-toolbar">
        <form action="${pageContext.request.contextPath}/admin/utilisateurs" method="GET" class="au-toolbar-form" id="formFiltres">
            
            <%-- Recherche --%>
            <div class="au-recherche-wrapper">
                <i class="fas fa-search"></i>
                <input type="text" name="recherche" value="<%= recherche %>" 
                       placeholder="Rechercher par pseudo ou email..." 
                       class="au-recherche-input" id="rechercheInput">
                <% if (recherche != null && !recherche.isEmpty()) { %>
                    <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="au-recherche-clear" title="Effacer">
                        <i class="fas fa-times"></i>
                    </a>
                <% } %>
            </div>

            <%-- Filtre Rôle --%>
            <select name="role" id="filtreRole" class="au-select">
                <option value="tous" <%= "tous".equals(filtreRole) ? "selected" : "" %>>Tous les rôles</option>
                <option value="admin" <%= "admin".equals(filtreRole) ? "selected" : "" %>>Administrateurs</option>
                <option value="joueur" <%= "joueur".equals(filtreRole) ? "selected" : "" %>>Joueurs</option>
            </select>

            <%-- Filtre Statut --%>
            <select name="statut" id="filtreStatut" class="au-select">
                <option value="tous" <%= "tous".equals(filtreStatut) ? "selected" : "" %>>Tous les statuts</option>
                <option value="actif" <%= "actif".equals(filtreStatut) ? "selected" : "" %>>Actifs</option>
                <option value="inactif" <%= "inactif".equals(filtreStatut) ? "selected" : "" %>>Inactifs</option>
            </select>

            <%-- Filtre Premium --%>
            <select name="premium" id="filtrePremium" class="au-select">
                <option value="tous" <%= "tous".equals(filtrePremium) ? "selected" : "" %>>Tous</option>
                <option value="premium" <%= "premium".equals(filtrePremium) ? "selected" : "" %>>Premium</option>
                <option value="gratuit" <%= "gratuit".equals(filtrePremium) ? "selected" : "" %>>Gratuit</option>
            </select>

            <%-- Tri --%>
            <select name="tri" id="filtreTri" class="au-select">
                <option value="id" <%= "id".equals(tri) ? "selected" : "" %>>Tri : ID</option>
                <option value="pseudo" <%= "pseudo".equals(tri) ? "selected" : "" %>>Tri : Pseudo</option>
                <option value="email" <%= "email".equals(tri) ? "selected" : "" %>>Tri : Email</option>
                <option value="role" <%= "role".equals(tri) ? "selected" : "" %>>Tri : Rôle</option>
                <option value="date_inscription" <%= "date_inscription".equals(tri) ? "selected" : "" %>>Tri : Date inscription</option>
            </select>

            <%-- Réinitialiser --%>
            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreRole) || !"tous".equals(filtreStatut) || !"tous".equals(filtrePremium) || !"id".equals(tri)) { %>
                <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="au-btn-reset">
                    <i class="fas fa-undo"></i><span>Réinitialiser</span>
                </a>
            <% } %>
        </form>
    </div>

    <%-- ===== COMPTEUR DE RÉSULTATS ===== --%>
    <div class="au-resultats-info">
        <span><i class="fas fa-list"></i> <strong><%= totalUtilisateurs %></strong> résultat<%= totalUtilisateurs > 1 ? "s" : "" %></span>
        <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreRole) || !"tous".equals(filtreStatut) || !"tous".equals(filtrePremium)) { %>
            <span class="au-badge-filtre"><i class="fas fa-filter"></i> Filtré</span>
        <% } %>
    </div>

    <%-- ===== TABLEAU DES UTILISATEURS ===== --%>
    <div class="au-tableau-section">
        <% if (utilisateursAvecStats != null && !utilisateursAvecStats.isEmpty()) { %>
            <div class="au-table-responsive">
                <table class="au-table">
                    <thead>
                        <tr>
                            <th class="au-th-id">ID</th>
                            <th class="au-th-utilisateur">Utilisateur</th>
                            <th class="au-th-email">Email</th>
                            <th class="au-th-role">Rôle</th>
                            <th class="au-th-statut">Statut</th>
                            <th class="au-th-premium">Premium</th>
                            <th class="au-th-date">Inscription</th>
                            <th class="au-th-actions">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            for (Map<String, Object> data : utilisateursAvecStats) {
                                Utilisateur u = (Utilisateur) data.get("utilisateur");
                                boolean estPremiumUtilisateur = (Boolean) data.get("estPremium");
                                Map<String, Object> infosPremium = (Map<String, Object>) data.get("infosPremium");

                                String pseudo = u.getPseudo() != null ? u.getPseudo() : "N/A";
                                String email = u.getEmail() != null ? u.getEmail() : "Non renseigné";
                                String roleUtilisateur = u.getRole() != null ? u.getRole() : "joueur";
                                boolean actif = u.isActif();
                                String dateInscription = u.getDateInscription() != null ? sdf.format(u.getDateInscription()) : "N/A";
                                boolean estMoi = u.getId() == adminId;
                        %>
                            <tr class="au-row <%= !actif ? "au-row-inactif" : "" %>" data-user-id="<%= u.getId() %>">
                                
                                <%-- ID --%>
                                <td class="au-td-id">
                                    <span class="au-cell-id">#<%= u.getId() %></span>
                                </td>
                                
                                <%-- Utilisateur --%>
                                <td class="au-td-utilisateur">
                                    <div class="au-utilisateur-identite">
                                        <span class="au-avatar" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                                            <span><%= pseudo.substring(0, 1).toUpperCase() %></span>
                                        </span>
                                        <div>
                                            <span class="au-pseudo">
                                                <%= pseudo %>
                                                <% if (estMoi) { %>
                                                    <span class="au-badge-moi">Vous</span>
                                                <% } %>
                                            </span>
                                        </div>
                                    </div>
                                </td>
                                
                                <%-- Email --%>
                                <td class="au-td-email">
                                    <span class="au-email"><%= email %></span>
                                </td>
                                
                                <%-- Rôle --%>
                                <td class="au-td-role">
                                    <span class="au-badge-role <%= "admin".equals(roleUtilisateur) ? "au-role-admin" : ("gestionnaire".equals(roleUtilisateur) ? "au-role-gestionnaire" : "au-role-joueur") %>">
                                        <i class="fas <%= "admin".equals(roleUtilisateur) ? "fa-shield-alt" : ("gestionnaire".equals(roleUtilisateur) ? "fa-user-cog" : "fa-user") %>"></i>
                                        <%= "admin".equals(roleUtilisateur) ? "Super Admin" : ("gestionnaire".equals(roleUtilisateur) ? "Gestionnaire" : "Joueur") %>
                                    </span>
                                </td>
                                
                                <%-- Statut --%>
                                <td class="au-td-statut">
                                    <span class="au-badge-statut <%= actif ? "au-statut-actif" : "au-statut-inactif" %>">
                                        <i class="fas <%= actif ? "fa-check-circle" : "fa-ban" %>"></i>
                                        <%= actif ? "Actif" : "Inactif" %>
                                    </span>
                                </td>
                                
                                <%-- Premium --%>
                                <td class="au-td-premium">
                                    <span class="au-badge-premium <%= estPremiumUtilisateur ? "au-premium-actif" : "au-premium-inactif" %>">
                                        <i class="fas <%= estPremiumUtilisateur ? "fa-crown" : "fa-user" %>"></i>
                                        <%= estPremiumUtilisateur ? "Premium" : "Gratuit" %>
                                    </span>
                                </td>
                                
                                <%-- Date inscription --%>
                                <td class="au-td-date"><%= dateInscription %></td>
                                
                                <%-- Actions --%>
                                <td class="au-td-actions">
                                    <div class="au-actions-group">

                                        <%-- Voir détail --%>
                                        <a href="${pageContext.request.contextPath}/admin/utilisateurs/detail?id=<%= u.getId() %>" 
                                           class="au-btn-icon au-btn-voir" title="Voir le détail">
                                            <i class="fas fa-eye"></i>
                                        </a>

                                        <%-- 🔒 Si c'est un Super Admin ET que ce n'est pas moi : pas d'actions --%>
                                        <% if ("admin".equals(roleUtilisateur) && !estMoi) { %>
                                            <span class="au-btn-icon disabled" title="Super Administrateur - Actions limitées">
                                                <i class="fas fa-shield-alt"></i>
                                            </span>
                                        <% } else if (!estMoi) { %>

                                            <%-- Activer/Désactiver --%>
                                            <% if (actif) { %>
                                                <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline"
                                                      onsubmit="return confirm('Désactiver le compte de <%= pseudo.replace("'", "\\'") %> ?\n\nL\'utilisateur ne pourra plus se connecter.')">
                                                    <input type="hidden" name="action" value="desactiver">
                                                    <input type="hidden" name="id" value="<%= u.getId() %>">
                                                    <button type="submit" class="au-btn-icon au-btn-desactiver" title="Désactiver">
                                                        <i class="fas fa-ban"></i>
                                                    </button>
                                                </form>
                                            <% } else { %>
                                                <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline"
                                                      onsubmit="return confirm('Réactiver le compte de <%= pseudo.replace("'", "\\'") %> ?')">
                                                    <input type="hidden" name="action" value="activer">
                                                    <input type="hidden" name="id" value="<%= u.getId() %>">
                                                    <button type="submit" class="au-btn-icon au-btn-activer" title="Activer">
                                                        <i class="fas fa-check-circle"></i>
                                                    </button>
                                                </form>
                                            <% } %>

                                            <%-- 🔒 Promouvoir (joueur → gestionnaire) --%>
                                            <% if ("joueur".equals(roleUtilisateur)) { %>
                                                <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline"
                                                      onsubmit="return confirm('Promouvoir <%= pseudo.replace("'", "\\'") %> en Gestionnaire ?\n\nIl aura accès aux cours, quiz, thèmes et résultats.')">
                                                    <input type="hidden" name="action" value="promouvoir">
                                                    <input type="hidden" name="id" value="<%= u.getId() %>">
                                                    <button type="submit" class="au-btn-icon au-btn-promouvoir" title="Promouvoir Gestionnaire">
                                                        <i class="fas fa-arrow-up"></i>
                                                    </button>
                                                </form>
                                            <% } %>

                                            <%-- 🔒 Rétrograder (gestionnaire → joueur) --%>
                                            <% if ("gestionnaire".equals(roleUtilisateur)) { %>
                                                <form action="${pageContext.request.contextPath}/admin/utilisateurs/action" method="POST" class="au-form-inline"
                                                      onsubmit="return confirm('Rétrograder <%= pseudo.replace("'", "\\'") %> en Joueur ?\n\nIl perdra l\'accès à l\'administration.')">
                                                    <input type="hidden" name="action" value="retrograder">
                                                    <input type="hidden" name="id" value="<%= u.getId() %>">
                                                    <button type="submit" class="au-btn-icon au-btn-retrograder" title="Rétrograder Joueur">
                                                        <i class="fas fa-arrow-down"></i>
                                                    </button>
                                                </form>
                                            <% } %>

                                        <% } else { %>
                                            <%-- C'est moi : pas d'actions --%>
                                            <span class="au-btn-icon disabled" title="Vous ne pouvez pas agir sur votre propre compte">
                                                <i class="fas fa-lock"></i>
                                            </span>
                                        <% } %>
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
                <div class="au-pagination">
                    <% if (pageActuelle > 1) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle - 1 %>" class="au-page-link" title="Page précédente">
                            <i class="fas fa-chevron-left"></i>
                        </a>
                    <% } else { %>
                        <span class="au-page-link au-page-disabled"><i class="fas fa-chevron-left"></i></span>
                    <% } %>

                    <%
                        int debutPage = Math.max(1, pageActuelle - 2);
                        int finPage = Math.min(totalPages, pageActuelle + 2);
                        if (debutPage > 1) {
                    %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=1" class="au-page-link">1</a>
                        <% if (debutPage > 2) { %><span class="au-page-ellipsis">…</span><% } %>
                    <% } %>

                    <% for (int i = debutPage; i <= finPage; i++) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= i %>" 
                           class="au-page-link <%= i == pageActuelle ? "au-page-active" : "" %>"><%= i %></a>
                    <% } %>

                    <% if (finPage < totalPages) { %>
                        <% if (finPage < totalPages - 1) { %><span class="au-page-ellipsis">…</span><% } %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= totalPages %>" class="au-page-link"><%= totalPages %></a>
                    <% } %>

                    <% if (pageActuelle < totalPages) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle + 1 %>" class="au-page-link" title="Page suivante">
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    <% } else { %>
                        <span class="au-page-link au-page-disabled"><i class="fas fa-chevron-right"></i></span>
                    <% } %>

                    <span class="au-page-info">Page <strong><%= pageActuelle %></strong>/<strong><%= totalPages %></strong> · <%= totalUtilisateurs %> résultats</span>
                </div>
            <% } %>

        <% } else { %>
            <div class="au-empty">
                <div class="au-empty-icon"><i class="fas fa-users"></i></div>
                <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreRole) || !"tous".equals(filtreStatut) || !"tous".equals(filtrePremium)) { %>
                    <h3>Aucun utilisateur trouvé</h3>
                    <p>Aucun utilisateur ne correspond à vos critères. Modifiez vos filtres.</p>
                    <a href="${pageContext.request.contextPath}/admin/utilisateurs" class="au-btn-reset-empty">
                        <i class="fas fa-undo"></i> Réinitialiser les filtres
                    </a>
                <% } else { %>
                    <h3>Aucun utilisateur inscrit</h3>
                    <p>Les utilisateurs apparaîtront ici une fois inscrits sur la plateforme.</p>
                <% } %>
            </div>
        <% } %>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="au-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="au-btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard
        </a>
    </div>

</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        var form = document.getElementById('formFiltres');
        if (!form) return;

        ['filtreRole', 'filtreStatut', 'filtrePremium', 'filtreTri'].forEach(function(id) {
            var el = document.getElementById(id);
            if (el) el.addEventListener('change', function() { form.submit(); });
        });

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