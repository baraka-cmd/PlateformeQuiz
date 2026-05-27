// ============================================================
// FICHIER : src/main/webapp/views/assets/js/cours-navigation.js
// RÔLE    : Gestion de la barre latérale de navigation
//           Ouverture/fermeture des modules et chapitres
//           Un seul module ouvert à la fois
//           Responsive mobile
// ============================================================

'use strict';

// ===== VARIABLES =====
var currentOpenModule = null;
var isMobile = false;

// ===== INITIALISATION =====
document.addEventListener('DOMContentLoaded', function() {
    var sidebar = document.getElementById('coursSidebar');
    if (!sidebar) {
        return;
    }

    isMobile = window.innerWidth < 1024;

    initialiserModuleActif();
    initialiserMobile();
    initialiserRaccourcis();
    scrollToActiveElement();
});

// ============================================================
// GESTION DES MODULES
// ============================================================

function toggleModule(numero) {
    var moduleContent = document.getElementById('moduleContent' + numero);
    var moduleArrow = document.getElementById('moduleArrow' + numero);
    var moduleEl = document.querySelector('.sidebar-module[data-module="' + numero + '"]');

    if (!moduleContent || !moduleArrow) {
        return;
    }

    var isCurrentlyOpen = moduleContent.style.display === 'block';

    // Fermer le module actuellement ouvert
    if (currentOpenModule !== null && currentOpenModule !== numero) {
        fermerModule(currentOpenModule);
    }

    if (isCurrentlyOpen) {
        moduleContent.style.display = 'none';
        moduleArrow.classList.remove('fa-chevron-up');
        moduleArrow.classList.add('fa-chevron-down');
        currentOpenModule = null;
    } else {
        moduleContent.style.display = 'block';
        moduleArrow.classList.remove('fa-chevron-down');
        moduleArrow.classList.add('fa-chevron-up');
        fermerTousLesChapitres(numero);
        currentOpenModule = numero;

        if (moduleEl) {
            setTimeout(function() {
                moduleEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }, 100);
        }
    }

    sauvegarderModuleOuvert();
}

function fermerModule(numero) {
    var content = document.getElementById('moduleContent' + numero);
    var arrow = document.getElementById('moduleArrow' + numero);

    if (content) {
        content.style.display = 'none';
    }
    if (arrow) {
        arrow.classList.remove('fa-chevron-up');
        arrow.classList.add('fa-chevron-down');
    }

    fermerTousLesChapitres(numero);
}

function fermerTousLesChapitres(moduleNumero) {
    var moduleContent = document.getElementById('moduleContent' + moduleNumero);
    if (!moduleContent) {
        return;
    }

    var chapitreContents = moduleContent.querySelectorAll('.sidebar-lecons');
    var chapitreArrows = moduleContent.querySelectorAll('.chapitre-arrow');

    for (var i = 0; i < chapitreContents.length; i++) {
        chapitreContents[i].style.display = 'none';
    }

    for (var j = 0; j < chapitreArrows.length; j++) {
        chapitreArrows[j].classList.remove('fa-chevron-up');
        chapitreArrows[j].classList.add('fa-chevron-down');
    }
}

// ============================================================
// GESTION DES CHAPITRES
// ============================================================

function toggleChapitre(header, event) {
    if (event) {
        event.stopPropagation();
    }

    var leconsList = header.nextElementSibling;
    var arrow = header.querySelector('.chapitre-arrow');

    if (!leconsList || !arrow) {
        return;
    }

    var moduleEl = header.closest('.sidebar-module');
    var isLocked = moduleEl && moduleEl.classList.contains('verrouille');

    if (leconsList.style.display === 'block') {
        leconsList.style.display = 'none';
        arrow.classList.remove('fa-chevron-up');
        arrow.classList.add('fa-chevron-down');
    } else {
        leconsList.style.display = 'block';
        arrow.classList.remove('fa-chevron-down');
        arrow.classList.add('fa-chevron-up');

        if (!isLocked) {
            setTimeout(function() {
                header.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }, 100);
        }
    }
}

// ============================================================
// INITIALISATION DU MODULE ACTIF
// ============================================================

function initialiserModuleActif() {
    var activeLecon = document.querySelector('.sidebar-lecon.active');
    var activeChapitre = document.querySelector('.sidebar-chapitre-header.active');

    var moduleEl = null;
    if (activeLecon) {
        moduleEl = activeLecon.closest('.sidebar-module');
    } else if (activeChapitre) {
        moduleEl = activeChapitre.closest('.sidebar-module');
    }

    if (moduleEl) {
        var numero = parseInt(moduleEl.getAttribute('data-module'));
        if (numero) {
            var content = document.getElementById('moduleContent' + numero);
            var arrow = document.getElementById('moduleArrow' + numero);

            if (content) {
                content.style.display = 'block';
                currentOpenModule = numero;
            }
            if (arrow) {
                arrow.classList.remove('fa-chevron-down');
                arrow.classList.add('fa-chevron-up');
            }

            // Ouvrir le chapitre parent
            var chapitreHeader = null;
            if (activeLecon) {
                chapitreHeader = activeLecon.closest('.sidebar-chapitre').querySelector('.sidebar-chapitre-header');
            } else if (activeChapitre) {
                chapitreHeader = activeChapitre;
            }

            if (chapitreHeader) {
                var leconsList = chapitreHeader.nextElementSibling;
                var chapArrow = chapitreHeader.querySelector('.chapitre-arrow');
                if (leconsList) {
                    leconsList.style.display = 'block';
                }
                if (chapArrow) {
                    chapArrow.classList.remove('fa-chevron-down');
                    chapArrow.classList.add('fa-chevron-up');
                }
            }
        }
    }

    sauvegarderModuleOuvert();
}

// ============================================================
// SCROLL VERS L'ÉLÉMENT ACTIF
// ============================================================

function scrollToActiveElement() {
    var activeElement = document.querySelector('.sidebar-lecon.active, .sidebar-chapitre-header.active');
    if (activeElement) {
        setTimeout(function() {
            activeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }, 400);
    }
}

// ============================================================
// PERSISTANCE
// ============================================================

function sauvegarderModuleOuvert() {
    try {
        var courseId = getCourseId();
        var key = 'cours_sidebar_open_' + courseId;
        localStorage.setItem(key, currentOpenModule || 0);
    } catch (e) {
        // Silencieux
    }
}

function getCourseId() {
    if (typeof COURS_ID !== 'undefined') {
        return COURS_ID;
    }
    var match = window.location.href.match(/coursId=(\d+)/);
    return match ? match[1] : 'default';
}

// ============================================================
// MOBILE
// ============================================================

function initialiserMobile() {
    window.addEventListener('resize', function() {
        isMobile = window.innerWidth < 1024;
    });

    var links = document.querySelectorAll('.sidebar-lecon-link:not(.disabled)');
    for (var i = 0; i < links.length; i++) {
        links[i].addEventListener('click', function() {
            if (isMobile) {
                fermerMobileSidebar();
            }
        });
    }
}

function toggleMobileSidebar() {
    var sidebar = document.getElementById('coursSidebar');
    var overlay = document.getElementById('sidebarOverlay');

    if (!sidebar) {
        return;
    }

    var isOpen = sidebar.classList.contains('mobile-open');

    if (isOpen) {
        fermerMobileSidebar();
    } else {
        sidebar.classList.add('mobile-open');
        if (overlay) {
            overlay.classList.add('active');
        }
        document.body.style.overflow = 'hidden';
    }
}

function fermerMobileSidebar() {
    var sidebar = document.getElementById('coursSidebar');
    var overlay = document.getElementById('sidebarOverlay');

    if (sidebar) {
        sidebar.classList.remove('mobile-open');
    }
    if (overlay) {
        overlay.classList.remove('active');
    }
    document.body.style.overflow = '';
}

// ============================================================
// RACCOURCIS CLAVIER
// ============================================================

function initialiserRaccourcis() {
    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 'b') {
            e.preventDefault();
            if (isMobile) {
                toggleMobileSidebar();
            }
        }

        if (e.key === 'Escape' && isMobile) {
            fermerMobileSidebar();
        }
    });
}