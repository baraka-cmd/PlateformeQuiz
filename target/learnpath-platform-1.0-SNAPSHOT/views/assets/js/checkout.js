'use strict';

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('paymentForm');
    const phoneInput = document.getElementById('phoneNumber');
    const submitBtn = document.getElementById('submitBtn');
    
    // Validation du numéro de téléphone
    function validatePhoneNumber(phone) {
        phone = phone.replace(/\s/g, '');
        const regex = /^(\+?243)[0-9]{9}$/;
        return regex.test(phone);
    }
    
    function formatPhoneNumber(phone) {
        phone = phone.replace(/\s/g, '');
        if (!phone.startsWith('+') && !phone.startsWith('243')) {
            if (phone.startsWith('0')) {
                phone = '243' + phone.substring(1);
            } else if (phone.length === 9) {
                phone = '243' + phone;
            } else if (phone.length === 10 && phone.startsWith('8')) {
                phone = '243' + phone;
            }
        }
        if (!phone.startsWith('+')) {
            phone = '+' + phone;
        }
        return phone;
    }
    
    // Validation en temps réel
    phoneInput.addEventListener('input', function() {
        let value = this.value;
        if (value.length > 13) {
            this.value = value.substring(0, 13);
        }
    });
    
    // Soumission du formulaire
    form.addEventListener('submit', function(e) {
        let phone = phoneInput.value.trim();
        
        if (!phone) {
            e.preventDefault();
            alert('Veuillez entrer votre numéro de téléphone');
            return;
        }
        
        if (!validatePhoneNumber(phone)) {
            e.preventDefault();
            alert('Numéro de téléphone invalide. Format attendu: 243XXXXXXXXX ou +243XXXXXXXXX');
            return;
        }
        
        // Formater le numéro avant envoi
        const formattedPhone = formatPhoneNumber(phone);
        phoneInput.value = formattedPhone;
        
        // Désactiver le bouton pour éviter double soumission
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Traitement en cours...';
    });
});