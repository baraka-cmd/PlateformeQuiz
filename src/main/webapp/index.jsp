<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.learnpath.models.Utilisateur" %>

<%-- ============================================================
     FICHIER : index.jsp (Page d'accueil)
     RÔLE    : Vitrine de la plateforme LearnPath
              - Hero section avec image de fond et overlay
              - Statistiques de la plateforme
              - Pourquoi choisir LearnPath ?
              - Comment ca marche ?
              - Deux modes d'apprentissage (Quiz + Cours)
              - Themes disponibles
              - Temoignages
              - Offre Premium
              - FAQ
              - Call-to-action final
     ============================================================ --%>

<%-- ===== GARDE UNIQUEMENT CECI ===== --%>
<%
    // Recuperation du pseudo pour le message de bienvenue
    // utilisateurConnecte et role sont deja definis dans header.jsp
    String pseudoAccueil = null;
    String roleAccueil = null;

    if (session != null && session.getAttribute("utilisateur") != null) {
        com.learnpath.models.Utilisateur userAccueil = 
            (com.learnpath.models.Utilisateur) session.getAttribute("utilisateur");
        pseudoAccueil = userAccueil.getPseudo();
        roleAccueil = userAccueil.getRole();
    }

    request.setAttribute("pageTitle", "LearnPath - Plateforme de Quiz & Cours en Ligne | Progressez niveau par niveau");

    String accueilCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/accueil.css\">";
    request.setAttribute("extraHead", accueilCSS);

    String accueilJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/accueil.js\"></script>";
    request.setAttribute("extraScripts", accueilJS);
%>

<%@ include file="views/header.jsp" %>

<%-- ============================================================
     HERO SECTION
     ============================================================ --%>
<section class="hero-section">
    <div class="hero-overlay"></div>
    <div class="hero-bg-image"></div>

    <div class="hero-content">
        <div class="hero-badge">
            <i class="fas fa-award"></i>
            <span>Plateforme N°1 de Quiz et Cours en RDC</span>
        </div>

        <h1 class="hero-title">
            <span class="hero-icon">
                <i class="fas fa-brain"></i>
            </span>
            Learn<span class="hero-highlight">Path</span>
        </h1>

        <p class="hero-subtitle">
            Apprenez, testez vos connaissances et obtenez des <strong>certificats verifiables</strong>.<br>
            Progressez niveau par niveau et devenez expert dans votre domaine.
        </p>

        <div class="hero-actions">
            <% if (utilisateurConnecte == null) { %>
                <a href="${pageContext.request.contextPath}/inscription" class="btn-hero btn-hero-primary">
                    <i class="fas fa-rocket"></i>
                    <span>Commencer gratuitement</span>
                </a>
                <a href="${pageContext.request.contextPath}/connexion" class="btn-hero btn-hero-outline">
                    <i class="fas fa-sign-in-alt"></i>
                    <span>J'ai deja un compte</span>
                </a>
            <% } else if ("admin".equals(roleHeader)) { %>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-hero btn-hero-primary">
                    <i class="fas fa-tachometer-alt"></i>
                    <span>Dashboard Administration</span>
                </a>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-hero btn-hero-primary">
                    <i class="fas fa-play-circle"></i>
                    <span>Continuer les Quiz</span>
                </a>
                <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-hero btn-hero-outline">
                    <i class="fas fa-graduation-cap"></i>
                    <span>Voir les Cours</span>
                </a>
            <% } %>
        </div>

        <% if (utilisateurConnecte != null && "joueur".equals(roleHeader)) { %>
            <div class="hero-welcome">
                <div class="welcome-avatar-sm">
                    <i class="fas fa-user"></i>
                </div>
                <span>Bon retour, <strong><%= pseudoAccueil %></strong> ! Reprenez votre apprentissage.</span>
            </div>
        <% } %>

        <div class="hero-trust">
            <span class="trust-item">
                <i class="fas fa-check-circle"></i> 500+ apprenants actifs
            </span>
            <span class="trust-separator">|</span>
            <span class="trust-item">
                <i class="fas fa-check-circle"></i> 120+ quiz disponibles
            </span>
            <span class="trust-separator">|</span>
            <span class="trust-item">
                <i class="fas fa-check-circle"></i> 45+ cours complets
            </span>
        </div>
    </div>

    <div class="hero-scroll-indicator">
        <span>Decouvrir</span>
        <i class="fas fa-chevron-down"></i>
    </div>
</section>

<%-- ============================================================
     SECTION STATISTIQUES
     ============================================================ --%>
<section class="stats-section">
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon-wrapper">
                <i class="fas fa-users"></i>
            </div>
            <div class="stat-number">500<span class="stat-plus">+</span></div>
            <p class="stat-label">Apprenants actifs</p>
        </div>
        <div class="stat-card">
            <div class="stat-icon-wrapper">
                <i class="fas fa-question-circle"></i>
            </div>
            <div class="stat-number">120<span class="stat-plus">+</span></div>
            <p class="stat-label">Quiz disponibles</p>
        </div>
        <div class="stat-card">
            <div class="stat-icon-wrapper">
                <i class="fas fa-book"></i>
            </div>
            <div class="stat-number">45<span class="stat-plus">+</span></div>
            <p class="stat-label">Cours complets</p>
        </div>
        <div class="stat-card">
            <div class="stat-icon-wrapper">
                <i class="fas fa-star"></i>
            </div>
            <div class="stat-number">95<span class="stat-plus">%</span></div>
            <p class="stat-label">Satisfaction</p>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION POURQUOI CHOISIR LearnPath ?
     ============================================================ --%>
<section class="features-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-star"></i> Avantages</span>
        <h2>Pourquoi choisir LearnPath ?</h2>
        <p class="section-subtitle">Une plateforme complete pour apprendre et se certifier</p>
    </div>

    <div class="features-grid">
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-brain"></i>
            </div>
            <h3>Quiz et Cours combines</h3>
            <p>Testez vos connaissances avec des quiz ET apprenez en profondeur avec des cours complets.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
                <i class="fas fa-clock"></i>
            </div>
            <h3>Quiz chronometres</h3>
            <p>Testez-vous en conditions reelles avec un temps limite pour chaque quiz.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                <i class="fas fa-chart-line"></i>
            </div>
            <h3>Suivi de progression</h3>
            <p>Visualisez votre evolution sur les quiz et les cours en temps reel.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                <i class="fas fa-certificate"></i>
            </div>
            <h3>Certificats verifiables</h3>
            <p>Obtenez des certificats avec code unique apres avoir reussi les examens finaux.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-trophy"></i>
            </div>
            <h3>Systeme de niveaux</h3>
            <p>Debloquez les niveaux superieurs en validant toutes les parties du niveau en cours.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                <i class="fas fa-mobile-alt"></i>
            </div>
            <h3>100% Responsive</h3>
            <p>Accedez a la plateforme depuis votre ordinateur, tablette ou smartphone.</p>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION COMMENT CA MARCHE ?
     ============================================================ --%>
<section class="how-it-works">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-info-circle"></i> Processus</span>
        <h2>Comment ca marche ?</h2>
        <p class="section-subtitle">Trois etapes simples pour commencer votre apprentissage</p>
    </div>

    <div class="steps-grid">
        <div class="step-card">
            <div class="step-number">1</div>
            <div class="step-icon"><i class="fas fa-user-plus"></i></div>
            <h3>Creez votre compte</h3>
            <p>Inscrivez-vous gratuitement en moins de 2 minutes et accedez a tous les quiz elementaires et cours gratuits.</p>
            <% if (utilisateurConnecte == null) { %>
                <a href="${pageContext.request.contextPath}/inscription" class="step-link">S'inscrire <i class="fas fa-arrow-right"></i></a>
            <% } %>
        </div>

        <div class="step-card">
            <div class="step-number">2</div>
            <div class="step-icon"><i class="fas fa-layer-group"></i></div>
            <h3>Choisissez votre mode</h3>
            <p><strong>Quiz</strong> pour tester vos connaissances ou <strong>Cours</strong> pour apprendre en profondeur avec des lecons detaillees.</p>
            <% if (utilisateurConnecte != null && "joueur".equals(roleHeader)) { %>
                <a href="${pageContext.request.contextPath}/joueur/dashboard" class="step-link">Voir les quiz <i class="fas fa-arrow-right"></i></a>
            <% } %>
        </div>

        <div class="step-card">
            <div class="step-number">3</div>
            <div class="step-icon"><i class="fas fa-certificate"></i></div>
            <h3>Progressez et certifiez</h3>
            <p>Debloquez des niveaux, reussissez des examens et obtenez des <strong>certificats verifiables</strong> a vie.</p>
            <% if (utilisateurConnecte != null && "joueur".equals(roleHeader)) { %>
                <a href="${pageContext.request.contextPath}/joueur/cours" class="step-link">Voir les cours <i class="fas fa-arrow-right"></i></a>
            <% } %>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION DEUX MODES D'APPRENTISSAGE
     ============================================================ --%>
<section class="dual-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-th-large"></i> Apprentissage</span>
        <h2>Deux facons d'apprendre</h2>
        <p class="section-subtitle">Choisissez le mode qui vous convient le mieux</p>
    </div>

    <div class="dual-grid">
        <div class="dual-card quiz-card">
            <div class="dual-card-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-layer-group"></i>
            </div>
            <h3>Quiz Interactifs</h3>
            <p>6 themes - 3 niveaux - 20 parties par niveau</p>
            <ul class="dual-card-features">
                <li><i class="fas fa-check-circle"></i> 10 questions par quiz</li>
                <li><i class="fas fa-check-circle"></i> Chronometre de 3 minutes</li>
                <li><i class="fas fa-check-circle"></i> Score minimum : 8/10</li>
                <li><i class="fas fa-check-circle"></i> Niveaux qui se debloquent</li>
                <li><i class="fas fa-check-circle"></i> Correction detaillee</li>
            </ul>
            <a href="${pageContext.request.contextPath}/joueur/dashboard" class="dual-card-btn">
                <i class="fas fa-play"></i> Commencer les Quiz
            </a>
        </div>

        <div class="dual-card cours-card">
            <div class="dual-card-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-graduation-cap"></i>
            </div>
            <h3>Cours Complets</h3>
            <p>12 themes - 3 niveaux - 15 modules par cours</p>
            <ul class="dual-card-features">
                <li><i class="fas fa-check-circle"></i> Lecons detaillees avec exemples</li>
                <li><i class="fas fa-check-circle"></i> Code source pret a copier-coller</li>
                <li><i class="fas fa-check-circle"></i> Ressources telechargeables</li>
                <li><i class="fas fa-check-circle"></i> Examen par module (75%)</li>
                <li><i class="fas fa-check-circle"></i> Certificat verifiable (80%)</li>
            </ul>
            <a href="${pageContext.request.contextPath}/joueur/cours" class="dual-card-btn">
                <i class="fas fa-book"></i> Explorer les Cours
            </a>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION THEMES DISPONIBLES
     ============================================================ --%>
<section class="themes-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-book-open"></i> Contenu</span>
        <h2>Nos themes disponibles</h2>
        <p class="section-subtitle">Explorez nos differents domaines de connaissances</p>
    </div>

    <div class="themes-grid">
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #667eea, #5a6fd6);">
                <i class="fas fa-globe"></i>
            </div>
            <h4>Developpement Web</h4>
            <p>HTML, CSS, JavaScript, React, Node.js...</p>
            <span class="theme-badge">7 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #f093fb, #e084ef);">
                <i class="fas fa-coffee"></i>
            </div>
            <h4>Langage Java</h4>
            <p>Java SE, Spring Boot, Hibernate...</p>
            <span class="theme-badge">7 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #4facfe, #3d9be0);">
                <i class="fab fa-python"></i>
            </div>
            <h4>Langage Python</h4>
            <p>Django, Flask, Data Science, IA...</p>
            <span class="theme-badge">6 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #43e97b, #36d06a);">
                <i class="fas fa-database"></i>
            </div>
            <h4>Bases de Donnees</h4>
            <p>SQL, MySQL, PostgreSQL, MongoDB...</p>
            <span class="theme-badge">6 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #f5576c, #e0485c);">
                <i class="fas fa-shield-alt"></i>
            </div>
            <h4>Cybersecurite</h4>
            <p>Securite Web, Reseau, Pentesting...</p>
            <span class="theme-badge">6 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #f39c12, #da8c10);">
                <i class="fas fa-mobile-alt"></i>
            </div>
            <h4>Dev. Mobile</h4>
            <p>Flutter, React Native, Kotlin...</p>
            <span class="theme-badge">4 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                <i class="fas fa-robot"></i>
            </div>
            <h4>Intelligence Artificielle</h4>
            <p>Machine Learning, Deep Learning...</p>
            <span class="theme-badge">4 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #e91e63, #c2185b);">
                <i class="fas fa-palette"></i>
            </div>
            <h4>Design UI/UX</h4>
            <p>Figma, Adobe XD, Design Systems...</p>
            <span class="theme-badge">3 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #00bcd4, #0097a7);">
                <i class="fas fa-network-wired"></i>
            </div>
            <h4>Reseaux Informatiques</h4>
            <p>TCP/IP, Routage, Securite Reseau...</p>
            <span class="theme-badge">3 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #ff9800, #f57c00);">
                <i class="fab fa-linux"></i>
            </div>
            <h4>Systemes d'Exploitation</h4>
            <p>Linux, Windows Server, Bash...</p>
            <span class="theme-badge">3 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #795548, #5d4037);">
                <i class="fas fa-chart-pie"></i>
            </div>
            <h4>Data Science</h4>
            <p>Pandas, NumPy, Visualisation...</p>
            <span class="theme-badge">3 cours</span>
        </div>
        <div class="theme-card">
            <div class="theme-card-icon" style="background: linear-gradient(135deg, #607d8b, #455a64);">
                <i class="fab fa-php"></i>
            </div>
            <h4>Langage PHP</h4>
            <p>Laravel, Symfony, API REST...</p>
            <span class="theme-badge">4 cours</span>
        </div>
    </div>

    <% if (utilisateurConnecte != null && "joueur".equals(roleHeader)) { %>
        <div class="themes-cta">
            <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-hero btn-hero-primary">
                <i class="fas fa-graduation-cap"></i>
                <span>Explorer tous les cours</span>
            </a>
        </div>
    <% } %>
</section>

<%-- ============================================================
     SECTION TEMOIGNAGES
     ============================================================ --%>
<section class="testimonials-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-comment-dots"></i> Avis</span>
        <h2>Ce que disent nos utilisateurs</h2>
        <p class="section-subtitle">La satisfaction de nos apprenants est notre priorite</p>
    </div>

    <div class="testimonials-grid">
        <div class="testimonial-card">
            <div class="testimonial-stars">
                <i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i>
                <i class="fas fa-star"></i><i class="fas fa-star"></i>
            </div>
            <p class="testimonial-text">"Grace aux cours et quiz d'LearnPath, j'ai pu decrocher mon premier emploi en developpement web. Les certificats sont reconnus et la progression est tres motivante !"</p>
            <div class="testimonial-author">
                <div class="author-avatar"><i class="fas fa-user-graduate"></i></div>
                <div class="author-info">
                    <strong>Jean M.</strong>
                    <span>Developpeur Web Junior</span>
                </div>
            </div>
        </div>
        <div class="testimonial-card">
            <div class="testimonial-stars">
                <i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i>
                <i class="fas fa-star"></i><i class="fas fa-star"></i>
            </div>
            <p class="testimonial-text">"Excellente plateforme ! Les cours sont bien structures avec des exemples de code concrets. Les quiz permettent de valider ses connaissances efficacement."</p>
            <div class="testimonial-author">
                <div class="author-avatar"><i class="fas fa-user-tie"></i></div>
                <div class="author-info">
                    <strong>Marie K.</strong>
                    <span>Professionnelle IT</span>
                </div>
            </div>
        </div>
        <div class="testimonial-card">
            <div class="testimonial-stars">
                <i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i>
                <i class="fas fa-star"></i><i class="fas fa-star-half-alt"></i>
            </div>
            <p class="testimonial-text">"Le systeme de niveaux qui se debloquent progressivement est tres motivant. On a envie d'aller jusqu'au bout et de decrocher tous les certificats !"</p>
            <div class="testimonial-author">
                <div class="author-avatar"><i class="fas fa-user"></i></div>
                <div class="author-info">
                    <strong>Patrick T.</strong>
                    <span>Etudiant passionne</span>
                </div>
            </div>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION OFFRE PREMIUM
     ============================================================ --%>
<section class="premium-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-crown"></i> Premium</span>
        <h2>Debloquez toute la plateforme</h2>
        <p class="section-subtitle">Un seul paiement, acces a vie a tout le contenu</p>
    </div>

    <div class="premium-offer-card">
        <div class="premium-offer-glow"></div>
        <div class="premium-offer-icon">
            <i class="fas fa-crown"></i>
        </div>
        <h3>Pass Premium</h3>
        <div class="premium-offer-price">
            <span class="price-amount">4$</span>
            <span class="price-separator">ou</span>
            <span class="price-amount">10 000 FC</span>
        </div>
        <p class="premium-offer-duration">Paiement unique - Acces a vie</p>

        <ul class="premium-offer-features">
            <li><i class="fas fa-check-circle"></i> Tous les cours Moyen et Avance (12 themes)</li>
            <li><i class="fas fa-check-circle"></i> Tous les modules (2 a 15) de chaque cours</li>
            <li><i class="fas fa-check-circle"></i> Tous les quiz (360+ parties)</li>
            <li><i class="fas fa-check-circle"></i> Certificats verifiables a vie</li>
            <li><i class="fas fa-check-circle"></i> Acces illimite a toute la plateforme</li>
            <li><i class="fas fa-check-circle"></i> Support prioritaire</li>
        </ul>

        <a href="${pageContext.request.contextPath}/paiement" class="premium-offer-btn">
            <i class="fas fa-unlock-alt"></i>
            <span>Debloquer maintenant</span>
        </a>
        <p class="premium-offer-garantie">
            <i class="fas fa-shield-alt"></i> Paiement 100% securise par PawaPay
        </p>
    </div>
</section>

<%-- ============================================================
     SECTION FAQ
     ============================================================ --%>
<section class="faq-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-question-circle"></i> FAQ</span>
        <h2>Questions frequentes</h2>
        <p class="section-subtitle">Tout ce que vous devez savoir sur LearnPath</p>
    </div>

    <div class="faq-grid">
        <div class="faq-item">
            <div class="faq-question" onclick="toggleFaq(this)">
                <h4><i class="fas fa-chevron-right"></i> Comment acceder aux cours et quiz ?</h4>
            </div>
            <div class="faq-answer">
                <p>Creez un compte gratuitement, puis accedez a votre dashboard. Les quiz elementaires et le module 1 de chaque cours sont gratuits. Pour tout debloquer, passez Premium (4$ ou 10 000 FC).</p>
            </div>
        </div>
        <div class="faq-item">
            <div class="faq-question" onclick="toggleFaq(this)">
                <h4><i class="fas fa-chevron-right"></i> Comment fonctionne le systeme de niveaux ?</h4>
            </div>
            <div class="faq-answer">
                <p>Chaque theme a 3 niveaux : Elementaire, Moyen et Avance. Vous devez terminer le niveau Elementaire pour debloquer le Moyen, puis le Moyen pour l'Avance. Chaque niveau contient des quiz et/ou des cours a completer.</p>
            </div>
        </div>
        <div class="faq-item">
            <div class="faq-question" onclick="toggleFaq(this)">
                <h4><i class="fas fa-chevron-right"></i> Les certificats sont-ils reconnus ?</h4>
            </div>
            <div class="faq-answer">
                <p>Chaque certificat possede un code de verification unique. Vous pouvez le partager sur LinkedIn, l'ajouter a votre CV, et les employeurs peuvent verifier son authenticite en ligne.</p>
            </div>
        </div>
        <div class="faq-item">
            <div class="faq-question" onclick="toggleFaq(this)">
                <h4><i class="fas fa-chevron-right"></i> Le paiement est-il securise ?</h4>
            </div>
            <div class="faq-answer">
                <p>Oui, le paiement est traite par <strong>PawaPay</strong>, une plateforme certifiee. Vous payez via Mobile Money (Airtel Money, Orange Money, M-Pesa) en toute securite. Un seul paiement de 4$ ou 10 000 FC vous donne acces a vie.</p>
            </div>
        </div>
        <div class="faq-item">
            <div class="faq-question" onclick="toggleFaq(this)">
                <h4><i class="fas fa-chevron-right"></i> Puis-je apprendre sur mobile ?</h4>
            </div>
            <div class="faq-answer">
                <p>Absolument ! La plateforme est 100% responsive. Vous pouvez apprendre depuis votre ordinateur, tablette ou smartphone. L'interface s'adapte automatiquement a la taille de votre ecran.</p>
            </div>
        </div>
        <div class="faq-item">
            <div class="faq-question" onclick="toggleFaq(this)">
                <h4><i class="fas fa-chevron-right"></i> Y a-t-il un abonnement mensuel ?</h4>
            </div>
            <div class="faq-answer">
                <p>Non ! Un seul paiement de 4$ ou 10 000 FC vous donne acces a vie a toute la plateforme. Pas d'abonnement, pas de frais caches. Payez une fois, apprenez pour toujours.</p>
            </div>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION CALL-TO-ACTION FINAL
     ============================================================ --%>
<section class="cta-section">
    <div class="cta-content">
        <h2>Pret a commencer votre apprentissage ?</h2>
        <p>Rejoignez des centaines d'apprenants qui progressent chaque jour sur LearnPath.</p>

        <% if (utilisateurConnecte == null) { %>
            <div class="cta-buttons">
                <a href="${pageContext.request.contextPath}/inscription" class="btn-hero btn-hero-primary">
                    <i class="fas fa-user-plus"></i>
                    <span>Creer mon compte gratuit</span>
                </a>
                <p class="cta-note"><i class="fas fa-lock"></i> Inscription rapide et securisee. Aucun paiement requis pour commencer.</p>
            </div>
        <% } else if ("joueur".equals(roleHeader)) { %>
            <div class="cta-buttons">
                <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-hero btn-hero-primary">
                    <i class="fas fa-play"></i>
                    <span>Continuer les Quiz</span>
                </a>
                <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-hero btn-hero-outline" style="margin-top: 10px;">
                    <i class="fas fa-graduation-cap"></i>
                    <span>Voir les Cours</span>
                </a>
            </div>
        <% } %>
    </div>
</section>

<%@ include file="views/footer-index.jsp" %>