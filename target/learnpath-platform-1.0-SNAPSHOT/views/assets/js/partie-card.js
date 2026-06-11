/**
 * ============================================================
 * FICHIER : views/assets/js/components/partie-card.js
 * RÔLE    : Scripts du composant partie-card
 *           - Animation des cartes au scroll (révélation)
 *           - Effets de survol avancés
 *           - Gestion du clic sur les cartes verrouillées
 *           - Animation des badges
 *           - Effet ripple sur le bouton Jouer
 *           - Interaction avec l'icône de statut
 *           - Performance (pause animations)
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initCardsReveal();
        initHoverEffects();
        initBlockedCardsInteraction();
        initBadgeAnimation();
        initJouerButtonRipple();
        initIconInteraction();
    });

    /* ==========================================================
     SECTION 2 : RÉVÉLATION DES CARTES AU SCROLL
     ========================================================== */
    function initCardsReveal() {
        var cards = document.querySelectorAll('.partie-card');
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
                    var delay = index * 40; // Cascade rapide car beaucoup de cartes
                    
                    setTimeout(function() {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0) scale(1)';
                    }, delay);
                    
                    observer.unobserve(card);
                }
            });
        }, observerOptions);

        cards.forEach(function(card) {
            // Initialiser l'état invisible
            var computedStyle = window.getComputedStyle(card);
            if (!computedStyle.animationName || computedStyle.animationName === 'none') {
                card.style.opacity = '0';
                card.style.transform = 'translateY(20px) scale(0.92)';
                card.style.transition = 'all 0.45s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            }
            observer.observe(card);
        });
    }

    /* ==========================================================
     SECTION 3 : EFFETS DE SURVOL AVANCÉS
     ========================================================== */
    function initHoverEffects() {
        var cards = document.querySelectorAll('.partie-card.partie-prochaine, .partie-card.partie-reussie');
        if (cards.length === 0) return;

        // Effet 3D tilt léger au survol (desktop uniquement)
        if (!window.isTouchDevice || !window.isTouchDevice()) {
            cards.forEach(function(card) {
                card.addEventListener('mousemove', function(e) {
                    var rect = this.getBoundingClientRect();
                    var x = e.clientX - rect.left;
                    var y = e.clientY - rect.top;
                    
                    var centerX = rect.width / 2;
                    var centerY = rect.height / 2;
                    
                    var rotateX = (y - centerY) / centerY * -4;
                    var rotateY = (x - centerX) / centerX * 4;
                    
                    this.style.transform = 'translateY(-4px) perspective(500px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
                    this.style.transition = 'transform 0.1s ease';
                });

                card.addEventListener('mouseleave', function() {
                    this.style.transform = 'translateY(0) perspective(500px) rotateX(0) rotateY(0)';
                    this.style.transition = 'transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                });
            });
        }

        // Animation du hint "Rejouer" au survol des cartes réussies
        var reussieCards = document.querySelectorAll('.partie-card.partie-reussie');
        reussieCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                var hint = this.querySelector('.badge-rejouer-hint');
                if (hint) {
                    hint.style.opacity = '1';
                    hint.style.transform = 'translateY(0)';
                }
            });

            card.addEventListener('mouseleave', function() {
                var hint = this.querySelector('.badge-rejouer-hint');
                if (hint) {
                    hint.style.opacity = '0';
                    hint.style.transform = 'translateY(5px)';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 4 : INTERACTION AVEC LES CARTES VERROUILLÉES
     ========================================================== */
    function initBlockedCardsInteraction() {
        var blockedCards = document.querySelectorAll('.partie-card.partie-verrouillee');
        if (blockedCards.length === 0) return;

        blockedCards.forEach(function(card) {
            // Empêcher la navigation
            card.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                // Animation de vibration
                shakeElement(card);
                
                // Message toast
                var partieNumero = card.getAttribute('data-partie') || 'cette partie';
                if (window.showToast) {
                    window.showToast(
                        '🔒 Partie ' + partieNumero + ' : terminez les parties précédentes d\'abord.',
                        'info',
                        3000
                    );
                }
            });

            // Ajouter un tooltip
            var partieNumero = card.getAttribute('data-partie') || 'Cette partie';
            card.setAttribute('data-tooltip', '🔒 ' + partieNumero + ' est verrouillée. Terminez les parties précédentes.');

            // Style curseur
            card.style.cursor = 'not-allowed';
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
     SECTION 5 : ANIMATION DES BADGES
     ========================================================== */
    function initBadgeAnimation() {
        var badges = document.querySelectorAll('.badge-a-jouer');
        if (badges.length === 0) return;

        badges.forEach(function(badge) {
            // L'animation CSS popIn est déjà appliquée
            // Ajouter un effet au survol
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.1)';
                this.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                this.style.boxShadow = '0 6px 18px rgba(102, 126, 234, 0.45)';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
                this.style.boxShadow = '0 4px 14px rgba(102, 126, 234, 0.35)';
            });
        });

        // Badges "Réussi"
        var reussiBadges = document.querySelectorAll('.badge-reussi-partie');
        reussiBadges.forEach(function(badge) {
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.08)';
                this.style.transition = 'transform 0.3s ease';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });
        });
    }

    /* ==========================================================
     SECTION 6 : EFFET RIPPLE SUR LE BOUTON JOUER
     ========================================================== */
    function initJouerButtonRipple() {
        var jouerBtns = document.querySelectorAll('.btn-jouer-partie');
        if (jouerBtns.length === 0) return;

        jouerBtns.forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                // Créer un élément ripple
                var ripple = document.createElement('span');
                ripple.className = 'btn-ripple-effect';
                
                var rect = this.getBoundingClientRect();
                var size = Math.max(rect.width, rect.height);
                var x = e.clientX - rect.left - size / 2;
                var y = e.clientY - rect.top - size / 2;
                
                ripple.style.cssText = `
                    position: absolute;
                    width: ${size}px;
                    height: ${size}px;
                    left: ${x}px;
                    top: ${y}px;
                    border-radius: 50%;
                    background: rgba(255, 255, 255, 0.35);
                    transform: scale(0);
                    animation: rippleEffect 0.6s ease-out forwards;
                    pointer-events: none;
                    z-index: 0;
                `;
                
                this.style.position = 'relative';
                this.style.overflow = 'hidden';
                this.appendChild(ripple);
                
                // Nettoyer après l'animation
                ripple.addEventListener('animationend', function() {
                    if (ripple.parentNode) {
                        ripple.parentNode.removeChild(ripple);
                    }
                });
            });

            // Effet mousedown/up
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

    /* ==========================================================
     SECTION 7 : INTERACTION AVEC L'ICÔNE DE STATUT
     ========================================================== */
    function initIconInteraction() {
        var icons = document.querySelectorAll('.partie-statut');
        if (icons.length === 0) return;

        icons.forEach(function(icon) {
            var card = icon.closest('.partie-card');
            if (!card || card.classList.contains('partie-verrouillee')) return;

            // Effet au survol de l'icône
            icon.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.2)';
                this.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            icon.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });

            // Effet de clic sur l'icône
            icon.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                this.style.transform = 'scale(0.8)';
                this.style.transition = 'transform 0.2s ease';
                
                setTimeout(function() {
                    icon.style.transform = 'scale(1.15)';
                }, 150);
                
                setTimeout(function() {
                    icon.style.transform = 'scale(1)';
                }, 350);

                // Rediriger après l'animation
                var link = card.querySelector('a') || card.closest('a');
                if (link) {
                    var href = link.getAttribute('href');
                    if (href && href !== '#') {
                        setTimeout(function() {
                            window.location.href = href;
                        }, 400);
                    }
                }
            });

            icon.style.cursor = 'pointer';
        });
    }

    /* ==========================================================
     SECTION 8 : STYLE RIPPLE (INJECTÉ)
     ========================================================== */
    var rippleStyle = document.createElement('style');
    rippleStyle.textContent = `
        @keyframes rippleEffect {
            0% {
                transform: scale(0);
                opacity: 1;
            }
            100% {
                transform: scale(4);
                opacity: 0;
            }
        }
        
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-4px); }
            20%, 40%, 60%, 80% { transform: translateX(4px); }
        }
    `;
    document.head.appendChild(rippleStyle);

    /* ==========================================================
     SECTION 9 : COMPTEUR DE CARTES (ANIMATION)
     ========================================================== */
    function initCardsCounter() {
        var totalCards = document.querySelectorAll('.partie-card').length;
        var reussiesCards = document.querySelectorAll('.partie-card.partie-reussie').length;
        var prochaineCard = document.querySelector('.partie-card.partie-prochaine');

        // Si une carte prochaine existe, on peut logger des infos
        if (prochaineCard) {
            var partieNumero = prochaineCard.getAttribute('data-partie');
            console.log(
                '📊 Progression du niveau : ' + reussiesCards + '/' + totalCards + 
                ' parties réussies. Prochaine : Partie ' + partieNumero
            );
        }
    }

    setTimeout(initCardsCounter, 500);

    /* ==========================================================
     SECTION 10 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var prochaineCards = document.querySelectorAll('.partie-card.partie-prochaine');
        var reussieIcons = document.querySelectorAll('.partie-card.partie-reussie .partie-statut');
        var prochaineIcons = document.querySelectorAll('.partie-card.partie-prochaine .partie-statut');
        
        if (document.hidden) {
            prochaineCards.forEach(function(card) {
                card.style.animationPlayState = 'paused';
            });
            reussieIcons.forEach(function(icon) {
                icon.style.animationPlayState = 'paused';
            });
            prochaineIcons.forEach(function(icon) {
                icon.style.animationPlayState = 'paused';
            });
        } else {
            prochaineCards.forEach(function(card) {
                card.style.animationPlayState = 'running';
            });
            reussieIcons.forEach(function(icon) {
                icon.style.animationPlayState = 'running';
            });
            prochaineIcons.forEach(function(icon) {
                icon.style.animationPlayState = 'running';
            });
        }
    });

})();