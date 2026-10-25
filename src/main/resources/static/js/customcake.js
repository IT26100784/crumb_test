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
     * Update 2D Cake Graphic in the preview stage.
     */
    renderVisualizer() {
        const stage = document.getElementById('cake-stage-layers');
        if (!stage) return;

        // Clear existing layers
        stage.innerHTML = '';

        const tiersCount = this.state.tiers;
        const color = this.state.frostingColor;

        // Render each tier from bottom to top
        for (let i = 1; i <= tiersCount; i++) {
            const layer = document.createElement('div');
            layer.className = `cake-layer cake-layer-${i}`;
            layer.style.backgroundColor = color;
            
            // Add subtle border frosting accent
            layer.style.borderTop = '3px solid rgba(255, 255, 255, 0.45)';

            // If gold leaf is enabled
            if (this.state.goldLeafAccent) {
                layer.style.backgroundImage = 'radial-gradient(circle at 30% 40%, rgba(255,215,0,0.4) 0%, transparent 60%)';
            }

            stage.appendChild(layer);
        }

        // Add top ornament (Candles, Florals, or Monogram)
        const ornament = document.createElement('div');
        ornament.className = 'cake-topping-ornament';

        if (this.state.cakeType === 'BIRTHDAY' && this.state.includeCandles) {
            ornament.innerHTML = '🎂';
        } else if (this.state.cakeType === 'WEDDING' && this.state.floralArrangement) {
            ornament.innerHTML = '💐';
        } else if (this.state.cakeType === 'CELEBRATION') {
            ornament.innerHTML = '✨';
        } else {
            ornament.innerHTML = '🍓';
        }

        stage.appendChild(ornament);
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
            // Local fallback calculation
            let base = 50.0;
            if (this.state.cakeType === 'WEDDING') base = 120.0;
            if (this.state.cakeType === 'CELEBRATION') base = 75.0;

            const tierAdd = (this.state.tiers - 1) * 25.0;
            const extras = (this.state.includeCandles ? 5.0 : 0) +
                           (this.state.floralArrangement ? 20.0 : 0) +
                           (this.state.goldLeafAccent ? 15.0 : 0);

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
