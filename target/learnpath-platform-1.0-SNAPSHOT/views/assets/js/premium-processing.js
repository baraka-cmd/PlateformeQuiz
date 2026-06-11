'use strict';

document.addEventListener('DOMContentLoaded', function() {
    const reference = document.getElementById('referenceValue').value;
    const progressFill = document.getElementById('progressFill');
    const refreshBtn = document.getElementById('refreshBtn');
    const errorMessage = document.getElementById('errorMessage');
    const countdownElement = document.getElementById('countdown');
    
    let pollingInterval = null;
    let timeoutTimer = null;
    let timeLeft = 300; // 5 minutes en secondes
    
    // Mettre à jour le compte à rebours
    function updateCountdown() {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        countdownElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        if (timeLeft <= 0) {
            clearInterval(pollingInterval);
            clearTimeout(timeoutTimer);
            document.querySelector('.premium-processing-card h1').innerHTML = '<i class="fas fa-hourglass-end"></i> Temps expiré';
            document.querySelector('.processing-subtitle').innerHTML = 'Le délai de paiement a expiré. Veuillez réessayer.';
            refreshBtn.style.display = 'inline-flex';
            stopProgressAnimation();
        }
        timeLeft--;
    }
    
    // Animer la barre de progression
    function startProgressAnimation() {
        let width = 0;
        const interval = setInterval(function() {
            width += 0.33;
            if (width <= 100 && progressFill) {
                progressFill.style.width = width + '%';
            }
            if (width >= 100) {
                clearInterval(interval);
            }
        }, 100);
    }
    
    function stopProgressAnimation() {
        if (progressFill) {
            progressFill.style.width = '100%';
            progressFill.style.background = '#e74c3c';
        }
    }
    
    // Vérifier le statut du paiement premium
    async function checkPaymentStatus() {
        try {
            const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
            const response = await fetch(`${contextPath}/api/premium/status?reference=${reference}`);
            const data = await response.json();
            
            if (data.isSuccess) {
                clearInterval(pollingInterval);
                clearTimeout(timeoutTimer);
                
                document.querySelector('.premium-processing-card h1').innerHTML = '<i class="fas fa-check-circle"></i> Premium activé !';
                document.querySelector('.processing-subtitle').innerHTML = 'Votre compte a été mis à niveau. Redirection...';
                
                const statusSpan = document.querySelector('.status-pending');
                if (statusSpan) {
                    statusSpan.innerHTML = '<i class="fas fa-check-circle"></i> Activé';
                    statusSpan.style.background = '#e8f5e9';
                    statusSpan.style.color = '#27ae60';
                }
                
                setTimeout(function() {
                    window.location.href = `${contextPath}/joueur/cours`;
                }, 2000);
                
            } else if (!data.isPending) {
                clearInterval(pollingInterval);
                clearTimeout(timeoutTimer);
                
                document.querySelector('.premium-processing-card h1').innerHTML = '<i class="fas fa-times-circle"></i> Paiement échoué';
                document.querySelector('.processing-subtitle').innerHTML = 'Le paiement a échoué. Veuillez réessayer.';
                refreshBtn.style.display = 'inline-flex';
                stopProgressAnimation();
            }
            
        } catch (error) {
            console.error('Erreur:', error);
            errorMessage.style.display = 'flex';
            clearInterval(pollingInterval);
            refreshBtn.style.display = 'inline-flex';
        }
    }
    
    // Démarrer le polling
    function startPolling() {
        checkPaymentStatus();
        pollingInterval = setInterval(checkPaymentStatus, 3000);
    }
    
    // Démarrer le compte à rebours
    function startCountdown() {
        updateCountdown();
        timeoutTimer = setInterval(updateCountdown, 1000);
    }
    
    // Rafraîchir manuellement
    refreshBtn.addEventListener('click', function() {
        errorMessage.style.display = 'none';
        refreshBtn.style.display = 'none';
        startPolling();
        startCountdown();
        startProgressAnimation();
    });
    
    // Lancer les processus
    startProgressAnimation();
    startPolling();
    startCountdown();
});