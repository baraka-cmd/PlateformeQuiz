<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- ============================================================
     Fichier : inscription.jsp
     Rôle    : Page d'inscription AmijayQuiz
              - Formulaire complet avec validation
              - Panneau latéral informatif (avantages)
              - Vérification force mot de passe
              - Vérification correspondance mots de passe
              - Affichage des messages d'erreur/succès
              - Redirection après inscription réussie
     ============================================================ --%>

<%
    // Récupération des messages de la session
    String messageErreur = (String) session.getAttribute("messageErreur");
    String messageSucces = (String) session.getAttribute("messageSucces");
    
    // Valeurs de retour en cas d'erreur (pré-remplissage)
    String oldPseudo = (String) session.getAttribute("oldPseudo");
    String oldEmail = (String) session.getAttribute("oldEmail");
    
    // Suppression des messages après affichage
    if (messageErreur != null) session.removeAttribute("messageErreur");
    if (messageSucces != null) session.removeAttribute("messageSucces");
    if (oldPseudo != null) session.removeAttribute("oldPseudo");
    if (oldEmail != null) session.removeAttribute("oldEmail");
    
    // Titre de la page
    request.setAttribute("pageTitle", "Inscription - AmijayQuiz");
%>

<%
    // Chargement du CSS auth (partagé avec connexion)
    String authCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/auth.css\">";
    request.setAttribute("extraHead", authCSS);

    // Chargement du JS auth
    String authJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/auth.js\"></script>";
    request.setAttribute("extraScripts", authJS);
%>

<%@ include file="header.jsp" %>

<%-- ============================================================
     CONTENEUR PRINCIPAL (2 colonnes)
     ============================================================ --%>
<div class="auth-container">
    
    <%-- ============================================================
         PANNEAU GAUCHE : INFORMATIONS & AVANTAGES
         ============================================================ --%>
    <div class="auth-sidebar">
        <div class="sidebar-content">
            <%-- Logo --%>
            <a href="${pageContext.request.contextPath}/" class="sidebar-logo">
                <i class="fas fa-brain"></i>
                <span>AmijayQuiz</span>
            </a>
            
            <h2>Rejoignez la communauté !</h2>
            <p class="sidebar-desc">
                Créez votre compte gratuit et commencez votre parcours d'apprentissage dès maintenant.
            </p>
            
            <%-- Avantages --%>
            <div class="benefits-list">
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-graduation-cap"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Apprentissage progressif</strong>
                        <span>3 niveaux par thème pour maîtriser chaque sujet</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-trophy"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Système de récompenses</strong>
                        <span>Débloquez des niveaux et suivez votre progression</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-chart-line"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Statistiques détaillées</strong>
                        <span>Visualisez vos scores et votre évolution</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-clock"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Quiz chronométrés</strong>
                        <span>Testez vos connaissances en conditions réelles</span>
                    </div>
                </div>
            </div>
            
            <%-- Témoignage --%>
            <div class="sidebar-testimonial">
                <div class="testimonial-quote">
                    <i class="fas fa-quote-left"></i>
                    <p>Grâce à AmijayQuiz, j'ai pu valider mes compétences et progresser rapidement. La plateforme est intuitive et motivante !</p>
                </div>
                <div class="testimonial-author">
                    <div class="author-avatar-sm">
                        <i class="fas fa-user-graduate"></i>
                    </div>
                    <div class="author-info-sm">
                        <strong>Marie K.</strong>
                        <span>Étudiante en informatique</span>
                        <div class="author-stars">
                            <i class="fas fa-star"></i>
                            <i class="fas fa-star"></i>
                            <i class="fas fa-star"></i>
                            <i class="fas fa-star"></i>
                            <i class="fas fa-star"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <%-- ============================================================
         PANNEAU DROIT : FORMULAIRE D'INSCRIPTION
         ============================================================ --%>
    <div class="auth-form-container">
        <div class="auth-form-wrapper">
            
            <%-- En-tête du formulaire --%>
            <div class="auth-form-header">
                <div class="form-icon-circle">
                    <i class="fas fa-user-plus"></i>
                </div>
                <h1>Créer un compte</h1>
                <p>Remplissez le formulaire ci-dessous pour commencer</p>
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
            <form action="${pageContext.request.contextPath}/inscription" method="POST" id="formInscription" novalidate>
                
                <%-- ===== CHAMP PSEUDO ===== --%>
                <div class="form-group">
                    <label for="pseudo">
                        <i class="fas fa-user"></i>
                        <span>Pseudo</span>
                    </label>
                    <div class="input-wrapper">
                        <i class="fas fa-user input-icon"></i>
                        <input 
                            type="text" 
                            id="pseudo" 
                            name="pseudo" 
                            placeholder="Choisissez un pseudo unique"
                            value="<%= oldPseudo != null ? oldPseudo : "" %>"
                            minlength="3"
                            maxlength="50"
                            required
                            autocomplete="username"
                        >
                        <span class="input-status" id="pseudoStatus"></span>
                    </div>
                    <span class="form-hint">
                        <i class="fas fa-info-circle"></i> 3 à 50 caractères. Choisissez un pseudo qui vous identifie.
                    </span>
                    <span class="form-error" id="pseudoError"></span>
                </div>
                
                <%-- ===== CHAMP EMAIL (OPTIONNEL) ===== --%>
                <div class="form-group">
                    <label for="email">
                        <i class="fas fa-envelope"></i>
                        <span>Adresse email</span>
                        <span class="label-badge optional">Optionnel</span>
                    </label>
                    <div class="input-wrapper">
                        <i class="fas fa-envelope input-icon"></i>
                        <input 
                            type="email" 
                            id="email" 
                            name="email" 
                            placeholder="Votre adresse email (optionnelle)"
                            value="<%= oldEmail != null ? oldEmail : "" %>"
                            maxlength="100"
                            autocomplete="email"
                        >
                        <span class="input-status" id="emailStatus"></span>
                    </div>
                    <span class="form-hint">
                        <i class="fas fa-info-circle"></i> Pour vous connecter et récupérer votre compte en cas d'oubli.
                    </span>
                    <span class="form-error" id="emailError"></span>
                </div>
                
                <%-- ===== CHAMP MOT DE PASSE ===== --%>
                <div class="form-group">
                    <label for="motDePasse">
                        <i class="fas fa-lock"></i>
                        <span>Mot de passe</span>
                    </label>
                    <div class="input-wrapper">
                        <i class="fas fa-lock input-icon"></i>
                        <input 
                            type="password" 
                            id="motDePasse" 
                            name="motDePasse" 
                            placeholder="Minimum 6 caractères"
                            minlength="6"
                            maxlength="100"
                            required
                            autocomplete="new-password"
                        >
                        <button type="button" class="toggle-password-btn" id="togglePassword1" tabindex="-1" 
                                onclick="togglePassword('motDePasse', 'togglePassword1')"
                                aria-label="Afficher/masquer le mot de passe">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                    
                    <%-- Indicateur de force du mot de passe --%>
                    <div class="password-strength" id="passwordStrengthContainer">
                        <div class="strength-bar" id="strengthBar"></div>
                    </div>
                    <div class="password-strength-text" id="strengthText"></div>
                    
                    <%-- Exigences du mot de passe --%>
                    <div class="password-requirements" id="passwordRequirements">
                        <span class="requirement" id="reqLength">
                            <i class="fas fa-circle"></i> Au moins 6 caractères
                        </span>
                        <span class="requirement" id="reqUppercase">
                            <i class="fas fa-circle"></i> Au moins une majuscule
                        </span>
                        <span class="requirement" id="reqNumber">
                            <i class="fas fa-circle"></i> Au moins un chiffre ou caractère spécial
                        </span>
                    </div>
                </div>
                
                <%-- ===== CHAMP CONFIRMATION ===== --%>
                <div class="form-group">
                    <label for="confirmation">
                        <i class="fas fa-check-double"></i>
                        <span>Confirmer le mot de passe</span>
                    </label>
                    <div class="input-wrapper">
                        <i class="fas fa-check-double input-icon"></i>
                        <input 
                            type="password" 
                            id="confirmation" 
                            name="confirmation" 
                            placeholder="Répétez votre mot de passe"
                            minlength="6"
                            maxlength="100"
                            required
                            autocomplete="new-password"
                        >
                        <button type="button" class="toggle-password-btn" id="togglePassword2" tabindex="-1"
                                onclick="togglePassword('confirmation', 'togglePassword2')"
                                aria-label="Afficher/masquer la confirmation">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                    <span class="form-match" id="messageCorrespondance"></span>
                </div>
                
                <%-- ===== CASE À COCHER CONDITIONS ===== --%>
                <div class="form-group checkbox-group">
                    <label class="checkbox-label">
                        <input type="checkbox" id="acceptConditions" required>
                        <span class="checkbox-custom"></span>
                        <span class="checkbox-text">
                            J'accepte les 
                            <a href="#" class="form-link">conditions d'utilisation</a> 
                            et la 
                            <a href="#" class="form-link">politique de confidentialité</a>
                        </span>
                    </label>
                </div>
                
                                <%-- ===== BOUTON D'INSCRIPTION ===== --%>
                <button type="submit" class="btn-auth btn-auth-primary" id="btnInscription">
                    <i class="fas fa-user-plus"></i>
                    <span>Créer mon compte</span>
                    <span class="btn-loader" id="btnLoader">
                        <i class="fas fa-spinner fa-spin"></i>
                    </span>
                </button>
            </form>
            
            <%-- Séparateur --%>
            <div class="auth-separator">
                <span>OU</span>
            </div>
            
            <%-- ===== BOUTON INSCRIPTION AVEC GOOGLE ===== --%>
            <a href="${pageContext.request.contextPath}/google-login" class="btn-google">
                <svg class="google-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" width="20" height="20">
                    <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                    <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                    <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.97 16.46 0 20.12 0 24c0 3.88.97 7.54 2.56 10.78l7.97-6.19z"/>
                    <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
                </svg>
                <span>S'inscrire avec Google</span>
            </a>
            
            <%-- Lien connexion --%>
            <div class="auth-redirect">
                <i class="fas fa-sign-in-alt"></i>
                <span>Déjà un compte ?</span>
                <a href="${pageContext.request.contextPath}/connexion">
                    Se connecter
                    <i class="fas fa-arrow-right"></i>
                </a>
            </div>
            
            <%-- Retour accueil --%>
            <div class="auth-back">
                <a href="${pageContext.request.contextPath}/">
                    <i class="fas fa-arrow-left"></i> Retour à l'accueil
                </a>
            </div>
            
        </div>
    </div>
    
</div>

<%@ include file="footer.jsp" %>