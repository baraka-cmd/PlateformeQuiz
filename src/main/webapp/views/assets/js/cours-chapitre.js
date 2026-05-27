/**
 * ============================================================
 * FICHIER : views/assets/js/cours-chapitre.js
 * RÔLE    : Scripts de la page d'un chapitre de cours
 *           - Animation de la timeline
 *           - Animation des barres de progression
 *           - Effets de survol des leçons
 *           - Raccourcis clavier
 *           - Scroll vers la leçon active
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initProgressBarAnimation();
        initTimelineReveal();
        initLeconCardHover();
        initKeyboardNavigation();
        initScrollToActiveLecon();
        initNavButtonsHover();
    });

    /* ==========================================================
     SECTION 2 : ANIMATION BARRE DE PROGRESSION
     ========================================================== */
    function initProgressBarAnimation() {
        var progressFill = document.querySelector('.chapitre-progression-fill');
        if (!progressFill) return;

        var targetWidth = progressFill.style.width;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.3
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    progressFill.style.width = '0%';
                    progressFill.style.transition = 'width 1.4s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                    
                    setTimeout(function() {
                        progressFill.style.width = targetWidth;
                        
                        // Effet de brillance
                        setTimeout(function() {
                            progressFill.style.boxShadow = '0 0 12px rgba(102, 126, 234, 0.3)';
                            setTimeout(function() {
                                progressFill.style.boxShadow = 'none';
                            }, 600);
                        }, 1400);
                    }, 250);

                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        observer.observe(progressFill);
    }

    /* ==========================================================
     SECTION 3 : RÉVÉLATION DE LA TIMELINE
     ========================================================== */
    function initTimelineReveal() {
        var leconItems = document.querySelectorAll('.lecon-item');
        if (leconItems.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.1
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var item = entry.target;
                    var delay = index * 80;
                    
                    setTimeout(function() {
                        item.style.opacity = '1';
                        item.style.transform = 'translateX(0)';
                    }, delay);

                    // Animer la ligne de connexion
                    var line = item.querySelector('.lecon-timeline-line');
                    if (line) {
                        setTimeout(function() {
                            line.style.transition = 'background 0.5s ease';
                        }, delay + 300);
                    }

                    observer.unobserve(item);
                }
            });
        }, observerOptions);

        leconItems.forEach(function(item) {
            item.style.opacity = '0';
            item.style.transform = 'translateX(-15px)';
            item.style.transition = 'all 0.4s ease';
            observer.observe(item);
        });
    }

    /* ==========================================================
     SECTION 4 : EFFETS DE SURVOL DES CARTES LEÇONS
     ========================================================== */
    function initLeconCardHover() {
        var leconCards = document.querySelectorAll('.lecon-card:not(.verrouillee)');
        if (leconCards.length === 0) return;

        leconCards.forEach(function(card) {
            // Effet au survol
            card.addEventListener('mouseenter', function() {
                var numero = this.querySelector('.lecon-numero');
                var arrow = this.querySelector('.lecon-arrow');
                var badge = this.querySelector('.lecon-badge');

                if (numero) {
                    numero.style.transform = 'scale(1.1)';
                    numero.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
                if (arrow) {
                    arrow.style.transform = 'translateX(4px)';
                    arrow.style.color = '#667eea';
                    arrow.style.transition = 'all 0.3s ease';
                }
                if (badge && badge.classList.contains('active')) {
                    badge.style.transform = 'scale(1.05)';
                    badge.style.transition = 'all 0.3s ease';
                }
            });

            card.addEventListener('mouseleave', function() {
                var numero = this.querySelector('.lecon-numero');
                var arrow = this.querySelector('.lecon-arrow');
                var badge = this.querySelector('.lecon-badge');

                if (numero) numero.style.transform = 'scale(1)';
                if (arrow) {
                    arrow.style.transform = 'translateX(0)';
                    arrow.style.color = '#ccc';
                }
                if (badge) badge.style.transform = 'scale(1)';
            });

            // Effet mousedown
            card.addEventListener('mousedown', function() {
                this.style.transform = 'translateX(4px) scale(0.98)';
                this.style.transition = 'all 0.15s ease';
            });

            card.addEventListener('mouseup', function() {
                this.style.transform = 'translateX(4px) scale(1)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });
        });

        // Animation spéciale pour la leçon active (pulse léger)
        var activeCard = document.querySelector('.lecon-item.active .lecon-card');
        if (activeCard) {
            activeCard.style.animation = 'activePulse 3s ease-in-out infinite';
        }
    }

    /* ==========================================================
     SECTION 5 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardNavigation() {
        document.addEventListener('keydown', function(e) {
            // Ne pas activer dans les champs de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

            switch (e.key) {
                case 'Escape':
                    var backBtn = document.querySelector('.btn-back');
                    if (backBtn) window.location.href = backBtn.getAttribute('href');
                    break;

                case 'ArrowRight':
                    // Chapitre suivant
                    if (e.altKey) {
                        e.preventDefault();
                        var nextBtn = document.querySelector('.btn-nav-chapitre.suivant:not(.verrouille), .btn-nav-chapitre.examen');
                        if (nextBtn) window.location.href = nextBtn.getAttribute('href');
                    }
                    break;

                case 'ArrowLeft':
                    // Chapitre précédent
                    if (e.altKey) {
                        e.preventDefault();
                        var prevBtn = document.querySelector('.btn-nav-chapitre.precedent');
                        if (prevBtn) window.location.href = prevBtn.getAttribute('href');
                    }
                    break;

                case 'ArrowDown':
                    // Aller à la leçon active suivante
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        navigateToNextActiveLecon();
                    }
                    break;

                case 'ArrowUp':
                    // Aller à la leçon active précédente
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        navigateToPreviousActiveLecon();
                    }
                    break;

                case 'Enter':
                    // Ouvrir la première leçon active
                    var activeLink = document.querySelector('.lecon-item.active .lecon-card');
                    if (activeLink && activeLink.tagName === 'A') {
                        e.preventDefault();
                        window.location.href = activeLink.getAttribute('href');
                    }
                    break;
            }
        });
    }

    function navigateToNextActiveLecon() {
        var lecons = document.querySelectorAll('.lecon-item');
        var found = false;

        for (var i = 0; i < lecons.length; i++) {
            if (lecons[i].classList.contains('active')) {
                found = true;
                // Chercher la prochaine leçon accessible
                for (var j = i + 1; j < lecons.length; j++) {
                    var link = lecons[j].querySelector('.lecon-card');
                    if (link && link.tagName === 'A') {
                        window.location.href = link.getAttribute('href');
                        return;
                    }
                }
            }
        }
    }

    function navigateToPreviousActiveLecon() {
        var lecons = document.querySelectorAll('.lecon-item');
        var lastAccessible = null;

        for (var i = 0; i < lecons.length; i++) {
            var link = lecons[i].querySelector('.lecon-card');
            if (link && link.tagName === 'A') {
                if (lecons[i].classList.contains('active')) {
                    if (lastAccessible) {
                        window.location.href = lastAccessible.getAttribute('href');
                        return;
                    }
                }
                lastAccessible = link;
            }
        }
    }

    /* ==========================================================
     SECTION 6 : SCROLL VERS LA LEÇON ACTIVE
     ========================================================== */
    function initScrollToActiveLecon() {
        var activeLecon = document.querySelector('.lecon-item.active');
        if (!activeLecon) return;

        // Vérifier si on vient d'une autre page
        var referrer = document.referrer;
        var isFromNavigation = referrer && (
            referrer.includes('/joueur/cours/theme') || 
            referrer.includes('/joueur/cours/lecon')
        );

        if (isFromNavigation) {
            setTimeout(function() {
                var navbarHeight = document.querySelector('.navbar')?.offsetHeight || 70;
                var top = activeLecon.getBoundingClientRect().top + window.pageYOffset - navbarHeight - 30;
                
                window.scrollTo({ top: top, behavior: 'smooth' });

                // Mettre en évidence
                var card = activeLecon.querySelector('.lecon-card');
                if (card) {
                    card.style.boxShadow = '0 0 0 4px rgba(102, 126, 234, 0.25)';
                    card.style.transition = 'box-shadow 0.5s ease';
                    setTimeout(function() {
                        card.style.boxShadow = '';
                    }, 2000);
                }
            }, 400);
        }
    }

    /* ==========================================================
     SECTION 7 : ANIMATION DES BOUTONS DE NAVIGATION
     ========================================================== */
    function initNavButtonsHover() {
        var navButtons = document.querySelectorAll('.btn-nav-chapitre:not(.verrouille)');
        if (navButtons.length === 0) return;

        navButtons.forEach(function(btn) {
            btn.addEventListener('mouseenter', function() {
                var icon = this.querySelector('i');
                if (icon) {
                    if (this.classList.contains('precedent')) {
                        icon.style.transform = 'translateX(-4px)';
                    } else {
                        icon.style.transform = 'translateX(4px)';
                    }
                    icon.style.transition = 'all 0.3s ease';
                }
            });

            btn.addEventListener('mouseleave', function() {
                var icon = this.querySelector('i');
                if (icon) icon.style.transform = 'translateX(0)';
            });

            // Effet clic
            btn.addEventListener('mousedown', function() {
                this.style.transform = 'scale(0.96)';
                this.style.transition = 'all 0.15s ease';
            });

            btn.addEventListener('mouseup', function() {
                if (this.classList.contains('precedent')) {
                    this.style.transform = 'translateX(-4px)';
                } else {
                    this.style.transform = 'translateX(4px)';
                }
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });
        });
    }

    /* ==========================================================
     SECTION 8 : ANIMATION DES POINTS DE TIMELINE
     ========================================================== */
    function initTimelineDotsAnimation() {
        var dots = document.querySelectorAll('.lecon-timeline-dot');
        if (dots.length === 0) return;

        dots.forEach(function(dot) {
            dot.addEventListener('mouseenter', function() {
                if (this.closest('.lecon-item.verrouillee')) return;
                this.style.transform = 'scale(1.2)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            dot.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });
        });
    }

    setTimeout(initTimelineDotsAnimation, 600);

    /* ==========================================================
     SECTION 9 : COMPTEUR DE PROGRESSION (LOG)
     ========================================================== */
    function initProgressLog() {
        var totalLecons = document.querySelectorAll('.lecon-item').length;
        var leconsTerminees = document.querySelectorAll('.lecon-item.terminee').length;
        var pourcentage = totalLecons > 0 ? Math.round((leconsTerminees / totalLecons) * 100) : 0;

        if (totalLecons > 0) {
            console.log(
                '📖 Chapitre : ' + leconsTerminees + '/' + totalLecons + 
                ' leçons terminées (' + pourcentage + '%)'
            );
        }
    }

    setTimeout(initProgressLog, 800);

    /* ==========================================================
     SECTION 10 : STYLE ANIMATION PULSE (INJECTÉ)
     ========================================================== */
    var pulseStyle = document.createElement('style');
    pulseStyle.textContent = `
        @keyframes activePulse {
            0%, 100% { box-shadow: 0 0 0 0 rgba(102, 126, 234, 0); }
            50% { box-shadow: 0 0 0 6px rgba(102, 126, 234, 0.08); }
        }
    `;
    document.head.appendChild(pulseStyle);

    /* ==========================================================
     SECTION 11 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var activeDot = document.querySelector('.lecon-item.active .lecon-timeline-dot');
        var examenBtn = document.querySelector('.btn-nav-chapitre.examen');

        if (document.hidden) {
            if (activeDot) activeDot.style.animationPlayState = 'paused';
            if (examenBtn) examenBtn.style.animationPlayState = 'paused';
        } else {
            if (activeDot) activeDot.style.animationPlayState = 'running';
            if (examenBtn) examenBtn.style.animationPlayState = 'running';
        }
    });

})();