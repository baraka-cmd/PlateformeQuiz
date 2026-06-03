// ============================================================
// FICHIER : views/assets/js/footer.js
// RÔLE    : Scripts du pied de page AmijayQuiz
//           - Animation de la vague au scroll
//           - Animation des liens sociaux au survol
//           - Effet de fondu sur le copyright
//           - Compteur de visites (localStorage)
//           - Retour en haut de page
//           - Animation du coeur
//           - Gestion des liens externes
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        scrollTopThreshold: 400,
        socialAnimationDelay: 50
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initSocialLinksAnimation();
        initBackToTop();
        initCopyrightYear();
        initVisitCounter();
        initExternalLinksSecurity();
        initWaveAnimation();
        initFooterReveal();
        console.log('Footer - Initialise');
    });

    /* ==========================================================
     SECTION 3 : ANIMATION DES LIENS SOCIAUX
     ========================================================== */
    function initSocialLinksAnimation() {
        var socialLinks = document.querySelectorAll('.social-link');

        socialLinks.forEach(function(link) {
            // Effet d'apparition au survol
            link.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-4px) scale(1.1)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

                var icon = this.querySelector('i');
                if (icon) {
                    icon.style.transform = 'scale(1.2)';
                    icon.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            link.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';

                var icon = this.querySelector('i');
                if (icon) {
                    icon.style.transform = 'scale(1)';
                }
            });

            // Effet de focus pour l'accessibilite
            link.addEventListener('focus', function() {
                this.style.transform = 'translateY(-3px)';
                this.style.outline = '2px solid #667eea';
                this.style.outlineOffset = '3px';
            });

            link.addEventListener('blur', function() {
                this.style.transform = 'translateY(0)';
                this.style.outline = 'none';
            });
        });
    }

    /* ==========================================================
     SECTION 4 : BOUTON RETOUR EN HAUT
     ========================================================== */
    function initBackToTop() {
        // Creer le bouton retour en haut
        var backToTopBtn = document.createElement('button');
        backToTopBtn.className = 'back-to-top-btn';
        backToTopBtn.setAttribute('aria-label', 'Retour en haut de page');
        backToTopBtn.setAttribute('title', 'Retour en haut');
        backToTopBtn.innerHTML = '<i class="fas fa-chevron-up"></i>';
        backToTopBtn.style.cssText = 
            'position:fixed;bottom:30px;right:30px;width:48px;height:48px;' +
            'border-radius:50%;background:linear-gradient(135deg,#667eea,#764ba2);' +
            'color:white;border:none;cursor:pointer;font-size:18px;z-index:999;' +
            'box-shadow:0 4px 20px rgba(102,126,234,0.3);' +
            'opacity:0;visibility:hidden;transform:translateY(20px);' +
            'transition:all 0.3s cubic-bezier(0.4,0,0.2,1);' +
            'display:flex;align-items:center;justify-content:center;';

        document.body.appendChild(backToTopBtn);

        // Afficher/masquer le bouton au scroll
        window.addEventListener('scroll', function() {
            var scrollY = window.scrollY || window.pageYOffset;

            if (scrollY > config.scrollTopThreshold) {
                backToTopBtn.style.opacity = '1';
                backToTopBtn.style.visibility = 'visible';
                backToTopBtn.style.transform = 'translateY(0)';
            } else {
                backToTopBtn.style.opacity = '0';
                backToTopBtn.style.visibility = 'hidden';
                backToTopBtn.style.transform = 'translateY(20px)';
            }
        }, { passive: true });

        // Action de retour en haut
        backToTopBtn.addEventListener('click', function() {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });

            // Effet de clic
            this.style.transform = 'scale(0.9)';
            setTimeout(function() {
                backToTopBtn.style.transform = 'scale(1)';
            }, 150);
        });

        // Effet hover
        backToTopBtn.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px) scale(1.05)';
            this.style.boxShadow = '0 8px 30px rgba(102,126,234,0.5)';
        });

        backToTopBtn.addEventListener('mouseleave', function() {
            var scrollY = window.scrollY || window.pageYOffset;
            if (scrollY > config.scrollTopThreshold) {
                this.style.transform = 'translateY(0) scale(1)';
            }
            this.style.boxShadow = '0 4px 20px rgba(102,126,234,0.3)';
        });
    }

    /* ==========================================================
     SECTION 5 : ANNEE DU COPYRIGHT DYNAMIQUE
     ========================================================== */
    function initCopyrightYear() {
        var copyrightElements = document.querySelectorAll('.copyright');

        copyrightElements.forEach(function(element) {
            var currentYear = new Date().getFullYear();
            var yearText = element.textContent || element.innerText;

            if (!yearText.includes(currentYear.toString())) {
                var yearMatch = yearText.match(/\d{4}/);
                if (yearMatch && parseInt(yearMatch[0], 10) < currentYear) {
                    element.innerHTML = element.innerHTML.replace(yearMatch[0], currentYear.toString());
                }
            }
        });
    }

    /* ==========================================================
     SECTION 6 : COMPTEUR DE VISITES (LOCALSTORAGE)
     ========================================================== */
    function initVisitCounter() {
        var storageKey = 'amijayquiz_visit_count';
        var todayKey = 'amijayquiz_last_visit_date';
        var today = new Date().toDateString();

        try {
            var totalVisits = parseInt(localStorage.getItem(storageKey) || '0', 10);
            totalVisits = totalVisits + 1;
            localStorage.setItem(storageKey, totalVisits.toString());

            var lastVisitDate = localStorage.getItem(todayKey);
            if (lastVisitDate !== today) {
                localStorage.setItem(todayKey, today);
                console.log('Nouvelle visite aujourd\'hui - Total visites : ' + totalVisits);
            }
        } catch (e) {
            console.log('Compteur de visites non disponible');
        }
    }

    /* ==========================================================
     SECTION 7 : SECURITE DES LIENS EXTERNES
     ========================================================== */
    function initExternalLinksSecurity() {
        var externalLinks = document.querySelectorAll('.social-link, .footer a[target="_blank"]');

        externalLinks.forEach(function(link) {
            if (link.getAttribute('target') === '_blank') {
                if (!link.getAttribute('rel')) {
                    link.setAttribute('rel', 'noopener noreferrer');
                }
            }
        });
    }

    /* ==========================================================
     SECTION 8 : ANIMATION DE LA VAGUE AU SCROLL
     ========================================================== */
    function initWaveAnimation() {
        var wave = document.querySelector('.footer-wave svg');
        if (!wave) return;

        var footer = document.querySelector('.footer');
        if (!footer) return;

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                if (entries[0].isIntersecting) {
                    wave.style.transform = 'scaleY(0.8)';
                    wave.style.transformOrigin = 'bottom';
                    wave.style.transition = 'transform 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94)';

                    setTimeout(function() {
                        wave.style.transform = 'scaleY(1)';
                    }, 100);

                    observer.disconnect();
                }
            }, { threshold: 0.1 });

            observer.observe(footer);
        }
    }

    /* ==========================================================
     SECTION 9 : REVEAL DU FOOTER AU SCROLL
     ========================================================== */
    function initFooterReveal() {
        var footerColumns = document.querySelectorAll('.footer-column');
        if (footerColumns.length === 0) return;
        if (!('IntersectionObserver' in window)) return;

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.15,
            rootMargin: '0px 0px -30px 0px'
        });

        footerColumns.forEach(function(column, index) {
            column.style.opacity = '0';
            column.style.transform = 'translateY(25px)';
            column.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
            column.style.transitionDelay = (index * 0.1) + 's';
            observer.observe(column);
        });
    }

    /* ==========================================================
     SECTION 10 : ANIMATION DU COEUR
     ========================================================== */
    (function() {
        if (!document.querySelector('style[data-footer-animations]')) {
            var style = document.createElement('style');
            style.setAttribute('data-footer-animations', 'true');
            style.textContent =
                '@keyframes ftHeartBeat {' +
                '  0%, 100% { transform: scale(1); }' +
                '  15% { transform: scale(1.25); }' +
                '  30% { transform: scale(1); }' +
                '  45% { transform: scale(1.15); }' +
                '  60% { transform: scale(1); }' +
                '}' +
                '.heart-beat {' +
                '  display: inline-block;' +
                '  animation: ftHeartBeat 1.8s ease-in-out infinite;' +
                '  color: #e74c3c;' +
                '}';
            document.head.appendChild(style);
        }
    })();

    /* ==========================================================
     SECTION 11 : GESTION DES EMAILS
     ========================================================== */
    (function() {
        var emailLinks = document.querySelectorAll('a[href^="mailto:"]');

        emailLinks.forEach(function(link) {
            if (!link.getAttribute('title')) {
                link.setAttribute('title', 'Envoyer un email');
            }

            link.addEventListener('click', function() {
                this.style.opacity = '0.7';
                setTimeout(function() {
                    link.style.opacity = '1';
                }, 300);
            });
        });
    })();

    /* ==========================================================
     SECTION 12 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var footer = document.querySelector('.footer');
        if (!footer) return;

        if (document.hidden) {
            footer.classList.add('footer-hidden');
        } else {
            footer.classList.remove('footer-hidden');
        }
    });

    var perfStyle = document.createElement('style');
    perfStyle.textContent =
        '.footer-hidden .heart-beat { animation: none !important; }' +
        '.footer-hidden .social-link { transition: none !important; }';
    document.head.appendChild(perfStyle);

})();