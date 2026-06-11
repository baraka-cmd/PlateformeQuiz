// ============================================================
// FICHIER : views/assets/js/niveau.js
// RÔLE    : Script de la page niveau Quiz LearnPath
//           - Animation des cartes de parties
//           - Effet hover sur les cartes
//           - Indication visuelle "prochaine partie"
//           - Tooltip sur les parties bloquées
//           - Animation de la barre de progression
//           - Scroll fluide vers la prochaine partie
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var NiveauConfig = {
    scrollToProchaine: true,
    animationDuration: 300
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initPartieCards();
    initProgressionBar();
    initScrollToProchaine();
    initBannierePremium();
    initAlerteAnimation();
    console.log('✅ Niveau Quiz - Page initialisée');
});

// ============================================================
// ANIMATION DES CARTES DE PARTIES
// ============================================================
function initPartieCards() {
    var cartes = document.querySelectorAll('.partie-card');

    cartes.forEach(function(carte, index) {
        // Ajouter un délai d'animation progressif si pas déjà défini
        if (!carte.style.animationDelay) {
            carte.style.animationDelay = (0.05 * index) + 's';
        }

        // Effet hover avancé
        carte.addEventListener('mouseenter', function() {
            if (this.classList.contains('bloquee') || this.classList.contains('bloque-premium')) {
                return; // Pas d'effet hover sur les cartes bloquées
            }

            this.style.transform = 'translateY(-6px)';
            this.style.boxShadow = '0 15px 40px rgba(0, 0, 0, 0.12)';
            this.style.borderColor = '#667eea';
        });

        carte.addEventListener('mouseleave', function() {
            if (this.classList.contains('bloquee') || this.classList.contains('bloque-premium')) {
                return;
            }

            this.style.transform = '';
            this.style.boxShadow = '';
            this.style.borderColor = '';
        });

        // Effet clic
        carte.addEventListener('mousedown', function() {
            if (this.classList.contains('prochaine')) {
                this.style.transform = 'translateY(-2px) scale(0.98)';
            }
        });

        carte.addEventListener('mouseup', function() {
            if (this.classList.contains('prochaine')) {
                this.style.transform = 'translateY(-6px)';
            }
        });

        // Tooltip sur les cartes bloquées Premium
        if (carte.classList.contains('bloque-premium')) {
            carte.setAttribute('data-tooltip', 'Cette partie nécessite un compte Premium');
            carte.style.cursor = 'default';
        }

        // Badge "Prochaine" clignotement
        if (carte.classList.contains('prochaine')) {
            var badge = carte.querySelector('.partie-card-badge');
            if (badge) {
                setInterval(function() {
                    badge.style.opacity = badge.style.opacity === '0.7' ? '1' : '0.7';
                }, 1000);
            }
        }
    });
}

// ============================================================
// ANIMATION DE LA BARRE DE PROGRESSION
// ============================================================
function initProgressionBar() {
    var barre = document.querySelector('.progression-bar-inner');
    if (!barre) return;

    // Animer la barre de 0 à sa valeur actuelle
    var largeurFinale = barre.style.width;
    barre.style.width = '0%';

    setTimeout(function() {
        barre.style.width = largeurFinale;
    }, 500);

    // Effet de brillance périodique
    setInterval(function() {
        barre.style.boxShadow = '0 0 15px rgba(102, 126, 234, 0.4)';
        setTimeout(function() {
            barre.style.boxShadow = '';
        }, 1000);
    }, 3000);
}

// ============================================================
// SCROLL VERS LA PROCHAINE PARTIE
// ============================================================
function initScrollToProchaine() {
    if (!NiveauConfig.scrollToProchaine) return;

    var prochaineCarte = document.querySelector('.partie-card.prochaine');
    if (!prochaineCarte) return;

    // Ajouter un indicateur visuel
    prochaineCarte.style.transition = 'all 0.5s ease';

    // Scroller après un court délai (le temps que la page charge)
    setTimeout(function() {
        var headerHeight = document.querySelector('.navbar') ? 
            document.querySelector('.navbar').offsetHeight : 65;

        var position = prochaineCarte.getBoundingClientRect().top + 
            window.pageYOffset - headerHeight - 80;

        window.scrollTo({
            top: position,
            behavior: 'smooth'
        });

        // Effet de surbrillance temporaire
        prochaineCarte.style.boxShadow = '0 0 0 8px rgba(102, 126, 234, 0.3)';
        setTimeout(function() {
            prochaineCarte.style.boxShadow = '';
        }, 2000);
    }, 1000);
}

// ============================================================
// ANIMATION DE LA BANNIÈRE PREMIUM
// ============================================================
function initBannierePremium() {
    var banniere = document.querySelector('.premium-banner');
    if (!banniere) return;

    // Effet de lueur tournante
    var glow = banniere.querySelector('.premium-banner-glow');
    if (glow) {
        var angle = 0;
        setInterval(function() {
            angle = (angle + 0.5) % 360;
            glow.style.transform = 'rotate(' + angle + 'deg)';
        }, 50);
    }

    // Animation d'entrée
    banniere.style.opacity = '0';
    banniere.style.transform = 'translateY(20px)';
    banniere.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';

    setTimeout(function() {
        banniere.style.opacity = '1';
        banniere.style.transform = 'translateY(0)';
    }, 800);
}

// ============================================================
// ANIMATION DES ALERTES
// ============================================================
function initAlerteAnimation() {
    var alertes = document.querySelectorAll('.alerte-info');

    alertes.forEach(function(alerte, index) {
        alerte.style.opacity = '0';
        alerte.style.transform = 'translateY(15px)';
        alerte.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

        setTimeout(function() {
            alerte.style.opacity = '1';
            alerte.style.transform = 'translateY(0)';
        }, 400 + (index * 150));
    });
}

// ============================================================
// ANIMATION DE LA GRILLE AU SCROLL
// ============================================================
(function initScrollAnimation() {
    if (!('IntersectionObserver' in window)) return;

    var observer = new IntersectionObserver(function(entries) {
        entries.forEach(function(entry) {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    // Observer les cartes de partie
    var cartes = document.querySelectorAll('.partie-card');
    cartes.forEach(function(carte) {
        observer.observe(carte);
    });
})();

// ============================================================
// COMPTEUR DE PARTIES RÉUSSIES (ANIMATION)
// ============================================================
(function initCompteurParties() {
    var compteurElement = document.querySelector('.niveau-header-text p strong');
    if (!compteurElement) return;

    var valeurFinale = parseInt(compteurElement.textContent) || 0;
    var duree = 1000; // 1 seconde
    var debut = Date.now();

    function animerCompteur() {
        var maintenant = Date.now();
        var progression = Math.min((maintenant - debut) / duree, 1);
        var valeurActuelle = Math.floor(progression * valeurFinale);

        compteurElement.textContent = valeurActuelle;

        if (progression < 1) {
            requestAnimationFrame(animerCompteur);
        }
    }

    // Démarrer l'animation quand l'élément est visible
    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting) {
            animerCompteur();
            observer.disconnect();
        }
    });
    observer.observe(compteurElement);
})();

// ============================================================
// CONFETTIS LÉGERS SI NIVEAU TERMINÉ
// ============================================================
(function initConfettisNiveauTermine() {
    var alerteTermine = document.querySelector('.alerte-termine');
    if (!alerteTermine) return;

    // Créer quelques confettis légers
    var emojis = ['🎉', '🏆', '⭐', '✨'];

    for (var i = 0; i < 8; i++) {
        var confetti = document.createElement('span');
        confetti.textContent = emojis[Math.floor(Math.random() * emojis.length)];
        confetti.style.cssText = 
            'position: absolute; font-size: 20px; pointer-events: none; ' +
            'animation: confettiFall ' + (2 + Math.random() * 2) + 's ease-out forwards; ' +
            'animation-delay: ' + (Math.random() * 1.5) + 's; ' +
            'left: ' + (Math.random() * 80 + 10) + '%; top: -20px; opacity: 0;';

        alerteTermine.style.position = 'relative';
        alerteTermine.style.overflow = 'hidden';
        alerteTermine.appendChild(confetti);
    }

    // Injecter l'animation
    if (!document.getElementById('confetti-fall-style')) {
        var style = document.createElement('style');
        style.id = 'confetti-fall-style';
        style.textContent = 
            '@keyframes confettiFall {' +
            '  0% { opacity: 1; transform: translateY(0) rotate(0deg); }' +
            '  100% { opacity: 0; transform: translateY(100px) rotate(360deg); }' +
            '}';
        document.head.appendChild(style);
    }
})();

// ============================================================
// NOTIFICATION SI PARTIES BLOQUÉES
// ============================================================
(function notificationPartiesBloquees() {
    var ctaPremium = document.querySelector('.premium-cta-card');
    if (!ctaPremium) return;

    // Observer quand le CTA devient visible
    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting) {
            // Animation d'entrée
            ctaPremium.style.opacity = '0';
            ctaPremium.style.transform = 'translateY(20px)';
            ctaPremium.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

            setTimeout(function() {
                ctaPremium.style.opacity = '1';
                ctaPremium.style.transform = 'translateY(0)';
            }, 200);

            observer.disconnect();
        }
    }, { threshold: 0.3 });

    observer.observe(ctaPremium);
})();