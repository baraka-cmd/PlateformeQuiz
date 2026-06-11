<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- ============================================================
     FICHIER : views/connexion.jsp
     RÔLE    : Page de connexion LearnPath
              - Connexion par EMAIL et mot de passe
              - Layout 2 colonnes (sidebar + formulaire)
              - Avantages : Quiz + Cours + Certificats
              - Option "Se souvenir de moi"
              - Lien "Mot de passe oublié"
              - Design professionnel complet
     ============================================================ --%>

<%
    String messageErreur = (String) session.getAttribute("messageErreur");
    String messageSucces = (String) session.getAttribute("messageSucces");
    String oldEmail = (String) session.getAttribute("oldEmail");
    
    if (messageErreur != null) session.removeAttribute("messageErreur");
    if (messageSucces != null) session.removeAttribute("messageSucces");
    if (oldEmail != null) session.removeAttribute("oldEmail");
    
    request.setAttribute("pageTitle", "Connexion - LearnPath");
%>

<%
    String authCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/auth.css\">";
    request.setAttribute("extraHead", authCSS);
%>

<%@ include file="header.jsp" %>

<div class="auth-container">
    
    <%-- ============================================================
         PANNEAU GAUCHE : INFORMATIONS & AVANTAGES
         ============================================================ --%>
    <div class="auth-sidebar">
        <div class="sidebar-content">
            
            <a href="${pageContext.request.contextPath}/" class="sidebar-logo">
                <img src="${pageContext.request.contextPath}/views/assets/images/logo.png" alt="LearnPath" style="height: 30px; width: auto;">
                <span>LearnPath</span>
            </a>
            
            <h2>Content de vous revoir !</h2>
            <p class="sidebar-desc">
                Connectez-vous pour reprendre votre parcours d'apprentissage là où vous l'avez laissé.
            </p>
            
            <%-- Avantages --%>
            <div class="benefits-list">
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-graduation-cap"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Cours complets</strong>
                        <span>12 thèmes, 3 niveaux, 15 modules par cours</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-layer-group"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Quiz interactifs</strong>
                        <span>6 thèmes, 3 niveaux, 20 parties par niveau</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-certificate"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Certificats vérifiables</strong>
                        <span>Validez vos compétences avec des certificats</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-chart-line"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Suivi de progression</strong>
                        <span>Visualisez votre évolution en temps réel</span>
                    </div>
                </div>
            </div>
            
            <%-- Citation --%>
            <div class="sidebar-testimonial">
                <div class="testimonial-quote">
                    <i class="fas fa-quote-left"></i>
                    <p>L'apprentissage est un trésor qui suivra son propriétaire partout.</p>
                </div>
                <div class="testimonial-author">
                    <div class="author-avatar-sm">
                        <i class="fas fa-feather-alt"></i>
                    </div>
                    <div class="author-info-sm">
                        <strong>Proverbe chinois</strong>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <%-- ============================================================
         PANNEAU DROIT : FORMULAIRE DE CONNEXION
         ============================================================ --%>
    <div class="auth-form-container">
        <div class="auth-form-wrapper">
            
            <div class="auth-form-header">
                <div class="form-icon-circle">
                    <i class="fas fa-sign-in-alt"></i>
                </div>
                <h1>Connexion</h1>
                <p>Connectez-vous avec votre email et mot de passe</p>
            </div>
            
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
            
            <form action="${pageContext.request.contextPath}/connexion" method="POST" id="formConnexion" novalidate>
                
                <%-- ===== CHAMP EMAIL ===== --%>
                <div class="form-group">
                    <label for="email">
                        <i class="fas fa-envelope"></i>
                        <span>Adresse email</span>
                    </label>
                    <div class="input-wrapper">
                        <i class="fas fa-envelope input-icon"></i>
                        <input type="email" id="email" name="email" 
                               placeholder="Entrez votre adresse email"
                               value="<%= oldEmail != null ? oldEmail : "" %>"
                               required autocomplete="email" autofocus>
                        <span class="input-status" id="emailStatus"></span>
                    </div>
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
                        <input type="password" id="motDePasse" name="motDePasse" 
                               placeholder="Entrez votre mot de passe"
                               required autocomplete="current-password">
                        <button type="button" class="toggle-password-btn" id="togglePassword" tabindex="-1"
                                aria-label="Afficher/masquer le mot de passe">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                    <span class="form-error" id="motDePasseError"></span>
                </div>
                
                <%-- ===== OPTIONS : SE SOUVENIR + MOT DE PASSE OUBLIÉ ===== --%>
                <div class="form-options">
                    <label class="checkbox-label remember-me">
                        <input type="checkbox" name="rememberMe" id="rememberMe">
                        <span class="checkbox-custom"></span>
                        <span class="checkbox-text">Se souvenir de moi</span>
                    </label>

                    <a href="${pageContext.request.contextPath}/mot-de-passe-oublie" class="forgot-password">
                        <i class="fas fa-question-circle"></i>
                        Mot de passe oublié ?
                    </a>
                </div>
                
                <%-- ===== BOUTON DE CONNEXION ===== --%>
                <button type="submit" class="btn-auth btn-auth-primary" id="btnConnexion">
                    <i class="fas fa-sign-in-alt"></i>
                    <span>Se connecter</span>
                    <span class="btn-loader" id="btnLoader"><i class="fas fa-spinner fa-spin"></i></span>
                </button>
            </form>
            
            <%-- Séparateur --%>
            <div class="auth-separator"><span>OU</span></div>
            
            <%-- ===== BOUTON CONNEXION AVEC GOOGLE ===== --%>
            <a href="${pageContext.request.contextPath}/google-login" class="btn-google">
                <svg class="google-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" width="20" height="20">
                    <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                    <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                    <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.97 16.46 0 20.12 0 24c0 3.88.97 7.54 2.56 10.78l7.97-6.19z"/>
                    <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
                </svg>
                <span>Continuer avec Google</span>
            </a>
            
            <%-- Lien inscription --%>
            <div class="auth-redirect">
                <i class="fas fa-user-plus"></i>
                <span>Pas encore de compte ?</span>
                <a href="${pageContext.request.contextPath}/inscription">Créer un compte <i class="fas fa-arrow-right"></i></a>
            </div>
            
            <%-- Retour accueil --%>
            <div class="auth-back">
                <a href="${pageContext.request.contextPath}/"><i class="fas fa-arrow-left"></i> Retour à l'accueil</a>
            </div>
            
        </div>
    </div>
    
</div>

<%
    String connexionJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/connexion.js\"></script>";
    request.setAttribute("extraScripts", connexionJS);
%>

<%@ include file="footer.jsp" %>