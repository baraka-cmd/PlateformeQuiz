// ============================================================
// FICHIER : views/assets/js/main.js
// RÔLE    : Script principal de la plateforme AmijayQuiz
//           - Initialisation globale
//           - Gestion du menu utilisateur déroulant
//           - Gestion des messages flash (succès/erreur)
//           - Animations au scroll (reveal)
//           - Gestion des tooltips
//           - Gestion des confirmations de suppression
//           - Utilitaires communs
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION GLOBALE
// ============================================================
var AmijayQuiz = AmijayQuiz || {};

AmijayQuiz.config = {
    contextPath: window.location.pathname.split('/')[1] || '',
    animationDuration: 300,
    messageTimeout: 5000,
    scrollRevealThreshold: 0.15
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initUserMenu();
    initFlashMessages();
    initScrollReveal();
    initTooltips();
    initSmoothScroll();
    initConfirmDialogs();
    initActiveNavLink();
    console.log('✅ AmijayQuiz - Plateforme initialisée');
});

// ============================================================
// MENU UTILISATEUR DÉROULANT
// ============================================================
function initUserMenu() {
    var trigger = document.getElementById('userMenuTrigger');
    var dropdown = document.getElementById('userMenuDropdown');

    if (!trigger || !dropdown) return;

    // Toggle dropdown au clic
    trigger.addEventListener('click', function(e) {
        e.stopPropagation();
        var isOpen = dropdown.style.opacity === '1';
        
        if (isOpen) {
            closeDropdown();
        } else {
            openDropdown();
        }
    });

    // Fermer au clic extérieur
    document.addEventListener('click', function(e) {
        if (!dropdown.contains(e.target) && e.target !== trigger) {
            closeDropdown();
        }
    });

    // Fermer avec la touche Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeDropdown();
        }
    });

    function openDropdown() {
        dropdown.style.opacity = '1';
        dropdown.style.visibility = 'visible';
        dropdown.style.transform = 'translateY(0)';
        trigger.setAttribute('aria-expanded', 'true');
    }

    function closeDropdown() {
        dropdown.style.opacity = '0';
        dropdown.style.visibility = 'hidden';
        dropdown.style.transform = 'translateY(-10px)';
        trigger.setAttribute('aria-expanded', 'false');
    }
}

// ============================================================
// MESSAGES FLASH (SUCCÈS / ERREUR)
// ============================================================
function initFlashMessages() {
    var messages = document.querySelectorAll('.message-alerte');

    messages.forEach(function(message) {
        // Ajouter un bouton de fermeture
        var closeBtn = document.createElement('button');
        closeBtn.className = 'message-close-btn';
        closeBtn.innerHTML = '<i class="fas fa-times"></i>';
        closeBtn.setAttribute('aria-label', 'Fermer le message');
        closeBtn.style.cssText = 'background: none; border: none; cursor: pointer; padding: 4px 8px; margin-left: auto; opacity: 0.6; font-size: 14px; color: inherit;';
        
        closeBtn.addEventListener('click', function() {
            dismissMessage(message);
        });

        message.appendChild(closeBtn);

        // Auto-dismiss après le délai configuré
        var timeout = parseInt(message.getAttribute('data-timeout')) || AmijayQuiz.config.messageTimeout;
        
        if (timeout > 0) {
            setTimeout(function() {
                dismissMessage(message);
            }, timeout);
        }
    });
}

function dismissMessage(message) {
    if (!message || message.classList.contains('dismissing')) return;

    message.classList.add('dismissing');
    message.style.transition = 'all 0.3s ease';
    message.style.opacity = '0';
    message.style.transform = 'translateX(20px)';
    message.style.maxHeight = message.offsetHeight + 'px';

    setTimeout(function() {
        message.style.maxHeight = '0';
        message.style.margin = '0';
        message.style.padding = '0';
        message.style.borderWidth = '0';
        message.style.overflow = 'hidden';
    }, 200);

    setTimeout(function() {
        if (message.parentNode) {
            message.parentNode.removeChild(message);
        }
    }, 500);
}

// ============================================================
// ANIMATIONS AU SCROLL (REVEAL)
// ============================================================
function initScrollReveal() {
    var revealElements = document.querySelectorAll('[data-reveal], .animate-on-scroll');

    if (revealElements.length === 0) return;

    // Observer pour les animations au scroll
    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('revealed');
                    observer.unobserve(entry.target);
                }
            });
        }, {
            threshold: AmijayQuiz.config.scrollRevealThreshold,
            rootMargin: '0px 0px -50px 0px'
        });

        revealElements.forEach(function(element) {
            element.classList.add('reveal-hidden');
            observer.observe(element);
        });
    } else {
        // Fallback pour navigateurs sans IntersectionObserver
        revealElements.forEach(function(element) {
            element.classList.add('revealed');
        });
    }
}

// ============================================================
// TOOLTIPS
// ============================================================
function initTooltips() {
    var tooltipElements = document.querySelectorAll('[data-tooltip]');

    tooltipElements.forEach(function(element) {
        element.addEventListener('mouseenter', function(e) {
            showTooltip(element);
        });

        element.addEventListener('mouseleave', function() {
            hideTooltip(element);
        });

        element.addEventListener('focus', function() {
            showTooltip(element);
        });

        element.addEventListener('blur', function() {
            hideTooltip(element);
        });
    });
}

function showTooltip(element) {
    var tooltipText = element.getAttribute('data-tooltip');
    if (!tooltipText) return;

    // Supprimer tout tooltip existant
    hideAllTooltips();

    var tooltip = document.createElement('div');
    tooltip.className = 'custom-tooltip';
    tooltip.textContent = tooltipText;
    tooltip.style.cssText = 'position: absolute; z-index: 9999; background: #1a1a2e; color: white; padding: 8px 14px; border-radius: 8px; font-size: 12px; font-weight: 500; white-space: nowrap; pointer-events: none; box-shadow: 0 4px 15px rgba(0,0,0,0.2); animation: fadeInUp 0.2s ease;';

    document.body.appendChild(tooltip);

    // Positionner le tooltip
    var rect = element.getBoundingClientRect();
    var tooltipRect = tooltip.getBoundingClientRect();

    var top = rect.top - tooltipRect.height - 10 + window.scrollY;
    var left = rect.left + (rect.width / 2) - (tooltipRect.width / 2);

    // Ajuster si dépasse à gauche
    if (left < 10) left = 10;

    // Ajuster si dépasse à droite
    if (left + tooltipRect.width > window.innerWidth - 10) {
        left = window.innerWidth - tooltipRect.width - 10;
    }

    // Afficher en dessous si pas assez d'espace au-dessus
    if (top < window.scrollY + 10) {
        top = rect.bottom + 10 + window.scrollY;
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

function hideAllTooltips() {
    document.querySelectorAll('.custom-tooltip').forEach(function(tooltip) {
        tooltip.remove();
    });
}

// ============================================================
// SMOOTH SCROLL
// ============================================================
function initSmoothScroll() {
    var smoothLinks = document.querySelectorAll('a[href^="#"]:not([href="#"])');

    smoothLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            var targetId = this.getAttribute('href');
            var targetElement = document.querySelector(targetId);

            if (targetElement) {
                e.preventDefault();
                
                var headerHeight = document.querySelector('.navbar') ? 
                    document.querySelector('.navbar').offsetHeight : 65;
                
                var targetPosition = targetElement.getBoundingClientRect().top + 
                    window.pageYOffset - headerHeight - 20;

                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });

                // Mettre à jour l'URL sans recharger
                if (history.pushState) {
                    history.pushState(null, null, targetId);
                }
            }
        });
    });
}

// ============================================================
// CONFIRMATIONS DE SUPPRESSION
// ============================================================
function initConfirmDialogs() {
    var confirmButtons = document.querySelectorAll('[data-confirm]');

    confirmButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            var message = this.getAttribute('data-confirm') || 
                'Êtes-vous sûr de vouloir effectuer cette action ?';
            
            var titre = this.getAttribute('data-confirm-title') || 'Confirmation';
            var btnOk = this.getAttribute('data-confirm-ok') || 'Confirmer';
            var btnCancel = this.getAttribute('data-confirm-cancel') || 'Annuler';

            if (!confirm(message)) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }
        });
    });
}

// ============================================================
// LIEN ACTIF DANS LA NAVIGATION
// ============================================================
function initActiveNavLink() {
    var currentPath = window.location.pathname;
    var navLinks = document.querySelectorAll('.nav-links a');

    navLinks.forEach(function(link) {
        var href = link.getAttribute('href');
        
        if (href && currentPath.indexOf(href.replace(/\/$/, '')) !== -1) {
            // Ne pas marquer "active" sur le lien dashboard si on est sur une sous-page
            var isDashboard = href.indexOf('dashboard') !== -1;
            var isOnSubPage = currentPath !== href && currentPath.indexOf(href) === 0;

            if (!isDashboard || !isOnSubPage) {
                navLinks.forEach(function(l) { l.classList.remove('active'); });
                link.classList.add('active');
            }
        }
    });
}

// ============================================================
// UTILITAIRES
// ============================================================

/**
 * Formate un nombre de secondes en chaîne lisible
 * @param {number} secondes - Temps en secondes
 * @returns {string} Temps formaté (ex: "2 min 30 s")
 */
AmijayQuiz.formaterTemps = function(secondes) {
    if (secondes < 0) secondes = 0;

    var minutes = Math.floor(secondes / 60);
    var secs = Math.floor(secondes % 60);

    if (minutes > 0) {
        return minutes + ' min ' + secs + ' s';
    }
    return secs + ' s';
};

/**
 * Formate une date en français
 * @param {Date|string} date - La date à formater
 * @returns {string} Date formatée (ex: "15 juin 2026")
 */
AmijayQuiz.formaterDate = function(date) {
    if (typeof date === 'string') {
        date = new Date(date);
    }

    var options = { day: 'numeric', month: 'long', year: 'numeric' };
    return date.toLocaleDateString('fr-FR', options);
};

/**
 * Tronque un texte à une longueur donnée
 * @param {string} texte - Le texte à tronquer
 * @param {number} longueur - Longueur maximale
 * @returns {string} Texte tronqué
 */
AmijayQuiz.tronquerTexte = function(texte, longueur) {
    if (!texte) return '';
    longueur = longueur || 100;

    if (texte.length <= longueur) return texte;
    return texte.substring(0, longueur).trim() + '...';
};

/**
 * Affiche une notification toast
 * @param {string} message - Le message à afficher
 * @param {string} type - Le type de notification (success, error, info, warning)
 * @param {number} duree - Durée d'affichage en ms
 */
AmijayQuiz.toast = function(message, type, duree) {
    type = type || 'info';
    duree = duree || 4000;

    // Supprimer les toasts existants
    var existingToasts = document.querySelectorAll('.amijay-toast');
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
    toast.className = 'amijay-toast';
    toast.style.cssText = 'position: fixed; bottom: 30px; right: 30px; z-index: 9999; ' +
        'background: white; padding: 16px 22px; border-radius: 14px; box-shadow: 0 10px 40px rgba(0,0,0,0.15); ' +
        'display: flex; align-items: center; gap: 12px; font-size: 14px; font-weight: 500; ' +
        'max-width: 400px; animation: fadeInRight 0.3s ease; ' +
        'border-left: 4px solid ' + (colors[type] || '#667eea') + ';';

    toast.innerHTML = '<i class="fas ' + (icons[type] || 'fa-info-circle') + 
        '" style="font-size: 20px; color: ' + (colors[type] || '#667eea') + '; flex-shrink: 0;"></i>' +
        '<span>' + message + '</span>';

    document.body.appendChild(toast);

    // Supprimer après le délai
    setTimeout(function() {
        toast.style.transition = 'all 0.3s ease';
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';

        setTimeout(function() {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, duree);
};

/**
 * Copie un texte dans le presse-papier
 * @param {string} texte - Le texte à copier
 * @returns {Promise<boolean>} True si la copie a réussi
 */
AmijayQuiz.copierPressePapier = function(texte) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        return navigator.clipboard.writeText(texte).then(function() {
            return true;
        }).catch(function() {
            return false;
        });
    }

    // Fallback pour navigateurs plus anciens
    var textarea = document.createElement('textarea');
    textarea.value = texte;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();

    try {
        document.execCommand('copy');
        document.body.removeChild(textarea);
        return Promise.resolve(true);
    } catch (e) {
        document.body.removeChild(textarea);
        return Promise.resolve(false);
    }
};

/**
 * Ajoute une classe avec animation à un élément
 * @param {HTMLElement} element - L'élément cible
 * @param {string} className - La classe à ajouter
 * @param {number} delay - Délai avant ajout en ms
 */
AmijayQuiz.animerElement = function(element, className, delay) {
    delay = delay || 0;

    setTimeout(function() {
        element.classList.add(className);
    }, delay);
};

// ============================================================
// ANIMATIONS CSS (STYLES INJECTÉS DYNAMIQUEMENT)
// ============================================================
(function injectAnimations() {
    var style = document.createElement('style');
    style.textContent = 
        '.reveal-hidden { opacity: 0; transform: translateY(25px); transition: opacity 0.6s ease, transform 0.6s ease; }' +
        '.revealed { opacity: 1 !important; transform: translateY(0) !important; }' +
        '.dismissing { transition: all 0.3s ease; }' +
        '@keyframes fadeInRight { from { opacity: 0; transform: translateX(50px); } to { opacity: 1; transform: translateX(0); } }' +
        '.custom-tooltip::after { content: ""; position: absolute; bottom: -6px; left: 50%; transform: translateX(-50%); ' +
        '  width: 0; height: 0; border-left: 6px solid transparent; border-right: 6px solid transparent; border-top: 6px solid #1a1a2e; }';
    document.head.appendChild(style);
})();