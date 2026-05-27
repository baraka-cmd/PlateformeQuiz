<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- ============================================================
     Fichier : changer-mot-de-passe.jsp
     Rôle    : Page de changement de mot de passe
              - Formulaire sécurisé (ancien MDP + nouveau + confirmation)
              - Validation force du mot de passe
              - Messages de succès/erreur
     ============================================================ --%>

<%
    // Vérification que l'utilisateur est connecté
    if (session == null || session.getAttribute("utilisateur") == null) {
        response.sendRedirect(request.getContextPath() + "/connexion");
        return;
    }

    // Récupération des messages de la session
    String messageErreur = (String) session.getAttribute("messageErreur");
    String messageSucces = (String) session.getAttribute("messageSucces");
    
    // Suppression des messages après affichage
    if (messageErreur != null) session.removeAttribute("messageErreur");
    if (messageSucces != null) session.removeAttribute("messageSucces");
    
    // Titre de la page
    request.setAttribute("pageTitle", "Changer mon mot de passe - AmijayQuiz");

    // Chargement des styles spécifiques
    String mdpCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/changer-mot-de-passe.css\">";
    request.setAttribute("extraHead", mdpCSS);
    
    // Chargement du script spécifique
    String mdpJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/changer-mot-de-passe.js\"></script>";
    request.setAttribute("extraScripts", mdpJS);
%>

<%@ include file="header.jsp" %>

<%-- ============================================================
     CONTENEUR PRINCIPAL
     ============================================================ --%>
<div class="mdp-container">
    
    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-key"></i>
            <span>Changer mon mot de passe</span>
        </span>
    </nav>

    <%-- ============================================================
         CARTE DU FORMULAIRE
         ============================================================ --%>
    <div class="mdp-card">
        
        <%-- En-tête --%>
        <div class="mdp-card-header">
            <div class="mdp-icon-circle">
                <i class="fas fa-lock"></i>
            </div>
            <h1>Changer mon mot de passe</h1>
            <p>Pour votre sécurité, choisissez un mot de passe fort et unique</p>
        </div>
        
        <%-- Messages d'alerte --%>
        <% if (messageErreur != null) { %>
            <div class="message-alerte message-erreur">
                <i class="fas fa-exclamation-circle"></i>
                <span><%= messageErreur %></span>
            </div>
        <% } %>
        
        <% if (messageSucces != null) { %>
            <div class="message-alerte message-succes">
                <i class="fas fa-check-circle"></i>
                <span><%= messageSucces %></span>
            </div>
        <% } %>
        
        <%-- Formulaire --%>
        <form action="${pageContext.request.contextPath}/changer-mot-de-passe" method="POST" id="formChangerMdp" novalidate>
            
            <%-- ===== ANCIEN MOT DE PASSE ===== --%>
            <div class="form-group">
                <label for="ancienMotDePasse">
                    <i class="fas fa-key"></i>
                    <span>Mot de passe actuel</span>
                </label>
                <div class="input-wrapper">
                    <i class="fas fa-key input-icon"></i>
                    <input 
                        type="password" 
                        id="ancienMotDePasse" 
                        name="ancienMotDePasse" 
                        placeholder="Entrez votre mot de passe actuel"
                        required
                        autocomplete="current-password"
                        autofocus
                    >
                    <button type="button" class="toggle-password-btn" id="toggleAncien" tabindex="-1"
                            aria-label="Afficher/masquer le mot de passe">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
                <span class="form-error" id="ancienError"></span>
            </div>
            
            <%-- ===== NOUVEAU MOT DE PASSE ===== --%>
            <div class="form-group">
                <label for="nouveauMotDePasse">
                    <i class="fas fa-lock"></i>
                    <span>Nouveau mot de passe</span>
                </label>
                <div class="input-wrapper">
                    <i class="fas fa-lock input-icon"></i>
                    <input 
                        type="password" 
                        id="nouveauMotDePasse" 
                        name="nouveauMotDePasse" 
                        placeholder="Minimum 8 caractères"
                        minlength="8"
                        maxlength="100"
                        required
                        autocomplete="new-password"
                    >
                    <button type="button" class="toggle-password-btn" id="toggleNouveau" tabindex="-1"
                            aria-label="Afficher/masquer le mot de passe">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
                
                <%-- Indicateur de force --%>
                <div class="password-strength" id="passwordStrengthContainer">
                    <div class="strength-bar" id="strengthBar"></div>
                </div>
                <div class="password-strength-text" id="strengthText"></div>
                
                <%-- Exigences --%>
                <div class="password-requirements" id="passwordRequirements">
                    <span class="requirement" id="reqLength">
                        <i class="fas fa-circle"></i> Au moins 8 caractères
                    </span>
                    <span class="requirement" id="reqUppercase">
                        <i class="fas fa-circle"></i> Au moins une majuscule
                    </span>
                    <span class="requirement" id="reqLowercase">
                        <i class="fas fa-circle"></i> Au moins une minuscule
                    </span>
                    <span class="requirement" id="reqNumber">
                        <i class="fas fa-circle"></i> Au moins un chiffre
                    </span>
                    <span class="requirement" id="reqSpecial">
                        <i class="fas fa-circle"></i> Au moins un caractère spécial
                    </span>
                </div>
            </div>
            
            <%-- ===== CONFIRMATION ===== --%>
            <div class="form-group">
                <label for="confirmation">
                    <i class="fas fa-check-double"></i>
                    <span>Confirmer le nouveau mot de passe</span>
                </label>
                <div class="input-wrapper">
                    <i class="fas fa-check-double input-icon"></i>
                    <input 
                        type="password" 
                        id="confirmation" 
                        name="confirmation" 
                        placeholder="Répétez votre nouveau mot de passe"
                        minlength="8"
                        maxlength="100"
                        required
                        autocomplete="new-password"
                    >
                    <button type="button" class="toggle-password-btn" id="toggleConfirm" tabindex="-1"
                            aria-label="Afficher/masquer la confirmation">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
                <span class="form-match" id="messageCorrespondance"></span>
            </div>
            
            <%-- ===== CONSEILS DE SÉCURITÉ ===== --%>
            <div class="security-tips">
                <h4><i class="fas fa-shield-alt"></i> Conseils de sécurité</h4>
                <ul>
                    <li><i class="fas fa-check-circle"></i> Utilisez un mot de passe différent de votre ancien mot de passe</li>
                    <li><i class="fas fa-check-circle"></i> Ne réutilisez pas un mot de passe d'un autre site</li>
                    <li><i class="fas fa-check-circle"></i> Évitez les informations personnelles (date de naissance, nom...)</li>
                    <li><i class="fas fa-check-circle"></i> Un mot de passe fort contient au moins 12 caractères</li>
                </ul>
            </div>
            
            <%-- ===== BOUTONS ===== --%>
            <div class="form-actions">
                <button type="submit" class="btn-mdp btn-mdp-primary" id="btnChangerMdp">
                    <i class="fas fa-save"></i>
                    <span>Changer mon mot de passe</span>
                    <span class="btn-loader" id="btnLoader">
                        <i class="fas fa-spinner fa-spin"></i>
                    </span>
                </button>
                
                <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-mdp btn-mdp-secondary">
                    <i class="fas fa-times"></i>
                    <span>Annuler</span>
                </a>
            </div>
            
        </form>
    </div>
</div>

<%@ include file="footer.jsp" %>