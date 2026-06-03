// ============================================================
// FICHIER : views/assets/js/admin-questions.js
// RÔLE    : Script de la page d'administration des questions
//           - Ouverture/fermeture de la modale
//           - Confirmation de suppression
//           - Recherche en temps réel
//           - Animation des lignes du tableau
//           - Raccourcis clavier
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        animationDelay: 60,
        searchDebounce: 300
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initRowAnimations();
        initSearchAutofocus();
        initKeyboardShortcuts();
        initDeleteConfirmation();
        console.log('Admin Questions - Page initialisee');
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
     SECTION 4 : FOCUS AUTO SUR LA RECHERCHE
     ========================================================== */
    function initSearchAutofocus() {
        var searchInput = document.querySelector('.recherche-input');
        if (!searchInput) return;

        // Si une recherche est déjà en cours, focus sur le champ
        if (searchInput.value.length > 0) {
            searchInput.focus();
            // Placer le curseur à la fin du texte
            var length = searchInput.value.length;
            searchInput.setSelectionRange(length, length);
        }
    }

    /* ==========================================================
     SECTION 5 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ignorer si on est dans un champ de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || 
                e.target.tagName === 'SELECT' || e.target.isContentEditable) {
                return;
            }

            // Ignorer si la modale est ouverte
            var modal = document.getElementById('modalQuestion');
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
                    var modalQuestion = document.getElementById('modalQuestion');
                    if (modalQuestion && modalQuestion.style.display === 'flex') {
                        fermerModalQuestion();
                    }
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 6 : CONFIRMATION DE SUPPRESSION AMÉLIORÉE
     ========================================================== */
    function initDeleteConfirmation() {
        var deleteForms = document.querySelectorAll('.form-action');

        deleteForms.forEach(function(form) {
            var actionInput = form.querySelector('input[name="action"]');
            if (!actionInput || actionInput.value !== 'supprimer') return;

            form.addEventListener('submit', function(e) {
                var row = this.closest('tr');
                var questionTexte = '';
                if (row) {
                    var texteCell = row.querySelector('.cell-texte');
                    if (texteCell) {
                        questionTexte = texteCell.textContent.trim();
                    }
                }

                var message = 'Supprimer definitivement cette question ?\n\n';
                if (questionTexte) {
                    message += '"' + questionTexte + '"\n\n';
                }
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
     SECTION 7 : RECHERCHE EN TEMPS RÉEL (DEBOUNCE)
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
            }, config.searchDebounce);
        });
    })();

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
     SECTION 9 : COMPTEUR DE CARACTÈRES POUR LE TEXTAREA
     ========================================================== */
    (function initCharCounter() {
        var textarea = document.getElementById('texteQuestion');
        if (!textarea) return;

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
    })();

    /* ==========================================================
     SECTION 10 : SOULIGNER LA LIGNE MODIFIÉE
     ========================================================== */
    (function initModifiedRowHighlight() {
        // Vérifier si un message de succès contient "modifiée"
        var messageSucces = document.querySelector('.message-succes span');
        if (messageSucces && messageSucces.textContent.includes('modifi')) {
            // Faire défiler jusqu'au tableau
            var tableau = document.querySelector('.admin-table');
            if (tableau) {
                tableau.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    })();

})();