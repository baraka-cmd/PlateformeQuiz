<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.learnpath.models.Cours" %>
<%@ page import="com.learnpath.models.ExamenModule" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-module-examen.jsp
     RÔLE    : Formulaire de création/modification d'un examen de module
              - Mode création (depuis la grille des modules)
              - Mode modification (depuis un examen existant)
              - Configuration : titre, description, nb questions, seuil
              - Navigation vers les questions après création
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    ExamenModule examen = (ExamenModule) request.getAttribute("examen");
    Integer moduleNumero = (Integer) request.getAttribute("moduleNumero");
    Boolean modeModification = (Boolean) request.getAttribute("modeModification");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (cours == null || examen == null || moduleNumero == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (modeModification == null) modeModification = false;

    int coursId = cours.getId();
    String titrePage = modeModification ? "Modifier l'examen" : "Créer l'examen";

    request.setAttribute("pageTitle", titrePage + " - Module " + moduleNumero + " - " + cours.getTitre() + " - Administration");
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
        <a href="${pageContext.request.contextPath}/admin/cours/modules?coursId=<%= coursId %>" class="breadcrumb-link">
            <i class="fas fa-cubes"></i><span>Modules</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas <%= modeModification ? "fa-edit" : "fa-plus-circle" %>"></i>
            <span><%= titrePage %> - Module <%= moduleNumero %></span>
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

            <form action="${pageContext.request.contextPath}/admin/cours/modules/<%= modeModification ? "modifier" : "creer" %>" method="POST" id="formExamen" novalidate>
                <input type="hidden" name="modeModification" value="<%= modeModification %>">
                <input type="hidden" name="coursId" value="<%= coursId %>">
                <input type="hidden" name="module" value="<%= moduleNumero %>">
                <% if (modeModification) { %>
                    <input type="hidden" name="examenId" value="<%= examen.getId() %>">
                <% } %>

                <%-- ===== CARTE : CONFIGURATION DE L'EXAMEN ===== --%>
                <div class="acc-card">
                    <div class="acc-card-header">
                        <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                            <i class="fas fa-clipboard-check"></i>
                        </div>
                        <div class="acc-card-header-text">
                            <h2><%= titrePage %> - Module <%= moduleNumero %></h2>
                            <p>Cours : <%= cours.getTitre() %></p>
                        </div>
                    </div>
                    <div class="acc-card-body">

                        <%-- Titre de l'examen --%>
                        <div class="acc-form-group">
                            <label for="titre" class="acc-label">
                                <i class="fas fa-heading"></i><span>Titre de l'examen</span>
                            </label>
                            <input type="text" id="titre" name="titre" class="acc-input"
                                   value="<%= examen.getTitre() != null ? examen.getTitre() : "Examen Module " + moduleNumero %>"
                                   placeholder="Ex: Examen Module <%= moduleNumero %> : Bases de données"
                                   maxlength="200" required autofocus>
                        </div>

                        <%-- Description --%>
                        <div class="acc-form-group">
                            <label for="description" class="acc-label">
                                <i class="fas fa-align-left"></i><span>Description / Consignes</span><span class="acc-optional">(optionnel)</span>
                            </label>
                            <textarea id="description" name="description" class="acc-textarea" rows="2"
                                      maxlength="300" placeholder="Consignes pour l'examen..."><%= examen.getDescription() != null ? examen.getDescription() : "" %></textarea>
                        </div>

                        <%-- Nombre de questions + Seuil côte à côte --%>
                        <div class="acc-form-row acc-form-row-2col">
                            <div class="acc-form-group">
                                <label for="nombreQuestions" class="acc-label">
                                    <i class="fas fa-question-circle"></i><span>Nombre de questions</span>
                                </label>
                                <input type="number" id="nombreQuestions" name="nombreQuestions" class="acc-input"
                                       value="<%= examen.getNombreQuestions() %>" min="5" max="50" required>
                                <span class="acc-hint"><i class="fas fa-info-circle"></i> Entre 5 et 50 questions (défaut : 20)</span>
                            </div>

                            <div class="acc-form-group">
                                <label for="seuilReussite" class="acc-label">
                                    <i class="fas fa-flag-checkered"></i><span>Seuil de réussite (%)</span>
                                </label>
                                <input type="number" id="seuilReussite" name="seuilReussite" class="acc-input"
                                       value="<%= examen.getSeuilReussite() %>" min="50" max="100" required>
                                <span class="acc-hint"><i class="fas fa-info-circle"></i> Pourcentage minimum pour valider (défaut : 75%)</span>
                            </div>
                        </div>

                    </div>
                </div>

                <%-- ===== BOUTONS D'ACTION ===== --%>
                <div class="acc-form-actions">
                    <a href="${pageContext.request.contextPath}/admin/cours/modules?coursId=<%= coursId %>" class="acc-btn acc-btn-secondary">
                        <i class="fas fa-arrow-left"></i><span>Annuler</span>
                    </a>

                    <% if (modeModification && examen.getId() > 0) { %>
                        <a href="${pageContext.request.contextPath}/admin/cours/modules/questions?examenId=<%= examen.getId() %>&coursId=<%= coursId %>" 
                           class="acc-btn" style="background: #f3e5f5; color: #9b59b6; border: 1px solid #e1bee7;">
                            <i class="fas fa-list-ul"></i><span>Gérer les questions</span>
                        </a>
                    <% } %>

                    <button type="submit" class="acc-btn acc-btn-primary" id="btnEnregistrerExamen">
                        <i class="fas fa-save"></i><span><%= modeModification ? "Enregistrer" : "Créer l'examen" %></span>
                    </button>
                </div>

            </form>
        </div>

        <%-- ===== COLONNE DROITE : RÉSUMÉ + GUIDE ===== --%>
        <div class="acc-col-sidebar">

            <%-- ===== RÉSUMÉ ===== --%>
            <div class="acc-card">
                <div class="acc-card-header acc-card-header-sm">
                    <div class="acc-card-header-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                        <i class="fas fa-eye"></i>
                    </div>
                    <div class="acc-card-header-text"><h2>Résumé</h2></div>
                </div>
                <div class="acc-card-body">
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-cube" style="color: #667eea; margin-right: 6px;"></i>Module</span>
                            <span style="font-weight: 700; font-size: 14px; color: #1a1a2e;"><%= moduleNumero %> / 15</span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-book" style="color: #667eea; margin-right: 6px;"></i>Cours</span>
                            <span style="font-weight: 600; font-size: 12px; color: #1a1a2e; text-align: right;"><%= cours.getTitre() %></span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-question-circle" style="color: #667eea; margin-right: 6px;"></i>Questions</span>
                            <span style="font-weight: 700; font-size: 14px; color: #1a1a2e;"><%= examen.getNombreQuestions() %></span>
                        </div>
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #fafbfe; border-radius: 10px;">
                            <span style="font-size: 12px; color: #555;"><i class="fas fa-flag-checkered" style="color: #667eea; margin-right: 6px;"></i>Score minimum</span>
                            <span style="font-weight: 700; font-size: 14px; color: #1a1a2e;"><%= examen.getSeuilReussite() %>%</span>
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
                        <li><i class="fas fa-check-circle"></i><span><strong>20 questions :</strong> C'est le format standard pour les examens de module.</span></li>
                        <li><i class="fas fa-check-circle"></i><span><strong>Seuil 75% :</strong> L'apprenant doit obtenir 15/20 pour valider le module.</span></li>
                        <li><i class="fas fa-check-circle"></i><span><strong>Questions après :</strong> Ajoutez les questions une fois l'examen créé.</span></li>
                        <li><i class="fas fa-check-circle"></i><span><strong>4 propositions :</strong> Chaque question aura 4 choix (A, B, C, D).</span></li>
                    </ul>
                </div>
            </div>

        </div>
    </div>
</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    document.getElementById('formExamen').addEventListener('submit', function(e) {
        var nbQuestions = document.getElementById('nombreQuestions');
        var seuil = document.getElementById('seuilReussite');
        var titre = document.getElementById('titre');
        var hasError = false;

        [nbQuestions, seuil, titre].forEach(function(el) { el.classList.remove('acc-input-error'); });

        if (!titre.value.trim()) {
            titre.classList.add('acc-input-error');
            hasError = true;
        }

        var nb = parseInt(nbQuestions.value);
        if (isNaN(nb) || nb < 5 || nb > 50) {
            nbQuestions.classList.add('acc-input-error');
            hasError = true;
        }

        var s = parseInt(seuil.value);
        if (isNaN(s) || s < 50 || s > 100) {
            seuil.classList.add('acc-input-error');
            hasError = true;
        }

        if (hasError) {
            e.preventDefault();
        } else {
            var btn = document.getElementById('btnEnregistrerExamen');
            if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Enregistrement...</span>'; }
        }
    });
</script>

<%@ include file="../footer-connecte.jsp" %>