<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>
<%@ page import="com.amijayquiz.models.Certificat" %>
<%@ page import="com.amijayquiz.models.ExamenModule" %>
<%@ page import="com.amijayquiz.models.ProgressionModule" %>
<%@ page import="com.amijayquiz.services.PaymentService" %>
<%@ page import="com.amijayquiz.services.CourseService" %>

<%
    // Récupération des attributs
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
    
    // Récupération de l'utilisateur pour le paiement
    com.amijayquiz.models.Utilisateur utilisateur = (com.amijayquiz.models.Utilisateur) session.getAttribute("utilisateur");

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
    
    // Données pour le paiement
    double coursPrix = CourseService.getCoursePrice(cours.getId());
    boolean coursPayant = coursPrix > 0;
    boolean accesDejaAchete = false;
    
    if (utilisateur != null && coursPayant) {
        accesDejaAchete = PaymentService.canAccessCourse(utilisateur.getId(), cours.getId());
    }

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
%>

<%
    String coursPlanCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-plan.css\">";
    request.setAttribute("extraHead", coursPlanCSS);
%>

<%@ include file="../header.jsp" %>

<div class="cours-plan-container">

    <!-- Breadcrumb -->
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

    <!-- En-tête du cours -->
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

        <div class="plan-motivation">
            <i class="<%= iconeMotivation %>"></i>
            <span><%= messageMotivation %></span>
        </div>
    </div>

    <!-- Section Paiement (cours payants uniquement) -->
    <% if (coursPayant && !accesDejaAchete) { %>
    <div class="course-payment-section">
        <div class="payment-card">
            <div class="payment-card-header">
                <i class="fas fa-crown"></i>
                <h3>Version Premium</h3>
            </div>
            <div class="payment-card-price">
                <span class="price-amount"><%= String.format("%.2f", coursPrix) %> USD</span>
                <span class="price-label">Accès à vie</span>
            </div>
            <ul class="payment-features">
                <li><i class="fas fa-check-circle"></i> Accès à tous les chapitres</li>
                <li><i class="fas fa-check-circle"></i> Quiz et examens inclus</li>
                <li><i class="fas fa-check-circle"></i> Certificat de réussite</li>
                <li><i class="fas fa-check-circle"></i> Support prioritaire</li>
            </ul>
            <a href="${pageContext.request.contextPath}/joueur/paiement/checkout?courseId=<%= cours.getId() %>" 
               class="btn-buy">
                <i class="fas fa-shopping-cart"></i> Acheter le cours
            </a>
            <p class="payment-guarantee">
                <i class="fas fa-shield-alt"></i> Paiement 100% sécurisé
            </p>
        </div>
    </div>
    <% } else if (coursPayant && accesDejaAchete) { %>
    <div class="course-payment-section purchased">
        <div class="purchased-card">
            <i class="fas fa-check-circle"></i>
            <div>
                <h4>Vous avez déjà accès à ce cours</h4>
                <p>Merci pour votre achat ! Profitez de tout le contenu.</p>
            </div>
        </div>
    </div>
    <% } %>

    <!-- Liste des modules (TOUJOURS affichée pour les cours gratuits OU payants déjà achetés) -->
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

                String statusClass = reussi ? "reussi" : (debloque ? "debloque" : "verrouille");
                String statusIcon = reussi ? "fa-check-circle" : (debloque ? "fa-unlock-alt" : "fa-lock");
                String statusColor = reussi ? "#27ae60" : (debloque ? "#667eea" : "#aaa");
                String statusText = reussi ? "Réussi" : (debloque ? "Débloqué" : "Verrouillé");
        %>
            <div class="module-card <%= statusClass %>">
                
                <div class="module-card-header" onclick="toggleModuleChapitres(<%= numero %>)">
                    <div class="module-card-number" style="background: <%= statusColor %>;">
                        <span><%= numero %></span>
                    </div>
                    <div class="module-card-info">
                        <h3>Module <%= numero %></h3>
                        <div class="module-card-meta">
                            <span><i class="fas fa-file-alt"></i> <%= nbLecons %> leçons</span>
                        </div>
                    </div>
                    <div class="module-card-status" style="color: <%= statusColor %>;">
                        <i class="fas <%= statusIcon %>"></i>
                        <span><%= statusText %></span>
                        <i class="fas fa-chevron-down module-toggle-icon" id="moduleToggleIcon<%= numero %>"></i>
                    </div>
                </div>

                <div class="module-card-content" id="moduleContent<%= numero %>" style="display: <%= (debloque || reussi) ? "block" : "none" %>;">
                    
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

                </div>
            </div>
        <%  }
        } else { %>
            <div class="empty-modules">
                <i class="fas fa-inbox"></i>
                <p>Aucun module disponible pour ce cours.</p>
            </div>
        <% } %>
    </div>

    <!-- Examen Final -->
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

    <!-- Bouton retour -->
    <div class="plan-footer">
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" 
           class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au niveau <%= cours.getNiveauFormate() %>
        </a>
    </div>

</div>

<script>
    function toggleModuleChapitres(numero) {
        var content = document.getElementById('moduleContent' + numero);
        var icon = document.getElementById('moduleToggleIcon' + numero);
        
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

<%@ include file="../footer.jsp" %>