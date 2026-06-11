// ============================================================
// FICHIER : views/assets/js/admin-resultats-joueur.js
// RÔLE    : Scripts de la page détail joueur
//           - Animation des barres de progression
//           - Animation des compteurs statistiques
//           - Animation des lignes du tableau
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        animationDelay: 40,
        compteurDuree: 1200
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initProgressionBars();
        initCompteursStatistiques();
        initRowAnimations();
        initBadgeAnimations();
        console.log('Admin Resultats Joueur - Page initialisee');
    });

    /* ==========================================================
     SECTION 3 : ANIMATION DES BARRES DE PROGRESSION
     ========================================================== */
    function initProgressionBars() {
        var barres = document.querySelectorAll('.progression-bar-inner');

        if (!('IntersectionObserver' in window)) {
            barres.forEach(function(barre) {
                barre.style.width = barre.style.width || '0%';
            });
            return;
        }

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    var largeurFinale = entry.target.style.width || '0%';
                    entry.target.style.width = '0%';
                    entry.target.style.transition = 'width 1s cubic-bezier(0.25, 0.46, 0.45, 0.94)';

                    setTimeout(function() {
                        entry.target.style.width = largeurFinale;
                    }, 200);

                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.3 });

        barres.forEach(function(barre) {
            observer.observe(barre);
        });
    }

    /* ==========================================================
     SECTION 4 : ANIMATION DES COMPTEURS STATISTIQUES
     ========================================================== */
    function initCompteursStatistiques() {
        var compteurs = document.querySelectorAll('.admin-stat-nombre');

        if (!('IntersectionObserver' in window)) {
            compteurs.forEach(function(c) { c.style.opacity = '1'; });
            return;
        }

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    animerCompteur(entry.target);
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.3 });

        compteurs.forEach(function(compteur) {
            compteur.style.opacity = '0';
            compteur.style.transform = 'scale(0.8)';
            compteur.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            observer.observe(compteur);
        });
    }

    function animerCompteur(element) {
        var texteComplet = element.textContent.trim();
        var match = texteComplet.match(/([\d.]+)/);
        if (!match) {
            element.style.opacity = '1';
            element.style.transform = 'scale(1)';
            return;
        }

        var valeurFinale = parseFloat(match[1]);
        var suffixe = texteComplet.replace(match[1], '');
        var duree = config.compteurDuree;
        var debutTimestamp = null;

        element.style.opacity = '1';
        element.style.transform = 'scale(1)';

        function step(timestamp) {
            if (!debutTimestamp) debutTimestamp = timestamp;
            var progression = Math.min((timestamp - debutTimestamp) / duree, 1);
            var eased = 1 - Math.pow(1 - progression, 3);
            var valeurActuelle;

            if (texteComplet.includes('%')) {
                valeurActuelle = (valeurFinale * eased).toFixed(1);
            } else if (texteComplet.includes('min')) {
                // Ne pas animer les temps
                element.textContent = texteComplet;
                return;
            } else {
                valeurActuelle = Math.floor(valeurFinale * eased);
            }

            element.textContent = valeurActuelle + suffixe;

            if (progression < 1) {
                requestAnimationFrame(step);
            } else {
                element.textContent = texteComplet;
            }
        }

        requestAnimationFrame(step);
    }

    /* ==========================================================
     SECTION 5 : ANIMATION DES LIGNES DU TABLEAU
     ========================================================== */
    function initRowAnimations() {
        var rows = document.querySelectorAll('.admin-table tbody tr');

        rows.forEach(function(row, index) {
            row.style.opacity = '0';
            row.style.transform = 'translateX(-10px)';
            row.style.transition = 'all 0.4s ease';

            setTimeout(function() {
                row.style.opacity = '1';
                row.style.transform = 'translateX(0)';
            }, 100 + (index * config.animationDelay));
        });
    }

    /* ==========================================================
     SECTION 6 : ANIMATION DES BADGES
     ========================================================== */
    function initBadgeAnimations() {
        var badges = document.querySelectorAll('.badge-premium-actif, .badge-premium-inactif, .badge-statut, .badge-score');

        badges.forEach(function(badge) {
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.05)';
                this.style.transition = 'transform 0.2s ease';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });
        });
    }

    /* ==========================================================
     SECTION 7 : ANIMATION DE L'AVATAR
     ========================================================== */
    (function initAvatarAnimation() {
        var avatar = document.querySelector('.joueur-profil-avatar');
        if (!avatar) return;

        avatar.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.1) rotate(5deg)';
            this.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
        });

        avatar.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1) rotate(0deg)';
        });
    })();

    /* ==========================================================
     SECTION 8 : HIGHLIGHT DU MEILLEUR SCORE
     ========================================================== */
    (function initBestScoreHighlight() {
        var bestScoreRow = document.querySelector('.admin-table tbody tr:first-child');
        if (!bestScoreRow) return;

        var scoreBadge = bestScoreRow.querySelector('.badge-score');
        if (scoreBadge && scoreBadge.classList.contains('badge-excellent')) {
            bestScoreRow.style.background = 'linear-gradient(135deg, #f0fff4 0%, #ffffff 50%)';
            bestScoreRow.style.borderLeft = '3px solid #27ae60';
        }
    })();

})();