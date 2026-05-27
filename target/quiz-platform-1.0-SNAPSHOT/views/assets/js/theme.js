/**
 * ============================================================
 * FICHIER : views/assets/js/theme.js
 * RÔLE    : Scripts de la page thème AmijayQuiz
 *           - Animation de la barre de progression au scroll
 *           - Animation des cartes niveaux (révélation cascade)
 *           - Effets de survol avancés sur les cartes
 *           - Animation de l'icône du thème
 *           - Gestion du message de motivation
 *           - Animation du breadcrumb
 *           - Gestion des niveaux bloqués (info-bulle)
 *           - Raccourcis clavier
 *           - Responsive : adaptation mobile
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initProgressBarAnimation();
        initLevelCardsReveal();
        initLevelCardHover();
        initThemeIconInteraction();
        initMotivationBanner();
        initBreadcrumbAnimation();
        initBlockedLevelTooltip();
        initKeyboardNavigation();
        initSmoothScrollToLevels();
    });

    /* ==========================================================
     SECTION 2 : ANIMATION BARRE DE PROGRESSION
     ========================================================== */
    function initProgressBarAnimation() {
        const progressBar = document.querySelector('.progress-bar-inner');
        if (!progressBar) return;

        const targetWidth = progressBar.style.width;
        
        // Réinitialiser pour l'animation
        progressBar.style.width = '0%';
        progressBar.style.transition = 'width 1.4s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
        
        // Observer pour déclencher l'animation quand la barre est visible
        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.5
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    setTimeout(function() {
                        progressBar.style.width = targetWidth;
                        
                        // Effet de brillance à la fin
                        setTimeout(function() {
                            progressBar.style.boxShadow = '0 0 15px rgba(102, 126, 234, 0.3)';
                            setTimeout(function() {
                                progressBar.style.boxShadow = 'none';
                                progressBar.style.transition = 'width 0.3s ease, box-shadow 0.5s ease';
                            }, 600);
                        }, 1400);
                    }, 300);
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        observer.observe(progressBar.parentElement);
    }

    /* ==========================================================
     SECTION 3 : RÉVÉLATION DES CARTES NIVEAUX
     ========================================================== */
    function initLevelCardsReveal() {
        const niveauCards = document.querySelectorAll('.niveau-card');
        if (niveauCards.length === 0) return;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.15
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    const card = entry.target;
                    const delay = index * 120;
                    
                    setTimeout(function() {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0) scale(1)';
                    }, delay);
                    
                    observer.unobserve(card);
                }
            });
        }, observerOptions);

        niveauCards.forEach(function(card) {
            // Initialiser l'état invisible
            const computedStyle = window.getComputedStyle(card);
            if (!computedStyle.animationName || computedStyle.animationName === 'none') {
                card.style.opacity = '0';
                card.style.transform = 'translateY(35px) scale(0.95)';
                card.style.transition = 'all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            }
            observer.observe(card);
        });
    }

    /* ==========================================================
     SECTION 4 : EFFETS DE SURVOL DES CARTES NIVEAUX
     ========================================================== */
    function initLevelCardHover() {
        const niveauCards = document.querySelectorAll('.niveau-card.debloque, .niveau-card.termine');
        if (niveauCards.length === 0) return;

        // Effet 3D au survol (desktop uniquement)
        if (!window.isTouchDevice || !window.isTouchDevice()) {
            niveauCards.forEach(function(card) {
                card.addEventListener('mousemove', function(e) {
                    const rect = this.getBoundingClientRect();
                    const x = e.clientX - rect.left;
                    const y = e.clientY - rect.top;
                    
                    const centerX = rect.width / 2;
                    const centerY = rect.height / 2;
                    
                    const rotateX = (y - centerY) / centerY * -6;
                    const rotateY = (x - centerX) / centerX * 6;
                    
                    this.style.transform = 'translateY(-5px) perspective(800px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
                    this.style.transition = 'transform 0.1s ease';
                    this.style.boxShadow = '0 15px 35px rgba(0, 0, 0, 0.1)';
                });

                card.addEventListener('mouseleave', function() {
                    this.style.transform = 'translateY(0) perspective(800px) rotateX(0) rotateY(0)';
                    this.style.transition = 'transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                    this.style.boxShadow = '';
                });
            });
        }

        // Animation du bouton au survol
        niveauCards.forEach(function(card) {
            const btn = card.querySelector('.btn-niveau');
            if (!btn) return;

            card.addEventListener('mouseenter', function() {
                if (btn.classList.contains('btn-continuer')) {
                    btn.style.transform = 'translateY(-2px)';
                    btn.style.boxShadow = '0 8px 20px rgba(102, 126, 234, 0.4)';
                }
            });

            card.addEventListener('mouseleave', function() {
                if (btn.classList.contains('btn-continuer')) {
                    btn.style.transform = 'translateY(0)';
                    btn.style.boxShadow = '';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 5 : INTERACTION AVEC L'ICÔNE DU THÈME
     ========================================================== */
    function initThemeIconInteraction() {
        const themeIcon = document.querySelector('.theme-icon-large');
        if (!themeIcon) return;

        // Effet de rotation au clic
        themeIcon.addEventListener('click', function() {
            this.style.transition = 'transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            this.style.transform = 'scale(0.9) rotate(10deg)';
            
            setTimeout(() => {
                this.style.transform = 'scale(1.1) rotate(-5deg)';
            }, 200);
            
            setTimeout(() => {
                this.style.transform = 'scale(1) rotate(0deg)';
            }, 500);
        });

        // Curseur pointer
        themeIcon.style.cursor = 'pointer';
        themeIcon.setAttribute('title', 'Cliquez pour animer');
        themeIcon.setAttribute('data-tooltip', '🎯 ' + (document.querySelector('.theme-title')?.textContent || 'Thème'));
    }

    /* ==========================================================
     SECTION 6 : ANIMATION DU MESSAGE DE MOTIVATION
     ========================================================== */
    function initMotivationBanner() {
        const banner = document.querySelector('.motivation-banner');
        if (!banner) return;

        // Observer pour déclencher l'animation d'entrée
        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.5
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateX(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        // Initialiser l'état
        banner.style.opacity = '0';
        banner.style.transform = 'translateX(-30px)';
        banner.style.transition = 'all 0.7s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
        
        observer.observe(banner);

        // Effet de survol
        banner.addEventListener('mouseenter', function() {
            const icon = this.querySelector('i');
            if (icon) {
                icon.style.transform = 'scale(1.3) rotate(10deg)';
                icon.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            }
        });

        banner.addEventListener('mouseleave', function() {
            const icon = this.querySelector('i');
            if (icon) {
                icon.style.transform = 'scale(1) rotate(0deg)';
            }
        });
    }

    /* ==========================================================
     SECTION 7 : ANIMATION DU BREADCRUMB
     ========================================================== */
    function initBreadcrumbAnimation() {
        const breadcrumbLinks = document.querySelectorAll('.breadcrumb-link');
        if (breadcrumbLinks.length === 0) return;

        breadcrumbLinks.forEach(function(link, index) {
            // Apparition séquentielle
            link.style.opacity = '0';
            link.style.transform = 'translateX(-15px)';
            link.style.transition = 'all 0.4s ease';
            
            setTimeout(function() {
                link.style.opacity = '1';
                link.style.transform = 'translateX(0)';
            }, 100 + (index * 80));

            // Effet au survol
            link.addEventListener('mouseenter', function() {
                const icon = this.querySelector('i');
                if (icon) {
                    icon.style.transform = 'translateX(-3px)';
                    icon.style.transition = 'all 0.3s ease';
                }
            });

            link.addEventListener('mouseleave', function() {
                const icon = this.querySelector('i');
                if (icon) {
                    icon.style.transform = 'translateX(0)';
                }
            });
        });

        // Animation du séparateur current
        const current = document.querySelector('.breadcrumb-current');
        if (current) {
            current.style.opacity = '0';
            current.style.transform = 'translateX(-15px)';
            current.style.transition = 'all 0.4s ease 0.3s';
            
            setTimeout(function() {
                current.style.opacity = '1';
                current.style.transform = 'translateX(0)';
            }, 400);
        }
    }

    /* ==========================================================
     SECTION 8 : TOOLTIP POUR LES NIVEAUX BLOQUÉS
     ========================================================== */
    function initBlockedLevelTooltip() {
        const blockedCards = document.querySelectorAll('.niveau-card.bloque');
        if (blockedCards.length === 0) return;

        blockedCards.forEach(function(card) {
            // Ajouter un tooltip informatif
            const niveauName = card.querySelector('.niveau-nom')?.textContent || 'ce niveau';
            card.setAttribute('data-tooltip', '🔒 ' + niveauName + ' : terminez le niveau précédent pour débloquer');
            
            // Effet de vibration légère au clic (pour indiquer que c'est bloqué)
            card.addEventListener('click', function(e) {
                e.preventDefault();
                shakeElement(card);
                
                // Afficher un toast discret
                if (window.showToast) {
                    window.showToast('Ce niveau est bloqué. Terminez le niveau précédent d\'abord.', 'info', 3000);
                }
            });
        });
    }

    /**
     * Animation de vibration
     */
    function shakeElement(element) {
        if (!element) return;
        
        element.style.animation = 'none';
        element.offsetHeight;
        element.style.animation = 'shake 0.5s ease';
        
        element.addEventListener('animationend', function handler() {
            element.removeEventListener('animationend', handler);
            element.style.animation = '';
        });
    }

    /* ==========================================================
     SECTION 9 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardNavigation() {
        document.addEventListener('keydown', function(e) {
            // Ne pas activer si on est dans un champ de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') {
                return;
            }

            switch (e.key) {
                case 'Escape':
                    // Retour au dashboard
                    const backBtn = document.querySelector('.btn-retour-theme');
                    if (backBtn) {
                        window.location.href = backBtn.getAttribute('href');
                    }
                    break;
                    
                case '1':
                    // Aller au niveau élémentaire (première carte)
                    navigateToLevel(0);
                    break;
                    
                case '2':
                    // Aller au niveau moyen (deuxième carte)
                    navigateToLevel(1);
                    break;
                    
                case '3':
                    // Aller au niveau avancé (troisième carte)
                    navigateToLevel(2);
                    break;
                    
                case 'ArrowLeft':
                    // Navigation dans le breadcrumb
                    const breadcrumbLink = document.querySelector('.breadcrumb-link');
                    if (breadcrumbLink && e.altKey) {
                        e.preventDefault();
                        window.location.href = breadcrumbLink.getAttribute('href');
                    }
                    break;
            }
        });
    }

    /**
     * Naviguer vers un niveau spécifique
     */
    function navigateToLevel(index) {
        const niveauCards = document.querySelectorAll('.niveau-card.debloque a, .niveau-card.termine a');
        if (niveauCards.length > index) {
            const link = niveauCards[index];
            const href = link.getAttribute('href');
            if (href && href !== '#') {
                window.location.href = href;
            }
        }
    }

    /* ==========================================================
     SECTION 10 : SCROLL DOUX VERS LES NIVEAUX
     ========================================================== */
    function initSmoothScrollToLevels() {
        // Détecter si on vient du dashboard (via un paramètre ou referrer)
        const referrer = document.referrer;
        const niveauxSection = document.querySelector('.niveaux-section');
        
        if (niveauxSection && referrer && referrer.includes('/joueur/dashboard')) {
            // Petit délai pour laisser la page se charger
            setTimeout(function() {
                niveauxSection.scrollIntoView({ 
                    behavior: 'smooth', 
                    block: 'start' 
                });
            }, 400);
        }
    }

    /* ==========================================================
     SECTION 11 : EFFET DE COMPTAGE DES STATISTIQUES
     ========================================================== */
    function initStatsCountAnimation() {
        const statNumbers = document.querySelectorAll('.theme-stat-item strong');
        if (statNumbers.length === 0) return;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.5
        };

        let animated = false;

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting && !animated) {
                    animated = true;
                    
                    statNumbers.forEach(function(stat, index) {
                        const text = stat.textContent.trim();
                        const target = parseInt(text, 10);
                        
                        if (isNaN(target)) return;
                        
                        const duration = 1000 + (index * 200);
                        const startTime = performance.now();
                        
                        function update(currentTime) {
                            const elapsed = currentTime - startTime;
                            const progress = Math.min(elapsed / duration, 1);
                            const eased = 1 - Math.pow(1 - progress, 3);
                            const current = Math.floor(eased * target);
                            
                            stat.textContent = current;
                            
                            if (progress < 1) {
                                requestAnimationFrame(update);
                            } else {
                                stat.textContent = target;
                            }
                        }
                        
                        setTimeout(function() {
                            requestAnimationFrame(update);
                        }, index * 150);
                    });
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        const themeHeader = document.querySelector('.theme-header');
        if (themeHeader) {
            observer.observe(themeHeader);
        }
    }

    // Initialiser le comptage après un délai
    setTimeout(initStatsCountAnimation, 600);

    /* ==========================================================
     SECTION 12 : ANIMATION DES BARRES DE PROGRESSION DES NIVEAUX
     ========================================================== */
    function initLevelProgressBars() {
        const progressFills = document.querySelectorAll('.niveau-card .progress-fill');
        if (progressFills.length === 0) return;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.3
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    const targetWidth = entry.target.style.width;
                    
                    // Réinitialiser pour animer
                    entry.target.style.width = '0%';
                    entry.target.style.transition = 'width 1s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                    
                    setTimeout(function() {
                        entry.target.style.width = targetWidth;
                    }, 200);
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        progressFills.forEach(function(fill) {
            observer.observe(fill);
        });
    }

    setTimeout(initLevelProgressBars, 800);

    /* ==========================================================
     SECTION 13 : PERFORMANCE
     ========================================================== */
    // Mettre en pause les animations quand la page est cachée
    document.addEventListener('visibilitychange', function() {
        const themeIcon = document.querySelector('.theme-icon-large');
        const motivationBanner = document.querySelector('.motivation-banner');
        
        if (document.hidden) {
            if (themeIcon) themeIcon.style.animationPlayState = 'paused';
            if (motivationBanner) motivationBanner.style.animationPlayState = 'paused';
        } else {
            if (themeIcon) themeIcon.style.animationPlayState = 'running';
            if (motivationBanner) motivationBanner.style.animationPlayState = 'running';
        }
    });

    /* ==========================================================
     SECTION 14 : STYLE SHAKE (INJECTÉ)
     ========================================================== */
    const shakeStyle = document.createElement('style');
    shakeStyle.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
            20%, 40%, 60%, 80% { transform: translateX(5px); }
        }
    `;
    document.head.appendChild(shakeStyle);

})();