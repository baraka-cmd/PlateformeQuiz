// ============================================================
// FICHIER : views/assets/js/admin-cours-chapitres.js
// RÔLE    : Script de la page liste des chapitres
//           - Messages flash auto-fermants
//           - Animations des modules et chapitres
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
        tooltipOffset: 10,
        keyboardShortcuts: true
    };

    // ==========================================================
    // 2. INITIALISATION
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('Admin Cours Chapitres - Interface initialisée');
    });

    function initAll() {
        initFlashMessages();
        initAnimations();
        initTooltips();
        initKeyboardShortcuts();
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
        closeBtn.innerHTML = '<i class="fas fa-times"></i>';
        closeBtn.setAttribute('aria-label', 'Fermer le message');
        closeBtn.style.cssText = [
            'position: absolute; right: 12px; top: 50%; transform: translateY(-50%);',
            'background: none; border: none; cursor: pointer; padding: 6px 8px;',
            'border-radius: 50%; font-size: 13px; color: inherit; opacity: 0.6;',
            'transition: all 0.2s ease; line-height: 1;'
        ].join(' ');

        closeBtn.addEventListener('mouseenter', function() {
            this.style.opacity = '1'; this.style.background = 'rgba(0,0,0,0.05)';
        });
        closeBtn.addEventListener('mouseleave', function() {
            this.style.opacity = '0.6'; this.style.background = 'none';
        });
        closeBtn.addEventListener('click', function(e) {
            e.stopPropagation(); dismissMessage(message);
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
            message.style.maxHeight = '0'; message.style.margin = '0';
            message.style.padding = '0'; message.style.borderWidth = '0'; message.style.overflow = 'hidden';
        }, CONFIG.messageFadeDuration * 0.6);
        setTimeout(function() {
            if (message.parentNode) message.parentNode.removeChild(message);
        }, CONFIG.messageFadeDuration + 200);
    }

    // ==========================================================
    // 4. ANIMATIONS
    // ==========================================================
    function initAnimations() {
        var modules = document.querySelectorAll('.accp-module-card');
        var chapitres = document.querySelectorAll('.accp-chapitre-item');

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                        observer.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.1, rootMargin: '0px 0px -20px 0px' });

            modules.forEach(function(mod, i) {
                mod.style.opacity = '0'; mod.style.transform = 'translateY(20px)';
                mod.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                mod.style.transitionDelay = (i * 0.08) + 's';
                observer.observe(mod);
            });

            chapitres.forEach(function(chap, i) {
                chap.style.opacity = '0'; chap.style.transform = 'translateX(-10px)';
                chap.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
                chap.style.transitionDelay = (i * 0.04) + 's';
                observer.observe(chap);
            });
        } else {
            modules.forEach(function(mod, i) {
                mod.style.opacity = '0'; mod.style.transform = 'translateY(20px)';
                setTimeout(function() {
                    mod.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                    mod.style.opacity = '1'; mod.style.transform = 'translateY(0)';
                }, 50 + (i * 100));
            });
            chapitres.forEach(function(chap, i) {
                chap.style.opacity = '0'; chap.style.transform = 'translateX(-10px)';
                setTimeout(function() {
                    chap.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
                    chap.style.opacity = '1'; chap.style.transform = 'translateX(0)';
                }, 100 + (i * 50));
            });
        }
    }

    // ==========================================================
    // 5. TOOLTIPS
    // ==========================================================
    function initTooltips() {
        var currentTooltip = null;
        document.querySelectorAll('[title]').forEach(function(element) {
            var title = element.getAttribute('title');
            if (!title) return;

            element.addEventListener('mouseenter', function() { showTooltip(element, title); });
            element.addEventListener('mouseleave', hideTooltip);
            element.addEventListener('click', hideTooltip);
        });

        function showTooltip(element, text) {
            hideTooltip();
            var tooltip = document.createElement('div');
            tooltip.textContent = text;
            tooltip.style.cssText = [
                'position: fixed; z-index: 9999; background: #1a1a2e; color: white;',
                'padding: 7px 14px; border-radius: 8px; font-size: 11px; font-weight: 500;',
                'font-family: "Poppins", sans-serif; white-space: nowrap; pointer-events: none;',
                'box-shadow: 0 6px 20px rgba(0,0,0,0.25); opacity: 0; transform: translateY(4px);',
                'transition: opacity 0.2s ease, transform 0.2s ease;'
            ].join(' ');
            document.body.appendChild(tooltip);

            var rect = element.getBoundingClientRect();
            var ttRect = tooltip.getBoundingClientRect();
            var top = rect.top - ttRect.height - CONFIG.tooltipOffset;
            var left = rect.left + (rect.width / 2) - (ttRect.width / 2);
            if (left < 12) left = 12;
            if (left + ttRect.width > window.innerWidth - 12) left = window.innerWidth - ttRect.width - 12;
            if (top < 12) top = rect.bottom + CONFIG.tooltipOffset;

            tooltip.style.top = top + 'px'; tooltip.style.left = left + 'px';
            requestAnimationFrame(function() {
                tooltip.style.opacity = '1'; tooltip.style.transform = 'translateY(0)';
            });
            currentTooltip = tooltip;
        }

        function hideTooltip() {
            if (currentTooltip) {
                currentTooltip.style.opacity = '0'; currentTooltip.style.transform = 'translateY(4px)';
                var ref = currentTooltip;
                setTimeout(function() { if (ref.parentNode) ref.parentNode.removeChild(ref); }, 200);
                currentTooltip = null;
            }
        }
        window.addEventListener('scroll', hideTooltip, { passive: true });
    }

    // ==========================================================
    // 6. RACCOURCIS CLAVIER
    // ==========================================================
    function initKeyboardShortcuts() {
        if (!CONFIG.keyboardShortcuts) return;
        document.addEventListener('keydown', function(e) {
            var ae = document.activeElement;
            if (ae && (ae.tagName === 'INPUT' || ae.tagName === 'TEXTAREA' || ae.tagName === 'SELECT' || ae.isContentEditable)) return;
            if (e.key === 'Escape') {
                var back = document.querySelector('.accp-btn-retour');
                if (back) window.location.href = back.getAttribute('href');
            }
        });
    }

    function log(msg) {
        if (window.console) console.log('%c[Admin Chapitres] %c' + msg, 'color: #667eea; font-weight: 700;', 'color: #555;');
    }

})();