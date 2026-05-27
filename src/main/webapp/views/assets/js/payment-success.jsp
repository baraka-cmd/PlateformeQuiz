'use strict';

document.addEventListener('DOMContentLoaded', function() {
    
    // Animation de la carte de succès
    const successCard = document.querySelector('.success-card');
    if (successCard) {
        successCard.style.opacity = '0';
        successCard.style.transform = 'translateY(20px)';
        setTimeout(function() {
            successCard.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            successCard.style.opacity = '1';
            successCard.style.transform = 'translateY(0)';
        }, 100);
    }
    
    // Animation des éléments débloqués
    const unlockedItems = document.querySelectorAll('.unlocked-item');
    unlockedItems.forEach(function(item, index) {
        item.style.opacity = '0';
        item.style.transform = 'translateX(-10px)';
        setTimeout(function() {
            item.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateX(0)';
        }, 300 + (index * 100));
    });
    
    // Stocker la confirmation dans localStorage (pour affichage ultérieur)
    try {
        const reference = document.querySelector('.recap-value')?.innerText;
        if (reference) {
            const confirmedPayments = JSON.parse(localStorage.getItem('confirmedPayments') || '[]');
            if (!confirmedPayments.includes(reference)) {
                confirmedPayments.push(reference);
                localStorage.setItem('confirmedPayments', JSON.stringify(confirmedPayments));
            }
        }
    } catch(e) {}
    
    // Ajouter un effet de confettis (optionnel)
    function launchSimpleConfetti() {
        const colors = ['#667eea', '#27ae60', '#f39c12', '#e74c3c'];
        for (let i = 0; i < 50; i++) {
            const confetti = document.createElement('div');
            confetti.style.position = 'fixed';
            confetti.style.width = '8px';
            confetti.style.height = '8px';
            confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            confetti.style.borderRadius = '50%';
            confetti.style.left = Math.random() * window.innerWidth + 'px';
            confetti.style.top = '-10px';
            confetti.style.pointerEvents = 'none';
            confetti.style.zIndex = '9999';
            confetti.style.opacity = '0.8';
            document.body.appendChild(confetti);
            
            const animation = confetti.animate([
                { transform: 'translateY(0) rotate(0deg)', opacity: 0.8 },
                { transform: `translateY(${window.innerHeight}px) rotate(${Math.random() * 360}deg)`, opacity: 0 }
            ], {
                duration: 2000 + Math.random() * 1000,
                easing: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)'
            });
            
            animation.onfinish = function() { confetti.remove(); };
        }
    }
    
    // Lancer les confettis après 500ms
    setTimeout(launchSimpleConfetti, 300);
});