<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- ============================================================
     Fichier : mot-de-passe-oublie.jsp
     Rôle    : Page de réinitialisation de mot de passe
              - Formulaire pour saisir l'adresse email
              - Envoi d'un nouveau mot de passe par email
              - Messages de succès/erreur
     ============================================================ --%>

<%
    // Récupération des messages de la session
    String messageErreur = (String) session.getAttribute("messageErreur");
    String messageSucces = (String) session.getAttribute("messageSucces");
    String oldEmail = (String) session.getAttribute("oldEmail");
    
    // Suppression des messages après affichage
    if (messageErreur != null) session.removeAttribute("messageErreur");
    if (messageSucces != null) session.removeAttribute("messageSucces");
    if (oldEmail != null) session.removeAttribute("oldEmail");
    
    // Titre de la page
    request.setAttribute("pageTitle", "Mot de passe oublié - AmijayQuiz");
%>

<%
    // Chargement du CSS auth (partagé avec connexion)
    String authCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/mot-de-passe-oublie.css\">";
    request.setAttribute("extraHead", authCSS);

    // Chargement du JS auth
    String authJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/mot-de-passe-oublie.js\"></script>";
    request.setAttribute("extraScripts", authJS);
%>

<%@ include file="header.jsp" %>

<%-- ============================================================
     CONTENEUR PRINCIPAL
     ============================================================ --%>
<div class="auth-container">
    
    <%-- ============================================================
         PANNEAU GAUCHE : INFORMATIONS
         ============================================================ --%>
    <div class="auth-sidebar">
        <div class="sidebar-content">
            <%-- Logo --%>
            <a href="${pageContext.request.contextPath}/" class="sidebar-logo">
                <i class="fas fa-brain"></i>
                <span>AmijayQuiz</span>
            </a>
            
            <h2>Mot de passe oublié ?</h2>
            <p class="sidebar-desc">
                Pas de panique ! Entrez votre adresse email et nous vous enverrons un nouveau mot de passe.
            </p>
            
            <%-- Étapes --%>
            <div class="benefits-list">
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-envelope"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>1. Entrez votre email</strong>
                        <span>L'adresse associée à votre compte</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-paper-plane"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>2. Recevez un email</strong>
                        <span>Un nouveau mot de passe vous sera envoyé</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-lock"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>3. Connectez-vous</strong>
                        <span>Utilisez ce mot de passe puis changez-le</span>
                    </div>
                </div>
                
                <div class="benefit-item">
                    <div class="benefit-icon">
                        <i class="fas fa-shield-alt"></i>
                    </div>
                    <div class="benefit-text">
                        <strong>Sécurisé</strong>
                        <span>Votre mot de passe est hashé et protégé</span>
                    </div>
                </div>
            </div>
            
            <%-- Citation --%>
            <div class="sidebar-testimonial">
                <div class="testimonial-quote">
                    <i class="fas fa-quote-left"></i>
                    <p>La sécurité est un processus, pas un produit.</p>
                </div>
                <div class="testimonial-author">
                    <div class="author-avatar-sm">
                        <i class="fas fa-user-shield"></i>
                    </div>
                    <div class="author-info-sm">
                        <strong>Bruce Schneier</strong>
                        <span>Expert en sécurité informatique</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <%-- ============================================================
         PANNEAU DROIT : FORMULAIRE
         ============================================================ --%>
    <div class="auth-form-container">
        <div class="auth-form-wrapper">
            
            <%-- En-tête du formulaire --%>
            <div class="auth-form-header">
                <div class="form-icon-circle">
                    <i class="fas fa-key"></i>
                </div>
                <h1>Réinitialiser le mot de passe</h1>
                <p>Entrez votre adresse email pour recevoir un nouveau mot de passe</p>
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
            <form action="${pageContext.request.contextPath}/mot-de-passe-oublie" method="POST" id="formMdpOublie" novalidate>
                
                <%-- ===== CHAMP EMAIL ===== --%>
                <div class="form-group">
                    <label for="email">
                        <i class="fas fa-envelope"></i>
                        <span>Adresse email</span>
                    </label>
                    <div class="input-wrapper">
                        <i class="fas fa-envelope input-icon"></i>
                        <input 
                            type="email" 
                            id="email" 
                            name="email" 
                            placeholder="Entrez votre adresse email"
                            value="<%= oldEmail != null ? oldEmail : "" %>"
                            required
                            autocomplete="email"
                            autofocus
                        >
                        <span class="input-status" id="emailStatus"></span>
                    </div>
                    <span class="form-hint">
                        <i class="fas fa-info-circle"></i> 
                        Vous recevrez un nouveau mot de passe à cette adresse si un compte existe.
                    </span>
                    <span class="form-error" id="emailError"></span>
                </div>
                
                <%-- ===== BOUTON D'ENVOI ===== --%>
                <button type="submit" class="btn-auth btn-auth-primary" id="btnResetMdp">
                    <i class="fas fa-paper-plane"></i>
                    <span>Envoyer le nouveau mot de passe</span>
                    <span class="btn-loader" id="btnLoader">
                        <i class="fas fa-spinner fa-spin"></i>
                    </span>
                </button>
            </form>
            
            <%-- Séparateur --%>
            <div class="auth-separator">
                <span>OU</span>
            </div>
            
            <%-- Lien connexion --%>
            <div class="auth-redirect">
                <i class="fas fa-sign-in-alt"></i>
                <span>Vous vous souvenez de votre mot de passe ?</span>
                <a href="${pageContext.request.contextPath}/connexion">
                    Se connecter
                    <i class="fas fa-arrow-right"></i>
                </a>
            </div>
            
            <%-- Lien inscription --%>
            <div class="auth-redirect" style="margin-top: 10px;">
                <i class="fas fa-user-plus"></i>
                <span>Pas encore de compte ?</span>
                <a href="${pageContext.request.contextPath}/inscription">
                    Créer un compte
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