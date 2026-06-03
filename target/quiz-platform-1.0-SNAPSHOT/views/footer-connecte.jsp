<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.time.Year" %>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     FICHIER : footer-connecte.jsp
     RÔLE    : Pied de page pour utilisateurs CONNECTES
              - Ferme le conteneur principal
              - 4 colonnes : Plateforme, Navigation, Ressources, Contact
              - Liens adaptes au joueur connecte
              - Copyright dynamique
              - Ferme les balises <main>, <body>, <html>
     ============================================================ --%>

</div>
<%-- Fin du conteneur principal --%>

<%
    // Recuperation de l'utilisateur connecte
    Utilisateur utilisateurFooter = null;
    String roleFooter = null;
    String pseudoFooter = null;

    if (session != null && session.getAttribute("utilisateur") != null) {
        utilisateurFooter = (Utilisateur) session.getAttribute("utilisateur");
        roleFooter = utilisateurFooter.getRole();
        pseudoFooter = utilisateurFooter.getPseudo();
    }
%>

<%-- ============================================================
     PIED DE PAGE - VERSION CONNECTEE
     ============================================================ --%>
<footer class="footer" role="contentinfo">

    <%-- Vague decorative en haut du footer --%>
    <div class="footer-wave">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 120" preserveAspectRatio="none">
            <path fill="rgba(255, 255, 255, 0.97)" d="M0,64L80,69.3C160,75,320,85,480,80C640,75,800,53,960,48C1120,43,1280,53,1360,58.7L1440,64L1440,120L1360,120C1280,120,1120,120,960,120C800,120,640,120,480,120C320,120,160,120,80,120L0,120Z"></path>
        </svg>
    </div>

    <%-- Contenu principal du footer --%>
    <div class="footer-content">

        <%-- ============================================================
             Colonne 1 : Logo et description de la plateforme
             ============================================================ --%>
        <div class="footer-column footer-brand">
            <a href="${pageContext.request.contextPath}/" class="footer-logo">
                <i class="fas fa-brain"></i>
                <span>AmijayQuiz</span>
            </a>
            <p class="footer-description">
                Plateforme de quiz et cours en ligne. Testez vos connaissances,
                progressez niveau par niveau et obtenez des certificats verifiables.
                Apprenez a votre rythme, ou que vous soyez.
            </p>

            <%-- Statut Premium --%>
            <div class="footer-premium-status">
                <%
                    boolean estPremiumFooter = false;
                    if (utilisateurFooter != null && "joueur".equals(roleFooter)) {
                        com.amijayquiz.dao.AccessPremiumDAO premiumDAOFooter = new com.amijayquiz.dao.AccessPremiumDAO();
                        estPremiumFooter = premiumDAOFooter.estPremium(utilisateurFooter.getId());
                    }

                    if (estPremiumFooter) {
                %>
                    <span class="footer-premium-badge footer-premium-actif">
                        <i class="fas fa-crown"></i> Premium actif
                    </span>
                <%
                    } else if ("joueur".equals(roleFooter)) {
                %>
                    <a href="${pageContext.request.contextPath}/paiement" class="footer-premium-badge footer-premium-inactif">
                        <i class="fas fa-crown"></i> Devenir Premium (4$ ou 10 000 FC)
                    </a>
                <%
                    }
                %>
            </div>

            <div class="footer-social">
                <a href="https://facebook.com/BarakaNtwaliPuruzi" target="_blank" rel="noopener noreferrer"
                   class="social-link" aria-label="Facebook" title="Facebook">
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

        <%-- ============================================================
             Colonne 2 : Navigation rapide (connecte)
             ============================================================ --%>
        <div class="footer-column footer-links">
            <h4 class="footer-title">
                <i class="fas fa-compass"></i> Navigation rapide
            </h4>
            <ul>
                <% if ("admin".equals(roleFooter)) { %>
                    <li><a href="${pageContext.request.contextPath}/admin/dashboard"><i class="fas fa-chevron-right"></i> Dashboard Admin</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/questions"><i class="fas fa-chevron-right"></i> Gerer les questions</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/quiz"><i class="fas fa-chevron-right"></i> Gerer les quiz</a></li>
                    <li><a href="${pageContext.request.contextPath}/admin/resultats"><i class="fas fa-chevron-right"></i> Resultats</a></li>
                <% } else { %>
                    <li><a href="${pageContext.request.contextPath}/joueur/dashboard"><i class="fas fa-chevron-right"></i> Mon Dashboard</a></li>
                    <li><a href="${pageContext.request.contextPath}/joueur/cours"><i class="fas fa-chevron-right"></i> Parcourir les cours</a></li>
                    <li><a href="${pageContext.request.contextPath}/joueur/dashboard#quiz-section"><i class="fas fa-chevron-right"></i> Parcourir les quiz</a></li>
                    <li><a href="${pageContext.request.contextPath}/joueur/historique"><i class="fas fa-chevron-right"></i> Mon historique</a></li>
                    <li><a href="${pageContext.request.contextPath}/changer-mot-de-passe"><i class="fas fa-chevron-right"></i> Changer mon mot de passe</a></li>
                <% } %>
            </ul>
        </div>

        <%-- ============================================================
             Colonne 3 : Ressources et progression
             ============================================================ --%>
        <div class="footer-column footer-resources">
            <h4 class="footer-title">
                <i class="fas fa-chart-line"></i> Progression et ressources
            </h4>
            <ul>
                <% if ("joueur".equals(roleFooter)) { %>
                    <li><i class="fas fa-check-circle" style="color: #27ae60;"></i> Continuez votre apprentissage</li>
                    <li><i class="fas fa-check-circle" style="color: #27ae60;"></i> Debloquez tous les niveaux</li>
                    <li><i class="fas fa-check-circle" style="color: #27ae60;"></i> Obtenez des certificats</li>
                <% } else { %>
                    <li><i class="fas fa-check-circle" style="color: #27ae60;"></i> Gerer le contenu de la plateforme</li>
                    <li><i class="fas fa-check-circle" style="color: #27ae60;"></i> Suivre les performances</li>
                    <li><i class="fas fa-check-circle" style="color: #27ae60;"></i> Gerer les utilisateurs</li>
                <% } %>
            </ul>

            <% if ("joueur".equals(roleFooter) && !estPremiumFooter) { %>
                <a href="${pageContext.request.contextPath}/paiement" class="btn-contact" style="margin-top: 14px;">
                    <i class="fas fa-rocket"></i> Passer Premium
                </a>
            <% } %>
        </div>

        <%-- ============================================================
             Colonne 4 : Contact
             ============================================================ --%>
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
                    <span>Republique Democratique du Congo</span>
                </li>
            </ul>
            <a href="mailto:barakantwali06@gmail.com" class="btn-contact">
                <i class="fas fa-paper-plane"></i> Nous contacter
            </a>
        </div>

    </div>

    <%-- ============================================================
         BARRE INFERIEURE : COPYRIGHT ET MENTIONS
         ============================================================ --%>
    <div class="footer-bottom">
        <div class="footer-bottom-content">
            <p class="copyright">
                <i class="far fa-copyright"></i>
                <%= Year.now().getValue() %> <strong>AmijayQuiz</strong>. Tous droits reserves.
            </p>
            <p class="footer-tagline">
                Developpe avec
                <i class="fas fa-heart heart-beat"></i>
                et
                <i class="fas fa-coffee"></i>
                par l'equipe AmijayQuiz
            </p>
            <div class="footer-legal">
                <a href="#">Confidentialite</a>
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
     ============================================================ --%>
<script src="${pageContext.request.contextPath}/views/assets/js/main.js"></script>

<%-- Bloc pour les scripts specifiques a chaque page --%>
<%= request.getAttribute("extraScripts") != null ? request.getAttribute("extraScripts") : "" %>

</body>
</html>