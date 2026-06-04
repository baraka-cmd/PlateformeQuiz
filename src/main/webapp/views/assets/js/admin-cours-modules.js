// ============================================================
// FICHIER : views/assets/js/admin-cours-modules.js
// RÔLE    : Script de la page de gestion des 15 modules
//           - Animations des cartes modules
//           - Compteurs statistiques animés
//           - Messages flash auto-fermants
//           - Raccourcis clavier
//           - Tooltips
// ============================================================

(function() {
    'use strict';

    // ==========================================================
    // 1. CONFIGURATION
    // ==========================================================
    var CONFIG = {
        messageTimeout: 6000,
        messageFadeDuration: 400,
        cardAnimationDelay: 30,
        tooltipOffset: 10,
        keyboardShortcuts: true
    };

    // ==========================================================
    // 2. INITIALISATION
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('Admin Cours Modules - Interface initialisée');
    });

    function initAll() {
        initFlashMessages();
        initCardAnimations();
        initStatCounters();
        initKeyboardShortcuts();
        initTooltips();
    }

    // ==========================================================
    // 3. MESSAGES FLASH AUTO-FERMANTS
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
    // 4. ANIMATION DES CARTES MODULES
    // ==========================================================
    function initCardAnimations() {
        var cards = document.querySelectorAll('.acm-card');

        if (cards.length === 0) return;

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        animateCard(entry.target);
                        observer.unobserve(entry.target);
                    }
                });
            }, {
                threshold: 0.1,
                rootMargin: '0px 0px -20px 0px'
            });

            cards.forEach(function(card) {
                card.style.opacity = '0';
                card.style.transform = 'translateY(15px)';
                observer.observe(card);
            });
        } else {
            cards.forEach(function(card, index) {
                card.style.opacity = '0';
                card.style.transform = 'translateY(15px)';
                setTimeout(function() {
                    animateCard(card);
                }, CONFIG.cardAnimationDelay * index);
            });
        }

        // Effet hover amélioré
        cards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                var numero = this.querySelector('.acm-card-numero');
                if (numero) {
                    numero.style.transform = 'scale(1.1)';
                    numero.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }
            });

            card.addEventListener('mouseleave', function() {
                var numero = this.querySelector('.acm-card-numero');
                if (numero) {
                    numero.style.transform = 'scale(1)';
                }
            });
        });
    }

    function animateCard(card) {
        card.style.transition = 'opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1), transform 0.5s cubic-bezier(0.4, 0, 0.2, 1)';
        card.style.opacity = '1';
        card.style.transform = 'translateY(0)';
    }

    // ==========================================================
    // 5. COMPTEURS STATISTIQUES ANIMÉS
    // ==========================================================
    function initStatCounters() {
        var counters = document.querySelectorAll('.acm-stat-item strong');

        counters.forEach(function(counter) {
            var finalText = counter.textContent.trim();
            var finalValue = parseInt(finalText);
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
        var duration = 800;
        var startTime = null;
        var startValue = 0;

        element.style.opacity = '1';

        function step(timestamp) {
            if (!startTime) startTime = timestamp;
            var progress = Math.min((timestamp - startTime) / duration, 1);
            var eased = 1 - Math.pow(1 - progress, 3);
            var currentValue = Math.floor(startValue + (finalValue - startValue) * eased);

            element.textContent = currentValue;

            if (progress < 1) {
                requestAnimationFrame(step);
            } else {
                element.textContent = originalText;
            }
        }

        requestAnimationFrame(step);
    }

    // ==========================================================
    // 6. RACCOURCIS CLAVIER
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
                case 'Escape':
                    var backButton = document.querySelector('.acm-btn-retour');
                    if (backButton) window.location.href = backButton.getAttribute('href');
                    break;

                case 'c':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        var firstCreerBtn = document.querySelector('.acm-btn-creer');
                        if (firstCreerBtn) window.location.href = firstCreerBtn.getAttribute('href');
                    }
                    break;
            }
        });
    }

    // ==========================================================
    // 7. TOOLTIPS
    // ==========================================================
    function initTooltips() {
        var tooltipElements = document.querySelectorAll('[title]');
        var currentTooltip = null;

        tooltipElements.forEach(function(element) {
            var title = element.getAttribute('title');
            if (!title) return;

            element.addEventListener('mouseenter', function() {
                showTooltip(element, title);
            });

            element.addEventListener('mouseleave', function() {
                hideTooltip();
            });

            element.addEventListener('click', function() {
                hideTooltip();
            });
        });

        function showTooltip(element, text) {
            hideTooltip();

            var tooltip = document.createElement('div');
            tooltip.className = 'acm-custom-tooltip';
            tooltip.textContent = text;
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

            var rect = element.getBoundingClientRect();
            var tooltipRect = tooltip.getBoundingClientRect();
            var top = rect.top - tooltipRect.height - CONFIG.tooltipOffset;
            var left = rect.left + (rect.width / 2) - (tooltipRect.width / 2);

            if (left < 12) left = 12;
            if (left + tooltipRect.width > window.innerWidth - 12) {
                left = window.innerWidth - tooltipRect.width - 12;
            }
            if (top < 12) top = rect.bottom + CONFIG.tooltipOffset;

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

        window.addEventListener('scroll', function() { hideTooltip(); }, { passive: true });
    }

    // ==========================================================
    // 8. UTILITAIRES
    // ==========================================================
    function log(message) {
        if (window.console && console.log) {
            console.log('%c[Admin Cours Modules] %c' + message,
                'color: #667eea; font-weight: 700;',
                'color: #555;');
        }
    }

})();