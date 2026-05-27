/**
 * ============================================================
 * FICHIER : views/assets/js/historique.js
 * RÔLE    : Scripts de la page historique AmijayQuiz
 *           - Animation des cartes statistiques au scroll
 *           - Compteurs animés pour les statistiques
 *           - Gestion des filtres (auto-soumission)
 *           - Révélation des lignes du tableau
 *           - Effets de survol avancés
 *           - Raccourcis clavier
 *           - Gestion de la pagination
 *           - Tooltips sur les boutons
 *           - Performance (pause animations)
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initStatCounters();
        initTableRowReveal();
        initFilterAutoSubmit();
        initHoverEffects();
        initKeyboardShortcuts();
        initPaginationSmoothScroll();
        initRejouerTooltips();
    });

    /* ==========================================================
     SECTION 2 : COMPTEURS ANIMÉS POUR LES STATISTIQUES
     ========================================================== */
    function initStatCounters() {
        var statNumbers = document.querySelectorAll('.stat-number-h, .stat-number-adv');
        if (statNumbers.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -40px 0px',
            threshold: 0.2
        };

        var animated = false;

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting && !animated) {
                    animated = true;
                    animateAllStats(statNumbers);
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        var statsSection = document.querySelector('.stats-section');
        if (statsSection) {
            observer.observe(statsSection);
        } else {
            // Fallback : animer après un délai
            setTimeout(function() {
                animateAllStats(statNumbers);
            }, 500);
        }
    }

    function animateAllStats(statNumbers) {
        statNumbers.forEach(function(stat, index) {
            var text = stat.textContent.trim();
            
            // Extraire la valeur numérique (gère "85%", "12", "120 min")
            var match = text.match(/^([\d]+(\.[\d]+)?)/);
            if (!match) return;

            var targetValue = parseFloat(match[1]);
            var suffix = text.replace(match[1], '');
            var isDecimal = match[2] !== undefined;
            var decimals = isDecimal ? match[2].length - 1 : 0;

            var duration = 1200 + (index * 150);
            var startTime = performance.now();

            function update(currentTime) {
                var elapsed = currentTime - startTime;
                var progress = Math.min(elapsed / duration, 1);
                var eased = 1 - Math.pow(1 - progress, 3);

                var currentValue = startValue + (targetValue - startValue) * eased;
                
                if (isDecimal) {
                    stat.textContent = currentValue.toFixed(decimals) + suffix;
                } else {
                    stat.textContent = Math.floor(currentValue) + suffix;
                }

                if (progress < 1) {
                    requestAnimationFrame(update);
                } else {
                    stat.textContent = targetValue.toFixed(decimals) + suffix;
                    
                    // Effet de validation
                    stat.style.transform = 'scale(1.06)';
                    stat.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                    setTimeout(function() {
                        stat.style.transform = 'scale(1)';
                    }, 300);
                }
            }

            var startValue = 0;
            setTimeout(function() {
                requestAnimationFrame(update);
            }, index * 100);
        });
    }

    /* ==========================================================
     SECTION 3 : RÉVÉLATION DES LIGNES DU TABLEAU
     ========================================================== */
    function initTableRowReveal() {
        var rows = document.querySelectorAll('.table-h tbody tr');
        if (rows.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.05
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var row = entry.target;
                    var delay = index * 40;
                    
                    setTimeout(function() {
                        row.style.opacity = '1';
                        row.style.transform = 'translateX(0)';
                    }, delay);
                    
                    observer.unobserve(row);
                }
            });
        }, observerOptions);

        rows.forEach(function(row, index) {
            row.style.opacity = '0';
            row.style.transform = 'translateX(-15px)';
            row.style.transition = 'all 0.4s ease';
            observer.observe(row);
        });
    }

    /* ==========================================================
     SECTION 4 : AUTO-SOUMISSION DES FILTRES
     ========================================================== */
    function initFilterAutoSubmit() {
        var filterSelects = document.querySelectorAll('.filtre-select');
        if (filterSelects.length === 0) return;

        filterSelects.forEach(function(select) {
            select.addEventListener('change', function() {
                // Ajouter une classe pour indiquer le chargement
                var filtresBar = document.querySelector('.filtres-bar');
                if (filtresBar) {
                    filtresBar.style.opacity = '0.7';
                    filtresBar.style.transition = 'opacity 0.3s ease';
                    filtresBar.style.pointerEvents = 'none';
                }

                // Soumettre le formulaire après un court délai
                setTimeout(function() {
                    var form = select.closest('form');
                    if (form) {
                        form.submit();
                    }
                }, 300);
            });

            // Mettre en évidence le select au focus
            select.addEventListener('focus', function() {
                this.closest('.filtre-group').style.transform = 'scale(1.02)';
                this.closest('.filtre-group').style.transition = 'transform 0.3s ease';
            });

            select.addEventListener('blur', function() {
                this.closest('.filtre-group').style.transform = 'scale(1)';
            });
        });
    }

    /* ==========================================================
     SECTION 5 : EFFETS DE SURVOL AVANCÉS
     ========================================================== */
    function initHoverEffects() {
        // Cartes statistiques
        var statCards = document.querySelectorAll('.stat-card-h, .stat-card-adv');
        statCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.stat-icon-h, .stat-icon-adv');
                if (icon) {
                    icon.style.transform = 'scale(1.12) rotate(-5deg)';
                    icon.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            card.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.stat-icon-h, .stat-icon-adv');
                if (icon) {
                    icon.style.transform = 'scale(1) rotate(0deg)';
                }
            });
        });

        // Boutons rejouer
        var rejouerBtns = document.querySelectorAll('.btn-rejouer');
        rejouerBtns.forEach(function(btn) {
            btn.addEventListener('mouseenter', function() {
                this.style.transform = 'rotate(-30deg) scale(1.15)';
            });

            btn.addEventListener('mouseleave', function() {
                this.style.transform = 'rotate(0deg) scale(1)';
            });
        });

        // Lignes du tableau
        var rows = document.querySelectorAll('.table-h tbody tr');
        rows.forEach(function(row) {
            row.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.quiz-cell-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.2)';
                    icon.style.transition = 'transform 0.3s ease';
                }
            });

            row.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.quiz-cell-icon');
                if (icon) {
                    icon.style.transform = 'scale(1)';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 6 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ne pas activer dans les champs de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') {
                return;
            }

            switch (e.key) {
                case 'f':
                case 'F':
                    // Focus sur le premier filtre
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var firstFilter = document.querySelector('.filtre-select');
                        if (firstFilter) firstFilter.focus();
                    }
                    break;

                case 'r':
                case 'R':
                    // Réinitialiser les filtres
                    if (e.ctrlKey && e.shiftKey) {
                        e.preventDefault();
                        var resetBtn = document.querySelector('.btn-reset-filtres');
                        if (resetBtn) {
                            window.location.href = resetBtn.getAttribute('href');
                        }
                    }
                    break;

                case 'Escape':
                    // Retour au dashboard
                    var backBtn = document.querySelector('.btn-retour, .header-back-btn');
                    if (backBtn) {
                        window.location.href = backBtn.getAttribute('href');
                    }
                    break;

                case 'ArrowLeft':
                    // Page précédente
                    if (e.altKey) {
                        e.preventDefault();
                        var prevLink = document.querySelector('.pagination-bar .page-link:not(.disabled) .fa-angle-left');
                        if (prevLink) {
                            var link = prevLink.closest('.page-link');
                            if (link) window.location.href = link.getAttribute('href');
                        }
                    }
                    break;

                case 'ArrowRight':
                    // Page suivante
                    if (e.altKey) {
                        e.preventDefault();
                        var nextLink = document.querySelector('.pagination-bar .page-link:not(.disabled) .fa-angle-right');
                        if (nextLink) {
                            var link = nextLink.closest('.page-link');
                            if (link) window.location.href = link.getAttribute('href');
                        }
                    }
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 7 : SCROLL DOUX POUR LA PAGINATION
     ========================================================== */
    function initPaginationSmoothScroll() {
        var paginationLinks = document.querySelectorAll('.pagination-bar .page-link:not(.disabled)');
        if (paginationLinks.length === 0) return;

        paginationLinks.forEach(function(link) {
            link.addEventListener('click', function(e) {
                // Ajouter un effet de chargement
                var tableauSection = document.querySelector('.tableau-section');
                if (tableauSection) {
                    tableauSection.style.opacity = '0.6';
                    tableauSection.style.transition = 'opacity 0.3s ease';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 8 : TOOLTIPS SUR LES BOUTONS REJOUER
     ========================================================== */
    function initRejouerTooltips() {
        var rejouerBtns = document.querySelectorAll('.btn-rejouer');
        if (rejouerBtns.length === 0) return;

        rejouerBtns.forEach(function(btn) {
            var quizTitle = btn.getAttribute('data-tooltip') || 'Rejouer ce quiz';
            btn.setAttribute('title', quizTitle);
        });
    }

    /* ==========================================================
     SECTION 9 : ANIMATION DES BADGES AU SURVOL
     ========================================================== */
    function initBadgeHover() {
        var badges = document.querySelectorAll('.badge-score-h, .badge-reussite-h');
        if (badges.length === 0) return;

        badges.forEach(function(badge) {
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.06)';
                this.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });
        });
    }

    setTimeout(initBadgeHover, 800);

    /* ==========================================================
     SECTION 10 : GESTION DE L'ÉTAT VIDE
     ========================================================== */
    function initEmptyStateAction() {
        var emptyStateBtn = document.querySelector('.empty-state .btn-commencer');
        if (!emptyStateBtn) return;

        emptyStateBtn.addEventListener('click', function(e) {
            // Effet de transition
            document.body.style.opacity = '0.8';
            document.body.style.transition = 'opacity 0.3s ease';
        });
    }

    initEmptyStateAction();

    /* ==========================================================
     SECTION 11 : ANIMATION DE L'EN-TÊTE
     ========================================================== */
    function initHeaderAnimation() {
        var headerIcon = document.querySelector('.header-icon');
        if (!headerIcon) return;

        headerIcon.addEventListener('click', function() {
            this.style.transform = 'scale(0.85) rotate(-10deg)';
            this.style.transition = 'transform 0.3s ease';
            
            setTimeout(function() {
                headerIcon.style.transform = 'scale(1.1) rotate(5deg)';
            }, 150);
            
            setTimeout(function() {
                headerIcon.style.transform = 'scale(1) rotate(0deg)';
            }, 400);
        });

        headerIcon.style.cursor = 'pointer';
        headerIcon.setAttribute('title', 'Cliquez pour animer');
    }

    initHeaderAnimation();

    /* ==========================================================
     SECTION 12 : DÉFILEMENT FLUIDE DU TABLEAU
     ========================================================== */
    function initTableScrollHint() {
        var tableResponsive = document.querySelector('.table-responsive');
        if (!tableResponsive) return;

        // Vérifier si le tableau nécessite un scroll horizontal
        function checkScroll() {
            var table = tableResponsive.querySelector('table');
            if (!table) return;

            if (table.scrollWidth > tableResponsive.clientWidth) {
                tableResponsive.setAttribute('data-scrollable', 'true');
            } else {
                tableResponsive.removeAttribute('data-scrollable');
            }
        }

        checkScroll();

        var resizeTimeout;
        window.addEventListener('resize', function() {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(checkScroll, 200);
        });
    }

    initTableScrollHint();

    /* ==========================================================
     SECTION 13 : STYLE SCROLL HINT (INJECTÉ)
     ========================================================== */
    var scrollHintStyle = document.createElement('style');
    scrollHintStyle.textContent = `
        .table-responsive[data-scrollable="true"] {
            position: relative;
        }
        
        .table-responsive[data-scrollable="true"]::after {
            content: 'Faites glisser →';
            position: absolute;
            bottom: 8px;
            right: 12px;
            background: rgba(102, 126, 234, 0.9);
            color: white;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 10px;
            font-weight: 500;
            pointer-events: none;
            animation: hintPulse 2s ease-in-out infinite;
            z-index: 2;
        }
        
        @keyframes hintPulse {
            0%, 100% { opacity: 0.7; }
            50% { opacity: 1; }
        }
    `;
    document.head.appendChild(scrollHintStyle);

    /* ==========================================================
     SECTION 14 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            // Réduire les animations quand la page est cachée
            document.body.classList.add('page-hidden');
        } else {
            document.body.classList.remove('page-hidden');
        }
    });

})();