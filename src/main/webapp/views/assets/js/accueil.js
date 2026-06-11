// ============================================================
// FICHIER : views/assets/js/accueil.js
// RÔLE    : Script de la page d'accueil LearnPath
//           - Animation des compteurs statistiques
//           - Animation au scroll (reveal)
//           - FAQ accordeon
//           - Smooth scroll pour le scroll indicator
//           - Effet parallaxe sur le hero
//           - Animation des cartes au survol
//           - Gestion du header transparent
//           - Particules dynamiques dans le hero
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        compteurDuree: 2000,
        revealThreshold: 0.15,
        parallaxIntensity: 0.3,
        navbarScrollThreshold: 50
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initCompteursStatistiques();
        initScrollReveal();
        initFaqAccordeon();
        initSmoothScroll();
        initHeroParallax();
        initHeaderScroll();
        initHeroParticles();
        initCardsHover();
        initScrollIndicator();
        console.log('Page d\'accueil LearnPath initialisee');
    });

    /* ==========================================================
     SECTION 3 : COMPTEURS STATISTIQUES ANIMES
     ========================================================== */
    function initCompteursStatistiques() {
        var compteurs = document.querySelectorAll('.stat-number');

        if (!('IntersectionObserver' in window)) {
            compteurs.forEach(function(c) { c.style.opacity = '1'; });
            return;
        }

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    animerCompteur(entry.target);
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.5 });

        compteurs.forEach(function(compteur) {
            compteur.style.opacity = '0';
            observer.observe(compteur);
        });
    }

    function animerCompteur(element) {
        var texteComplet = element.textContent.trim();
        var match = texteComplet.match(/(\d+)/);
        if (!match) { element.style.opacity = '1'; return; }

        var valeurFinale = parseInt(match[1]);
        var suffixe = texteComplet.replace(match[1], '');
        var debut = 0;
        var duree = config.compteurDuree;
        var debutTimestamp = null;

        element.style.opacity = '1';

        function step(timestamp) {
            if (!debutTimestamp) debutTimestamp = timestamp;
            var progression = Math.min((timestamp - debutTimestamp) / duree, 1);
            var eased = 1 - Math.pow(1 - progression, 3);
            var valeurActuelle = Math.floor(debut + (valeurFinale - debut) * eased);

            element.innerHTML = valeurActuelle + suffixe;

            if (progression < 1) {
                requestAnimationFrame(step);
            } else {
                element.innerHTML = texteComplet;
            }
        }

        requestAnimationFrame(step);
    }

    /* ==========================================================
     SECTION 4 : ANIMATIONS AU SCROLL (REVEAL)
     ========================================================== */
    function initScrollReveal() {
        if (!('IntersectionObserver' in window)) return;

        var elementsToReveal = document.querySelectorAll(
            '.feature-card, .step-card, .dual-card, .theme-card, ' +
            '.testimonial-card, .faq-item, .stat-card'
        );

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, {
            threshold: config.revealThreshold,
            rootMargin: '0px 0px -40px 0px'
        });

        elementsToReveal.forEach(function(element, index) {
            element.style.opacity = '0';
            element.style.transform = 'translateY(30px)';
            element.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
            element.style.transitionDelay = (index % 4) * 0.08 + 's';
            observer.observe(element);
        });
    }

    /* ==========================================================
     SECTION 5 : FAQ ACCORDEON
     ========================================================== */
    function initFaqAccordeon() {
        var faqQuestions = document.querySelectorAll('.faq-question');

        faqQuestions.forEach(function(question) {
            question.addEventListener('click', function() {
                var faqItem = this.closest('.faq-item');
                if (!faqItem) return;

                var isActive = faqItem.classList.contains('active');

                // Fermer tous les autres
                document.querySelectorAll('.faq-item.active').forEach(function(item) {
                    if (item !== faqItem) {
                        item.classList.remove('active');
                    }
                });

                // Toggle celui-ci
                if (isActive) {
                    faqItem.classList.remove('active');
                } else {
                    faqItem.classList.add('active');
                }
            });

            // Accessibilite clavier
            question.setAttribute('tabindex', '0');
            question.setAttribute('role', 'button');
            question.setAttribute('aria-expanded', 'false');

            question.addEventListener('keydown', function(e) {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    this.click();
                }
            });

            // Mettre a jour aria-expanded
            question.addEventListener('click', function() {
                var faqItem = this.closest('.faq-item');
                if (faqItem) {
                    var isActive = faqItem.classList.contains('active');
                    this.setAttribute('aria-expanded', isActive ? 'true' : 'false');
                }
            });
        });
    }

    /* ==========================================================
     SECTION 6 : SMOOTH SCROLL
     ========================================================== */
    function initSmoothScroll() {
        var smoothLinks = document.querySelectorAll('a[href^="#"]:not([href="#"])');

        smoothLinks.forEach(function(link) {
            link.addEventListener('click', function(e) {
                var targetId = this.getAttribute('href').substring(1);
                var target = document.getElementById(targetId);

                if (target) {
                    e.preventDefault();
                    var headerHeight = document.querySelector('.navbar') ?
                        document.querySelector('.navbar').offsetHeight : 65;
                    var targetPosition = target.getBoundingClientRect().top +
                        window.pageYOffset - headerHeight - 20;

                    window.scrollTo({
                        top: targetPosition,
                        behavior: 'smooth'
                    });

                    if (history.pushState) {
                        history.pushState(null, null, '#' + targetId);
                    }
                }
            });
        });
    }

    /* ==========================================================
     SECTION 7 : SCROLL INDICATOR
     ========================================================== */
    function initScrollIndicator() {
        var scrollIndicator = document.querySelector('.hero-scroll-indicator');
        if (!scrollIndicator) return;

        scrollIndicator.addEventListener('click', function() {
            var statsSection = document.querySelector('.stats-section');
            if (statsSection) {
                var headerHeight = document.querySelector('.navbar') ?
                    document.querySelector('.navbar').offsetHeight : 65;
                var targetPosition = statsSection.getBoundingClientRect().top +
                    window.pageYOffset - headerHeight;

                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });
            }
        });

        // Masquer le scroll indicator quand on a scrolle
        window.addEventListener('scroll', function() {
            if (window.scrollY > 200) {
                scrollIndicator.style.opacity = '0';
                scrollIndicator.style.transition = 'opacity 0.3s ease';
            } else {
                scrollIndicator.style.opacity = '1';
            }
        }, { passive: true });
    }

    /* ==========================================================
     SECTION 8 : EFFET PARALLAXE SUR LE HERO
     ========================================================== */
    function initHeroParallax() {
        var heroBg = document.querySelector('.hero-bg-image');
        if (!heroBg) return;

        window.addEventListener('scroll', function() {
            var scrollY = window.scrollY || window.pageYOffset;
            var heroSection = document.querySelector('.hero-section');
            if (!heroSection) return;

            var heroHeight = heroSection.offsetHeight;

            if (scrollY < heroHeight) {
                var translateY = scrollY * config.parallaxIntensity;
                heroBg.style.transform = 'translateY(' + translateY + 'px)';
            }
        }, { passive: true });
    }

    /* ==========================================================
     SECTION 9 : HEADER TRANSPARENT AU DEBUT
     ========================================================== */
    function initHeaderScroll() {
        var navbar = document.querySelector('.navbar');
        if (!navbar) return;

        var heroSection = document.querySelector('.hero-section');
        if (!heroSection) return;

        function updateHeaderStyle() {
            var scrollY = window.scrollY || window.pageYOffset;

            if (scrollY > config.navbarScrollThreshold) {
                navbar.style.background = '#ffffff';
                navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.08)';
                navbar.classList.add('navbar-scrolled');
            } else {
                navbar.style.background = 'rgba(255, 255, 255, 0.95)';
                navbar.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.04)';
                navbar.classList.remove('navbar-scrolled');
            }
        }

        updateHeaderStyle();
        window.addEventListener('scroll', updateHeaderStyle, { passive: true });
    }

    /* ==========================================================
     SECTION 10 : PARTICULES DYNAMIQUES DANS LE HERO
     ========================================================== */
    function initHeroParticles() {
        var heroSection = document.querySelector('.hero-section');
        if (!heroSection) return;

        var canvas = document.createElement('canvas');
        canvas.className = 'hero-particles-canvas';
        canvas.style.cssText = 'position:absolute;top:0;left:0;width:100%;height:100%;z-index:1;pointer-events:none;';
        heroSection.appendChild(canvas);

        var ctx = canvas.getContext('2d');
        var particles = [];
        var maxParticles = 40;
        var mouseX = -100;
        var mouseY = -100;

        function resizeCanvas() {
            canvas.width = heroSection.offsetWidth;
            canvas.height = heroSection.offsetHeight;
        }
        resizeCanvas();
        window.addEventListener('resize', resizeCanvas);

        // Suivre la souris
        heroSection.addEventListener('mousemove', function(e) {
            var rect = heroSection.getBoundingClientRect();
            mouseX = e.clientX - rect.left;
            mouseY = e.clientY - rect.top;
        });

        heroSection.addEventListener('mouseleave', function() {
            mouseX = -100;
            mouseY = -100;
        });

        // Creer les particules
        for (var i = 0; i < maxParticles; i++) {
            particles.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                radius: 1 + Math.random() * 2.5,
                speedX: -0.2 + Math.random() * 0.4,
                speedY: -0.2 + Math.random() * 0.4,
                opacity: 0.2 + Math.random() * 0.5
            });
        }

        function animate() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            particles.forEach(function(p) {
                p.x += p.speedX;
                p.y += p.speedY;

                if (p.x < 0 || p.x > canvas.width) p.speedX *= -1;
                if (p.y < 0 || p.y > canvas.height) p.speedY *= -1;

                p.x = Math.max(0, Math.min(canvas.width, p.x));
                p.y = Math.max(0, Math.min(canvas.height, p.y));

                // Attraction legere vers la souris
                var dx = mouseX - p.x;
                var dy = mouseY - p.y;
                var distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < 150 && distance > 0) {
                    p.x += dx * 0.005;
                    p.y += dy * 0.005;
                }

                ctx.beginPath();
                ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
                ctx.fillStyle = 'rgba(255, 255, 255, ' + p.opacity + ')';
                ctx.fill();
            });

            // Lignes entre particules proches
            for (var i = 0; i < particles.length; i++) {
                for (var j = i + 1; j < particles.length; j++) {
                    var dx = particles[i].x - particles[j].x;
                    var dy = particles[i].y - particles[j].y;
                    var distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < 100) {
                        ctx.beginPath();
                        ctx.moveTo(particles[i].x, particles[i].y);
                        ctx.lineTo(particles[j].x, particles[j].y);
                        ctx.strokeStyle = 'rgba(255, 255, 255, ' + (0.06 * (1 - distance / 100)) + ')';
                        ctx.lineWidth = 0.5;
                        ctx.stroke();
                    }
                }
            }

            requestAnimationFrame(animate);
        }

        animate();
    }

    /* ==========================================================
     SECTION 11 : EFFET HOVER SUR LES CARTES
     ========================================================== */
    function initCardsHover() {
        var cards = document.querySelectorAll(
            '.feature-card, .step-card, .theme-card, .testimonial-card, .stat-card'
        );

        cards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                this.style.transition = 'all 0.35s cubic-bezier(0.4, 0, 0.2, 1)';
            });

            card.addEventListener('mousemove', function(e) {
                var rect = this.getBoundingClientRect();
                var x = e.clientX - rect.left;
                var y = e.clientY - rect.top;
                var centerX = rect.width / 2;
                var centerY = rect.height / 2;

                var rotateX = (y - centerY) / centerY * -5;
                var rotateY = (x - centerX) / centerX * 5;

                this.style.transform = 'perspective(800px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg) translateY(-5px)';
            });

            card.addEventListener('mouseleave', function() {
                this.style.transform = 'perspective(800px) rotateX(0) rotateY(0) translateY(0)';
            });
        });
    }

    /* ==========================================================
     SECTION 12 : ANIMATION DU BOUTON PREMIUM
     ========================================================== */
    (function initPremiumButtonGlow() {
        var premiumBtn = document.querySelector('.premium-offer-btn');
        if (!premiumBtn) return;

        setInterval(function() {
            premiumBtn.style.boxShadow = '0 8px 35px rgba(243, 156, 18, 0.5)';
            setTimeout(function() {
                premiumBtn.style.boxShadow = '0 8px 30px rgba(243, 156, 18, 0.3)';
            }, 1200);
        }, 2500);
    })();

    /* ==========================================================
     SECTION 13 : ANIMATION DES ETAPES (HOW IT WORKS)
     ========================================================== */
    (function initStepsAnimation() {
        var steps = document.querySelectorAll('.step-card');
        if (steps.length === 0) return;

        if (!('IntersectionObserver' in window)) return;

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    var number = entry.target.querySelector('.step-number');
                    if (number) {
                        number.style.transform = 'scale(0)';
                        number.style.transition = 'transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                        setTimeout(function() {
                            number.style.transform = 'scale(1)';
                        }, 200);
                    }
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.3 });

        steps.forEach(function(step) {
            observer.observe(step);
        });
    })();

    /* ==========================================================
     SECTION 14 : TYPING EFFECT SUR LE HERO (OPTIONNEL)
     ========================================================== */
    (function initHeroTypingEffect() {
        var subtitle = document.querySelector('.hero-subtitle');
        if (!subtitle) return;

        var textes = [
            'Apprenez, testez vos connaissances et obtenez des <strong>certificats verifiables</strong>.<br>Progressez niveau par niveau et devenez expert dans votre domaine.',
            'Des <strong>cours complets</strong> avec exemples de code et ressources telechargeables.<br>Votre progression est sauvegardee automatiquement.',
            'Rejoignez <strong>500+ apprenants</strong> qui progressent chaque jour.<br>Commencez gratuitement des maintenant !'
        ];

        var index = 0;
        setInterval(function() {
            index = (index + 1) % textes.length;
            subtitle.style.opacity = '0';
            subtitle.style.transition = 'opacity 0.4s ease';

            setTimeout(function() {
                subtitle.innerHTML = textes[index];
                subtitle.style.opacity = '1';
            }, 400);
        }, 6000);
    })();

    /* ==========================================================
     SECTION 15 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            document.body.classList.add('page-hidden');
        } else {
            document.body.classList.remove('page-hidden');
        }
    });

    var perfStyle = document.createElement('style');
    perfStyle.textContent = '.page-hidden .hero-particles-canvas { display: none; }';
    document.head.appendChild(perfStyle);

})();