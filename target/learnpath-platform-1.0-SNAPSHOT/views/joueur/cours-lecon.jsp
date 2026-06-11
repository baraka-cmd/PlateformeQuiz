<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.learnpath.models.Cours" %>
<%@ page import="com.learnpath.models.Chapitre" %>
<%@ page import="com.learnpath.models.Lecon" %>
<%@ page import="com.learnpath.models.ProgressionCours" %>
<%@ page import="com.learnpath.models.Ressource" %>
<%@ page import="com.learnpath.models.ExamenModule" %>
<%@ page import="com.learnpath.models.ProgressionModule" %>

<%-- ============================================================
     FICHIER : views/joueur/cours-lecon.jsp
     RÔLE    : Page de contenu d'une lecon de cours LearnPath
              - Explications texte enrichi (HTML)
              - Exemples de code prets a copier-coller
              - Ressources telechargeables
              - Navigation precedente/suivante
              - Sidebar avec sommaire du chapitre
              - Modales pour le plan du cours et la vue du chapitre
              - Barre de progression de lecture
              - Validation de la lecon
     ============================================================ --%>

<%
    // ===== RECUPERATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    Chapitre chapitre = (Chapitre) request.getAttribute("chapitre");
    Lecon lecon = (Lecon) request.getAttribute("lecon");
    ProgressionCours progression = (ProgressionCours) request.getAttribute("progression");
    Lecon leconPrecedente = (Lecon) request.getAttribute("leconPrecedente");
    Lecon leconSuivante = (Lecon) request.getAttribute("leconSuivante");
    Boolean estDerniereLecon = (Boolean) request.getAttribute("estDerniereLecon");
    Integer totalLeconsChapitre = (Integer) request.getAttribute("totalLeconsChapitre");
    Boolean moduleDebloque = (Boolean) request.getAttribute("moduleDebloque");
    Boolean moduleReussi = (Boolean) request.getAttribute("moduleReussi");
    Integer moduleActiveNumero = (Integer) request.getAttribute("moduleActiveNumero");
    List<Map<String, Object>> modules = (List<Map<String, Object>>) request.getAttribute("modules");
    Integer moduleSuivant = (Integer) request.getAttribute("moduleSuivant");
    Boolean tousChapitresTermines = (Boolean) request.getAttribute("tousChapitresTermines");

    // ===== VERIFICATION SECURITE =====
    if (cours == null || chapitre == null || lecon == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    // ===== VALEURS PAR DEFAUT =====
    boolean estTerminee = (progression != null && progression.isTermine());
    int leconActive = lecon.getOrdre();
    if (estDerniereLecon == null) estDerniereLecon = false;
    if (totalLeconsChapitre == null) totalLeconsChapitre = 0;
    if (moduleDebloque == null) moduleDebloque = false;
    if (moduleReussi == null) moduleReussi = false;
    if (moduleActiveNumero == null) moduleActiveNumero = 1;
    if (moduleSuivant == null) moduleSuivant = 0;
    if (tousChapitresTermines == null) tousChapitresTermines = false;

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");

    // Temps de lecture
    String tempsLectureFormate = lecon.getTempsLectureFormate();
    int nombreMots = lecon.getNombreMots();

    request.setAttribute("pageTitle", lecon.getTitre() + " - " + cours.getTitre() + " - LearnPath");
%>

<%
    String coursLeconCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-lecon.css\">";
    request.setAttribute("extraHead", coursLeconCSS);
%>

<%@ include file="../header.jsp" %>

<div class="lecon-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="breadcrumb-link">
            <span><%= cours.getTheme() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" class="breadcrumb-link">
            <span><%= cours.getNiveauFormate() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="breadcrumb-link">
            <i class="fas fa-sitemap"></i><span>Plan</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/chapitre?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>" class="breadcrumb-link">
            <i class="fas fa-bookmark"></i><span><%= chapitre.getTitre() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-file-alt"></i><span>Lecon <%= leconActive %></span>
        </span>
    </nav>

    <%-- ============================================================
         LAYOUT 2 COLONNES
         ============================================================ --%>
    <div class="lecon-layout">

        <%-- ============================================================
             COLONNE PRINCIPALE : CONTENU DE LA LECON
             ============================================================ --%>
        <div class="lecon-main">

            <%-- En-tete de la lecon --%>
            <div class="lecon-header">
                <div class="lecon-header-badges">
                    <span class="lecon-badge-cours" style="background: <%= cours.getCouleurTheme() %>;">
                        <i class="<%= cours.getIconeTheme() %>"></i> <%= cours.getTitre() %>
                    </span>
                    <span class="lecon-badge-chapitre">
                        <i class="fas fa-bookmark"></i> <%= chapitre.getTitre() %>
                    </span>
                    <span class="lecon-badge-module">
                        <i class="fas fa-cube"></i> Module <%= moduleActiveNumero %>
                    </span>
                    <% if (!lecon.isGratuit()) { %>
                        <span class="lecon-badge-premium">
                            <i class="fas fa-crown"></i> Premium
                        </span>
                    <% } %>
                </div>
                <h1 class="lecon-titre"><%= lecon.getTitre() %></h1>
                <div class="lecon-header-meta">
                    <span class="lecon-meta-item">
                        <i class="fas fa-list-ol"></i> Lecon <%= leconActive %> sur <%= totalLeconsChapitre %>
                    </span>
                    <span class="lecon-meta-item">
                        <i class="fas fa-book-reader"></i> <%= tempsLectureFormate %> de lecture
                    </span>
                    <span class="lecon-meta-item">
                        <i class="fas fa-font"></i> <%= nombreMots %> mots
                    </span>
                    <% if (lecon.hasCodeSource()) { %>
                        <span class="lecon-meta-item">
                            <i class="fas fa-code"></i> Exemple de code inclus
                        </span>
                    <% } %>
                    <% if (estTerminee) { %>
                        <span class="lecon-meta-item termine">
                            <i class="fas fa-check-circle"></i> Terminee
                        </span>
                    <% } %>
                </div>
            </div>

            <%-- ===== CONTENU TEXTE : EXPLICATIONS ===== --%>
            <% if (lecon.hasContenuTexte()) { %>
                <div class="lecon-texte">
                    <%= lecon.getContenuTexte() %>
                </div>
            <% } else { %>
                <div class="lecon-texte-vide">
                    <div class="empty-icon">
                        <i class="fas fa-pen-fancy"></i>
                    </div>
                    <h3>Contenu en cours de redaction</h3>
                    <p>Le contenu detaille de cette lecon sera bientot disponible.</p>
                </div>
            <% } %>

            <%-- ===== CODE SOURCE ===== --%>
            <% if (lecon.hasCodeSource()) { %>
                <div class="lecon-code">
                    <div class="code-header">
                        <div class="code-header-left">
                            <i class="fas fa-code"></i>
                            <span class="code-title">Exemple de code</span>
                            <span class="code-badge">Pret a copier-coller</span>
                        </div>
                        <div class="code-header-right">
                            <button type="button" class="code-copy-btn" onclick="copierCode()" title="Copier le code dans le presse-papier">
                                <i class="fas fa-copy"></i>
                                <span>Copier le code</span>
                            </button>
                            <button type="button" class="code-expand-btn" onclick="toggleCodeExpand()" title="Afficher en plein ecran">
                                <i class="fas fa-expand"></i>
                            </button>
                        </div>
                    </div>
                    <pre class="code-block" id="codeBlock"><code><%= lecon.getCodeSource().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") %></code></pre>
                    <div class="code-footer">
                        <span class="code-langage"><i class="fas fa-lightbulb"></i> Conseil : Copiez ce code et testez-le dans votre environnement de developpement.</span>
                    </div>
                </div>
            <% } %>

            <%-- ===== RESSOURCES ===== --%>
            <% if (lecon.hasRessources()) { %>
                <div class="lecon-ressources">
                    <h3 class="ressources-title">
                        <i class="fas fa-paperclip"></i> Ressources telechargeables
                    </h3>
                    <div class="ressources-grid">
                        <% for (Ressource ressource : lecon.getRessources()) { %>
                            <a href="<%= ressource.getUrlFichier() %>"
                               class="ressource-card"
                               download
                               title="Telecharger <%= ressource.getTitre() %>">
                                <div class="ressource-icon" style="background: <%= ressource.getCouleurType() %>;">
                                    <i class="<%= ressource.getIcone() %>"></i>
                                </div>
                                <div class="ressource-info">
                                    <span class="ressource-nom"><%= ressource.getTitre() %></span>
                                    <% if (ressource.getTaille() != null) { %>
                                        <span class="ressource-taille"><%= ressource.getTaille() %></span>
                                    <% } %>
                                </div>
                                <i class="fas fa-download ressource-download"></i>
                            </a>
                        <% } %>
                    </div>
                </div>
            <% } %>

            <%-- ===== MESSAGE DE FIN DE LECON ===== --%>
            <div class="lecon-fin-message">
                <div class="fin-message-icon">
                    <i class="fas fa-flag-checkered"></i>
                </div>
                <div class="fin-message-content">
                    <h4>
                        <% if (estTerminee) { %>
                            <i class="fas fa-check-circle" style="color: #27ae60;"></i> Lecon terminee !
                        <% } else { %>
                            <i class="fas fa-book-open"></i> Fin de la lecon
                        <% } %>
                    </h4>
                    <p>
                        <% if (estTerminee) { %>
                            Vous avez deja valide cette lecon. Passez a la suite !
                        <% } else if (leconSuivante != null) { %>
                            Pret a passer a la lecon suivante ? Cliquez sur le bouton ci-dessous.
                        <% } else { %>
                            Derniere lecon de ce module ! Validez pour continuer votre progression.
                        <% } %>
                    </p>
                </div>
            </div>

            <%-- ===== NAVIGATION BAS ===== --%>
            <div class="lecon-navigation">
                <% if (leconPrecedente != null) { %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/lecon?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>&leconId=<%= leconPrecedente.getId() %>"
                       class="btn-nav-lecon precedent">
                        <i class="fas fa-arrow-left"></i>
                        <div class="btn-nav-text">
                            <span class="nav-label">Lecon precedente</span>
                            <span class="nav-titre"><%= leconPrecedente.getTitre() %></span>
                        </div>
                    </a>
                <% } else { %>
                    <div></div>
                <% } %>

                <% if (leconSuivante != null) { %>
                    <button type="button" class="btn-nav-lecon valider" onclick="validerLecon()">
                        <div class="btn-nav-text">
                            <span class="nav-label">Valider et continuer</span>
                            <span class="nav-titre"><%= leconSuivante.getTitre() %></span>
                        </div>
                        <i class="fas fa-check-circle"></i>
                    </button>
                <% } else if (estDerniereLecon && moduleSuivant > 0) { %>
                    <button type="button" class="btn-nav-lecon valider" onclick="validerLecon()">
                        <div class="btn-nav-text">
                            <span class="nav-label">Valider et continuer</span>
                            <span class="nav-titre">Module <%= moduleSuivant %></span>
                        </div>
                        <i class="fas fa-arrow-right"></i>
                    </button>
                <% } else if (estDerniereLecon && tousChapitresTermines) { %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/examen?coursId=<%= cours.getId() %>"
                       class="btn-nav-lecon examen">
                        <div class="btn-nav-text">
                            <span class="nav-label">Valider et passer l'examen final</span>
                            <span class="nav-titre">Obtenir le certificat</span>
                        </div>
                        <i class="fas fa-trophy"></i>
                    </a>
                <% } else { %>
                    <button type="button" class="btn-nav-lecon valider" onclick="validerLecon()">
                        <div class="btn-nav-text">
                            <span class="nav-label">Valider la lecon</span>
                            <span class="nav-titre">Terminer</span>
                        </div>
                        <i class="fas fa-check-circle"></i>
                    </button>
                <% } %>
            </div>

        </div>

        <%-- ============================================================
             COLONNE LATERALE : SOMMAIRE DU CHAPITRE
             ============================================================ --%>
        <aside class="lecon-sidebar" id="leconSidebar">

            <%-- Carte sommaire du chapitre --%>
            <div class="sidebar-card">
                <div class="sidebar-header">
                    <h3><i class="fas fa-list"></i> Sommaire du chapitre</h3>
                    <span class="sidebar-progress"><%= leconActive %>/<%= totalLeconsChapitre %></span>
                </div>

                <div class="sidebar-lecons">
                    <% if (chapitre.getLecons() != null) {
                        for (Lecon l : chapitre.getLecons()) {
                            boolean isActive = (l.getId() == lecon.getId());
                            boolean isTermineeLecon = progression != null && progression.isTermine() && l.getOrdre() <= leconActive;
                            boolean isAccessible = (l.getOrdre() <= leconActive) || (moduleDebloque || moduleReussi);
                    %>
                        <a href="${pageContext.request.contextPath}/joueur/cours/lecon?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>&leconId=<%= l.getId() %>"
                           class="sidebar-lecon <%= isActive ? "active" : "" %> <%= !isAccessible ? "verrouillee" : "" %>">
                            <span class="sidebar-lecon-numero">
                                <% if (isActive) { %>
                                    <i class="fas fa-play-circle"></i>
                                <% } else if (isTermineeLecon) { %>
                                    <i class="fas fa-check-circle"></i>
                                <% } else if (isAccessible) { %>
                                    <span><%= l.getOrdre() %></span>
                                <% } else { %>
                                    <i class="fas fa-lock"></i>
                                <% } %>
                            </span>
                            <span class="sidebar-lecon-titre"><%= l.getTitre() %></span>
                            <span class="sidebar-lecon-duree"><%= l.getTempsLectureFormate() %></span>
                        </a>
                    <%      }
                    } %>
                </div>
            </div>

            <%-- Carte infos lecon --%>
            <div class="sidebar-info-card">
                <h4><i class="fas fa-info-circle"></i> Informations</h4>
                <div class="sidebar-info-item">
                    <i class="fas fa-book-reader"></i>
                    <span><strong>Temps de lecture :</strong> <%= tempsLectureFormate %></span>
                </div>
                <div class="sidebar-info-item">
                    <i class="fas fa-font"></i>
                    <span><strong>Nombre de mots :</strong> <%= nombreMots %></span>
                </div>
                <% if (lecon.hasCodeSource()) { %>
                    <div class="sidebar-info-item">
                        <i class="fas fa-code"></i>
                        <span><strong>Exemple de code :</strong> Oui</span>
                    </div>
                <% } %>
                <% if (lecon.hasRessources()) { %>
                    <div class="sidebar-info-item">
                        <i class="fas fa-paperclip"></i>
                        <span><strong>Ressources :</strong> <%= lecon.getNombreRessources() %></span>
                    </div>
                <% } %>
                <div class="sidebar-info-item">
                    <i class="fas fa-check-circle"></i>
                    <span><strong>Statut :</strong> <%= estTerminee ? "Terminee" : "En cours" %></span>
                </div>
            </div>

            <%-- Navigation rapide (modales) --%>
            <div class="sidebar-nav">
                <button type="button" class="sidebar-nav-btn" onclick="ouvrirModalPlanCours()" title="Voir le plan complet du cours">
                    <i class="fas fa-sitemap"></i>
                    <span>Plan du cours</span>
                </button>
                <button type="button" class="sidebar-nav-btn" onclick="ouvrirModalVueChapitre()" title="Voir toutes les lecons du chapitre">
                    <i class="fas fa-bookmark"></i>
                    <span>Vue du chapitre</span>
                </button>
            </div>
        </aside>

    </div>

</div>

<%-- ============================================================
     MODALE : PLAN DU COURS
     ============================================================ --%>
<div class="modal-overlay" id="modalPlanCours" style="display: none;">
    <div class="modal-container modal-plan-cours">
        <div class="modal-header">
            <div class="modal-header-left">
                <div class="modal-header-icon" style="background: <%= cours.getCouleurTheme() %>;">
                    <i class="<%= cours.getIconeTheme() %>"></i>
                </div>
                <div class="modal-header-text">
                    <h2><%= cours.getTitre() %></h2>
                    <span class="modal-header-badge">
                        <i class="<%= cours.getIconeNiveau() %>"></i> <%= cours.getNiveauFormate() %>
                    </span>
                </div>
            </div>
            <button type="button" class="modal-close-btn" onclick="fermerModalPlanCours()" title="Fermer">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <div class="modal-plan-guide">
                <i class="fas fa-info-circle"></i>
                <span>Terminez chaque module pour debloquer le suivant. Score minimum : <strong>75%</strong> par examen de module.</span>
            </div>
            <div class="modal-modules-list">
                <% if (modules != null && !modules.isEmpty()) {
                    for (Map<String, Object> moduleData : modules) {
                        int numero = (Integer) moduleData.get("numero");
                        boolean debloque = (Boolean) moduleData.get("debloque");
                        boolean reussi = (Boolean) moduleData.get("reussi");
                        ProgressionModule progModule = (ProgressionModule) moduleData.get("progression");
                        ExamenModule examen = (ExamenModule) moduleData.get("examen");
                        List<Map<String, Object>> chapitresData = (List<Map<String, Object>>) moduleData.get("chapitres");
                        int nbLeconsModule = (Integer) moduleData.get("nbLecons");

                        String statusIcon = reussi ? "fa-check-circle" : (debloque ? "fa-unlock-alt" : "fa-lock");
                        String statusColor = reussi ? "#27ae60" : (debloque ? "#667eea" : "#aaa");
                %>
                    <div class="modal-module-item <%= reussi ? "reussi" : (debloque ? "debloque" : "verrouille") %>">
                        <div class="modal-module-header">
                            <span class="modal-module-numero" style="background: <%= statusColor %>;"><%= numero %></span>
                            <span class="modal-module-titre">Module <%= numero %></span>
                            <span class="modal-module-lecons"><i class="fas fa-file-alt"></i> <%= nbLeconsModule %> lecons</span>
                            <i class="fas <%= statusIcon %> modal-module-status" style="color: <%= statusColor %>;"></i>
                        </div>
                        <% if (chapitresData != null && !chapitresData.isEmpty()) { %>
                            <div class="modal-module-chapitres">
                                <% for (Map<String, Object> chapData : chapitresData) {
                                    Chapitre chap = (Chapitre) chapData.get("chapitre");
                                    boolean isCurrentChap = (chap.getId() == chapitre.getId());
                                %>
                                    <div class="modal-chapitre-item <%= isCurrentChap ? "actif" : "" %>">
                                        <span class="modal-chapitre-dot" style="background: <%= isCurrentChap ? "#667eea" : "#ccc" %>;"></span>
                                        <span class="modal-chapitre-nom"><%= chap.getTitre() %></span>
                                        <% if (isCurrentChap) { %>
                                            <span class="modal-chapitre-badge">Actuel</span>
                                        <% } %>
                                    </div>
                                <% } %>
                            </div>
                        <% } %>
                    </div>
                <%  }
                } %>
            </div>
        </div>
    </div>
</div>

<%-- ============================================================
     MODALE : VUE DU CHAPITRE
     ============================================================ --%>
<div class="modal-overlay" id="modalVueChapitre" style="display: none;">
    <div class="modal-container modal-vue-chapitre">
        <div class="modal-header">
            <div class="modal-header-left">
                <div class="modal-header-icon" style="background: #667eea;">
                    <i class="fas fa-bookmark"></i>
                </div>
                <div class="modal-header-text">
                    <h2><%= chapitre.getTitre() %></h2>
                    <span class="modal-header-badge">
                        <i class="fas fa-cube"></i> Module <%= moduleActiveNumero %>
                    </span>
                </div>
            </div>
            <button type="button" class="modal-close-btn" onclick="fermerModalVueChapitre()" title="Fermer">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <% if (chapitre.getDescription() != null && !chapitre.getDescription().isEmpty()) { %>
                <div class="modal-chapitre-desc">
                    <p><%= chapitre.getDescription() %></p>
                </div>
            <% } %>
            <% if (chapitre.getObjectifs() != null && !chapitre.getObjectifs().isEmpty()) { %>
                <div class="modal-chapitre-objectifs">
                    <h4><i class="fas fa-bullseye"></i> Objectifs</h4>
                    <p><%= chapitre.getObjectifs() %></p>
                </div>
            <% } %>
            <div class="modal-lecons-liste">
                <h4><i class="fas fa-list-ol"></i> Lecons du chapitre (<%= totalLeconsChapitre %>)</h4>
                <% if (chapitre.getLecons() != null && !chapitre.getLecons().isEmpty()) {
                    for (Lecon l : chapitre.getLecons()) {
                        boolean isActive = (l.getId() == lecon.getId());
                %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/lecon?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>&leconId=<%= l.getId() %>"
                       class="modal-lecon-item <%= isActive ? "actif" : "" %>">
                        <span class="modal-lecon-numero"><%= l.getOrdre() %></span>
                        <span class="modal-lecon-titre"><%= l.getTitre() %></span>
                        <span class="modal-lecon-duree"><%= l.getTempsLectureFormate() %></span>
                        <i class="fas fa-arrow-right modal-lecon-fleche"></i>
                    </a>
                <%      }
                } %>
            </div>
        </div>
    </div>
</div>

<%-- ===== FORMULAIRE CACHE POUR VALIDATION ===== --%>
<form id="formValiderLecon" action="${pageContext.request.contextPath}/joueur/cours/valider" method="POST" style="display: none;">
    <input type="hidden" name="coursId" value="<%= cours.getId() %>">
    <input type="hidden" name="chapitreId" value="<%= chapitre.getId() %>">
    <input type="hidden" name="leconId" value="<%= lecon.getId() %>">
    <input type="hidden" name="tempsPasse" id="tempsPasseInput" value="0">
</form>

<%-- ===== NOTIFICATION DE COPIE ===== --%>
<div class="copie-notification" id="copieNotification" style="display: none;">
    <i class="fas fa-check-circle"></i>
    <span>Code copie dans le presse-papier !</span>
</div>

<%-- ===== CHARGEMENT DES SCRIPTS ===== --%>
<%
    String coursLeconJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-lecon.js\"></script>";
    request.setAttribute("extraScripts", coursLeconJS);
%>

<script>
    var TEMPS_DEBUT_LECON = Date.now();
    var EST_TERMINEE = <%= estTerminee %>;
    var LECON_ID = <%= lecon.getId() %>;
    var COURS_ID = <%= cours.getId() %>;
    var CHAPITRE_ID = <%= chapitre.getId() %>;
</script>

<%@ include file="../footer-connecte.jsp" %>