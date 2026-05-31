// ============================================================
// FICHIER : views/assets/js/cours-niveau.js
// RÔLE    : Script de la page niveau Cours AmijayQuiz
//           - Animation des cartes de cours
//           - Effet hover sur les cartes
//           - Animation des barres de progression
//           - Animation du guide d'architecture au scroll
//           - Animation de la banniere Premium
//           - Compteurs animes (chapitres, lecons)
//           - Smooth scroll vers les cours
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var CoursNiveauConfig = {
    animationDuree: 500,
    compteurDuree: 1200
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initCartesCours();
    initBarresProgression();
    initArchitectureGuide();
    initBannierePremium();
    initCompteursStatistiques();
    initScrollToCours();
    initInfoCards();
    console.log('Cours Niveau - Page initialisee');
});

// ============================================================
// ANIMATION DES CARTES DE COURS
// ============================================================
function initCartesCours() {
    var cartes = document.querySelectorAll('.cours-card');

    cartes.forEach(function(carte, index) {
        // Effet hover avance
        carte.addEventListener('mouseenter', function() {
            if (this.classList.contains('termine')) return;

            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 15px 45px rgba(0, 0, 0, 0.1)';

            var icon = this.querySelector('.cours-card-icon');
            if (icon) {
                icon.style.transform = 'scale(1.1) rotate(-5deg)';
            }
        });

        carte.addEventListener('mouseleave', function() {
            if (this.classList.contains('termine')) return;

            this.style.transform = '';
            this.style.boxShadow = '';

            var icon = this.querySelector('.cours-card-icon');
            if (icon) {
                icon.style.transform = '';
            }
        });

        // Effet clic
        carte.addEventListener('mousedown', function() {
            this.style.transform = 'translateY(-2px) scale(0.98)';
        });

        carte.addEventListener('mouseup', function() {
            this.style.transform = 'translateY(-5px)';
        });

        // Animation d'entree au scroll
        animerEntreeCarte(carte, index);
    });
}

function animerEntreeCarte(carte, index) {
    if (!('IntersectionObserver' in window)) return;

    carte.style.opacity = '0';
    carte.style.transform = 'translateY(30px)';
    carte.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting) {
            setTimeout(function() {
                carte.style.opacity = '1';
                carte.style.transform = 'translateY(0)';
            }, 100 + (index * 80));
            observer.disconnect();
        }
    }, { threshold: 0.1, rootMargin: '0px 0px -30px 0px' });

    observer.observe(carte);
}

// ============================================================
// ANIMATION DES BARRES DE PROGRESSION
// ============================================================
function initBarresProgression() {
    var barres = document.querySelectorAll(
        '.cours-progress-fill, ' +
        '.banner-progress-fill, ' +
        '.niveau-progress-fill'
    );

    barres.forEach(function(barre) {
        var largeurFinale = barre.style.width || '0%';

        // Animer de 0 a la valeur finale
        barre.style.width = '0%';
        barre.style.transition = 'width 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94)';

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
// ANIMATION DU GUIDE D'ARCHITECTURE
// ============================================================
function initArchitectureGuide() {
    var niveaux = document.querySelectorAll('.arch-level');

    niveaux.forEach(function(niveau, index) {
        niveau.style.opacity = '0';
        niveau.style.transform = 'translateX(-20px)';
        niveau.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                if (entries[0].isIntersecting) {
                    setTimeout(function() {
                        niveau.style.opacity = '1';
                        niveau.style.transform = 'translateX(0)';
                    }, 150 + (index * 120));
                    observer.disconnect();
                }
            }, { threshold: 0.2 });

            observer.observe(niveau);
        } else {
            setTimeout(function() {
                niveau.style.opacity = '1';
                niveau.style.transform = 'translateX(0)';
            }, 300 + (index * 150));
        }

        // Effet hover
        niveau.addEventListener('mouseenter', function() {
            var numero = this.querySelector('.arch-level-number');
            if (numero) {
                numero.style.transform = 'scale(1.15)';
                numero.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            }
        });

        niveau.addEventListener('mouseleave', function() {
            var numero = this.querySelector('.arch-level-number');
            if (numero) {
                numero.style.transform = 'scale(1)';
            }
        });
    });
}

// ============================================================
// ANIMATION DE LA BANNIERE PREMIUM
// ============================================================
function initBannierePremium() {
    var banniere = document.querySelector('.premium-banner');
    if (!banniere) return;

    // Animation d'entree
    banniere.style.opacity = '0';
    banniere.style.transform = 'translateY(25px)';
    banniere.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';

    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                setTimeout(function() {
                    banniere.style.opacity = '1';
                    banniere.style.transform = 'translateY(0)';
                }, 300);
                observer.disconnect();
            }
        }, { threshold: 0.2 });

        observer.observe(banniere);
    } else {
        setTimeout(function() {
            banniere.style.opacity = '1';
            banniere.style.transform = 'translateY(0)';
        }, 500);
    }

    // Lueur tournante
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
            banniere.style.borderColor = '#e67e22';
            banniere.style.boxShadow = '0 20px 60px rgba(243, 156, 18, 0.2)';
        });

        btn.addEventListener('mouseleave', function() {
            banniere.style.borderColor = '#f39c12';
            banniere.style.boxShadow = '';
        });
    }
}

// ============================================================
// COMPTEURS ANIMES
// ============================================================
function initCompteursStatistiques() {
    var compteurs = document.querySelectorAll(
        '.banner-stat-item strong, ' +
        '.cours-stat strong'
    );

    compteurs.forEach(function(compteur) {
        var texte = compteur.textContent.trim();
        var valeurFinale = parseInt(texte.replace(/[^0-9]/g, '')) || 0;
        if (valeurFinale === 0) return;

        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                animerCompteur(compteur, 0, valeurFinale);
                observer.disconnect();
            }
        }, { threshold: 0.5 });

        observer.observe(compteur);
    });
}

function animerCompteur(element, debut, fin) {
    var duree = CoursNiveauConfig.compteurDuree;
    var debutTimestamp = Date.now();

    function step() {
        var maintenant = Date.now();
        var progression = Math.min((maintenant - debutTimestamp) / duree, 1);
        var eased = 1 - Math.pow(1 - progression, 3);
        var valeurActuelle = Math.floor(debut + (fin - debut) * eased);

        element.textContent = valeurActuelle;

        if (progression < 1) {
            requestAnimationFrame(step);
        } else {
            element.textContent = fin;
        }
    }

    requestAnimationFrame(step);
}

// ============================================================
// SCROLL VERS LES COURS
// ============================================================
function initScrollToCours() {
    var premiereCarte = document.querySelector('.cours-card:not(.termine)');
    if (!premiereCarte) {
        premiereCarte = document.querySelector('.cours-card');
    }

    if (!premiereCarte) return;

    setTimeout(function() {
        var headerHeight = document.querySelector('.navbar') ?
            document.querySelector('.navbar').offsetHeight : 65;

        var position = premiereCarte.getBoundingClientRect().top +
            window.pageYOffset - headerHeight - 80;

        // Ne pas scroller si l'element est deja visible
        if (premiereCarte.getBoundingClientRect().top < window.innerHeight / 2) return;

        window.scrollTo({
            top: position,
            behavior: 'smooth'
        });
    }, 1200);
}

// ============================================================
// ANIMATION DES CARTES D'INFORMATION
// ============================================================
function initInfoCards() {
    var infoCards = document.querySelectorAll('.info-card');

    infoCards.forEach(function(card, index) {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                if (entries[0].isIntersecting) {
                    setTimeout(function() {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0)';
                    }, 100 + (index * 120));
                    observer.disconnect();
                }
            }, { threshold: 0.2 });

            observer.observe(card);
        }
    });
}

// ============================================================
// ANIMATION DU BADGE TERMINE
// ============================================================
(function initBadgeTermineAnimation() {
    var badges = document.querySelectorAll('.cours-card-badge-termine');

    badges.forEach(function(badge) {
        // Effet de brillance periodique
        setInterval(function() {
            badge.style.boxShadow = '0 0 20px rgba(39, 174, 96, 0.6)';
            setTimeout(function() {
                badge.style.boxShadow = '0 4px 10px rgba(39, 174, 96, 0.3)';
            }, 1000);
        }, 3000);
    });
})();

// ============================================================
// TOOLTIP SUR LES BOUTONS
// ============================================================
(function initTooltipsBoutons() {
    var boutons = document.querySelectorAll('.btn-cours-action');

    boutons.forEach(function(bouton) {
        bouton.addEventListener('mouseenter', function() {
            var tooltipText = '';
            if (this.classList.contains('btn-voir-plan')) {
                tooltipText = 'Voir le plan detaille du cours avec tous les modules';
            } else if (this.classList.contains('btn-commencer-cours')) {
                tooltipText = 'Commencer le premier chapitre de ce cours';
            } else if (this.classList.contains('btn-revoir-cours')) {
                tooltipText = 'Revoir les chapitres de ce cours';
            }

            if (tooltipText && !this._tooltip) {
                var tooltip = document.createElement('span');
                tooltip.className = 'btn-tooltip';
                tooltip.textContent = tooltipText;
                tooltip.style.cssText =
                    'position: absolute; bottom: calc(100% + 8px); left: 50%; transform: translateX(-50%); ' +
                    'background: #1a1a2e; color: white; padding: 6px 12px; border-radius: 6px; font-size: 11px; ' +
                    'white-space: nowrap; pointer-events: none; z-index: 100; animation: fadeInUp 0.2s ease;';

                this.style.position = 'relative';
                this.appendChild(tooltip);
                this._tooltip = tooltip;
            }
        });

        bouton.addEventListener('mouseleave', function() {
            if (this._tooltip) {
                this._tooltip.remove();
                this._tooltip = null;
            }
        });
    });
})();

// ============================================================
// MESSAGE DE MOTIVATION DYNAMIQUE
// ============================================================
(function messageMotivationDynamique() {
    var banniere = document.querySelector('.motivation-banner p');
    if (!banniere) return;

    var messages = [
        'Chaque lecon vous rapproche de la maîtrise du sujet.',
        'La pratique reguliere est la cle de la reussite.',
        'Prenez votre temps pour bien comprendre chaque concept.',
        'Les exemples de code sont faits pour etre testes et modifies.'
    ];

    var index = 0;
    setInterval(function() {
        index = (index + 1) % messages.length;

        banniere.style.opacity = '0';
        banniere.style.transition = 'opacity 0.3s ease';

        setTimeout(function() {
            banniere.textContent = messages[index];
            banniere.style.opacity = '1';
        }, 300);
    }, 12000);
})();