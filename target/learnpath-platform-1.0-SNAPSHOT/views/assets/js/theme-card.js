/**
 * ============================================================
 * FICHIER : views/assets/js/components/theme-card.js
 * RÔLE    : Scripts du composant theme-card
 *           - Animation des barres de progression au scroll
 *           - Effets de survol avancés (3D tilt)
 *           - Révélation des cartes au scroll
 *           - Animation du badge trophée
 *           - Animation des mini-badges
 *           - Effet ripple sur le bouton "Voir le parcours"
 *           - Performance (pause animations)
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initProgressBarsAnimation();
        initCardsReveal();
        initTiltEffect();
        initTropheeAnimation();
        initMiniBadgesAnimation();
        initVoirParcoursRipple();
    });

    /* ==========================================================
     SECTION 2 : ANIMATION DES BARRES DE PROGRESSION
     ========================================================== */
    function initProgressBarsAnimation() {
        var progressFills = document.querySelectorAll('.theme-card .progress-fill');
        if (progressFills.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.2
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    var fill = entry.target;
                    var targetWidth = fill.style.width;
                    
                    // Réinitialiser pour l'animation
                    fill.style.width = '0%';
                    fill.style.transition = 'width 1.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                    
                    setTimeout(function() {
                        fill.style.width = targetWidth;
                        
                        // Effet de brillance à la fin
                        setTimeout(function() {
                            fill.style.boxShadow = '0 0 12px rgba(102, 126, 234, 0.35)';
                            setTimeout(function() {
                                fill.style.boxShadow = 'none';
                                fill.style.transition = 'width 0.3s ease, box-shadow 0.5s ease';
                            }, 700);
                        }, 1300);
                    }, 200);
                    
                    observer.unobserve(fill);
                }
            });
        }, observerOptions);

        progressFills.forEach(function(fill) {
            observer.observe(fill);
        });
    }

    /* ==========================================================
     SECTION 3 : RÉVÉLATION DES CARTES AU SCROLL
     ========================================================== */
    function initCardsReveal() {
        var cards = document.querySelectorAll('.theme-card');
        if (cards.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.1
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var card = entry.target;
                    var delay = index * 120;
                    
                    setTimeout(function() {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0) scale(1)';
                    }, delay);
                    
                    observer.unobserve(card);
                }
            });
        }, observerOptions);

        cards.forEach(function(card) {
            // Vérifier si une animation CSS n'est pas déjà appliquée
            var computedStyle = window.getComputedStyle(card);
            if (!computedStyle.animationName || computedStyle.animationName === 'none') {
                card.style.opacity = '0';
                card.style.transform = 'translateY(25px) scale(0.96)';
                card.style.transition = 'all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            }
            observer.observe(card);
        });
    }

    /* ==========================================================
     SECTION 4 : EFFET 3D TILT AU SURVOL (DESKTOP UNIQUEMENT)
     ========================================================== */
    function initTiltEffect() {
        // Ne pas activer sur les appareils tactiles
        if (window.isTouchDevice && window.isTouchDevice()) return;

        var cards = document.querySelectorAll('.theme-card');
        if (cards.length === 0) return;

        cards.forEach(function(card) {
            card.addEventListener('mousemove', function(e) {
                var rect = this.getBoundingClientRect();
                var x = e.clientX - rect.left;
                var y = e.clientY - rect.top;
                
                var centerX = rect.width / 2;
                var centerY = rect.height / 2;
                
                var rotateX = (y - centerY) / centerY * -4;
                var rotateY = (x - centerX) / centerX * 4;
                
                this.style.transform = 'translateY(-5px) perspective(700px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
                this.style.transition = 'transform 0.1s ease';
                this.style.boxShadow = '0 20px 50px rgba(0, 0, 0, 0.12)';
            });

            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) perspective(700px) rotateX(0) rotateY(0)';
                this.style.transition = 'transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                this.style.boxShadow = '';
            });
        });
    }

    /* ==========================================================
     SECTION 5 : ANIMATION DU BADGE TROPHÉE
     ========================================================== */
    function initTropheeAnimation() {
        var tropheeBadges = document.querySelectorAll('.theme-badge-termine');
        if (tropheeBadges.length === 0) return;

        tropheeBadges.forEach(function(badge) {
            // Effet au survol
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.2) rotate(10deg)';
                this.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                this.style.boxShadow = '0 6px 25px rgba(255, 215, 0, 0.6)';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1) rotate(0deg)';
                this.style.boxShadow = '0 4px 15px rgba(255, 215, 0, 0.4)';
            });

            // Effet de clic rebond
            badge.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                this.style.transform = 'scale(0.8)';
                this.style.transition = 'transform 0.2s ease';
                
                setTimeout(function() {
                    badge.style.transform = 'scale(1.25) rotate(-5deg)';
                }, 150);
                
                setTimeout(function() {
                    badge.style.transform = 'scale(1) rotate(0deg)';
                }, 400);

                // Rediriger après l'animation
                var link = badge.closest('.theme-card-link');
                if (link) {
                    var href = link.getAttribute('href');
                    if (href && href !== '#') {
                        setTimeout(function() {
                            window.location.href = href;
                        }, 450);
                    }
                }
            });

            badge.style.cursor = 'pointer';
            badge.setAttribute('title', 'Thème terminé ! Cliquez pour voir le détail');
        });
    }

    /* ==========================================================
     SECTION 6 : ANIMATION DES MINI-BADGES
     ========================================================== */
    function initMiniBadgesAnimation() {
        var miniBadges = document.querySelectorAll('.theme-card .mini-badge');
        if (miniBadges.length === 0) return;

        miniBadges.forEach(function(badge, index) {
            // Apparition séquentielle
            badge.style.opacity = '0';
            badge.style.transform = 'translateY(5px)';
            badge.style.transition = 'all 0.3s ease';
            
            setTimeout(function() {
                badge.style.opacity = '1';
                badge.style.transform = 'translateY(0)';
            }, 400 + (index * 60));

            // Effet au survol
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-2px) scale(1.06)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
            });
        });
    }

    /* ==========================================================
     SECTION 7 : EFFET RIPPLE SUR LE BOUTON "VOIR LE PARCOURS"
     ========================================================== */
    function initVoirParcoursRipple() {
        var voirBtns = document.querySelectorAll('.btn-voir-parcours');
        if (voirBtns.length === 0) return;

        voirBtns.forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                // L'effet ripple n'est pas nécessaire ici car le bouton
                // est dans un lien <a>, mais on ajoute un effet visuel
                
                // Effet mousedown rapide
                this.style.transform = 'scale(0.94)';
                this.style.transition = 'transform 0.15s ease';
                
                setTimeout(function() {
                    btn.style.transform = 'translateY(-2px)';
                }, 150);
            });

            btn.addEventListener('mouseenter', function() {
                var icon = this.querySelector('i');
                if (icon) {
                    icon.style.transform = 'translateX(3px)';
                    icon.style.transition = 'all 0.3s ease';
                }
            });

            btn.addEventListener('mouseleave', function() {
                var icon = this.querySelector('i');
                if (icon) {
                    icon.style.transform = 'translateX(0)';
                }
                this.style.transform = '';
            });
        });
    }

    /* ==========================================================
     SECTION 8 : ANIMATION DE L'ICÔNE DE LA CARTE
     ========================================================== */
    function initIconAnimation() {
        var icons = document.querySelectorAll('.theme-card-icon');
        if (icons.length === 0) return;

        icons.forEach(function(icon) {
            icon.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.1) rotate(-6deg)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            icon.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1) rotate(0deg)';
            });
        });
    }

    setTimeout(initIconAnimation, 600);

    /* ==========================================================
     SECTION 9 : COMPTEUR DE THÈMES (LOG)
     ========================================================== */
    function initThemesCounter() {
        var totalThemes = document.querySelectorAll('.theme-card').length;
        var completedThemes = document.querySelectorAll('.theme-badge-termine').length;
        
        if (totalThemes > 0) {
            console.log(
                '📚 ' + totalThemes + ' thème' + (totalThemes > 1 ? 's' : '') + 
                ' disponible' + (totalThemes > 1 ? 's' : '') + 
                (completedThemes > 0 ? ' | 🏆 ' + completedThemes + ' terminé' + (completedThemes > 1 ? 's' : '') : '')
            );
        }
    }

    setTimeout(initThemesCounter, 800);

    /* ==========================================================
     SECTION 10 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var tropheeBadges = document.querySelectorAll('.theme-badge-termine');
        var dotPulses = document.querySelectorAll('.footer-niveau-actif i');
        
        if (document.hidden) {
            tropheeBadges.forEach(function(badge) {
                badge.style.animationPlayState = 'paused';
            });
            dotPulses.forEach(function(dot) {
                dot.style.animationPlayState = 'paused';
            });
        } else {
            tropheeBadges.forEach(function(badge) {
                badge.style.animationPlayState = 'running';
            });
            dotPulses.forEach(function(dot) {
                dot.style.animationPlayState = 'running';
            });
        }
    });

})();