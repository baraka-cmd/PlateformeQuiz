// ============================================================
// FICHIER : src/main/webapp/views/assets/js/cours-plan.js
// RÔLE    : Gestion du plan de cours organisé par modules
//           Toggle modules, scroll fluide, progression,
//           animations, retour en haut
// ============================================================

'use strict';

// ===== CONSTANTES =====
const SCROLL_OFFSET = 80;

// ===== VARIABLES =====
let backToTopVisible = false;

// ===== INITIALISATION =====
document.addEventListener('DOMContentLoaded', function() {
    initialiserToggleModules();
    initialiserBackToTop();
    initialiserSmoothScroll();
    ouvrirPremierModuleNonReussi();
    initialiserRaccourcis();
    initialiserAnimations();
});

// ============================================================
// TOGGLE MODULES (OUVRIR/FERMER)
// ============================================================

function toggleModuleChapitres(numero) {
    const content = document.getElementById('moduleContent' + numero);
    const icon = document.getElementById('moduleToggleIcon' + numero);

    if (!content || !icon) return;

    const isOpen = content.style.display !== 'none';

    if (isOpen) {
        content.style.display = 'none';
        icon.classList.remove('fa-chevron-up');
        icon.classList.add('fa-chevron-down');
        sauvegarderEtatModule(numero, false);
    } else {
        content.style.display = 'block';
        icon.classList.remove('fa-chevron-down');
        icon.classList.add('fa-chevron-up');
        sauvegarderEtatModule(numero, true);

        // Scroll fluide vers le contenu du module
        setTimeout(() => {
            content.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }, 150);
    }
}

function ouvrirPremierModuleNonReussi() {
    const modules = document.querySelectorAll('.module-card');

    for (const module of modules) {
        if (module.classList.contains('debloque') && !module.classList.contains('reussi')) {
            const moduleId = module.querySelector('.module-card-content');
            if (moduleId) {
                const id = moduleId.id.replace('moduleContent', '');
                const content = document.getElementById('moduleContent' + id);
                const icon = document.getElementById('moduleToggleIcon' + id);

                if (content && content.style.display === 'none') {
                    content.style.display = 'block';
                    if (icon) {
                        icon.classList.remove('fa-chevron-down');
                        icon.classList.add('fa-chevron-up');
                    }
                }

                // Scroll vers ce module
                setTimeout(() => {
                    module.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }, 300);
                break;
            }
        }
    }
}

// ============================================================
// PERSISTANCE (LOCAL STORAGE)
// ============================================================

function sauvegarderEtatModule(moduleNumero, isOpen) {
    try {
        const courseId = getCourseIdFromURL();
        const key = 'cours_plan_' + courseId + '_modules_open';
        const etat = JSON.parse(localStorage.getItem(key) || '{}');
        etat['module_' + moduleNumero] = isOpen;
        localStorage.setItem(key, JSON.stringify(etat));
    } catch (e) {
        // Silencieux
    }
}

function restaurerEtatModules() {
    try {
        const courseId = getCourseIdFromURL();
        const key = 'cours_plan_' + courseId + '_modules_open';
        const etat = JSON.parse(localStorage.getItem(key) || '{}');

        Object.keys(etat).forEach(key => {
            if (key.startsWith('module_') && etat[key] === true) {
                const moduleNumero = key.replace('module_', '');
                const content = document.getElementById('moduleContent' + moduleNumero);
                const icon = document.getElementById('moduleToggleIcon' + moduleNumero);

                if (content) {
                    content.style.display = 'block';
                    if (icon) {
                        icon.classList.remove('fa-chevron-down');
                        icon.classList.add('fa-chevron-up');
                    }
                }
            }
        });
    } catch (e) {
        // Silencieux
    }
}

// ============================================================
// BOUTON RETOUR EN HAUT
// ============================================================

function initialiserBackToTop() {
    let btn = document.getElementById('btnBackToTop');

    if (!btn) {
        btn = document.createElement('button');
        btn.id = 'btnBackToTop';
        btn.className = 'btn-back-to-top';
        btn.innerHTML = '<i class="fas fa-chevron-up"></i>';
        btn.setAttribute('aria-label', 'Retour en haut de la page');
        btn.setAttribute('title', 'Retour en haut');
        btn.addEventListener('click', scrollToTop);
        document.body.appendChild(btn);
    }

    window.addEventListener('scroll', function() {
        const shouldShow = window.pageYOffset > 500;

        if (shouldShow && !backToTopVisible) {
            btn.classList.add('visible');
            backToTopVisible = true;
        } else if (!shouldShow && backToTopVisible) {
            btn.classList.remove('visible');
            backToTopVisible = false;
        }
    }, { passive: true });
}

function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

// ============================================================
// SMOOTH SCROLL POUR LES ANCRES INTERNES
// ============================================================

function initialiserSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href').substring(1);
            const target = document.getElementById(targetId);

            if (target) {
                e.preventDefault();
                const top = target.getBoundingClientRect().top + window.pageYOffset - SCROLL_OFFSET;
                window.scrollTo({ top: top, behavior: 'smooth' });
            }
        });
    });
}

// ============================================================
// ANIMATIONS AU SCROLL
// ============================================================

function initialiserAnimations() {
    if ('IntersectionObserver' in window) {
        const observerOptions = {
            root: null,
            rootMargin: '0px',
            threshold: 0.1
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        document.querySelectorAll('.module-card').forEach(card => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            observer.observe(card);
        });

        // Déclencher l'animation immédiatement pour les modules visibles
        setTimeout(() => {
            document.querySelectorAll('.module-card').forEach(card => {
                const rect = card.getBoundingClientRect();
                if (rect.top < window.innerHeight) {
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }
            });
        }, 100);
    }
}

// ============================================================
// RACCOURCIS CLAVIER
// ============================================================

function initialiserRaccourcis() {
    document.addEventListener('keydown', function(e) {
        // Ctrl+Home : retour en haut
        if (e.ctrlKey && e.key === 'Home') {
            e.preventDefault();
            scrollToTop();
        }

        // Échap : fermer tous les modules
        if (e.key === 'Escape') {
            document.querySelectorAll('.module-card-content').forEach(content => {
                if (content.style.display === 'block') {
                    const id = content.id.replace('moduleContent', '');
                    toggleModuleChapitres(parseInt(id));
                }
            });
        }
    });
}

// ============================================================
// FONCTIONS UTILITAIRES
// ============================================================

function getCourseIdFromURL() {
    const match = window.location.href.match(/coursId=(\d+)/);
    return match ? match[1] : 'default';
}

// ============================================================
// RESTAURATION DE L'ÉTAT AU CHARGEMENT
// ============================================================

restaurerEtatModules();