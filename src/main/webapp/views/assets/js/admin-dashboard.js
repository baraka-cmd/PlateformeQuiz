// ============================================================
// FICHIER : views/assets/js/admin-dashboard.js
// RÔLE    : Script du tableau de bord administrateur
//           - Animation des compteurs statistiques
//           - Actualisation de la date en temps réel
//           - Animation des cartes au survol
//           - Animation des barres de progression
//           - Rafraîchissement automatique des données
//           - Gestion des messages flash
//           - Raccourcis clavier
//           - Tooltips
//           - Notification toast
//           - Détection d'inactivité
//           - Gestion du temps réel
// ============================================================

(function() {
    'use strict';

    // ==========================================================
    // 1. CONFIGURATION GLOBALE
    // ==========================================================
    var CONFIG = {
        compteurDuree: 1500,
        autoRefreshInterval: 120000,
        messageTimeout: 6000,
        messageFadeDuration: 400,
        animationDelay: 50,
        inactivityTimeout: 1800000,
        scrollRevealThreshold: 0.15,
        keyboardShortcuts: true,
        dateUpdateInterval: 30000,
        progressBarAnimationDuration: 1200
    };

    // ==========================================================
    // 2. ÉTAT GLOBAL
    // ==========================================================
    var state = {
        isDeleting: false,
        inactivityTimer: null,
        lastActivity: Date.now(),
        countersAnimated: false
    };

    // ==========================================================
    // 3. INITIALISATION
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('✅ Dashboard Admin - Interface initialisée avec succès');
    });

    function initAll() {
        initCompteursStatistiques();
        initDateActualisation();
        initCartesHover();
        initProgressBarsAnimation();
        initFlashMessages();
        initScrollReveal();
        initTooltips();
        initKeyboardShortcuts();
        initInactivityDetection();
        initAutoRefresh();
        initPulseAnimations();
        initStatCardClickNavigation();
        log('📊 Tous les modules du dashboard sont initialisés');
    }

    // ==========================================================
    // 4. ANIMATION DES COMPTEURS STATISTIQUES
    // ==========================================================
    function initCompteursStatistiques() {
        var compteurs = document.querySelectorAll('.admin-stat-nombre');

        if (compteurs.length === 0) return;

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        animerCompteur(entry.target);
                        observer.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.3, rootMargin: '0px 0px -30px 0px' });

            compteurs.forEach(function(compteur, index) {
                compteur.style.opacity = '0';
                compteur.style.transform = 'scale(0.8)';
                compteur.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                compteur.style.transitionDelay = (index * 0.05) + 's';
                observer.observe(compteur);
            });
        } else {
            // Fallback pour navigateurs sans IntersectionObserver
            compteurs.forEach(function(compteur, index) {
                compteur.style.opacity = '0';
                compteur.style.transform = 'scale(0.8)';
                setTimeout(function() {
                    animerCompteur(compteur);
                }, 100 + (index * 80));
            });
        }
    }

    function animerCompteur(element) {
        var texteComplet = element.textContent.trim();
        var match = texteComplet.match(/(\d+)/);

        if (!match) {
            element.style.opacity = '1';
            element.style.transform = 'scale(1)';
            element.style.transition = 'opacity 0.4s ease, transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            return;
        }

        var valeurFinale = parseInt(match[1], 10);
        var suffixe = texteComplet.replace(match[1], '');
        var duree = CONFIG.compteurDuree;
        var debutTimestamp = null;

        element.style.opacity = '1';
        element.style.transform = 'scale(1)';
        element.style.transition = 'opacity 0.4s ease, transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

        function step(timestamp) {
            if (!debutTimestamp) debutTimestamp = timestamp;
            var progression = Math.min((timestamp - debutTimestamp) / duree, 1);

            // Easing : ease-out cubic
            var eased = 1 - Math.pow(1 - progression, 3);
            var valeurActuelle = Math.floor(valeurFinale * eased);

            element.textContent = valeurActuelle + suffixe;

            if (progression < 1) {
                requestAnimationFrame(step);
            } else {
                element.textContent = texteComplet;
                element.style.transform = 'scale(1)';

                // Micro-animation de fin
                element.style.transform = 'scale(1.08)';
                setTimeout(function() {
                    element.style.transform = 'scale(1)';
                }, 150);
            }
        }

        requestAnimationFrame(step);
    }

    // ==========================================================
    // 5. ACTUALISATION DE LA DATE
    // ==========================================================
    function initDateActualisation() {
        var dateElement = document.querySelector('.admin-welcome-date span');
        if (!dateElement) return;

        function mettreAJourDate() {
            var maintenant = new Date();
            var options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
            var dateFormatee = maintenant.toLocaleDateString('fr-FR', options);

            // Animation de transition
            dateElement.style.opacity = '0';
            dateElement.style.transform = 'translateY(-5px)';
            dateElement.style.transition = 'opacity 0.2s ease, transform 0.2s ease';

            setTimeout(function() {
                dateElement.textContent = dateFormatee;
                dateElement.style.opacity = '1';
                dateElement.style.transform = 'translateY(0)';
            }, 200);
        }

        // Mise à jour initiale
        mettreAJourDate();

        // Mise à jour périodique (toutes les 30 secondes)
        setInterval(mettreAJourDate, CONFIG.dateUpdateInterval);
    }

    // ==========================================================
    // 6. EFFET HOVER SUR LES CARTES
    // ==========================================================
    function initCartesHover() {
        var cartes = document.querySelectorAll('.admin-stat-card, .admin-dash-card, .admin-shortcut-item');

        cartes.forEach(function(carte) {
            carte.addEventListener('mouseenter', function() {
                var icon = this.querySelector('.admin-stat-icon, .shortcut-icon, .admin-dash-card-title i');
                if (icon) {
                    icon.style.transform = 'scale(1.1) rotate(-5deg)';
                    icon.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                }

                // Effet de brillance sur les cartes statistiques
                if (this.classList.contains('admin-stat-card')) {
                    this.style.boxShadow = '0 12px 40px rgba(102, 126, 234, 0.12)';
                }
            });

            carte.addEventListener('mouseleave', function() {
                var icon = this.querySelector('.admin-stat-icon, .shortcut-icon, .admin-dash-card-title i');
                if (icon) {
                    icon.style.transform = 'scale(1) rotate(0deg)';
                }

                if (this.classList.contains('admin-stat-card')) {
                    this.style.boxShadow = '';
                }
            });
        });
    }

    // ==========================================================
    // 7. ANIMATION DES BARRES DE PROGRESSION
    // ==========================================================
    function initProgressBarsAnimation() {
        var progressBars = document.querySelectorAll('.repartition-bar-fill, .activity-bar-fill');

        if (progressBars.length === 0) return;

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        animerBarreProgression(entry.target);
                        observer.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.3 });

            progressBars.forEach(function(bar) {
                // Sauvegarder la largeur cible
                var targetWidth = bar.style.width;
                bar.dataset.targetWidth = targetWidth;
                bar.style.width = '0%';
                observer.observe(bar);
            });
        } else {
            // Fallback : animation directe avec délai
            progressBars.forEach(function(bar, index) {
                var targetWidth = bar.style.width;
                bar.dataset.targetWidth = targetWidth;
                bar.style.width = '0%';
                setTimeout(function() {
                    animerBarreProgression(bar);
                }, 200 + (index * 150));
            });
        }
    }

    function animerBarreProgression(bar) {
        var targetWidth = bar.dataset.targetWidth || bar.style.width || '0%';
        bar.style.transition = 'width ' + (CONFIG.progressBarAnimationDuration / 1000) + 's cubic-bezier(0.4, 0, 0.2, 1)';
        bar.style.width = targetWidth;
    }

    // ==========================================================
    // 8. MESSAGES FLASH
    // ==========================================================
    function initFlashMessages() {
        var messages = document.querySelectorAll('.message-alerte');

        messages.forEach(function(message, index) {
            // Ajouter un bouton de fermeture
            addCloseButton(message);

            // Animation d'entrée
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
    // 9. ANIMATIONS AU SCROLL (REVEAL)
    // ==========================================================
    function initScrollReveal() {
        var revealElements = document.querySelectorAll('.admin-dash-card, .admin-shortcuts-grid');

        if (revealElements.length === 0) return;

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry, index) {
                    if (entry.isIntersecting) {
                        setTimeout(function() {
                            entry.target.style.opacity = '1';
                            entry.target.style.transform = 'translateY(0)';
                        }, index * 80);
                        observer.unobserve(entry.target);
                    }
                });
            }, {
                threshold: CONFIG.scrollRevealThreshold,
                rootMargin: '0px 0px -30px 0px'
            });

            revealElements.forEach(function(element) {
                element.style.opacity = '0';
                element.style.transform = 'translateY(20px)';
                element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
                observer.observe(element);
            });
        } else {
            revealElements.forEach(function(element) {
                element.style.opacity = '1';
                element.style.transform = 'translateY(0)';
            });
        }
    }

    // ==========================================================
    // 10. TOOLTIPS
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
            tooltip.className = 'ad-custom-tooltip';
            tooltip.textContent = text;
            tooltip.setAttribute('role', 'tooltip');
            tooltip.style.cssText = [
                'position: fixed;',
                'z-index: 9999;',
                'background: #1a1a2e;',
                'color: white;',
                'padding: 8px 14px;',
                'border-radius: 8px;',
                'font-size: 11px;',
                'font-weight: 500;',
                'font-family: "Poppins", sans-serif;',
                'white-space: nowrap;',
                'pointer-events: none;',
                'box-shadow: 0 6px 25px rgba(0,0,0,0.3);',
                'opacity: 0;',
                'transform: translateY(4px);',
                'transition: opacity 0.2s ease, transform 0.2s ease;'
            ].join(' ');

            document.body.appendChild(tooltip);

            var rect = element.getBoundingClientRect();
            var tooltipRect = tooltip.getBoundingClientRect();

            var top = rect.top - tooltipRect.height - 10;
            var left = rect.left + (rect.width / 2) - (tooltipRect.width / 2);

            // Ajustements écran
            if (left < 12) left = 12;
            if (left + tooltipRect.width > window.innerWidth - 12) {
                left = window.innerWidth - tooltipRect.width - 12;
            }
            if (top < 12) {
                top = rect.bottom + 10;
                tooltip.classList.add('tooltip-bottom');
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
    // 11. RACCOURCIS CLAVIER
    // ==========================================================
    function initKeyboardShortcuts() {
        if (!CONFIG.keyboardShortcuts) return;

        document.addEventListener('keydown', function(e) {
            // Ne pas intercepter si focus sur un champ de saisie
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
                case '1':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        window.location.href = getContextPath() + '/admin/cours';
                    }
                    break;
                case '2':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        window.location.href = getContextPath() + '/admin/quiz';
                    }
                    break;
                case '3':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        window.location.href = getContextPath() + '/admin/themes';
                    }
                    break;
                case '4':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        window.location.href = getContextPath() + '/admin/resultats';
                    }
                    break;
                case '5':
                    if (e.ctrlKey || e.metaKey) {
                        e.preventDefault();
                        window.location.href = getContextPath() + '/admin/paiements';
                    }
                    break;
                case 'r':
                    if (e.ctrlKey && e.shiftKey) {
                        e.preventDefault();
                        rafraichirDashboard();
                    }
                    break;
            }
        });
    }

    function getContextPath() {
        return window.location.pathname.split('/')[1] ? '/' + window.location.pathname.split('/')[1] : '';
    }

    // ==========================================================
    // 12. DÉTECTION D'INACTIVITÉ
    // ==========================================================
    function initInactivityDetection() {
        function reinitialiserTimer() {
            state.lastActivity = Date.now();
            clearTimeout(state.inactivityTimer);
            state.inactivityTimer = setTimeout(function() {
                log('⚠️ Inactivité détectée - Rafraîchissement automatique');
                rafraichirDashboard();
            }, CONFIG.inactivityTimeout);
        }

        // Événements de détection d'activité
        ['mousemove', 'keydown', 'scroll', 'click', 'touchstart'].forEach(function(eventName) {
            document.addEventListener(eventName, reinitialiserTimer, { passive: true });
        });

        reinitialiserTimer();
    }

    // ==========================================================
    // 13. RAFRAÎCHISSEMENT AUTOMATIQUE
    // ==========================================================
    function initAutoRefresh() {
        if (CONFIG.autoRefreshInterval <= 0) return;

        setInterval(function() {
            // Animation de pulse sur les compteurs
            var statNumbers = document.querySelectorAll('.admin-stat-nombre');
            statNumbers.forEach(function(stat) {
                stat.style.transform = 'scale(1.05)';
                stat.style.transition = 'transform 0.2s ease';
                setTimeout(function() {
                    stat.style.transform = 'scale(1)';
                }, 200);
            });
        }, CONFIG.autoRefreshInterval);
    }

    function rafraichirDashboard() {
        // Animation de chargement
        var container = document.querySelector('.admin-dashboard-container');
        if (container) {
            container.style.opacity = '0.6';
            container.style.transition = 'opacity 0.3s ease';
        }

        // Recharger la page
        setTimeout(function() {
            window.location.reload();
        }, 300);
    }

    // ==========================================================
    // 14. ANIMATION PULSE SUR LES ÉLÉMENTS IMPORTANTS
    // ==========================================================
    function initPulseAnimations() {
        // Animation pulse sur le badge d'alerte paiements
        var badgeWarning = document.querySelector('.admin-dash-card-badge.warning');
        if (badgeWarning) {
            // L'animation est déjà gérée par le CSS
            // Ajouter un arrêt au survol
            badgeWarning.addEventListener('mouseenter', function() {
                this.style.animationPlayState = 'paused';
            });
            badgeWarning.addEventListener('mouseleave', function() {
                this.style.animationPlayState = 'running';
            });
        }

        // Animation pulse sur le point vert "Plateforme en ligne"
        var dotActive = document.querySelector('.mini-dot.active');
        if (dotActive) {
            dotActive.addEventListener('mouseenter', function() {
                this.style.animationPlayState = 'paused';
            });
            dotActive.addEventListener('mouseleave', function() {
                this.style.animationPlayState = 'running';
            });
        }
    }

    // ==========================================================
    // 15. NAVIGATION PAR CLIC SUR LES CARTES STATISTIQUES
    // ==========================================================
    function initStatCardClickNavigation() {
        var navigationMap = {
            'utilisateurs': '/admin/resultats',
            'premium': '/admin/paiements',
            'cours': '/admin/cours',
            'quiz': '/admin/quiz',
            'parties': '/admin/resultats',
            'paiements': '/admin/paiements'
        };

        var statCards = document.querySelectorAll('.admin-stat-card');
        statCards.forEach(function(card) {
            var label = card.querySelector('.admin-stat-label');
            if (!label) return;

            var labelText = label.textContent.toLowerCase();
            var targetUrl = null;

            // Chercher une correspondance dans la map
            for (var key in navigationMap) {
                if (labelText.includes(key)) {
                    targetUrl = getContextPath() + navigationMap[key];
                    break;
                }
            }

            if (targetUrl) {
                card.style.cursor = 'pointer';
                card.setAttribute('title', 'Cliquer pour voir le détail');

                card.addEventListener('click', function(e) {
                    // Ne pas naviguer si on a cliqué sur un lien ou bouton
                    if (e.target.closest('a') || e.target.closest('button')) return;
                    window.location.href = targetUrl;
                });
            }
        });
    }

    // ==========================================================
    // 16. NOTIFICATION TOAST
    // ==========================================================
    function showToast(message, type, duration) {
        type = type || 'info';
        duration = duration || 4000;

        var existingToasts = document.querySelectorAll('.ad-toast');
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

        var toast = document.createElement('div');
        toast.className = 'ad-toast';
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
            'animation: adToastIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);',
            'border-left: 4px solid ' + (colors[type] || '#667eea') + ';'
        ].join(' ');

        toast.innerHTML = [
            '<i class="fas ' + (icons[type] || 'fa-info-circle') + '"',
            'style="font-size: 20px; color: ' + (colors[type] || '#667eea') + '; flex-shrink: 0; margin-top: 1px;"></i>',
            '<span style="flex: 1; color: #1a1a2e;">' + message + '</span>',
            '<button onclick="this.parentElement.remove()"',
            'style="background: none; border: none; cursor: pointer; padding: 2px 4px; color: #8b8fa3; font-size: 14px; flex-shrink: 0; margin-top: -2px;"',
            'aria-label="Fermer">',
            '<i class="fas fa-times"></i>',
            '</button>'
        ].join('');

        document.body.appendChild(toast);

        setTimeout(function() {
            if (toast.parentNode) {
                toast.style.transition = 'all 0.3s ease';
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(50px) scale(0.9)';
                setTimeout(function() {
                    if (toast.parentNode) toast.parentNode.removeChild(toast);
                }, 300);
            }
        }, duration);
    }

    // Injecter les keyframes d'animation
    if (!document.getElementById('ad-toast-keyframes')) {
        var style = document.createElement('style');
        style.id = 'ad-toast-keyframes';
        style.textContent = [
            '@keyframes adToastIn {',
            '  from { opacity: 0; transform: translateX(60px) scale(0.85); }',
            '  to { opacity: 1; transform: translateX(0) scale(1); }',
            '}'
        ].join('\n');
        document.head.appendChild(style);
    }

    // ==========================================================
    // 17. UTILITAIRES
    // ==========================================================
    function log(message) {
        if (window.console && console.log) {
            var timestamp = new Date().toLocaleTimeString('fr-FR');
            console.log(
                '%c[' + timestamp + '] %c[Admin Dashboard] %c' + message,
                'color: #8b8fa3; font-size: 10px;',
                'color: #667eea; font-weight: 700;',
                'color: #555;'
            );
        }
    }

    // Exposer les fonctions utiles globalement
    window.AdminDashboard = {
        showToast: showToast,
        rafraichirDashboard: rafraichirDashboard,
        log: log
    };

})();