<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.learnpath.models.Theme" %>

<%-- ============================================================
     FICHIER : views/admin/admin-theme-modifier.jsp
     RÔLE    : Formulaire de modification d'un thème existant
              - Pré-rempli avec les données du thème
              - Même interface que la création
              - Prévisualisation en temps réel
              - Sélecteur d'icônes avec sélection actuelle
              - Palette de couleurs avec couleur actuelle
              - Indicateur du nombre de cours associés
              - Avertissement si des cours utilisent ce thème
              - Boutons : Enregistrer, Annuler, Voir les cours liés
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Theme theme = (Theme) request.getAttribute("theme");
    String[] iconesDisponibles = (String[]) request.getAttribute("iconesDisponibles");
    String messageErreur = (String) request.getAttribute("messageErreur");
    Integer nbCoursAssocies = (Integer) request.getAttribute("nbCoursAssocies");
    
    // Sécurité : redirection si le thème n'existe pas
    if (theme == null) {
        response.sendRedirect(request.getContextPath() + "/admin/themes");
        return;
    }
    
    // Valeurs par défaut
    if (iconesDisponibles == null) iconesDisponibles = new String[0];
    if (nbCoursAssocies == null) nbCoursAssocies = 0;
    
    // Récupération des valeurs actuelles du thème
    String themeNom = theme.getNom() != null ? theme.getNom() : "";
    String themeDescription = theme.getDescription() != null ? theme.getDescription() : "";
    String themeIcone = theme.getIcone() != null ? theme.getIcone() : "fas fa-book";
    String themeCouleur = theme.getCouleur() != null ? theme.getCouleur() : "#667eea";
    String themeCouleurHex = themeCouleur.replace("#", "");
    int themeOrdre = theme.getOrdre() > 0 ? theme.getOrdre() : 1;
    int themeId = theme.getId();
    
    // ===== CATÉGORIES D'ICÔNES =====
    String[][] categoriesIcones = {
        {"Développement & Tech", "fas fa-globe", "fas fa-code", "fas fa-laptop-code", "fas fa-server", 
         "fas fa-cloud", "fas fa-microchip", "fas fa-cogs", "fas fa-terminal", "fas fa-database"},
        {"Langages & Frameworks", "fas fa-coffee", "fab fa-python", "fab fa-php", "fab fa-java", 
         "fab fa-js", "fab fa-react", "fab fa-angular", "fab fa-node-js", "fab fa-laravel"},
        {"Sécurité & Réseaux", "fas fa-shield-alt", "fas fa-lock", "fas fa-network-wired", 
         "fas fa-fingerprint", "fas fa-user-shield", "fas fa-key", "fas fa-firewall"},
        {"Mobile & Design", "fas fa-mobile-alt", "fas fa-tablet-alt", "fas fa-palette", 
         "fas fa-paint-brush", "fas fa-vector-square", "fas fa-layer-group"},
        {"Science & Data", "fas fa-robot", "fas fa-brain", "fas fa-chart-pie", "fas fa-chart-line", 
         "fas fa-flask", "fas fa-atom", "fas fa-dna", "fas fa-calculator"},
        {"Business & Général", "fas fa-book", "fas fa-graduation-cap", "fas fa-briefcase", 
         "fas fa-chart-bar", "fas fa-bullhorn", "fas fa-globe-americas"}
    };
    
    // ===== COULEURS SUGGÉRÉES =====
    String[] couleursSuggestions = {
        "#667eea", "#764ba2", "#f093fb", "#4facfe", "#43e97b", 
        "#f5576c", "#f39c12", "#e74c3c", "#27ae60", "#2980b9",
        "#9b59b6", "#e91e63", "#00bcd4", "#ff9800", "#795548",
        "#607d8b", "#009688", "#8bc34a", "#ff5722", "#3f51b5"
    };
    
    // ===== TITRE DE LA PAGE =====
    request.setAttribute("pageTitle", "Modifier le thème - " + themeNom + " - Administration - LearnPath");
    
    // ===== CHARGEMENT CSS/JS =====
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-theme-creer.css\">";
    request.setAttribute("extraHead", adminCSS);
    
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-theme-creer.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<%-- ============================================================
     CONTENEUR PRINCIPAL
     ============================================================ --%>
<div class="atc-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/themes" class="breadcrumb-link">
            <i class="fas fa-tags"></i>
            <span>Thèmes</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-edit"></i>
            <span>Modifier : <%= themeNom %></span>
        </span>
    </nav>

    <%-- ============================================================
         MESSAGE D'ERREUR
         ============================================================ --%>
    <% if (messageErreur != null && !messageErreur.isEmpty()) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ============================================================
         ALERTE : THÈME AVEC COURS ASSOCIÉS
         ============================================================ --%>
    <% if (nbCoursAssocies > 0) { %>
        <div class="atc-alerte-cours" style="
            background: linear-gradient(135deg, #fff8e1, #fff3cd);
            border: 1px solid #ffe0a3;
            border-left: 4px solid #f39c12;
            border-radius: 14px;
            padding: 16px 20px;
            margin-bottom: 20px;
            display: flex;
            align-items: flex-start;
            gap: 14px;
            animation: atcFadeIn 0.5s ease;
        ">
            <div style="
                width: 42px;
                height: 42px;
                border-radius: 10px;
                background: #f39c12;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 18px;
                color: white;
                flex-shrink: 0;
                box-shadow: 0 4px 12px rgba(243, 156, 18, 0.25);
            ">
                <i class="fas fa-exclamation-triangle"></i>
            </div>
            <div style="flex: 1;">
                <strong style="display: block; font-size: 14px; color: #1a1a2e; margin-bottom: 4px;">
                    Attention : Ce thème est utilisé par <%= nbCoursAssocies %> cours
                </strong>
                <p style="font-size: 12px; color: #555; margin: 0; line-height: 1.6;">
                    Les modifications apportées à ce thème (nom, icône, couleur) seront immédiatement visibles 
                    sur tous les cours associés et dans l'interface utilisateur. 
                    <br>Le <strong>nom du thème</strong> ne peut pas être modifié car il est référencé par les cours.
                </p>
            </div>
        </div>
    <% } %>

    <%-- ============================================================
         DISPOSITION 2 COLONNES
         ============================================================ --%>
    <div class="atc-layout">

        <%-- ============================================================
             COLONNE GAUCHE : FORMULAIRE
             ============================================================ --%>
        <div class="atc-col-form">

            <form action="${pageContext.request.contextPath}/admin/themes/modifier" 
                  method="POST" 
                  id="formThemeModifier"
                  novalidate>

                <input type="hidden" name="id" value="<%= themeId %>">

                <%-- ========================================================
                     CARTE 1 : INFORMATIONS GÉNÉRALES
                     ======================================================== --%>
                <div class="atc-card">
                    <div class="atc-card-header">
                        <div class="atc-card-header-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                            <i class="fas fa-info-circle"></i>
                        </div>
                        <div class="atc-card-header-text">
                            <h2>Informations générales</h2>
                            <p>Modifiez le nom et la description du thème</p>
                        </div>
                        <%-- Badge ID --%>
                        <div style="margin-left: auto; display: flex; align-items: center; gap: 6px; padding: 6px 12px; background: #f0f1f7; border-radius: 20px; font-size: 11px; color: #8b8fa3; font-weight: 600;">
                            <i class="fas fa-hashtag"></i>
                            <span>ID : <%= themeId %></span>
                        </div>
                    </div>

                    <div class="atc-card-body">

                        <%-- Nom du thème --%>
                        <div class="atc-form-group">
                            <label for="nom" class="atc-label">
                                <i class="fas fa-tag"></i>
                                <span>Nom du thème</span>
                                <span class="atc-required" title="Champ obligatoire">*</span>
                                <% if (nbCoursAssocies > 0) { %>
                                    <span style="font-size: 10px; color: #f39c12; font-weight: 600; margin-left: 6px;">
                                        <i class="fas fa-lock"></i> Verrouillé
                                    </span>
                                <% } %>
                            </label>
                            <div class="atc-input-wrapper">
                                <input 
                                    type="text" 
                                    id="nom" 
                                    name="nom" 
                                    class="atc-input"
                                    value="<%= themeNom %>" 
                                    placeholder="Ex: Intelligence Artificielle" 
                                    maxlength="100" 
                                    required 
                                    autofocus
                                    autocomplete="off"
                                    <%= nbCoursAssocies > 0 ? "readonly style='background: #f5f6fa; color: #8b8fa3; cursor: not-allowed;'" : "" %>
                                >
                                <span class="atc-input-counter" id="nomCounter">
                                    <span id="nomCount"><%= themeNom.length() %></span>/100
                                </span>
                            </div>
                            <% if (nbCoursAssocies > 0) { %>
                                <span class="atc-hint" style="color: #f39c12;">
                                    <i class="fas fa-info-circle"></i>
                                    Le nom est verrouillé car <%= nbCoursAssocies %> cours utilisent ce thème.
                                </span>
                            <% } else { %>
                                <span class="atc-hint">
                                    <i class="fas fa-info-circle"></i>
                                    Choisissez un nom clair et unique. Il sera visible par tous les utilisateurs.
                                </span>
                            <% } %>
                            <span class="atc-error" id="nomError"></span>
                        </div>

                        <%-- Description --%>
                        <div class="atc-form-group">
                            <label for="description" class="atc-label">
                                <i class="fas fa-align-left"></i>
                                <span>Description</span>
                                <span class="atc-optional">(optionnel)</span>
                            </label>
                            <textarea 
                                id="description" 
                                name="description" 
                                class="atc-textarea"
                                rows="3" 
                                maxlength="300"
                                placeholder="Brève description du thème pour aider les utilisateurs à comprendre son contenu..."
                            ><%= themeDescription %></textarea>
                            <div class="atc-textarea-footer">
                                <span class="atc-hint">
                                    <i class="fas fa-lightbulb"></i>
                                    Décrivez en une phrase ce que ce thème regroupe comme sujets.
                                </span>
                                <span class="atc-input-counter" id="descCounter">
                                    <span id="descCount"><%= themeDescription.length() %></span>/300
                                </span>
                            </div>
                        </div>

                    </div>
                </div>

                <%-- ========================================================
                     CARTE 2 : APPARENCE
                     ======================================================== --%>
                <div class="atc-card">
                    <div class="atc-card-header">
                        <div class="atc-card-header-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                            <i class="fas fa-palette"></i>
                        </div>
                        <div class="atc-card-header-text">
                            <h2>Apparence du thème</h2>
                            <p>Modifiez l'icône et la couleur associées</p>
                        </div>
                    </div>

                    <div class="atc-card-body">

                        <%-- Icône + Couleur côte à côte --%>
                        <div class="atc-form-row atc-form-row-2col">

                            <%-- SÉLECTEUR D'ICÔNE --%>
                            <div class="atc-form-group">
                                <label class="atc-label">
                                    <i class="fas fa-icons"></i>
                                    <span>Icône du thème</span>
                                </label>

                                <%-- Barre de recherche d'icônes --%>
                                <div class="atc-icon-search">
                                    <i class="fas fa-search"></i>
                                    <input 
                                        type="text" 
                                        id="iconSearch" 
                                        class="atc-input"
                                        placeholder="Rechercher une icône..."
                                        autocomplete="off"
                                    >
                                </div>

                                <%-- Liste des icônes par catégories --%>
                                <div class="atc-icon-selector" id="iconSelector">
                                    <input type="hidden" name="icone" id="iconeInput" value="<%= themeIcone %>">

                                    <% for (String[] categorie : categoriesIcones) { %>
                                        <div class="atc-icon-category" data-category="<%= categorie[0].toLowerCase() %>">
                                            <span class="atc-icon-category-title"><%= categorie[0] %></span>
                                            <div class="atc-icon-grid">
                                                <% for (int i = 1; i < categorie.length; i++) { 
                                                    String icone = categorie[i];
                                                    boolean isSelected = icone.equals(themeIcone);
                                                %>
                                                    <button 
                                                        type="button"
                                                        class="atc-icon-item <%= isSelected ? "selected" : "" %>"
                                                        data-icon="<%= icone %>"
                                                        onclick="selectionnerIcone(this, '<%= icone %>')"
                                                        title="<%= icone %>"
                                                    >
                                                        <i class="<%= icone %>"></i>
                                                    </button>
                                                <% } %>
                                            </div>
                                        </div>
                                    <% } %>
                                </div>

                                <%-- Icône actuellement sélectionnée --%>
                                <div class="atc-icon-selected" id="iconSelectedDisplay">
                                    <span class="atc-icon-selected-label">Icône sélectionnée :</span>
                                    <span class="atc-icon-selected-preview">
                                        <i class="<%= themeIcone %>"></i>
                                    </span>
                                    <code class="atc-icon-selected-code"><%= themeIcone %></code>
                                </div>
                            </div>

                            <%-- SÉLECTEUR DE COULEUR --%>
                            <div class="atc-form-group">
                                <label class="atc-label">
                                    <i class="fas fa-paint-brush"></i>
                                    <span>Couleur du thème</span>
                                </label>

                                <%-- Color picker + champ texte --%>
                                <div class="atc-color-picker-group">
                                    <div class="atc-color-picker-wrapper">
                                        <input 
                                            type="color" 
                                            id="couleurPicker" 
                                            class="atc-color-picker"
                                            value="<%= themeCouleur %>"
                                        >
                                        <span class="atc-color-picker-label">Choisir</span>
                                    </div>
                                    <div class="atc-color-input-wrapper">
                                        <span class="atc-color-hash">#</span>
                                        <input 
                                            type="text" 
                                            id="couleur" 
                                            name="couleur" 
                                            class="atc-input atc-color-input"
                                            value="<%= themeCouleurHex %>" 
                                            placeholder="667eea" 
                                            maxlength="6"
                                            pattern="^[0-9a-fA-F]{6}$"
                                            required
                                        >
                                    </div>
                                </div>

                                <%-- Palette de couleurs suggérées --%>
                                <div class="atc-color-palette">
                                    <span class="atc-color-palette-label">Couleurs suggérées :</span>
                                    <div class="atc-color-swatches">
                                        <% for (String couleur : couleursSuggestions) { 
                                            String couleurHex = couleur.substring(1);
                                            boolean isSelected = couleur.equalsIgnoreCase(themeCouleur);
                                        %>
                                            <button 
                                                type="button"
                                                class="atc-color-swatch <%= isSelected ? "selected" : "" %>"
                                                style="background: <%= couleur %>;"
                                                data-color="<%= couleurHex %>"
                                                onclick="selectionnerCouleur(this, '<%= couleurHex %>')"
                                                title="<%= couleur %>"
                                            >
                                                <i class="fas fa-check"></i>
                                            </button>
                                        <% } %>
                                    </div>
                                </div>
                            </div>

                        </div>

                        <%-- Ordre d'affichage --%>
                        <div class="atc-form-group atc-form-group-ordre">
                            <label for="ordre" class="atc-label">
                                <i class="fas fa-sort-numeric-down"></i>
                                <span>Ordre d'affichage</span>
                            </label>
                            <div class="atc-ordre-wrapper">
                                <button type="button" class="atc-ordre-btn" onclick="decrementerOrdre()" title="Diminuer l'ordre">
                                    <i class="fas fa-minus"></i>
                                </button>
                                <input 
                                    type="number" 
                                    id="ordre" 
                                    name="ordre" 
                                    class="atc-input atc-ordre-input"
                                    value="<%= themeOrdre %>" 
                                    min="1" 
                                    max="999"
                                    required
                                >
                                <button type="button" class="atc-ordre-btn" onclick="incrementerOrdre()" title="Augmenter l'ordre">
                                    <i class="fas fa-plus"></i>
                                </button>
                            </div>
                            <span class="atc-hint">
                                <i class="fas fa-info-circle"></i>
                                Détermine la position du thème dans la liste. Les thèmes sont triés par ordre croissant.
                            </span>
                        </div>

                    </div>
                </div>

                <%-- ========================================================
                     BOUTONS D'ACTION
                     ======================================================== --%>
                <div class="atc-form-actions">
                    <a href="${pageContext.request.contextPath}/admin/themes" class="atc-btn atc-btn-secondary">
                        <i class="fas fa-arrow-left"></i>
                        <span>Annuler</span>
                    </a>

                    <%-- Bouton Voir les cours liés (si applicable) --%>
                    <% if (nbCoursAssocies > 0) { %>
                        <a href="${pageContext.request.contextPath}/admin/cours?theme=<%= java.net.URLEncoder.encode(themeNom, "UTF-8") %>" 
                           class="atc-btn" 
                           style="background: #fef3e2; color: #e67e22; border: 1px solid #fad7a0;">
                            <i class="fas fa-book"></i>
                            <span>Voir les <%= nbCoursAssocies %> cours liés</span>
                        </a>
                    <% } %>

                    <button type="submit" class="atc-btn atc-btn-primary" id="btnModifierTheme">
                        <i class="fas fa-save"></i>
                        <span>Enregistrer les modifications</span>
                    </button>
                </div>

            </form>

        </div>

        <%-- ============================================================
             COLONNE DROITE : APERÇU + GUIDE
             ============================================================ --%>
        <div class="atc-col-sidebar">

            <%-- ========================================================
                 APERÇU EN TEMPS RÉEL
                 ======================================================== --%>
            <div class="atc-card atc-card-preview">
                <div class="atc-card-header atc-card-header-sm">
                    <div class="atc-card-header-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                        <i class="fas fa-eye"></i>
                    </div>
                    <div class="atc-card-header-text">
                        <h2>Aperçu du thème</h2>
                    </div>
                </div>

                <div class="atc-card-body atc-preview-body">

                    <%-- Carte thème simulée --%>
                    <div class="atc-preview-card" id="previewCard" style="border-color: <%= themeCouleur %>40;">
                        <div class="atc-preview-card-header">
                            <div class="atc-preview-icon" id="previewIcon" style="background: linear-gradient(135deg, <%= themeCouleur %>, <%= themeCouleur %>dd);">
                                <i class="<%= themeIcone %>"></i>
                            </div>
                            <div class="atc-preview-info">
                                <h3 class="atc-preview-nom" id="previewNom"><%= themeNom %></h3>
                                <span class="atc-preview-description" id="previewDescription">
                                    <%= themeDescription.isEmpty() ? "Description du thème apparaîtra ici" : (themeDescription.length() > 80 ? themeDescription.substring(0, 80) + "..." : themeDescription) %>
                                </span>
                            </div>
                        </div>
                        <div class="atc-preview-footer">
                            <span class="atc-preview-ordre" id="previewOrdre">Ordre : <%= themeOrdre %></span>
                            <span class="atc-preview-couleur-code" id="previewCouleurCode"><%= themeCouleur %></span>
                        </div>
                    </div>

                    <%-- Détails de l'aperçu --%>
                    <div class="atc-preview-details">
                        <div class="atc-preview-detail-item">
                            <span class="atc-preview-detail-label">Icône</span>
                            <code class="atc-preview-detail-value" id="previewIconCode"><%= themeIcone %></code>
                        </div>
                        <div class="atc-preview-detail-item">
                            <span class="atc-preview-detail-label">Couleur</span>
                            <code class="atc-preview-detail-value" id="previewCouleurHex"><%= themeCouleur %></code>
                        </div>
                        <div class="atc-preview-detail-item">
                            <span class="atc-preview-detail-label">Ordre</span>
                            <code class="atc-preview-detail-value" id="previewOrdreValue"><%= themeOrdre %></code>
                        </div>
                    </div>

                </div>
            </div>

            <%-- ========================================================
                 STATISTIQUES DU THÈME
                 ======================================================== --%>
            <div class="atc-card" style="border: 1px solid var(--atc-border);">
                <div class="atc-card-header atc-card-header-sm">
                    <div class="atc-card-header-icon" style="background: linear-gradient(135deg, #2980b9, #3498db);">
                        <i class="fas fa-chart-bar"></i>
                    </div>
                    <div class="atc-card-header-text">
                        <h2>Statistiques</h2>
                    </div>
                </div>

                <div class="atc-card-body">
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;">
                                <i class="fas fa-book" style="color: #667eea; margin-right: 6px;"></i>
                                Cours associés
                            </span>
                            <span style="font-weight: 700; font-size: 16px; color: #1a1a2e;"><%= nbCoursAssocies %></span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;">
                                <i class="fas fa-calendar-alt" style="color: #667eea; margin-right: 6px;"></i>
                                Créé le
                            </span>
                            <span style="font-weight: 600; font-size: 13px; color: #1a1a2e;">
                                <%= theme.getDateCreation() != null ? new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("fr", "FR")).format(theme.getDateCreation()) : "N/A" %>
                            </span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;">
                                <i class="fas fa-hashtag" style="color: #667eea; margin-right: 6px;"></i>
                                ID du thème
                            </span>
                            <span style="font-weight: 600; font-size: 13px; color: #1a1a2e;">#<%= themeId %></span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ========================================================
                 GUIDE DE BONNES PRATIQUES
                 ======================================================== --%>
            <div class="atc-card atc-card-guide">
                <div class="atc-card-header atc-card-header-sm">
                    <div class="atc-card-header-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                        <i class="fas fa-lightbulb"></i>
                    </div>
                    <div class="atc-card-header-text">
                        <h2>Bonnes pratiques</h2>
                    </div>
                </div>

                <div class="atc-card-body">
                    <ul class="atc-guide-list">
                        <li>
                            <i class="fas fa-check-circle"></i>
                            <span><strong>Nom unique :</strong> Chaque thème doit avoir un nom distinct.</span>
                        </li>
                        <li>
                            <i class="fas fa-check-circle"></i>
                            <span><strong>Icône pertinente :</strong> Choisissez une icône qui représente bien le sujet.</span>
                        </li>
                        <li>
                            <i class="fas fa-check-circle"></i>
                            <span><strong>Couleur distinctive :</strong> Utilisez une couleur unique pour différencier visuellement.</span>
                        </li>
                        <li>
                            <i class="fas fa-check-circle"></i>
                            <span><strong>Ordre logique :</strong> Les thèmes les plus populaires en premier.</span>
                        </li>
                    </ul>
                </div>
            </div>

        </div>

    </div>

</div>

<%-- ============================================================
     SCRIPTS INLINE POUR LA PRÉVISUALISATION
     ============================================================ --%>
<script>
    // ==========================================================
    // INITIALISATION DE L'ÉTAT AVEC LES VALEURS DU THÈME
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        // Déclencher les événements initiaux pour les compteurs
        document.getElementById('nom').dispatchEvent(new Event('input', { bubbles: true }));
        document.getElementById('description').dispatchEvent(new Event('input', { bubbles: true }));
        document.getElementById('ordre').dispatchEvent(new Event('input', { bubbles: true }));
        
        // Mettre à jour l'état global du script
        if (window.state) {
            window.state.selectedIcon = '<%= themeIcone %>';
            window.state.selectedColor = '<%= themeCouleurHex %>';
        }
    });

    // ==========================================================
    // SÉLECTION D'ICÔNE (redéfinition pour s'assurer qu'elle existe)
    // ==========================================================
    if (typeof selectionnerIcone !== 'function') {
        window.selectionnerIcone = function(button, iconClass) {
            document.querySelectorAll('.atc-icon-item.selected').forEach(function(item) {
                item.classList.remove('selected');
            });
            button.classList.add('selected');
            document.getElementById('iconeInput').value = iconClass;
            
            var displayIcon = document.querySelector('#iconSelectedDisplay i');
            var displayCode = document.querySelector('#iconSelectedDisplay code');
            if (displayIcon) displayIcon.className = iconClass;
            if (displayCode) displayCode.textContent = iconClass;
            
            var previewIcon = document.querySelector('#previewIcon i');
            var previewIconCode = document.getElementById('previewIconCode');
            if (previewIcon) previewIcon.className = iconClass;
            if (previewIconCode) previewIconCode.textContent = iconClass;
        };
    }

    // ==========================================================
    // SÉLECTION DE COULEUR (redéfinition pour s'assurer qu'elle existe)
    // ==========================================================
    if (typeof selectionnerCouleur !== 'function') {
        window.selectionnerCouleur = function(button, couleurHex) {
            document.querySelectorAll('.atc-color-swatch.selected').forEach(function(item) {
                item.classList.remove('selected');
            });
            button.classList.add('selected');
            
            document.getElementById('couleur').value = couleurHex;
            document.getElementById('couleurPicker').value = '#' + couleurHex;
            
            var couleur = '#' + couleurHex;
            var previewIcon = document.getElementById('previewIcon');
            if (previewIcon) {
                previewIcon.style.background = 'linear-gradient(135deg, ' + couleur + ', ' + couleur + 'dd)';
            }
            document.getElementById('previewCouleurHex').textContent = couleur;
            document.getElementById('previewCouleurCode').textContent = couleur;
        };
    }

    // ==========================================================
    // BOUTONS D'ORDRE (redéfinition)
    // ==========================================================
    if (typeof incrementerOrdre !== 'function') {
        window.incrementerOrdre = function() {
            var input = document.getElementById('ordre');
            var val = parseInt(input.value) || 0;
            if (val < 999) {
                input.value = val + 1;
                input.dispatchEvent(new Event('input', { bubbles: true }));
            }
        };
    }

    if (typeof decrementerOrdre !== 'function') {
        window.decrementerOrdre = function() {
            var input = document.getElementById('ordre');
            var val = parseInt(input.value) || 2;
            if (val > 1) {
                input.value = val - 1;
                input.dispatchEvent(new Event('input', { bubbles: true }));
            }
        };
    }

    // ==========================================================
    // MISE À JOUR DU NOM EN TEMPS RÉEL
    // ==========================================================
    document.getElementById('nom').addEventListener('input', function() {
        var nom = this.value.trim() || 'Nom du thème';
        document.getElementById('previewNom').textContent = nom;
    });

    // ==========================================================
    // MISE À JOUR DE LA DESCRIPTION EN TEMPS RÉEL
    // ==========================================================
    document.getElementById('description').addEventListener('input', function() {
        var desc = this.value.trim() || 'Description du thème apparaîtra ici';
        if (desc.length > 80) desc = desc.substring(0, 80) + '...';
        document.getElementById('previewDescription').textContent = desc;
    });

    // ==========================================================
    // MISE À JOUR DE L'ORDRE EN TEMPS RÉEL
    // ==========================================================
    document.getElementById('ordre').addEventListener('input', function() {
        var ordre = this.value || '1';
        document.getElementById('previewOrdre').textContent = 'Ordre : ' + ordre;
        document.getElementById('previewOrdreValue').textContent = ordre;
    });

    // ==========================================================
    // COMPTEURS DE CARACTÈRES
    // ==========================================================
    document.getElementById('nom').addEventListener('input', function() {
        var count = this.value.length;
        document.getElementById('nomCount').textContent = count;
        var counter = document.getElementById('nomCounter');
        if (counter) {
            counter.style.color = count > 90 ? '#e74c3c' : count > 70 ? '#f39c12' : '#8b8fa3';
        }
    });

    document.getElementById('description').addEventListener('input', function() {
        var count = this.value.length;
        document.getElementById('descCount').textContent = count;
    });

    // ==========================================================
    // VALIDATION AVANT SOUMISSION
    // ==========================================================
    document.getElementById('formThemeModifier').addEventListener('submit', function(e) {
        var nom = document.getElementById('nom');
        var couleur = document.getElementById('couleur');
        var nomError = document.getElementById('nomError');
        var hasError = false;

        nomError.textContent = '';
        nom.classList.remove('atc-input-error');
        couleur.classList.remove('atc-input-error');

        if (!nom.value.trim() && !nom.readOnly) {
            nomError.textContent = 'Le nom du thème est obligatoire.';
            nom.classList.add('atc-input-error');
            nom.focus();
            hasError = true;
        } else if (!nom.readOnly && nom.value.trim().length < 2) {
            nomError.textContent = 'Le nom doit contenir au moins 2 caractères.';
            nom.classList.add('atc-input-error');
            nom.focus();
            hasError = true;
        }

        if (!couleur.value.trim().match(/^[0-9a-fA-F]{6}$/)) {
            couleur.classList.add('atc-input-error');
            couleur.focus();
            hasError = true;
        }

        if (hasError) {
            e.preventDefault();
        } else {
            var submitBtn = document.getElementById('btnModifierTheme');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Enregistrement...</span>';
            }
        }
    });
</script>

<%@ include file="../footer-connecte.jsp" %>