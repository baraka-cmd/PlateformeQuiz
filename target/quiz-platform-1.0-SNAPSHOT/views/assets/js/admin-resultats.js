// ============================================================
// FICHIER : views/assets/js/admin-resultats.js
// RÔLE    : Scripts des pages de résultats administrateur
//           - Animation des compteurs statistiques
//           - Recherche en temps réel avec debounce
//           - Animation des lignes du tableau
//           - Raccourcis clavier
//           - Export CSV
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        animationDelay: 40,
        searchDebounce: 300,
        compteurDuree: 1500
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initCompteursStatistiques();
        initRowAnimations();
        initLiveSearch();
        initSearchAutofocus();
        initKeyboardShortcuts();
        console.log('Admin Resultats - Page initialisee');
    });

    /* ==========================================================
     SECTION 3 : ANIMATION DES COMPTEURS STATISTIQUES
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
        // Extraire le nombre (peut contenir %)
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
     SECTION 4 : ANIMATION DES LIGNES DU TABLEAU
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
     SECTION 5 : RECHERCHE EN TEMPS RÉEL (DEBOUNCE)
     ========================================================== */
    function initLiveSearch() {
        var searchInput = document.querySelector('.recherche-input');
        if (!searchInput) return;

        var debounceTimer = null;

        searchInput.addEventListener('input', function() {
            clearTimeout(debounceTimer);

            var query = this.value.toLowerCase().trim();
            var rows = document.querySelectorAll('.admin-table tbody tr');

            debounceTimer = setTimeout(function() {
                rows.forEach(function(row) {
                    var text = row.textContent.toLowerCase();
                    if (query === '' || text.includes(query)) {
                        row.style.display = '';
                        row.style.opacity = '1';
                    } else {
                        row.style.display = 'none';
                        row.style.opacity = '0';
                    }
                });
            }, config.searchDebounce);
        });
    }

    /* ==========================================================
     SECTION 6 : FOCUS AUTO SUR LA RECHERCHE
     ========================================================== */
    function initSearchAutofocus() {
        var searchInput = document.querySelector('.recherche-input');
        if (!searchInput) return;

        if (searchInput.value.length > 0) {
            searchInput.focus();
            var length = searchInput.value.length;
            searchInput.setSelectionRange(length, length);
        }
    }

    /* ==========================================================
     SECTION 7 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || 
                e.target.tagName === 'SELECT' || e.target.isContentEditable) {
                return;
            }

            switch (e.key) {
                case 'f':
                case 'F':
                    if (e.ctrlKey) {
                        e.preventDefault();
                        var searchInput = document.querySelector('.recherche-input');
                        if (searchInput) searchInput.focus();
                    }
                    break;

                case 'e':
                case 'E':
                    if (e.ctrlKey && e.shiftKey) {
                        e.preventDefault();
                        exporterCSV();
                    }
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 8 : EXPORT CSV
     ========================================================== */
    function exporterCSV() {
        var table = document.querySelector('.admin-table');
        if (!table) return;

        var rows = table.querySelectorAll('tr');
        var csv = [];

        for (var i = 0; i < rows.length; i++) {
            var row = [];
            var cols = rows[i].querySelectorAll('th, td');

            for (var j = 0; j < cols.length - 1; j++) { // -1 pour ignorer la colonne Actions
                var text = cols[j].textContent.trim();
                // Échapper les guillemets
                text = text.replace(/"/g, '""');
                row.push('"' + text + '"');
            }

            csv.push(row.join(','));
        }

        var csvString = csv.join('\n');
        var blob = new Blob(['\uFEFF' + csvString], { type: 'text/csv;charset=utf-8;' });
        var link = document.createElement('a');
        var url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', 'resultats_amijayquiz_' + new Date().toISOString().slice(0, 10) + '.csv');
        link.style.display = 'none';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    // Exposer la fonction d'export globalement
    window.exporterCSV = exporterCSV;

    /* ==========================================================
     SECTION 9 : ANIMATION DES BOUTONS D'ACTION
     ========================================================== */
    (function initActionButtonsAnimation() {
        var actionButtons = document.querySelectorAll('.btn-action-icon');

        actionButtons.forEach(function(btn) {
            btn.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.15)';
                this.style.transition = 'transform 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            btn.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });

            btn.addEventListener('mousedown', function() {
                this.style.transform = 'scale(0.9)';
            });

            btn.addEventListener('mouseup', function() {
                this.style.transform = 'scale(1.15)';
            });
        });
    })();

    /* ==========================================================
     SECTION 10 : ANIMATION DES BADGES
     ========================================================== */
    (function initBadgeAnimation() {
        var badges = document.querySelectorAll('.badge-statut, .badge-score');

        badges.forEach(function(badge) {
            badge.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.05)';
                this.style.transition = 'transform 0.2s ease';
            });

            badge.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });
        });
    })();

})();