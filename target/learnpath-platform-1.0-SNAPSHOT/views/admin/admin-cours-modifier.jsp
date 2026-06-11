<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.learnpath.models.Cours" %>
<%@ page import="com.learnpath.models.Theme" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-modifier.jsp
     RÔLE    : Formulaire de modification d'un cours existant
              - Pré-rempli avec les données du cours
              - Thème et niveau verrouillés si apprenants inscrits
              - Changement de statut (brouillon → publié → archivé)
              - Aperçu en temps réel
              - Avertissement si apprenants ou certificats
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    List<Theme> themes = (List<Theme>) request.getAttribute("themes");
    String[] niveaux = (String[]) request.getAttribute("niveaux");
    Integer nbApprenants = (Integer) request.getAttribute("nbApprenants");
    Integer nbCertificats = (Integer) request.getAttribute("nbCertificats");
    Boolean aDesApprenants = (Boolean) request.getAttribute("aDesApprenants");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (cours == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (themes == null) themes = new java.util.ArrayList<>();
    if (niveaux == null) niveaux = new String[]{"elementaire", "moyen", "avance"};
    if (nbApprenants == null) nbApprenants = 0;
    if (nbCertificats == null) nbCertificats = 0;
    if (aDesApprenants == null) aDesApprenants = false;

    String coursTitre = cours.getTitre() != null ? cours.getTitre() : "";
    String coursTheme = cours.getTheme() != null ? cours.getTheme() : "";
    String coursNiveau = cours.getNiveau() != null ? cours.getNiveau() : "elementaire";
    String coursDescription = cours.getDescription() != null ? cours.getDescription() : "";
    String coursObjectifs = cours.getObjectifs() != null ? cours.getObjectifs() : "";
    String coursPrerequis = cours.getPrerequis() != null ? cours.getPrerequis() : "";
    String coursImage = cours.getImage() != null ? cours.getImage() : "";
    String coursDuree = cours.getDureeEstimee() != null ? cours.getDureeEstimee() : "";
    String coursStatut = cours.getStatut() != null ? cours.getStatut() : "brouillon";
    int coursId = cours.getId();

    request.setAttribute("pageTitle", "Modifier le cours - " + coursTitre + " - Administration");
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
            <i class="fas fa-edit"></i><span>Modifier : <%= coursTitre %></span>
        </span>
    </nav>

    <%-- ===== MESSAGE D'ERREUR ===== --%>
    <% if (messageErreur != null && !messageErreur.isEmpty()) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i><span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== ALERTE APPRENANTS ===== --%>
    <% if (aDesApprenants) { %>
        <div class="acc-alerte-apprenants">
            <div class="acc-alerte-icon">
                <i class="fas fa-users"></i>
            </div>
            <div class="acc-alerte-content">
                <strong>Ce cours a des apprenants inscrits</strong>
                <p>
                    <%= nbApprenants %> apprenant<%= nbApprenants > 1 ? "s" : "" %> 
                    <% if (nbCertificats > 0) { %>
                        • <%= nbCertificats %> certificat<%= nbCertificats > 1 ? "s" : "" %> délivré<%= nbCertificats > 1 ? "s" : "" %>
                    <% } %>
                    <br>Le <strong>thème</strong> et le <strong>niveau</strong> sont verrouillés pour préserver l'intégrité des données.
                </p>
            </div>
        </div>
    <% } %>

    <%-- ===== LAYOUT 2 COLONNES ===== --%>
    <div class="acc-layout">

        <%-- ===== COLONNE GAUCHE : FORMULAIRE ===== --%>
        <div class="acc-col-form">

            <form action="${pageContext.request.contextPath}/admin/cours/modifier" method="POST" id="formCoursModifier" novalidate>
                <input type="hidden" name="id" value="<%= coursId %>">

                <%-- ===== CARTE 1 : INFORMATIONS GÉNÉRALES ===== --%>
                <div class="acc-card">
                    <div class="acc-card-header">
                        <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                            <i class="fas fa-info-circle"></i>
                        </div>
                        <div class="acc-card-header-text">
                            <h2>Informations générales</h2>
                            <p>Modifiez le thème, le niveau et le titre du cours</p>
                        </div>
                        <div class="acc-card-header-badge">
                            <span style="display: flex; align-items: center; gap: 6px; padding: 6px 12px; background: #f0f1f7; border-radius: 20px; font-size: 11px; color: #8b8fa3; font-weight: 600;">
                                <i class="fas fa-hashtag"></i><span>ID : <%= coursId %></span>
                            </span>
                        </div>
                    </div>
                    <div class="acc-card-body">

                        <%-- Thème + Niveau côte à côte --%>
                        <div class="acc-form-row acc-form-row-2col">
                            <div class="acc-form-group">
                                <label for="theme" class="acc-label">
                                    <i class="fas fa-tag"></i><span>Thème</span><span class="acc-required">*</span>
                                    <% if (aDesApprenants) { %>
                                        <span style="font-size: 10px; color: #f39c12; font-weight: 600; margin-left: 6px;"><i class="fas fa-lock"></i> Verrouillé</span>
                                    <% } %>
                                </label>
                                <select id="theme" name="theme" class="acc-select" <%= aDesApprenants ? "disabled" : "required" %>>
                                    <option value="">-- Choisir un thème --</option>
                                    <% for (Theme t : themes) { %>
                                        <option value="<%= t.getNom() %>" <%= t.getNom().equals(coursTheme) ? "selected" : "" %>>
                                            <%= t.getNom() %>
                                        </option>
                                    <% } %>
                                </select>
                                <% if (aDesApprenants) { %>
                                    <span class="acc-hint" style="color: #f39c12;"><i class="fas fa-info-circle"></i> Thème verrouillé car des apprenants sont inscrits.</span>
                                <% } %>
                            </div>

                            <div class="acc-form-group">
                                <label for="niveau" class="acc-label">
                                    <i class="fas fa-signal"></i><span>Niveau</span><span class="acc-required">*</span>
                                    <% if (aDesApprenants) { %>
                                        <span style="font-size: 10px; color: #f39c12; font-weight: 600; margin-left: 6px;"><i class="fas fa-lock"></i> Verrouillé</span>
                                    <% } %>
                                </label>
                                <select id="niveau" name="niveau" class="acc-select" <%= aDesApprenants ? "disabled" : "required" %>>
                                    <% for (String niveau : niveaux) { 
                                        String niveauFormate = "";
                                        if ("elementaire".equals(niveau)) niveauFormate = "Élémentaire";
                                        else if ("moyen".equals(niveau)) niveauFormate = "Moyen";
                                        else if ("avance".equals(niveau)) niveauFormate = "Avancé";
                                    %>
                                        <option value="<%= niveau %>" <%= niveau.equals(coursNiveau) ? "selected" : "" %>>
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
                                       value="<%= coursTitre %>" placeholder="Ex: Introduction au Développement Web"
                                       maxlength="200" required autofocus>
                                <span class="acc-input-counter" id="titreCounter">
                                    <span id="titreCount"><%= coursTitre.length() %></span>/200
                                </span>
                            </div>
                            <span class="acc-error" id="titreError"></span>
                        </div>

                        <%-- Description --%>
                        <div class="acc-form-group">
                            <label for="description" class="acc-label">
                                <i class="fas fa-align-left"></i><span>Description</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="description" name="description" class="acc-textarea" rows="3"
                                      maxlength="500" placeholder="Décrivez brièvement le contenu du cours..."><%= coursDescription %></textarea>
                            <div class="acc-textarea-footer">
                                <span class="acc-hint"><i class="fas fa-lightbulb"></i> Résumez en quelques phrases.</span>
                                <span class="acc-input-counter" id="descCounter"><span id="descCount"><%= coursDescription.length() %></span>/500</span>
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
                            <p>Modifiez les objectifs, prérequis et statut</p>
                        </div>
                    </div>
                    <div class="acc-card-body">

                        <%-- Objectifs --%>
                        <div class="acc-form-group">
                            <label for="objectifs" class="acc-label">
                                <i class="fas fa-flag-checkered"></i><span>Objectifs pédagogiques</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="objectifs" name="objectifs" class="acc-textarea" rows="3"
                                      maxlength="500" placeholder="Ce que l'apprenant saura faire..."><%= coursObjectifs %></textarea>
                        </div>

                        <%-- Prérequis --%>
                        <div class="acc-form-group">
                            <label for="prerequis" class="acc-label">
                                <i class="fas fa-clipboard-list"></i><span>Prérequis</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="prerequis" name="prerequis" class="acc-textarea" rows="2"
                                      maxlength="300" placeholder="Connaissances nécessaires..."><%= coursPrerequis %></textarea>
                        </div>

                        <%-- Image + Durée + Statut --%>
                        <div class="acc-form-row acc-form-row-3col">
                            <div class="acc-form-group">
                                <label for="image" class="acc-label">
                                    <i class="fas fa-image"></i><span>Image</span><span class="acc-optional">(optionnel)</span>
                                </label>
                                <input type="text" id="image" name="image" class="acc-input"
                                       value="<%= coursImage %>" placeholder="URL de l'image">
                            </div>

                            <div class="acc-form-group">
                                <label for="dureeEstimee" class="acc-label">
                                    <i class="fas fa-clock"></i><span>Durée estimée</span><span class="acc-optional">(optionnel)</span>
                                </label>
                                <input type="text" id="dureeEstimee" name="dureeEstimee" class="acc-input"
                                       value="<%= coursDuree %>" placeholder="Ex: 40 heures">
                            </div>

                            <div class="acc-form-group">
                                <label for="statut" class="acc-label">
                                    <i class="fas fa-toggle-on"></i><span>Statut</span>
                                </label>
                                <select id="statut" name="statut" class="acc-select">
                                    <option value="brouillon" <%= "brouillon".equals(coursStatut) ? "selected" : "" %>>Brouillon</option>
                                    <option value="publié" <%= "publié".equals(coursStatut) ? "selected" : "" %>>Publié</option>
                                    <option value="archivé" <%= "archivé".equals(coursStatut) ? "selected" : "" %>>Archivé</option>
                                </select>
                                <span class="acc-hint"><i class="fas fa-info-circle"></i> Seuls les cours publiés sont visibles par les apprenants.</span>
                            </div>
                        </div>

                    </div>
                </div>

                <%-- ===== BOUTONS D'ACTION ===== --%>
                <div class="acc-form-actions">
                    <a href="${pageContext.request.contextPath}/admin/cours" class="acc-btn acc-btn-secondary">
                        <i class="fas fa-arrow-left"></i><span>Annuler</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/cours/modules?coursId=<%= coursId %>" class="acc-btn" style="background: #fef3e2; color: #e67e22; border: 1px solid #fad7a0;">
                        <i class="fas fa-cubes"></i><span>Gérer les modules</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= coursId %>" class="acc-btn" style="background: #f3e5f5; color: #9b59b6; border: 1px solid #e1bee7;">
                        <i class="fas fa-bookmark"></i><span>Gérer les chapitres</span>
                    </a>
                    <button type="submit" class="acc-btn acc-btn-primary" id="btnModifierCours">
                        <i class="fas fa-save"></i><span>Enregistrer</span>
                    </button>
                </div>

            </form>
        </div>

        <%-- ===== COLONNE DROITE : APERÇU + GUIDE ===== --%>
        <div class="acc-col-sidebar">

            <%-- ===== APERÇU ===== --%>
            <div class="acc-card acc-card-preview">
                <div class="acc-card-header acc-card-header-sm">
                    <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                        <i class="fas fa-eye"></i>
                    </div>
                    <div class="acc-card-header-text"><h2>Aperçu du cours</h2></div>
                </div>
                <div class="acc-card-body acc-preview-body">
                    <div class="acc-preview-card" id="previewCard" style="border-color: <%= cours.getCouleurTheme() %>40;">
                        <div class="acc-preview-icon" id="previewIcon" style="background: linear-gradient(135deg, <%= cours.getCouleurTheme() %>, <%= cours.getCouleurTheme() %>dd);">
                            <i class="<%= cours.getIconeTheme() %>"></i>
                        </div>
                        <h3 class="acc-preview-titre" id="previewTitre"><%= coursTitre %></h3>
                        <span class="acc-preview-theme" id="previewTheme"><%= coursTheme %> • <%= cours.getNiveauFormate() %></span>
                        <p class="acc-preview-desc" id="previewDesc"><%= coursDescription.isEmpty() ? "Description du cours" : (coursDescription.length() > 100 ? coursDescription.substring(0, 100) + "..." : coursDescription) %></p>
                        <div class="acc-preview-meta">
                            <span id="previewDuree"><i class="fas fa-clock"></i> <%= coursDuree.isEmpty() ? "Durée estimée" : coursDuree %></span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ===== STATISTIQUES ===== --%>
            <div class="acc-card">
                <div class="acc-card-header acc-card-header-sm">
                    <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #2980b9, #3498db);">
                        <i class="fas fa-chart-bar"></i>
                    </div>
                    <div class="acc-card-header-text"><h2>Statistiques</h2></div>
                </div>
                <div class="acc-card-body">
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-users" style="color: #667eea; margin-right: 6px;"></i>Apprenants</span>
                            <span style="font-weight: 700; font-size: 16px; color: #1a1a2e;"><%= nbApprenants %></span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-certificate" style="color: #f39c12; margin-right: 6px;"></i>Certificats</span>
                            <span style="font-weight: 700; font-size: 16px; color: #1a1a2e;"><%= nbCertificats %></span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-calendar-alt" style="color: #667eea; margin-right: 6px;"></i>Créé le</span>
                            <span style="font-weight: 600; font-size: 13px; color: #1a1a2e;">
                                <%= cours.getDateCreation() != null ? new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("fr", "FR")).format(cours.getDateCreation()) : "N/A" %>
                            </span>
                        </div>
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
        var theme = themeSelect && themeSelect.options[themeSelect.selectedIndex] ? themeSelect.options[themeSelect.selectedIndex].text : 'Thème';
        var niveau = niveauSelect && niveauSelect.options[niveauSelect.selectedIndex] ? niveauSelect.options[niveauSelect.selectedIndex].text : 'Niveau';
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
    document.getElementById('formCoursModifier').addEventListener('submit', function(e) {
        var hasError = false;
        var titre = document.getElementById('titre');
        var titreError = document.getElementById('titreError');
        titre.classList.remove('acc-input-error');
        if (titreError) titreError.textContent = '';

        if (!titre.value.trim()) {
            titre.classList.add('acc-input-error');
            if (titreError) titreError.textContent = 'Le titre est obligatoire.';
            hasError = true;
        } else if (titre.value.trim().length < 5) {
            titre.classList.add('acc-input-error');
            if (titreError) titreError.textContent = 'Minimum 5 caractères.';
            hasError = true;
        }

        if (hasError) {
            e.preventDefault();
        } else {
            var btn = document.getElementById('btnModifierCours');
            if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Enregistrement...</span>'; }
        }
    });

    document.addEventListener('DOMContentLoaded', function() {
        updatePreviewMeta();
        if (titreInput) titreInput.dispatchEvent(new Event('input'));
        if (descInput) descInput.dispatchEvent(new Event('input'));
        if (dureeInput) dureeInput.dispatchEvent(new Event('input'));
    });
</script>

<%@ include file="../footer-connecte.jsp" %>