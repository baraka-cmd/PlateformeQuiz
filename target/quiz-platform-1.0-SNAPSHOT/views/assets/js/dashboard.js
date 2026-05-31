// ============================================================
// FICHIER : views/assets/js/dashboard.js
// RÔLE    : Script du tableau de bord unifié AmijayQuiz
//           - Animation des compteurs statistiques
//           - Animation des barres de progression
//           - Effet hover sur les cartes thèmes
//           - Gestion du "Voir plus" de thèmes
//           - Animation de la bannière Premium
//           - Actualisation du temps réel (date)
//           - Effet vague sur le header
//           - Animation des tips au scroll
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var DashboardConfig = {
    animationDuree: 800,
    compteurDuree: 1500,
    actualiserDate: true
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initCompteursStatistiques();
    initBarresProgression();
    initCartesThemes();
    initBannierePremium();
    initGuidesAnimation();
    initTipsAnimation();
    initActualisationDate();
    initHeaderWaveEffect();
    console.log('✅ Dashboard - Page initialisée');
});

// ============================================================
// ANIMATION DES COMPTEURS STATISTIQUES
// ============================================================
function initCompteursStatistiques() {
    var compteurs = document.querySelectorAll('.stat-number');

    compteurs.forEach(function(compteur) {
        var texte = compteur.textContent.trim();

        // Ignorer les textes non numériques (ex: "Actif", "Gratuit")
        if (texte === 'Actif' || texte === 'Gratuit' || texte === 'Premium') return;

        // Extraire le nombre
        var match = texte.match(/([\d.]+)/);
        if (!match) return;

        var valeurFinale = parseFloat(match[1]);
        var suffixe = texte.replace(match[1], '');
        var estEntier = !texte.includes('.');

        // Observer pour déclencher l'animation
        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                if (entries[0].isIntersecting) {
                    animerCompteur(compteur, 0, valeurFinale, suffixe, estEntier);
                    observer.disconnect();
                }
            }, { threshold: 0.3 });

            observer.observe(compteur);
        } else {
            animerCompteur(compteur, 0, valeurFinale, suffixe, estEntier);
        }
    });
}

function animerCompteur(element, debut, fin, suffixe, estEntier) {
    var duree = DashboardConfig.compteurDuree;
    var debutTimestamp = Date.now();

    function step() {
        var maintenant = Date.now();
        var progression = Math.min((maintenant - debutTimestamp) / duree, 1);

        // Fonction d'easing ease-out
        var eased = 1 - Math.pow(1 - progression, 3);

        var valeurActuelle;
        if (estEntier) {
            valeurActuelle = Math.floor(debut + (fin - debut) * eased);
        } else {
            valeurActuelle = (debut + (fin - debut) * eased).toFixed(1);
        }

        element.textContent = valeurActuelle + (suffixe || '');

        if (progression < 1) {
            requestAnimationFrame(step);
        } else {
            element.textContent = fin + (suffixe || '');
        }
    }

    requestAnimationFrame(step);
}

// ============================================================
// ANIMATION DES BARRES DE PROGRESSION
// ============================================================
function initBarresProgression() {
    var barres = document.querySelectorAll(
        '.cours-global-progress-fill, ' +
        '.progress-fill, ' +
        '.theme-progress-fill, ' +
        '.progression-bar-inner, ' +
        '.banner-progress-fill'
    );

    barres.forEach(function(barre) {
        var largeurFinale = barre.style.width || '0%';

        // Animer de 0
        barre.style.width = '0%';
        barre.style.transition = 'width 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94)';

        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                if (entries[0].isIntersecting) {
                    setTimeout(function() {
                        barre.style.width = largeurFinale;
                    }, 300);
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
// EFFET HOVER SUR LES CARTES THÈMES
// ============================================================
function initCartesThemes() {
    var cartes = document.querySelectorAll('.theme-card-link');

    cartes.forEach(function(carte) {
        carte.addEventListener('mouseenter', function() {
            var card = this.querySelector('.theme-card');
            if (card) {
                card.style.transform = 'translateY(-5px)';
                card.style.boxShadow = '0 15px 45px rgba(0, 0, 0, 0.1)';
            }
        });

        carte.addEventListener('mouseleave', function() {
            var card = this.querySelector('.theme-card');
            if (card) {
                card.style.transform = '';
                card.style.boxShadow = '';
            }
        });
    });
}

// ============================================================
// ANIMATION BANNIÈRE PREMIUM
// ============================================================
function initBannierePremium() {
    var banniere = document.querySelector('.dashboard-premium-banner');
    if (!banniere) return;

    // Lueur tournante
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

    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting) {
            setTimeout(function() {
                banniere.style.opacity = '1';
                banniere.style.transform = 'translateY(0)';
            }, 200);
            observer.disconnect();
        }
    }, { threshold: 0.2 });

    observer.observe(banniere);

    // Effet hover bouton
    var btn = banniere.querySelector('.premium-banner-btn');
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
// ANIMATION DES GUIDES
// ============================================================
function initGuidesAnimation() {
    var guides = document.querySelectorAll('.guide-card');

    guides.forEach(function(guide, index) {
        guide.style.opacity = '0';
        guide.style.transform = 'translateY(25px)';
        guide.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                setTimeout(function() {
                    guide.style.opacity = '1';
                    guide.style.transform = 'translateY(0)';
                }, 200 + (index * 150));
                observer.disconnect();
            }
        }, { threshold: 0.2 });

        observer.observe(guide);
    });
}

// ============================================================
// ANIMATION DES TIPS (CONSEILS)
// ============================================================
function initTipsAnimation() {
    var tips = document.querySelectorAll('.tip-card');

    tips.forEach(function(tip, index) {
        tip.style.opacity = '0';
        tip.style.transform = 'translateY(20px)';
        tip.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                setTimeout(function() {
                    tip.style.opacity = '1';
                    tip.style.transform = 'translateY(0)';
                }, 100 + (index * 120));
                observer.disconnect();
            }
        }, { threshold: 0.2 });

        observer.observe(tip);
    });
}

// ============================================================
// ACTUALISATION DE LA DATE EN TEMPS RÉEL
// ============================================================
function initActualisationDate() {
    if (!DashboardConfig.actualiserDate) return;

    var dateElement = document.querySelector('.welcome-date');
    if (!dateElement) return;

    // Mettre à jour chaque minute
    setInterval(function() {
        var maintenant = new Date();
        var options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
        var dateFormatee = maintenant.toLocaleDateString('fr-FR', options);

        var icon = dateElement.querySelector('i');
        if (icon) {
            dateElement.innerHTML = '';
            dateElement.appendChild(icon.cloneNode(true));
            dateElement.appendChild(document.createTextNode(' ' + dateFormatee));
        }
    }, 60000);
}

// ============================================================
// EFFET VAGUE SUR LE HEADER
// ============================================================
function initHeaderWaveEffect() {
    var header = document.querySelector('.welcome-header');
    if (!header) return;

    header.addEventListener('mousemove', function(e) {
        var rect = header.getBoundingClientRect();
        var x = e.clientX - rect.left;
        var y = e.clientY - rect.top;

        var pseudo = header.querySelector('::before');
        if (pseudo) {
            var centerX = rect.width / 2;
            var centerY = rect.height / 2;
            var moveX = (x - centerX) / centerX * 10;
            var moveY = (y - centerY) / centerY * 10;

            header.style.setProperty('--mouse-x', moveX + 'px');
            header.style.setProperty('--mouse-y', moveY + 'px');
        }
    });
}

// ============================================================
// ANIMATION DES CARTES STATISTIQUES
// ============================================================
(function initStatsCardsAnimation() {
    var statsCards = document.querySelectorAll('.stat-card');

    statsCards.forEach(function(card, index) {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px) scale(0.95)';
        card.style.transition = 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                setTimeout(function() {
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0) scale(1)';
                }, 50 + (index * 60));
                observer.disconnect();
            }
        }, { threshold: 0.2 });

        observer.observe(card);
    });
})();

// ============================================================
// ANIMATION DU TABLEAU D'ACTIVITÉS
// ============================================================
(function initTableauAnimation() {
    var lignes = document.querySelectorAll('.table-historique tbody tr');

    lignes.forEach(function(ligne, index) {
        ligne.style.opacity = '0';
        ligne.style.transform = 'translateX(-10px)';
        ligne.style.transition = 'all 0.4s ease';

        var observer = new IntersectionObserver(function(entries) {
            if (entries[0].isIntersecting) {
                setTimeout(function() {
                    ligne.style.opacity = '1';
                    ligne.style.transform = 'translateX(0)';
                }, 100 + (index * 80));
                observer.disconnect();
            }
        }, { threshold: 0.1 });

        observer.observe(ligne);
    });
})();

// ============================================================
// BOUTON "VOIR PLUS" ANIMATION
// ============================================================
(function initVoirPlusAnimation() {
    var voirPlusCards = document.querySelectorAll('.voir-plus-card');

    voirPlusCards.forEach(function(card) {
        card.addEventListener('mouseenter', function() {
            var icon = this.querySelector('.voir-plus-icon');
            if (icon) {
                icon.style.transform = 'scale(1.15) rotate(10deg)';
            }
        });

        card.addEventListener('mouseleave', function() {
            var icon = this.querySelector('.voir-plus-icon');
            if (icon) {
                icon.style.transform = '';
            }
        });
    });
})();

// ============================================================
// MESSAGE DE BIENVENUE DYNAMIQUE
// ============================================================
(function messageBienvenueDynamique() {
    var citationElement = document.querySelector('.welcome-quote span');
    if (!citationElement) return;

    var citations = [
        'L\'apprentissage est un trésor qui suivra son propriétaire partout.',
        'La connaissance s\'acquiert par l\'expérience, tout le reste n\'est que de l\'information.',
        'Apprendre sans réfléchir est vain. Réfléchir sans apprendre est dangereux.',
        'Le savoir est le seul bien qui augmente quand on le partage.',
        'L\'éducation est l\'arme la plus puissante pour changer le monde.'
    ];

    var index = 0;
    setInterval(function() {
        index = (index + 1) % citations.length;

        citationElement.style.opacity = '0';
        citationElement.style.transition = 'opacity 0.4s ease';

        setTimeout(function() {
            citationElement.textContent = citations[index];
            citationElement.style.opacity = '1';
        }, 400);
    }, 15000); // Changer toutes les 15 secondes
})();

// ============================================================
// DÉTECTION DU PREMIER PASSAGE (MESSAGE D'ACCUEIL)
// ============================================================
(function messagePremierPassage() {
    var dejaVisite = localStorage.getItem('amijayquiz_dashboard_visite');

    if (!dejaVisite) {
        localStorage.setItem('amijayquiz_dashboard_visite', 'true');

        setTimeout(function() {
            if (typeof AmijayQuiz !== 'undefined' && AmijayQuiz.toast) {
                AmijayQuiz.toast(
                    '👋 Bienvenue sur votre tableau de bord ! Explorez les cours et quiz disponibles.',
                    'info',
                    6000
                );
            }
        }, 2000);
    }
})();

// ============================================================
// RAFRAÎCHISSEMENT DOUX AU RETOUR SUR LA PAGE
// ============================================================
document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
        // La page redevient visible
        var dateElement = document.querySelector('.welcome-date');
        if (dateElement && DashboardConfig.actualiserDate) {
            var maintenant = new Date();
            var options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
            var dateFormatee = maintenant.toLocaleDateString('fr-FR', options);

            var icon = dateElement.querySelector('i');
            if (icon) {
                dateElement.innerHTML = '';
                dateElement.appendChild(icon.cloneNode(true));
                dateElement.appendChild(document.createTextNode(' ' + dateFormatee));
            }
        }
    }
});