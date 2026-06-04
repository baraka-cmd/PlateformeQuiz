<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Cours" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-chapitre-creer.jsp
     RÔLE    : Formulaire de création d'un chapitre
              - Sélection du module (1-15)
              - Titre, description, objectifs
              - Ordre auto-suggéré
              - Validation et prévisualisation
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    Integer coursId = (Integer) request.getAttribute("coursId");
    Integer modulePreselectionne = (Integer) request.getAttribute("modulePreselectionne");
    Integer nombreModules = (Integer) request.getAttribute("nombreModules");
    Integer ordreSuggere = (Integer) request.getAttribute("ordreSuggere");
    String messageErreur = (String) request.getAttribute("messageErreur");
    Boolean modeModification = (Boolean) request.getAttribute("modeModification");

    if (cours == null || coursId == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (modulePreselectionne == null) modulePreselectionne = 1;
    if (nombreModules == null) nombreModules = 15;
    if (ordreSuggere == null) ordreSuggere = 1;
    if (modeModification == null) modeModification = false;

    request.setAttribute("pageTitle", "Créer un chapitre - " + cours.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-creer.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="acc-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= coursId %>" class="breadcrumb-link">
            <i class="fas fa-bookmark"></i><span>Chapitres</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-plus-circle"></i><span>Créer un chapitre</span>
        </span>
    </nav>

    <%-- ===== MESSAGE D'ERREUR ===== --%>
    <% if (messageErreur != null && !messageErreur.isEmpty()) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i><span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== LAYOUT 2 COLONNES ===== --%>
    <div class="acc-layout">

        <div class="acc-col-form">

            <form action="${pageContext.request.contextPath}/admin/cours/chapitres/creer" method="POST" id="formChapitre" novalidate>
                <input type="hidden" name="coursId" value="<%= coursId %>">

                <%-- ===== CARTE : INFORMATIONS DU CHAPITRE ===== --%>
                <div class="acc-card">
                    <div class="acc-card-header">
                        <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                            <i class="fas fa-bookmark"></i>
                        </div>
                        <div class="acc-card-header-text">
                            <h2>Nouveau chapitre</h2>
                            <p>Cours : <%= cours.getTitre() %></p>
                        </div>
                    </div>
                    <div class="acc-card-body">

                        <%-- Module + Ordre côte à côte --%>
                        <div class="acc-form-row acc-form-row-2col">
                            <div class="acc-form-group">
                                <label for="module" class="acc-label">
                                    <i class="fas fa-cube"></i><span>Module</span><span class="acc-required">*</span>
                                </label>
                                <select id="module" name="module" class="acc-select" required>
                                    <% for (int i = 1; i <= nombreModules; i++) { %>
                                        <option value="<%= i %>" <%= i == modulePreselectionne ? "selected" : "" %>>Module <%= i %></option>
                                    <% } %>
                                </select>
                                <span class="acc-hint"><i class="fas fa-info-circle"></i> Le module auquel appartient ce chapitre.</span>
                            </div>

                            <div class="acc-form-group">
                                <label for="ordre" class="acc-label">
                                    <i class="fas fa-sort-numeric-down"></i><span>Ordre dans le module</span>
                                </label>
                                <input type="number" id="ordre" name="ordre" class="acc-input"
                                       value="<%= ordreSuggere %>" min="1" max="100" required>
                                <span class="acc-hint"><i class="fas fa-info-circle"></i> Position du chapitre dans le module (suggéré : <%= ordreSuggere %>).</span>
                            </div>
                        </div>

                        <%-- Titre --%>
                        <div class="acc-form-group">
                            <label for="titre" class="acc-label">
                                <i class="fas fa-heading"></i><span>Titre du chapitre</span><span class="acc-required">*</span>
                            </label>
                            <input type="text" id="titre" name="titre" class="acc-input"
                                   placeholder="Ex: Introduction aux bases de données" maxlength="200" required autofocus>
                            <span class="acc-error" id="titreError"></span>
                        </div>

                        <%-- Description --%>
                        <div class="acc-form-group">
                            <label for="description" class="acc-label">
                                <i class="fas fa-align-left"></i><span>Description</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="description" name="description" class="acc-textarea" rows="2"
                                      maxlength="300" placeholder="Résumé du contenu du chapitre..."></textarea>
                        </div>

                        <%-- Objectifs --%>
                        <div class="acc-form-group">
                            <label for="objectifs" class="acc-label">
                                <i class="fas fa-bullseye"></i><span>Objectifs pédagogiques</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="objectifs" name="objectifs" class="acc-textarea" rows="2"
                                      maxlength="300" placeholder="Ce que l'apprenant va apprendre dans ce chapitre..."></textarea>
                        </div>

                    </div>
                </div>

                <%-- ===== BOUTONS ===== --%>
                <div class="acc-form-actions">
                    <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= coursId %>" class="acc-btn acc-btn-secondary">
                        <i class="fas fa-arrow-left"></i><span>Annuler</span>
                    </a>
                    <button type="submit" class="acc-btn acc-btn-primary" id="btnCreerChapitre">
                        <i class="fas fa-save"></i><span>Créer le chapitre</span>
                    </button>
                </div>

            </form>
        </div>

        <%-- ===== COLONNE DROITE : APERÇU + GUIDE ===== --%>
        <div class="acc-col-sidebar">

            <div class="acc-card">
                <div class="acc-card-header acc-card-header-sm">
                    <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                        <i class="fas fa-eye"></i>
                    </div>
                    <div class="acc-card-header-text"><h2>Aperçu</h2></div>
                </div>
                <div class="acc-card-body">
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-cube" style="color: #667eea; margin-right: 6px;"></i>Module</span>
                            <span style="font-weight: 700; font-size: 14px; color: #1a1a2e;" id="previewModule"><%= modulePreselectionne %></span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-sort-numeric-down" style="color: #667eea; margin-right: 6px;"></i>Ordre</span>
                            <span style="font-weight: 700; font-size: 14px; color: #1a1a2e;" id="previewOrdre"><%= ordreSuggere %></span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-heading" style="color: #667eea; margin-right: 6px;"></i>Titre</span>
                            <span style="font-weight: 600; font-size: 12px; color: #1a1a2e; text-align: right;" id="previewTitre">—</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="acc-card acc-card-guide">
                <div class="acc-card-header acc-card-header-sm">
                    <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #2980b9, #3498db);">
                        <i class="fas fa-lightbulb"></i>
                    </div>
                    <div class="acc-card-header-text"><h2>Bonnes pratiques</h2></div>
                </div>
                <div class="acc-card-body">
                    <ul class="acc-guide-list">
                        <li><i class="fas fa-check-circle"></i><span><strong>Module :</strong> Choisissez le module auquel appartient ce chapitre.</span></li>
                        <li><i class="fas fa-check-circle"></i><span><strong>Ordre :</strong> Détermine la position dans le module.</span></li>
                        <li><i class="fas fa-check-circle"></i><span><strong>Titre :</strong> Clair et descriptif (min. 2 caractères).</span></li>
                        <li><i class="fas fa-check-circle"></i><span><strong>Leçons :</strong> À ajouter après la création du chapitre.</span></li>
                    </ul>
                </div>
            </div>

        </div>
    </div>
</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    document.getElementById('module').addEventListener('change', function() {
        document.getElementById('previewModule').textContent = this.value;
    });
    document.getElementById('ordre').addEventListener('input', function() {
        document.getElementById('previewOrdre').textContent = this.value || '—';
    });
    document.getElementById('titre').addEventListener('input', function() {
        document.getElementById('previewTitre').textContent = this.value.trim() || '—';
    });

    document.getElementById('formChapitre').addEventListener('submit', function(e) {
        var titre = document.getElementById('titre');
        var hasError = false;
        titre.classList.remove('acc-input-error');
        document.getElementById('titreError').textContent = '';

        if (!titre.value.trim()) {
            titre.classList.add('acc-input-error');
            document.getElementById('titreError').textContent = 'Le titre est obligatoire.';
            hasError = true;
        } else if (titre.value.trim().length < 2) {
            titre.classList.add('acc-input-error');
            document.getElementById('titreError').textContent = 'Minimum 2 caractères.';
            hasError = true;
        }

        if (hasError) {
            e.preventDefault();
        } else {
            var btn = document.getElementById('btnCreerChapitre');
            if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Création...</span>'; }
        }
    });
</script>

<%@ include file="../footer-connecte.jsp" %>