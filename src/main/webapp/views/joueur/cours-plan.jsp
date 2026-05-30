<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>
<%@ page import="com.amijayquiz.models.Certificat" %>
<%@ page import="com.amijayquiz.models.ExamenModule" %>
<%@ page import="com.amijayquiz.models.ProgressionModule" %>

<%-- ============================================================
     Fichier : cours-plan.jsp
     Rôle    : Plan détaillé d'un cours organisé par MODULES
              MODIFIÉ : Badge "Premium" sur modules 2+ si non premium
              Bouton "Débloquer" redirige vers la page de paiement
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    List<Map<String, Object>> modules = (List<Map<String, Object>>) request.getAttribute("modules");
    Integer nombreModules = (Integer) request.getAttribute("nombreModules");
    Integer modulesReussis = (Integer) request.getAttribute("modulesReussis");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    Integer totalLeconsTerminees = (Integer) request.getAttribute("totalLeconsTerminees");
    Integer pourcentageGlobal = (Integer) request.getAttribute("pourcentageGlobal");
    Boolean examenAccessible = (Boolean) request.getAttribute("examenAccessible");
    Boolean dejaCertifie = (Boolean) request.getAttribute("dejaCertifie");
    Certificat certificatExistant = (Certificat) request.getAttribute("certificatExistant");
    String messageMotivation = (String) request.getAttribute("messageMotivation");
    String iconeMotivation = (String) request.getAttribute("iconeMotivation");

    // ===== 🆕 VÉRIFICATION PREMIUM =====
    Boolean estPremium = (Boolean) request.getAttribute("estPremium");
    if (estPremium == null) {
        com.amijayquiz.dao.AccessPremiumDAO premiumDAO = new com.amijayquiz.dao.AccessPremiumDAO();
        com.amijayquiz.models.Utilisateur utilisateur = (com.amijayquiz.models.Utilisateur) session.getAttribute("utilisateur");
        estPremium = (utilisateur != null) && premiumDAO.estPremium(utilisateur.getId());
    }

    if (cours == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    if (nombreModules == null) nombreModules = 15;
    if (modulesReussis == null) modulesReussis = 0;
    if (totalLecons == null) totalLecons = 0;
    if (totalLeconsTerminees == null) totalLeconsTerminees = 0;
    if (pourcentageGlobal == null) pourcentageGlobal = 0;
    if (examenAccessible == null) examenAccessible = false;
    if (dejaCertifie == null) dejaCertifie = false;
    if (messageMotivation == null) messageMotivation = "Commencez dès maintenant votre apprentissage !";
    if (iconeMotivation == null) iconeMotivation = "fas fa-rocket";

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
%>

<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String coursPlanCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-plan.css\">";
    request.setAttribute("extraHead", coursPlanCSS);
%>

<%@ include file="../header.jsp" %>

<div class="cours-plan-container">

    <%-- ===== BREADCRUMB ===== --%>
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
        <span class="breadcrumb-current">
            <i class="fas fa-sitemap"></i><span>Plan</span>
        </span>
    </nav>

    <%-- ===== EN-TÊTE DU COURS ===== --%>
    <div class="plan-header">
        <div class="plan-header-top">
            <div class="plan-header-icon" style="background: linear-gradient(135deg, <%= cours.getCouleurTheme() %>, <%= cours.getCouleurTheme() %>dd);">
                <i class="<%= cours.getIconeTheme() %>"></i>
            </div>
            <div class="plan-header-info">
                <h1><%= cours.getTitre() %></h1>
                <div class="plan-header-meta">
                    <span class="plan-badge" style="background: <%= cours.getCouleurTheme() %>20; color: <%= cours.getCouleurTheme() %>;">
                        <i class="<%= cours.getIconeTheme() %>"></i> <%= cours.getTheme() %>
                    </span>
                    <span class="plan-badge" style="background: <%= cours.getCouleurNiveau() %>20; color: <%= cours.getCouleurNiveau() %>;">
                        <i class="<%= cours.getIconeNiveau() %>"></i> <%= cours.getNiveauFormate() %>
                    </span>
                    <%-- 🆕 BADGE PREMIUM --%>
                    <% if (estPremium) { %>
                        <span class="plan-badge" style="background: #ffd70020; color: #f39c12;">
                            <i class="fas fa-crown"></i> Premium
                        </span>
                    <% } %>
                </div>
            </div>
            <div class="plan-header-statut">
                <% if (dejaCertifie) { %>
                    <span class="statut-badge certifie">
                        <i class="fas fa-crown"></i> Certifié
                    </span>
                <% } else if (pourcentageGlobal >= 100) { %>
                    <span class="statut-badge termine">
                        <i class="fas fa-check-circle"></i> Terminé
                    </span>
                <% } else if (pourcentageGlobal > 0) { %>
                    <span class="statut-badge en-cours">
                        <i class="fas fa-spinner"></i> <%= pourcentageGlobal %>%
                    </span>
                <% } %>
            </div>
        </div>

        <%-- Message de motivation --%>
        <div class="plan-motivation">
            <i class="<%= iconeMotivation %>"></i>
            <span><%= messageMotivation %></span>
        </div>
        
        <%-- ===== 🆕 BANNIÈRE PREMIUM SI NON PREMIUM ===== --%>
        <% if (!estPremium) { %>
            <div class="plan-premium-banner">
                <div class="premium-banner-icon">
                    <i class="fas fa-crown"></i>
                </div>
                <div class="premium-banner-content">
                    <strong>Module 1 gratuit uniquement</strong>
                    <p>Les modules 2 à 15 nécessitent un compte Premium. Débloquez tout pour 4$ !</p>
                </div>
                <a href="${pageContext.request.contextPath}/paiement" class="premium-banner-btn">
                    <i class="fas fa-unlock-alt"></i> Débloquer (4$)
                </a>
            </div>
        <% } %>
    </div>

    <%-- ===== LISTE DES MODULES ===== --%>
    <div class="modules-list">
        <% if (modules != null && !modules.isEmpty()) {
            for (Map<String, Object> moduleData : modules) {
                int numero = (Integer) moduleData.get("numero");
                boolean debloque = (Boolean) moduleData.get("debloque");
                boolean reussi = (Boolean) moduleData.get("reussi");
                ProgressionModule progression = (ProgressionModule) moduleData.get("progression");
                ExamenModule examen = (ExamenModule) moduleData.get("examen");
                List<Map<String, Object>> chapitresData = (List<Map<String, Object>>) moduleData.get("chapitres");
                int nbLecons = (Integer) moduleData.get("nbLecons");

                // ===== 🆕 VÉRIFIER SI LE MODULE EST BLOQUÉ PAR PREMIUM =====
                boolean bloqueParPremium = (numero > 1) && !estPremium;
                
                String statusClass;
                String statusIcon;
                String statusColor;
                String statusText;
                
                if (bloqueParPremium) {
                    statusClass = "bloque-premium";
                    statusIcon = "fa-crown";
                    statusColor = "#f39c12";
                    statusText = "Premium";
                } else {
                    statusClass = reussi ? "reussi" : (debloque ? "debloque" : "verrouille");
                    statusIcon = reussi ? "fa-check-circle" : (debloque ? "fa-unlock-alt" : "fa-lock");
                    statusColor = reussi ? "#27ae60" : (debloque ? "#667eea" : "#aaa");
                    statusText = reussi ? "Réussi" : (debloque ? "Débloqué" : "Verrouillé");
                }
        %>
            <%-- ===== CARTE MODULE ===== --%>
            <div class="module-card <%= statusClass %>">
                
                <%-- En-tête du module --%>
                <div class="module-card-header" onclick="toggleModuleChapitres(<%= numero %>)">
                    <div class="module-card-number" style="background: <%= statusColor %>;">
                        <span><%= numero %></span>
                    </div>
                    <div class="module-card-info">
                        <h3>Module <%= numero %></h3>
                        <div class="module-card-meta">
                            <span><i class="fas fa-file-alt"></i> <%= nbLecons %> leçons</span>
                            <% if (bloqueParPremium) { %>
                                <span class="module-premium-badge">
                                    <i class="fas fa-crown"></i> Premium
                                </span>
                            <% } %>
                        </div>
                    </div>
                    <div class="module-card-status" style="color: <%= statusColor %>;">
                        <i class="fas <%= statusIcon %>"></i>
                        <span><%= statusText %></span>
                        <i class="fas fa-chevron-down module-toggle-icon" id="moduleToggleIcon<%= numero %>"></i>
                    </div>
                </div>

                <%-- Contenu du module --%>
                <div class="module-card-content" id="moduleContent<%= numero %>" 
                     style="display: <%= (debloque || reussi) && !bloqueParPremium ? "block" : "none" %>;">
                    
                    <% if (bloqueParPremium) { %>
                        <%-- 🆕 MESSAGE PREMIUM POUR MODULES BLOQUÉS --%>
                        <div class="module-premium-message">
                            <div class="premium-lock-icon">
                                <i class="fas fa-crown"></i>
                            </div>
                            <p>Ce module nécessite un compte <strong>Premium</strong>.</p>
                            <a href="${pageContext.request.contextPath}/paiement" class="btn-premium-module">
                                <i class="fas fa-unlock-alt"></i> Débloquer pour 4$
                            </a>
                        </div>
                    <% } else { %>
                        <%-- Chapitres du module --%>
                        <% if (chapitresData != null && !chapitresData.isEmpty()) { %>
                            <div class="module-chapitres">
                                <% for (Map<String, Object> chapData : chapitresData) {
                                    Chapitre chapitre = (Chapitre) chapData.get("chapitre");
                                    int nbLeconsChap = (Integer) chapData.get("nbLecons");
                                %>
                                    <div class="module-chapitre-item">
                                        <div class="module-chapitre-header-row">
                                            <span class="chapitre-status-dot" style="background: <%= (debloque || reussi) ? "#667eea" : "#ccc" %>;"></span>
                                            <span class="chapitre-titre"><%= chapitre.getTitre() %></span>
                                            <span class="chapitre-lecons-count"><%= nbLeconsChap %> leçon<%= nbLeconsChap > 1 ? "s" : "" %></span>
                                            
                                            <% if (debloque || reussi) { %>
                                                <a href="${pageContext.request.contextPath}/joueur/cours/chapitre?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>" 
                                                   class="chapitre-acces-btn">
                                                    <i class="fas fa-arrow-right"></i>
                                                </a>
                                            <% } %>
                                        </div>
                                    </div>
                                <% } %>
                            </div>
                        <% } else { %>
                            <div class="module-chapitres-vide">
                                <i class="fas fa-inbox"></i>
                                <span>Aucun chapitre dans ce module</span>
                            </div>
                        <% } %>

                        <%-- Bouton Examen du module --%>
                        <div class="module-examen-action">
                            <% if (reussi && progression != null) { %>
                                <div class="module-examen-reussi">
                                    <i class="fas fa-trophy"></i>
                                    <span>Module réussi - <%= progression.getPourcentageFormate() %></span>
                                    <a href="${pageContext.request.contextPath}/joueur/cours/examen-module?coursId=<%= cours.getId() %>&module=<%= numero %>" 
                                       class="btn-examen-module btn-repasser">
                                        <i class="fas fa-redo"></i> Repasser
                                    </a>
                                </div>
                            <% } else if (debloque && examen != null) { %>
                                <a href="${pageContext.request.contextPath}/joueur/cours/examen-module?coursId=<%= cours.getId() %>&module=<%= numero %>" 
                                   class="btn-examen-module">
                                    <i class="fas fa-clipboard-check"></i>
                                    Passer l'examen du Module <%= numero %>
                                    <span class="btn-examen-info">75% requis</span>
                                    <i class="fas fa-arrow-right"></i>
                                </a>
                            <% } else if (debloque && examen == null) { %>
                                <span class="module-examen-indisponible">
                                    <i class="fas fa-clock"></i> Examen bientôt disponible
                                </span>
                            <% } else { %>
                                <span class="module-examen-verrouille">
                                    <i class="fas fa-lock"></i> Réussissez le Module <%= numero - 1 %> pour débloquer
                                </span>
                            <% } %>
                        </div>
                    <% } %>

                </div>
            </div>
        <%  }
        } %>
    </div>

    <%-- ===== SECTION EXAMEN FINAL ===== --%>
    <div class="examen-final-section">
        <div class="examen-final-card <%= dejaCertifie ? "certifie" : (examenAccessible ? "accessible" : "verrouille") %>">
            <div class="examen-final-icon">
                <% if (dejaCertifie) { %>
                    <i class="fas fa-crown"></i>
                <% } else if (examenAccessible) { %>
                    <i class="fas fa-trophy"></i>
                <% } else { %>
                    <i class="fas fa-lock"></i>
                <% } %>
            </div>
            <div class="examen-final-content">
                <h3>
                    <% if (dejaCertifie) { %>
                        Certificat déjà obtenu
                    <% } else if (examenAccessible) { %>
                        Examen Final
                    <% } else { %>
                        Examen Final Verrouillé
                    <% } %>
                </h3>
                <p>
                    <% if (dejaCertifie) { %>
                        Mention : <strong style="color: <%= certificatExistant.getCouleurMention() %>;"><%= certificatExistant.getMention() %></strong>
                    <% } else if (examenAccessible) { %>
                        Tous les modules sont réussis. Score minimum : <strong>80%</strong>.
                    <% } else { %>
                        Réussissez tous les modules pour débloquer (<%= modulesReussis %>/<%= nombreModules %>).
                    <% } %>
                </p>
            </div>
            <div class="examen-final-action">
                <% if (dejaCertifie) { %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/certificat?coursId=<%= cours.getId() %>" 
                       class="btn-examen-final btn-voir-certificat">
                        <i class="fas fa-certificate"></i> Voir mon certificat
                    </a>
                <% } else if (examenAccessible) { %>
                    <a href="${pageContext.request.contextPath}/joueur/cours/examen?coursId=<%= cours.getId() %>" 
                       class="btn-examen-final btn-passer-examen">
                        <i class="fas fa-play-circle"></i> Passer l'examen final
                    </a>
                <% } %>
            </div>
        </div>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="plan-footer">
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" 
           class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au niveau <%= cours.getNiveauFormate() %>
        </a>
    </div>

</div>

<script>
    function toggleModuleChapitres(numero) {
        const content = document.getElementById('moduleContent' + numero);
        const icon = document.getElementById('moduleToggleIcon' + numero);
        
        if (!content || !icon) return;
        
        if (content.style.display === 'none') {
            content.style.display = 'block';
            icon.classList.remove('fa-chevron-down');
            icon.classList.add('fa-chevron-up');
        } else {
            content.style.display = 'none';
            icon.classList.remove('fa-chevron-up');
            icon.classList.add('fa-chevron-down');
        }
    }
</script>

<%
    String coursPlanJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-plan.js\"></script>";
    request.setAttribute("extraScripts", coursPlanJS);
%>

<%@ include file="../footer.jsp" %>