// ============================================================
// FICHIER : views/assets/js/cours-plan.js
// RÔLE    : Script du plan de cours détaillé AmijayQuiz
//           - Toggle ouverture/fermeture des modules
//           - Un seul module ouvert à la fois (accordéon)
//           - Scroll automatique vers le module actif
//           - Animation de la barre de progression
//           - Effet hover sur les chapitres
//           - Tooltip sur les modules verrouillés
//           - Smooth scroll vers l'examen final
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var PlanConfig = {
    moduleOuvert: null,
    animationDuration: 300,
    scrollToActif: true
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initModuleToggle();
    initAccordeonModule();
    initProgressionBar();
    initScrollToModuleActif();
    initChapitreHover();
    initTooltipsVerrouilles();
    initBannierePremium();
    initExamenFinalAnimation();
    console.log('✅ Plan Cours - Page initialisée');
});

// ============================================================
// TOGGLE DES MODULES
// ============================================================
function initModuleToggle() {
    var headers = document.querySelectorAll('.module-card-header');

    headers.forEach(function(header) {
        header.addEventListener('click', function() {
            var moduleCard = this.closest('.module-card');
            if (!moduleCard) return;

            // Ne pas toggle si le module est bloqué premium
            if (moduleCard.classList.contains('bloque-premium')) {
                // Rediriger vers la page de paiement
                var contexte = window.location.pathname.split('/')[1] || '';
                window.location.href = '/' + contexte + '/paiement';
                return;
            }

            var numero = extraireNumeroModule(moduleCard);
            toggleModule(numero, moduleCard);
        });

        // Accessibilité : touche Enter/Space
        header.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.click();
            }
        });

        // Rendre les headers focusables
        if (!header.hasAttribute('tabindex')) {
            header.setAttribute('tabindex', '0');
            header.setAttribute('role', 'button');
            header.setAttribute('aria-expanded', 'false');
        }
    });
}

function toggleModule(numero, moduleCard) {
    var content = document.getElementById('moduleContent' + numero);
    var icon = document.getElementById('moduleToggleIcon' + numero);
    var header = moduleCard ? moduleCard.querySelector('.module-card-header') : null;

    if (!content || !icon) return;

    var isOpen = content.style.display !== 'none';

    if (isOpen) {
        // Fermer le module
        fermerModule(content, icon, header);
    } else {
        // Fermer le module précédemment ouvert (accordéon)
        if (PlanConfig.moduleOuvert && PlanConfig.moduleOuvert !== numero) {
            var prevContent = document.getElementById('moduleContent' + PlanConfig.moduleOuvert);
            var prevIcon = document.getElementById('moduleToggleIcon' + PlanConfig.moduleOuvert);
            var prevHeader = document.querySelector('.module-card[data-module="' + PlanConfig.moduleOuvert + '"] .module-card-header');
            
            if (prevContent && prevIcon) {
                fermerModule(prevContent, prevIcon, prevHeader);
            }
        }

        // Ouvrir ce module
        ouvrirModule(content, icon, header);
        PlanConfig.moduleOuvert = numero;
    }
}

function ouvrirModule(content, icon, header) {
    content.style.display = 'block';
    content.style.animation = 'cpSlideDown 0.3s ease';

    icon.classList.remove('fa-chevron-down');
    icon.classList.add('fa-chevron-up');
    icon.style.transform = 'rotate(180deg)';

    if (header) {
        header.setAttribute('aria-expanded', 'true');
    }
}

function fermerModule(content, icon, header) {
    content.style.display = 'none';
    content.style.animation = '';

    icon.classList.remove('fa-chevron-up');
    icon.classList.add('fa-chevron-down');
    icon.style.transform = '';

    if (header) {
        header.setAttribute('aria-expanded', 'false');
    }
}

function extraireNumeroModule(moduleCard) {
    // Essayer de trouver le numéro dans le texte
    var titre = moduleCard.querySelector('.module-card-info h3');
    if (titre) {
        var match = titre.textContent.match(/Module\s+(\d+)/i);
        if (match) return parseInt(match[1]);
    }

    // Essayer avec le numéro affiché
    var numeroElem = moduleCard.querySelector('.module-card-number span');
    if (numeroElem) {
        return parseInt(numeroElem.textContent);
    }

    return 0;
}

// ============================================================
// ACCORDÉON : UN SEUL MODULE OUVERT À LA FOIS
// ============================================================
function initAccordeonModule() {
    // Trouver le module actif (débloqué et non réussi) et l'ouvrir par défaut
    var moduleDebloque = document.querySelector('.module-card.debloque:not(.reussi)');
    if (moduleDebloque) {
        var numero = extraireNumeroModule(moduleDebloque);
        if (numero > 0) {
            var content = document.getElementById('moduleContent' + numero);
            var icon = document.getElementById('moduleToggleIcon' + numero);
            var header = moduleDebloque.querySelector('.module-card-header');

            if (content && icon && content.style.display === 'none') {
                ouvrirModule(content, icon, header);
                PlanConfig.moduleOuvert = numero;
            }
        }
    }
}

// ============================================================
// ANIMATION BARRE DE PROGRESSION
// ============================================================
function initProgressionBar() {
    var barres = document.querySelectorAll('.cours-progress-fill, .niveau-progress-fill, .progression-bar-inner, .banner-progress-fill');

    barres.forEach(function(barre) {
        var largeurFinale = barre.style.width || barre.getAttribute('data-width') || '0%';

        // Animer de 0 à la valeur finale
        barre.style.width = '0%';
        barre.style.transition = 'width 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94)';

        // Observer pour déclencher l'animation quand visible
        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                if (entries[0].isIntersecting) {
                    setTimeout(function() {
                        barre.style.width = largeurFinale;
                    }, 200);
                    observer.disconnect();
                }
            }, { threshold: 0.5 });

            observer.observe(barre);
        } else {
            setTimeout(function() {
                barre.style.width = largeurFinale;
            }, 500);
        }
    });
}

// ============================================================
// SCROLL VERS LE MODULE ACTIF
// ============================================================
function initScrollToModuleActif() {
    if (!PlanConfig.scrollToActif) return;

    // Trouver le premier module débloqué non réussi
    var moduleCible = document.querySelector('.module-card.debloque:not(.reussi)');

    // Si aucun, trouver le premier module réussi
    if (!moduleCible) {
        moduleCible = document.querySelector('.module-card.reussi:last-of-type');
    }

    if (!moduleCible) return;

    setTimeout(function() {
        var headerHeight = document.querySelector('.navbar') ? 
            document.querySelector('.navbar').offsetHeight : 65;

        var position = moduleCible.getBoundingClientRect().top + 
            window.pageYOffset - headerHeight - 60;

        window.scrollTo({
            top: position,
            behavior: 'smooth'
        });

        // Surbrillance temporaire
        moduleCible.style.boxShadow = '0 0 0 6px rgba(102, 126, 234, 0.2)';
        moduleCible.style.transition = 'box-shadow 0.5s ease';
        setTimeout(function() {
            moduleCible.style.boxShadow = '';
        }, 2000);
    }, 800);
}

// ============================================================
// EFFET HOVER SUR LES CHAPITRES
// ============================================================
function initChapitreHover() {
    var chapitres = document.querySelectorAll('.module-chapitre-item');

    chapitres.forEach(function(chapitre) {
        chapitre.addEventListener('mouseenter', function() {
            this.style.background = '#fafbfe';
            this.style.borderRadius = '8px';
            this.style.padding = '12px 14px';
            this.style.transition = 'all 0.2s ease';
        });

        chapitre.addEventListener('mouseleave', function() {
            this.style.background = '';
            this.style.borderRadius = '';
            this.style.padding = '';
        });
    });
}

// ============================================================
// TOOLTIPS SUR MODULES VERROUILLÉS
// ============================================================
function initTooltipsVerrouilles() {
    var modulesVerrouilles = document.querySelectorAll('.module-card.verrouille, .module-card.bloque-premium');

    modulesVerrouilles.forEach(function(module) {
        var header = module.querySelector('.module-card-header');
        if (!header) return;

        var message = '';
        if (module.classList.contains('bloque-premium')) {
            message = 'Ce module nécessite un compte Premium. Cliquez pour débloquer.';
        } else {
            message = 'Réussissez le module précédent pour débloquer celui-ci.';
        }

        header.setAttribute('data-tooltip', message);
    });
}

// ============================================================
// ANIMATION BANNIÈRE PREMIUM
// ============================================================
function initBannierePremium() {
    var banniere = document.querySelector('.premium-banner');
    if (!banniere) return;

    // Observer pour déclencher l'animation
    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                banniere.style.opacity = '0';
                banniere.style.transform = 'translateY(20px)';
                banniere.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';

                setTimeout(function() {
                    banniere.style.opacity = '1';
                    banniere.style.transform = 'translateY(0)';
                }, 100);

                observer.disconnect();
            }
        }, { threshold: 0.2 });

        observer.observe(banniere);
    }

    // Animation de la lueur
    var glow = banniere.querySelector('.premium-banner-glow');
    if (glow) {
        var angle = 0;
        setInterval(function() {
            angle = (angle + 0.5) % 360;
            glow.style.transform = 'rotate(' + angle + 'deg)';
        }, 50);
    }

    // Effet hover sur le bouton
    var btn = banniere.querySelector('.premium-btn');
    if (btn) {
        btn.addEventListener('mouseenter', function() {
            banniere.style.borderColor = '#f39c12';
            banniere.style.boxShadow = '0 15px 50px rgba(243, 156, 18, 0.15)';
        });

        btn.addEventListener('mouseleave', function() {
            banniere.style.borderColor = '';
            banniere.style.boxShadow = '';
        });
    }
}

// ============================================================
// ANIMATION EXAMEN FINAL
// ============================================================
function initExamenFinalAnimation() {
    var examenCard = document.querySelector('.examen-final-card');
    if (!examenCard) return;

    // Effet de pulsation si accessible
    if (examenCard.classList.contains('accessible')) {
        var pulseInterval = setInterval(function() {
            examenCard.style.boxShadow = '0 0 30px rgba(102, 126, 234, 0.3)';
            setTimeout(function() {
                examenCard.style.boxShadow = '';
            }, 1200);
        }, 2500);
    }

    // Animation d'entrée
    examenCard.style.opacity = '0';
    examenCard.style.transform = 'translateY(20px)';
    examenCard.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';

    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting) {
            setTimeout(function() {
                examenCard.style.opacity = '1';
                examenCard.style.transform = 'translateY(0)';
            }, 200);
            observer.disconnect();
        }
    }, { threshold: 0.3 });

    observer.observe(examenCard);
}

// ============================================================
// COMPTEUR DE LEÇONS (ANIMATION)
// ============================================================
(function initCompteurLecons() {
    var compteurElements = document.querySelectorAll('.plan-header-statut .stat-number, .cours-progress-detail strong');

    compteurElements.forEach(function(element) {
        var texte = element.textContent;
        var valeurFinale = parseInt(texte.replace(/[^0-9]/g, '')) || 0;
        if (valeurFinale === 0) return;

        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                animerCompteur(element, valeurFinale);
                observer.disconnect();
            }
        }, { threshold: 0.5 });

        observer.observe(element);
    });

    function animerCompteur(element, valeurFinale) {
        var duree = 1000;
        var debut = Date.now();

        function step() {
            var maintenant = Date.now();
            var progression = Math.min((maintenant - debut) / duree, 1);
            var valeurActuelle = Math.floor(progression * valeurFinale);

            element.textContent = valeurActuelle;

            if (progression < 1) {
                requestAnimationFrame(step);
            } else {
                element.textContent = valeurFinale;
            }
        }

        requestAnimationFrame(step);
    }
})();

// ============================================================
// CONFIRMATION AVANT DE QUITTER L'EXAMEN
// ============================================================
(function confirmationQuitter() {
    var liensExamen = document.querySelectorAll('a[href*="examen-module"], a[href*="examen?"]');

    liensExamen.forEach(function(lien) {
        lien.addEventListener('click', function(e) {
            var message = 'Vous allez être redirigé vers l\'examen. Continuer ?';
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });
})();