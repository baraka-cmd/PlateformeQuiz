<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- ============================================================
     FICHIER : views/admin/admin-theme-creer.jsp
     RÔLE    : Formulaire de création d'un thème de cours
              - Interface professionnelle complète
              - Prévisualisation en temps réel
              - Sélecteur d'icônes avec rendu visuel
              - Palette de couleurs avec picker + suggestions
              - Validation côté client ET serveur
              - Aperçu dynamique du thème
              - Breadcrumb hiérarchique
              - Messages d'erreur contextuels
              - Boutons d'action clairs
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    String[] iconesDisponibles = (String[]) request.getAttribute("iconesDisponibles");
    Integer ordreSuggere = (Integer) request.getAttribute("ordreSuggere");
    String messageErreur = (String) request.getAttribute("messageErreur");
    
    // Valeurs par défaut
    if (iconesDisponibles == null) iconesDisponibles = new String[0];
    if (ordreSuggere == null) ordreSuggere = 1;
    
    // ===== CATÉGORIES D'ICÔNES POUR UN MEILLEUR ORGANISATION =====
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
    request.setAttribute("pageTitle", "Créer un thème - Administration - AmijayQuiz");
    
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
            <i class="fas fa-plus-circle"></i>
            <span>Créer un thème</span>
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
         DISPOSITION 2 COLONNES
         Colonne gauche : Formulaire (2/3)
         Colonne droite : Aperçu + Guide (1/3)
         ============================================================ --%>
    <div class="atc-layout">

        <%-- ============================================================
             COLONNE GAUCHE : FORMULAIRE
             ============================================================ --%>
        <div class="atc-col-form">

            <form action="${pageContext.request.contextPath}/admin/themes/creer" 
                  method="POST" 
                  id="formThemeCreer"
                  novalidate>

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
                            <p>Définissez le nom et la description du thème</p>
                        </div>
                    </div>

                    <div class="atc-card-body">

                        <%-- Nom du thème --%>
                        <div class="atc-form-group">
                            <label for="nom" class="atc-label">
                                <i class="fas fa-tag"></i>
                                <span>Nom du thème</span>
                                <span class="atc-required" title="Champ obligatoire">*</span>
                            </label>
                            <div class="atc-input-wrapper">
                                <input 
                                    type="text" 
                                    id="nom" 
                                    name="nom" 
                                    class="atc-input"
                                    placeholder="Ex: Intelligence Artificielle" 
                                    maxlength="100" 
                                    required 
                                    autofocus
                                    autocomplete="off"
                                >
                                <span class="atc-input-counter" id="nomCounter">
                                    <span id="nomCount">0</span>/100
                                </span>
                            </div>
                            <span class="atc-hint">
                                <i class="fas fa-info-circle"></i>
                                Choisissez un nom clair et unique. Il sera visible par tous les utilisateurs.
                            </span>
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
                            ></textarea>
                            <div class="atc-textarea-footer">
                                <span class="atc-hint">
                                    <i class="fas fa-lightbulb"></i>
                                    Décrivez en une phrase ce que ce thème regroupe comme sujets.
                                </span>
                                <span class="atc-input-counter" id="descCounter">
                                    <span id="descCount">0</span>/300
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
                            <p>Choisissez l'icône et la couleur associées</p>
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
                                    <input type="hidden" name="icone" id="iconeInput" value="fas fa-book">

                                    <% for (String[] categorie : categoriesIcones) { %>
                                        <div class="atc-icon-category" data-category="<%= categorie[0].toLowerCase() %>">
                                            <span class="atc-icon-category-title"><%= categorie[0] %></span>
                                            <div class="atc-icon-grid">
                                                <% for (int i = 1; i < categorie.length; i++) { 
                                                    String icone = categorie[i];
                                                    boolean isSelected = "fas fa-book".equals(icone);
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
                                        <i class="fas fa-book"></i>
                                    </span>
                                    <code class="atc-icon-selected-code">fas fa-book</code>
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
                                            value="#667eea"
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
                                            value="667eea" 
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
                                        <% for (String couleur : couleursSuggestions) { %>
                                            <button 
                                                type="button"
                                                class="atc-color-swatch <%= "#667eea".equals(couleur) ? "selected" : "" %>"
                                                style="background: <%= couleur %>;"
                                                data-color="<%= couleur.substring(1) %>"
                                                onclick="selectionnerCouleur(this, '<%= couleur.substring(1) %>')"
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
                                    value="<%= ordreSuggere %>" 
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
                    <button type="submit" class="atc-btn atc-btn-primary" id="btnCreerTheme">
                        <i class="fas fa-save"></i>
                        <span>Créer le thème</span>
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
                    <div class="atc-preview-card" id="previewCard">
                        <div class="atc-preview-card-header">
                            <div class="atc-preview-icon" id="previewIcon" style="background: linear-gradient(135deg, #667eea, #667eeadd);">
                                <i class="fas fa-book"></i>
                            </div>
                            <div class="atc-preview-info">
                                <h3 class="atc-preview-nom" id="previewNom">Nom du thème</h3>
                                <span class="atc-preview-description" id="previewDescription">
                                    Description du thème apparaîtra ici
                                </span>
                            </div>
                        </div>
                        <div class="atc-preview-footer">
                            <span class="atc-preview-ordre" id="previewOrdre">Ordre : <%= ordreSuggere %></span>
                            <span class="atc-preview-couleur-code" id="previewCouleurCode">#667eea</span>
                        </div>
                    </div>

                    <%-- Détails de l'aperçu --%>
                    <div class="atc-preview-details">
                        <div class="atc-preview-detail-item">
                            <span class="atc-preview-detail-label">Icône</span>
                            <code class="atc-preview-detail-value" id="previewIconCode">fas fa-book</code>
                        </div>
                        <div class="atc-preview-detail-item">
                            <span class="atc-preview-detail-label">Couleur</span>
                            <code class="atc-preview-detail-value" id="previewCouleurHex">#667eea</code>
                        </div>
                        <div class="atc-preview-detail-item">
                            <span class="atc-preview-detail-label">Ordre</span>
                            <code class="atc-preview-detail-value" id="previewOrdreValue"><%= ordreSuggere %></code>
                        </div>
                    </div>

                </div>
            </div>

            <%-- ========================================================
                 GUIDE DE BONNES PRATIQUES
                 ======================================================== --%>
            <div class="atc-card atc-card-guide">
                <div class="atc-card-header atc-card-header-sm">
                    <div class="atc-card-header-icon" style="background: linear-gradient(135deg, #2980b9, #3498db);">
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
                        <li>
                            <i class="fas fa-check-circle"></i>
                            <span><strong>Description claire :</strong> Aidez les utilisateurs à comprendre le contenu.</span>
                        </li>
                    </ul>
                </div>
            </div>

            <%-- ========================================================
                 RAPPEL HIÉRARCHIE
                 ======================================================== --%>
            <div class="atc-card atc-card-hierarchy">
                <div class="atc-card-header atc-card-header-sm">
                    <div class="atc-card-header-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                        <i class="fas fa-sitemap"></i>
                    </div>
                    <div class="atc-card-header-text">
                        <h2>Hiérarchie du contenu</h2>
                    </div>
                </div>

                <div class="atc-card-body">
                    <div class="atc-hierarchy-flow">
                        <div class="atc-hierarchy-item atc-hierarchy-current">
                            <i class="fas fa-tag"></i>
                            <span>Thème</span>
                        </div>
                        <i class="fas fa-arrow-down atc-hierarchy-arrow"></i>
                        <div class="atc-hierarchy-item">
                            <i class="fas fa-book"></i>
                            <span>Cours</span>
                        </div>
                        <i class="fas fa-arrow-down atc-hierarchy-arrow"></i>
                        <div class="atc-hierarchy-item">
                            <i class="fas fa-cube"></i>
                            <span>Modules (1-15)</span>
                        </div>
                        <i class="fas fa-arrow-down atc-hierarchy-arrow"></i>
                        <div class="atc-hierarchy-item">
                            <i class="fas fa-bookmark"></i>
                            <span>Chapitres</span>
                        </div>
                        <i class="fas fa-arrow-down atc-hierarchy-arrow"></i>
                        <div class="atc-hierarchy-item">
                            <i class="fas fa-file-alt"></i>
                            <span>Leçons</span>
                        </div>
                    </div>
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
    // SÉLECTION D'ICÔNE
    // ==========================================================
    function selectionnerIcone(button, iconClass) {
        // Retirer la sélection précédente
        document.querySelectorAll('.atc-icon-item.selected').forEach(function(item) {
            item.classList.remove('selected');
        });
        
        // Ajouter la nouvelle sélection
        button.classList.add('selected');
        document.getElementById('iconeInput').value = iconClass;
        
        // Mettre à jour l'affichage
        document.getElementById('iconSelectedDisplay').querySelector('i').className = iconClass;
        document.getElementById('iconSelectedDisplay').querySelector('code').textContent = iconClass;
        
        // Mettre à jour l'aperçu
        document.getElementById('previewIcon').querySelector('i').className = iconClass;
        document.getElementById('previewIconCode').textContent = iconClass;
    }

    // ==========================================================
    // RECHERCHE D'ICÔNES
    // ==========================================================
    document.getElementById('iconSearch').addEventListener('input', function() {
        var searchTerm = this.value.toLowerCase().trim();
        var categories = document.querySelectorAll('.atc-icon-category');
        var found = false;

        categories.forEach(function(category) {
            var items = category.querySelectorAll('.atc-icon-item');
            var categoryVisible = false;

            items.forEach(function(item) {
                var iconClass = item.getAttribute('data-icon').toLowerCase();
                if (searchTerm === '' || iconClass.includes(searchTerm)) {
                    item.style.display = '';
                    categoryVisible = true;
                } else {
                    item.style.display = 'none';
                }
            });

            category.style.display = categoryVisible ? '' : 'none';
            if (categoryVisible) found = true;
        });
    });

    // ==========================================================
    // SÉLECTION DE COULEUR
    // ==========================================================
    function selectionnerCouleur(button, couleurHex) {
        // Retirer la sélection précédente
        document.querySelectorAll('.atc-color-swatch.selected').forEach(function(item) {
            item.classList.remove('selected');
        });
        
        // Ajouter la nouvelle sélection
        button.classList.add('selected');
        
        // Mettre à jour les champs
        document.getElementById('couleur').value = couleurHex;
        document.getElementById('couleurPicker').value = '#' + couleurHex;
        
        // Mettre à jour l'aperçu
        updateColorPreview(couleurHex);
    }

    // Synchronisation color picker → champ texte
    document.getElementById('couleurPicker').addEventListener('input', function() {
        var hex = this.value.replace('#', '');
        document.getElementById('couleur').value = hex;
        updateColorPreview(hex);
        updateSwatchSelection(hex);
    });

    // Synchronisation champ texte → color picker
    document.getElementById('couleur').addEventListener('input', function() {
        var hex = this.value.replace('#', '');
        if (hex.match(/^[0-9a-fA-F]{6}$/)) {
            document.getElementById('couleurPicker').value = '#' + hex;
            updateColorPreview(hex);
            updateSwatchSelection(hex);
        }
    });

    function updateColorPreview(hex) {
        var couleur = '#' + hex;
        document.getElementById('previewIcon').style.background = 'linear-gradient(135deg, ' + couleur + ', ' + couleur + 'dd)';
        document.getElementById('previewCouleurHex').textContent = couleur;
        document.getElementById('previewCouleurCode').textContent = couleur;
    }

    function updateSwatchSelection(hex) {
        document.querySelectorAll('.atc-color-swatch.selected').forEach(function(item) {
            item.classList.remove('selected');
        });
        var swatch = document.querySelector('.atc-color-swatch[data-color="' + hex + '"]');
        if (swatch) swatch.classList.add('selected');
    }

    // ==========================================================
    // MISE À JOUR DU NOM EN TEMPS RÉEL
    // ==========================================================
    document.getElementById('nom').addEventListener('input', function() {
        var nom = this.value.trim() || 'Nom du thème';
        document.getElementById('previewNom').textContent = nom;
    });

    // Compteur de caractères pour le nom
    document.getElementById('nom').addEventListener('input', function() {
        var count = this.value.length;
        document.getElementById('nomCount').textContent = count;
        var counter = document.getElementById('nomCounter');
        counter.style.color = count > 90 ? '#e74c3c' : count > 70 ? '#f39c12' : '#8b8fa3';
    });

    // ==========================================================
    // MISE À JOUR DE LA DESCRIPTION EN TEMPS RÉEL
    // ==========================================================
    document.getElementById('description').addEventListener('input', function() {
        var desc = this.value.trim() || 'Description du thème apparaîtra ici';
        if (desc.length > 80) desc = desc.substring(0, 80) + '...';
        document.getElementById('previewDescription').textContent = desc;
    });

    // Compteur de caractères pour la description
    document.getElementById('description').addEventListener('input', function() {
        var count = this.value.length;
        document.getElementById('descCount').textContent = count;
    });

    // ==========================================================
    // MISE À JOUR DE L'ORDRE EN TEMPS RÉEL
    // ==========================================================
    document.getElementById('ordre').addEventListener('input', function() {
        var ordre = this.value || '<%= ordreSuggere %>';
        document.getElementById('previewOrdre').textContent = 'Ordre : ' + ordre;
        document.getElementById('previewOrdreValue').textContent = ordre;
    });

    // ==========================================================
    // BOUTONS +/- POUR L'ORDRE
    // ==========================================================
    function incrementerOrdre() {
        var input = document.getElementById('ordre');
        var val = parseInt(input.value) || 0;
        if (val < 999) {
            input.value = val + 1;
            input.dispatchEvent(new Event('input'));
        }
    }

    function decrementerOrdre() {
        var input = document.getElementById('ordre');
        var val = parseInt(input.value) || 2;
        if (val > 1) {
            input.value = val - 1;
            input.dispatchEvent(new Event('input'));
        }
    }

    // ==========================================================
    // VALIDATION AVANT SOUMISSION
    // ==========================================================
    document.getElementById('formThemeCreer').addEventListener('submit', function(e) {
        var nom = document.getElementById('nom');
        var couleur = document.getElementById('couleur');
        var nomError = document.getElementById('nomError');
        var hasError = false;

        // Réinitialiser les erreurs
        nomError.textContent = '';
        nom.classList.remove('atc-input-error');

        // Validation du nom
        if (!nom.value.trim()) {
            nomError.textContent = 'Le nom du thème est obligatoire.';
            nom.classList.add('atc-input-error');
            nom.focus();
            hasError = true;
        } else if (nom.value.trim().length < 2) {
            nomError.textContent = 'Le nom doit contenir au moins 2 caractères.';
            nom.classList.add('atc-input-error');
            nom.focus();
            hasError = true;
        }

        // Validation de la couleur
        if (!couleur.value.trim().match(/^[0-9a-fA-F]{6}$/)) {
            couleur.classList.add('atc-input-error');
            couleur.focus();
            hasError = true;
        } else {
            couleur.classList.remove('atc-input-error');
        }

        if (hasError) {
            e.preventDefault();
        }
    });

    // ==========================================================
    // INITIALISATION DES COMPTEURS
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        // Déclencher les événements initiaux
        document.getElementById('nom').dispatchEvent(new Event('input'));
        document.getElementById('description').dispatchEvent(new Event('input'));
        document.getElementById('ordre').dispatchEvent(new Event('input'));
    });
</script>

<%@ include file="../footer-connecte.jsp" %>