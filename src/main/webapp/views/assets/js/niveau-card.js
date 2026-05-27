/**
 * ============================================================
 * FICHIER : views/assets/js/components/niveau-card.js
 * RÔLE    : Scripts du composant niveau-card
 *           - Animation des barres de progression au scroll
 *           - Effets de survol avancés (3D tilt)
 *           - Gestion du clic sur les niveaux bloqués
 *           - Animation du badge de statut
 *           - Révélation des cartes au scroll
 *           - Interaction avec l'icône du niveau
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
        initBlockedCardsInteraction();
        initBadgeAnimation();
        initIconInteraction();
    });

    /* ==========================================================
     SECTION 2 : ANIMATION DES BARRES DE PROGRESSION
     ========================================================== */
    function initProgressBarsAnimation() {
        var progressFills = document.querySelectorAll('.niveau-card .progress-fill');
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
                    fill.style.transition = 'width 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                    
                    setTimeout(function() {
                        fill.style.width = targetWidth;
                        
                        // Effet de brillance à la fin
                        setTimeout(function() {
                            fill.style.boxShadow = '0 0 10px rgba(102, 126, 234, 0.3)';
                            setTimeout(function() {
                                fill.style.boxShadow = 'none';
                            }, 600);
                        }, 1200);
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
        var cards = document.querySelectorAll('.niveau-card');
        if (cards.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.1
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var card = entry.target;
                    var delay = index * 100;
                    
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
                card.style.transform = 'translateY(30px) scale(0.95)';
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

        var cards = document.querySelectorAll('.niveau-card.niveau-debloque, .niveau-card.niveau-termine');
        if (cards.length === 0) return;

        cards.forEach(function(card) {
            card.addEventListener('mousemove', function(e) {
                var rect = this.getBoundingClientRect();
                var x = e.clientX - rect.left;
                var y = e.clientY - rect.top;
                
                var centerX = rect.width / 2;
                var centerY = rect.height / 2;
                
                var rotateX = (y - centerY) / centerY * -5;
                var rotateY = (x - centerX) / centerX * 5;
                
                this.style.transform = 'translateY(-5px) perspective(600px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
                this.style.transition = 'transform 0.1s ease';
            });

            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) perspective(600px) rotateX(0) rotateY(0)';
                this.style.transition = 'transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });
        });
    }

    /* ==========================================================
     SECTION 5 : INTERACTION AVEC LES CARTES BLOQUÉES
     ========================================================== */
    function initBlockedCardsInteraction() {
        var blockedCards = document.querySelectorAll('.niveau-card.niveau-bloque');
        if (blockedCards.length === 0) return;

        blockedCards.forEach(function(card) {
            // Empêcher la navigation
            card.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                // Animation de vibration
                shakeElement(card);
                
                // Afficher un message toast
                var niveauNom = card.querySelector('.niveau-nom')?.textContent || 'ce niveau';
                var messageBloque = card.querySelector('.niveau-message-bloque')?.textContent?.trim() || 
                    'Terminez le niveau précédent pour débloquer';
                
                if (window.showToast) {
                    window.showToast('🔒 ' + niveauNom + ' : ' + messageBloque, 'info', 3500);
                }
            });

            // Ajouter un tooltip
            var niveauNom = card.querySelector('.niveau-nom')?.textContent || 'Ce niveau';
            card.setAttribute('data-tooltip', '🔒 ' + niveauNom + ' est bloqué. Terminez le niveau précédent d\'abord.');
        });
    }

    /**
     * Animation de vibration pour les éléments
     */
    function shakeElement(element) {
        if (!element) return;
        
        element.style.animation = 'none';
        element.offsetHeight; // Force reflow
        element.style.animation = 'shake 0.5s ease';
        
        element.addEventListener('animationend', function handler() {
            element.removeEventListener('animationend', handler);
            element.style.animation = '';
        });
    }

    /* ==========================================================
     SECTION 6 : ANIMATION DU BADGE DE STATUT
     ========================================================== */
    function initBadgeAnimation() {
        var badges = document.querySelectorAll('.niveau-statut-badge');
        if (badges.length === 0) return;

        badges.forEach(function(badge) {
            // Animation d'entrée différée
            badge.style.opacity = '0';
            badge.style.transform = 'translateX(10px)';
            badge.style.transition = 'all 0.4s ease 0.5s';
            
            setTimeout(function() {
                badge.style.opacity = '1';
                badge.style.transform = 'translateX(0)';
            }, 600);

            // Effet au survol
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.08)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });
        });
    }

    /* ==========================================================
     SECTION 7 : INTERACTION AVEC L'ICÔNE DU NIVEAU
     ========================================================== */
    function initIconInteraction() {
        var icons = document.querySelectorAll('.niveau-icon-wrapper');
        if (icons.length === 0) return;

        icons.forEach(function(icon) {
            // Effet au survol
            icon.addEventListener('mouseenter', function() {
                if (this.closest('.niveau-bloque')) return;
                
                this.style.transform = 'scale(1.12) rotate(-8deg)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            icon.addEventListener('mouseleave', function() {
                if (this.closest('.niveau-bloque')) return;
                
                this.style.transform = 'scale(1) rotate(0deg)';
            });

            // Effet de clic
            icon.addEventListener('click', function(e) {
                if (this.closest('.niveau-bloque')) {
                    e.preventDefault();
                    e.stopPropagation();
                    return;
                }
                
                this.style.transform = 'scale(0.85)';
                this.style.transition = 'transform 0.2s ease';
                
                setTimeout(function() {
                    icon.style.transform = 'scale(1.05)';
                }, 150);
                
                setTimeout(function() {
                    icon.style.transform = 'scale(1)';
                }, 350);
            });
        });
    }

    /* ==========================================================
     SECTION 8 : ANIMATION DES BOUTONS
     ========================================================== */
    function initButtonAnimation() {
        var buttons = document.querySelectorAll('.btn-niveau:not(.btn-bloque)');
        if (buttons.length === 0) return;

        buttons.forEach(function(btn) {
            btn.addEventListener('mousedown', function() {
                this.style.transform = 'scale(0.95)';
                this.style.transition = 'transform 0.15s ease';
            });

            btn.addEventListener('mouseup', function() {
                this.style.transform = 'translateY(-2px)';
                this.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            btn.addEventListener('mouseleave', function() {
                this.style.transform = '';
            });
        });
    }

    setTimeout(initButtonAnimation, 800);

    /* ==========================================================
     SECTION 9 : STYLE SHAKE (INJECTÉ DANS LE DOM)
     ========================================================== */
    var shakeStyle = document.createElement('style');
    shakeStyle.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
            20%, 40%, 60%, 80% { transform: translateX(5px); }
        }
    `;
    document.head.appendChild(shakeStyle);

    /* ==========================================================
     SECTION 10 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var cards = document.querySelectorAll('.niveau-card.niveau-debloque');
        
        if (document.hidden) {
            cards.forEach(function(card) {
                card.style.animationPlayState = 'paused';
            });
        } else {
            cards.forEach(function(card) {
                card.style.animationPlayState = 'running';
            });
        }
    });

})();