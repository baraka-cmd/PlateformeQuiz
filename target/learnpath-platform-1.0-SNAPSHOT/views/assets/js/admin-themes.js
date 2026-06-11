// ============================================================
// FICHIER : views/assets/js/admin-themes.js
// RÔLE    : Script de la page liste des thèmes admin
//           - Gestion des filtres et recherche
//           - Pagination dynamique
//           - Confirmations de suppression
//           - Animations au scroll
//           - Tooltips interactifs
//           - Messages flash auto-fermants
//           - Compteurs statistiques
//           - Tri des colonnes
//           - Raccourcis clavier
// ============================================================

(function() {
    'use strict';

    // ==========================================================
    // 1. CONFIGURATION
    // ==========================================================
     var CONFIG = {
        messageTimeout: 6000,
        messageFadeDuration: 400,
        rowAnimationDelay: 40,
        tooltipOffset: 10,
        searchDebounceDelay: 500,
        scrollRevealThreshold: 0.1,
        autoRefreshInterval: 0,
        keyboardShortcuts: true
    };

    // ==========================================================
    // 2. ÉTAT GLOBAL
    // ==========================================================
    var state = {
        isDeleting: false,
        searchTimer: null,
        currentSortColumn: null,
        currentSortDirection: 'asc'
    };

    // ==========================================================
    // 3. INITIALISATION
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('Admin Thèmes - Interface initialisée');
    });

    function initAll() {
        initFlashMessages();
        initTableRowAnimations();
        initTooltips();
        initSearchDebounce();
        initDeleteConfirmations();
        initScrollReveal();
        initStatCounters();
        initKeyboardShortcuts();
        initAutoRefresh();
        initSmoothFocus();
        initColumnSortIndicators();
    }

    // ==========================================================
    // 4. MESSAGES FLASH AUTO-FERMANTS
    // ==========================================================
    function initFlashMessages() {
        var messages = document.querySelectorAll('.message-alerte');

        messages.forEach(function(message, index) {
            // Ajouter un bouton de fermeture
            addCloseButton(message);

            // Animation d'entrée décalée
            message.style.opacity = '0';
            message.style.transform = 'translateY(-15px)';
            setTimeout(function() {
                message.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                message.style.opacity = '1';
                message.style.transform = 'translateY(0)';
            }, 50 + (index * 80));

            // Auto-fermeture
            var timeout = parseInt(message.getAttribute('data-timeout')) || CONFIG.messageTimeout;
            if (timeout > 0) {
                setTimeout(function() {
                    dismissMessage(message);
                }, timeout);
            }
        });
    }

    function addCloseButton(message) {
        var closeBtn = document.createElement('button');
        closeBtn.className = 'message-close-btn';
        closeBtn.innerHTML = '<i class="fas fa-times"></i>';
        closeBtn.setAttribute('aria-label', 'Fermer le message');
        closeBtn.style.cssText = [
            'position: absolute;',
            'right: 12px;',
            'top: 50%;',
            'transform: translateY(-50%);',
            'background: none;',
            'border: none;',
            'cursor: pointer;',
            'padding: 6px 8px;',
            'border-radius: 50%;',
            'font-size: 13px;',
            'color: inherit;',
            'opacity: 0.6;',
            'transition: all 0.2s ease;',
            'line-height: 1;'
        ].join(' ');

        closeBtn.addEventListener('mouseenter', function() {
            this.style.opacity = '1';
            this.style.background = 'rgba(0,0,0,0.05)';
        });

        closeBtn.addEventListener('mouseleave', function() {
            this.style.opacity = '0.6';
            this.style.background = 'none';
        });

        closeBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            dismissMessage(message);
        });

        message.style.position = 'relative';
        message.style.paddingRight = '45px';
        message.appendChild(closeBtn);
    }

    function dismissMessage(message) {
        if (!message || message.classList.contains('dismissing')) return;

        message.classList.add('dismissing');
        message.style.transition = 'all ' + (CONFIG.messageFadeDuration / 1000) + 's ease';
        message.style.opacity = '0';
        message.style.transform = 'translateX(25px)';
        message.style.maxHeight = message.offsetHeight + 'px';
        message.style.marginBottom = message.offsetHeight + 'px';

        setTimeout(function() {
            message.style.maxHeight = '0';
            message.style.margin = '0';
            message.style.padding = '0';
            message.style.borderWidth = '0';
            message.style.overflow = 'hidden';
        }, CONFIG.messageFadeDuration * 0.6);

        setTimeout(function() {
            if (message.parentNode) {
                message.parentNode.removeChild(message);
            }
        }, CONFIG.messageFadeDuration + 200);
    }

    // ==========================================================
    // 5. ANIMATION DES LIGNES DU TABLEAU
    // ==========================================================
    function initTableRowAnimations() {
        var rows = document.querySelectorAll('.admin-table tbody tr');

        if (rows.length === 0) return;

        // Si IntersectionObserver est disponible, animer au scroll
        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        animateRow(entry.target);
                        observer.unobserve(entry.target);
                    }
                });
            }, {
                threshold: CONFIG.scrollRevealThreshold,
                rootMargin: '0px 0px -30px 0px'
            });

            rows.forEach(function(row) {
                row.style.opacity = '0';
                row.style.transform = 'translateY(12px)';
                observer.observe(row);
            });
        } else {
            // Fallback : animation séquentielle
            rows.forEach(function(row, index) {
                row.style.opacity = '0';
                row.style.transform = 'translateY(12px)';
                setTimeout(function() {
                    animateRow(row);
                }, CONFIG.rowAnimationDelay * index);
            });
        }
    }

    function animateRow(row) {
        row.style.transition = 'opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1), transform 0.5s cubic-bezier(0.4, 0, 0.2, 1)';
        row.style.opacity = '1';
        row.style.transform = 'translateY(0)';
    }

    // ==========================================================
    // 6. TOOLTIPS INTERACTIFS
    // ==========================================================
    function initTooltips() {
        var tooltipElements = document.querySelectorAll('[data-tooltip]');
        var currentTooltip = null;

        tooltipElements.forEach(function(element) {
            element.addEventListener('mouseenter', function(e) {
                showTooltip(element, e);
            });

            element.addEventListener('mouseleave', function() {
                hideTooltip();
            });

            element.addEventListener('focus', function(e) {
                showTooltip(element, e);
            });

            element.addEventListener('blur', function() {
                hideTooltip();
            });

            // Empêcher le tooltip de rester affiché si l'élément est supprimé
            element.addEventListener('DOMNodeRemoved', function() {
                hideTooltip();
            });
        });

        function showTooltip(element, event) {
            hideTooltip();

            var tooltipText = element.getAttribute('data-tooltip');
            if (!tooltipText) return;

            var tooltip = document.createElement('div');
            tooltip.className = 'custom-tooltip';
            tooltip.textContent = tooltipText;
            tooltip.setAttribute('role', 'tooltip');
            tooltip.style.cssText = [
                'position: fixed;',
                'z-index: 9999;',
                'background: #1a1a2e;',
                'color: white;',
                'padding: 7px 14px;',
                'border-radius: 8px;',
                'font-size: 11px;',
                'font-weight: 500;',
                'font-family: "Poppins", sans-serif;',
                'white-space: nowrap;',
                'pointer-events: none;',
                'box-shadow: 0 6px 20px rgba(0,0,0,0.25);',
                'opacity: 0;',
                'transform: translateY(4px);',
                'transition: opacity 0.2s ease, transform 0.2s ease;'
            ].join(' ');

            document.body.appendChild(tooltip);

            // Positionner
            var rect = element.getBoundingClientRect();
            var tooltipRect = tooltip.getBoundingClientRect();

            var top = rect.top - tooltipRect.height - CONFIG.tooltipOffset;
            var left = rect.left + (rect.width / 2) - (tooltipRect.width / 2);

            // Ajustements si dépasse de l'écran
            if (left < 12) left = 12;
            if (left + tooltipRect.width > window.innerWidth - 12) {
                left = window.innerWidth - tooltipRect.width - 12;
            }
            if (top < 12) {
                top = rect.bottom + CONFIG.tooltipOffset;
                tooltip.classList.add('tooltip-bottom');
            }

            tooltip.style.top = top + 'px';
            tooltip.style.left = left + 'px';

            // Animation d'entrée
            requestAnimationFrame(function() {
                tooltip.style.opacity = '1';
                tooltip.style.transform = 'translateY(0)';
            });

            currentTooltip = tooltip;
        }

        function hideTooltip() {
            if (currentTooltip) {
                currentTooltip.style.opacity = '0';
                currentTooltip.style.transform = 'translateY(4px)';
                var tooltipRef = currentTooltip;
                setTimeout(function() {
                    if (tooltipRef.parentNode) {
                        tooltipRef.parentNode.removeChild(tooltipRef);
                    }
                }, 200);
                currentTooltip = null;
            }
        }

        // Nettoyer tous les tooltips au défilement
        window.addEventListener('scroll', function() {
            hideTooltip();
        }, { passive: true });
    }

       
    // ==========================================================
    // 8. CONFIRMATIONS DE SUPPRESSION
    // ==========================================================
    function initDeleteConfirmations() {
        // Écouter tous les formulaires de suppression
        var deleteForms = document.querySelectorAll('.form-action-inline');

        deleteForms.forEach(function(form) {
            form.addEventListener('submit', function(e) {
                var button = form.querySelector('.btn-supprimer');
                
                // Si le bouton est désactivé (cours associés), empêcher la soumission
                if (button && button.disabled) {
                    e.preventDefault();
                    showDeleteBlockedMessage(form);
                    return false;
                }
                
                // Si pas encore confirmé, empêcher et demander confirmation
                if (!form.dataset.confirmed) {
                    e.preventDefault();
                    showDeleteConfirmation(form);
                    return false;
                }
                
                // Réinitialiser pour la prochaine fois
                delete form.dataset.confirmed;
            });
        });
    }

    function showDeleteConfirmation(form) {
        var nomTheme = '';
        var row = form.closest('tr');
        if (row) {
            var nomElement = row.querySelector('.theme-nom');
            if (nomElement) {
                nomTheme = nomElement.textContent.trim();
            }
        }

        // Empêcher les doubles confirmations
        if (state.isDeleting) return;
        state.isDeleting = true;

        var confirmed = confirm(
            '⚠️  SUPPRESSION DÉFINITIVE\n\n' +
            'Thème : « ' + nomTheme + ' »\n\n' +
            'Cette action est irréversible. Le thème sera définitivement supprimé.\n' +
            'Les cours associés ne seront PAS affectés.\n\n' +
            'Confirmez-vous la suppression ?'
        );

        if (confirmed) {
            form.dataset.confirmed = 'true';
            form.submit();
        }

        // Réinitialiser après un court délai
        setTimeout(function() {
            state.isDeleting = false;
        }, 500);
    }

    function showDeleteBlockedMessage(form) {
        var nomTheme = '';
        var nbCours = 0;
        var row = form.closest('tr');
        
        if (row) {
            var nomElement = row.querySelector('.theme-nom');
            if (nomElement) {
                nomTheme = nomElement.textContent.trim();
            }
            
            var badgeElement = row.querySelector('.badge-cours span');
            if (badgeElement) {
                nbCours = parseInt(badgeElement.textContent.trim()) || 0;
            }
        }

        // Notification toast
        showToast(
            'Impossible de supprimer « ' + nomTheme + ' »\n' +
            nbCours + ' cours y sont associés. Réassignez-les d\'abord.',
            'warning',
            6000
        );
    }

    // ==========================================================
    // 9. ANIMATIONS AU SCROLL (REVEAL)
    // ==========================================================
    function initScrollReveal() {
        var revealElements = document.querySelectorAll('.admin-info-cards .info-card, .admin-guide');

        if (revealElements.length === 0) return;

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry, index) {
                    if (entry.isIntersecting) {
                        setTimeout(function() {
                            entry.target.style.opacity = '1';
                            entry.target.style.transform = 'translateY(0)';
                        }, index * 100);
                        observer.unobserve(entry.target);
                    }
                });
            }, {
                threshold: CONFIG.scrollRevealThreshold,
                rootMargin: '0px 0px -20px 0px'
            });

            revealElements.forEach(function(element) {
                element.style.opacity = '0';
                element.style.transform = 'translateY(20px)';
                element.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                observer.observe(element);
            });
        } else {
            // Fallback pour navigateurs sans support
            revealElements.forEach(function(element) {
                element.style.opacity = '1';
                element.style.transform = 'translateY(0)';
            });
        }
    }

    // ==========================================================
    // 10. COMPTEURS STATISTIQUES ANIMÉS
    // ==========================================================
    function initStatCounters() {
        var counters = document.querySelectorAll('.header-stat-nombre');

        counters.forEach(function(counter) {
            var finalValue = parseInt(counter.textContent.trim());
            if (isNaN(finalValue)) return;

            counter.style.opacity = '0';

            if ('IntersectionObserver' in window) {
                var observer = new IntersectionObserver(function(entries) {
                    if (entries[0].isIntersecting) {
                        animateCounter(counter, finalValue);
                        observer.disconnect();
                    }
                }, { threshold: 0.5 });

                observer.observe(counter);
            } else {
                setTimeout(function() {
                    animateCounter(counter, finalValue);
                }, 300);
            }
        });
    }

    function animateCounter(element, finalValue) {
        var duration = 1000;
        var startTime = null;
        var startValue = 0;

        element.style.opacity = '1';

        function step(timestamp) {
            if (!startTime) startTime = timestamp;
            var progress = Math.min((timestamp - startTime) / duration, 1);
            var eased = 1 - Math.pow(1 - progress, 3); // Ease-out cubic
            var currentValue = Math.floor(startValue + (finalValue - startValue) * eased);

            element.textContent = currentValue;

            if (progress < 1) {
                requestAnimationFrame(step);
            } else {
                element.textContent = finalValue;
            }
        }

        requestAnimationFrame(step);
    }

    // ==========================================================
    // 11. RACCOURCIS CLAVIER
    // ==========================================================
    function initKeyboardShortcuts() {
        if (!CONFIG.keyboardShortcuts) return;

        document.addEventListener('keydown', function(e) {
            // Ne pas intercepter si on est dans un champ de formulaire
            var activeElement = document.activeElement;
            if (activeElement && (
                activeElement.tagName === 'INPUT' ||
                activeElement.tagName === 'TEXTAREA' ||
                activeElement.tagName === 'SELECT' ||
                activeElement.isContentEditable
            )) {
                return;
            }

            switch (e.key) {
                case 'n':
                    // Ctrl+N : Nouveau thème
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var addButton = document.querySelector('.btn-ajouter-theme');
                        if (addButton) {
                            window.location.href = addButton.getAttribute('href');
                        }
                    }
                    break;

                case 'f':
                    // Ctrl+F : Focus recherche
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var searchInput = document.getElementById('rechercheInput');
                        if (searchInput) {
                            searchInput.focus();
                            searchInput.select();
                        }
                    }
                    break;

                case 'r':
                    // Ctrl+R : Réinitialiser les filtres
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var resetButton = document.querySelector('.btn-reset-filtres');
                        if (resetButton) {
                            window.location.href = resetButton.getAttribute('href');
                        }
                    }
                    break;

                case 'Escape':
                    // Échap : Effacer la recherche
                    var searchInput = document.getElementById('rechercheInput');
                    if (searchInput && document.activeElement === searchInput) {
                        searchInput.value = '';
                        searchInput.blur();
                        document.getElementById('formFiltres').submit();
                    }
                    break;
            }
        });
    }

    // ==========================================================
    // 12. RAFRAÎCHISSEMENT AUTOMATIQUE (OPTIONNEL)
    // ==========================================================
    function initAutoRefresh() {
        if (CONFIG.autoRefreshInterval <= 0) return;

        setInterval(function() {
            // Animation de pulse subtile sur le compteur
            var statNumbers = document.querySelectorAll('.header-stat-nombre');
            statNumbers.forEach(function(stat) {
                stat.style.transform = 'scale(1.08)';
                stat.style.transition = 'transform 0.2s ease';
                setTimeout(function() {
                    stat.style.transform = 'scale(1)';
                }, 200);
            });
        }, CONFIG.autoRefreshInterval);
    }

    // ==========================================================
    // 13. FOCUS DOUX SUR LES ÉLÉMENTS
    // ==========================================================
    function initSmoothFocus() {
        // Ajouter un effet de transition au focus
        var focusableElements = document.querySelectorAll(
            'a, button, input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );

        focusableElements.forEach(function(element) {
            element.addEventListener('focus', function() {
                this.style.transition = 'box-shadow 0.2s ease, border-color 0.2s ease';
            });
        });
    }

    // ==========================================================
    // 14. INDICATEURS DE TRI SUR LES COLONNES
    // ==========================================================
    function initColumnSortIndicators() {
        var headers = document.querySelectorAll('.admin-table th');

        headers.forEach(function(header) {
            // Ajouter une icône de tri si la colonne est triable
            if (header.classList.contains('th-id') ||
                header.classList.contains('th-theme') ||
                header.classList.contains('th-ordre') ||
                header.classList.contains('th-cours') ||
                header.classList.contains('th-date')) {

                header.style.cursor = 'pointer';
                header.setAttribute('title', 'Cliquer pour trier');

                header.addEventListener('click', function() {
                    handleColumnSort(header);
                });
            }
        });
    }

    function handleColumnSort(header) {
        // Cette fonction est un placeholder pour un futur tri côté client
        // Actuellement le tri se fait côté serveur via le select
        var select = document.querySelector('select[name="tri"]');
        if (select) {
            select.focus();
            // Animation de clic
            header.style.transform = 'scale(0.96)';
            setTimeout(function() {
                header.style.transform = 'scale(1)';
            }, 150);
        }
    }

    // ==========================================================
    // 15. CHANGEMENT NOMBRE D'ÉLÉMENTS PAR PAGE
    // ==========================================================
    window.changerNbParPage = function(nb) {
        var url = new URL(window.location.href);
        url.searchParams.set('limite', nb);
        url.searchParams.delete('page'); // Revenir à la page 1
        window.location.href = url.toString();
    };

    // ==========================================================
    // 16. CONFIRMATION DE SUPPRESSION (FONCTION GLOBALE)
    // ==========================================================
    window.confirmerSuppression = function(event, nomTheme, nbCours) {
        event.preventDefault();

        if (nbCours > 0) {
            showToast(
                'Impossible de supprimer « ' + nomTheme + ' »\n' +
                nbCours + ' cours y sont associés. Réassignez-les d\'abord.',
                'warning',
                6000
            );
            return false;
        }

        if (state.isDeleting) return false;
        state.isDeleting = true;

        var confirmed = confirm(
            '⚠️  SUPPRESSION DÉFINITIVE\n\n' +
            'Thème : « ' + nomTheme + ' »\n\n' +
            'Cette action est irréversible.\n\n' +
            'Confirmez-vous la suppression ?'
        );

        if (confirmed && event.target) {
            var form = event.target.closest('form');
            if (form) {
                form.submit();
            }
        }

        setTimeout(function() {
            state.isDeleting = false;
        }, 500);

        return false;
    };

    // ==========================================================
    // 17. NOTIFICATION TOAST
    // ==========================================================
    function showToast(message, type, duration) {
        type = type || 'info';
        duration = duration || 4000;

        // Supprimer les toasts existants
        var existingToasts = document.querySelectorAll('.ath-toast');
        existingToasts.forEach(function(t) { t.remove(); });

        var icons = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            info: 'fa-info-circle',
            warning: 'fa-exclamation-triangle'
        };

        var colors = {
            success: '#27ae60',
            error: '#e74c3c',
            info: '#667eea',
            warning: '#f39c12'
        };

        var backgrounds = {
            success: '#e8f5e9',
            error: '#fdecea',
            info: '#eef0ff',
            warning: '#fff8e1'
        };

        var toast = document.createElement('div');
        toast.className = 'ath-toast';
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'polite');
        toast.style.cssText = [
            'position: fixed;',
            'bottom: 30px;',
            'right: 30px;',
            'z-index: 9999;',
            'background: white;',
            'padding: 16px 22px;',
            'border-radius: 14px;',
            'box-shadow: 0 12px 40px rgba(0,0,0,0.18);',
            'display: flex;',
            'align-items: flex-start;',
            'gap: 12px;',
            'font-size: 13px;',
            'font-weight: 500;',
            'font-family: "Poppins", sans-serif;',
            'max-width: 420px;',
            'line-height: 1.5;',
            'animation: athToastIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);',
            'border-left: 4px solid ' + (colors[type] || '#667eea') + ';',
            'white-space: pre-line;'
        ].join(' ');

        toast.innerHTML = [
            '<i class="fas ' + (icons[type] || 'fa-info-circle') + '"',
            'style="font-size: 20px; color: ' + (colors[type] || '#667eea') + '; flex-shrink: 0; margin-top: 1px;"></i>',
            '<span style="flex: 1; color: #1a1a2e;">' + message.replace(/\n/g, '<br>') + '</span>',
            '<button onclick="this.parentElement.remove()"',
            'style="background: none; border: none; cursor: pointer; padding: 2px 4px; color: #8b8fa3; font-size: 14px; flex-shrink: 0; margin-top: -2px;"',
            'aria-label="Fermer">',
            '<i class="fas fa-times"></i>',
            '</button>'
        ].join('');

        document.body.appendChild(toast);

        // Auto-suppression
        setTimeout(function() {
            if (toast.parentNode) {
                toast.style.transition = 'all 0.3s ease';
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(50px) scale(0.9)';

                setTimeout(function() {
                    if (toast.parentNode) {
                        toast.parentNode.removeChild(toast);
                    }
                }, 300);
            }
        }, duration);
    }

    // Injection du keyframe d'animation du toast
    if (!document.getElementById('ath-toast-keyframes')) {
        var style = document.createElement('style');
        style.id = 'ath-toast-keyframes';
        style.textContent = '@keyframes athToastIn { from { opacity: 0; transform: translateX(60px) scale(0.85); } to { opacity: 1; transform: translateX(0) scale(1); } }';
        document.head.appendChild(style);
    }

    // ==========================================================
    // 18. UTILITAIRES
    // ==========================================================
    function log(message) {
        if (window.console && console.log) {
            console.log('%c[Admin Thèmes] %c' + message,
                'color: #667eea; font-weight: 700;',
                'color: #555;');
        }
    }

    // Exposer showToast globalement
    window.showToast = showToast;

})();