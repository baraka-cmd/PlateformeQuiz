<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.learnpath.models.Cours" %>

<%-- ============================================================
     FICHIER : views/admin/admin-certificats.jsp
     RÔLE    : Page d'administration des certificats
              - Tableau paginé avec filtres et recherche
              - Actions : Révoquer / Restaurer
              - Badges de statut (valide/révoqué)
              - Mentions colorées
     ============================================================ --%>

<%
    List<Map<String, Object>> certificats = (List<Map<String, Object>>) request.getAttribute("certificats");
    Integer total = (Integer) request.getAttribute("total");
    Integer pageActuelle = (Integer) request.getAttribute("page");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    String recherche = (String) request.getAttribute("recherche");
    String filtreCours = (String) request.getAttribute("filtreCours");
    String filtreStatut = (String) request.getAttribute("filtreStatut");
    List<Cours> coursList = (List<Cours>) request.getAttribute("coursList");
    String urlPagination = (String) request.getAttribute("urlPagination");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (certificats == null) certificats = new java.util.ArrayList<>();
    if (total == null) total = 0;
    if (pageActuelle == null) pageActuelle = 1;
    if (totalPages == null) totalPages = 1;
    if (recherche == null) recherche = "";
    if (filtreCours == null) filtreCours = "tous";
    if (filtreStatut == null) filtreStatut = "tous";
    if (coursList == null) coursList = new java.util.ArrayList<>();
    if (urlPagination == null) urlPagination = "/admin/certificats?";

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new java.util.Locale("fr", "FR"));

    request.setAttribute("pageTitle", "Certificats - Administration - LearnPath");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-certificats.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-certificats.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<div class="acert-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-certificate"></i><span>Certificats</span>
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

    <%-- ===== EN-TÊTE ===== --%>
    <div class="acert-header">
        <div class="acert-header-left">
            <div class="acert-header-icon">
                <i class="fas fa-certificate"></i>
            </div>
            <div class="acert-header-text">
                <h1>Gestion des certificats</h1>
                <p><%= total %> certificat<%= total > 1 ? "s" : "" %> au total</p>
            </div>
        </div>
    </div>

    <%-- ===== BARRE DE FILTRES ===== --%>
    <div class="acert-toolbar">
        <form action="${pageContext.request.contextPath}/admin/certificats" method="GET" class="acert-toolbar-form" id="formFiltres">
            
            <%-- Recherche --%>
            <div class="acert-recherche-wrapper">
                <i class="fas fa-search"></i>
                <input type="text" name="recherche" value="<%= recherche %>" 
                       placeholder="Rechercher par code ou pseudo..." 
                       class="acert-recherche-input" id="rechercheInput">
                <% if (recherche != null && !recherche.isEmpty()) { %>
                    <a href="${pageContext.request.contextPath}/admin/certificats" class="acert-recherche-clear" title="Effacer">
                        <i class="fas fa-times"></i>
                    </a>
                <% } %>
            </div>

            <%-- Filtre Cours --%>
            <select name="cours" id="filtreCours" class="acert-select">
                <option value="tous" <%= "tous".equals(filtreCours) ? "selected" : "" %>>Tous les cours</option>
                <% for (Cours c : coursList) { %>
                    <option value="<%= c.getId() %>" <%= String.valueOf(c.getId()).equals(filtreCours) ? "selected" : "" %>>
                        <%= c.getTitre() %>
                    </option>
                <% } %>
            </select>

            <%-- Filtre Statut --%>
            <select name="statut" id="filtreStatut" class="acert-select">
                <option value="tous" <%= "tous".equals(filtreStatut) ? "selected" : "" %>>Tous les statuts</option>
                <option value="valides" <%= "valides".equals(filtreStatut) ? "selected" : "" %>>Valides</option>
                <option value="revoques" <%= "revoques".equals(filtreStatut) ? "selected" : "" %>>Révoqués</option>
            </select>

            <%-- Réinitialiser --%>
            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreCours) || !"tous".equals(filtreStatut)) { %>
                <a href="${pageContext.request.contextPath}/admin/certificats" class="acert-btn-reset">
                    <i class="fas fa-undo"></i><span>Réinitialiser</span>
                </a>
            <% } %>
        </form>
    </div>

    <%-- ===== COMPTEUR ===== --%>
    <div class="acert-resultats-info">
        <span><i class="fas fa-list"></i> <strong><%= total %></strong> résultat<%= total > 1 ? "s" : "" %></span>
        <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreCours) || !"tous".equals(filtreStatut)) { %>
            <span class="acert-badge-filtre"><i class="fas fa-filter"></i> Filtré</span>
        <% } %>
    </div>

    <%-- ===== TABLEAU ===== --%>
    <div class="acert-tableau-section">
        <% if (certificats != null && !certificats.isEmpty()) { %>
            <div class="acert-table-responsive">
                <table class="acert-table">
                    <thead>
                        <tr>
                            <th class="acert-th-id">ID</th>
                            <th class="acert-th-code">Code</th>
                            <th class="acert-th-apprenant">Apprenant</th>
                            <th class="acert-th-cours">Cours</th>
                            <th class="acert-th-score">Score</th>
                            <th class="acert-th-mention">Mention</th>
                            <th class="acert-th-date">Délivré le</th>
                            <th class="acert-th-statut">Statut</th>
                            <th class="acert-th-actions">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            String[] mentions = {"Passable", "Bien", "Très Bien", "Excellent"};
                            String[] mentionColors = {"#f39c12", "#2980b9", "#27ae60", "#ffd700"};
                            String[] mentionIcons = {"fas fa-check-circle", "fas fa-star", "fas fa-trophy", "fas fa-crown"};

                            for (Map<String, Object> row : certificats) {
                                int id = (Integer) row.get("id");
                                String code = (String) row.get("codeVerification");
                                String pseudo = (String) row.get("pseudo");
                                String titreCours = (String) row.get("titreCours");
                                double scoreFinal = (Double) row.get("scoreFinal");
                                String mention = (String) row.get("mention");
                                java.sql.Timestamp dateObtention = (java.sql.Timestamp) row.get("dateObtention");
                                boolean estValide = (Boolean) row.get("estValide");
                                String dateStr = dateObtention != null ? sdf.format(dateObtention) : "N/A";

                                String mentionColor = "#f39c12";
                                String mentionIcon = "fas fa-check-circle";
                                for (int i = 0; i < mentions.length; i++) {
                                    if (mentions[i].equals(mention)) {
                                        mentionColor = mentionColors[i];
                                        mentionIcon = mentionIcons[i];
                                        break;
                                    }
                                }

                                if (pseudo == null) pseudo = "N/A";
                                if (titreCours == null) titreCours = "N/A";
                                if (titreCours.length() > 40) titreCours = titreCours.substring(0, 40) + "...";
                        %>
                            <tr>
                                <td class="acert-td-id"><span class="acert-cell-id">#<%= id %></span></td>
                                <td class="acert-td-code">
                                    <code class="acert-code"><%= code %></code>
                                </td>
                                <td class="acert-td-apprenant">
                                    <div class="acert-utilisateur">
                                        <span class="acert-utilisateur-avatar"><i class="fas fa-user"></i></span>
                                        <span><%= pseudo %></span>
                                    </div>
                                </td>
                                <td class="acert-td-cours"><%= titreCours %></td>
                                <td class="acert-td-score"><%= String.format("%.0f%%", scoreFinal) %></td>
                                <td class="acert-td-mention">
                                    <span style="color: <%= mentionColor %>; font-weight: 600;">
                                        <i class="<%= mentionIcon %>"></i> <%= mention %>
                                    </span>
                                </td>
                                <td class="acert-td-date"><%= dateStr %></td>
                                <td class="acert-td-statut">
                                    <span class="acert-badge-statut <%= estValide ? "acert-badge-valide" : "acert-badge-revoque" %>">
                                        <i class="fas <%= estValide ? "fa-check-circle" : "fa-ban" %>"></i>
                                        <%= estValide ? "Valide" : "Révoqué" %>
                                    </span>
                                </td>
                                <td class="acert-td-actions">
                                    <% if (estValide) { %>
                                        <form action="${pageContext.request.contextPath}/admin/certificats" method="POST" class="acert-form-inline"
                                              onsubmit="return confirm('Révoquer le certificat #<%= id %> ?\n\nCette action empêchera la vérification publique du certificat.')">
                                            <input type="hidden" name="action" value="revoquer">
                                            <input type="hidden" name="id" value="<%= id %>">
                                            <button type="submit" class="acert-btn-icon acert-btn-revoquer" title="Révoquer">
                                                <i class="fas fa-ban"></i>
                                            </button>
                                        </form>
                                    <% } else { %>
                                        <form action="${pageContext.request.contextPath}/admin/certificats" method="POST" class="acert-form-inline"
                                              onsubmit="return confirm('Restaurer le certificat #<%= id %> ?')">
                                            <input type="hidden" name="action" value="restaurer">
                                            <input type="hidden" name="id" value="<%= id %>">
                                            <button type="submit" class="acert-btn-icon acert-btn-restaurer" title="Restaurer">
                                                <i class="fas fa-undo"></i>
                                            </button>
                                        </form>
                                    <% } %>
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
                <div class="acert-pagination">
                    <% if (pageActuelle > 1) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle - 1 %>" class="acert-page-link" title="Page précédente">
                            <i class="fas fa-chevron-left"></i>
                        </a>
                    <% } else { %>
                        <span class="acert-page-link acert-page-disabled"><i class="fas fa-chevron-left"></i></span>
                    <% } %>

                    <%
                        int debutPage = Math.max(1, pageActuelle - 2);
                        int finPage = Math.min(totalPages, pageActuelle + 2);
                        if (debutPage > 1) {
                    %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=1" class="acert-page-link">1</a>
                        <% if (debutPage > 2) { %><span class="acert-page-ellipsis">…</span><% } %>
                    <% } %>

                    <% for (int i = debutPage; i <= finPage; i++) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= i %>" 
                           class="acert-page-link <%= i == pageActuelle ? "acert-page-active" : "" %>"><%= i %></a>
                    <% } %>

                    <% if (finPage < totalPages) { %>
                        <% if (finPage < totalPages - 1) { %><span class="acert-page-ellipsis">…</span><% } %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= totalPages %>" class="acert-page-link"><%= totalPages %></a>
                    <% } %>

                    <% if (pageActuelle < totalPages) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle + 1 %>" class="acert-page-link" title="Page suivante">
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    <% } else { %>
                        <span class="acert-page-link acert-page-disabled"><i class="fas fa-chevron-right"></i></span>
                    <% } %>

                    <span class="acert-page-info">Page <strong><%= pageActuelle %></strong>/<strong><%= totalPages %></strong> · <%= total %> résultats</span>
                </div>
            <% } %>

        <% } else { %>
            <div class="acert-empty">
                <div class="acert-empty-icon"><i class="fas fa-certificate"></i></div>
                <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreCours) || !"tous".equals(filtreStatut)) { %>
                    <h3>Aucun certificat trouvé</h3>
                    <p>Aucun certificat ne correspond à vos critères.</p>
                    <a href="${pageContext.request.contextPath}/admin/certificats" class="acert-btn-reset-empty">
                        <i class="fas fa-undo"></i> Réinitialiser les filtres
                    </a>
                <% } else { %>
                    <h3>Aucun certificat délivré</h3>
                    <p>Les certificats apparaîtront ici une fois que des apprenants auront réussi des examens finaux.</p>
                <% } %>
            </div>
        <% } %>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="acert-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="acert-btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard
        </a>
    </div>

</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        var form = document.getElementById('formFiltres');
        if (!form) return;

        ['filtreCours', 'filtreStatut'].forEach(function(id) {
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