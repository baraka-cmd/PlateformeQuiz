<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.learnpath.models.Theme" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-creer.jsp
     RÔLE    : Formulaire de création d'un cours
              - Sélection du thème (depuis la table themes)
              - Sélection du niveau
              - Champs : titre, description, objectifs, prérequis
              - Image/icône, durée estimée
              - Aperçu en temps réel
              - Validation côté client et serveur
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    List<Theme> themes = (List<Theme>) request.getAttribute("themes");
    String[] niveaux = (String[]) request.getAttribute("niveaux");
    String messageErreur = (String) request.getAttribute("messageErreur");
    
    // Valeurs sauvegardées en cas d'erreur
    String oldTitre = (String) request.getAttribute("oldTitre");
    String oldTheme = (String) request.getAttribute("oldTheme");
    String oldNiveau = (String) request.getAttribute("oldNiveau");
    String oldDescription = (String) request.getAttribute("oldDescription");
    String oldObjectifs = (String) request.getAttribute("oldObjectifs");
    String oldPrerequis = (String) request.getAttribute("oldPrerequis");
    String oldImage = (String) request.getAttribute("oldImage");
    String oldDuree = (String) request.getAttribute("oldDuree");
    
    if (themes == null) themes = new java.util.ArrayList<>();
    if (niveaux == null) niveaux = new String[]{"elementaire", "moyen", "avance"};
    if (oldTitre == null) oldTitre = "";
    if (oldTheme == null) oldTheme = "";
    if (oldNiveau == null) oldNiveau = "elementaire";
    if (oldDescription == null) oldDescription = "";
    if (oldObjectifs == null) oldObjectifs = "";
    if (oldPrerequis == null) oldPrerequis = "";
    if (oldImage == null) oldImage = "";
    if (oldDuree == null) oldDuree = "";
    
    request.setAttribute("pageTitle", "Créer un cours - Administration - LearnPath");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-creer.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-cours-creer.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
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
        <span class="breadcrumb-current">
            <i class="fas fa-plus-circle"></i><span>Créer un cours</span>
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

        <%-- ===== COLONNE GAUCHE : FORMULAIRE ===== --%>
        <div class="acc-col-form">

            <form action="${pageContext.request.contextPath}/admin/cours/creer" method="POST" id="formCoursCreer" novalidate>

                <%-- ===== CARTE 1 : INFORMATIONS GÉNÉRALES ===== --%>
                <div class="acc-card">
                    <div class="acc-card-header">
                        <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                            <i class="fas fa-info-circle"></i>
                        </div>
                        <div class="acc-card-header-text">
                            <h2>Informations générales</h2>
                            <p>Définissez le thème, le niveau et le titre du cours</p>
                        </div>
                    </div>
                    <div class="acc-card-body">

                        <%-- Thème + Niveau côte à côte --%>
                        <div class="acc-form-row acc-form-row-2col">
                            <div class="acc-form-group">
                                <label for="theme" class="acc-label">
                                    <i class="fas fa-tag"></i><span>Thème</span><span class="acc-required">*</span>
                                </label>
                                <select id="theme" name="theme" class="acc-select" required>
                                    <option value="">-- Choisir un thème --</option>
                                    <% for (Theme t : themes) { %>
                                        <option value="<%= t.getNom() %>" <%= t.getNom().equals(oldTheme) ? "selected" : "" %>>
                                            <%= t.getNom() %>
                                        </option>
                                    <% } %>
                                </select>
                                <span class="acc-error" id="themeError"></span>
                            </div>

                            <div class="acc-form-group">
                                <label for="niveau" class="acc-label">
                                    <i class="fas fa-signal"></i><span>Niveau</span><span class="acc-required">*</span>
                                </label>
                                <select id="niveau" name="niveau" class="acc-select" required>
                                    <% for (String niveau : niveaux) { 
                                        String niveauFormate = "";
                                        if ("elementaire".equals(niveau)) niveauFormate = "Élémentaire";
                                        else if ("moyen".equals(niveau)) niveauFormate = "Moyen";
                                        else if ("avance".equals(niveau)) niveauFormate = "Avancé";
                                    %>
                                        <option value="<%= niveau %>" <%= niveau.equals(oldNiveau) ? "selected" : "" %>>
                                            <%= niveauFormate %>
                                        </option>
                                    <% } %>
                                </select>
                            </div>
                        </div>

                        <%-- Titre --%>
                        <div class="acc-form-group">
                            <label for="titre" class="acc-label">
                                <i class="fas fa-heading"></i><span>Titre du cours</span><span class="acc-required">*</span>
                            </label>
                            <div class="acc-input-wrapper">
                                <input type="text" id="titre" name="titre" class="acc-input"
                                       value="<%= oldTitre %>" placeholder="Ex: Introduction au Développement Web"
                                       maxlength="200" required autofocus autocomplete="off">
                                <span class="acc-input-counter" id="titreCounter">
                                    <span id="titreCount"><%= oldTitre.length() %></span>/200
                                </span>
                            </div>
                            <span class="acc-hint">
                                <i class="fas fa-info-circle"></i> Minimum 5 caractères. Titre unique pour ce thème et niveau.
                            </span>
                            <span class="acc-error" id="titreError"></span>
                        </div>

                        <%-- Description --%>
                        <div class="acc-form-group">
                            <label for="description" class="acc-label">
                                <i class="fas fa-align-left"></i><span>Description</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="description" name="description" class="acc-textarea" rows="3"
                                      maxlength="500" placeholder="Décrivez brièvement le contenu du cours..."><%= oldDescription %></textarea>
                            <div class="acc-textarea-footer">
                                <span class="acc-hint"><i class="fas fa-lightbulb"></i> Résumez en quelques phrases ce que les apprenants vont apprendre.</span>
                                <span class="acc-input-counter" id="descCounter"><span id="descCount"><%= oldDescription.length() %></span>/500</span>
                            </div>
                        </div>

                    </div>
                </div>

                <%-- ===== CARTE 2 : CONTENU PÉDAGOGIQUE ===== --%>
                <div class="acc-card">
                    <div class="acc-card-header">
                        <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                            <i class="fas fa-bullseye"></i>
                        </div>
                        <div class="acc-card-header-text">
                            <h2>Contenu pédagogique</h2>
                            <p>Définissez les objectifs et prérequis du cours</p>
                        </div>
                    </div>
                    <div class="acc-card-body">

                        <%-- Objectifs --%>
                        <div class="acc-form-group">
                            <label for="objectifs" class="acc-label">
                                <i class="fas fa-flag-checkered"></i><span>Objectifs pédagogiques</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="objectifs" name="objectifs" class="acc-textarea" rows="3"
                                      maxlength="500" placeholder="Ce que l'apprenant saura faire à la fin du cours..."><%= oldObjectifs %></textarea>
                        </div>

                        <%-- Prérequis --%>
                        <div class="acc-form-group">
                            <label for="prerequis" class="acc-label">
                                <i class="fas fa-clipboard-list"></i><span>Prérequis</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="prerequis" name="prerequis" class="acc-textarea" rows="2"
                                      maxlength="300" placeholder="Connaissances nécessaires avant de commencer..."><%= oldPrerequis %></textarea>
                        </div>

                        <%-- Image + Durée côte à côte --%>
                        <div class="acc-form-row acc-form-row-2col">
                            <div class="acc-form-group">
                                <label for="image" class="acc-label">
                                    <i class="fas fa-image"></i><span>Image/icône</span><span class="acc-optional">(optionnel)</span>
                                </label>
                                <input type="text" id="image" name="image" class="acc-input"
                                       value="<%= oldImage %>" placeholder="URL de l'image (optionnelle)">
                                <span class="acc-hint"><i class="fas fa-info-circle"></i> URL externe vers une image représentative.</span>
                            </div>

                            <div class="acc-form-group">
                                <label for="dureeEstimee" class="acc-label">
                                    <i class="fas fa-clock"></i><span>Durée estimée</span><span class="acc-optional">(optionnel)</span>
                                </label>
                                <input type="text" id="dureeEstimee" name="dureeEstimee" class="acc-input"
                                       value="<%= oldDuree %>" placeholder="Ex: 40 heures">
                            </div>
                        </div>

                    </div>
                </div>

                <%-- ===== BOUTONS D'ACTION ===== --%>
                <div class="acc-form-actions">
                    <a href="${pageContext.request.contextPath}/admin/cours" class="acc-btn acc-btn-secondary">
                        <i class="fas fa-arrow-left"></i><span>Annuler</span>
                    </a>
                    <button type="submit" class="acc-btn acc-btn-primary" id="btnCreerCours">
                        <i class="fas fa-save"></i><span>Créer le cours</span>
                    </button>
                </div>

            </form>
        </div>

        <%-- ===== COLONNE DROITE : APERÇU + GUIDE ===== --%>
        <div class="acc-col-sidebar">

            <%-- ===== APERÇU EN TEMPS RÉEL ===== --%>
            <div class="acc-card acc-card-preview">
                <div class="acc-card-header acc-card-header-sm">
                    <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                        <i class="fas fa-eye"></i>
                    </div>
                    <div class="acc-card-header-text"><h2>Aperçu du cours</h2></div>
                </div>
                <div class="acc-card-body acc-preview-body">
                    <div class="acc-preview-card" id="previewCard">
                        <div class="acc-preview-icon" id="previewIcon" style="background: linear-gradient(135deg, #667eea, #667eeadd);">
                            <i class="fas fa-book"></i>
                        </div>
                        <h3 class="acc-preview-titre" id="previewTitre">Titre du cours</h3>
                        <span class="acc-preview-theme" id="previewTheme">Thème • Niveau</span>
                        <p class="acc-preview-desc" id="previewDesc">Description du cours apparaîtra ici</p>
                        <div class="acc-preview-meta">
                            <span id="previewDuree"><i class="fas fa-clock"></i> Durée estimée</span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ===== GUIDE ===== --%>
            <div class="acc-card acc-card-guide">
                <div class="acc-card-header acc-card-header-sm">
                    <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #2980b9, #3498db);">
                        <i class="fas fa-lightbulb"></i>
                    </div>
                    <div class="acc-card-header-text"><h2>Bonnes pratiques</h2></div>
                </div>
                <div class="acc-card-body">
                    <ul class="acc-guide-list">
                        <li><i class="fas fa-check-circle"></i><span><strong>Statut initial :</strong> Le cours est créé en brouillon. Publiez-le quand il est prêt.</span></li>
                        <li><i class="fas fa-check-circle"></i><span><strong>15 modules :</strong> Chaque cours a 15 modules. Configurez-les après la création.</span></li>
                        <li><i class="fas fa-check-circle"></i><span><strong>Titre unique :</strong> Chaque cours doit avoir un titre unique par thème et niveau.</span></li>
                    </ul>
                </div>
            </div>

            <%-- ===== HIÉRARCHIE ===== --%>
            <div class="acc-card acc-card-hierarchy">
                <div class="acc-card-header acc-card-header-sm">
                    <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                        <i class="fas fa-sitemap"></i>
                    </div>
                    <div class="acc-card-header-text"><h2>Structure</h2></div>
                </div>
                <div class="acc-card-body">
                    <div class="acc-hierarchy-flow">
                        <div class="acc-hierarchy-item"><i class="fas fa-book"></i><span>Cours</span></div>
                        <i class="fas fa-arrow-down acc-hierarchy-arrow"></i>
                        <div class="acc-hierarchy-item"><i class="fas fa-cube"></i><span>15 Modules</span></div>
                        <i class="fas fa-arrow-down acc-hierarchy-arrow"></i>
                        <div class="acc-hierarchy-item"><i class="fas fa-bookmark"></i><span>Chapitres</span></div>
                        <i class="fas fa-arrow-down acc-hierarchy-arrow"></i>
                        <div class="acc-hierarchy-item"><i class="fas fa-file-alt"></i><span>Leçons</span></div>
                    </div>
                </div>
            </div>

        </div>
    </div>
</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    // Prévisualisation en temps réel
    var titreInput = document.getElementById('titre');
    var themeSelect = document.getElementById('theme');
    var niveauSelect = document.getElementById('niveau');
    var descInput = document.getElementById('description');
    var dureeInput = document.getElementById('dureeEstimee');

    if (titreInput) titreInput.addEventListener('input', function() {
        document.getElementById('previewTitre').textContent = this.value.trim() || 'Titre du cours';
    });

    if (themeSelect) themeSelect.addEventListener('change', updatePreviewMeta);
    if (niveauSelect) niveauSelect.addEventListener('change', updatePreviewMeta);

    function updatePreviewMeta() {
        var theme = themeSelect.options[themeSelect.selectedIndex]?.text || 'Thème';
        var niveau = niveauSelect.options[niveauSelect.selectedIndex]?.text || 'Niveau';
        document.getElementById('previewTheme').textContent = theme + ' • ' + niveau;
    }

    if (descInput) descInput.addEventListener('input', function() {
        var desc = this.value.trim() || 'Description du cours apparaîtra ici';
        if (desc.length > 100) desc = desc.substring(0, 100) + '...';
        document.getElementById('previewDesc').textContent = desc;
    });

    if (dureeInput) dureeInput.addEventListener('input', function() {
        document.getElementById('previewDuree').innerHTML = '<i class="fas fa-clock"></i> ' + (this.value.trim() || 'Durée estimée');
    });

    // Compteurs
    ['titre', 'description'].forEach(function(id) {
        var input = document.getElementById(id);
        var counter = document.getElementById(id + 'Count');
        if (input && counter) {
            input.addEventListener('input', function() { counter.textContent = this.value.length; });
        }
    });

    // Validation
    document.getElementById('formCoursCreer').addEventListener('submit', function(e) {
        var hasError = false;
        var theme = document.getElementById('theme');
        var niveau = document.getElementById('niveau');
        var titre = document.getElementById('titre');

        [theme, niveau, titre].forEach(function(el) { el.classList.remove('acc-input-error'); });
        ['themeError', 'titreError'].forEach(function(id) {
            var err = document.getElementById(id); if (err) err.textContent = '';
        });

        if (!theme.value) {
            theme.classList.add('acc-input-error');
            document.getElementById('themeError').textContent = 'Veuillez sélectionner un thème.';
            hasError = true;
        }
        if (!niveau.value) {
            niveau.classList.add('acc-input-error');
            hasError = true;
        }
        if (!titre.value.trim()) {
            titre.classList.add('acc-input-error');
            document.getElementById('titreError').textContent = 'Le titre est obligatoire.';
            hasError = true;
        } else if (titre.value.trim().length < 5) {
            titre.classList.add('acc-input-error');
            document.getElementById('titreError').textContent = 'Minimum 5 caractères.';
            hasError = true;
        }

        if (hasError) e.preventDefault();
    });

    document.addEventListener('DOMContentLoaded', function() {
        updatePreviewMeta();
        if (titreInput) titreInput.dispatchEvent(new Event('input'));
        if (descInput) descInput.dispatchEvent(new Event('input'));
        if (dureeInput) dureeInput.dispatchEvent(new Event('input'));
    });
</script>

<%@ include file="../footer-connecte.jsp" %>