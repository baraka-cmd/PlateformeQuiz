<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.learnpath.models.Cours" %>
<%@ page import="com.learnpath.models.Chapitre" %>
<%@ page import="com.learnpath.models.Lecon" %>
<%@ page import="com.learnpath.models.Ressource" %>
<%@ page import="java.util.List" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-lecon-apercu.jsp
     RÔLE    : Aperçu d'une leçon (vue admin)
              - Affiche la leçon comme l'apprenant la verra
              - Contenu texte formaté, code source, ressources
              - Badge "Mode aperçu" pour indiquer le contexte admin
              - Bouton retour à l'édition
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    Chapitre chapitre = (Chapitre) request.getAttribute("chapitre");
    Lecon lecon = (Lecon) request.getAttribute("lecon");

    if (lecon == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    int leconId = lecon.getId();
    int chapitreId = chapitre != null ? chapitre.getId() : lecon.getChapitreId();
    int coursId = cours != null ? cours.getId() : (chapitre != null ? chapitre.getCoursId() : 0);

    String titreLecon = lecon.getTitre() != null ? lecon.getTitre() : "Sans titre";
    String contenuTexte = lecon.getContenuTexte() != null ? lecon.getContenuTexte() : "";
    String codeSource = lecon.getCodeSource() != null ? lecon.getCodeSource() : "";
    String tempsLecture = lecon.getTempsLectureFormate();
    int nombreMots = lecon.getNombreMots();
    boolean hasCode = lecon.hasCodeSource();
    boolean hasRessources = lecon.hasRessources();
    boolean estGratuit = lecon.isGratuit();

    request.setAttribute("pageTitle", "Aperçu - " + titreLecon + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-lecon-apercu.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="acla-container">

    <%-- ===== BANDEAU MODE APERÇU ===== --%>
    <div class="acla-banner">
        <div class="acla-banner-left">
            <div class="acla-banner-icon">
                <i class="fas fa-eye"></i>
            </div>
            <div class="acla-banner-text">
                <strong>Mode aperçu</strong>
                <span>Vous visualisez cette leçon comme un apprenant. <a href="${pageContext.request.contextPath}/admin/cours/lecons/modifier?id=<%= leconId %>&chapitreId=<%= chapitreId %>&coursId=<%= coursId %>">Retourner à l'édition</a></span>
            </div>
        </div>
        <div class="acla-banner-actions">
            <a href="${pageContext.request.contextPath}/admin/cours/lecons/modifier?id=<%= leconId %>&chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" class="acla-btn-edit">
                <i class="fas fa-edit"></i><span>Modifier</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours/lecons?chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" class="acla-btn-back">
                <i class="fas fa-arrow-left"></i><span>Retour aux leçons</span>
            </a>
        </div>
    </div>

    <%-- ===== CONTENU DE LA LEÇON (VUE APPRENANT) ===== --%>
    <div class="acla-content">

        <%-- En-tête de la leçon --%>
        <div class="acla-header">
            <div class="acla-header-badges">
                <span class="acla-badge-cours" style="background: <%= cours != null ? cours.getCouleurTheme() : "#667eea" %>;">
                    <i class="<%= cours != null ? cours.getIconeTheme() : "fas fa-book" %>"></i>
                    <%= cours != null ? cours.getTitre() : "Cours" %>
                </span>
                <span class="acla-badge-chapitre">
                    <i class="fas fa-bookmark"></i> <%= chapitre != null ? chapitre.getTitre() : "Chapitre" %>
                </span>
                <% if (!estGratuit) { %>
                    <span class="acla-badge-premium">
                        <i class="fas fa-crown"></i> Premium
                    </span>
                <% } %>
            </div>
            <h1 class="acla-titre"><%= titreLecon %></h1>
            <div class="acla-meta">
                <span><i class="fas fa-book-reader"></i> <%= tempsLecture %> de lecture</span>
                <span class="acla-meta-sep">•</span>
                <span><i class="fas fa-font"></i> <%= nombreMots %> mots</span>
                <% if (hasCode) { %>
                    <span class="acla-meta-sep">•</span>
                    <span><i class="fas fa-code"></i> Exemple de code inclus</span>
                <% } %>
            </div>
        </div>

        <%-- Contenu texte --%>
        <div class="acla-texte">
            <% if (contenuTexte != null && !contenuTexte.isEmpty()) { %>
                <%= contenuTexte %>
            <% } else { %>
                <div class="acla-empty-texte">
                    <i class="fas fa-pen-fancy"></i>
                    <h3>Aucun contenu texte</h3>
                    <p>Cette leçon n'a pas encore de contenu rédigé.</p>
                </div>
            <% } %>
        </div>

        <%-- Code source --%>
        <% if (hasCode) { %>
            <div class="acla-code-section">
                <div class="acla-code-header">
                    <i class="fas fa-code"></i>
                    <span>Exemple de code</span>
                    <span class="acla-code-badge">Prêt à copier-coller</span>
                </div>
                <pre class="acla-code-block"><code><%= codeSource.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") %></code></pre>
            </div>
        <% } %>

        <%-- Ressources --%>
        <% if (hasRessources) { %>
            <div class="acla-ressources-section">
                <h3><i class="fas fa-paperclip"></i> Ressources téléchargeables</h3>
                <div class="acla-ressources-grid">
                    <% for (Ressource ressource : lecon.getRessources()) { %>
                        <a href="<%= ressource.getUrlFichier() %>" class="acla-ressource-card" download>
                            <div class="acla-ressource-icon" style="background: <%= ressource.getCouleurType() %>;">
                                <i class="<%= ressource.getIcone() %>"></i>
                            </div>
                            <div class="acla-ressource-info">
                                <span class="acla-ressource-nom"><%= ressource.getTitre() %></span>
                                <% if (ressource.getTaille() != null) { %>
                                    <span class="acla-ressource-taille"><%= ressource.getTaille() %></span>
                                <% } %>
                            </div>
                            <i class="fas fa-download acla-ressource-download"></i>
                        </a>
                    <% } %>
                </div>
            </div>
        <% } %>

    </div>

    <%-- ===== BOUTONS RETOUR ===== --%>
    <div class="acla-back">
        <a href="${pageContext.request.contextPath}/admin/cours/lecons/modifier?id=<%= leconId %>&chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" class="acla-btn-edit-bottom">
            <i class="fas fa-edit"></i><span>Modifier cette leçon</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/cours/lecons?chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" class="acla-btn-back-bottom">
            <i class="fas fa-arrow-left"></i><span>Retour aux leçons</span>
        </a>
    </div>

</div>

<%@ include file="../footer-connecte.jsp" %>