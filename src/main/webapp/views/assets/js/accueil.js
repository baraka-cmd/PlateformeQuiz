/**
 * ============================================================
 * FICHIER : views/assets/js/accueil.js
 * RÔLE    : Scripts de la page d'accueil AmijayQuiz
 *           - Animation des compteurs de statistiques
 *           - Animation des nombres (défilement)
 *           - Gestion du scroll pour les animations
 *           - Effet parallaxe léger sur le hero
 *           - Animation des formes géométriques
 *           - Gestion des cartes flottantes (hero visuel)
 *           - Intersection Observer pour révéler les sections
 *           - Effet de vague sur le titre
 *           - Gestion du responsive
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initCounters();
        initParallaxEffect();
        initFloatingCards();
        initScrollReveal();
        initTypewriterEffect();
        initMouseParallax();
    });

    /* ==========================================================
     SECTION 2 : COMPTEURS ANIMÉS (STATISTIQUES)
     ========================================================== */
    function initCounters() {
        const counters = document.querySelectorAll('.stats-section .counter');
        if (counters.length === 0) return;

        let countersAnimated = false;

        // Configuration de l'Intersection Observer
        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -80px 0px',
            threshold: 0.3
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting && !countersAnimated) {
                    countersAnimated = true;
                    animateAllCounters(counters);
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        // Observer la section des statistiques
        const statsSection = document.querySelector('.stats-section');
        if (statsSection) {
            observer.observe(statsSection);
        } else {
            // Fallback : animer immédiatement
            animateAllCounters(counters);
        }
    }

    function animateAllCounters(counters) {
        counters.forEach(function(counter) {
            const targetText = counter.textContent.trim();
            const target = parseInt(targetText, 10);
            
            if (isNaN(target)) return;

            const duration = 2000; // Durée totale en ms
            const startTime = performance.now();
            const startValue = 0;

            function updateCounter(currentTime) {
                const elapsed = currentTime - startTime;
                const progress = Math.min(elapsed / duration, 1);

                // Fonction d'easing easeOutExpo pour un effet de ralentissement
                const easedProgress = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
                
                const currentValue = Math.floor(startValue + (target - startValue) * easedProgress);
                counter.textContent = currentValue;

                if (progress < 1) {
                    requestAnimationFrame(updateCounter);
                } else {
                    counter.textContent = target; // Valeur finale exacte
                    
                    // Petite animation de pulsation à la fin
                    counter.style.transform = 'scale(1.1)';
                    counter.style.transition = 'transform 0.3s ease';
                    setTimeout(function() {
                        counter.style.transform = 'scale(1)';
                    }, 300);
                }
            }

            requestAnimationFrame(updateCounter);
        });
    }

    /* ==========================================================
     SECTION 3 : EFFET PARALLAXE SUR LE HERO
     ========================================================== */
    function initParallaxEffect() {
        const heroSection = document.querySelector('.hero-section');
        const shapes = document.querySelectorAll('.shape');
        
        if (!heroSection || shapes.length === 0) return;

        let ticking = false;

        window.addEventListener('scroll', function() {
            if (!ticking) {
                window.requestAnimationFrame(function() {
                    const scrollY = window.scrollY;
                    const heroHeight = heroSection.offsetHeight;
                    
                    // Ne pas appliquer le parallaxe si on a dépassé le hero
                    if (scrollY <= heroHeight) {
                        const factor = scrollY / heroHeight;
                        
                        shapes.forEach(function(shape, index) {
                            const speed = 0.3 + (index * 0.15);
                            const yOffset = scrollY * speed;
                            const xOffset = scrollY * (speed * 0.5);
                            
                            shape.style.transform = 'translateY(' + yOffset + 'px) translateX(' + xOffset + 'px)';
                        });
                    }
                    
                    ticking = false;
                });
                ticking = true;
            }
        }, { passive: true });
    }

    /* ==========================================================
     SECTION 4 : CARTES FLOTTANTES (HERO VISUEL)
     ========================================================== */
    function initFloatingCards() {
        const floatingCards = document.querySelectorAll('.floating-card');
        if (floatingCards.length === 0) return;

        // Animation subtile au survol
        floatingCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-20px) scale(1.05)';
                this.style.boxShadow = '0 15px 35px rgba(0, 0, 0, 0.2)';
                this.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });

            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
                this.style.boxShadow = '0 8px 25px rgba(0, 0, 0, 0.12)';
            });
        });

        // Animation d'entrée séquentielle
        floatingCards.forEach(function(card, index) {
            card.style.opacity = '0';
            card.style.transform = 'translateY(30px) scale(0.8)';
            card.style.transition = 'all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            
            setTimeout(function() {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0) scale(1)';
            }, 600 + (index * 200));
        });
    }

    /* ==========================================================
     SECTION 5 : RÉVÉLATION DES SECTIONS AU SCROLL
     ========================================================== */
    function initScrollReveal() {
        // Éléments à révéler
        const revealElements = document.querySelectorAll(
            '.step-card, .feature-card, .theme-card, .testimonial-card, .stat-item'
        );

        if (revealElements.length === 0) return;

        const observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.15
        };

        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry, index) {
                if (entry.isIntersecting) {
                    // Délai progressif pour effet cascade
                    const delay = index * 60;
                    
                    setTimeout(function() {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0) scale(1)';
                    }, delay);
                    
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        // Initialiser les éléments
        revealElements.forEach(function(el) {
            // Vérifier si l'élément n'a pas déjà une animation CSS
            const computedStyle = window.getComputedStyle(el);
            if (computedStyle.animationName === 'none' || computedStyle.animationName === '') {
                el.style.opacity = '0';
                el.style.transform = 'translateY(35px) scale(0.95)';
                el.style.transition = 'all 0.7s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            }
            observer.observe(el);
        });
    }

    /* ==========================================================
     SECTION 6 : EFFET TYPEWRITER SUR LE SLOGAN
     ========================================================== */
    function initTypewriterEffect() {
        const slogan = document.querySelector('.hero-subtitle');
        if (!slogan) return;

        const originalText = slogan.textContent.trim();
        const words = originalText.split(' ');
        
        // Animation subtile : les mots apparaissent un par un
        slogan.style.opacity = '0';
        slogan.textContent = '';
        
        let wordIndex = 0;
        const interval = setInterval(function() {
            if (wordIndex < words.length) {
                slogan.textContent += (wordIndex > 0 ? ' ' : '') + words[wordIndex];
                wordIndex++;
            } else {
                clearInterval(interval);
                slogan.style.opacity = '1';
            }
        }, 150);

        // Fallback si l'animation ne se termine pas
        setTimeout(function() {
            if (slogan.textContent !== originalText) {
                slogan.textContent = originalText;
                slogan.style.opacity = '1';
            }
        }, words.length * 200 + 500);
    }

    /* ==========================================================
     SECTION 7 : EFFET PARALLAXE SOURIS (DESKTOP UNIQUEMENT)
     ========================================================== */
    function initMouseParallax() {
        // Ne pas activer sur mobile/touchscreen
        if (window.isTouchDevice && window.isTouchDevice()) return;

        const heroVisual = document.querySelector('.hero-visual');
        const illustrationCircle = document.querySelector('.illustration-circle');
        
        if (!heroVisual || !illustrationCircle) return;

        let mouseX = 0;
        let mouseY = 0;
        let targetX = 0;
        let targetY = 0;
        let currentX = 0;
        let currentY = 0;

        document.addEventListener('mousemove', function(e) {
            const windowWidth = window.innerWidth;
            const windowHeight = window.innerHeight;
            
            // Normaliser les coordonnées entre -1 et 1
            mouseX = (e.clientX / windowWidth - 0.5) * 2;
            mouseY = (e.clientY / windowHeight - 0.5) * 2;
        });

        // Animation fluide avec requestAnimationFrame
        function animate() {
            // Interpolation linéaire pour un mouvement fluide
            const ease = 0.05;
            targetX += (mouseX - targetX) * ease;
            targetY += (mouseY - targetY) * ease;
            
            currentX += (targetX - currentX) * ease;
            currentY += (targetY - currentY) * ease;

            const moveX = currentX * 25;
            const moveY = currentY * 25;

            if (heroVisual) {
                heroVisual.style.transform = 'translateX(' + moveX + 'px) translateY(' + moveY + 'px)';
                heroVisual.style.transition = 'none';
            }

            if (illustrationCircle) {
                illustrationCircle.style.transform = 
                    'translate(-50%, -50%) translateX(' + (moveX * 0.5) + 'px) translateY(' + (moveY * 0.5) + 'px)';
            }

            requestAnimationFrame(animate);
        }

        requestAnimationFrame(animate);

        // Réinitialiser au redimensionnement
        window.addEventListener('resize', function() {
            mouseX = 0;
            mouseY = 0;
            targetX = 0;
            targetY = 0;
            currentX = 0;
            currentY = 0;
        });
    }

    /* ==========================================================
     SECTION 8 : EFFET DE SURVOL DES CARTES THÈMES
     ========================================================== */
    function initThemeCardHover() {
        const themeCards = document.querySelectorAll('.theme-card');
        if (themeCards.length === 0) return;

        themeCards.forEach(function(card) {
            card.addEventListener('mouseenter', function(e) {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                
                const centerX = rect.width / 2;
                const centerY = rect.height / 2;
                
                const rotateX = (y - centerY) / centerY * -8;
                const rotateY = (x - centerX) / centerX * 8;
                
                card.style.transform = 
                    'translateY(-8px) perspective(1000px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
                card.style.transition = 'transform 0.1s ease';
            });

            card.addEventListener('mousemove', function(e) {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                
                const centerX = rect.width / 2;
                const centerY = rect.height / 2;
                
                const rotateX = (y - centerY) / centerY * -8;
                const rotateY = (x - centerX) / centerX * 8;
                
                card.style.transform = 
                    'translateY(-8px) perspective(1000px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
            });

            card.addEventListener('mouseleave', function() {
                card.style.transform = 'translateY(0) perspective(1000px) rotateX(0) rotateY(0)';
                card.style.transition = 'transform 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            });
        });
    }

    // Initialiser l'effet 3D des cartes thèmes
    if (!window.isTouchDevice || !window.isTouchDevice()) {
        setTimeout(initThemeCardHover, 1000);
    }

    /* ==========================================================
     SECTION 9 : ANIMATION DES ÉTOILES (TÉMOIGNAGES)
     ========================================================== */
    function initStarAnimation() {
        const testimonialCards = document.querySelectorAll('.testimonial-card');
        if (testimonialCards.length === 0) return;

        testimonialCards.forEach(function(card) {
            card.addEventListener('mouseenter', function() {
                const stars = this.querySelectorAll('.fa-star, .fa-star-half-alt');
                stars.forEach(function(star, index) {
                    star.style.transition = 'all 0.3s ease';
                    star.style.transform = 'scale(1.3) rotate(' + ((index - 2) * 10) + 'deg)';
                    star.style.color = '#ffd700';
                    star.style.textShadow = '0 0 10px rgba(255, 215, 0, 0.5)';
                    
                    setTimeout(function() {
                        star.style.transform = 'scale(1) rotate(0deg)';
                        star.style.textShadow = 'none';
                    }, 300 + (index * 80));
                });
            });
        });
    }

    initStarAnimation();

    /* ==========================================================
     SECTION 10 : GESTION DU REDIMENSIONNEMENT
     ========================================================== */
    let resizeTimeout;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(function() {
            // Réinitialiser les animations si nécessaire
            const heroSection = document.querySelector('.hero-section');
            if (heroSection && window.innerWidth <= 768) {
                // Sur mobile, réinitialiser les transformations parallaxe
                const shapes = document.querySelectorAll('.shape');
                shapes.forEach(function(shape) {
                    shape.style.transform = 'translateY(0) translateX(0)';
                });
            }
        }, 250);
    });

    /* ==========================================================
     SECTION 11 : DÉTECTION DU MODE SOMBRE DU SYSTÈME (FUTUR)
     ========================================================== */
    const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)');
    
    function handleDarkModeChange(e) {
        // Préparé pour une future fonctionnalité de thème sombre
        if (e.matches) {
            console.log('Mode sombre détecté - fonctionnalité à venir');
        } else {
            console.log('Mode clair détecté');
        }
    }
    
    darkModeQuery.addEventListener('change', handleDarkModeChange);
    // handleDarkModeChange(darkModeQuery); // Décommenter pour activer

})();