<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     Fichier : index.jsp (Page d'accueil)
     Rôle    : Vitrine de la plateforme AmijayQuiz
              - Hero section avec animation
              - Statistiques de la plateforme
              - Fonctionnalités (Quiz + Cours)
              - Thèmes disponibles
              - Section Cours & Certifications
              - Témoignages
              - Call-to-action
     ============================================================ --%>

<%
    // Récupération de l'utilisateur connecté via la session
    String pseudo = null;
    
    if (session != null && session.getAttribute("utilisateur") != null) {
        Utilisateur user = (Utilisateur) session.getAttribute("utilisateur");
        pseudo = user.getPseudo();
    }
    
    // Titre de la page
    request.setAttribute("pageTitle", "AmijayQuiz - Quiz & Cours en Ligne | Progressez niveau par niveau");

    // Chargement des styles et scripts spécifiques
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
    <div class="hero-bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
    </div>
    
    <div class="hero-content">
        <div class="hero-badge">
            <i class="fas fa-award"></i>
            <span>Plateforme N°1 de Quiz & Cours en RDC</span>
        </div>
        
        <h1 class="hero-title">
            <span class="hero-icon">
                <i class="fas fa-brain"></i>
            </span>
            Amijay<span class="hero-highlight">Quiz</span>
        </h1>
        
        <p class="hero-subtitle">
            <strong>Quiz interactifs</strong> et <strong>cours complets</strong> en informatique<br>
            Progressez niveau par niveau et obtenez des <strong>certificats vérifiables</strong>.
        </p>
        
        <div class="hero-actions">
            <% if (utilisateurConnecte == null) { %>
                <a href="${pageContext.request.contextPath}/inscription" class="btn-hero btn-hero-primary">
                    <i class="fas fa-rocket"></i>
                    <span>Commencer gratuitement</span>
                </a>
                <a href="${pageContext.request.contextPath}/connexion" class="btn-hero btn-hero-outline">
                    <i class="fas fa-sign-in-alt"></i>
                    <span>J'ai déjà un compte</span>
                </a>
            <% } else if ("admin".equals(role)) { %>
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
        
        <%-- Message de bienvenue --%>
        <% if (utilisateurConnecte != null && !"admin".equals(role)) { %>
            <div class="hero-welcome">
                <div class="welcome-avatar-sm">
                    <i class="fas fa-user"></i>
                </div>
                <span>Bon retour, <strong><%= pseudo %></strong> ! Reprenez votre apprentissage.</span>
            </div>
        <% } %>
    </div>
    
    <%-- Illustration --%>
    <div class="hero-visual">
        <div class="hero-illustration">
            <div class="illustration-circle">
                <i class="fas fa-graduation-cap"></i>
            </div>
            <div class="floating-card floating-card-1">
                <i class="fas fa-check-circle"></i>
                <span>Quiz réussis</span>
            </div>
            <div class="floating-card floating-card-2">
                <i class="fas fa-certificate"></i>
                <span>Certificats</span>
            </div>
            <div class="floating-card floating-card-3">
                <i class="fas fa-chart-line"></i>
                <span>Progression</span>
            </div>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION STATISTIQUES
     ============================================================ --%>
<section class="stats-section">
    <div class="stats-grid">
        <div class="stat-item">
            <div class="stat-icon-wrapper"><i class="fas fa-users"></i></div>
            <div class="stat-number"><span class="counter">500</span><span>+</span></div>
            <p class="stat-label">Apprenants actifs</p>
        </div>
        <div class="stat-item">
            <div class="stat-icon-wrapper"><i class="fas fa-question-circle"></i></div>
            <div class="stat-number"><span class="counter">120</span><span>+</span></div>
            <p class="stat-label">Quiz disponibles</p>
        </div>
        <div class="stat-item">
            <div class="stat-icon-wrapper"><i class="fas fa-book"></i></div>
            <div class="stat-number"><span class="counter">45</span><span>+</span></div>
            <p class="stat-label">Cours complets</p>
        </div>
        <div class="stat-item">
            <div class="stat-icon-wrapper"><i class="fas fa-certificate"></i></div>
            <div class="stat-number"><span class="counter">95</span><span>%</span></div>
            <p class="stat-label">Satisfaction</p>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION COMMENT ÇA MARCHE ?
     ============================================================ --%>
<section class="how-it-works">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-info-circle"></i> Processus</span>
        <h2>Comment ça marche ?</h2>
        <p class="section-subtitle">Deux façons d'apprendre sur AmijayQuiz</p>
    </div>
    
    <div class="steps-grid">
        <div class="step-card">
            <div class="step-number">1</div>
            <div class="step-icon"><i class="fas fa-user-plus"></i></div>
            <h3>Créez votre compte</h3>
            <p>Inscrivez-vous gratuitement et accédez à tous les quiz élémentaires et cours gratuits.</p>
            <% if (utilisateurConnecte == null) { %>
                <a href="${pageContext.request.contextPath}/inscription" class="step-link">S'inscrire <i class="fas fa-arrow-right"></i></a>
            <% } %>
        </div>
        
        <div class="step-card">
            <div class="step-number">2</div>
            <div class="step-icon"><i class="fas fa-layer-group"></i></div>
            <h3>Choisissez votre mode</h3>
            <p><strong>Quiz</strong> pour tester vos connaissances ou <strong>Cours</strong> pour apprendre en profondeur.</p>
            <% if (utilisateurConnecte != null && "joueur".equals(role)) { %>
                <a href="${pageContext.request.contextPath}/joueur/dashboard" class="step-link">Voir les quiz <i class="fas fa-arrow-right"></i></a>
            <% } %>
        </div>
        
        <div class="step-card">
            <div class="step-number">3</div>
            <div class="step-icon"><i class="fas fa-certificate"></i></div>
            <h3>Progressez et certifiez</h3>
            <p>Débloquez des niveaux, réussissez des examens et obtenez des <strong>certificats vérifiables</strong>.</p>
            <% if (utilisateurConnecte != null && "joueur".equals(role)) { %>
                <a href="${pageContext.request.contextPath}/joueur/cours" class="step-link">Voir les cours <i class="fas fa-arrow-right"></i></a>
            <% } %>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION FONCTIONNALITÉS
     ============================================================ --%>
<section class="features-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-star"></i> Avantages</span>
        <h2>Pourquoi choisir AmijayQuiz ?</h2>
        <p class="section-subtitle">Une plateforme complète pour apprendre et se certifier</p>
    </div>
    
    <div class="features-grid">
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-brain"></i>
            </div>
            <h3>Quiz + Cours</h3>
            <p>Testez vos connaissances avec des quiz ET apprenez avec des cours complets.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
                <i class="fas fa-clock"></i>
            </div>
            <h3>Quiz chronométrés</h3>
            <p>Testez-vous en conditions réelles avec un temps limité pour chaque quiz.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                <i class="fas fa-chart-line"></i>
            </div>
            <h3>Suivi de progression</h3>
            <p>Visualisez votre évolution sur les quiz et les cours en temps réel.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                <i class="fas fa-certificate"></i>
            </div>
            <h3>Certificats vérifiables</h3>
            <p>Obtenez des certificats avec code unique après avoir réussi les examens finaux.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-trophy"></i>
            </div>
            <h3>Système de niveaux</h3>
            <p>Débloquez les niveaux supérieurs en validant toutes les parties du niveau en cours.</p>
        </div>
        <div class="feature-card">
            <div class="feature-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                <i class="fas fa-mobile-alt"></i>
            </div>
            <h3>100% Responsive</h3>
            <p>Accédez à la plateforme depuis votre ordinateur, tablette ou smartphone.</p>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION QUIZ + COURS
     ============================================================ --%>
<section class="dual-section">
    <div class="dual-grid">
        <%-- Carte Quiz --%>
        <div class="dual-card quiz-card">
            <div class="dual-card-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-layer-group"></i>
            </div>
            <h3>? Quiz Interactifs</h3>
            <p>6 thèmes ? 3 niveaux ? 60 quiz par thème<br>Testez vos connaissances et débloquez des niveaux.</p>
            <a href="${pageContext.request.contextPath}/joueur/dashboard" class="dual-card-btn">
                <i class="fas fa-play"></i> Commencer les Quiz
            </a>
        </div>
        
        <%-- Carte Cours --%>
        <div class="dual-card cours-card">
            <div class="dual-card-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-graduation-cap"></i>
            </div>
            <h3>? Cours Complets</h3>
            <p>12 thèmes ? 45 cours ? Certificats<br>Apprenez en profondeur et obtenez des certifications.</p>
            <a href="${pageContext.request.contextPath}/joueur/cours" class="dual-card-btn">
                <i class="fas fa-book"></i> Explorer les Cours
            </a>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION THÈMES DISPONIBLES
     ============================================================ --%>
<section class="themes-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-book-open"></i> Contenu</span>
        <h2>Nos thèmes disponibles</h2>
        <p class="section-subtitle">Explorez nos différents domaines de connaissances</p>
    </div>
    
    <div class="themes-grid">
        <div class="theme-card"><div class="theme-card-icon" style="background:linear-gradient(135deg,#667eea,#5a6fd6);"><i class="fas fa-globe"></i></div><h4>Développement Web</h4><p>HTML, CSS, JavaScript, React, Node.js...</p><span class="theme-badge">7 cours</span></div>
        <div class="theme-card"><div class="theme-card-icon" style="background:linear-gradient(135deg,#f093fb,#e084ef);"><i class="fas fa-coffee"></i></div><h4>Langage Java</h4><p>Java SE, Spring Boot, Hibernate...</p><span class="theme-badge">7 cours</span></div>
        <div class="theme-card"><div class="theme-card-icon" style="background:linear-gradient(135deg,#4facfe,#3d9be0);"><i class="fab fa-python"></i></div><h4>Langage Python</h4><p>Django, Flask, Data Science, IA...</p><span class="theme-badge">6 cours</span></div>
        <div class="theme-card"><div class="theme-card-icon" style="background:linear-gradient(135deg,#43e97b,#36d06a);"><i class="fas fa-database"></i></div><h4>Bases de Données</h4><p>SQL, MySQL, PostgreSQL, MongoDB...</p><span class="theme-badge">6 cours</span></div>
        <div class="theme-card"><div class="theme-card-icon" style="background:linear-gradient(135deg,#f5576c,#e0485c);"><i class="fas fa-shield-alt"></i></div><h4>Cybersécurité</h4><p>Sécurité Web, Réseau, Pentesting...</p><span class="theme-badge">6 cours</span></div>
        <div class="theme-card"><div class="theme-card-icon" style="background:linear-gradient(135deg,#f39c12,#da8c10);"><i class="fas fa-mobile-alt"></i></div><h4>Dév. Mobile</h4><p>Flutter, React Native, Kotlin...</p><span class="theme-badge">4 cours</span></div>
    </div>
    
    <% if (utilisateurConnecte != null && "joueur".equals(role)) { %>
        <div class="themes-cta">
            <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-hero btn-hero-primary">
                <i class="fas fa-graduation-cap"></i><span>Explorer tous les cours</span>
            </a>
        </div>
    <% } %>
</section>

<%-- ============================================================
     SECTION TÉMOIGNAGES
     ============================================================ --%>
<section class="testimonials-section">
    <div class="section-header">
        <span class="section-badge"><i class="fas fa-comment-dots"></i> Avis</span>
        <h2>Ce que disent nos utilisateurs</h2>
        <p class="section-subtitle">La satisfaction de nos apprenants est notre priorité</p>
    </div>
    
    <div class="testimonials-grid">
        <div class="testimonial-card">
            <div class="testimonial-stars"><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i></div>
            <p class="testimonial-text">"Grâce aux cours et quiz d'AmijayQuiz, j'ai pu décrocher mon premier emploi en développement web. Les certificats sont reconnus !"</p>
            <div class="testimonial-author"><div class="author-avatar"><i class="fas fa-user-graduate"></i></div><div class="author-info"><strong>Jean M.</strong><span>Développeur Web Junior</span></div></div>
        </div>
        <div class="testimonial-card">
            <div class="testimonial-stars"><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i></div>
            <p class="testimonial-text">"Excellente plateforme ! Les cours sont bien structurés et les quiz permettent de valider ses connaissances."</p>
            <div class="testimonial-author"><div class="author-avatar"><i class="fas fa-user-tie"></i></div><div class="author-info"><strong>Marie K.</strong><span>Professionnelle IT</span></div></div>
        </div>
        <div class="testimonial-card">
            <div class="testimonial-stars"><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star-half-alt"></i></div>
            <p class="testimonial-text">"Le système de niveaux qui se débloquent progressivement est très motivant. On a envie d'aller jusqu'au bout !"</p>
            <div class="testimonial-author"><div class="author-avatar"><i class="fas fa-user"></i></div><div class="author-info"><strong>Patrick T.</strong><span>Étudiant passionné</span></div></div>
        </div>
    </div>
</section>

<%-- ============================================================
     SECTION CALL-TO-ACTION FINAL
     ============================================================ --%>
<section class="cta-section">
    <div class="cta-content">
        <h2>Prêt à commencer votre apprentissage ?</h2>
        <p>Rejoignez des centaines d'apprenants qui progressent chaque jour sur AmijayQuiz.</p>
        
        <% if (utilisateurConnecte == null) { %>
            <div class="cta-buttons">
                <a href="${pageContext.request.contextPath}/inscription" class="btn-hero btn-hero-primary">
                    <i class="fas fa-user-plus"></i><span>Créer mon compte gratuit</span>
                </a>
                <p class="cta-note"><i class="fas fa-lock"></i> Inscription rapide et sécurisée.</p>
            </div>
        <% } else if ("joueur".equals(role)) { %>
            <div class="cta-buttons">
                <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-hero btn-hero-primary">
                    <i class="fas fa-play"></i><span>Continuer les Quiz</span>
                </a>
                <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-hero btn-hero-outline" style="margin-top:10px;">
                    <i class="fas fa-graduation-cap"></i><span>Voir les Cours</span>
                </a>
            </div>
        <% } %>
    </div>
</section>

<%@ include file="views/footer.jsp" %>