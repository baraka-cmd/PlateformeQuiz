// ============================================================
// FICHIER : src/main/webapp/views/assets/js/cours-resultat-module.js
// RÔLE    : Gestion de la page de résultat d'examen de module
//           Animations, affichage/masquage détails,
//           Raccourcis clavier, graphiques optionnels
// ============================================================

'use strict';

// ============================================================
// INITIALISATION AU CHARGEMENT
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initAnimations();
    initToggleDetails();
    initKeyboardShortcuts();
    initCopyResult();
    initPrint();
    initShareButtons();
});

// ============================================================
// ANIMATIONS D'ENTRÉE
// ============================================================
function initAnimations() {
    // Animation du cercle de score
    const scoreCircle = document.querySelector('.score-circle');
    if (scoreCircle) {
        scoreCircle.classList.add('animated');
    }

    // Animation des statistiques
    const statItems = document.querySelectorAll('.stat-item');
    statItems.forEach((item, index) => {
        item.style.animation = `fadeInUp 0.5s ease ${index * 0.1}s forwards`;
        item.style.opacity = '0';
    });

    // Animation des éléments de correction un par un
    const correctionItems = document.querySelectorAll('.correction-item');
    correctionItems.forEach((item, index) => {
        item.style.animation = `slideInLeft 0.4s ease ${index * 0.05}s forwards`;
        item.style.opacity = '0';
    });
}

// ============================================================
// AFFICHAGE/MASQUAGE DES DÉTAILS DE CORRECTION
// ============================================================
function initToggleDetails() {
    const correctionItems = document.querySelectorAll('.correction-item');
    
    correctionItems.forEach(item => {
        // Ajouter un bouton toggle pour les questions longues
        const questionText = item.querySelector('.correction-question');
        if (questionText && questionText.textContent.length > 200) {
            const toggleBtn = document.createElement('button');
            toggleBtn.className = 'toggle-detail-btn';
            toggleBtn.innerHTML = '<i class="fas fa-chevron-down"></i> Voir plus';
            toggleBtn.onclick = () => toggleDetail(item, toggleBtn);
            
            const header = item.querySelector('.correction-item-header');
            if (header) {
                header.after(toggleBtn);
            }
        }
    });
}

function toggleDetail(item, btn) {
    const body = item.querySelector('.correction-item-body');
    if (body) {
        if (body.style.maxHeight) {
            body.style.maxHeight = null;
            btn.innerHTML = '<i class="fas fa-chevron-down"></i> Voir plus';
        } else {
            body.style.maxHeight = body.scrollHeight + 'px';
            btn.innerHTML = '<i class="fas fa-chevron-up"></i> Voir moins';
        }
    }
}

// ============================================================
// RACCOURCIS CLAVIER
// ============================================================
function initKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl + P : Imprimer
        if (e.ctrlKey && e.key === 'p') {
            e.preventDefault();
            imprimerResultat();
        }
        
        // Ctrl + C : Copier le résultat
        if (e.ctrlKey && e.key === 'c' && !e.target.matches('input, textarea')) {
            e.preventDefault();
            copierResultat();
        }
        
        // Échap : Fermer les toggles ouverts
        if (e.key === 'Escape') {
            const openToggles = document.querySelectorAll('.correction-item-body[style*="max-height"]');
            openToggles.forEach(toggle => {
                toggle.style.maxHeight = null;
                const btn = toggle.closest('.correction-item')?.querySelector('.toggle-detail-btn');
                if (btn) btn.innerHTML = '<i class="fas fa-chevron-down"></i> Voir plus';
            });
        }
        
        // Flèche gauche : Retour au plan
        if (e.key === 'ArrowLeft' && !e.ctrlKey && !e.altKey) {
            const planLink = document.querySelector('.btn-plan-retour');
            if (planLink) {
                window.location.href = planLink.getAttribute('href');
            }
        }
        
        // Flèche droite : Module suivant (si disponible)
        if (e.key === 'ArrowRight' && !e.ctrlKey && !e.altKey) {
            const nextLink = document.querySelector('.btn-module-suivant');
            if (nextLink) {
                window.location.href = nextLink.getAttribute('href');
            }
        }
        
        // R : Réessayer l'examen (si échec)
        if (e.key === 'r' || e.key === 'R') {
            const retryLink = document.querySelector('.btn-reessayer');
            if (retryLink) {
                window.location.href = retryLink.getAttribute('href');
            }
        }
    });
}

// ============================================================
// COPIER LE RÉSULTAT
// ============================================================
function initCopyResult() {
    // Ajouter un bouton de copie dans le header
    const header = document.querySelector('.score-header');
    if (header && !document.querySelector('.copy-result-btn')) {
        const copyBtn = document.createElement('button');
        copyBtn.className = 'copy-result-btn';
        copyBtn.innerHTML = '<i class="fas fa-copy"></i> Copier le résultat';
        copyBtn.onclick = copierResultat;
        copyBtn.style.cssText = 'position: absolute; top: 20px; right: 20px; background: none; border: none; color: #667eea; cursor: pointer; font-size: 13px;';
        
        const scoreCard = document.querySelector('.score-card');
        if (scoreCard) {
            scoreCard.style.position = 'relative';
            scoreCard.appendChild(copyBtn);
        }
    }
}

function copierResultat() {
    const scoreCard = document.querySelector('.score-card');
    const alerte = document.querySelector('.alerte-resultat');
    const performance = document.querySelector('.performance-card');
    
    if (!scoreCard) return;
    
    // Extraire les informations
    const titre = document.querySelector('.score-titre')?.innerText || 'Résultat examen';
    const module = document.querySelector('.score-module')?.innerText || '';
    const score = document.querySelector('.score-valeur')?.innerText || '';
    const detail = document.querySelector('.score-detail')?.innerText || '';
    const performanceText = performance?.querySelector('p')?.innerText || '';
    const alerteText = alerte?.querySelector('.alerte-content p')?.innerText || '';
    
    const texteCopie = `📊 ${titre}
    
📚 ${module}
✅ Score : ${score} (${detail})
📈 ${performanceText}
${alerteText}

---
LearnPath - Votre plateforme d'apprentissage`;
    
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(texteCopie).then(() => {
            afficherNotification('Résultat copié !');
        }).catch(() => {
            copieFallback(texteCopie);
        });
    } else {
        copieFallback(texteCopie);
    }
}

function copieFallback(texte) {
    const textarea = document.createElement('textarea');
    textarea.value = texte;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    
    try {
        document.execCommand('copy');
        afficherNotification('Résultat copié !');
    } catch (err) {
        console.error('Erreur lors de la copie :', err);
        afficherNotification('Impossible de copier', 'error');
    }
    
    document.body.removeChild(textarea);
}

function afficherNotification(message, type = 'success') {
    let notification = document.getElementById('resultNotification');
    if (!notification) {
        notification = document.createElement('div');
        notification.id = 'resultNotification';
        notification.style.cssText = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: ${type === 'success' ? '#27ae60' : '#e74c3c'};
            color: white;
            padding: 12px 20px;
            border-radius: 10px;
            font-size: 13px;
            font-weight: 500;
            z-index: 1000;
            opacity: 0;
            transition: opacity 0.3s;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        `;
        document.body.appendChild(notification);
    }
    
    notification.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i> ${message}`;
    notification.style.backgroundColor = type === 'success' ? '#27ae60' : '#e74c3c';
    notification.style.opacity = '1';
    
    setTimeout(() => {
        notification.style.opacity = '0';
    }, 3000);
}

// ============================================================
// IMPRESSION
// ============================================================
function initPrint() {
    const printBtn = document.createElement('button');
    printBtn.className = 'print-result-btn';
    printBtn.innerHTML = '<i class="fas fa-print"></i> Imprimer';
    printBtn.onclick = imprimerResultat;
    printBtn.style.cssText = 'position: fixed; bottom: 20px; left: 20px; background: #667eea; color: white; border: none; border-radius: 30px; padding: 10px 20px; font-size: 13px; cursor: pointer; z-index: 100; box-shadow: 0 2px 8px rgba(0,0,0,0.2); display: flex; align-items: center; gap: 8px;';
    
    document.body.appendChild(printBtn);
}

function imprimerResultat() {
    const originalTitle = document.title;
    document.title = 'Résultat examen - LearnPath';
    
    const printContent = document.querySelector('.resultat-module-container');
    if (!printContent) return;
    
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Résultat examen - LearnPath</title>
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
            <style>
                body {
                    font-family: 'Poppins', sans-serif;
                    padding: 40px;
                    max-width: 900px;
                    margin: 0 auto;
                    color: #333;
                }
                .score-card {
                    text-align: center;
                    margin-bottom: 30px;
                }
                .score-circle {
                    width: 120px;
                    height: 120px;
                    border-radius: 50%;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    margin: 0 auto 20px;
                }
                .correction-item {
                    margin-bottom: 20px;
                    padding: 15px;
                    border-left: 3px solid;
                }
                .correction-item.correct { border-left-color: #27ae60; }
                .correction-item.incorrect { border-left-color: #e74c3c; }
                .reponse-detail { margin-top: 10px; padding: 8px; border-radius: 8px; }
                .reponse-ok { background: #e8f5e9; }
                .reponse-ko { background: #ffebee; }
                .reponse-correcte { background: #eef2ff; }
                @media print {
                    body { padding: 0; }
                    .print-result-btn, .copy-result-btn, .toggle-detail-btn { display: none; }
                }
            </style>
        </head>
        <body>
            ${printContent.cloneNode(true).innerHTML}
        </body>
        </html>
    `);
    printWindow.document.close();
    printWindow.print();
    
    document.title = originalTitle;
}

// ============================================================
// BOUTONS DE PARTAGE
// ============================================================
function initShareButtons() {
    if (!navigator.share) return;
    
    const shareBtn = document.createElement('button');
    shareBtn.className = 'share-result-btn';
    shareBtn.innerHTML = '<i class="fas fa-share-alt"></i> Partager';
    shareBtn.onclick = partagerResultat;
    shareBtn.style.cssText = 'position: fixed; bottom: 20px; right: 100px; background: #667eea; color: white; border: none; border-radius: 30px; padding: 10px 20px; font-size: 13px; cursor: pointer; z-index: 100; display: flex; align-items: center; gap: 8px;';
    
    document.body.appendChild(shareBtn);
}

function partagerResultat() {
    const scoreText = document.querySelector('.score-valeur')?.innerText || '';
    const module = document.querySelector('.score-module')?.innerText || '';
    const isSuccess = document.querySelector('.score-card.reussi') !== null;
    
    const shareData = {
        title: 'LearnPath - Résultat examen',
        text: isSuccess 
            ? `🎉 J'ai réussi ${module} avec ${scoreText} sur LearnPath !` 
            : `📚 J'ai obtenu ${scoreText} à ${module} sur LearnPath. À réviser !`,
        url: window.location.href
    };
    
    navigator.share(shareData).catch(() => {
        afficherNotification('Partage annulé', 'info');
    });
}

// ============================================================
// ANIMATIONS CSS DYNAMIQUES
// ============================================================
const styleSheet = document.createElement('style');
styleSheet.textContent = `
    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
    
    @keyframes slideInLeft {
        from {
            opacity: 0;
            transform: translateX(-20px);
        }
        to {
            opacity: 1;
            transform: translateX(0);
        }
    }
    
    .score-circle.animated {
        animation: pulse 0.6s ease-out;
    }
    
    @keyframes pulse {
        0% { transform: scale(0.8); opacity: 0; }
        70% { transform: scale(1.05); }
        100% { transform: scale(1); opacity: 1; }
    }
    
    .toggle-detail-btn {
        background: none;
        border: none;
        color: #667eea;
        cursor: pointer;
        font-size: 12px;
        padding: 8px 12px;
        margin: 8px 0;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-family: inherit;
    }
    
    .toggle-detail-btn:hover {
        text-decoration: underline;
    }
    
    .correction-item-body {
        overflow: hidden;
        transition: max-height 0.3s ease;
        max-height: none;
    }
    
    .stat-item {
        animation-fill-mode: forwards;
    }
    
    .correction-item {
        animation-fill-mode: forwards;
    }
`;
document.head.appendChild(styleSheet);