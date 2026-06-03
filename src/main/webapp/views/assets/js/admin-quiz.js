// ============================================================
// FICHIER : views/assets/js/admin-quiz.js
// RÔLE    : Script de la page d'administration des quiz
//           - Ouverture/fermeture de la modale
//           - Confirmation de suppression
//           - Animation des lignes du tableau
//           - Raccourcis clavier
//           - Filtre automatique par niveau
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        animationDelay: 60
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initRowAnimations();
        initKeyboardShortcuts();
        initDeleteConfirmation();
        initNiveauSelectAutoSubmit();
        initSearchAutofocus();
        console.log('Admin Quiz - Page initialisee');
    });

    /* ==========================================================
     SECTION 3 : ANIMATION DES LIGNES DU TABLEAU
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
     SECTION 4 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ignorer si on est dans un champ de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || 
                e.target.tagName === 'SELECT' || e.target.isContentEditable) {
                return;
            }

            // Ignorer si la modale est ouverte
            var modal = document.getElementById('modalQuiz');
            if (modal && modal.style.display === 'flex') return;

            switch (e.key) {
                case 'n':
                case 'N':
                    if (e.ctrlKey) {
                        e.preventDefault();
                        ouvrirModalAjout();
                    }
                    break;

                case 'f':
                case 'F':
                    if (e.ctrlKey) {
                        e.preventDefault();
                        var searchInput = document.querySelector('.recherche-input');
                        if (searchInput) searchInput.focus();
                    }
                    break;

                case 'Escape':
                    var modalQuiz = document.getElementById('modalQuiz');
                    if (modalQuiz && modalQuiz.style.display === 'flex') {
                        fermerModalQuiz();
                    }
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 5 : CONFIRMATION DE SUPPRESSION AMÉLIORÉE
     ========================================================== */
    function initDeleteConfirmation() {
        var deleteForms = document.querySelectorAll('.form-action');

        deleteForms.forEach(function(form) {
            var actionInput = form.querySelector('input[name="action"]');
            if (!actionInput || actionInput.value !== 'supprimer') return;

            form.addEventListener('submit', function(e) {
                var row = this.closest('tr');
                var quizTitre = '';
                if (row) {
                    var titreCell = row.querySelector('.cell-titre');
                    if (titreCell) {
                        quizTitre = titreCell.textContent.trim();
                    }
                }

                var message = 'Supprimer definitivement ce quiz ?\n\n';
                if (quizTitre) {
                    message += '"' + quizTitre + '"\n\n';
                }
                message += 'Les questions associees ne seront pas supprimees mais dissociees.\n';
                message += 'Cette action est irreversible.';

                if (!confirm(message)) {
                    e.preventDefault();
                } else {
                    // Animation de suppression
                    if (row) {
                        row.style.transition = 'all 0.3s ease';
                        row.style.opacity = '0';
                        row.style.transform = 'translateX(30px)';
                    }
                }
            });
        });
    }

    /* ==========================================================
     SECTION 6 : AUTO-SUBMIT DU SELECT NIVEAU
     ========================================================== */
    function initNiveauSelectAutoSubmit() {
        var selectNiveau = document.querySelector('select[name="niveau"]');
        if (!selectNiveau) return;

        // Le select a déjà un onchange="this.form.submit()" dans le HTML
        // On ajoute juste une animation visuelle
        selectNiveau.addEventListener('change', function() {
            var form = this.closest('form');
            if (form) {
                // Ajouter un indicateur de chargement
                var btnFiltrer = form.querySelector('.btn-rechercher');
                if (btnFiltrer) {
                    btnFiltrer.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Filtrage...';
                    btnFiltrer.style.opacity = '0.7';
                    btnFiltrer.style.pointerEvents = 'none';
                }
            }
        });
    }

    /* ==========================================================
     SECTION 7 : FOCUS AUTO SUR LA RECHERCHE
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
     SECTION 8 : ANIMATION DES BOUTONS D'ACTION
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
     SECTION 9 : COMPTEUR DE CARACTÈRES POUR LE TITRE
     ========================================================== */
    (function initCharCounter() {
        var titreInput = document.getElementById('titre');
        if (!titreInput) return;

        var counter = document.createElement('span');
        counter.className = 'char-counter';
        counter.style.cssText = 
            'display:block;text-align:right;font-size:11px;color:#8b8fa3;margin-top:4px;';
        titreInput.parentNode.appendChild(counter);

        function updateCounter() {
            var length = titreInput.value.length;
            counter.textContent = length + ' caractere' + (length > 1 ? 's' : '');
            
            if (length > 100) {
                counter.style.color = '#e74c3c';
            } else if (length > 70) {
                counter.style.color = '#f39c12';
            } else {
                counter.style.color = '#8b8fa3';
            }
        }

        titreInput.addEventListener('input', updateCounter);
        updateCounter();
    })();

    /* ==========================================================
     SECTION 10 : RECHERCHE EN TEMPS RÉEL (DEBOUNCE)
     ========================================================== */
    (function initLiveSearch() {
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
            }, 300);
        });
    })();

    /* ==========================================================
     SECTION 11 : SOULIGNER LA LIGNE MODIFIÉE
     ========================================================== */
    (function initModifiedRowHighlight() {
        var messageSucces = document.querySelector('.message-succes span');
        if (messageSucces && messageSucces.textContent.includes('modifi')) {
            var tableau = document.querySelector('.admin-table');
            if (tableau) {
                tableau.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    })();

})();