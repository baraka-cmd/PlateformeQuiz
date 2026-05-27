<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.time.Year" %>

<%-- ============================================================
     Fichier : footer.jsp
     Rôle    : Pied de page commun à toutes les pages
              - Ferme le conteneur principal
              - Affiche les informations de la plateforme
              - Liens vers réseaux sociaux et contact
              - Copyright dynamique
              - Ferme les balises <main>, <body>, <html>
     ============================================================ --%>

</div>
<%-- Fin du conteneur principal --%>

<%-- ============================================================
     PIED DE PAGE
     Structure en 4 colonnes sur desktop, empilées sur mobile
     ============================================================ --%>
<footer class="footer" role="contentinfo">
    
    <%-- Vague décorative en haut du footer --%>
    <div class="footer-wave">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 120" preserveAspectRatio="none">
            <path fill="rgba(255, 255, 255, 0.97)" d="M0,64L80,69.3C160,75,320,85,480,80C640,75,800,53,960,48C1120,43,1280,53,1360,58.7L1440,64L1440,120L1360,120C1280,120,1120,120,960,120C800,120,640,120,480,120C320,120,160,120,80,120L0,120Z"></path>
        </svg>
    </div>
    
    <%-- Contenu principal du footer --%>
    <div class="footer-content">
        
        <%-- Colonne 1 : Logo et description --%>
        <div class="footer-column footer-brand">
            <a href="${pageContext.request.contextPath}/" class="footer-logo">
                <i class="fas fa-brain"></i>
                <span>AmijayQuiz</span>
            </a>
            <p class="footer-description">
                Plateforme de quiz interactive pour tester, améliorer et valider vos connaissances. 
                Progressez niveau par niveau et devenez expert dans votre domaine.
            </p>
            <div class="footer-social">
                <a href="https://facebook.com/BarakaNtwaliPuruzi" target="_blank" rel="noopener noreferrer" 
                   class="social-link" aria-label="Facebook Ir Baraka Ntwali Puruzi" title="Facebook">
                    <i class="fab fa-facebook-f"></i>
                </a>
                <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" 
                   class="social-link" aria-label="Twitter" title="Twitter">
                    <i class="fab fa-x-twitter"></i>
                </a>
                <a href="https://github.com" target="_blank" rel="noopener noreferrer" 
                   class="social-link" aria-label="GitHub" title="GitHub">
                    <i class="fab fa-github"></i>
                </a>
                <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer" 
                   class="social-link" aria-label="LinkedIn" title="LinkedIn">
                    <i class="fab fa-linkedin-in"></i>
                </a>
                <a href="https://instagram.com" target="_blank" rel="noopener noreferrer" 
                   class="social-link" aria-label="Instagram" title="Instagram">
                    <i class="fab fa-instagram"></i>
                </a>
            </div>
        </div>
        
        <%-- Colonne 2 : Liens rapides --%>
        <div class="footer-column footer-links">
            <h4 class="footer-title">
                <i class="fas fa-link"></i> Liens rapides
            </h4>
            <ul>
                <li><a href="${pageContext.request.contextPath}/"><i class="fas fa-chevron-right"></i> Accueil</a></li>
                <li><a href="${pageContext.request.contextPath}/inscription"><i class="fas fa-chevron-right"></i> Créer un compte</a></li>
                <li><a href="${pageContext.request.contextPath}/connexion"><i class="fas fa-chevron-right"></i> Se connecter</a></li>
                <% if (session.getAttribute("utilisateur") != null && "joueur".equals(((com.amijayquiz.models.Utilisateur)session.getAttribute("utilisateur")).getRole())) { %>
                    <li><a href="${pageContext.request.contextPath}/joueur/dashboard"><i class="fas fa-chevron-right"></i> Mon parcours</a></li>
                    <li><a href="${pageContext.request.contextPath}/joueur/historique"><i class="fas fa-chevron-right"></i> Historique</a></li>
                <% } %>
            </ul>
        </div>
        
        <%-- Colonne 3 : Ressources --%>
        <div class="footer-column footer-resources">
            <h4 class="footer-title">
                <i class="fas fa-book-open"></i> Ressources
            </h4>
            <ul>
                <li><a href="#"><i class="fas fa-chevron-right"></i> Guide du débutant</a></li>
                <li><a href="#"><i class="fas fa-chevron-right"></i> Conseils et astuces</a></li>
                <li><a href="#"><i class="fas fa-chevron-right"></i> FAQ</a></li>
                <li><a href="#"><i class="fas fa-chevron-right"></i> Blog</a></li>
                <li><a href="#"><i class="fas fa-chevron-right"></i> Conditions d'utilisation</a></li>
            </ul>
        </div>
        
        <%-- Colonne 4 : Contact --%>
        <div class="footer-column footer-contact">
            <h4 class="footer-title">
                <i class="fas fa-address-card"></i> Contact
            </h4>
            <ul class="contact-list">
                <li>
                    <i class="fas fa-user"></i>
                    <span><strong>Ir Baraka Ntwali Puruzi</strong></span>
                </li>
                <li>
                    <i class="fas fa-envelope"></i>
                    <a href="mailto:barakantwali06@gmail.com">barakantwali06@gmail.com</a>
                </li>
                <li>
                    <i class="fas fa-phone"></i>
                    <a href="tel:+243984270438">+243 984 270 438</a>
                </li>
                <li>
                    <i class="fas fa-map-marker-alt"></i>
                    <span>République Démocratique du Congo</span>
                </li>
            </ul>
            <a href="mailto:barakantwali06@gmail.com" class="btn-contact">
                <i class="fas fa-paper-plane"></i> Nous contacter
            </a>
        </div>
        
    </div>
    
    <%-- Barre inférieure : copyright et mentions --%>
    <div class="footer-bottom">
        <div class="footer-bottom-content">
            <p class="copyright">
                <i class="far fa-copyright"></i> 
                <%= Year.now().getValue() %> <strong>AmijayQuiz</strong>. Tous droits réservés.
            </p>
            <p class="footer-tagline">
                Développé avec 
                <i class="fas fa-heart heart-beat"></i> 
                et 
                <i class="fas fa-coffee"></i> 
                par l'équipe AmijayQuiz
            </p>
            <div class="footer-legal">
                <a href="#">Confidentialité</a>
                <span class="separator">|</span>
                <a href="#">Conditions</a>
                <span class="separator">|</span>
                <a href="#">Cookies</a>
            </div>
        </div>
    </div>
    
</footer>

<%-- ============================================================
     SCRIPTS GLOBAUX
     Chargés en fin de page pour optimiser le rendu
     ============================================================ --%>
<%-- Script principal de la plateforme --%>
<script src="${pageContext.request.contextPath}/views/assets/js/main.js"></script>

<%-- Bloc pour les scripts spécifiques à chaque page --%>
<%= request.getAttribute("extraScripts") != null ? request.getAttribute("extraScripts") : "" %>

</body>
</html>