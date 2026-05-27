/**
 * ============================================================
 * FICHIER : views/assets/js/cours-themes.js
 * RÔLE    : Scripts de la page d'accueil des cours
 *           - Animation des compteurs de statistiques
 *           - Animation de la barre de progression globale
 *           - Animation des cartes thèmes (révélation)
 *           - Animation des barres de progression des thèmes
 *           - Effets de survol avancés
 *           - Compteurs animés
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initStatCounters();
        initGlobalProgressBar();
        initThemeProgressBars();
        initThemeCardHover();
        initTropheeAnimation();
    });

    /* ==========================================================
     SECTION 2 : ANIMATION DES COMPTEURS DE STATISTIQUES
     ========================================================== */
    function initStatCounters() {
        var statNumbers = document.querySelectorAll('.stat-mini strong');
        if (statNumbers.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.3
        };

        var animated = false;

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting && !animated) {
                    animated = true;
                    animateCounters(statNumbers);
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        var statsBanner = document.querySelector('.stats-banner');
        if (statsBanner) {
            observer.observe(statsBanner);
        } else {
            setTimeout(function() {
                animateCounters(statNumbers);
            }, 500);
        }
    }

    function animateCounters(statNumbers) {
        statNumbers.forEach(function(stat, index) {
            var text = stat.textContent.trim();
            
            // Extraire la valeur numérique (gère "85%" → 85, "12" → 12)
            var match = text.match(/^([\d]+(\.[\d]+)?)/);
            if (!match) return;

            var targetValue = parseFloat(match[1]);
            var suffix = text.replace(match[1], '');
            var isDecimal = match[2] !== undefined;
            var decimals = isDecimal ? match[2].length - 1 : 0;

            var duration = 1500 + (index * 200);
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
            }, index * 150);
        });
    }

    /* ==========================================================
     SECTION 3 : ANIMATION BARRE DE PROGRESSION GLOBALE
     ========================================================== */
    function initGlobalProgressBar() {
        var progressFill = document.querySelector('.global-progress-fill');
        if (!progressFill) return;

        var targetWidth = progressFill.style.width;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.3
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    // Réinitialiser pour l'animation
                    progressFill.style.width = '0%';
                    progressFill.style.transition = 'width 1.5s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                    
                    setTimeout(function() {
                        progressFill.style.width = targetWidth;
                        
                        // Effet de brillance à la fin
                        setTimeout(function() {
                            progressFill.style.boxShadow = '0 0 15px rgba(102, 126, 234, 0.3)';
                            setTimeout(function() {
                                progressFill.style.boxShadow = 'none';
                            }, 700);
                        }, 1500);
                    }, 300);

                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        observer.observe(progressFill);
    }

    /* ==========================================================
     SECTION 4 : ANIMATION BARRES DE PROGRESSION DES THÈMES
     ========================================================== */
    function initThemeProgressBars() {
        var progressFills = document.querySelectorAll('.theme-progress-fill');
        if (progressFills.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.1
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    var fill = entry.target;
                    var targetWidth = fill.style.width;
                    
                    fill.style.width = '0%';
                    fill.style.transition = 'width 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                    
                    setTimeout(function() {
                        fill.style.width = targetWidth;
                    }, 150);

                    observer.unobserve(fill);
                }
            });
        }, observerOptions);

        progressFills.forEach(function(fill) {
            observer.observe(fill);
        });
    }

    /* ==========================================================
     SECTION 5 : EFFETS DE SURVOL AVANCÉS DES CARTES THÈMES
     ========================================================== */
    function initThemeCardHover() {
        var cards = document.querySelectorAll('.theme-card');
        if (cards.length === 0) return;

        // Effet 3D au survol (desktop uniquement)
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
                    
                    this.style.transform = 'translateY(-6px) perspective(600px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
                    this.style.transition = 'transform 0.1s ease';
                });

                card.addEventListener('mouseleave', function() {
                    this.style.transform = 'translateY(0) perspective(600px) rotateX(0) rotateY(0)';
                    this.style.transition = 'transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                });
            });
        }

        // Animation de l'icône et de la flèche
        cards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.theme-card-icon');
                var arrow = this.querySelector('.theme-footer-arrow');
                
                if (icon) {
                    icon.style.transform = 'scale(1.1) rotate(-6deg)';
                    icon.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
                if (arrow) {
                    arrow.style.transform = 'translateX(3px)';
                    arrow.style.transition = 'all 0.3s ease';
                }
            });

            card.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.theme-card-icon');
                var arrow = this.querySelector('.theme-footer-arrow');
                
                if (icon) icon.style.transform = 'scale(1) rotate(0deg)';
                if (arrow) arrow.style.transform = 'translateX(0)';
            });
        });
    }

    /* ==========================================================
     SECTION 6 : ANIMATION DU BADGE TROPHÉE
     ========================================================== */
    function initTropheeAnimation() {
        var trophees = document.querySelectorAll('.theme-badge-completed');
        if (trophees.length === 0) return;

        trophees.forEach(function(trophee) {
            // Effet au survol
            trophee.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.2) rotate(10deg)';
                this.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                this.style.boxShadow = '0 6px 25px rgba(255, 215, 0, 0.6)';
            });

            trophee.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1) rotate(0deg)';
                this.style.boxShadow = '0 4px 15px rgba(255, 215, 0, 0.4)';
            });

            // Effet clic rebond
            trophee.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                this.style.transform = 'scale(0.8)';
                this.style.transition = 'transform 0.2s ease';
                
                setTimeout(function() {
                    trophee.style.transform = 'scale(1.3) rotate(-5deg)';
                }, 150);
                
                setTimeout(function() {
                    trophee.style.transform = 'scale(1) rotate(0deg)';
                }, 400);
            });

            trophee.style.cursor = 'pointer';
            trophee.setAttribute('title', 'Thème terminé ! 🎉');
        });
    }

    /* ==========================================================
     SECTION 7 : ANIMATION DES CARTES INFO
     ========================================================== */
    function initInfoCardsAnimation() {
        var infoCards = document.querySelectorAll('.info-card');
        if (infoCards.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -20px 0px',
            threshold: 0.2
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    var card = entry.target;
                    
                    setTimeout(function() {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0)';
                    }, index * 100);

                    observer.unobserve(card);
                }
            });
        }, observerOptions);

        infoCards.forEach(function(card) {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s ease';
            observer.observe(card);
        });
    }

    setTimeout(initInfoCardsAnimation, 600);

    /* ==========================================================
     SECTION 8 : COMPTEUR DE THÈMES (LOG)
     ========================================================== */
    function initThemeCounter() {
        var totalThemes = document.querySelectorAll('.theme-card').length;
        var completedThemes = document.querySelectorAll('.theme-badge-completed').length;
        
        if (totalThemes > 0) {
            console.log(
                '📚 ' + totalThemes + ' thème' + (totalThemes > 1 ? 's' : '') + 
                ' disponible' + (totalThemes > 1 ? 's' : '') + 
                (completedThemes > 0 ? ' | 🏆 ' + completedThemes + ' terminé' + (completedThemes > 1 ? 's' : '') : '')
            );
        }
    }

    setTimeout(initThemeCounter, 1000);

    /* ==========================================================
     SECTION 9 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var trophees = document.querySelectorAll('.theme-badge-completed');
        
        if (document.hidden) {
            trophees.forEach(function(trophee) {
                trophee.style.animationPlayState = 'paused';
            });
        } else {
            trophees.forEach(function(trophee) {
                trophee.style.animationPlayState = 'running';
            });
        }
    });

})();