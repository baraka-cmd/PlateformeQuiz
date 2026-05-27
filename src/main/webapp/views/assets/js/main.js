
/**
 * ============================================================
 * FICHIER : views/assets/js/main.js
 * RÔLE    : Scripts globaux de la plateforme AmijayQuiz
 *           - Gestion du scroll de la navbar (ombre + masquage)
 *           - Détection de la page active dans la navigation
 *           - Gestion des messages d'alerte (auto-dismiss + fermeture)
 *           - Toggle affichage mot de passe
 *           - Menu mobile (hamburger)
 *           - Animation au scroll (révélation progressive)
 *           - Gestion du thème (light/dark) - préparé pour le futur
 *           - Utilitaires globaux (formatage, validation, etc.)
 *           - Détection mobile/touchscreen
 *           - Gestionnaire d'erreurs global
 * ============================================================
 */

(function() {
    'use strict';

        document.addEventListener('DOMContentLoaded', function() {
            initNavbarScroll();
            initActiveNavLink();
            initAlertAutoDismiss();
            initSmoothScroll();
            initMobileMenu();
            initScrollReveal();
            initBackToTop();
            initTooltips();
            initUserMenu();  // ← AJOUTER CETTE LIGNE
        });
    /* ==========================================================
     SECTION 2 : NAVBAR - OMBRE ET EFFET AU SCROLL
     ========================================================== */
    function initNavbarScroll() {
        const navbar = document.querySelector('.navbar');
        if (!navbar) return;

        let lastScrollY = window.scrollY;
        let ticking = false;

        window.addEventListener('scroll', function() {
            if (!ticking) {
                window.requestAnimationFrame(function() {
                    const scrollY = window.scrollY;
                    
                    // Ajouter/retirer l'ombre selon la position du scroll
                    if (scrollY > 15) {
                        navbar.classList.add('scrolled');
                    } else {
                        navbar.classList.remove('scrolled');
                    }
                    
                    // Masquer/afficher la navbar selon la direction du scroll
                    if (scrollY > lastScrollY && scrollY > 200) {
                        // Scroll vers le bas → masquer
                        navbar.style.transform = 'translateY(-100%)';
                    } else {
                        // Scroll vers le haut → afficher
                        navbar.style.transform = 'translateY(0)';
                    }
                    
                    lastScrollY = scrollY;
                    ticking = false;
                });
                ticking = true;
            }
        }, { passive: true });
    }

    /* ==========================================================
     SECTION 3 : LIEN ACTIF DANS LA NAVIGATION
     ========================================================== */
    function initActiveNavLink() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-links a:not(.btn-connexion):not(.btn-deconnexion)');
        
        navLinks.forEach(function(link) {
            const href = link.getAttribute('href');
            if (!href) return;
            
            // Ignorer le lien d'accueil s'il y a d'autres correspondances plus spécifiques
            if (href === '/' || href === window.location.origin + '/') {
                if (currentPath === '/' || currentPath === window.location.origin + '/') {
                    link.classList.add('active');
                }
                return;
            }
            
            // Vérifier si l'URL courante contient le href du lien
            const cleanHref = href.replace(window.location.origin, '');
            if (cleanHref && currentPath.includes(cleanHref)) {
                // Retirer active de tous les liens d'abord
                navLinks.forEach(l => l.classList.remove('active'));
                link.classList.add('active');
            }
        });
    }

    /* ==========================================================
     SECTION 4 : MESSAGES D'ALERTE - AUTO-DISMISS + FERMETURE
     ========================================================== */
    function initAlertAutoDismiss() {
        const alerts = document.querySelectorAll('.message-alerte');
        
        alerts.forEach(function(alert, index) {
            // Délai progressif pour chaque alerte
            const delay = 6000 + (index * 1500);
            
            // Ajouter un bouton de fermeture
            addCloseButton(alert);
            
            // Auto-dismiss après le délai
            setTimeout(function() {
                dismissAlert(alert);
            }, delay);
        });
    }

    function addCloseButton(alert) {
        // Vérifier si le bouton existe déjà
        if (alert.querySelector('.alert-close-btn')) return;
        
        const closeBtn = document.createElement('button');
        closeBtn.innerHTML = '<i class="fas fa-times"></i>';
        closeBtn.className = 'alert-close-btn';
        closeBtn.setAttribute('aria-label', 'Fermer le message');
        closeBtn.setAttribute('title', 'Fermer');
        closeBtn.setAttribute('type', 'button');
        
        // Styles du bouton
        Object.assign(closeBtn.style, {
            background: 'none',
            border: 'none',
            color: 'inherit',
            cursor: 'pointer',
            marginLeft: 'auto',
            fontSize: '14px',
            opacity: '0.6',
            transition: 'all 0.3s ease',
            padding: '6px 8px',
            borderRadius: '6px',
            flexShrink: '0',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
        });
        
        // Effets hover
        closeBtn.addEventListener('mouseenter', function() {
            this.style.opacity = '1';
            this.style.backgroundColor = 'rgba(0, 0, 0, 0.05)';
        });
        
        closeBtn.addEventListener('mouseleave', function() {
            this.style.opacity = '0.6';
            this.style.backgroundColor = 'transparent';
        });
        
        // Action de fermeture
        closeBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            dismissAlert(alert);
        });
        
        // Ajouter le bouton à l'alerte
        alert.style.display = 'flex';
        alert.style.alignItems = 'center';
        alert.appendChild(closeBtn);
    }

    function dismissAlert(alert) {
        if (!alert || alert.dataset.dismissing === 'true') return;
        
        alert.dataset.dismissing = 'true';
        alert.style.transition = 'all 0.4s ease';
        alert.style.opacity = '0';
        alert.style.transform = 'translateY(-20px) scale(0.95)';
        alert.style.maxHeight = alert.offsetHeight + 'px';
        
        // Forcer le reflow
        alert.offsetHeight;
        alert.style.maxHeight = '0';
        alert.style.marginTop = '0';
        alert.style.marginBottom = '0';
        alert.style.paddingTop = '0';
        alert.style.paddingBottom = '0';
        alert.style.borderWidth = '0';
        
        // Supprimer après l'animation
        alert.addEventListener('transitionend', function handler() {
            alert.removeEventListener('transitionend', handler);
            if (alert.parentNode) {
                alert.parentNode.removeChild(alert);
            }
        });
        
        // Fallback si transitionend ne se déclenche pas
        setTimeout(function() {
            if (alert.parentNode) {
                alert.parentNode.removeChild(alert);
            }
        }, 500);
    }

    /* ==========================================================
     SECTION 5 : SCROLL DOUX POUR LES ANCRES INTERNES
     ========================================================== */
    function initSmoothScroll() {
        document.addEventListener('click', function(e) {
            const link = e.target.closest('a[href^="#"]');
            if (!link) return;
            
            const targetId = link.getAttribute('href');
            if (targetId === '#') return;
            
            const target = document.querySelector(targetId);
            if (target) {
                e.preventDefault();
                
                const navbarHeight = document.querySelector('.navbar')?.offsetHeight || 70;
                const targetPosition = target.getBoundingClientRect().top + window.pageYOffset;
                const offsetPosition = targetPosition - navbarHeight - 20;
                
                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
                
                // Mettre le focus sur l'élément pour l'accessibilité
                target.setAttribute('tabindex', '-1');
                target.focus({ preventScroll: true });
            }
        });
    }

    /* ==========================================================
     SECTION 6 : MENU MOBILE (HAMBURGER)
     ========================================================== */
    function initMobileMenu() {
        const navbar = document.querySelector('.navbar');
        if (!navbar) return;
        
        // Créer le bouton hamburger
        const hamburger = document.createElement('button');
        hamburger.className = 'hamburger-btn';
        hamburger.setAttribute('aria-label', 'Menu de navigation');
        hamburger.setAttribute('aria-expanded', 'false');
        hamburger.setAttribute('type', 'button');
        hamburger.innerHTML = `
            <span class="hamburger-line"></span>
            <span class="hamburger-line"></span>
            <span class="hamburger-line"></span>
        `;
        
        // Styles du bouton hamburger
        Object.assign(hamburger.style, {
            display: 'none',
            flexDirection: 'column',
            gap: '5px',
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '8px',
            zIndex: '1001'
        });
        
        // Insérer le bouton dans la navbar
        const logo = navbar.querySelector('.logo');
        if (logo && logo.parentNode) {
            logo.parentNode.insertBefore(hamburger, logo.nextSibling);
        }
        
        // Styles responsives pour le hamburger
        const style = document.createElement('style');
        style.textContent = `
            @media (max-width: 768px) {
                .hamburger-btn {
                    display: flex !important;
                }
                
                .hamburger-line {
                    width: 24px;
                    height: 2px;
                    background: #4a00e0;
                    border-radius: 2px;
                    transition: all 0.3s ease;
                }
                
                .hamburger-btn[aria-expanded="true"] .hamburger-line:nth-child(1) {
                    transform: translateY(7px) rotate(45deg);
                }
                
                .hamburger-btn[aria-expanded="true"] .hamburger-line:nth-child(2) {
                    opacity: 0;
                }
                
                .hamburger-btn[aria-expanded="true"] .hamburger-line:nth-child(3) {
                    transform: translateY(-7px) rotate(-45deg);
                }
                
                .nav-links {
                    display: none !important;
                    position: absolute;
                    top: 100%;
                    left: 0;
                    right: 0;
                    background: white;
                    flex-direction: column;
                    padding: 20px;
                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
                    gap: 10px !important;
                }
                
                .nav-links.active {
                    display: flex !important;
                }
                
                .nav-links .user-info {
                    flex-direction: column;
                    width: 100%;
                    margin-left: 0 !important;
                }
            }
        `;
        document.head.appendChild(style);
        
        // Toggle menu mobile
        const navLinks = navbar.querySelector('.nav-links');
        hamburger.addEventListener('click', function() {
            const isOpen = hamburger.getAttribute('aria-expanded') === 'true';
            hamburger.setAttribute('aria-expanded', !isOpen);
            navLinks.classList.toggle('active');
            
            // Empêcher le scroll quand le menu est ouvert
            document.body.style.overflow = isOpen ? '' : 'hidden';
        });
        
        // Fermer le menu en cliquant en dehors
        document.addEventListener('click', function(e) {
            if (!navbar.contains(e.target) && navLinks.classList.contains('active')) {
                navLinks.classList.remove('active');
                hamburger.setAttribute('aria-expanded', 'false');
                document.body.style.overflow = '';
            }
        });
        
        // Fermer le menu au clic sur un lien
        navLinks.querySelectorAll('a').forEach(function(link) {
            link.addEventListener('click', function() {
                navLinks.classList.remove('active');
                hamburger.setAttribute('aria-expanded', 'false');
                document.body.style.overflow = '';
            });
        });
    }

    /* ==========================================================
     SECTION 7 : ANIMATION AU SCROLL (RÉVÉLATION PROGRESSIVE)
     ========================================================== */
    function initScrollReveal() {
        const revealElements = document.querySelectorAll('.reveal, .stat-card, .categorie-card, .niveau-card, .partie-card, .quiz-card');
        
        if (revealElements.length === 0) return;
        
        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -50px 0px',
            threshold: 0.1
        };
        
        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    // Délai progressif pour un effet cascade
                    setTimeout(function() {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                    }, index * 50);
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);
        
        revealElements.forEach(function(el) {
            el.style.opacity = '0';
            el.style.transform = 'translateY(25px)';
            el.style.transition = 'all 0.6s ease';
            observer.observe(el);
        });
    }

    /* ==========================================================
     SECTION 8 : BOUTON RETOUR EN HAUT
     ========================================================== */
    function initBackToTop() {
        // Créer le bouton
        const btn = document.createElement('button');
        btn.className = 'back-to-top';
        btn.innerHTML = '<i class="fas fa-arrow-up"></i>';
        btn.setAttribute('aria-label', 'Retour en haut de la page');
        btn.setAttribute('title', 'Retour en haut');
        btn.setAttribute('type', 'button');
        
        // Styles du bouton
        Object.assign(btn.style, {
            position: 'fixed',
            bottom: '25px',
            right: '25px',
            width: '48px',
            height: '48px',
            borderRadius: '14px',
            background: 'linear-gradient(135deg, #667eea, #764ba2)',
            color: 'white',
            border: 'none',
            cursor: 'pointer',
            fontSize: '18px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 6px 20px rgba(102, 126, 234, 0.3)',
            zIndex: '999',
            opacity: '0',
            visibility: 'hidden',
            transform: 'translateY(20px)',
            transition: 'all 0.3s ease'
        });
        
        document.body.appendChild(btn);
        
        // Afficher/masquer selon le scroll
        let scrollTicking = false;
        window.addEventListener('scroll', function() {
            if (!scrollTicking) {
                window.requestAnimationFrame(function() {
                    if (window.scrollY > 400) {
                        btn.style.opacity = '1';
                        btn.style.visibility = 'visible';
                        btn.style.transform = 'translateY(0)';
                    } else {
                        btn.style.opacity = '0';
                        btn.style.visibility = 'hidden';
                        btn.style.transform = 'translateY(20px)';
                    }
                    scrollTicking = false;
                });
                scrollTicking = true;
            }
        }, { passive: true });
        
        // Action de retour en haut
        btn.addEventListener('click', function() {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
        
        // Effet hover
        btn.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-4px)';
            this.style.boxShadow = '0 10px 30px rgba(102, 126, 234, 0.5)';
        });
        
        btn.addEventListener('mouseleave', function() {
            if (window.scrollY > 400) {
                this.style.transform = 'translateY(0)';
            }
            this.style.boxShadow = '0 6px 20px rgba(102, 126, 234, 0.3)';
        });
    }

    /* ==========================================================
     SECTION 9 : TOOLTIPS (INFOBULLES)
     ========================================================== */
    function initTooltips() {
        const tooltipElements = document.querySelectorAll('[data-tooltip]');
        
        tooltipElements.forEach(function(el) {
            el.addEventListener('mouseenter', function(e) {
                const tooltip = document.createElement('div');
                tooltip.className = 'custom-tooltip';
                tooltip.textContent = el.getAttribute('data-tooltip');
                
                Object.assign(tooltip.style, {
                    position: 'absolute',
                    background: '#1a1a2e',
                    color: 'white',
                    padding: '8px 14px',
                    borderRadius: '8px',
                    fontSize: '12px',
                    fontWeight: '500',
                    whiteSpace: 'nowrap',
                    zIndex: '9999',
                    pointerEvents: 'none',
                    opacity: '0',
                    transform: 'translateY(5px)',
                    transition: 'all 0.2s ease',
                    boxShadow: '0 4px 15px rgba(0, 0, 0, 0.2)'
                });
                
                document.body.appendChild(tooltip);
                
                const rect = el.getBoundingClientRect();
                const tooltipRect = tooltip.getBoundingClientRect();
                
                tooltip.style.left = rect.left + (rect.width / 2) - (tooltipRect.width / 2) + 'px';
                tooltip.style.top = rect.bottom + 8 + window.scrollY + 'px';
                
                // Animation d'entrée
                requestAnimationFrame(function() {
                    tooltip.style.opacity = '1';
                    tooltip.style.transform = 'translateY(0)';
                });
                
                // Stocker le tooltip pour le supprimer plus tard
                el._tooltip = tooltip;
            });
            
            el.addEventListener('mouseleave', function() {
                if (el._tooltip) {
                    el._tooltip.style.opacity = '0';
                    el._tooltip.style.transform = 'translateY(5px)';
                    
                    el._tooltip.addEventListener('transitionend', function handler() {
                        el._tooltip.removeEventListener('transitionend', handler);
                        if (el._tooltip.parentNode) {
                            el._tooltip.parentNode.removeChild(el._tooltip);
                        }
                        el._tooltip = null;
                    });
                    
                    // Fallback
                    setTimeout(function() {
                        if (el._tooltip && el._tooltip.parentNode) {
                            el._tooltip.parentNode.removeChild(el._tooltip);
                            el._tooltip = null;
                        }
                    }, 300);
                }
            });
        });
    }
    
    
            /* ==========================================================
     SECTION 9B : MENU UTILISATEUR DÉROULANT
     ========================================================== */
    function initUserMenu() {
        var trigger = document.getElementById('userMenuTrigger');
        var dropdown = document.getElementById('userMenuDropdown');
        
        if (!trigger || !dropdown) return;

        // Empêcher le lien du bouton trigger
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            var isOpen = dropdown.classList.contains('active');
            
            if (isOpen) {
                dropdown.classList.remove('active');
                trigger.setAttribute('aria-expanded', 'false');
            } else {
                dropdown.classList.add('active');
                trigger.setAttribute('aria-expanded', 'true');
                
                // Focus sur le premier élément du menu
                setTimeout(function() {
                    var firstItem = dropdown.querySelector('.user-menu-item');
                    if (firstItem) firstItem.focus();
                }, 100);
            }
        });

        // Fermer le menu en cliquant ailleurs
        document.addEventListener('click', function(e) {
            if (!trigger.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.classList.remove('active');
                trigger.setAttribute('aria-expanded', 'false');
            }
        });

        // Fermer avec Échap
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                if (dropdown.classList.contains('active')) {
                    dropdown.classList.remove('active');
                    trigger.setAttribute('aria-expanded', 'false');
                    trigger.focus();
                }
            }
        });

        // Navigation clavier dans le menu
        dropdown.addEventListener('keydown', function(e) {
            var items = dropdown.querySelectorAll('.user-menu-item');
            var activeElement = document.activeElement;
            var currentIndex = Array.from(items).indexOf(activeElement);

            switch (e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    if (currentIndex < items.length - 1) {
                        items[currentIndex + 1].focus();
                    } else {
                        items[0].focus();
                    }
                    break;
                    
                case 'ArrowUp':
                    e.preventDefault();
                    if (currentIndex > 0) {
                        items[currentIndex - 1].focus();
                    } else {
                        items[items.length - 1].focus();
                    }
                    break;
                    
                case 'Tab':
                    dropdown.classList.remove('active');
                    trigger.setAttribute('aria-expanded', 'false');
                    break;
            }
        });
    }
    function openMenu(trigger, dropdown, overlay) {
        trigger.setAttribute('aria-expanded', 'true');
        dropdown.classList.add('active');
        overlay.classList.add('active');
        
        // Focus sur le premier élément du menu
        setTimeout(function() {
            const firstItem = dropdown.querySelector('.user-menu-item');
            if (firstItem) firstItem.focus();
        }, 100);
    }

    function closeMenu(trigger, dropdown, overlay) {
        trigger.setAttribute('aria-expanded', 'false');
        dropdown.classList.remove('active');
        overlay.classList.remove('active');
        trigger.focus();
    }

    /* ==========================================================
     SECTION 10 : UTILITAIRES GLOBAUX
     ========================================================== */

    /**
     * Toggle affichage du mot de passe
     * @param {string} inputId - ID de l'input password
     * @param {string} iconId  - ID du bouton contenant l'icône
     */
    window.togglePassword = function(inputId, iconId) {
        const input = document.getElementById(inputId);
        const icon = document.getElementById(iconId);
        
        if (!input || !icon) return;
        
        const isPassword = input.getAttribute('type') === 'password';
        input.setAttribute('type', isPassword ? 'text' : 'password');
        
        const iconElement = icon.querySelector('i');
        if (iconElement) {
            iconElement.classList.toggle('fa-eye', !isPassword);
            iconElement.classList.toggle('fa-eye-slash', isPassword);
        }
        
        // Remettre le focus sur l'input
        input.focus();
    };

    /**
     * Formater un temps en secondes → MM:SS
     * @param {number} totalSeconds - Temps total en secondes
     * @returns {string} Temps formaté
     */
    window.formatTime = function(totalSeconds) {
        if (isNaN(totalSeconds) || totalSeconds < 0) return '00:00';
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = Math.floor(totalSeconds % 60);
        return String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
    };

    /**
     * Formater un temps en secondes → X min Y s
     * @param {number} totalSeconds - Temps total en secondes
     * @returns {string} Temps formaté en français
     */
    window.formatTimeLong = function(totalSeconds) {
        if (isNaN(totalSeconds) || totalSeconds < 0) return '0 s';
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = Math.floor(totalSeconds % 60);
        if (minutes > 0) {
            return minutes + ' min ' + seconds + ' s';
        }
        return seconds + ' s';
    };

    /**
     * Formater un nombre avec séparateur de milliers
     * @param {number} number - Nombre à formater
     * @returns {string} Nombre formaté
     */
    window.formatNumber = function(number) {
        return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    };

    /**
     * Encoder une chaîne pour URL
     * @param {string} str - Chaîne à encoder
     * @returns {string} Chaîne encodée
     */
    window.encodeURL = function(str) {
        return encodeURIComponent(str).replace(/%20/g, '+');
    };

    /**
     * Détecter si l'appareil est tactile
     * @returns {boolean}
     */
    window.isTouchDevice = function() {
        return ('ontouchstart' in window) || 
               (navigator.maxTouchPoints > 0) || 
               (navigator.msMaxTouchPoints > 0);
    };

    /**
     * Détecter si l'appareil est un mobile
     * @returns {boolean}
     */
    window.isMobile = function() {
        return /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    };

    /**
     * Obtenir la hauteur de la navbar
     * @returns {number} Hauteur en pixels
     */
    window.getNavbarHeight = function() {
        const navbar = document.querySelector('.navbar');
        return navbar ? navbar.offsetHeight : 70;
    };

    /**
     * Afficher une notification toast temporaire
     * @param {string} message - Message à afficher
     * @param {string} type    - Type : 'success', 'error', 'info', 'warning'
     * @param {number} duration - Durée en ms (défaut 4000)
     */
    window.showToast = function(message, type, duration) {
        type = type || 'info';
        duration = duration || 4000;
        
        const toast = document.createElement('div');
        toast.className = 'toast toast-' + type;
        toast.innerHTML = '<i class="fas fa-' + getToastIcon(type) + '"></i> ' + message;
        
        Object.assign(toast.style, {
            position: 'fixed',
            bottom: '30px',
            right: '30px',
            padding: '14px 22px',
            borderRadius: '12px',
            color: 'white',
            fontWeight: '500',
            fontSize: '14px',
            zIndex: '99999',
            opacity: '0',
            transform: 'translateX(100px)',
            transition: 'all 0.4s ease',
            boxShadow: '0 8px 30px rgba(0, 0, 0, 0.2)',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            maxWidth: '400px'
        });
        
        // Couleurs selon le type
        var colors = {
            success: 'linear-gradient(135deg, #27ae60, #2ecc71)',
            error: 'linear-gradient(135deg, #e74c3c, #c0392b)',
            info: 'linear-gradient(135deg, #667eea, #764ba2)',
            warning: 'linear-gradient(135deg, #f39c12, #e67e22)'
        };
        toast.style.background = colors[type] || colors.info;
        
        document.body.appendChild(toast);
        
        // Animation d'entrée
        requestAnimationFrame(function() {
            toast.style.opacity = '1';
            toast.style.transform = 'translateX(0)';
        });
        
        // Auto-dismiss
        setTimeout(function() {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100px)';
            toast.addEventListener('transitionend', function handler() {
                toast.removeEventListener('transitionend', handler);
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            });
        }, duration);
    };

    function getToastIcon(type) {
        var icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            info: 'info-circle',
            warning: 'exclamation-triangle'
        };
        return icons[type] || 'info-circle';
    }

    /**
     * Debounce : limiter la fréquence d'appel d'une fonction
     * @param {Function} func - Fonction à exécuter
     * @param {number} wait   - Délai en ms
     * @returns {Function}
     */
    window.debounce = function(func, wait) {
        var timeout;
        return function() {
            var context = this;
            var args = arguments;
            clearTimeout(timeout);
            timeout = setTimeout(function() {
                func.apply(context, args);
            }, wait);
        };
    };

    /**
     * Throttle : limiter le nombre d'appels par intervalle
     * @param {Function} func - Fonction à exécuter
     * @param {number} limit  - Intervalle en ms
     * @returns {Function}
     */
    window.throttle = function(func, limit) {
        var inThrottle;
        return function() {
            var context = this;
            var args = arguments;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(function() {
                    inThrottle = false;
                }, limit);
            }
        };
    };

    /* ==========================================================
     SECTION 11 : GESTIONNAIRE D'ERREURS GLOBAL
     ========================================================== */
    window.addEventListener('error', function(e) {
        // Logger l'erreur (en production, envoyer à un service de monitoring)
        if (window.console && window.console.error) {
            console.error('Erreur AmijayQuiz:', {
                message: e.message,
                source: e.filename,
                line: e.lineno,
                column: e.colno,
                error: e.error,
                timestamp: new Date().toISOString(),
                url: window.location.href,
                userAgent: navigator.userAgent
            });
        }
        
        // Afficher un message discret à l'utilisateur en cas d'erreur critique
        if (e.error && e.error.critical) {
            window.showToast('Une erreur est survenue. Veuillez réessayer.', 'error', 6000);
        }
    });

    // Gestion des promesses non rejetées
    window.addEventListener('unhandledrejection', function(e) {
        if (window.console && window.console.error) {
            console.error('Promesse non rejetée AmijayQuiz:', {
                reason: e.reason,
                timestamp: new Date().toISOString()
            });
        }
    });

})();