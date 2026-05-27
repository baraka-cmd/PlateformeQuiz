/**
 * ============================================================
 * FICHIER : views/assets/js/niveau.js
 * RÔLE    : Scripts de la page niveau AmijayQuiz
 *           - Animation de la barre de progression
 *           - Révélation des cartes parties (cascade)
 *           - Effets de survol avancés sur les cartes parties
 *           - Animation de l'icône du niveau
 *           - Gestion de l'alerte prochaine partie
 *           - Animation du badge "Niveau terminé"
 *           - Raccourcis clavier
 *           - Scroll automatique vers la prochaine partie
 *           - Gestion responsive
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initProgressBarAnimation();
        initPartieCardsReveal();
        initPartieCardHover();
        initNiveauIconInteraction();
        initProchainePartieHighlight();
        initTermineBadgeAnimation();
        initKeyboardNavigation();
        initScrollToProchaine();
        initAlertInteractions();
    });

    /* ==========================================================
     SECTION 2 : ANIMATION BARRE DE PROGRESSION
     ========================================================== */
    function initProgressBarAnimation() {
        const progressBar = document.querySelector('.progression-bar-inner');
        if (!progressBar) return;

        const targetWidth = progressBar.style.width;
        
        // Réinitialiser pour l'animation
        progressBar.style.width = '0%';
        progressBar.style.transition = 'width 1.4s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
        
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
                            progressBar.style.boxShadow = '0 0 15px rgba(102, 126, 234, 0.35)';
                            setTimeout(function() {
                                progressBar.style.boxShadow = 'none';
                            }, 700);
                        }, 1400);
                    }, 250);
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        observer.observe(progressBar.parentElement);
    }

    /* ==========================================================
     SECTION 3 : RÉVÉLATION DES CARTES PARTIES (CASCADE)
     ========================================================== */
    function initPartieCardsReveal() {
        const partieCards = document.querySelectorAll('.partie-card');
        if (partieCards.length === 0) return;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.1
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    const card = entry.target;
                    const delay = index * 50; // Cascade rapide car beaucoup de cartes
                    
                    setTimeout(function() {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0) scale(1)';
                    }, delay);
                    
                    observer.unobserve(card);
                }
            });
        }, observerOptions);

        partieCards.forEach(function(card, index) {
            // Initialiser l'état invisible
            card.style.opacity = '0';
            card.style.transform = 'translateY(25px) scale(0.92)';
            card.style.transition = 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            observer.observe(card);
        });
    }

    /* ==========================================================
     SECTION 4 : EFFETS DE SURVOL CARTES PARTIES
     ========================================================== */
    function initPartieCardHover() {
        const partieCards = document.querySelectorAll('.partie-card.prochaine, .partie-card.reussie');
        if (partieCards.length === 0) return;

        // Effet 3D léger au survol (desktop uniquement)
        if (!window.isTouchDevice || !window.isTouchDevice()) {
            partieCards.forEach(function(card) {
                card.addEventListener('mousemove', function(e) {
                    const rect = this.getBoundingClientRect();
                    const x = e.clientX - rect.left;
                    const y = e.clientY - rect.top;
                    
                    const centerX = rect.width / 2;
                    const centerY = rect.height / 2;
                    
                    const rotateX = (y - centerY) / centerY * -5;
                    const rotateY = (x - centerX) / centerX * 5;
                    
                    this.style.transform = 'translateY(-3px) perspective(600px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
                    this.style.transition = 'transform 0.1s ease';
                });

                card.addEventListener('mouseleave', function() {
                    this.style.transform = 'translateY(0) perspective(600px) rotateX(0) rotateY(0)';
                    this.style.transition = 'transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                });
            });
        }

        // Animation du badge "À jouer"
        const prochaineCard = document.querySelector('.partie-card.prochaine');
        if (prochaineCard) {
            const badge = prochaineCard.querySelector('.badge-a-jouer');
            if (badge) {
                // L'animation CSS est déjà définie, on renforce au survol
                prochaineCard.addEventListener('mouseenter', function() {
                    if (badge) {
                        badge.style.transform = 'scale(1.1)';
                        badge.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                    }
                });
                
                prochaineCard.addEventListener('mouseleave', function() {
                    if (badge) {
                        badge.style.transform = 'scale(1)';
                    }
                });
            }
        }
    }

    /* ==========================================================
     SECTION 5 : INTERACTION AVEC L'ICÔNE DU NIVEAU
     ========================================================== */
    function initNiveauIconInteraction() {
        const niveauIcon = document.querySelector('.niveau-header-icon');
        if (!niveauIcon) return;

        // Effet de rebond au clic
        niveauIcon.addEventListener('click', function() {
            this.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            this.style.transform = 'scale(0.85)';
            
            setTimeout(() => {
                this.style.transform = 'scale(1.12)';
            }, 150);
            
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 400);
        });

        niveauIcon.style.cursor = 'pointer';
        niveauIcon.setAttribute('title', 'Cliquez pour animer');
    }

    /* ==========================================================
     SECTION 6 : MISE EN ÉVIDENCE DE LA PROCHAINE PARTIE
     ========================================================== */
    function initProchainePartieHighlight() {
        const prochaineCard = document.querySelector('.partie-card.prochaine');
        if (!prochaineCard) return;

        // Ajouter un effet de pulse supplémentaire
        const pulseStyle = document.createElement('style');
        pulseStyle.textContent = `
            .partie-card.prochaine {
                animation: prochainePulse 2.5s ease-in-out infinite !important;
            }
            
            @keyframes prochainePulse {
                0%, 100% { 
                    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
                    border-color: #667eea;
                }
                50% { 
                    box-shadow: 0 4px 35px rgba(102, 126, 234, 0.35);
                    border-color: #4a00e0;
                }
            }
        `;
        document.head.appendChild(pulseStyle);

        // Ajouter un attribut pour le tooltip
        prochaineCard.setAttribute('data-tooltip', '🎯 Votre prochaine partie à jouer !');
    }

    /* ==========================================================
     SECTION 7 : ANIMATION BADGE "NIVEAU TERMINÉ"
     ========================================================== */
    function initTermineBadgeAnimation() {
        const termineBadge = document.querySelector('.statut-termine');
        if (!termineBadge) return;

        // Effet de confettis simple (icônes étoiles)
        const badgeRect = termineBadge.getBoundingClientRect();
        
        termineBadge.addEventListener('mouseenter', function() {
            createConfetti(this);
        });
    }

    function createConfetti(element) {
        const emojis = ['⭐', '🎉', '🏆', '✨', '💪'];
        const rect = element.getBoundingClientRect();
        
        for (let i = 0; i < 8; i++) {
            const particle = document.createElement('span');
            particle.textContent = emojis[Math.floor(Math.random() * emojis.length)];
            particle.style.cssText = `
                position: fixed;
                left: ${rect.left + rect.width / 2}px;
                top: ${rect.top + rect.height / 2}px;
                font-size: ${14 + Math.random() * 16}px;
                pointer-events: none;
                z-index: 9999;
                transition: all ${0.8 + Math.random() * 0.6}s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                opacity: 1;
            `;
            
            document.body.appendChild(particle);
            
            const angle = Math.random() * Math.PI * 2;
            const distance = 60 + Math.random() * 80;
            const tx = Math.cos(angle) * distance;
            const ty = Math.sin(angle) * distance - 40;
            
            requestAnimationFrame(() => {
                particle.style.transform = `translate(${tx}px, ${ty}px) rotate(${Math.random() * 360}deg)`;
                particle.style.opacity = '0';
            });
            
            setTimeout(() => {
                if (particle.parentNode) {
                    particle.parentNode.removeChild(particle);
                }
            }, 1500);
        }
    }

    /* ==========================================================
     SECTION 8 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardNavigation() {
        document.addEventListener('keydown', function(e) {
            // Ne pas activer dans les champs de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') {
                return;
            }

            switch (e.key) {
                case 'Escape':
                    // Retour au thème
                    const backBtn = document.querySelector('.btn-retour-niveau');
                    if (backBtn) {
                        window.location.href = backBtn.getAttribute('href');
                    }
                    break;
                    
                case 'j':
                case 'J':
                    // Aller à la prochaine partie à jouer
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        const prochaineLink = document.querySelector('.partie-card.prochaine .btn-jouer-partie');
                        if (prochaineLink) {
                            window.location.href = prochaineLink.getAttribute('href');
                        } else {
                            const alerteBtn = document.querySelector('.alerte-btn');
                            if (alerteBtn) {
                                window.location.href = alerteBtn.getAttribute('href');
                            }
                        }
                    }
                    break;
                    
                case 'ArrowLeft':
                    // Retour au thème avec Alt+Flèche gauche
                    if (e.altKey) {
                        e.preventDefault();
                        const retourBtn = document.querySelector('.btn-retour-niveau');
                        if (retourBtn) {
                            window.location.href = retourBtn.getAttribute('href');
                        }
                    }
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 9 : SCROLL AUTO VERS LA PROCHAINE PARTIE
     ========================================================== */
    function initScrollToProchaine() {
        const prochaineCard = document.querySelector('.partie-card.prochaine');
        if (!prochaineCard) return;

        // Vérifier si on vient d'une autre page (dashboard, theme)
        const referrer = document.referrer;
        const isFromNavigation = referrer && (
            referrer.includes('/joueur/dashboard') || 
            referrer.includes('/joueur/theme')
        );

        if (isFromNavigation) {
            // Petit délai pour laisser la page se charger
            setTimeout(function() {
                const navbarHeight = document.querySelector('.navbar')?.offsetHeight || 70;
                const cardTop = prochaineCard.getBoundingClientRect().top + window.pageYOffset;
                const offsetPosition = cardTop - navbarHeight - 40;
                
                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
                
                // Mettre en évidence brièvement
                prochaineCard.style.transition = 'all 0.3s ease';
                prochaineCard.style.transform = 'scale(1.05)';
                prochaineCard.style.boxShadow = '0 8px 35px rgba(102, 126, 234, 0.4)';
                
                setTimeout(function() {
                    prochaineCard.style.transform = '';
                    prochaineCard.style.boxShadow = '';
                }, 1000);
            }, 500);
        }
    }

    /* ==========================================================
     SECTION 10 : INTERACTIONS SUR LES ALERTES
     ========================================================== */
    function initAlertInteractions() {
        const alertes = document.querySelectorAll('.alerte-info');
        if (alertes.length === 0) return;

        alertes.forEach(function(alerte) {
            // Animation d'entrée déjà gérée par le CSS
            // Effet au survol
            alerte.addEventListener('mouseenter', function() {
                const iconWrapper = this.querySelector('.alerte-icon-wrapper');
                if (iconWrapper) {
                    iconWrapper.style.transform = 'scale(1.1) rotate(-5deg)';
                    iconWrapper.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            alerte.addEventListener('mouseleave', function() {
                const iconWrapper = this.querySelector('.alerte-icon-wrapper');
                if (iconWrapper) {
                    iconWrapper.style.transform = 'scale(1) rotate(0deg)';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 11 : ANIMATION COMPTEUR DE PARTIES
     ========================================================== */
    function initPartiesCountAnimation() {
        const countElements = document.querySelectorAll('.niveau-header-text strong');
        if (countElements.length === 0) return;

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
                    
                    countElements.forEach(function(el) {
                        const text = el.textContent.trim();
                        const target = parseInt(text, 10);
                        
                        if (isNaN(target) || target === 0) return;
                        
                        const duration = 800;
                        const startTime = performance.now();
                        
                        function update(currentTime) {
                            const elapsed = currentTime - startTime;
                            const progress = Math.min(elapsed / duration, 1);
                            const eased = 1 - Math.pow(1 - progress, 3);
                            const current = Math.floor(eased * target);
                            
                            el.textContent = current;
                            
                            if (progress < 1) {
                                requestAnimationFrame(update);
                            } else {
                                el.textContent = target;
                            }
                        }
                        
                        requestAnimationFrame(update);
                    });
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        const header = document.querySelector('.niveau-header');
        if (header) {
            observer.observe(header);
        }
    }

    setTimeout(initPartiesCountAnimation, 500);

    /* ==========================================================
     SECTION 12 : TOOLTIP SUR LES CARTES VERROUILLÉES
     ========================================================== */
    function initLockedCardsTooltip() {
        const lockedCards = document.querySelectorAll('.partie-card.verrouillee');
        if (lockedCards.length === 0) return;

        lockedCards.forEach(function(card) {
            const partieNumero = card.querySelector('.partie-numero')?.textContent || 'cette partie';
            card.setAttribute('data-tooltip', '🔒 ' + partieNumero + ' : terminez les parties précédentes d\'abord');
            
            // Empêcher le clic
            card.addEventListener('click', function(e) {
                e.preventDefault();
                shakeElement(card);
            });
        });
    }

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

    setTimeout(initLockedCardsTooltip, 800);

    /* ==========================================================
     SECTION 13 : ANIMATION BARRES DE PROGRESSION DES CARTES
     ========================================================== */
    function initCardProgressBars() {
        const progressFills = document.querySelectorAll('.partie-card .progress-fill');
        if (progressFills.length === 0) return;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.2
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    const targetWidth = entry.target.style.width;
                    
                    entry.target.style.width = '0%';
                    entry.target.style.transition = 'width 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                    
                    setTimeout(function() {
                        entry.target.style.width = targetWidth;
                    }, 150);
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        progressFills.forEach(function(fill) {
            observer.observe(fill);
        });
    }

    setTimeout(initCardProgressBars, 700);

    /* ==========================================================
     SECTION 14 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        const niveauIcon = document.querySelector('.niveau-header-icon');
        const prochaineCard = document.querySelector('.partie-card.prochaine');
        
        if (document.hidden) {
            if (niveauIcon) niveauIcon.style.animationPlayState = 'paused';
            if (prochaineCard) prochaineCard.style.animationPlayState = 'paused';
        } else {
            if (niveauIcon) niveauIcon.style.animationPlayState = 'running';
            if (prochaineCard) prochaineCard.style.animationPlayState = 'running';
        }
    });

    /* ==========================================================
     SECTION 15 : STYLE SHAKE (INJECTÉ)
     ========================================================== */
    const shakeStyle = document.createElement('style');
    shakeStyle.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-4px); }
            20%, 40%, 60%, 80% { transform: translateX(4px); }
        }
    `;
    document.head.appendChild(shakeStyle);

})();