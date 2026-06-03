// ============================================================
// FICHIER : views/assets/js/admin-quiz.js
// RÔLE    : Script des pages d'administration des quiz
//           - Gestion des modales (ajout, modification, questions)
//           - Dropdown d'actions
//           - Confirmation de suppression
//           - Compteur de questions sélectionnées
//           - Recherche en temps réel
//           - Animation des lignes
//           - Raccourcis clavier
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        animationDelay: 50,
        searchDebounce: 300
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initRowAnimations();
        initDropdownBehavior();
        initSearchAutofocus();
        initKeyboardShortcuts();
        initDeleteConfirmation();
        console.log('Admin Quiz - Pages initialisees');
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
     SECTION 4 : GESTION DU DROPDOWN
     ========================================================== */
    function initDropdownBehavior() {
        // Fermer tous les dropdowns au clic extérieur
        document.addEventListener('click', function(e) {
            if (!e.target.closest('.dropdown-actions')) {
                document.querySelectorAll('.dropdown-menu').forEach(function(menu) {
                    menu.style.display = 'none';
                });
            }
        });

        // Fermer les dropdowns avec Escape
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                document.querySelectorAll('.dropdown-menu').forEach(function(menu) {
                    menu.style.display = 'none';
                });
            }
        });
    }

    /* ==========================================================
     SECTION 5 : FOCUS AUTO SUR LA RECHERCHE
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
     SECTION 6 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ignorer si on est dans un champ de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || 
                e.target.tagName === 'SELECT' || e.target.isContentEditable) {
                return;
            }

            // Ignorer si une modale est ouverte
            var modals = document.querySelectorAll('.modal-overlay');
            var modalOpen = false;
            modals.forEach(function(m) {
                if (m.style.display === 'flex') modalOpen = true;
            });
            if (modalOpen) return;

            switch (e.key) {
                case 'n':
                case 'N':
                    if (e.ctrlKey) {
                        e.preventDefault();
                        var btnAjout = document.querySelector('.btn-ajouter-quiz');
                        if (btnAjout) btnAjout.click();
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
                    // Fermer toutes les modales
                    document.querySelectorAll('.modal-overlay').forEach(function(modal) {
                        modal.style.display = 'none';
                    });
                    document.body.style.overflow = '';
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 7 : CONFIRMATION DE SUPPRESSION
     ========================================================== */
    function initDeleteConfirmation() {
        // Surcharger la fonction confirmSuppression pour les dropdowns
        window.confirmSuppression = function(titre) {
            var message = 'Supprimer definitivement le quiz "' + titre + '" ?\n\n' +
                         'Les questions seront dissociees mais conservees.\n' +
                         'L\'historique des parties reste intact.\n' +
                         'Cette action est irreversible.';
            return confirm(message);
        };
    }

    /* ==========================================================
     SECTION 8 : RECHERCHE EN TEMPS RÉEL
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
                var visibleCount = 0;
                rows.forEach(function(row) {
                    var text = row.textContent.toLowerCase();
                    if (query === '' || text.includes(query)) {
                        row.style.display = '';
                        row.style.opacity = '1';
                        visibleCount++;
                    } else {
                        row.style.display = 'none';
                        row.style.opacity = '0';
                    }
                });
            }, config.searchDebounce);
        });
    })();

    /* ==========================================================
     SECTION 9 : ANIMATION DES BOUTONS
     ========================================================== */
    (function initActionButtonsAnimation() {
        var actionButtons = document.querySelectorAll('.btn-action-icon:not(.btn-menu)');

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
     SECTION 10 : COMPTEUR DE CARACTÈRES POUR LES CHAMPS
     ========================================================== */
    (function initCharCounters() {
        var textareas = document.querySelectorAll('textarea[id$="Ajout"], textarea[id$="Modif"]');

        textareas.forEach(function(textarea) {
            var counter = document.createElement('span');
            counter.className = 'char-counter';
            counter.style.cssText = 
                'display:block;text-align:right;font-size:11px;color:#8b8fa3;margin-top:4px;';
            textarea.parentNode.appendChild(counter);

            function updateCounter() {
                var length = textarea.value.length;
                counter.textContent = length + ' caractere' + (length > 1 ? 's' : '');
                
                if (length > 500) {
                    counter.style.color = '#e74c3c';
                } else if (length > 300) {
                    counter.style.color = '#f39c12';
                } else {
                    counter.style.color = '#8b8fa3';
                }
            }

            textarea.addEventListener('input', updateCounter);
            updateCounter();
        });
    })();

    /* ==========================================================
     SECTION 11 : MISE À JOUR DU COMPTEUR DE QUESTIONS SÉLECTIONNÉES
     ========================================================== */
    window.updateSelectedCount = function() {
        var checkboxes = document.querySelectorAll('input[name="idsQuestions"]:checked');
        var count = checkboxes.length;
        var counterSpan = document.getElementById('selectedCount');
        var btnAjouter = document.getElementById('btnAjouterQuestions');

        if (counterSpan) {
            counterSpan.textContent = count;
        }
        if (btnAjouter) {
            btnAjouter.disabled = (count === 0);
        }
    };

    /* ==========================================================
     SECTION 12 : ANIMATION DES MODALES
     ========================================================== */
    (function initModalAnimations() {
        // Ajouter une classe pour l'animation de fermeture
        var modals = document.querySelectorAll('.modal-overlay');
        modals.forEach(function(modal) {
            modal.addEventListener('click', function(e) {
                if (e.target === this) {
                    this.style.display = 'none';
                    document.body.style.overflow = '';
                }
            });
        });
    })();

    /* ==========================================================
     SECTION 13 : SOULIGNER LA LIGNE MODIFIÉE
     ========================================================== */
    (function initModifiedRowHighlight() {
        var messageSucces = document.querySelector('.message-succes span');
        if (messageSucces && (messageSucces.textContent.includes('modifi') || 
            messageSucces.textContent.includes('créé') || 
            messageSucces.textContent.includes('supprimé'))) {
            var tableau = document.querySelector('.admin-table');
            if (tableau) {
                tableau.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    })();

    /* ==========================================================
     SECTION 14 : DÉSACTIVER LE BOUTON D'AJOUT SI RIEN SÉLECTIONNÉ
     ========================================================== */
    (function initAjoutQuestionsButtonState() {
        var btnAjouter = document.getElementById('btnAjouterQuestions');
        if (btnAjouter) {
            updateSelectedCount();
        }
    })();

})();