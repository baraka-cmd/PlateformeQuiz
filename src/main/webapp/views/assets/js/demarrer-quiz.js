/**
 * ============================================================
 * FICHIER : views/assets/js/demarrer-quiz.js
 * RÔLE    : Scripts de la page de présentation du quiz
 *           - Animation de l'icône de la bannière
 *           - Effet ripple sur le bouton Démarrer
 *           - Raccourci clavier (Entrée pour démarrer)
 *           - Animation des badges au survol
 *           - Compteur à rebours simulé dans les infos
 *           - Effets de survol avancés
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initBannerIconInteraction();
        initStartButtonEffects();
        initKeyboardShortcut();
        initBadgeHoverEffects();
        initInfoCardAnimations();
        initSmoothScrollToTop();
    });

    /* ==========================================================
     SECTION 2 : INTERACTION AVEC L'ICÔNE DE LA BANNIÈRE
     ========================================================== */
    function initBannerIconInteraction() {
        var bannerIcon = document.querySelector('.banner-icon');
        if (!bannerIcon) return;

        // Effet de clic rebond
        bannerIcon.addEventListener('click', function() {
            this.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            this.style.transform = 'scale(0.85) rotate(-10deg)';
            
            setTimeout(function() {
                bannerIcon.style.transform = 'scale(1.1) rotate(5deg)';
            }, 150);
            
            setTimeout(function() {
                bannerIcon.style.transform = 'scale(1) rotate(0deg)';
            }, 400);
        });

        // Curseur pointer
        bannerIcon.style.cursor = 'pointer';
        bannerIcon.setAttribute('title', 'Cliquez pour animer');
        bannerIcon.setAttribute('data-tooltip', 'Prêt à commencer ? 🚀');
    }

    /* ==========================================================
     SECTION 3 : EFFETS SUR LE BOUTON DÉMARRER
     ========================================================== */
    function initStartButtonEffects() {
        var startBtn = document.querySelector('.btn-demarrer');
        if (!startBtn) return;

        // Effet ripple amélioré au clic
        startBtn.addEventListener('click', function(e) {
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
                background: rgba(255, 255, 255, 0.3);
                transform: scale(0);
                animation: rippleEffect 0.6s ease-out forwards;
                pointer-events: none;
                z-index: 0;
            `;
            
            this.appendChild(ripple);
            
            // Nettoyer après l'animation
            ripple.addEventListener('animationend', function() {
                if (ripple.parentNode) {
                    ripple.parentNode.removeChild(ripple);
                }
            });
        });

        // Effet de survol : déplacer l'icône
        startBtn.addEventListener('mouseenter', function() {
            var icon = this.querySelector('.fa-play-circle');
            if (icon) {
                icon.style.transform = 'scale(1.3)';
                icon.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            }
        });

        startBtn.addEventListener('mouseleave', function() {
            var icon = this.querySelector('.fa-play-circle');
            if (icon) {
                icon.style.transform = 'scale(1)';
            }
        });

        // Effet au focus (accessibilité)
        startBtn.addEventListener('focus', function() {
            this.style.boxShadow = '0 0 0 4px rgba(102, 126, 234, 0.3), 0 8px 25px rgba(102, 126, 234, 0.3)';
        });

        startBtn.addEventListener('blur', function() {
            this.style.boxShadow = '0 8px 25px rgba(102, 126, 234, 0.3)';
        });
    }

    // Ajouter l'animation ripple au DOM
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
        
        .btn-demarrer {
            position: relative;
            overflow: hidden;
        }
    `;
    document.head.appendChild(rippleStyle);

    /* ==========================================================
     SECTION 4 : RACCOURCI CLAVIER (ENTRÉE POUR DÉMARRER)
     ========================================================== */
    function initKeyboardShortcut() {
        document.addEventListener('keydown', function(e) {
            // Ne pas activer dans les champs de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') {
                return;
            }

            switch (e.key) {
                case 'Enter':
                    // Démarrer le quiz
                    var startBtn = document.querySelector('.btn-demarrer');
                    if (startBtn) {
                        e.preventDefault();
                        startBtn.click();
                    }
                    break;

                case 'Escape':
                    // Retour au niveau
                    var backBtn = document.querySelector('.btn-retour');
                    if (backBtn) {
                        window.location.href = backBtn.getAttribute('href');
                    }
                    break;

                case 'ArrowLeft':
                    // Retour au niveau avec Alt+Flèche gauche
                    if (e.altKey) {
                        e.preventDefault();
                        var retourBtn = document.querySelector('.btn-retour');
                        if (retourBtn) {
                            window.location.href = retourBtn.getAttribute('href');
                        }
                    }
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 5 : EFFETS DE SURVOL DES BADGES
     ========================================================== */
    function initBadgeHoverEffects() {
        var badges = document.querySelectorAll('.badge-niveau');
        if (badges.length === 0) return;

        badges.forEach(function(badge) {
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-3px) scale(1.05)';
                this.style.boxShadow = '0 6px 15px rgba(0, 0, 0, 0.15)';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
                this.style.boxShadow = 'none';
            });
        });
    }

    /* ==========================================================
     SECTION 6 : ANIMATION DES CARTES D'INFORMATION
     ========================================================== */
    function initInfoCardAnimations() {
        var infoCards = document.querySelectorAll('.info-card');
        if (infoCards.length === 0) return;

        // Observer pour révéler les cartes au scroll
        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.15
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var card = entry.target;
                    var delay = index * 100;
                    
                    setTimeout(function() {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0)';
                    }, delay);
                    
                    observer.unobserve(card);
                }
            });
        }, observerOptions);

        infoCards.forEach(function(card) {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            observer.observe(card);
        });

        // Effet de survol avancé
        infoCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.info-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.15) rotate(-5deg)';
                    icon.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            card.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.info-icon');
                if (icon) {
                    icon.style.transform = 'scale(1) rotate(0deg)';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 7 : SCROLL DOUX VERS LE HAUT
     ========================================================== */
    function initSmoothScrollToTop() {
        // Scroll vers le haut au chargement (utile si on vient d'une page plus basse)
        setTimeout(function() {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        }, 150);
    }

    /* ==========================================================
     SECTION 8 : ANIMATION DE LA BARRE DE SEUIL
     ========================================================== */
    function initSeuilAnimation() {
        var seuilAlerte = document.querySelector('.seuil-alerte');
        if (!seuilAlerte) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.5
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateX(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        seuilAlerte.style.opacity = '0';
        seuilAlerte.style.transform = 'translateX(-20px)';
        seuilAlerte.style.transition = 'all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
        observer.observe(seuilAlerte);
    }

    setTimeout(initSeuilAnimation, 300);

    /* ==========================================================
     SECTION 9 : ANIMATION DE LA LISTE DES RÈGLES
     ========================================================== */
    function initReglesAnimation() {
        var reglesItems = document.querySelectorAll('.regles-liste li');
        if (reglesItems.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -10px 0px',
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
                    
                    observer.unobserve(item);
                }
            });
        }, observerOptions);

        reglesItems.forEach(function(item) {
            item.style.opacity = '0';
            item.style.transform = 'translateX(-15px)';
            item.style.transition = 'all 0.4s ease';
            observer.observe(item);
        });
    }

    setTimeout(initReglesAnimation, 500);

    /* ==========================================================
     SECTION 10 : CONFIRMATION AVANT DE DÉMARRER (OPTIONNEL)
     ========================================================== */
    function initStartConfirmation() {
        var startForm = document.querySelector('.demarrer-form');
        if (!startForm) return;

        startForm.addEventListener('submit', function(e) {
            // Animation de transition
            var card = document.querySelector('.quiz-presentation-card');
            if (card) {
                card.style.transition = 'all 0.3s ease';
                card.style.opacity = '0.6';
                card.style.transform = 'scale(0.98)';
            }

            // Désactiver le bouton pour éviter double clic
            var btn = this.querySelector('.btn-demarrer');
            if (btn) {
                btn.disabled = true;
                btn.style.opacity = '0.7';
                btn.style.cursor = 'wait';
                
                var btnText = btn.querySelector('span:first-of-type');
                if (btnText) {
                    btnText.textContent = 'Démarrage en cours...';
                }
            }
        });
    }

    initStartConfirmation();

    /* ==========================================================
     SECTION 11 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var bannerIcon = document.querySelector('.banner-icon');
        var playIcon = document.querySelector('.btn-demarrer .fa-play-circle');
        
        if (document.hidden) {
            if (bannerIcon) bannerIcon.style.animationPlayState = 'paused';
            if (playIcon) playIcon.style.animationPlayState = 'paused';
        } else {
            if (bannerIcon) bannerIcon.style.animationPlayState = 'running';
            if (playIcon) playIcon.style.animationPlayState = 'running';
        }
    });

})();