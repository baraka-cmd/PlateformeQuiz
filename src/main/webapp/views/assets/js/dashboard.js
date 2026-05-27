/**
 * ============================================================
 * FICHIER : views/assets/js/dashboard.js
 * RÔLE    : Scripts du tableau de bord joueur AmijayQuiz
 *           - Animation des compteurs de statistiques (6 cartes)
 *           - Animation des cartes au scroll (Intersection Observer)
 *           - Gestion du message de bienvenue selon l'heure
 *           - Mise à jour automatique de la date
 *           - Animation de la carte "Reprendre" (Quiz + Cours)
 *           - Gestion des tooltips sur les badges
 *           - Effets de survol avancés
 *           - Animation des barres de progression (Quiz + Cours)
 *           - Gestion du responsive (tableau scrollable)
 *           - Raccourcis clavier
 *           - Gestion des onglets (Quiz/Cours)
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION PRINCIPALE
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initCounters();
        initScrollReveal();
        initProgressBars();
        initCoursProgressBars();
        initResumeCardsAnimation();
        initTableResponsive();
        initStatCardHover();
        initQuickActions();
        initDateUpdate();
        initBadgeAnimations();
        initActionTooltips();
        initTipCardEffects();
        initEmptyStateActions();
        initAvatarAnimation();
        initThemeCardHover();
        initCoursCardHover();
    });

    /* ==========================================================
     SECTION 2 : ANIMATION DES COMPTEURS DE STATISTIQUES
     ========================================================== */
    function initCounters() {
        const statNumbers = document.querySelectorAll('.stat-card .stat-number');
        if (statNumbers.length === 0) return;

        let countersAnimated = false;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -50px 0px',
            threshold: 0.2
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting && !countersAnimated) {
                    countersAnimated = true;
                    animateAllCounters(statNumbers);
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        const statsSection = document.querySelector('.stats-section');
        if (statsSection) {
            observer.observe(statsSection);
        } else {
            // Fallback : animer après un court délai
            setTimeout(function() {
                animateAllCounters(statNumbers);
            }, 500);
        }
    }

    /**
     * Anime tous les compteurs de statistiques (Quiz + Cours)
     */
    function animateAllCounters(statNumbers) {
        statNumbers.forEach(function(counter, index) {
            const text = counter.textContent.trim();
            
            // Extraire la valeur numérique (gère "85%" → 85, "12" → 12, "3.5" → 3.5)
            const match = text.match(/^([\d]+(\.[\d]+)?)/);
            if (!match) return;

            const targetValue = parseFloat(match[1]);
            const suffix = text.replace(match[1], '');
            const isDecimal = match[2] !== undefined;
            const decimals = isDecimal ? match[2].length - 1 : 0;

            const duration = 1800 + (index * 150);
            const startTime = performance.now();
            const startValue = 0;

            function updateCounter(currentTime) {
                const elapsed = currentTime - startTime;
                const progress = Math.min(elapsed / duration, 1);

                // Easing easeOutCubic pour un ralentissement naturel
                const easedProgress = 1 - Math.pow(1 - progress, 3);

                const currentValue = startValue + (targetValue - startValue) * easedProgress;
                
                if (isDecimal) {
                    counter.textContent = currentValue.toFixed(decimals) + suffix;
                } else {
                    counter.textContent = Math.floor(currentValue) + suffix;
                }

                if (progress < 1) {
                    requestAnimationFrame(updateCounter);
                } else {
                    counter.textContent = targetValue.toFixed(decimals) + suffix;

                    // Animation de validation finale
                    counter.style.transform = 'scale(1.08)';
                    counter.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                    
                    setTimeout(function() {
                        counter.style.transform = 'scale(1)';
                    }, 300);
                }
            }

            setTimeout(function() {
                requestAnimationFrame(updateCounter);
            }, index * 100);
        });
    }

    /* ==========================================================
     SECTION 3 : RÉVÉLATION DES CARTES AU SCROLL
     ========================================================== */
    function initScrollReveal() {
        const revealElements = document.querySelectorAll(
            '.stat-card, .resume-card, .tip-card, .theme-card, .theme-cours-card'
        );

        if (revealElements.length === 0) return;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -40px 0px',
            threshold: 0.1
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0) scale(1)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        revealElements.forEach(function(el, index) {
            // Vérifier si l'élément n'a pas déjà une animation CSS intégrée
            const computedStyle = window.getComputedStyle(el);
            if (!computedStyle.animationName || computedStyle.animationName === 'none') {
                el.style.opacity = '0';
                el.style.transform = 'translateY(30px) scale(0.97)';
                el.style.transition = 'all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) ' + (index * 0.05) + 's';
            }
            observer.observe(el);
        });
    }

    /* ==========================================================
     SECTION 4 : ANIMATION DES BARRES DE PROGRESSION (QUIZ)
     ========================================================== */
    function initProgressBars() {
        const progressBars = document.querySelectorAll('.mini-progress-fill, .theme-progress .progress-fill');
        if (progressBars.length === 0) return;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.3
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    const targetWidth = entry.target.style.width;
                    
                    entry.target.style.width = '0%';
                    entry.target.style.transition = 'width 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                    
                    setTimeout(function() {
                        entry.target.style.width = targetWidth;
                    }, 100);

                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        progressBars.forEach(function(bar) {
            observer.observe(bar);
        });
    }

    /* ==========================================================
     SECTION 5 : ANIMATION DES BARRES DE PROGRESSION (COURS)
     ========================================================== */
    function initCoursProgressBars() {
        // Barre de progression globale des cours
        const coursGlobalFill = document.querySelector('.cours-global-progress-fill');
        if (coursGlobalFill) {
            const targetWidth = coursGlobalFill.style.width;
            
            const observerOptions = {
                root: null,
                rootMargin: '0px 0px -30px 0px',
                threshold: 0.3
            };

            const observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        coursGlobalFill.style.width = '0%';
                        coursGlobalFill.style.transition = 'width 1.4s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                        
                        setTimeout(function() {
                            coursGlobalFill.style.width = targetWidth;
                        }, 150);

                        observer.unobserve(entry.target);
                    }
                });
            }, observerOptions);

            observer.observe(coursGlobalFill);
        }

        // Barres de progression des thèmes Cours
        const coursThemeFills = document.querySelectorAll('.theme-cours-card .progress-fill');
        if (coursThemeFills.length > 0) {
            const observerOptions2 = {
                root: null,
                rootMargin: '0px 0px -30px 0px',
                threshold: 0.3
            };

            const observer2 = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        const targetWidth = entry.target.style.width;
                        
                        entry.target.style.width = '0%';
                        entry.target.style.transition = 'width 1s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                        
                        setTimeout(function() {
                            entry.target.style.width = targetWidth;
                        }, 80);

                        observer2.unobserve(entry.target);
                    }
                });
            }, observerOptions2);

            coursThemeFills.forEach(function(bar) {
                observer2.observe(bar);
            });
        }
    }

    /* ==========================================================
     SECTION 6 : ANIMATION DES CARTES "REPRENDRE"
     ========================================================== */
    function initResumeCardsAnimation() {
        const resumeCards = document.querySelectorAll('.resume-card');
        if (resumeCards.length === 0) return;

        resumeCards.forEach(function(resumeCard) {
            // Effet de survol avec suivi de la souris (desktop uniquement)
            if (!isTouchDevice()) {
                resumeCard.addEventListener('mousemove', function(e) {
                    const rect = this.getBoundingClientRect();
                    const x = e.clientX - rect.left;
                    const y = e.clientY - rect.top;
                    
                    const centerX = rect.width / 2;
                    const centerY = rect.height / 2;
                    
                    const moveX = (x - centerX) / centerX * 2;
                    const moveY = (y - centerY) / centerY * 2;
                    
                    this.style.transform = 'translateY(-2px) translateX(' + moveX + 'px) translateY(' + moveY + 'px)';
                    this.style.transition = 'transform 0.1s ease';
                });

                resumeCard.addEventListener('mouseleave', function() {
                    this.style.transform = 'translateY(0) translateX(0) translateY(0)';
                    this.style.transition = 'transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                });
            }

            // Animation de l'icône pulse au survol
            const resumeIcon = resumeCard.querySelector('.resume-icon');
            if (resumeIcon) {
                resumeCard.addEventListener('mouseenter', function() {
                    if (resumeIcon) {
                        resumeIcon.style.animationDuration = '1.2s';
                    }
                });

                resumeCard.addEventListener('mouseleave', function() {
                    if (resumeIcon) {
                        resumeIcon.style.animationDuration = '2.5s';
                    }
                });
            }
        });
    }

    /* ==========================================================
     SECTION 7 : GESTION DU TABLEAU RESPONSIVE
     ========================================================== */
    function initTableResponsive() {
        const tableContainer = document.querySelector('.table-container');
        if (!tableContainer) return;

        function checkTableOverflow() {
            const table = tableContainer.querySelector('table');
            if (!table) return;

            if (table.scrollWidth > tableContainer.clientWidth) {
                tableContainer.classList.add('table-scrollable');
                tableContainer.setAttribute('data-scroll-hint', 'Faites glisser pour voir plus →');
            } else {
                tableContainer.classList.remove('table-scrollable');
                tableContainer.removeAttribute('data-scroll-hint');
            }
        }

        checkTableOverflow();
        
        let resizeTimeout;
        window.addEventListener('resize', function() {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(checkTableOverflow, 250);
        });

        // Ajouter le style pour l'indicateur de scroll
        const style = document.createElement('style');
        style.textContent = `
            .table-scrollable {
                position: relative;
                overflow-x: auto;
                -webkit-overflow-scrolling: touch;
            }
            
            .table-scrollable::after {
                content: attr(data-scroll-hint);
                position: absolute;
                bottom: 8px;
                right: 12px;
                background: rgba(102, 126, 234, 0.9);
                color: white;
                padding: 5px 10px;
                border-radius: 12px;
                font-size: 11px;
                font-weight: 500;
                pointer-events: none;
                animation: hintPulse 2s ease-in-out infinite;
                z-index: 2;
            }
            
            @keyframes hintPulse {
                0%, 100% { opacity: 0.7; }
                50% { opacity: 1; }
            }
            
            .table-scrollable::-webkit-scrollbar {
                height: 6px;
            }
            
            .table-scrollable::-webkit-scrollbar-track {
                background: #f1f1f1;
                border-radius: 10px;
            }
            
            .table-scrollable::-webkit-scrollbar-thumb {
                background: #667eea;
                border-radius: 10px;
            }
        `;
        document.head.appendChild(style);
    }

    /* ==========================================================
     SECTION 8 : EFFETS DE SURVOL DES CARTES STATISTIQUES
     ========================================================== */
    function initStatCardHover() {
        const statCards = document.querySelectorAll('.stat-card');
        if (statCards.length === 0) return;

        statCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                const iconWrapper = this.querySelector('.stat-icon-wrapper');
                if (iconWrapper) {
                    iconWrapper.style.transform = 'scale(1.1) rotate(-5deg)';
                    iconWrapper.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            card.addEventListener('mouseleave', function() {
                const iconWrapper = this.querySelector('.stat-icon-wrapper');
                if (iconWrapper) {
                    iconWrapper.style.transform = 'scale(1) rotate(0deg)';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 9 : EFFETS DE SURVOL DES CARTES THÈMES QUIZ
     ========================================================== */
    function initThemeCardHover() {
        const themeCards = document.querySelectorAll('.theme-card:not(.theme-cours-card)');
        if (themeCards.length === 0) return;

        themeCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                const icon = this.querySelector('.theme-card-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.08) rotate(-3deg)';
                    icon.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
                
                const btnVoir = this.querySelector('.btn-voir-parcours');
                if (btnVoir) {
                    btnVoir.style.gap = '10px';
                    btnVoir.style.transition = 'all 0.3s ease';
                }
            });

            card.addEventListener('mouseleave', function() {
                const icon = this.querySelector('.theme-card-icon');
                if (icon) {
                    icon.style.transform = 'scale(1) rotate(0deg)';
                }
                
                const btnVoir = this.querySelector('.btn-voir-parcours');
                if (btnVoir) {
                    btnVoir.style.gap = '5px';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 10 : EFFETS DE SURVOL DES CARTES THÈMES COURS
     ========================================================== */
    function initCoursCardHover() {
        const coursCards = document.querySelectorAll('.theme-cours-card');
        if (coursCards.length === 0) return;

        coursCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                const icon = this.querySelector('.theme-card-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.08) rotate(-3deg)';
                    icon.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }

                const badge = this.querySelector('.theme-badge-termine');
                if (badge) {
                    badge.style.transform = 'scale(1.1) rotate(10deg)';
                    badge.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }

                const btnVoir = this.querySelector('.btn-voir-parcours');
                if (btnVoir) {
                    btnVoir.style.gap = '10px';
                    btnVoir.style.color = '#f39c12';
                    btnVoir.style.transition = 'all 0.3s ease';
                }
            });

            card.addEventListener('mouseleave', function() {
                const icon = this.querySelector('.theme-card-icon');
                if (icon) {
                    icon.style.transform = 'scale(1) rotate(0deg)';
                }

                const badge = this.querySelector('.theme-badge-termine');
                if (badge) {
                    badge.style.transform = 'scale(1) rotate(0deg)';
                }

                const btnVoir = this.querySelector('.btn-voir-parcours');
                if (btnVoir) {
                    btnVoir.style.gap = '5px';
                    btnVoir.style.color = '#667eea';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 11 : ACTIONS RAPIDES (RACCOURCIS CLAVIER)
     ========================================================== */
    function initQuickActions() {
        document.addEventListener('keydown', function(e) {
            if (e.ctrlKey || e.metaKey) {
                switch (e.key.toLowerCase()) {
                    case 'q':
                        // Aller à la section Quiz
                        e.preventDefault();
                        const quizSection = document.getElementById('quiz-section');
                        if (quizSection) {
                            quizSection.scrollIntoView({ behavior: 'smooth' });
                        }
                        break;
                    case 'c':
                        // Aller à la section Cours
                        e.preventDefault();
                        const coursSection = document.getElementById('cours-section');
                        if (coursSection) {
                            coursSection.scrollIntoView({ behavior: 'smooth' });
                        }
                        break;
                    case 'p':
                        // Reprendre le parcours (première carte resume)
                        e.preventDefault();
                        const resumeBtn = document.querySelector('.resume-btn');
                        if (resumeBtn) {
                            const resumeLink = resumeBtn.closest('.resume-card');
                            if (resumeLink) {
                                window.location.href = resumeLink.getAttribute('href');
                            }
                        }
                        break;
                    case 'h':
                        // Aller à l'historique
                        e.preventDefault();
                        const historiqueLink = document.querySelector('.btn-voir-tout');
                        if (historiqueLink) {
                            window.location.href = historiqueLink.getAttribute('href');
                        }
                        break;
                    case 't':
                        // Scroll vers les thèmes Quiz
                        e.preventDefault();
                        const themesSection = document.querySelector('#quiz-section');
                        if (themesSection) {
                            themesSection.scrollIntoView({ behavior: 'smooth' });
                        }
                        break;
                }
            }
        });
    }

    /* ==========================================================
     SECTION 12 : MISE À JOUR DE LA DATE
     ========================================================== */
    function initDateUpdate() {
        const dateElement = document.querySelector('.welcome-date');
        if (!dateElement) return;

        function updateDateIfMidnight() {
            const now = new Date();
            const hours = now.getHours();
            const minutes = now.getMinutes();
            
            if (hours === 0 && minutes < 5) {
                console.log('Minuit passé - la date affichée peut être mise à jour au prochain rechargement.');
            }
        }

        setInterval(updateDateIfMidnight, 60000);
    }

    /* ==========================================================
     SECTION 13 : ANIMATION DES BADGES DE SCORE
     ========================================================== */
    function initBadgeAnimations() {
        const badges = document.querySelectorAll('.badge-score');
        if (badges.length === 0) return;

        badges.forEach(function(badge) {
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
     SECTION 14 : TOOLTIPS SUR LES BOUTONS D'ACTION
     ========================================================== */
    function initActionTooltips() {
        const rejouerBtns = document.querySelectorAll('.btn-rejouer');
        if (rejouerBtns.length === 0) return;

        rejouerBtns.forEach(function(btn) {
            btn.setAttribute('data-tooltip', 'Rejouer ce quiz');
            
            btn.addEventListener('mouseenter', function() {
                this.style.transform = 'rotate(-15deg) scale(1.1)';
            });

            btn.addEventListener('mouseleave', function() {
                this.style.transform = 'rotate(0deg) scale(1)';
            });
        });
    }

    /* ==========================================================
     SECTION 15 : EFFET DE VAGUE SUR LES CARTES CONSEILS
     ========================================================== */
    function initTipCardEffects() {
        const tipCards = document.querySelectorAll('.tip-card');
        if (tipCards.length === 0) return;

        tipCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                const icon = this.querySelector('.tip-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.15) rotate(5deg)';
                    icon.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            card.addEventListener('mouseleave', function() {
                const icon = this.querySelector('.tip-icon');
                if (icon) {
                    icon.style.transform = 'scale(1) rotate(0deg)';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 16 : GESTION DES ÉTATS VIDES
     ========================================================== */
    function initEmptyStateActions() {
        const emptyStateBtns = document.querySelectorAll('.empty-state .btn-commencer');
        if (emptyStateBtns.length === 0) return;

        emptyStateBtns.forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                const themesSection = document.querySelector('.themes-section');
                if (themesSection) {
                    e.preventDefault();
                    themesSection.scrollIntoView({ behavior: 'smooth' });
                }
            });
        });
    }

    /* ==========================================================
     SECTION 17 : ANIMATION DE L'AVATAR
     ========================================================== */
    function initAvatarAnimation() {
        const avatar = document.querySelector('.welcome-avatar');
        if (!avatar) return;

        avatar.addEventListener('click', function() {
            this.style.transform = 'scale(0.9)';
            this.style.transition = 'transform 0.15s ease';
            
            setTimeout(() => {
                this.style.transform = 'scale(1.08)';
            }, 150);
            
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 300);
        });

        avatar.style.cursor = 'pointer';
    }

    /* ==========================================================
     SECTION 18 : DÉTECTION APPAREIL TACTILE
     ========================================================== */
    function isTouchDevice() {
        return ('ontouchstart' in window) || 
               (navigator.maxTouchPoints > 0) || 
               (navigator.msMaxTouchPoints > 0);
    }

    /* ==========================================================
     SECTION 19 : ANIMATION DES BADGES DE THÈMES
     ========================================================== */
    function initThemeBadgeAnimations() {
        const themeBadges = document.querySelectorAll('.badge-quiz, .badge-cours, .badge-neutral');
        if (themeBadges.length === 0) return;

        themeBadges.forEach(function(badge) {
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.05)';
                this.style.transition = 'all 0.2s ease';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });
        });
    }

    setTimeout(initThemeBadgeAnimations, 1500);

    /* ==========================================================
     SECTION 20 : PERFORMANCE - NETTOYAGE MÉMOIRE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            document.body.classList.add('page-hidden');
        } else {
            document.body.classList.remove('page-hidden');
        }
    });

    const performanceStyle = document.createElement('style');
    performanceStyle.textContent = `
        .page-hidden .stat-card,
        .page-hidden .tip-card,
        .page-hidden .resume-card,
        .page-hidden .theme-card,
        .page-hidden .theme-cours-card {
            animation: none !important;
        }
        
        .page-hidden .resume-icon {
            animation: none !important;
        }
        
        .page-hidden .mini-progress-fill,
        .page-hidden .progress-fill,
        .page-hidden .cours-global-progress-fill {
            transition: none !important;
        }
    `;
    document.head.appendChild(performanceStyle);

})();