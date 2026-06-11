/**
 * ============================================================
 * FICHIER : views/assets/js/cours-resultat-examen.js
 * RÔLE    : Scripts de la page de résultat d'examen
 *           - Animation du cercle de score
 *           - Confettis si réussite
 *           - Révélation progressive de la correction
 *           - Raccourcis clavier
 *           - Effets de survol
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initScoreAnimation();
        initConfettiIfSuccess();
        initCorrectionReveal();
        initKeyboardShortcuts();
        initHoverEffects();
    });

    /* ==========================================================
     SECTION 2 : DÉTECTION RÉUSSITE
     ========================================================== */
    function isReussi() {
        var badgeReussi = document.querySelector('.badge-reussi');
        return badgeReussi !== null;
    }

    /* ==========================================================
     SECTION 3 : ANIMATION DU CERCLE DE SCORE
     ========================================================== */
    function initScoreAnimation() {
        var scoreCircle = document.querySelector('.score-circle');
        if (!scoreCircle) return;

        // Ajouter une pulsation continue si réussi
        scoreCircle.addEventListener('animationend', function() {
            if (scoreCircle.classList.contains('circle-reussi')) {
                scoreCircle.style.animation = 'circlePopIn 1s ease, scoreGlow 3s ease-in-out 1s infinite';
            }
        });

        // Injecter l'animation glow
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
     SECTION 4 : CONFETTIS SI RÉUSSITE
     ========================================================== */
    function initConfettiIfSuccess() {
        if (!isReussi()) return;

        setTimeout(function() {
            lancerConfettis();
        }, 1200);
    }

    function lancerConfettis() {
        var container = document.createElement('div');
        container.className = 'confetti-container';
        container.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            pointer-events: none; z-index: 9999; overflow: hidden;
        `;
        document.body.appendChild(container);

        var couleurs = [
            '#667eea', '#764ba2', '#27ae60', '#f39c12', '#e74c3c',
            '#f093fb', '#4facfe', '#43e97b', '#ffd700', '#ff6b6b',
            '#a29bfe', '#fd79a8', '#00cec9', '#fab1a0', '#81ecec'
        ];

        for (var i = 0; i < 100; i++) {
            creerConfetti(container, couleurs, i);
        }

        // Nettoyer après 5 secondes
        setTimeout(function() {
            container.style.transition = 'opacity 0.8s ease';
            container.style.opacity = '0';
            setTimeout(function() {
                if (container.parentNode) container.parentNode.removeChild(container);
            }, 800);
        }, 5000);
    }

    function creerConfetti(container, couleurs, index) {
        var piece = document.createElement('div');
        var couleur = couleurs[Math.floor(Math.random() * couleurs.length)];
        var forme = Math.floor(Math.random() * 3); // 0: carré, 1: cercle, 2: triangle
        var taille = 6 + Math.random() * 10;
        var startX = Math.random() * window.innerWidth;
        var rotation = Math.random() * 360;
        var duree = 2.5 + Math.random() * 3;
        var delai = Math.random() * 0.8;

        var styleBase = `
            position: absolute; left: ${startX}px; top: -30px;
            opacity: 1; background: ${couleur};
            animation: confettiFall ${duree}s ease-in ${delai}s forwards;
        `;

        if (forme === 0) {
            piece.style.cssText = styleBase + `width:${taille}px;height:${taille}px;border-radius:3px;transform:rotate(${rotation}deg);`;
        } else if (forme === 1) {
            piece.style.cssText = styleBase + `width:${taille}px;height:${taille}px;border-radius:50%;`;
        } else {
            piece.style.cssText = styleBase + `
                width:0;height:0;
                border-left:${taille/2}px solid transparent;
                border-right:${taille/2}px solid transparent;
                border-bottom:${taille}px solid ${couleur};
                background:transparent;
                transform:rotate(${rotation}deg);
            `;
        }

        container.appendChild(piece);

        setTimeout(function() {
            if (piece.parentNode) piece.parentNode.removeChild(piece);
        }, (duree + delai) * 1000 + 200);
    }

    // Injecter l'animation de chute
    var confettiStyle = document.createElement('style');
    confettiStyle.textContent = `
        @keyframes confettiFall {
            0% { transform: translateY(0) rotate(0deg) scale(1); opacity: 1; }
            25% { transform: translateY(${window.innerHeight * 0.3}px) rotate(180deg) scale(0.9); opacity: 0.9; }
            50% { transform: translateY(${window.innerHeight * 0.6}px) rotate(360deg) scale(0.8); opacity: 0.7; }
            75% { transform: translateY(${window.innerHeight * 0.85}px) rotate(540deg) scale(0.6); opacity: 0.4; }
            100% { transform: translateY(${window.innerHeight + 50}px) rotate(720deg) scale(0.2); opacity: 0; }
        }
    `;
    document.head.appendChild(confettiStyle);

    /* ==========================================================
     SECTION 5 : RÉVÉLATION PROGRESSIVE DE LA CORRECTION
     ========================================================== */
    function initCorrectionReveal() {
        var items = document.querySelectorAll('.correction-item');
        if (items.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.1
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var item = entry.target;
                    var delay = index * 60;

                    setTimeout(function() {
                        item.style.opacity = '1';
                        item.style.transform = 'translateX(0)';
                    }, delay);

                    observer.unobserve(item);
                }
            });
        }, observerOptions);

        items.forEach(function(item) {
            item.style.opacity = '0';
            item.style.transform = 'translateX(-15px)';
            item.style.transition = 'all 0.4s ease';
            observer.observe(item);
        });
    }

    /* ==========================================================
     SECTION 6 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

            switch (e.key) {
                case 'c': case 'C':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var certBtn = document.querySelector('.btn-certificat, .btn-certificat-bottom');
                        if (certBtn) window.location.href = certBtn.getAttribute('href');
                    }
                    break;
                case 't': case 'T':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var themeBtn = document.querySelector('.btn-theme');
                        if (themeBtn) window.location.href = themeBtn.getAttribute('href');
                    }
                    break;
                case 'r': case 'R':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var reessayerBtn = document.querySelector('.btn-reessayer');
                        if (reessayerBtn) window.location.href = reessayerBtn.getAttribute('href');
                    }
                    break;
                case 'Escape':
                    var themeBtn = document.querySelector('.btn-theme');
                    if (themeBtn) window.location.href = themeBtn.getAttribute('href');
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 7 : EFFETS DE SURVOL
     ========================================================== */
    function initHoverEffects() {
        // Animation des items de correction
        var items = document.querySelectorAll('.correction-item');
        items.forEach(function(item) {
            item.addEventListener('mouseenter', function() {
                var badge = this.querySelector('.correction-badge');
                if (badge) {
                    badge.style.transform = 'scale(1.08)';
                    badge.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });
            item.addEventListener('mouseleave', function() {
                var badge = this.querySelector('.correction-badge');
                if (badge) badge.style.transform = 'scale(1)';
            });
        });

        // Animation de la mention
        var mentionCard = document.querySelector('.mention-card');
        if (mentionCard) {
            mentionCard.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.mention-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.2) rotate(8deg)';
                    icon.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });
            mentionCard.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.mention-icon');
                if (icon) icon.style.transform = 'scale(1) rotate(0deg)';
            });
        }

        // Animation des boutons
        var btns = document.querySelectorAll('.btn-action, .btn-action-bottom');
        btns.forEach(function(btn) {
            btn.addEventListener('mousedown', function() {
                this.style.transform = 'scale(0.95)';
                this.style.transition = 'all 0.15s ease';
            });
            btn.addEventListener('mouseup', function() {
                this.style.transform = 'translateY(-2px)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });
            btn.addEventListener('mouseleave', function() {
                this.style.transform = '';
            });
        });
    }

    /* ==========================================================
     SECTION 8 : ANIMATION DU CODE DE VÉRIFICATION
     ========================================================== */
    function initCodeAnimation() {
        var codeStrong = document.querySelector('.mention-code strong');
        if (!codeStrong) return;

        codeStrong.addEventListener('click', function() {
            // Copier le code
            var code = this.textContent.trim();
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(code).then(function() {
                    var original = codeStrong.textContent;
                    codeStrong.textContent = '✓ Copié !';
                    codeStrong.style.background = '#27ae60';
                    codeStrong.style.color = 'white';
                    setTimeout(function() {
                        codeStrong.textContent = original;
                        codeStrong.style.background = '#f0f4ff';
                        codeStrong.style.color = '#667eea';
                    }, 2000);
                });
            }
        });

        codeStrong.style.cursor = 'pointer';
        codeStrong.setAttribute('title', 'Cliquer pour copier le code');
    }

    setTimeout(initCodeAnimation, 800);

    /* ==========================================================
     SECTION 9 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var scoreCircle = document.querySelector('.circle-reussi');
        if (document.hidden) {
            if (scoreCircle) scoreCircle.style.animationPlayState = 'paused';
        } else {
            if (scoreCircle) scoreCircle.style.animationPlayState = 'running';
        }
    });

})();