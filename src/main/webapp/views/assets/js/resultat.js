/**
 * ============================================================
 * FICHIER : views/assets/js/resultat.js
 * RÔLE    : Scripts de la page de résultat LearnPath
 *           - Animation du cercle de score (pop-in)
 *           - Confettis si partie réussie
 *           - Révélation progressive des questions (correction)
 *           - Animation des statistiques
 *           - Effet de comptage animé
 *           - Raccourcis clavier
 *           - Gestion du badge de performance
 *           - Effets de survol avancés
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initScoreCircleAnimation();
        initConfettiIfSuccess();
        initCorrectionReveal();
        initStatsAnimation();
        initPerformanceIconAnimation();
        initKeyboardShortcuts();
        initHoverEffects();
    });

    /* ==========================================================
     SECTION 2 : DÉTECTION RÉUSSITE (DEPUIS LE DOM)
     ========================================================== */
    function isPartieReussie() {
        var scoreBadge = document.querySelector('.badge-reussi');
        return scoreBadge !== null;
    }

    /* ==========================================================
     SECTION 3 : ANIMATION DU CERCLE DE SCORE
     ========================================================== */
    function initScoreCircleAnimation() {
        var scoreCircle = document.querySelector('.score-circle');
        if (!scoreCircle) return;

        // L'animation CSS popIn est déjà appliquée
        // On ajoute un effet de particules autour du cercle
        scoreCircle.addEventListener('animationend', function() {
            // Ajouter une légère pulsation continue si réussi
            if (scoreCircle.classList.contains('score-reussi')) {
                scoreCircle.style.animation = 'scorePopIn 1s ease, scoreGlow 3s ease-in-out 1s infinite';
            }
        });

        // Ajouter l'animation glow
        var glowStyle = document.createElement('style');
        glowStyle.textContent = `
            @keyframes scoreGlow {
                0%, 100% { box-shadow: 0 0 20px rgba(46, 204, 113, 0.15); }
                50% { box-shadow: 0 0 40px rgba(46, 204, 113, 0.3); }
            }
        `;
        document.head.appendChild(glowStyle);
    }

    /* ==========================================================
     SECTION 4 : CONFETTIS SI PARTIE RÉUSSIE
     ========================================================== */
    function initConfettiIfSuccess() {
        if (!isPartieReussie()) return;

        // Délai pour laisser les animations CSS se terminer
        setTimeout(function() {
            launchConfetti();
        }, 1200);
    }

    function launchConfetti() {
        var confettiContainer = document.createElement('div');
        confettiContainer.className = 'confetti-container';
        confettiContainer.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: 9999;
            overflow: hidden;
        `;
        document.body.appendChild(confettiContainer);

        var colors = [
            '#667eea', '#764ba2', '#27ae60', '#f39c12', '#e74c3c',
            '#f093fb', '#4facfe', '#43e97b', '#ffd700', '#ff6b6b',
            '#a29bfe', '#fd79a8', '#00cec9', '#fab1a0', '#81ecec'
        ];

        var shapes = ['square', 'circle', 'triangle'];

        for (var i = 0; i < 120; i++) {
            createConfettiPiece(confettiContainer, colors, shapes, i);
        }

        // Nettoyer après l'animation
        setTimeout(function() {
            confettiContainer.style.transition = 'opacity 0.8s ease';
            confettiContainer.style.opacity = '0';
            setTimeout(function() {
                if (confettiContainer.parentNode) {
                    confettiContainer.parentNode.removeChild(confettiContainer);
                }
            }, 800);
        }, 5000);
    }

    function createConfettiPiece(container, colors, shapes, index) {
        var piece = document.createElement('div');
        
        var color = colors[Math.floor(Math.random() * colors.length)];
        var shape = shapes[Math.floor(Math.random() * shapes.length)];
        var size = 6 + Math.random() * 10;
        var startX = Math.random() * window.innerWidth;
        var startY = -30;
        var rotation = Math.random() * 360;
        var duration = 2.5 + Math.random() * 3;
        var delay = Math.random() * 0.8;

        var styleBase = `
            position: absolute;
            left: ${startX}px;
            top: ${startY}px;
            background: ${color};
            opacity: 1;
            animation: confettiFall ${duration}s ease-in ${delay}s forwards;
        `;

        if (shape === 'square') {
            piece.style.cssText = styleBase + `
                width: ${size}px;
                height: ${size}px;
                border-radius: 3px;
                transform: rotate(${rotation}deg);
            `;
        } else if (shape === 'circle') {
            piece.style.cssText = styleBase + `
                width: ${size}px;
                height: ${size}px;
                border-radius: 50%;
            `;
        } else {
            // Triangle
            piece.style.cssText = styleBase + `
                width: 0;
                height: 0;
                border-left: ${size/2}px solid transparent;
                border-right: ${size/2}px solid transparent;
                border-bottom: ${size}px solid ${color};
                background: transparent;
                transform: rotate(${rotation}deg);
            `;
        }

        container.appendChild(piece);

        // Nettoyer la pièce après son animation
        setTimeout(function() {
            if (piece.parentNode) {
                piece.parentNode.removeChild(piece);
            }
        }, (duration + delay) * 1000 + 200);
    }

    // Ajouter l'animation CSS pour les confettis
    var confettiStyle = document.createElement('style');
    confettiStyle.textContent = `
        @keyframes confettiFall {
            0% {
                transform: translateY(0) rotate(0deg) scale(1);
                opacity: 1;
            }
            25% {
                transform: translateY(${window.innerHeight * 0.3}px) rotate(180deg) scale(0.9);
                opacity: 0.9;
            }
            50% {
                transform: translateY(${window.innerHeight * 0.6}px) rotate(360deg) scale(0.8);
                opacity: 0.7;
            }
            75% {
                transform: translateY(${window.innerHeight * 0.85}px) rotate(540deg) scale(0.6);
                opacity: 0.4;
            }
            100% {
                transform: translateY(${window.innerHeight + 50}px) rotate(720deg) scale(0.2);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(confettiStyle);

    /* ==========================================================
     SECTION 5 : RÉVÉLATION PROGRESSIVE DE LA CORRECTION
     ========================================================== */
    function initCorrectionReveal() {
        var questions = document.querySelectorAll('.question-resultat');
        if (questions.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.1
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var question = entry.target;
                    var delay = index * 80;
                    
                    setTimeout(function() {
                        question.style.opacity = '1';
                        question.style.transform = 'translateX(0)';
                    }, delay);
                    
                    observer.unobserve(question);
                }
            });
        }, observerOptions);

        questions.forEach(function(q) {
            q.style.opacity = '0';
            q.style.transform = 'translateX(-20px)';
            q.style.transition = 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            observer.observe(q);
        });
    }

    /* ==========================================================
     SECTION 6 : ANIMATION DES STATISTIQUES
     ========================================================== */
    function initStatsAnimation() {
        var statItems = document.querySelectorAll('.stat-item');
        if (statItems.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.3
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var item = entry.target;
                    
                    setTimeout(function() {
                        item.style.opacity = '1';
                        item.style.transform = 'translateY(0)';
                    }, index * 120);
                    
                    observer.unobserve(item);
                }
            });
        }, observerOptions);

        statItems.forEach(function(item) {
            item.style.opacity = '0';
            item.style.transform = 'translateY(15px)';
            item.style.transition = 'all 0.5s ease';
            observer.observe(item);
        });
    }

    /* ==========================================================
     SECTION 7 : ANIMATION DE L'ICÔNE DE PERFORMANCE
     ========================================================== */
    function initPerformanceIconAnimation() {
        var perfIcon = document.querySelector('.performance-icon');
        if (!perfIcon) return;

        // Ajouter un effet au survol
        perfIcon.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.2) rotate(10deg)';
            this.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
        });

        perfIcon.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1) rotate(0deg)';
        });

        // Effet de clic
        perfIcon.addEventListener('click', function() {
            this.style.transform = 'scale(0.8) rotate(-15deg)';
            setTimeout(() => {
                this.style.transform = 'scale(1.15) rotate(5deg)';
            }, 150);
            setTimeout(() => {
                this.style.transform = 'scale(1) rotate(0deg)';
            }, 400);
        });

        perfIcon.style.cursor = 'pointer';
    }

    /* ==========================================================
     SECTION 8 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ne pas activer dans les champs de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
                return;
            }

            switch (e.key) {
                case 'd':
                case 'D':
                    // Aller au dashboard
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var dashboardBtn = document.querySelector('.btn-action-primary');
                        if (dashboardBtn) {
                            window.location.href = dashboardBtn.getAttribute('href');
                        }
                    }
                    break;

                case 'n':
                case 'N':
                    // Aller au niveau
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var niveauBtn = document.querySelector('.btn-action-secondary');
                        if (niveauBtn) {
                            window.location.href = niveauBtn.getAttribute('href');
                        }
                    }
                    break;

                case 'h':
                case 'H':
                    // Aller à l'historique
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var historiqueBtn = document.querySelector('.btn-action-outline');
                        if (historiqueBtn) {
                            window.location.href = historiqueBtn.getAttribute('href');
                        }
                    }
                    break;

                case 's':
                case 'S':
                    // Partie suivante si disponible
                    if (e.ctrlKey || e.metaKey) {
                        var btnSuite = document.querySelector('.btn-suite');
                        if (btnSuite) {
                            e.preventDefault();
                            window.location.href = btnSuite.getAttribute('href');
                        }
                    }
                    break;

                case 'r':
                case 'R':
                    // Réessayer si disponible
                    if (e.ctrlKey || e.metaKey) {
                        var btnReessayer = document.querySelector('.btn-reessayer');
                        if (btnReessayer) {
                            e.preventDefault();
                            window.location.href = btnReessayer.getAttribute('href');
                        }
                    }
                    break;

                case 'Escape':
                    // Retour au dashboard
                    var dashboardBtn = document.querySelector('.btn-action-primary');
                    if (dashboardBtn) {
                        window.location.href = dashboardBtn.getAttribute('href');
                    }
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 9 : EFFETS DE SURVOL AVANCÉS
     ========================================================== */
    function initHoverEffects() {
        // Cartes de correction
        var questions = document.querySelectorAll('.question-resultat');
        questions.forEach(function(q) {
            q.addEventListener('mouseenter', function() {
                var badge = this.querySelector('.qr-badge');
                if (badge) {
                    badge.style.transform = 'scale(1.1)';
                    badge.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            q.addEventListener('mouseleave', function() {
                var badge = this.querySelector('.qr-badge');
                if (badge) {
                    badge.style.transform = 'scale(1)';
                }
            });
        });

        // Boutons d'action
        var actionBtns = document.querySelectorAll('.btn-action');
        actionBtns.forEach(function(btn) {
            btn.addEventListener('mousedown', function() {
                this.style.transform = 'scale(0.95)';
            });

            btn.addEventListener('mouseup', function() {
                this.style.transform = 'translateY(-3px)';
            });

            btn.addEventListener('mouseleave', function() {
                this.style.transform = '';
            });
        });

        // Alerte progression
        var alertes = document.querySelectorAll('.alerte-progression');
        alertes.forEach(function(alerte) {
            alerte.addEventListener('mouseenter', function() {
                var iconWrapper = this.querySelector('.alerte-icon-wrapper');
                if (iconWrapper) {
                    iconWrapper.style.transform = 'scale(1.1) rotate(-8deg)';
                    iconWrapper.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            alerte.addEventListener('mouseleave', function() {
                var iconWrapper = this.querySelector('.alerte-icon-wrapper');
                if (iconWrapper) {
                    iconWrapper.style.transform = 'scale(1) rotate(0deg)';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 10 : ANIMATION DU TROPHÉE (SI NIVEAU TERMINÉ)
     ========================================================== */
    function initTropheeAnimation() {
        var trophee = document.querySelector('.alerte-trophee');
        if (!trophee) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.5
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'scale(1)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        trophee.style.opacity = '0';
        trophee.style.transform = 'scale(0.5)';
        trophee.style.transition = 'all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
        observer.observe(trophee);
    }

    setTimeout(initTropheeAnimation, 600);

    /* ==========================================================
     SECTION 11 : DÉFILEMENT DOUX VERS LA CORRECTION
     ========================================================== */
    function initScrollToCorrection() {
        // Détecter si on vient de terminer un quiz (première visite)
        var isFirstView = !document.referrer.includes('/joueur/correction');
        
        if (isFirstView) {
            var detailCard = document.querySelector('.detail-card');
            if (detailCard) {
                setTimeout(function() {
                    var navbarHeight = document.querySelector('.navbar')?.offsetHeight || 70;
                    var cardTop = detailCard.getBoundingClientRect().top + window.pageYOffset;
                    var offsetPosition = cardTop - navbarHeight - 30;
                    
                    // Ne pas scroller automatiquement, laisser l'utilisateur voir son score d'abord
                    // Décommenter pour activer le scroll auto :
                    // window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
                }, 2000);
            }
        }
    }

    initScrollToCorrection();

    /* ==========================================================
     SECTION 12 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var scoreCircle = document.querySelector('.score-circle');
        
        if (document.hidden) {
            if (scoreCircle && scoreCircle.classList.contains('score-reussi')) {
                scoreCircle.style.animationPlayState = 'paused';
            }
        } else {
            if (scoreCircle && scoreCircle.classList.contains('score-reussi')) {
                scoreCircle.style.animationPlayState = 'running';
            }
        }
    });

})();