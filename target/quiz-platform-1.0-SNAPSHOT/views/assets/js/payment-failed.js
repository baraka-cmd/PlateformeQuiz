'use strict';

document.addEventListener('DOMContentLoaded', function() {
    
    // Animation d'entrée de la carte
    const failedCard = document.querySelector('.failed-card');
    if (failedCard) {
        failedCard.style.opacity = '0';
        failedCard.style.transform = 'translateY(20px)';
        setTimeout(function() {
            failedCard.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            failedCard.style.opacity = '1';
            failedCard.style.transform = 'translateY(0)';
        }, 100);
    }
    
    // Animation des solutions
    const solutionItems = document.querySelectorAll('.solution-item');
    solutionItems.forEach(function(item, index) {
        item.style.opacity = '0';
        item.style.transform = 'translateX(-10px)';
        setTimeout(function() {
            item.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateX(0)';
        }, 200 + (index * 100));
    });
    
    // Bouton réessayer - effet de pulsation
    const retryBtn = document.querySelector('.btn-retry');
    if (retryBtn) {
        retryBtn.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.02)';
        });
        retryBtn.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    }
    
    // Stocker l'échec dans sessionStorage pour analytics (optionnel)
    try {
        const courseId = window.location.href.match(/courseId=(\d+)/);
        if (courseId) {
            const failedAttempts = JSON.parse(sessionStorage.getItem('failedPaymentAttempts') || '[]');
            failedAttempts.push({
                courseId: courseId[1],
                timestamp: new Date().toISOString(),
                page: window.location.href
            });
            sessionStorage.setItem('failedPaymentAttempts', JSON.stringify(failedAttempts));
        }
    } catch(e) {}
    
    // Nettoyer les anciens timeouts de polling si présents
    try {
        if (window.pollingInterval) clearInterval(window.pollingInterval);
        if (window.timeoutTimer) clearTimeout(window.timeoutTimer);
    } catch(e) {}
    
    // Ajouter un tracker d'événement pour le support (optionnel)
    const supportBox = document.querySelector('.support-box');
    if (supportBox) {
        supportBox.addEventListener('click', function() {
            console.log('Support contact clicked - Payment failed page');
        });
    }
});