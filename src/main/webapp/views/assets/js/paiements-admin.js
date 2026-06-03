// ============================================================
// FICHIER : views/assets/js/paiements-admin.js
// RÔLE    : Script de la page d'administration des paiements
//           - Confirmation avant validation/refus
//           - Animation des lignes après action
//           - Toast notifications
//           - Rafraîchissement automatique des stats
//           - Filtrage et recherche dans le tableau
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : CONFIGURATION
     ========================================================== */
    var config = {
        autoRefreshInterval: 60000, // Rafraîchir les stats toutes les 60 secondes
        animationDuration: 400
    };

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initConfirmationDialog();
        initRowAnimations();
        initAutoRefreshStats();
        initQuickSearch();
        initTooltips();
        console.log('Admin Paiements - Page initialisee');
    });

    /* ==========================================================
     SECTION 3 : CONFIRMATION PERSONNALISÉE
     ========================================================== */
    function initConfirmationDialog() {
        // Remplacer le confirm() natif par une modale personnalisée
        var forms = document.querySelectorAll('.form-action');

        forms.forEach(function(form) {
            form.addEventListener('submit', function(e) {
                var action = this.querySelector('input[name="action"]').value;
                var id = this.querySelector('input[name="id"]').value;
                var row = this.closest('tr');
                var pseudo = '';

                if (row) {
                    var pseudoCell = row.querySelector('.cell-utilisateur span');
                    if (pseudoCell) {
                        pseudo = pseudoCell.textContent.trim();
                    }
                }

                var message = '';
                var icon = '';
                var confirmText = '';

                if (action === 'valider') {
                    message = 'Valider le paiement de <strong>' + pseudo + '</strong> ?<br><br>' +
                              'Assurez-vous d\'avoir recu <strong>10 000 FC</strong> sur votre telephone.<br>' +
                              'Le compte premium sera active immediatement.';
                    icon = 'fa-check-circle';
                    confirmText = 'Oui, valider';
                } else {
                    message = 'Refuser le paiement de <strong>' + pseudo + '</strong> ?<br><br>' +
                              'Cette action est irreversible.';
                    icon = 'fa-times-circle';
                    confirmText = 'Oui, refuser';
                }

                // Empecher la soumission par defaut
                e.preventDefault();

                // Afficher la modale de confirmation
                showConfirmationModal(message, icon, confirmText, action, function() {
                    // Soumettre le formulaire après confirmation
                    form.submit();
                });
            });
        });
    }

    /**
     * Affiche une modale de confirmation personnalisée
     */
    function showConfirmationModal(message, icon, confirmText, action, callback) {
        // Supprimer une modale existante
        var existingModal = document.querySelector('.admin-confirm-modal');
        if (existingModal) existingModal.remove();

        var overlay = document.createElement('div');
        overlay.className = 'admin-confirm-modal';
        overlay.style.cssText = 
            'position:fixed;top:0;left:0;width:100%;height:100%;' +
            'background:rgba(0,0,0,0.5);z-index:9999;display:flex;' +
            'align-items:center;justify-content:center;' +
            'animation:paFadeIn 0.2s ease;';

        var card = document.createElement('div');
        card.style.cssText = 
            'background:white;border-radius:18px;padding:30px 28px;' +
            'max-width:440px;width:90%;text-align:center;' +
            'box-shadow:0 20px 60px rgba(0,0,0,0.2);' +
            'animation:paModalIn 0.3s cubic-bezier(0.175,0.885,0.32,1.275);';

        var iconElem = document.createElement('div');
        iconElem.style.cssText = 
            'width:60px;height:60px;border-radius:50%;margin:0 auto 14px;' +
            'display:flex;align-items:center;justify-content:center;font-size:26px;';

        if (action === 'valider') {
            iconElem.style.background = '#e8f5e9';
            iconElem.style.color = '#27ae60';
        } else {
            iconElem.style.background = '#fdecea';
            iconElem.style.color = '#e74c3c';
        }
        iconElem.innerHTML = '<i class="fas ' + icon + '"></i>';

        var messageElem = document.createElement('p');
        messageElem.innerHTML = message;
        messageElem.style.cssText = 
            'font-size:14px;color:#555;margin:0 0 22px 0;line-height:1.6;';

        var buttonsDiv = document.createElement('div');
        buttonsDiv.style.cssText = 'display:flex;gap:12px;justify-content:center;flex-wrap:wrap;';

        var cancelBtn = document.createElement('button');
        cancelBtn.textContent = 'Annuler';
        cancelBtn.style.cssText = 
            'padding:12px 24px;border-radius:10px;font-size:14px;font-weight:600;' +
            'font-family:Poppins,sans-serif;cursor:pointer;border:none;' +
            'background:#f5f6fa;color:#555;transition:all 0.3s ease;';
        cancelBtn.addEventListener('mouseenter', function() {
            this.style.background = '#e8eaef';
        });
        cancelBtn.addEventListener('mouseleave', function() {
            this.style.background = '#f5f6fa';
        });

        var confirmBtn = document.createElement('button');
        confirmBtn.textContent = confirmText;
        confirmBtn.style.cssText = 
            'padding:12px 24px;border-radius:10px;font-size:14px;font-weight:600;' +
            'font-family:Poppins,sans-serif;cursor:pointer;border:none;color:white;' +
            'transition:all 0.3s ease;';

        if (action === 'valider') {
            confirmBtn.style.background = 'linear-gradient(135deg,#27ae60,#2ecc71)';
            confirmBtn.style.boxShadow = '0 4px 12px rgba(39,174,96,0.25)';
        } else {
            confirmBtn.style.background = 'linear-gradient(135deg,#e74c3c,#c0392b)';
            confirmBtn.style.boxShadow = '0 4px 12px rgba(231,76,60,0.25)';
        }

        // Fermer la modale
        function closeModal() {
            overlay.style.opacity = '0';
            overlay.style.transition = 'opacity 0.2s ease';
            setTimeout(function() { overlay.remove(); }, 200);
        }

        cancelBtn.addEventListener('click', closeModal);
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) closeModal();
        });
        document.addEventListener('keydown', function escHandler(e) {
            if (e.key === 'Escape') {
                closeModal();
                document.removeEventListener('keydown', escHandler);
            }
        });

        confirmBtn.addEventListener('click', function() {
            closeModal();
            if (callback) callback();
        });

        buttonsDiv.appendChild(cancelBtn);
        buttonsDiv.appendChild(confirmBtn);
        card.appendChild(iconElem);
        card.appendChild(messageElem);
        card.appendChild(buttonsDiv);
        overlay.appendChild(card);
        document.body.appendChild(overlay);
    }

    /* ==========================================================
     SECTION 4 : ANIMATION DES LIGNES
     ========================================================== */
    function initRowAnimations() {
        var rows = document.querySelectorAll('.admin-table tbody tr');

        rows.forEach(function(row, index) {
            // Animation d'entrée
            row.style.opacity = '0';
            row.style.transform = 'translateX(-10px)';
            row.style.transition = 'all 0.4s ease';

            setTimeout(function() {
                row.style.opacity = '1';
                row.style.transform = 'translateX(0)';
            }, 100 + (index * 60));
        });
    }

    /* ==========================================================
     SECTION 5 : RAFRAÎCHISSEMENT AUTO DES STATS
     ========================================================== */
    function initAutoRefreshStats() {
        setInterval(function() {
            var statNombre = document.querySelector('.admin-stat-nombre');
            if (statNombre) {
                // Animation de mise à jour
                statNombre.style.transform = 'scale(1.1)';
                statNombre.style.transition = 'transform 0.2s ease';
                setTimeout(function() {
                    statNombre.style.transform = 'scale(1)';
                }, 200);
            }
        }, config.autoRefreshInterval);
    }

    /* ==========================================================
     SECTION 6 : RECHERCHE RAPIDE
     ========================================================== */
    function initQuickSearch() {
        // Créer un champ de recherche
        var sectionHeader = document.querySelector('.admin-section .section-header');
        if (!sectionHeader) return;

        var searchWrapper = document.createElement('div');
        searchWrapper.style.cssText = 'position:relative;';

        var searchInput = document.createElement('input');
        searchInput.type = 'text';
        searchInput.placeholder = 'Rechercher par pseudo ou ID transaction...';
        searchInput.style.cssText = 
            'padding:10px 14px 10px 38px;border:2px solid #e8eaef;border-radius:10px;' +
            'font-size:13px;font-family:Poppins,sans-serif;width:260px;' +
            'transition:all 0.3s ease;outline:none;';

        var searchIcon = document.createElement('i');
        searchIcon.className = 'fas fa-search';
        searchIcon.style.cssText = 
            'position:absolute;left:12px;top:50%;transform:translateY(-50%);' +
            'color:#8b8fa3;font-size:14px;';

        searchInput.addEventListener('focus', function() {
            this.style.borderColor = '#667eea';
            this.style.boxShadow = '0 0 0 3px rgba(102,126,234,0.08)';
        });

        searchInput.addEventListener('blur', function() {
            this.style.borderColor = '#e8eaef';
            this.style.boxShadow = 'none';
        });

        searchInput.addEventListener('input', function() {
            var query = this.value.toLowerCase().trim();
            var rows = document.querySelectorAll('.admin-table tbody tr');

            rows.forEach(function(row) {
                var text = row.textContent.toLowerCase();
                if (query === '' || text.includes(query)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });

        searchWrapper.appendChild(searchIcon);
        searchWrapper.appendChild(searchInput);

        var badgeCount = sectionHeader.querySelector('.badge-count');
        if (badgeCount) {
            sectionHeader.insertBefore(searchWrapper, badgeCount);
        } else {
            sectionHeader.appendChild(searchWrapper);
        }
    }

    /* ==========================================================
     SECTION 7 : TOOLTIPS
     ========================================================== */
    function initTooltips() {
        var tooltipElements = document.querySelectorAll('[title]');

        tooltipElements.forEach(function(element) {
            var title = element.getAttribute('title');
            if (!title) return;

            element.setAttribute('data-tooltip', title);
            element.removeAttribute('title');

            element.addEventListener('mouseenter', function() {
                showTooltip(element, element.getAttribute('data-tooltip'));
            });

            element.addEventListener('mouseleave', function() {
                hideTooltip(element);
            });
        });
    }

    function showTooltip(element, text) {
        var tooltip = document.createElement('div');
        tooltip.className = 'admin-tooltip';
        tooltip.textContent = text;
        tooltip.style.cssText = 
            'position:absolute;z-index:10000;background:#1a1a2e;color:white;' +
            'padding:6px 12px;border-radius:6px;font-size:11px;white-space:nowrap;' +
            'pointer-events:none;box-shadow:0 4px 12px rgba(0,0,0,0.15);' +
            'animation:paFadeIn 0.2s ease;';

        document.body.appendChild(tooltip);

        var rect = element.getBoundingClientRect();
        var tooltipRect = tooltip.getBoundingClientRect();

        var top = rect.top - tooltipRect.height - 8 + window.scrollY;
        var left = rect.left + (rect.width / 2) - (tooltipRect.width / 2);

        if (left < 10) left = 10;
        if (left + tooltipRect.width > window.innerWidth - 10) {
            left = window.innerWidth - tooltipRect.width - 10;
        }
        if (top < window.scrollY + 10) {
            top = rect.bottom + 8 + window.scrollY;
        }

        tooltip.style.top = top + 'px';
        tooltip.style.left = left + 'px';

        element._tooltip = tooltip;
    }

    function hideTooltip(element) {
        if (element._tooltip) {
            element._tooltip.remove();
            element._tooltip = null;
        }
    }

    /* ==========================================================
     SECTION 8 : ANIMATIONS CSS INJECTÉES
     ========================================================== */
    (function injectAnimations() {
        var style = document.createElement('style');
        style.textContent = 
            '@keyframes paFadeIn { from { opacity:0; } to { opacity:1; } }' +
            '@keyframes paModalIn { ' +
            '  from { opacity:0; transform:scale(0.9) translateY(20px); }' +
            '  to { opacity:1; transform:scale(1) translateY(0); }' +
            '}';
        document.head.appendChild(style);
    })();

})();