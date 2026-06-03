// ============================================================
// FICHIER : views/assets/js/admin-dashboard.js
// RÔLE    : Script du tableau de bord administrateur
//           - Animation des compteurs statistiques
//           - Actualisation de la date
//           - Animation des cartes au survol
//           - Rafraîchissement automatique
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        compteurDuree: 1200,
        autoRefreshInterval: 120000, // 2 minutes
        animationDelay: 50
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initCompteursStatistiques();
        initActualisationDate();
        initCartesHover();
        initAutoRefresh();
        console.log('Admin Dashboard - Initialise');
    });

    /* ==========================================================
     SECTION 3 : ANIMATION DES COMPTEURS
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

        compteurs.forEach(function(compteur, index) {
            compteur.style.opacity = '0';
            compteur.style.transform = 'scale(0.8)';
            compteur.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            observer.observe(compteur);
        });
    }

    function animerCompteur(element) {
        var texteComplet = element.textContent.trim();
        var match = texteComplet.match(/(\d+)/);
        if (!match) {
            element.style.opacity = '1';
            element.style.transform = 'scale(1)';
            return;
        }

        var valeurFinale = parseInt(match[1], 10);
        var suffixe = texteComplet.replace(match[1], '');
        var duree = config.compteurDuree;
        var debutTimestamp = null;

        element.style.opacity = '1';
        element.style.transform = 'scale(1)';

        function step(timestamp) {
            if (!debutTimestamp) debutTimestamp = timestamp;
            var progression = Math.min((timestamp - debutTimestamp) / duree, 1);
            var eased = 1 - Math.pow(1 - progression, 3);
            var valeurActuelle = Math.floor(valeurFinale * eased);

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
     SECTION 4 : ACTUALISATION DE LA DATE
     ========================================================== */
    function initActualisationDate() {
        var dateElement = document.querySelector('.admin-welcome-date');
        if (!dateElement) return;

        setInterval(function() {
            var maintenant = new Date();
            var options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
            var dateFormatee = maintenant.toLocaleDateString('fr-FR', options);

            var icon = dateElement.querySelector('i');
            if (icon) {
                dateElement.innerHTML = '';
                dateElement.appendChild(icon.cloneNode(true));
                dateElement.appendChild(document.createTextNode(' ' + dateFormatee));
            }
        }, 60000);
    }

    /* ==========================================================
     SECTION 5 : EFFET HOVER SUR LES CARTES
     ========================================================== */
    function initCartesHover() {
        var cartes = document.querySelectorAll('.admin-stat-card, .admin-action-card');

        cartes.forEach(function(carte) {
            carte.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.admin-stat-icon, .admin-action-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.1) rotate(-5deg)';
                    icon.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            carte.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.admin-stat-icon, .admin-action-icon');
                if (icon) {
                    icon.style.transform = 'scale(1) rotate(0deg)';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 6 : RAFRAÎCHISSEMENT AUTOMATIQUE
     ========================================================== */
    function initAutoRefresh() {
        setInterval(function() {
            var compteurs = document.querySelectorAll('.admin-stat-nombre');
            compteurs.forEach(function(compteur) {
                compteur.style.transform = 'scale(1.05)';
                compteur.style.transition = 'transform 0.2s ease';
                setTimeout(function() {
                    compteur.style.transform = 'scale(1)';
                }, 200);
            });
        }, config.autoRefreshInterval);
    }

    /* ==========================================================
     SECTION 7 : ANIMATION DES ALERTES
     ========================================================== */
    (function initAlertesAnimation() {
        var alertes = document.querySelectorAll('.admin-alerte-paiement');

        alertes.forEach(function(alerte, index) {
            alerte.style.opacity = '0';
            alerte.style.transform = 'translateY(15px)';
            alerte.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

            if ('IntersectionObserver' in window) {
                var observer = new IntersectionObserver(function(entries) {
                    if (entries[0].isIntersecting) {
                        setTimeout(function() {
                            alerte.style.opacity = '1';
                            alerte.style.transform = 'translateY(0)';
                        }, 200 + (index * 100));
                        observer.disconnect();
                    }
                }, { threshold: 0.2 });

                observer.observe(alerte);
            } else {
                setTimeout(function() {
                    alerte.style.opacity = '1';
                    alerte.style.transform = 'translateY(0)';
                }, 300);
            }
        });
    })();

    /* ==========================================================
     SECTION 8 : ANIMATION DES LIGNES D'INSCRIPTIONS
     ========================================================== */
    (function initInscriptionsAnimation() {
        var lignes = document.querySelectorAll('.admin-list-item');

        lignes.forEach(function(ligne, index) {
            ligne.style.opacity = '0';
            ligne.style.transform = 'translateX(-10px)';
            ligne.style.transition = 'all 0.4s ease';

            if ('IntersectionObserver' in window) {
                var observer = new IntersectionObserver(function(entries) {
                    if (entries[0].isIntersecting) {
                        setTimeout(function() {
                            ligne.style.opacity = '1';
                            ligne.style.transform = 'translateX(0)';
                        }, 100 + (index * 80));
                        observer.disconnect();
                    }
                }, { threshold: 0.1 });

                observer.observe(ligne);
            } else {
                setTimeout(function() {
                    ligne.style.opacity = '1';
                    ligne.style.transform = 'translateX(0)';
                }, 200 + (index * 100));
            }
        });
    })();

})();