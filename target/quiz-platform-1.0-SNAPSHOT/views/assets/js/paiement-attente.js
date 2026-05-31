// ============================================================
// FICHIER : views/assets/js/paiement-attente.js
// RÔLE    : Script de la page d'attente de paiement
//           - Vérification périodique du statut du paiement
//           - Redirection automatique si succès/échec
//           - Bouton de vérification manuelle
//           - Animation du spinner
//           - Compteur de temps d'attente
//           - Gestion des timeouts
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var AttenteConfig = {
    reference: (typeof REFERENCE !== 'undefined') ? REFERENCE : '',
    contextPath: (typeof CONTEXT_PATH !== 'undefined') ? CONTEXT_PATH : '',
    intervalVerification: 5000, // 5 secondes entre chaque vérification
    maxVerifications: 36,       // 3 minutes maximum (36 x 5s = 180s)
    tempsAttenteTotal: 180,     // 3 minutes en secondes
    compteur: 0,
    intervalId: null,
    tempsRestant: 180,
    timerIntervalId: null
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    if (!AttenteConfig.reference) {
        console.warn('⚠️ Aucune référence de paiement trouvée.');
        return;
    }

    initVerificationAutomatique();
    initBoutonVerification();
    initCompteurAttente();
    initAnimationSpinner();
    console.log('✅ Paiement Attente - Page initialisée - Réf: ' + AttenteConfig.reference);
});

// ============================================================
// VÉRIFICATION PÉRIODIQUE AUTOMATIQUE
// ============================================================
function initVerificationAutomatique() {
    AttenteConfig.intervalId = setInterval(function() {
        AttenteConfig.compteur++;

        // Mettre à jour le message de statut
        var messageElement = document.querySelector('.attente-patience span');
        if (messageElement && AttenteConfig.compteur % 6 === 0) {
            var minutesRestantes = Math.ceil((AttenteConfig.maxVerifications - AttenteConfig.compteur) * 
                AttenteConfig.intervalVerification / 60000);
            messageElement.textContent = 'Vérification automatique en cours... ' +
                '(environ ' + minutesRestantes + ' min restantes)';
        }

        // Vérifier le statut
        verifierStatutPaiement(false);

        // Arrêter si trop de tentatives
        if (AttenteConfig.compteur >= AttenteConfig.maxVerifications) {
            clearInterval(AttenteConfig.intervalId);
            clearInterval(AttenteConfig.timerIntervalId);

            if (messageElement) {
                messageElement.textContent = '⏰ Temps d\'attente écoulé. ' +
                    'Si le paiement a été effectué, contactez le support.';
                messageElement.style.color = '#e74c3c';
            }

            // Afficher le bouton de contact support
            afficherBoutonSupport();
        }
    }, AttenteConfig.intervalVerification);
}

// ============================================================
// VÉRIFICATION DU STATUT
// ============================================================
function verifierStatutPaiement(estManuel) {
    var url = AttenteConfig.contextPath + '/api/paiements/statut?reference=' + 
        encodeURIComponent(AttenteConfig.reference);

    fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error('HTTP ' + response.status);
        }
        return response.json();
    })
    .then(function(data) {
        console.log('🔍 Vérification statut - Réf: ' + AttenteConfig.reference + ' - Statut: ' + data.statut);

        switch (data.statut) {
            case 'SUCCESS':
                // Paiement réussi → rediriger vers la page de succès
                clearInterval(AttenteConfig.intervalId);
                clearInterval(AttenteConfig.timerIntervalId);

                if (typeof AmijayQuiz !== 'undefined' && AmijayQuiz.toast) {
                    AmijayQuiz.toast('✅ Paiement confirmé ! Redirection...', 'success', 2000);
                }

                setTimeout(function() {
                    window.location.href = AttenteConfig.contextPath + '/paiement-succes';
                }, 1000);
                break;

            case 'FAILED':
            case 'CANCELLED':
            case 'EXPIRED':
                // Paiement échoué → rediriger vers la page d'échec
                clearInterval(AttenteConfig.intervalId);
                clearInterval(AttenteConfig.timerIntervalId);

                if (estManuel && typeof AmijayQuiz !== 'undefined' && AmijayQuiz.toast) {
                    AmijayQuiz.toast('❌ Paiement échoué. Redirection...', 'error', 2000);
                }

                setTimeout(function() {
                    window.location.href = AttenteConfig.contextPath + 
                        '/paiement-echoue?reference=' + encodeURIComponent(AttenteConfig.reference);
                }, 1500);
                break;

            case 'PENDING':
                // Toujours en attente
                if (estManuel) {
                    if (typeof AmijayQuiz !== 'undefined' && AmijayQuiz.toast) {
                        AmijayQuiz.toast('⏳ Paiement toujours en attente. Vérifiez votre téléphone.', 
                            'info', 4000);
                    }
                }
                break;

            default:
                console.log('Statut inconnu : ' + data.statut);
                break;
        }
    })
    .catch(function(error) {
        console.error('❌ Erreur vérification : ' + error.message);

        if (estManuel && typeof AmijayQuiz !== 'undefined' && AmijayQuiz.toast) {
            AmijayQuiz.toast('Erreur de connexion. Nouvelle tentative automatique...', 
                'warning', 3000);
        }
    });
}

// ============================================================
// BOUTON DE VÉRIFICATION MANUELLE
// ============================================================
function initBoutonVerification() {
    var btnVerifier = document.getElementById('btnVerifier');
    if (!btnVerifier) return;

    btnVerifier.addEventListener('click', function() {
        // Désactiver le bouton pendant la vérification
        this.disabled = true;
        var originalHTML = this.innerHTML;
        this.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Vérification...</span>';
        this.style.opacity = '0.7';

        // Vérifier
        verifierStatutPaiement(true);

        // Réactiver après 3 secondes
        setTimeout(function() {
            btnVerifier.disabled = false;
            btnVerifier.innerHTML = originalHTML;
            btnVerifier.style.opacity = '1';
        }, 3000);
    });
}

// ============================================================
// COMPTEUR DE TEMPS D'ATTENTE
// ============================================================
function initCompteurAttente() {
    var timerElement = document.getElementById('tempsAttente');
    if (!timerElement) {
        // Créer l'élément s'il n'existe pas
        var attentePatience = document.querySelector('.attente-patience');
        if (attentePatience) {
            timerElement = document.createElement('span');
            timerElement.id = 'tempsAttente';
            timerElement.style.cssText = 'font-weight: 600; color: #667eea; margin-left: 5px;';
            attentePatience.appendChild(timerElement);
        } else {
            return;
        }
    }

    AttenteConfig.timerIntervalId = setInterval(function() {
        AttenteConfig.tempsRestant--;

        if (AttenteConfig.tempsRestant <= 0) {
            clearInterval(AttenteConfig.timerIntervalId);
            timerElement.textContent = '(expiré)';
            timerElement.style.color = '#e74c3c';
            return;
        }

        var minutes = Math.floor(AttenteConfig.tempsRestant / 60);
        var secondes = AttenteConfig.tempsRestant % 60;
        timerElement.textContent = '(' + minutes + ':' + 
            (secondes < 10 ? '0' : '') + secondes + ')';

        // Changer la couleur quand il reste moins d'1 minute
        if (AttenteConfig.tempsRestant <= 60) {
            timerElement.style.color = '#f39c12';
        }
    }, 1000);
}

// ============================================================
// ANIMATION DU SPINNER
// ============================================================
function initAnimationSpinner() {
    var spinner = document.querySelector('.spinner-ring');
    if (!spinner) return;

    // Animation de pulsation supplémentaire
    var pulseInterval = setInterval(function() {
        spinner.style.boxShadow = '0 0 20px rgba(243, 156, 18, 0.4)';
        setTimeout(function() {
            spinner.style.boxShadow = '';
        }, 1000);
    }, 2000);

    // Arrêter quand la vérification s'arrête
    var observer = new MutationObserver(function() {
        if (!document.querySelector('.spinner-ring')) {
            clearInterval(pulseInterval);
            observer.disconnect();
        }
    });
    observer.observe(document.body, { childList: true, subtree: true });
}

// ============================================================
// AFFICHER BOUTON SUPPORT
// ============================================================
function afficherBoutonSupport() {
    var actionsDiv = document.querySelector('.attente-actions');
    if (!actionsDiv) return;

    // Vérifier si le bouton existe déjà
    if (document.getElementById('btnSupport')) return;

    var btnSupport = document.createElement('a');
    btnSupport.id = 'btnSupport';
    btnSupport.href = 'mailto:barakantwali06@gmail.com?subject=Probl%C3%A8me%20paiement%20' + 
        encodeURIComponent(AttenteConfig.reference);
    btnSupport.className = 'btn-verifier';
    btnSupport.style.cssText = 'background: #e74c3c; box-shadow: 0 4px 15px rgba(231, 76, 60, 0.25);';
    btnSupport.innerHTML = '<i class="fas fa-headset"></i><span>Contacter le support</span>';

    actionsDiv.appendChild(btnSupport);
}

// ============================================================
// EMPÊCHER DE QUITTER LA PAGE ACCIDENTELLEMENT
// ============================================================
window.addEventListener('beforeunload', function(e) {
    if (AttenteConfig.intervalId && AttenteConfig.compteur < AttenteConfig.maxVerifications) {
        var message = 'Un paiement est en cours. Êtes-vous sûr de vouloir quitter ?';
        e.returnValue = message;
        return message;
    }
});

// ============================================================
// RAFRAÎCHIR LE STATUT SI LA PAGE REVIENT EN AVANT-PLAN
// ============================================================
document.addEventListener('visibilitychange', function() {
    if (!document.hidden && AttenteConfig.intervalId) {
        // La page redevient visible, vérifier immédiatement
        console.log('👁️ Page visible - Vérification immédiate');
        verifierStatutPaiement(false);

        // Réinitialiser le compteur d'attente
        AttenteConfig.tempsRestant = AttenteConfig.tempsAttenteTotal;
    }
});