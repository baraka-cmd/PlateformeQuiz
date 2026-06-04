<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-lecon-creer.jsp
     RÔLE    : Formulaire de création/modification d'une leçon
              - Mode création ou modification
              - Éditeur de texte avec barre d'outils (gras, italique, titres, listes, code, lien)
              - Zone de code source séparée
              - Prévisualisation en temps réel (onglets Éditer / Aperçu)
              - Type d'accès : Gratuit / Premium
              - Ordre auto-suggéré
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    Chapitre chapitre = (Chapitre) request.getAttribute("chapitre");
    Lecon lecon = (Lecon) request.getAttribute("lecon");
    Integer ordreSuggere = (Integer) request.getAttribute("ordreSuggere");
    Boolean modeModification = (Boolean) request.getAttribute("modeModification");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (chapitre == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (modeModification == null) modeModification = false;
    if (lecon == null) lecon = new Lecon();
    if (ordreSuggere == null) ordreSuggere = 1;

    int chapitreId = chapitre.getId();
    int coursId = cours != null ? cours.getId() : chapitre.getCoursId();
    int leconId = lecon.getId();

    String titrePage = modeModification ? "Modifier la leçon" : "Créer une leçon";
    String titreLecon = lecon.getTitre() != null ? lecon.getTitre() : "";
    String contenuTexte = lecon.getContenuTexte() != null ? lecon.getContenuTexte() : "";
    String codeSource = lecon.getCodeSource() != null ? lecon.getCodeSource() : "";
    int ordreLecon = modeModification ? lecon.getOrdre() : ordreSuggere;
    boolean estGratuit = modeModification ? lecon.isGratuit() : true;

    request.setAttribute("pageTitle", titrePage + " - " + chapitre.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-lecon-creer.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-cours-lecon-creer.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<div class="acle-container">

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
        <a href="${pageContext.request.contextPath}/admin/cours/lecons?chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" class="breadcrumb-link">
            <i class="fas fa-file-alt"></i><span>Leçons</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas <%= modeModification ? "fa-edit" : "fa-plus-circle" %>"></i>
            <span><%= titrePage %></span>
        </span>
    </nav>

    <%-- ===== MESSAGE D'ERREUR ===== --%>
    <% if (messageErreur != null && !messageErreur.isEmpty()) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i><span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== FORMULAIRE ===== --%>
    <form action="${pageContext.request.contextPath}/admin/cours/lecons/<%= modeModification ? "modifier" : "creer" %>" method="POST" id="formLecon" novalidate>
        <input type="hidden" name="chapitreId" value="<%= chapitreId %>">
        <input type="hidden" name="coursId" value="<%= coursId %>">
        <% if (modeModification) { %>
            <input type="hidden" name="leconId" value="<%= leconId %>">
        <% } %>

        <%-- ===== LAYOUT 2 COLONNES ===== --%>
        <div class="acle-layout">

            <%-- ===== COLONNE GAUCHE : CONTENU PRINCIPAL ===== --%>
            <div class="acle-col-main">

                <%-- ===== CARTE 1 : INFORMATIONS GÉNÉRALES ===== --%>
                <div class="acle-card">
                    <div class="acle-card-header">
                        <div class="acle-card-header-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                            <i class="fas fa-info-circle"></i>
                        </div>
                        <div class="acle-card-header-text">
                            <h2>Informations générales</h2>
                            <p>Définissez le titre et les métadonnées de la leçon</p>
                        </div>
                    </div>
                    <div class="acle-card-body">

                        <%-- Titre + Ordre + Accès sur une ligne --%>
                        <div class="acle-form-row acle-form-row-3col">
                            <div class="acle-form-group acle-form-group-titre">
                                <label for="titre" class="acle-label">
                                    <i class="fas fa-heading"></i><span>Titre</span><span class="acle-required">*</span>
                                </label>
                                <input type="text" id="titre" name="titre" class="acle-input"
                                       value="<%= titreLecon %>" placeholder="Titre de la leçon"
                                       maxlength="200" required autofocus>
                                <span class="acle-error" id="titreError"></span>
                            </div>

                            <div class="acle-form-group acle-form-group-ordre">
                                <label for="ordre" class="acle-label">
                                    <i class="fas fa-sort-numeric-down"></i><span>Ordre</span>
                                </label>
                                <input type="number" id="ordre" name="ordre" class="acle-input"
                                       value="<%= ordreLecon %>" min="1" max="100">
                            </div>

                            <div class="acle-form-group acle-form-group-acces">
                                <label class="acle-label">
                                    <i class="fas fa-lock-open"></i><span>Accès</span>
                                </label>
                                <div class="acle-acces-options">
                                    <label class="acle-acces-option <%= estGratuit ? "active" : "" %>">
                                        <input type="radio" name="acces" value="gratuit" <%= estGratuit ? "checked" : "" %>>
                                        <i class="fas fa-check-circle"></i>
                                        <span>Gratuit</span>
                                    </label>
                                    <label class="acle-acces-option <%= !estGratuit ? "active" : "" %>">
                                        <input type="radio" name="acces" value="premium" <%= !estGratuit ? "checked" : "" %>>
                                        <i class="fas fa-crown"></i>
                                        <span>Premium</span>
                                    </label>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>

                <%-- ===== CARTE 2 : ÉDITEUR DE CONTENU ===== --%>
                <div class="acle-card">
                    <div class="acle-card-header">
                        <div class="acle-card-header-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                            <i class="fas fa-pen-fancy"></i>
                        </div>
                        <div class="acle-card-header-text">
                            <h2>Contenu de la leçon</h2>
                            <p>Rédigez le contenu texte avec mise en forme</p>
                        </div>
                        <%-- Onglets Éditer / Aperçu --%>
                        <div class="acle-editor-tabs">
                            <button type="button" class="acle-tab active" data-tab="editor" onclick="switchTab('editor')">
                                <i class="fas fa-edit"></i> Éditer
                            </button>
                            <button type="button" class="acle-tab" data-tab="preview" onclick="switchTab('preview')">
                                <i class="fas fa-eye"></i> Aperçu
                            </button>
                        </div>
                    </div>
                    <div class="acle-card-body">

                        <%-- Barre d'outils de l'éditeur --%>
                        <div class="acle-toolbar-editor" id="editorToolbar">
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('strong')" title="Gras">
                                <i class="fas fa-bold"></i>
                            </button>
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('em')" title="Italique">
                                <i class="fas fa-italic"></i>
                            </button>
                            <span class="acle-toolbar-sep"></span>
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('h3')" title="Titre de section">
                                <i class="fas fa-heading"></i>
                            </button>
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('p')" title="Paragraphe">
                                <i class="fas fa-paragraph"></i>
                            </button>
                            <span class="acle-toolbar-sep"></span>
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('ul')" title="Liste à puces">
                                <i class="fas fa-list-ul"></i>
                            </button>
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('ol')" title="Liste numérotée">
                                <i class="fas fa-list-ol"></i>
                            </button>
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('li')" title="Élément de liste">
                                <i class="fas fa-circle"></i>
                            </button>
                            <span class="acle-toolbar-sep"></span>
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('pre')" title="Bloc de code">
                                <i class="fas fa-code"></i>
                            </button>
                            <button type="button" class="acle-toolbar-btn" onclick="insertTag('a')" title="Lien">
                                <i class="fas fa-link"></i>
                            </button>
                        </div>

                        <%-- Zone d'édition --%>
                        <div class="acle-editor-area" id="editorArea">
                            <textarea id="contenuTexte" name="contenuTexte" class="acle-textarea-editor"
                                      placeholder="<h3>Titre de section</h3><p>Rédigez votre contenu ici...</p><p>Utilisez la barre d'outils ci-dessus pour insérer des balises HTML.</p>"
                                      rows="16"><%= contenuTexte %></textarea>
                        </div>

                        <%-- Zone d'aperçu --%>
                        <div class="acle-preview-area" id="previewArea" style="display: none;">
                            <div class="acle-preview-content" id="previewContent">
                                <%= contenuTexte.isEmpty() ? "<p style='color: #8b8fa3; font-style: italic;'>L'aperçu du contenu apparaîtra ici...</p>" : contenuTexte %>
                            </div>
                        </div>

                    </div>
                </div>

                <%-- ===== CARTE 3 : CODE SOURCE ===== --%>
                <div class="acle-card">
                    <div class="acle-card-header">
                        <div class="acle-card-header-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                            <i class="fas fa-code"></i>
                        </div>
                        <div class="acle-card-header-text">
                            <h2>Code source d'exemple</h2>
                            <p>Ajoutez un exemple de code que les apprenants pourront copier</p>
                        </div>
                    </div>
                    <div class="acle-card-body">
                        <textarea id="codeSource" name="codeSource" class="acle-textarea-code"
                                  placeholder="// Collez votre code source ici...
// Les apprenants pourront le copier-coller depuis la plateforme."
                                  rows="12" spellcheck="false"><%= codeSource %></textarea>
                        <span class="acle-hint">
                            <i class="fas fa-info-circle"></i> Ce code sera affiché avec coloration syntaxique dans un bloc prêt à copier.
                        </span>
                    </div>
                </div>

                <%-- ===== BOUTONS ===== --%>
                <div class="acle-form-actions">
                    <a href="${pageContext.request.contextPath}/admin/cours/lecons?chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" 
                       class="acle-btn acle-btn-secondary">
                        <i class="fas fa-arrow-left"></i><span>Annuler</span>
                    </a>
                    <% if (modeModification) { %>
                        <a href="${pageContext.request.contextPath}/admin/cours/lecons/apercu?id=<%= leconId %>" 
                           class="acle-btn acle-btn-preview" target="_blank">
                            <i class="fas fa-eye"></i><span>Aperçu</span>
                        </a>
                    <% } %>
                    <button type="submit" class="acle-btn acle-btn-primary" id="btnEnregistrer">
                        <i class="fas fa-save"></i><span><%= modeModification ? "Enregistrer" : "Créer la leçon" %></span>
                    </button>
                </div>

            </div>

            <%-- ===== COLONNE DROITE : RÉSUMÉ + GUIDE ===== --%>
            <div class="acle-col-sidebar">

                <div class="acle-card">
                    <div class="acle-card-header acle-card-header-sm">
                        <div class="acle-card-header-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                            <i class="fas fa-info-circle"></i>
                        </div>
                        <div class="acle-card-header-text"><h2>Résumé</h2></div>
                    </div>
                    <div class="acle-card-body">
                        <div style="display: flex; flex-direction: column; gap: 12px;">
                            <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                                <span style="font-size: 12px; color: #555;"><i class="fas fa-bookmark" style="color: #667eea; margin-right: 6px;"></i>Chapitre</span>
                                <span style="font-weight: 600; font-size: 12px; color: #1a1a2e; text-align: right;"><%= chapitre.getTitre() %></span>
                            </div>
                            <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                                <span style="font-size: 12px; color: #555;"><i class="fas fa-sort-numeric-down" style="color: #667eea; margin-right: 6px;"></i>Ordre</span>
                                <span style="font-weight: 700; font-size: 14px; color: #1a1a2e;" id="previewOrdre"><%= ordreLecon %></span>
                            </div>
                            <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                                <span style="font-size: 12px; color: #555;"><i class="fas fa-lock-open" style="color: #667eea; margin-right: 6px;"></i>Accès</span>
                                <span style="font-weight: 700; font-size: 14px; color: #1a1a2e;" id="previewAcces"><%= estGratuit ? "Gratuit" : "Premium" %></span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="acle-card acle-card-guide">
                    <div class="acle-card-header acle-card-header-sm">
                        <div class="acle-card-header-icon" style="background: linear-gradient(135deg, #2980b9, #3498db);">
                            <i class="fas fa-lightbulb"></i>
                        </div>
                        <div class="acle-card-header-text"><h2>Guide</h2></div>
                    </div>
                    <div class="acle-card-body">
                        <ul class="acle-guide-list">
                            <li><i class="fas fa-check-circle"></i><span><strong>Contenu texte :</strong> Utilisez des balises HTML simples (&lt;p&gt;, &lt;h3&gt;, &lt;ul&gt;, &lt;li&gt;, &lt;strong&gt;, &lt;em&gt;, &lt;pre&gt;, &lt;a&gt;).</span></li>
                            <li><i class="fas fa-check-circle"></i><span><strong>Code source :</strong> Sera affiché dans un bloc de code coloré, prêt à copier.</span></li>
                            <li><i class="fas fa-check-circle"></i><span><strong>Aperçu :</strong> Utilisez l'onglet Aperçu pour vérifier le rendu avant d'enregistrer.</span></li>
                            <li><i class="fas fa-check-circle"></i><span><strong>Gratuit/Premium :</strong> Les leçons premium nécessitent un compte payant.</span></li>
                        </ul>
                    </div>
                </div>

            </div>
        </div>
    </form>
</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    // ===== SWITCH ÉDITER / APERÇU =====
    function switchTab(tab) {
        document.querySelectorAll('.acle-tab').forEach(function(t) { t.classList.remove('active'); });
        document.querySelector('.acle-tab[data-tab="' + tab + '"]').classList.add('active');

        if (tab === 'editor') {
            document.getElementById('editorArea').style.display = 'block';
            document.getElementById('editorToolbar').style.display = 'flex';
            document.getElementById('previewArea').style.display = 'none';
        } else {
            document.getElementById('editorArea').style.display = 'none';
            document.getElementById('editorToolbar').style.display = 'none';
            document.getElementById('previewArea').style.display = 'block';
            document.getElementById('previewContent').innerHTML = document.getElementById('contenuTexte').value || "<p style='color: #8b8fa3; font-style: italic;'>L'aperçu du contenu apparaîtra ici...</p>";
        }
    }

    // ===== INSERTION DE BALISES HTML =====
    function insertTag(tag) {
        var textarea = document.getElementById('contenuTexte');
        var start = textarea.selectionStart;
        var end = textarea.selectionEnd;
        var selectedText = textarea.value.substring(start, end);
        var replacement = '';

        switch (tag) {
            case 'strong': replacement = '<strong>' + (selectedText || 'texte en gras') + '</strong>'; break;
            case 'em': replacement = '<em>' + (selectedText || 'texte en italique') + '</em>'; break;
            case 'h3': replacement = '<h3>' + (selectedText || 'Titre de section') + '</h3>\n'; break;
            case 'p': replacement = '<p>' + (selectedText || 'Nouveau paragraphe') + '</p>\n'; break;
            case 'ul': replacement = '<ul>\n  <li>' + (selectedText || 'Élément 1') + '</li>\n  <li>Élément 2</li>\n</ul>\n'; break;
            case 'ol': replacement = '<ol>\n  <li>' + (selectedText || 'Élément 1') + '</li>\n  <li>Élément 2</li>\n</ol>\n'; break;
            case 'li': replacement = '<li>' + (selectedText || 'Élément') + '</li>\n'; break;
            case 'pre': replacement = '<pre><code>' + (selectedText || '// Votre code ici') + '</code></pre>\n'; break;
            case 'a': replacement = '<a href="' + (selectedText || '#') + '">' + (selectedText || 'Texte du lien') + '</a>'; break;
            default: replacement = selectedText;
        }

        textarea.value = textarea.value.substring(0, start) + replacement + textarea.value.substring(end);
        textarea.focus();
        textarea.selectionStart = start + replacement.length;
        textarea.selectionEnd = start + replacement.length;
    }

    // ===== PRÉVISUALISATION ORDRE ET ACCÈS =====
    document.getElementById('ordre').addEventListener('input', function() {
        document.getElementById('previewOrdre').textContent = this.value || '—';
    });

    document.querySelectorAll('input[name="acces"]').forEach(function(radio) {
        radio.addEventListener('change', function() {
            document.getElementById('previewAcces').textContent = this.value === 'gratuit' ? 'Gratuit' : 'Premium';
            document.querySelectorAll('.acle-acces-option').forEach(function(opt) { opt.classList.remove('active'); });
            this.closest('.acle-acces-option').classList.add('active');
        });
    });

    // ===== VALIDATION =====
    document.getElementById('formLecon').addEventListener('submit', function(e) {
        var titre = document.getElementById('titre');
        var hasError = false;
        titre.classList.remove('acle-input-error');
        document.getElementById('titreError').textContent = '';

        if (!titre.value.trim()) {
            titre.classList.add('acle-input-error');
            document.getElementById('titreError').textContent = 'Le titre est obligatoire.';
            hasError = true;
        } else if (titre.value.trim().length < 2) {
            titre.classList.add('acle-input-error');
            document.getElementById('titreError').textContent = 'Minimum 2 caractères.';
            hasError = true;
        }

        if (hasError) {
            e.preventDefault();
        } else {
            var btn = document.getElementById('btnEnregistrer');
            if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Enregistrement...</span>'; }
        }
    });
</script>

<%@ include file="../footer-connecte.jsp" %>