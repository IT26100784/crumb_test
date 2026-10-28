/**
 * CRUMB BAKERY - CUSTOM CAKE BUILDER MODULE
 * Component: customcake
 * 
 * WHY: Provides an interactive cake configurator with live 2D visualizer,
 * dynamic polymorphism price engine, lead-time date validation, and order submission.
 */

const CustomCakeBuilder = {
    state: {
        cakeType: 'BIRTHDAY',
        flavor: 'Belgian Chocolate Fudge',
        tiers: 2,
        frostingColor: '#5C2C16', // Dark chocolate default
        inscription: 'Happy Birthday!',
        includeCandles: true,
        floralArrangement: false,
        goldLeafAccent: false,
        specialInstructions: '',
        estimatedPrice: 75.0
    },

    flavorColorMap: {
        'Belgian Chocolate Fudge': '#4A2311',
        'Tahitian Vanilla Bean': '#FDF4DC',
        'Velvet Royale': '#7E1925',
        'Salted Caramel Lotus': '#C68A4C',
        'Matcha White Chocolate': '#8FA382'
    },

    /**
     * Minimum lead-time in days per cake type.
     */
    leadDaysMap: {
        'BIRTHDAY': 2,
        'WEDDING': 5,
        'CELEBRATION': 3
    },

    /**
     * Initialize cake builder on DOM load.
     */
    init() {
        this.bindEvents();
        this.updateDateConstraints();
        this.renderVisualizer();
        this.calculatePrice();
    },

    /**
     * Bind form controls and interactive buttons.
     */
    bindEvents() {
        // Cake Type selection cards
        document.querySelectorAll('.cake-type-card').forEach(card => {
            card.addEventListener('click', () => {
                document.querySelectorAll('.cake-type-card').forEach(c => c.classList.remove('active'));
                card.classList.add('active');
                this.state.cakeType = card.dataset.type || 'BIRTHDAY';
                
                // Show/hide type-specific controls
                this.updateTypeSpecificControls();
                this.updateDateConstraints();
                this.renderVisualizer();
                this.calculatePrice();
            });
        });

        // Flavor chips
        document.querySelectorAll('.flavor-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                document.querySelectorAll('.flavor-chip').forEach(c => c.classList.remove('active'));
                chip.classList.add('active');
                this.state.flavor = chip.dataset.flavor;
                this.state.frostingColor = this.flavorColorMap[this.state.flavor] || '#5C2C16';
                this.renderVisualizer();
                this.calculatePrice();
            });
        });

        // Tiers selector
        const tiersInput = document.getElementById('cake-tiers-select');
        if (tiersInput) {
            tiersInput.addEventListener('change', (e) => {
                this.state.tiers = parseInt(e.target.value) || 1;
                this.renderVisualizer();
                this.calculatePrice();
            });
        }

        // Inscription input
        const inscriptionInput = document.getElementById('cake-inscription-input');
        if (inscriptionInput) {
            inscriptionInput.addEventListener('input', (e) => {
                this.state.inscription = e.target.value;
                const previewEl = document.getElementById('cake-preview-inscription');
                if (previewEl) {
                    previewEl.textContent = e.target.value ? `"${e.target.value}"` : '';
                }
                this.calculatePrice();
            });
        }

        // Toggles: Candles, Florals, Gold Leaf
        const candlesToggle = document.getElementById('cake-opt-candles');
        if (candlesToggle) {
            candlesToggle.addEventListener('change', (e) => {
                this.state.includeCandles = e.target.checked;
                this.renderVisualizer();
                this.calculatePrice();
            });
        }

        const floralToggle = document.getElementById('cake-opt-floral');
        if (floralToggle) {
            floralToggle.addEventListener('change', (e) => {
                this.state.floralArrangement = e.target.checked;
                this.renderVisualizer();
                this.calculatePrice();
            });
        }

        const goldLeafToggle = document.getElementById('cake-opt-goldleaf');
        if (goldLeafToggle) {
            goldLeafToggle.addEventListener('change', (e) => {
                this.state.goldLeafAccent = e.target.checked;
                this.renderVisualizer();
                this.calculatePrice();
            });
        }

        // Submit Button
        const submitBtn = document.getElementById('submit-custom-cake-btn');
        if (submitBtn) {
            submitBtn.addEventListener('click', () => this.submitOrder());
        }
    },

    /**
     * Show/Hide type-specific options (e.g. Wedding flowers vs Birthday candles).
     */
    updateTypeSpecificControls() {
        const type = this.state.cakeType;
        const candlesRow = document.getElementById('opt-row-candles');
        const floralRow = document.getElementById('opt-row-floral');
        const goldLeafRow = document.getElementById('opt-row-goldleaf');

        if (candlesRow) candlesRow.style.display = (type === 'BIRTHDAY') ? 'flex' : 'none';
        if (floralRow) floralRow.style.display = (type === 'WEDDING') ? 'flex' : 'none';
        if (goldLeafRow) goldLeafRow.style.display = (type === 'CELEBRATION') ? 'flex' : 'none';

        // Adjust default tiers for wedding
        const tiersInput = document.getElementById('cake-tiers-select');
        if (tiersInput && type === 'WEDDING' && this.state.tiers < 2) {
            tiersInput.value = '3';
            this.state.tiers = 3;
        }
    },

    /**
     * Calculate and enforce minimum required delivery lead time.
     */
    updateDateConstraints() {
        const dateInput = document.getElementById('cake-date-input');
        if (!dateInput) return;

        const leadDays = this.leadDaysMap[this.state.cakeType] || 2;
        const minDate = new Date();
        minDate.setDate(minDate.getDate() + leadDays);

        const isoDate = minDate.toISOString().split('T')[0];
        dateInput.min = isoDate;
        
        if (!dateInput.value || dateInput.value < isoDate) {
            dateInput.value = isoDate;
        }

        const leadNotice = document.getElementById('cake-lead-notice');
        if (leadNotice) {
            leadNotice.textContent = `Requires at least ${leadDays} days advance preparation for kitchen craftsmanship.`;
        }
    },

    /**
     * Update Realistic 3D Aesthetic Cake Graphic in the preview stage.
     */
    renderVisualizer() {
        const stage = document.getElementById('cake-stage-layers');
        if (!stage) return;
        stage.innerHTML = '';

        const tiersCount = this.state.tiers;
        const baseColor = this.state.frostingColor;

        // Flavor realistic lighting gradients & dripping glazes
        const flavorShades = {
            'Belgian Chocolate Fudge': {
                bodyGrad: 'linear-gradient(90deg, #200E05 0%, #401C0B 22%, #5D2A12 50%, #301407 82%, #180903 100%)',
                capGrad: 'radial-gradient(ellipse at center, #5D2A12 0%, #401C0B 65%, #200E05 100%)',
                dripColor: '#1A0B04'
            },
            'Tahitian Vanilla Bean': {
                bodyGrad: 'linear-gradient(90deg, #D4C3A3 0%, #FAF1DE 26%, #FFF9EE 50%, #E8DAC0 80%, #C4B290 100%)',
                capGrad: 'radial-gradient(ellipse at center, #FFF9EE 0%, #FAF1DE 65%, #D4C3A3 100%)',
                dripColor: '#C4B290'
            },
            'Velvet Royale': {
                bodyGrad: 'linear-gradient(90deg, #4A0E15 0%, #7E1925 24%, #9B2333 50%, #60111A 80%, #35080E 100%)',
                capGrad: 'radial-gradient(ellipse at center, #9B2333 0%, #7E1925 65%, #4A0E15 100%)',
                dripColor: '#FAF1DE'
            },
            'Salted Caramel Lotus': {
                bodyGrad: 'linear-gradient(90deg, #7A461A 0%, #B8732E 24%, #D68D3F 50%, #9C5C1F 80%, #5E320E 100%)',
                capGrad: 'radial-gradient(ellipse at center, #D68D3F 0%, #B8732E 65%, #7A461A 100%)',
                dripColor: '#4A2306'
            },
            'Matcha White Chocolate': {
                bodyGrad: 'linear-gradient(90deg, #4F6346 0%, #7B956E 24%, #97B389 50%, #68825C 80%, #3B4B34 100%)',
                capGrad: 'radial-gradient(ellipse at center, #97B389 0%, #7B956E 65%, #4F6346 100%)',
                dripColor: '#FFF8E6'
            }
        };

        const theme = flavorShades[this.state.flavor] || {
            bodyGrad: `linear-gradient(90deg, rgba(0,0,0,0.5) 0%, rgba(255,255,255,0.2) 25%, ${baseColor} 50%, rgba(0,0,0,0.6) 100%)`,
            capGrad: baseColor,
            dripColor: 'rgba(0,0,0,0.35)'
        };

        // Render each tier from bottom to top
        for (let i = 1; i <= tiersCount; i++) {
            const tierEl = document.createElement('div');
            tierEl.className = `realistic-tier tier-${i}`;

            // Top Cap Ellipse
            const cap = document.createElement('div');
            cap.className = 'tier-cap';
            cap.style.background = theme.capGrad;

            // Tier Body Cylinder
            const body = document.createElement('div');
            body.className = 'tier-body';
            body.style.background = theme.bodyGrad;

            // Realistic dripping ganache glaze on top tier and middle tier
            if (i === tiersCount || i === 2) {
                const drip = document.createElement('div');
                drip.className = 'realistic-drip';
                drip.innerHTML = `
                    <div class="drip-drops">
                        <div class="drip-drop" style="background:${theme.dripColor}; height:14px;"></div>
                        <div class="drip-drop" style="background:${theme.dripColor}; height:22px;"></div>
                        <div class="drip-drop" style="background:${theme.dripColor}; height:11px;"></div>
                        <div class="drip-drop" style="background:${theme.dripColor}; height:18px;"></div>
                        <div class="drip-drop" style="background:${theme.dripColor}; height:13px;"></div>
                    </div>
                `;
                body.appendChild(drip);
            }

            // Pearl bead piping row at the base
            const pearlRow = document.createElement('div');
            pearlRow.className = 'pearl-bead-row';
            body.appendChild(pearlRow);

            // Gold Leaf Flakes if enabled
            if (this.state.goldLeafAccent) {
                for (let g = 0; g < 4; g++) {
                    const flake = document.createElement('div');
                    flake.className = 'gold-leaf-flake';
                    flake.style.width = `${Math.floor(Math.random() * 8) + 6}px`;
                    flake.style.height = `${Math.floor(Math.random() * 6) + 4}px`;
                    flake.style.top = `${Math.floor(Math.random() * 60) + 15}%`;
                    flake.style.left = `${Math.floor(Math.random() * 70) + 15}%`;
                    body.appendChild(flake);
                }
            }

            // Inscription Fondant Ribbon on the front tier (bottom or middle tier)
            if (i === 1 && this.state.inscription && this.state.inscription.trim().length > 0) {
                const ribbon = document.createElement('div');
                ribbon.className = 'fondant-ribbon-plaque';
                ribbon.innerHTML = `<span>${this.state.inscription}</span>`;
                body.appendChild(ribbon);
            }

            tierEl.appendChild(cap);
            tierEl.appendChild(body);
            stage.appendChild(tierEl);
        }

        // Add Realistic Topper
        if (this.state.cakeType === 'BIRTHDAY' && this.state.includeCandles) {
            const candle = document.createElement('div');
            candle.className = 'realistic-candle';
            candle.innerHTML = `
                <div class="candle-flame"></div>
                <div class="candle-wick"></div>
                <div class="candle-stick"></div>
            `;
            stage.appendChild(candle);
        } else if (this.state.cakeType === 'WEDDING' && this.state.floralArrangement) {
            const florals = document.createElement('div');
            florals.className = 'realistic-florals';
            florals.innerHTML = `
                <svg viewBox="0 0 80 50" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="34" cy="24" r="14" fill="#F4D3C4" stroke="#E5A894" stroke-width="2"/>
                    <circle cx="46" cy="22" r="12" fill="#E8B4B8" stroke="#D38B92" stroke-width="2"/>
                    <circle cx="40" cy="14" r="10" fill="#FFF2EB" stroke="#ECC4B2" stroke-width="2"/>
                    <path d="M18 32 C12 20 28 16 32 24 Z" fill="#6B8E65"/>
                    <path d="M62 30 C70 20 54 14 50 24 Z" fill="#587A52"/>
                    <circle cx="34" cy="24" r="5" fill="#E5A894"/>
                    <circle cx="46" cy="22" r="4" fill="#D38B92"/>
                </svg>
            `;
            stage.appendChild(florals);
        } else if (this.state.cakeType === 'CELEBRATION') {
            const monogram = document.createElement('div');
            monogram.className = 'realistic-florals';
            monogram.innerHTML = `
                <div style="background: linear-gradient(135deg, #FFDF73, #D4AF37, #AA771C); padding: 5px 14px; border-radius: 9999px; font-family: var(--font-script); color: #2B1602; font-weight: 800; font-size: 1rem; box-shadow: 0 4px 15px rgba(212, 175, 55, 0.6); border: 1.5px solid #FFEAA7;">
                    Celebration &bull; 2026
                </div>
            `;
            stage.appendChild(monogram);
        }
    },

    /**
     * Calls live price calculation API or local preview engine.
     */
    async calculatePrice() {
        const dateInput = document.getElementById('cake-date-input');
        const reqDate = dateInput ? dateInput.value : new Date().toISOString().split('T')[0];

        const payload = {
            cakeType: this.state.cakeType,
            flavor: this.state.flavor,
            sizeTiers: `${this.state.tiers} Tier(s)`,
            tierCount: this.state.tiers,
            inscription: this.state.inscription,
            candleSetIncluded: this.state.includeCandles,
            freshFlorals: this.state.floralArrangement,
            edibleGoldLeaf: this.state.goldLeafAccent,
            specialInstructions: this.state.specialInstructions,
            eventDate: reqDate
        };

        try {
            const res = await Crumb.api('/custom-cakes/preview', {
                method: 'POST',
                body: payload
            });

            if (res.success && res.data) {
                this.state.estimatedPrice = res.data.estimatedPrice;
                this.updatePriceUI(res.data.estimatedPrice);
            }
        } catch (e) {
            // Local fallback calculation in LKR
            let base = 6500.0;
            if (this.state.cakeType === 'WEDDING') base = 18000.0;
            if (this.state.cakeType === 'CELEBRATION') base = 8500.0;

            const tierAdd = (this.state.tiers - 1) * 6000.0;
            const extras = (this.state.includeCandles ? 450.0 : 0) +
                           (this.state.floralArrangement ? 2500.0 : 0) +
                           (this.state.goldLeafAccent ? 3500.0 : 0);

            const total = base + tierAdd + extras;
            this.state.estimatedPrice = total;
            this.updatePriceUI(total);
        }
    },

    updatePriceUI(price) {
        const priceDisplay = document.getElementById('builder-total-price');
        if (priceDisplay) priceDisplay.textContent = Crumb.formatCurrency(price);

        const breakdownTier = document.getElementById('breakdown-tiers-label');
        if (breakdownTier) breakdownTier.textContent = `${this.state.tiers} Tier(s) (${this.state.tiers * 15} Slices)`;

        const breakdownType = document.getElementById('breakdown-type-label');
        if (breakdownType) breakdownType.textContent = this.state.cakeType;
    },

    /**
     * Submit Custom Cake Order to Backend.
     */
    async submitOrder() {
        if (!Crumb.currentUser) {
            Crumb.showToast('Please sign in or create an account to book your bespoke custom cake.', 'warning');
            CustomerAuth.openAuthModal();
            return;
        }

        const dateInput = document.getElementById('cake-date-input');
        const specialInstr = document.getElementById('cake-instructions-input');

        const payload = {
            cakeType: this.state.cakeType,
            flavor: this.state.flavor,
            sizeTiers: `${this.state.tiers} Tier(s)`,
            tierCount: this.state.tiers,
            inscription: this.state.inscription,
            candleSetIncluded: this.state.includeCandles,
            freshFlorals: this.state.floralArrangement,
            edibleGoldLeaf: this.state.goldLeafAccent,
            specialInstructions: specialInstr ? specialInstr.value : '',
            eventDate: dateInput ? dateInput.value : ''
        };

        const submitBtn = document.getElementById('submit-custom-cake-btn');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Submitting to Kitchen...';
        }

        try {
            const res = await Crumb.api('/custom-cakes', {
                method: 'POST',
                body: payload
            });

            if (res.success && res.data) {
                Crumb.showToast(`Custom cake booked successfully! Reference: #${res.data.id}`, 'success');
                this.showBookingConfirmation(res.data);
            }
        } catch (error) {
            Crumb.showToast(error.message || 'Failed to submit custom cake order.', 'error');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles"></i> Book Bespoke Cake';
            }
        }
    },

    showBookingConfirmation(cake) {
        const modal = document.getElementById('cake-success-modal');
        if (!modal) return;

        document.getElementById('cake-conf-ref').textContent = cake.id ? `CAKE-#${cake.id}` : 'CAKE-CONFIRMED';
        document.getElementById('cake-conf-type').textContent = cake.cakeType || this.state.cakeType;
        document.getElementById('cake-conf-flavor').textContent = cake.flavor;
        document.getElementById('cake-conf-tiers').textContent = cake.sizeTiers || `${this.state.tiers} Tier(s)`;
        document.getElementById('cake-conf-date').textContent = Crumb.formatDate(cake.eventDate);
        document.getElementById('cake-conf-price').textContent = Crumb.formatCurrency(cake.estimatedPrice);

        Crumb.openModal('cake-success-modal');
    }
};

document.addEventListener('DOMContentLoaded', () => {
    CustomCakeBuilder.init();
});
