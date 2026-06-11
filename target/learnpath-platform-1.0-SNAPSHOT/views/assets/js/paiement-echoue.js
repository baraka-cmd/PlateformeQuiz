// ============================================================
// FICHIER : views/assets/js/paiement-echoue.js
// RÔLE    : Script de la page d'échec de paiement
//           - Animation du cercle d'échec
//           - Suggestions dynamiques selon la cause
//           - Bouton réessayer avec effet visuel
//           - Copie de la référence pour le support
//           - Animations de consolation
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var EchecConfig = {
    reference: extraireReferenceUrl(),
    causes: [
        {
            id: 'solde',
            icon: 'fa-wallet',
            titre: 'Solde insuffisant',
            conseil: 'Vérifiez que votre compte Mobile Money a au moins 4$ ou 10 000 FC.'
        },
        {
            id: 'annulation',
            icon: 'fa-ban',
            titre: 'Paiement annulé',
            conseil: 'Vous avez peut-être annulé la transaction. Réessayez et validez sur votre téléphone.'
        },
        {
            id: 'delai',
            icon: 'fa-clock',
            titre: 'Délai expiré',
            conseil: 'La transaction a pris trop de temps. Relancez un nouveau paiement.'
        },
        {
            id: 'reseau',
            icon: 'fa-wifi',
            titre: 'Problème réseau',
            conseil: 'Vérifiez votre connexion internet et celle de votre téléphone.'
        }
    ]
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initAnimationEchec();
    initCausesAnimation();
    initBoutonReessayer();
    initCopieReference();
    initSupportContact();
    console.log('✅ Paiement Échec - Page initialisée');
});

// ============================================================
// EXTRAIRE LA RÉFÉRENCE DE L'URL
// ============================================================
function extraireReferenceUrl() {
    var params = new URLSearchParams(window.location.search);
    return params.get('reference') || '';
}

// ============================================================
// ANIMATION DU CERCLE D'ÉCHEC
// ============================================================
function initAnimationEchec() {
    var cercleEchec = document.querySelector('.echec-cercle');
    if (!cercleEchec) return;

    // Animation de secousse après l'apparition
    setTimeout(function() {
        cercleEchec.style.animation = 'none';
        cercleEchec.offsetHeight; // Trigger reflow
        cercleEchec.style.animation = 'shake 0.6s ease';
    }, 700);

    // Pulsation rouge périodique
    setInterval(function() {
        cercleEchec.style.boxShadow = '0 0 30px rgba(231, 76, 60, 0.4)';
        setTimeout(function() {
            cercleEchec.style.boxShadow = '';
        }, 1200);
    }, 3000);

    // Injecter l'animation shake si pas déjà présente
    if (!document.getElementById('shake-keyframes')) {
        var style = document.createElement('style');
        style.id = 'shake-keyframes';
        style.textContent = 
            '@keyframes shake {' +
            '  0%, 100% { transform: translateX(0); }' +
            '  10%, 30%, 50%, 70%, 90% { transform: translateX(-8px); }' +
            '  20%, 40%, 60%, 80% { transform: translateX(8px); }' +
            '}';
        document.head.appendChild(style);
    }
}

// ============================================================
// ANIMATION DES CAUSES POSSIBLES
// ============================================================
function initCausesAnimation() {
    var causeItems = document.querySelectorAll('.echec-causes li');
    if (causeItems.length === 0) return;

    causeItems.forEach(function(item, index) {
        item.style.opacity = '0';
        item.style.transform = 'translateY(15px)';
        item.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';

        setTimeout(function() {
            item.style.opacity = '1';
            item.style.transform = 'translateY(0)';
        }, 500 + (index * 120));

        // Survol : mise en évidence
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.02)';
            this.style.background = '#fff';
            this.style.boxShadow = '0 4px 15px rgba(0, 0, 0, 0.06)';
        });

        item.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
            this.style.background = '';
            this.style.boxShadow = '';
        });
    });
}

// ============================================================
// BOUTON RÉESSAYER AVEC EFFET
// ============================================================
function initBoutonReessayer() {
    var btnReessayer = document.querySelector('.btn-echec-primary');
    if (!btnReessayer) return;

    // Effet de pulsation pour inciter à cliquer
    btnReessayer.style.animation = 'pulse 2s ease-in-out infinite';

    btnReessayer.addEventListener('click', function(e) {
        // Effet visuel au clic
        this.style.transform = 'scale(0.95)';
        this.style.transition = 'transform 0.1s ease';

        setTimeout(function() {
            btnReessayer.style.transform = '';
        }, 150);

        // Afficher un toast
        if (typeof LearnPath !== 'undefined' && LearnPath.toast) {
            LearnPath.toast('Redirection vers la page de paiement...', 'info', 2000);
        }
    });

    // Animation périodique pour attirer l'attention
    var angle = 0;
    setInterval(function() {
        angle = (angle + 2) % 360;
        var scale = 1 + Math.sin(angle * Math.PI / 180) * 0.02;
        btnReessayer.style.transform = 'scale(' + scale + ')';
    }, 50);
}

// ============================================================
// COPIER LA RÉFÉRENCE POUR LE SUPPORT
// ============================================================
function initCopieReference() {
    var referenceElement = document.querySelector('.echec-details .detail-valeur');
    if (!referenceElement || !EchecConfig.reference) return;

    // Ajouter une icône de copie
    var copierIcon = document.createElement('i');
    copierIcon.className = 'fas fa-copy';
    copierIcon.style.cssText = 
        'margin-left: 8px; cursor: pointer; font-size: 12px; color: #667eea; ' +
        'transition: all 0.3s ease;';
    copierIcon.title = 'Copier la référence';
    referenceElement.appendChild(copierIcon);

    copierIcon.addEventListener('click', function(e) {
        e.stopPropagation();

        if (typeof LearnPath !== 'undefined' && LearnPath.copierPressePapier) {
            LearnPath.copierPressePapier(EchecConfig.reference).then(function(success) {
                if (success) {
                    copierIcon.className = 'fas fa-check';
                    copierIcon.style.color = '#27ae60';

                    if (typeof LearnPath !== 'undefined' && LearnPath.toast) {
                        LearnPath.toast('📋 Référence copiée !', 'success', 2000);
                    }

                    setTimeout(function() {
                        copierIcon.className = 'fas fa-copy';
                        copierIcon.style.color = '#667eea';
                    }, 2000);
                }
            });
        } else {
            // Fallback
            var textarea = document.createElement('textarea');
            textarea.value = EchecConfig.reference;
            textarea.style.position = 'fixed';
            textarea.style.opacity = '0';
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            
            copierIcon.className = 'fas fa-check';
            copierIcon.style.color = '#27ae60';
            setTimeout(function() {
                copierIcon.className = 'fas fa-copy';
                copierIcon.style.color = '#667eea';
            }, 2000);
        }
    });
}

// ============================================================
// INITIALISATION DES CONTACTS SUPPORT
// ============================================================
function initSupportContact() {
    var supportLinks = document.querySelectorAll('.support-contact-item');
    
    supportLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            // Ajouter la référence au sujet de l'email si c'est un mailto
            if (this.href.startsWith('mailto:') && EchecConfig.reference) {
                var sujet = 'Problème paiement - Réf: ' + EchecConfig.reference;
                this.href = this.href.split('?')[0] + '?subject=' + encodeURIComponent(sujet);
            }
        });
    });
}

// ============================================================
// ANIMATION DES CARTES DE RAPPEL
// ============================================================
(function initRappelAnimation() {
    var rappelItems = document.querySelectorAll('.rappel-item');
    
    rappelItems.forEach(function(item, index) {
        item.style.opacity = '0';
        item.style.transform = 'scale(0.9)';
        item.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

        setTimeout(function() {
            item.style.opacity = '1';
            item.style.transform = 'scale(1)';
        }, 800 + (index * 100));
    });
})();

// ============================================================
// MESSAGE DE CONSOLATION DYNAMIQUE
// ============================================================
(function messageConsolation() {
    var messages = [
        'Ne vous découragez pas ! 💪',
        'Ce n\'est qu\'un petit contretemps ! 😊',
        'La persévérance est la clé du succès ! 🔑',
        'Réessayez, vous y êtes presque ! 🎯',
        'Chaque échec est une leçon ! 📚'
    ];

    var messageElem = document.querySelector('.echec-sous-titre');
    if (messageElem) {
        // Changer le message toutes les 5 secondes
        var index = 0;
        setInterval(function() {
            index = (index + 1) % messages.length;
            messageElem.style.opacity = '0';
            messageElem.style.transition = 'opacity 0.3s ease';

            setTimeout(function() {
                messageElem.textContent = messages[index];
                messageElem.style.opacity = '1';
            }, 300);
        }, 5000);
    }
})();

// ============================================================
// DÉTECTION DU MODE HORS LIGNE
// ============================================================
window.addEventListener('offline', function() {
    if (typeof LearnPath !== 'undefined' && LearnPath.toast) {
        LearnPath.toast('⚠️ Vous êtes hors ligne. Vérifiez votre connexion internet.', 'warning', 5000);
    }
});

window.addEventListener('online', function() {
    if (typeof LearnPath !== 'undefined' && LearnPath.toast) {
        LearnPath.toast('✅ Connexion rétablie ! Vous pouvez réessayer le paiement.', 'success', 3000);
    }
});