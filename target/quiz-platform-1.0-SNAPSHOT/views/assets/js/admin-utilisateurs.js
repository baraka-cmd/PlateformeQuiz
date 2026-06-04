// ============================================================
// FICHIER : views/assets/js/admin-utilisateurs.js
// RÔLE    : Script des pages utilisateurs (liste + détail)
//           - Messages flash auto-fermants
//           - Animations des lignes du tableau
//           - Compteurs statistiques animés
//           - Raccourcis clavier
//           - Tooltips
//           - Confirmation actions sensibles
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
        searchDebounceDelay: 600,
        scrollRevealThreshold: 0.1,
        counterAnimationDuration: 1000,
        keyboardShortcuts: true
    };

    // ==========================================================
    // 2. ÉTAT GLOBAL
    // ==========================================================
    var state = {
        isDeleting: false,
        countersAnimated: false
    };

    // ==========================================================
    // 3. INITIALISATION
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('Admin Utilisateurs - Interface initialisée');
    });

    function initAll() {
        initFlashMessages();
        initTableRowAnimations();
        initStatCounters();
        initTooltips();
        initKeyboardShortcuts();
        initActionConfirmations();
        initHoverEffects();
    }

    // ==========================================================
    // 4. MESSAGES FLASH AUTO-FERMANTS
    // ==========================================================
    function initFlashMessages() {
        var messages = document.querySelectorAll('.message-alerte');

        messages.forEach(function(message, index) {
            addCloseButton(message);

            message.style.opacity = '0';
            message.style.transform = 'translateY(-15px)';
            setTimeout(function() {
                message.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                message.style.opacity = '1';
                message.style.transform = 'translateY(0)';
            }, 50 + (index * 80));

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
            'position: absolute;', 'right: 12px;', 'top: 50%;', 'transform: translateY(-50%);',
            'background: none;', 'border: none;', 'cursor: pointer;', 'padding: 6px 8px;',
            'border-radius: 50%;', 'font-size: 13px;', 'color: inherit;', 'opacity: 0.6;',
            'transition: all 0.2s ease;', 'line-height: 1;'
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
        var rows = document.querySelectorAll('.au-table tbody tr');

        if (rows.length === 0) return;

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
    // 6. COMPTEURS STATISTIQUES ANIMÉS
    // ==========================================================
    function initStatCounters() {
        var counters = document.querySelectorAll('.au-stat-nombre, .au-stat-nombre-detail');

        counters.forEach(function(counter) {
            var finalText = counter.textContent.trim();
            var finalValue = parseInt(finalText.replace(/[^0-9]/g, ''));
            if (isNaN(finalValue)) return;

            counter.style.opacity = '0';

            if ('IntersectionObserver' in window) {
                var observer = new IntersectionObserver(function(entries) {
                    if (entries[0].isIntersecting) {
                        animateCounter(counter, finalValue, finalText);
                        observer.disconnect();
                    }
                }, { threshold: 0.5 });

                observer.observe(counter);
            } else {
                setTimeout(function() {
                    animateCounter(counter, finalValue, finalText);
                }, 300);
            }
        });
    }

    function animateCounter(element, finalValue, originalText) {
        var duration = CONFIG.counterAnimationDuration;
        var startTime = null;
        var startValue = 0;

        element.style.opacity = '1';

        function step(timestamp) {
            if (!startTime) startTime = timestamp;
            var progress = Math.min((timestamp - startTime) / duration, 1);
            var eased = 1 - Math.pow(1 - progress, 3);
            var currentValue = Math.floor(startValue + (finalValue - startValue) * eased);

            // Garder le texte original mais remplacer le nombre
            var displayText = originalText.replace(/\d+/, currentValue);
            // Si le texte contient un pourcentage
            if (originalText.includes('%')) {
                displayText = currentValue + '%';
            }
            element.textContent = displayText;

            if (progress < 1) {
                requestAnimationFrame(step);
            } else {
                element.textContent = originalText;
            }
        }

        requestAnimationFrame(step);
    }

    // ==========================================================
    // 7. TOOLTIPS
    // ==========================================================
    function initTooltips() {
        var tooltipElements = document.querySelectorAll('[title]');
        var currentTooltip = null;

        tooltipElements.forEach(function(element) {
            var title = element.getAttribute('title');
            if (!title || title.length < 3) return;

            element.addEventListener('mouseenter', function(e) {
                showTooltip(element, title, e);
            });

            element.addEventListener('mouseleave', function() {
                hideTooltip();
            });

            element.addEventListener('click', function() {
                hideTooltip();
            });
        });

        function showTooltip(element, text, event) {
            hideTooltip();

            var tooltip = document.createElement('div');
            tooltip.className = 'au-custom-tooltip';
            tooltip.textContent = text;
            tooltip.setAttribute('role', 'tooltip');
            tooltip.style.cssText = [
                'position: fixed;', 'z-index: 9999;', 'background: #1a1a2e;', 'color: white;',
                'padding: 8px 14px;', 'border-radius: 8px;', 'font-size: 11px;', 'font-weight: 500;',
                'font-family: "Poppins", sans-serif;', 'white-space: nowrap;', 'pointer-events: none;',
                'box-shadow: 0 6px 25px rgba(0,0,0,0.3);', 'opacity: 0;', 'transform: translateY(4px);',
                'transition: opacity 0.2s ease, transform 0.2s ease;'
            ].join(' ');

            document.body.appendChild(tooltip);

            var rect = element.getBoundingClientRect();
            var tooltipRect = tooltip.getBoundingClientRect();

            var top = rect.top - tooltipRect.height - CONFIG.tooltipOffset;
            var left = rect.left + (rect.width / 2) - (tooltipRect.width / 2);

            if (left < 12) left = 12;
            if (left + tooltipRect.width > window.innerWidth - 12) {
                left = window.innerWidth - tooltipRect.width - 12;
            }
            if (top < 12) {
                top = rect.bottom + CONFIG.tooltipOffset;
            }

            tooltip.style.top = top + 'px';
            tooltip.style.left = left + 'px';

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
                var ref = currentTooltip;
                setTimeout(function() {
                    if (ref.parentNode) ref.parentNode.removeChild(ref);
                }, 200);
                currentTooltip = null;
            }
        }

        window.addEventListener('scroll', function() {
            hideTooltip();
        }, { passive: true });
    }

    // ==========================================================
    // 8. RACCOURCIS CLAVIER
    // ==========================================================
    function initKeyboardShortcuts() {
        if (!CONFIG.keyboardShortcuts) return;

        document.addEventListener('keydown', function(e) {
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
                case 'f':
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
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var resetBtn = document.querySelector('.au-btn-reset');
                        if (resetBtn) {
                            window.location.href = resetBtn.getAttribute('href');
                        } else {
                            window.location.href = window.location.pathname;
                        }
                    }
                    break;

                case 'Escape':
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
    // 9. CONFIRMATIONS ACTIONS SENSIBLES
    // ==========================================================
    function initActionConfirmations() {
        // Désactiver : confirmation supplémentaire
        var desactiverForms = document.querySelectorAll('form[action*="action"] input[value="desactiver"]');
        desactiverForms.forEach(function(input) {
            var form = input.closest('form');
            if (form) {
                form.addEventListener('submit', function(e) {
                    if (!confirm('⚠️ Êtes-vous sûr de vouloir désactiver ce compte ?\n\nL\'utilisateur ne pourra plus se connecter.')) {
                        e.preventDefault();
                    }
                });
            }
        });

        // Promouvoir : confirmation supplémentaire
        var promouvoirForms = document.querySelectorAll('form[action*="action"] input[value="promouvoir"]');
        promouvoirForms.forEach(function(input) {
            var form = input.closest('form');
            if (form) {
                form.addEventListener('submit', function(e) {
                    if (!confirm('👑 Êtes-vous sûr de vouloir promouvoir cet utilisateur en Administrateur ?\n\nIl aura accès à toutes les fonctionnalités d\'administration.')) {
                        e.preventDefault();
                    }
                });
            }
        });

        // Rétrograder : confirmation supplémentaire
        var retrograderForms = document.querySelectorAll('form[action*="action"] input[value="retrograder"]');
        retrograderForms.forEach(function(input) {
            var form = input.closest('form');
            if (form) {
                form.addEventListener('submit', function(e) {
                    if (!confirm('⬇️ Êtes-vous sûr de vouloir rétrograder cet administrateur en Joueur ?\n\nIl perdra l\'accès à l\'administration.')) {
                        e.preventDefault();
                    }
                });
            }
        });
    }

    // ==========================================================
    // 10. EFFETS HOVER
    // ==========================================================
    function initHoverEffects() {
        // Cartes statistiques
        var statCards = document.querySelectorAll('.au-stat-card');
        statCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.au-stat-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.1) rotate(-5deg)';
                    icon.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });
            card.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.au-stat-icon');
                if (icon) {
                    icon.style.transform = 'scale(1) rotate(0deg)';
                }
            });
        });

        // Boutons d'action
        var actionBtns = document.querySelectorAll('.au-btn-icon:not(.disabled)');
        actionBtns.forEach(function(btn) {
            btn.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-2px)';
            });
            btn.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
            });
        });

        // Items de liste
        var listItems = document.querySelectorAll('.au-liste-item');
        listItems.forEach(function(item) {
            item.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.au-liste-icon');
                if (icon) {
                    icon.style.transform = 'scale(1.05)';
                    icon.style.transition = 'transform 0.2s ease';
                }
            });
            item.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.au-liste-icon');
                if (icon) {
                    icon.style.transform = 'scale(1)';
                }
            });
        });
    }

    // ==========================================================
    // 11. UTILITAIRES
    // ==========================================================
    function log(message) {
        if (window.console && console.log) {
            console.log('%c[Admin Utilisateurs] %c' + message,
                'color: #667eea; font-weight: 700;',
                'color: #555;');
        }
    }

})();