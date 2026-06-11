// ============================================================
// FICHIER : views/assets/js/cours-chapitre.js
// RÔLE    : Script de la page chapitre de cours LearnPath
//           - Animation des cartes de lecons
//           - Effet hover sur les lecons
//           - Indicateur visuel de progression
//           - Animation des icones d'etat
//           - Tooltip sur les lecons verrouillees
//           - Smooth scroll vers la lecon active
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var ChapitreConfig = {
    scrollToActive: true,
    animationDuree: 400
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initLeconCards();
    initScrollToActiveLecon();
    initTooltipsLeconsBloquees();
    initAnimationIcones();
    initProgressionVisuelle();
    console.log('Cours Chapitre - Page initialisee');
});

// ============================================================
// ANIMATION DES CARTES DE LECONS
// ============================================================
function initLeconCards() {
    var cartes = document.querySelectorAll('.lecon-item');

    cartes.forEach(function(carte, index) {
        // Effet hover
        carte.addEventListener('mouseenter', function() {
            if (this.classList.contains('terminee')) return;

            this.style.transform = 'translateY(-3px)';
            this.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.08)';

            var numero = this.querySelector('.lecon-numero');
            if (numero && !this.classList.contains('active')) {
                numero.style.transform = 'scale(1.1)';
                numero.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            }
        });

        carte.addEventListener('mouseleave', function() {
            if (this.classList.contains('terminee')) return;

            this.style.transform = '';
            this.style.boxShadow = '';

            var numero = this.querySelector('.lecon-numero');
            if (numero && !this.classList.contains('active')) {
                numero.style.transform = 'scale(1)';
            }
        });

        // Effet clic
        carte.addEventListener('mousedown', function() {
            this.style.transform = 'translateY(-1px) scale(0.99)';
        });

        carte.addEventListener('mouseup', function() {
            this.style.transform = 'translateY(-3px)';
        });

        // Animation d'entree au scroll
        animerEntreeLecon(carte, index);
    });
}

function animerEntreeLecon(carte, index) {
    if (!('IntersectionObserver' in window)) {
        carte.style.opacity = '1';
        return;
    }

    carte.style.opacity = '0';
    carte.style.transform = 'translateY(20px)';
    carte.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';

    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting) {
            setTimeout(function() {
                carte.style.opacity = '1';
                carte.style.transform = 'translateY(0)';
            }, 80 + (index * 60));
            observer.disconnect();
        }
    }, { threshold: 0.1, rootMargin: '0px 0px -20px 0px' });

    observer.observe(carte);
}

// ============================================================
// SCROLL VERS LA LECON ACTIVE
// ============================================================
function initScrollToActiveLecon() {
    if (!ChapitreConfig.scrollToActive) return;

    var leconActive = document.querySelector('.lecon-item.active');
    if (!leconActive) {
        leconActive = document.querySelector('.lecon-item:not(.terminee)');
    }

    if (!leconActive) return;

    setTimeout(function() {
        var headerHeight = document.querySelector('.navbar') ?
            document.querySelector('.navbar').offsetHeight : 65;

        var position = leconActive.getBoundingClientRect().top +
            window.pageYOffset - headerHeight - 100;

        // Ne pas scroller si l'element est deja bien visible
        var rect = leconActive.getBoundingClientRect();
        if (rect.top > 100 && rect.top < window.innerHeight - 150) return;

        window.scrollTo({
            top: position,
            behavior: 'smooth'
        });

        // Effet de surbrillance temporaire
        leconActive.style.boxShadow = '0 0 0 6px rgba(102, 126, 234, 0.15)';
        leconActive.style.transition = 'box-shadow 0.5s ease';
        setTimeout(function() {
            leconActive.style.boxShadow = '';
        }, 2000);
    }, 600);
}

// ============================================================
// TOOLTIPS SUR LES LECONS BLOQUEES
// ============================================================
function initTooltipsLeconsBloquees() {
    var leconsBloquees = document.querySelectorAll('.lecon-acces-btn.disabled');

    leconsBloquees.forEach(function(bouton) {
        var leconItem = bouton.closest('.lecon-item');
        if (!leconItem) return;

        var titre = '';
        var titreElem = leconItem.querySelector('.lecon-info h3');
        if (titreElem) {
            titre = titreElem.textContent.trim();
        }

        bouton.setAttribute('data-tooltip', 'Lecon non accessible pour le moment');
        bouton.setAttribute('aria-label', 'Lecon verrouillee : ' + titre);

        // Afficher un petit message au clic
        bouton.addEventListener('click', function(e) {
            e.preventDefault();

            var message = document.createElement('div');
            message.className = 'lecon-lock-message';
            message.textContent = 'Terminez la lecon precedente pour debloquer celle-ci';
            message.style.cssText =
                'position: absolute; top: -45px; left: 50%; transform: translateX(-50%); ' +
                'background: #1a1a2e; color: white; padding: 8px 14px; border-radius: 8px; ' +
                'font-size: 11px; white-space: nowrap; z-index: 10; pointer-events: none; ' +
                'animation: fadeInUp 0.3s ease; box-shadow: 0 4px 12px rgba(0,0,0,0.15);';

            // Ajouter une fleche
            var arrow = document.createElement('span');
            arrow.style.cssText =
                'position: absolute; bottom: -5px; left: 50%; transform: translateX(-50%); ' +
                'width: 0; height: 0; border-left: 6px solid transparent; ' +
                'border-right: 6px solid transparent; border-top: 6px solid #1a1a2e;';
            message.appendChild(arrow);

            this.style.position = 'relative';
            this.appendChild(message);

            setTimeout(function() {
                if (message.parentNode) {
                    message.remove();
                }
            }, 2500);
        });
    });
}

// ============================================================
// ANIMATION DES ICONES D'ETAT
// ============================================================
function initAnimationIcones() {
    // Animation de la coche verte (lecon terminee)
    var iconesCheck = document.querySelectorAll('.lecon-item.terminee .lecon-numero i');

    iconesCheck.forEach(function(icone) {
        // Effet de rebond periodique
        setInterval(function() {
            icone.style.transform = 'scale(1.3)';
            icone.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

            setTimeout(function() {
                icone.style.transform = 'scale(1)';
            }, 300);
        }, 4000);
    });

    // Animation de l'icone play (lecon active)
    var iconesPlay = document.querySelectorAll('.lecon-item.active .lecon-numero i');

    iconesPlay.forEach(function(icone) {
        // Effet de pulsation
        setInterval(function() {
            icone.style.opacity = '0.6';
            icone.style.transition = 'opacity 0.5s ease';

            setTimeout(function() {
                icone.style.opacity = '1';
            }, 500);
        }, 1500);
    });
}

// ============================================================
// INDICATEUR VISUEL DE PROGRESSION
// ============================================================
function initProgressionVisuelle() {
    var totalLecons = document.querySelectorAll('.lecon-item').length;
    var leconsTerminees = document.querySelectorAll('.lecon-item.terminee').length;

    if (totalLecons === 0) return;

    // Ajouter une barre de progression subtile sous le compteur
    var compteurElem = document.querySelector('.chapitre-lecon-count');
    if (!compteurElem) return;

    var progressionBar = document.createElement('div');
    progressionBar.className = 'chapitre-mini-progress';
    progressionBar.style.cssText =
        'height: 4px; background: #e8eaef; border-radius: 4px; margin-top: 8px; overflow: hidden; ' +
        'width: 100%;';

    var progressionFill = document.createElement('div');
    progressionFill.className = 'chapitre-mini-progress-fill';
    var pourcentage = Math.round((leconsTerminees / totalLecons) * 100);
    progressionFill.style.cssText =
        'height: 100%; background: linear-gradient(90deg, #667eea, #764ba2); border-radius: 4px; ' +
        'width: 0%; transition: width 1s cubic-bezier(0.25, 0.46, 0.45, 0.94);';

    progressionBar.appendChild(progressionFill);
    compteurElem.style.display = 'block';
    compteurElem.appendChild(progressionBar);

    // Animer la barre
    setTimeout(function() {
        progressionFill.style.width = pourcentage + '%';
    }, 300);

    // Mettre a jour le texte du compteur
    compteurElem.innerHTML = '<i class="fas fa-file-alt"></i> ' +
        leconsTerminees + ' / ' + totalLecons + ' lecons terminees';
    compteurElem.appendChild(progressionBar);
}

// ============================================================
// ANIMATION DES OBJECTIFS
// ============================================================
(function initObjectifsAnimation() {
    var objectifs = document.querySelector('.chapitre-objectifs');
    if (!objectifs) return;

    objectifs.style.opacity = '0';
    objectifs.style.transform = 'translateY(10px)';
    objectifs.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                setTimeout(function() {
                    objectifs.style.opacity = '1';
                    objectifs.style.transform = 'translateY(0)';
                }, 400);
                observer.disconnect();
            }
        }, { threshold: 0.3 });

        observer.observe(objectifs);
    } else {
        setTimeout(function() {
            objectifs.style.opacity = '1';
            objectifs.style.transform = 'translateY(0)';
        }, 500);
    }
})();

// ============================================================
// NAVIGATION CLAVIER (ACCESSIBILITE)
// ============================================================
(function initKeyboardNavigation() {
    var lecons = document.querySelectorAll('.lecon-acces-btn:not(.disabled)');

    lecons.forEach(function(bouton, index) {
        bouton.setAttribute('tabindex', '0');

        bouton.addEventListener('keydown', function(e) {
            if (e.key === 'ArrowDown' || e.key === 'ArrowRight') {
                e.preventDefault();
                var next = lecons[index + 1];
                if (next) next.focus();
            }

            if (e.key === 'ArrowUp' || e.key === 'ArrowLeft') {
                e.preventDefault();
                var prev = lecons[index - 1];
                if (prev) prev.focus();
            }
        });
    });
})();

// ============================================================
// CONFIRMATION AVANT DE QUITTER SI LECON ACTIVE
// ============================================================
(function confirmationQuitter() {
    var leconActive = document.querySelector('.lecon-item.active .lecon-acces-btn');
    if (!leconActive) return;

    // Ajouter un indicateur visuel
    leconActive.style.animation = 'chPulseBtn 2s ease-in-out infinite';

    // Injecter l'animation
    if (!document.getElementById('pulse-btn-style')) {
        var style = document.createElement('style');
        style.id = 'pulse-btn-style';
        style.textContent =
            '@keyframes chPulseBtn {' +
            '  0%, 100% { box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2); }' +
            '  50% { box-shadow: 0 4px 25px rgba(102, 126, 234, 0.5); }' +
            '}';
        document.head.appendChild(style);
    }
})();

// ============================================================
// ANIMATION DU TITRE DU CHAPITRE
// ============================================================
(function initTitreAnimation() {
    var titre = document.querySelector('.chapitre-header h1');
    if (!titre) return;

    // Effet de soulignement progressif
    var soulignement = document.createElement('span');
    soulignement.className = 'titre-underline';
    soulignement.style.cssText =
        'display: block; width: 0; height: 3px; background: var(--ch-gradient, linear-gradient(135deg, #667eea, #764ba2)); ' +
        'border-radius: 3px; margin-top: 6px; transition: width 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);';

    titre.appendChild(soulignement);

    setTimeout(function() {
        soulignement.style.width = '60px';
    }, 500);
})();