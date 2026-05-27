<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>
<%@ page import="com.amijayquiz.models.ProgressionCours" %>
<%@ page import="com.amijayquiz.models.Ressource" %>

<%-- ============================================================
     Fichier : cours-lecon.jsp
     Rôle    : Page de contenu d'une leçon de cours
              Affiche : explications texte enrichi,
              exemples de code prêts à copier-coller,
              ressources téléchargeables et navigation
              Style W3Schools : texte + code + pratique
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    Chapitre chapitre = (Chapitre) request.getAttribute("chapitre");
    Lecon lecon = (Lecon) request.getAttribute("lecon");
    ProgressionCours progression = (ProgressionCours) request.getAttribute("progression");
    Lecon leconPrecedente = (Lecon) request.getAttribute("leconPrecedente");
    Lecon leconSuivante = (Lecon) request.getAttribute("leconSuivante");
    Boolean estDerniereLecon = (Boolean) request.getAttribute("estDerniereLecon");
    Boolean estDernierChapitre = (Boolean) request.getAttribute("estDernierChapitre");
    Chapitre prochainChapitre = (Chapitre) request.getAttribute("prochainChapitre");
    Integer totalLeconsChapitre = (Integer) request.getAttribute("totalLeconsChapitre");
    List<Chapitre> chapitresDuCours = (List<Chapitre>) request.getAttribute("chapitresDuCours");
    Boolean tousChapitresTermines = (Boolean) request.getAttribute("tousChapitresTermines");

    if (cours == null || chapitre == null || lecon == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    boolean estTerminee = (progression != null && progression.isTermine());
    int leconActive = lecon.getOrdre();
    if (estDerniereLecon == null) estDerniereLecon = false;
    if (estDernierChapitre == null) estDernierChapitre = false;
    if (tousChapitresTermines == null) tousChapitresTermines = false;

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
    
    // Temps de lecture
    int tempsLecture = lecon.getTempsLectureEstime();
    String tempsLectureFormate = lecon.getTempsLectureFormate();
    int nombreMots = lecon.getNombreMots();
%>



<%-- ===== CHARGEMENT DES STYLES ===== --%>
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
            <i class="fas fa-file-alt"></i><span>Leçon <%= leconActive %></span>
        </span>
    </nav>

    <%-- ============================================================
         LAYOUT 2 COLONNES
         ============================================================ --%>
    <div class="lecon-layout">

        <%-- ============================================================
             COLONNE PRINCIPALE : CONTENU DE LA LEÇON
             ============================================================ --%>
        <div class="lecon-main">

            <%-- En-tête de la leçon --%>
            <div class="lecon-header">
                <div class="lecon-header-badges">
                    <span class="lecon-badge-cours" style="background: <%= cours.getCouleurTheme() %>;">
                        <i class="<%= cours.getIconeTheme() %>"></i> <%= cours.getTitre() %>
                    </span>
                    <span class="lecon-badge-chapitre">
                        <i class="fas fa-bookmark"></i> <%= chapitre.getTitre() %>
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
                        <i class="fas fa-list-ol"></i> Leçon <%= leconActive %> sur <%= totalLeconsChapitre %>
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
                            <i class="fas fa-check-circle"></i> Terminée
                        </span>
                    <% } %>
                </div>
            </div>

            <%-- ===== VIDÉO (OPTIONNELLE - conservée pour compatibilité) ===== --%>
            <% if (lecon.hasVideo()) { %>
                <div class="lecon-video">
                    <div class="video-container">
                        <iframe src="<%= lecon.getYoutubeEmbedUrl() %>" 
                                frameborder="0" 
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                allowfullscreen
                                title="Vidéo de la leçon">
                        </iframe>
                    </div>
                </div>
            <% } %>

            <%-- ===== CONTENU TEXTE : EXPLICATIONS (STYLE W3SCHOOLS) ===== --%>
            <% if (lecon.hasContenuTexte()) { %>
                <div class="lecon-texte">
                    <%= lecon.getContenuTexte() %>
                </div>
            <% } else { %>
                <div class="lecon-texte-vide">
                    <div class="empty-icon">
                        <i class="fas fa-pen-fancy"></i>
                    </div>
                    <h3>Contenu en cours de rédaction</h3>
                    <p>Le contenu détaillé de cette leçon sera bientôt disponible.</p>
                </div>
            <% } %>

            <%-- ===== CODE SOURCE : EXEMPLE PRÊT À COPIER-COLLER ===== --%>
            <% if (lecon.hasCodeSource()) { %>
                <div class="lecon-code">
                    <div class="code-header">
                        <div class="code-header-left">
                            <i class="fas fa-code"></i>
                            <span class="code-title">Exemple de code</span>
                            <span class="code-badge">Prêt à copier-coller</span>
                        </div>
                        <div class="code-header-right">
                            <button class="code-copy-btn" onclick="copierCode()" title="Copier le code dans le presse-papier">
                                <i class="fas fa-copy"></i>
                                <span>Copier le code</span>
                            </button>
                            <button class="code-expand-btn" onclick="toggleCodeExpand()" title="Afficher en plein écran">
                                <i class="fas fa-expand"></i>
                            </button>
                        </div>
                    </div>
                    <pre class="code-block" id="codeBlock"><code><%= lecon.getCodeSource().replace("<", "&lt;").replace(">", "&gt;") %></code></pre>
                    <div class="code-footer">
                        <span class="code-langage">💡 Conseil : Copiez ce code et testez-le dans votre environnement de développement.</span>
                    </div>
                </div>
            <% } %>

            <%-- ===== RESSOURCES ===== --%>
            <% if (lecon.hasRessources()) { %>
                <div class="lecon-ressources">
                    <h3 class="ressources-title">
                        <i class="fas fa-paperclip"></i> Ressources téléchargeables
                    </h3>
                    <div class="ressources-grid">
                        <% for (Ressource ressource : lecon.getRessources()) { %>
                            <a href="<%= ressource.getUrlFichier() %>" 
                               class="ressource-card" 
                               download
                               title="Télécharger <%= ressource.getTitre() %>">
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

            <%-- ===== MESSAGE DE FIN DE LEÇON ===== --%>
            <div class="lecon-fin-message">
                <div class="fin-message-icon">
                    <i class="fas fa-flag-checkered"></i>
                </div>
                <div class="fin-message-content">
                    <h4>
                        <% if (estTerminee) { %>
                            ✅ Leçon terminée !
                        <% } else { %>
                            📚 Fin de la leçon
                        <% } %>
                    </h4>
                    <p>
                        <% if (estTerminee) { %>
                            Vous avez déjà validé cette leçon. Passez à la suite !
                        <% } else if (leconSuivante != null) { %>
                            Prêt à passer à la leçon suivante ? Cliquez sur le bouton ci-dessous.
                        <% } else if (estDerniereLecon && !estDernierChapitre) { %>
                            Dernière leçon de ce chapitre ! Validez pour passer au chapitre suivant.
                        <% } else if (estDerniereLecon && estDernierChapitre && !tousChapitresTermines) { %>
                            Dernière leçon du cours ! Validez pour terminer ce chapitre.
                        <% } else if (estDerniereLecon && estDernierChapitre && tousChapitresTermines) { %>
                            🎉 Tous les chapitres sont terminés ! Vous pouvez passer l'examen final.
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
                            <span class="nav-label">Leçon précédente</span>
                            <span class="nav-titre"><%= leconPrecedente.getTitre() %></span>
                        </div>
                    </a>
                <% } else { %>
                    <div></div>
                <% } %>

                <% if (leconSuivante != null) { %>
                    <button type="button" class="btn-nav-lecon valider" onclick="validerLecon()">
                        <div class="btn-nav-text">
                            <span class="nav-label">Valider & continuer</span>
                            <span class="nav-titre"><%= leconSuivante.getTitre() %></span>
                        </div>
                        <i class="fas fa-check-circle"></i>
                    </button>
                <% } else if (estDerniereLecon && !estDernierChapitre && prochainChapitre != null) { %>
                    <button type="button" class="btn-nav-lecon valider" onclick="validerLecon()">
                        <div class="btn-nav-text">
                            <span class="nav-label">Valider & passer au chapitre suivant</span>
                            <span class="nav-titre"><%= prochainChapitre.getTitre() %></span>
                        </div>
                        <i class="fas fa-arrow-right"></i>
                    </button>
                <% } else if (estDerniereLecon && estDernierChapitre && tousChapitresTermines) { %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/examen?coursId=<%= cours.getId() %>" 
                       class="btn-nav-lecon examen">
                        <div class="btn-nav-text">
                            <span class="nav-label">Valider & passer l'examen final</span>
                            <span class="nav-titre">🎓 Obtenir le certificat</span>
                        </div>
                        <i class="fas fa-trophy"></i>
                    </a>
                <% } else if (estDerniereLecon && estDernierChapitre) { %>
                    <button type="button" class="btn-nav-lecon valider" onclick="validerLecon()">
                        <div class="btn-nav-text">
                            <span class="nav-label">Valider la leçon</span>
                            <span class="nav-titre">✅ Terminer</span>
                        </div>
                        <i class="fas fa-check-circle"></i>
                    </button>
                <% } %>
            </div>

        </div>

        <%-- ============================================================
             COLONNE LATÉRALE : SOMMAIRE DU CHAPITRE
             ============================================================ --%>
        <aside class="lecon-sidebar">
            <%-- Carte sommaire --%>
            <div class="sidebar-card">
                <div class="sidebar-header">
                    <h3><i class="fas fa-list"></i> Sommaire du chapitre</h3>
                    <span class="sidebar-progress"><%= leconActive %>/<%= totalLeconsChapitre %></span>
                </div>
                
                <div class="sidebar-lecons">
                    <% if (chapitresDuCours != null) {
                        for (Chapitre chap : chapitresDuCours) {
                            if (chap.getId() == chapitre.getId() && chap.getLecons() != null) {
                                for (Lecon l : chap.getLecons()) {
                                    boolean isActive = (l.getId() == lecon.getId());
                                    boolean isTermineeLecon = (progression != null && progression.isTermine() && l.getOrdre() <= leconActive);
                                    boolean isAccessible = (l.getOrdre() <= leconActive);
                    %>
                        <a href="${pageContext.request.contextPath}/joueur/cours/lecon?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>&leconId=<%= l.getId() %>" 
                           class="sidebar-lecon <%= isActive ? "active" : "" %> <%= !isAccessible ? "verrouillee" : "" %>">
                            <span class="sidebar-lecon-numero">
                                <% if (isActive) { %>
                                    <i class="fas fa-play-circle"></i>
                                <% } else if (isTermineeLecon) { %>
                                    <i class="fas fa-check-circle"></i>
                                <% } else if (isAccessible) { %>
                                    <%= l.getOrdre() %>
                                <% } else { %>
                                    <i class="fas fa-lock"></i>
                                <% } %>
                            </span>
                            <span class="sidebar-lecon-titre"><%= l.getTitre() %></span>
                            <span class="sidebar-lecon-duree">
                                <%= l.getTempsLectureFormate() %>
                            </span>
                        </a>
                    <%          }
                            }
                        }
                    } %>
                </div>
            </div>

            <%-- Carte infos leçon --%>
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
                    <span><strong>Statut :</strong> <%= estTerminee ? "Terminée" : "En cours" %></span>
                </div>
            </div>

            <%-- Navigation rapide --%>
            <div class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" 
                   class="sidebar-nav-btn">
                    <i class="fas fa-sitemap"></i>
                    <span>Plan du cours</span>
                </a>
                <a href="${pageContext.request.contextPath}/joueur/cours/chapitre?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>" 
                   class="sidebar-nav-btn">
                    <i class="fas fa-bookmark"></i>
                    <span>Vue du chapitre</span>
                </a>
            </div>
        </aside>

    </div>

</div>

<%-- ===== FORMULAIRE CACHÉ POUR VALIDATION ===== --%>
<form id="formValiderLecon" action="${pageContext.request.contextPath}/joueur/cours/valider" method="POST" style="display: none;">
    <input type="hidden" name="coursId" value="<%= cours.getId() %>">
    <input type="hidden" name="chapitreId" value="<%= chapitre.getId() %>">
    <input type="hidden" name="leconId" value="<%= lecon.getId() %>">
    <input type="hidden" name="tempsPasse" id="tempsPasseInput" value="0">
</form>

<%-- ===== NOTIFICATION DE COPIE ===== --%>
<div class="copie-notification" id="copieNotification" style="display: none;">
    <i class="fas fa-check-circle"></i>
    <span>Code copié dans le presse-papier !</span>
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

<%@ include file="../footer.jsp" %>