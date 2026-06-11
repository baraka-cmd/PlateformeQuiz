/**
 * ============================================================
 * FICHIER : views/assets/js/cours-certificat.js
 * RÔLE    : Scripts de la page de certificat
 *           - Impression du certificat
 *           - Téléchargement PDF (simulation)
 *           - Partage sur réseaux sociaux
 *           - Copie du code de vérification
 *           - Animation du diplôme
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initPrintButton();
        initShareButton();
        initCodeCopy();
        initDiplomeAnimation();
    });

    /* ==========================================================
     SECTION 2 : IMPRESSION DU CERTIFICAT
     ========================================================== */
    function initPrintButton() {
        window.imprimerCertificat = function() {
            // Ajouter une classe pour l'impression
            document.body.classList.add('impression-en-cours');
            
            // Petit délai pour l'animation
            setTimeout(function() {
                window.print();
                document.body.classList.remove('impression-en-cours');
            }, 300);
        };

        // Raccourci Ctrl+P
        document.addEventListener('keydown', function(e) {
            if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
                // Ne pas interférer avec l'impression native
                // Juste s'assurer que le certificat est prêt
            }
        });
    }

    /* ==========================================================
     SECTION 3 : PARTAGE DU CERTIFICAT
     ========================================================== */
    function initShareButton() {
        window.partagerCertificat = function() {
            var url = window.location.href;
            var titre = document.querySelector('.diplome-cours')?.textContent || 'Certificat LearnPath';
            var texte = 'Je viens d\'obtenir mon certificat ' + titre + ' sur LearnPath ! 🎓';

            // Créer un modal de partage
            var modal = document.createElement('div');
            modal.className = 'share-modal';
            modal.innerHTML = `
                <div class="share-modal-card">
                    <div class="share-modal-header">
                        <h3><i class="fas fa-share-alt"></i> Partager mon certificat</h3>
                        <button class="share-modal-close" onclick="this.closest('.share-modal').remove()">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                    <div class="share-modal-body">
                        <div class="share-options">
                            <a href="https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}" 
                               target="_blank" rel="noopener" class="share-option linkedin">
                                <i class="fab fa-linkedin-in"></i>
                                <span>LinkedIn</span>
                            </a>
                            <a href="https://twitter.com/intent/tweet?text=${encodeURIComponent(texte)}&url=${encodeURIComponent(url)}" 
                               target="_blank" rel="noopener" class="share-option twitter">
                                <i class="fab fa-twitter"></i>
                                <span>Twitter</span>
                            </a>
                            <a href="https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}" 
                               target="_blank" rel="noopener" class="share-option facebook">
                                <i class="fab fa-facebook-f"></i>
                                <span>Facebook</span>
                            </a>
                            <a href="https://wa.me/?text=${encodeURIComponent(texte + ' ' + url)}" 
                               target="_blank" rel="noopener" class="share-option whatsapp">
                                <i class="fab fa-whatsapp"></i>
                                <span>WhatsApp</span>
                            </a>
                        </div>
                        <div class="share-copy-link">
                            <input type="text" value="${url}" id="shareLinkInput" readonly>
                            <button onclick="copierLienPartage()" class="btn-copy-link">
                                <i class="fas fa-copy"></i> Copier
                            </button>
                        </div>
                    </div>
                </div>
            `;

            // Styles du modal
            Object.assign(modal.style, {
                position: 'fixed', top: '0', left: '0', width: '100%', height: '100%',
                background: 'rgba(0,0,0,0.55)', zIndex: '9999',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                backdropFilter: 'blur(3px)'
            });

            document.body.appendChild(modal);

            // Fermer en cliquant dehors
            modal.addEventListener('click', function(e) {
                if (e.target === modal) modal.remove();
            });
        };

        // Fonction globale pour copier le lien
        window.copierLienPartage = function() {
            var input = document.getElementById('shareLinkInput');
            if (!input) return;

            input.select();
            input.setSelectionRange(0, 99999);

            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(input.value).then(function() {
                    var btn = document.querySelector('.btn-copy-link');
                    if (btn) {
                        var original = btn.innerHTML;
                        btn.innerHTML = '<i class="fas fa-check"></i> Copié !';
                        btn.style.background = '#27ae60';
                        setTimeout(function() {
                            btn.innerHTML = original;
                            btn.style.background = '';
                        }, 2000);
                    }
                });
            }
        };
    }

    // Injecter les styles du modal de partage
    var shareModalStyle = document.createElement('style');
    shareModalStyle.textContent = `
        .share-modal-card {
            background: white; border-radius: 20px; padding: 28px;
            max-width: 480px; width: 90%; box-shadow: 0 25px 60px rgba(0,0,0,0.2);
            animation: shareModalIn 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }
        @keyframes shareModalIn {
            from { opacity: 0; transform: translateY(40px) scale(0.95); }
            to { opacity: 1; transform: translateY(0) scale(1); }
        }
        .share-modal-header {
            display: flex; align-items: center; justify-content: space-between;
            margin-bottom: 20px;
        }
        .share-modal-header h3 {
            font-size: 18px; font-weight: 600; color: #1a1a2e; margin: 0;
            display: flex; align-items: center; gap: 10px;
        }
        .share-modal-header h3 i { color: #667eea; }
        .share-modal-close {
            background: none; border: none; color: #aaa; font-size: 18px;
            cursor: pointer; padding: 5px; transition: all 0.3s ease;
        }
        .share-modal-close:hover { color: #e74c3c; }
        .share-options {
            display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
            margin-bottom: 18px;
        }
        .share-option {
            display: flex; align-items: center; gap: 10px; padding: 12px 16px;
            border-radius: 12px; text-decoration: none; font-size: 14px; font-weight: 600;
            color: white; transition: all 0.3s ease;
        }
        .share-option:hover { transform: translateY(-2px); }
        .share-option.linkedin { background: linear-gradient(135deg, #0077b5, #0a66c2); }
        .share-option.twitter { background: linear-gradient(135deg, #1da1f2, #0d8bd9); }
        .share-option.facebook { background: linear-gradient(135deg, #1877f2, #0d6efd); }
        .share-option.whatsapp { background: linear-gradient(135deg, #25d366, #1da851); }
        .share-copy-link { display: flex; gap: 8px; }
        .share-copy-link input {
            flex: 1; padding: 10px 14px; border: 2px solid #e0e0e0; border-radius: 10px;
            font-size: 12px; font-family: 'Poppins', sans-serif; outline: none;
        }
        .share-copy-link input:focus { border-color: #667eea; }
        .btn-copy-link {
            padding: 10px 16px; background: #667eea; color: white; border: none;
            border-radius: 10px; font-size: 13px; font-weight: 600; cursor: pointer;
            font-family: 'Poppins', sans-serif; transition: all 0.3s ease;
            display: flex; align-items: center; gap: 6px; white-space: nowrap;
        }
        .btn-copy-link:hover { background: #5a6fd6; }
        @media (max-width: 500px) {
            .share-options { grid-template-columns: 1fr; }
        }
    `;
    document.head.appendChild(shareModalStyle);

    /* ==========================================================
     SECTION 4 : COPIE DU CODE DE VÉRIFICATION
     ========================================================== */
    function initCodeCopy() {
        var codeElement = document.querySelector('.verification-valeur');
        if (!codeElement) return;

        codeElement.addEventListener('click', function() {
            var code = this.textContent.trim();

            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(code).then(function() {
                    var original = codeElement.textContent;
                    var originalBg = codeElement.style.background;
                    
                    codeElement.textContent = '✓ Code copié !';
                    codeElement.style.background = '#27ae60';
                    codeElement.style.color = 'white';
                    
                    setTimeout(function() {
                        codeElement.textContent = original;
                        codeElement.style.background = originalBg;
                        codeElement.style.color = '';
                    }, 2000);
                });
            }
        });

        codeElement.style.cursor = 'pointer';
        codeElement.setAttribute('title', 'Cliquer pour copier le code de vérification');
    }

    /* ==========================================================
     SECTION 5 : ANIMATION DU DIPLÔME
     ========================================================== */
    function initDiplomeAnimation() {
        var diplome = document.querySelector('.certificat-diplome');
        if (!diplome) return;

        // Effet de survol subtil
        diplome.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.005)';
            this.style.transition = 'transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
        });

        diplome.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });

        // Observer pour l'animation d'entrée
        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.2
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        // Réinitialiser pour l'animation (si pas déjà fait par le CSS)
        var computedStyle = window.getComputedStyle(diplome);
        if (!computedStyle.animationName || computedStyle.animationName === 'none') {
            diplome.style.opacity = '0';
            diplome.style.transform = 'translateY(30px)';
            diplome.style.transition = 'all 0.8s ease';
            observer.observe(diplome);
        }
    }

    /* ==========================================================
     SECTION 6 : LOG CERTIFICAT
     ========================================================== */
    function initLogCertificat() {
        var nom = document.querySelector('.diplome-nom')?.textContent?.trim();
        var cours = document.querySelector('.diplome-cours')?.textContent?.trim();
        var mention = document.querySelector('.diplome-score-valeur.mention')?.textContent?.trim();

        if (nom && cours) {
            console.log(
                '🎓 Certificat affiché | ' + nom + ' | ' + cours + 
                (mention ? ' | ' + mention : '')
            );
        }
    }

    setTimeout(initLogCertificat, 500);

})();