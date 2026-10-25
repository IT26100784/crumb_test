/**
 * CRUMB BAKERY - PRODUCT CATALOG & SHOPPING CART MODULE
 * Component: product
 * 
 * WHY: Powers artisan product browsing, category filter chips, live search,
 * quick-view modal with volume discount pricing, and persistent shopping cart.
 */

const ProductCatalog = {
    products: [],
    activeCategory: 'ALL',
    searchQuery: '',
    sortBy: 'rating',
    cart: [],

    /**
     * Initialize catalog and shopping cart on page load.
     */
    async init() {
        this.loadCartFromStorage();
        this.bindEvents();
        await this.fetchProducts();
        this.renderCartUI();
    },

    /**
     * Binds UI filter, search, and cart drawer listeners.
     */
    bindEvents() {
        // Category filter chips
        document.querySelectorAll('.category-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                document.querySelectorAll('.category-chip').forEach(c => c.classList.remove('active'));
                chip.classList.add('active');
                this.activeCategory = chip.dataset.category || 'ALL';
                this.filterAndRenderProducts();
            });
        });

        // Navigation links that filter categories (e.g. - Our Bread -, - Sweet Treats -)
        document.querySelectorAll('a[data-category-filter]').forEach(navLink => {
            navLink.addEventListener('click', () => {
                const targetCat = navLink.dataset.categoryFilter;
                const matchingChip = document.querySelector(`.category-chip[data-category="${targetCat}"]`);
                if (matchingChip) {
                    matchingChip.click();
                }
            });
        });

        // Search input with debounce
        const searchInput = document.getElementById('catalog-search-input');
        if (searchInput) {
            let debounceTimer;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(() => {
                    this.searchQuery = e.target.value.trim().toLowerCase();
                    this.filterAndRenderProducts();
                }, 250);
            });
        }

        // Sort selector
        const sortSelect = document.getElementById('catalog-sort-select');
        if (sortSelect) {
            sortSelect.addEventListener('change', (e) => {
                this.sortBy = e.target.value;
                this.filterAndRenderProducts();
            });
        }

        // Cart drawer open trigger
        document.querySelectorAll('.cart-trigger-btn').forEach(btn => {
            btn.addEventListener('click', () => this.openCartDrawer());
        });

        // Cart drawer close trigger
        const closeCartBtn = document.getElementById('close-cart-btn');
        if (closeCartBtn) {
            closeCartBtn.addEventListener('click', () => this.closeCartDrawer());
        }

        // Close cart on overlay click
        const cartOverlay = document.getElementById('cart-drawer-overlay');
        if (cartOverlay) {
            cartOverlay.addEventListener('click', (e) => {
                if (e.target === cartOverlay) {
                    this.closeCartDrawer();
                }
            });
        }

        // Proceed to Checkout button in cart drawer
        const checkoutBtn = document.getElementById('cart-checkout-btn');
        if (checkoutBtn) {
            checkoutBtn.addEventListener('click', () => {
                if (this.cart.length === 0) {
                    Crumb.showToast('Your cart is empty. Add some delicious bakery treats!', 'warning');
                    return;
                }
                this.closeCartDrawer();
                if (window.OrderModule) {
                    window.OrderModule.openCheckoutModal();
                }
            });
        }
    },

    /**
     * Fetch products from REST API.
     */
    async fetchProducts() {
        const grid = document.getElementById('products-grid');
        if (!grid) return;

        // Show skeleton loading cards
        grid.innerHTML = Array(6).fill(0).map(() => `
            <div class="product-card skeleton" style="height: 380px;"></div>
        `).join('');

        try {
            const res = await Crumb.api('/products');
            if (res.success && res.data) {
                this.products = res.data;
                this.filterAndRenderProducts();
            }
        } catch (error) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 3rem;">
                    <i class="fa-solid fa-triangle-exclamation" style="font-size: 2.5rem; color: var(--color-accent); margin-bottom: 1rem;"></i>
                    <p>Unable to load products. Please check back shortly.</p>
                </div>
            `;
        }
    },

    /**
     * Filter, sort, and render product cards.
     */
    filterAndRenderProducts() {
        const grid = document.getElementById('products-grid');
        if (!grid) return;

        let filtered = [...this.products];

        // Category filter
        if (this.activeCategory !== 'ALL') {
            filtered = filtered.filter(p => p.productType === this.activeCategory);
        }

        // Keyword search
        if (this.searchQuery) {
            filtered = filtered.filter(p =>
                p.name.toLowerCase().includes(this.searchQuery) ||
                (p.description && p.description.toLowerCase().includes(this.searchQuery)) ||
                (p.category && p.category.toLowerCase().includes(this.searchQuery))
            );
        }

        // Sorting
        if (this.sortBy === 'price-asc') {
            filtered.sort((a, b) => a.basePrice - b.basePrice);
        } else if (this.sortBy === 'price-desc') {
            filtered.sort((a, b) => b.basePrice - a.basePrice);
        } else if (this.sortBy === 'name') {
            filtered.sort((a, b) => a.name.localeCompare(b.name));
        } else {
            // Default: highest rated
            filtered.sort((a, b) => (b.rating || 0) - (a.rating || 0));
        }

        if (filtered.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 4rem 1rem;">
                    <i class="fa-solid fa-cookie-bite" style="font-size: 3rem; color: var(--color-accent); margin-bottom: 1rem;"></i>
                    <h4>No baked treats found</h4>
                    <p>Try searching with another keyword or resetting the category filter.</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = filtered.map(product => {
            const badgeClass = product.productType === 'CAKE' ? 'badge-cake' :
                               product.productType === 'PASTRY' ? 'badge-pastry' : 'badge-bread';
            
            return `
                <div class="product-card reveal">
                    <div class="product-media">
                        <span class="badge ${badgeClass} product-badge-float">${product.category || product.productType}</span>
                        <img class="product-img" src="${product.imageUrl}" alt="${product.name}" loading="lazy">
                        <button class="quick-view-overlay-btn" title="Quick View" onclick="ProductCatalog.openQuickView(${product.id})">
                            <i class="fa-solid fa-eye"></i>
                        </button>
                    </div>
                    <div class="product-body">
                        <div class="product-meta-row">
                            <span class="product-id-tag">#PRD-0${product.id}</span>
                            <span class="product-rating">
                                <i class="fa-solid fa-star"></i> ${product.rating ? product.rating.toFixed(1) : '5.0'} (${product.reviewCount || 12})
                            </span>
                            <span>${product.stockQuantity > 0 ? 'In Stock' : 'Out of Stock'}</span>
                        </div>
                        <h4 class="product-title" onclick="ProductCatalog.openQuickView(${product.id})">${product.name}</h4>
                        <p class="product-desc">${product.description || ''}</p>
                        <div class="product-footer">
                            <div class="product-price">${Crumb.formatCurrency(product.basePrice)} <span>/ each</span></div>
                            <button class="btn btn-primary btn-sm" onclick="ProductCatalog.addToCart(${product.id})">
                                <i class="fa-solid fa-cart-plus"></i> Add
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        Crumb.initScrollReveal();
    },

    /**
     * Open Quick-View Modal for a product.
     */
    async openQuickView(productId) {
        const product = this.products.find(p => p.id === productId);
        if (!product) return;

        const modal = document.getElementById('quick-view-modal');
        if (!modal) return;

        document.getElementById('qv-img').src = product.imageUrl;
        document.getElementById('qv-title').textContent = product.name;
        document.getElementById('qv-category').innerHTML = `${product.category || product.productType} &bull; <span class="product-id-tag" style="margin-left: 6px;">ID: #PRD-0${product.id}</span>`;
        document.getElementById('qv-desc').textContent = product.description;
        document.getElementById('qv-price').textContent = Crumb.formatCurrency(product.basePrice);
        document.getElementById('qv-allergens').textContent = product.allergens || 'None reported';
        document.getElementById('qv-storage').textContent = product.storageInstructions || 'Store in a cool dry place.';
        document.getElementById('qv-serving').textContent = product.servingRecommendation || 'Enjoy fresh!';

        // Volume discount note based on polymorphism
        const discountNote = document.getElementById('qv-discount-note');
        if (discountNote) {
            if (product.productType === 'CAKE') {
                discountNote.textContent = 'Buy 2 or more cakes and receive an automatic 5% party discount!';
            } else if (product.productType === 'PASTRY') {
                discountNote.textContent = 'Pastry Box Special: Order 6+ pastries for 10% off; order 12+ for 15% off!';
            } else if (product.productType === 'BREAD') {
                discountNote.textContent = 'Bakery Bundle: Order 3+ loaves and enjoy an automatic 5% bundle discount.';
            }
        }

        // Setup quantity input & dynamic price preview
        const qtyInput = document.getElementById('qv-qty-input');
        const finalPriceDisplay = document.getElementById('qv-total-price');
        
        const updatePrice = async () => {
            const qty = parseInt(qtyInput.value) || 1;
            try {
                const res = await Crumb.api(`/products/${productId}/price?quantity=${qty}`);
                if (res.success && res.data) {
                    finalPriceDisplay.textContent = Crumb.formatCurrency(res.data.finalPrice);
                }
            } catch (e) {
                finalPriceDisplay.textContent = Crumb.formatCurrency(product.basePrice * qty);
            }
        };

        if (qtyInput) {
            qtyInput.value = 1;
            qtyInput.oninput = updatePrice;
        }
        updatePrice();

        // Add to cart action in quick view
        const addBtn = document.getElementById('qv-add-cart-btn');
        if (addBtn) {
            addBtn.onclick = () => {
                const qty = parseInt(qtyInput.value) || 1;
                this.addToCart(productId, qty);
                Crumb.closeModal('quick-view-modal');
            };
        }

        Crumb.openModal('quick-view-modal');
    },

    /**
     * Shopping Cart Operations
     */
    addToCart(productId, quantity = 1) {
        const product = this.products.find(p => p.id === productId);
        if (!product) return;

        const existing = this.cart.find(item => item.id === productId);
        if (existing) {
            existing.quantity += quantity;
        } else {
            this.cart.push({
                id: product.id,
                name: product.name,
                basePrice: product.basePrice,
                imageUrl: product.imageUrl,
                productType: product.productType,
                quantity: quantity
            });
        }

        this.saveCartToStorage();
        this.renderCartUI();
        Crumb.showToast(`Added ${quantity}x "${product.name}" to cart!`, 'success');
    },

    updateCartItemQuantity(productId, delta) {
        const item = this.cart.find(i => i.id === productId);
        if (!item) return;

        item.quantity += delta;
        if (item.quantity <= 0) {
            this.cart = this.cart.filter(i => i.id !== productId);
        }

        this.saveCartToStorage();
        this.renderCartUI();
    },

    removeCartItem(productId) {
        this.cart = this.cart.filter(i => i.id !== productId);
        this.saveCartToStorage();
        this.renderCartUI();
        Crumb.showToast('Item removed from cart.', 'info');
    },

    clearCart() {
        this.cart = [];
        this.saveCartToStorage();
        this.renderCartUI();
    },

    saveCartToStorage() {
        localStorage.setItem('crumb_cart', JSON.stringify(this.cart));
    },

    loadCartFromStorage() {
        try {
            const raw = localStorage.getItem('crumb_cart');
            this.cart = raw ? JSON.parse(raw) : [];
        } catch (e) {
            this.cart = [];
        }
    },

    renderCartUI() {
        // Update header badges
        const totalCount = this.cart.reduce((sum, item) => sum + item.quantity, 0);
        document.querySelectorAll('.cart-badge').forEach(badge => {
            badge.textContent = totalCount;
            badge.style.display = totalCount > 0 ? 'flex' : 'none';
        });

        // Render Cart Drawer body
        const container = document.getElementById('cart-drawer-items');
        if (!container) return;

        if (this.cart.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 4rem 1rem; color: var(--text-muted);">
                    <i class="fa-solid fa-basket-shopping" style="font-size: 3rem; margin-bottom: 1rem; color: var(--color-accent-light);"></i>
                    <p style="font-weight: 600;">Your cart is currently empty.</p>
                    <p style="font-size: 0.85rem;">Discover our fresh loaves, croissants, and celebration cakes!</p>
                </div>
            `;
            this.updateCartTotals(0);
            return;
        }

        container.innerHTML = this.cart.map(item => `
            <div class="cart-item-row">
                <img class="cart-item-thumb" src="${item.imageUrl}" alt="${item.name}">
                <div class="cart-item-info">
                    <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem;">
                        <h5 class="cart-item-name">${item.name}</h5>
                        <span class="product-id-tag" style="font-size: 0.65rem; padding: 0.1rem 0.35rem;">#PRD-0${item.id}</span>
                    </div>
                    <div class="cart-item-price">${Crumb.formatCurrency(item.basePrice)} each</div>
                    <div class="cart-item-qty-row">
                        <button class="qty-btn" onclick="ProductCatalog.updateCartItemQuantity(${item.id}, -1)">-</button>
                        <span style="font-size: 0.9rem; font-weight: 700; min-width: 20px; text-align: center;">${item.quantity}</span>
                        <button class="qty-btn" onclick="ProductCatalog.updateCartItemQuantity(${item.id}, 1)">+</button>
                        <button style="margin-left: auto; background: none; border: none; color: var(--state-error); cursor: pointer; font-size: 0.85rem;" onclick="ProductCatalog.removeCartItem(${item.id})">
                            <i class="fa-solid fa-trash-can"></i>
                        </button>
                    </div>
                </div>
            </div>
        `).join('');

        const subtotal = this.cart.reduce((sum, item) => sum + (item.basePrice * item.quantity), 0);
        this.updateCartTotals(subtotal);
    },

    updateCartTotals(subtotal) {
        const subtotalEl = document.getElementById('cart-subtotal');
        if (subtotalEl) subtotalEl.textContent = Crumb.formatCurrency(subtotal);

        // VIP Discount preview
        const isVip = Crumb.currentUser && Crumb.currentUser.tier === 'PREMIUM';
        const vipBanner = document.getElementById('cart-vip-banner');
        const vipDiscountRow = document.getElementById('cart-vip-discount-row');
        const vipDiscountVal = document.getElementById('cart-vip-discount-val');
        const finalTotalEl = document.getElementById('cart-final-total');

        let discount = 0;
        if (isVip && subtotal > 0) {
            discount = subtotal * 0.10; // 10% VIP Gold Discount
            if (vipBanner) vipBanner.style.display = 'flex';
            if (vipDiscountRow) vipDiscountRow.style.display = 'flex';
            if (vipDiscountVal) vipDiscountVal.textContent = `-${Crumb.formatCurrency(discount)}`;
        } else {
            if (vipBanner) vipBanner.style.display = 'none';
            if (vipDiscountRow) vipDiscountRow.style.display = 'none';
        }

        if (finalTotalEl) {
            finalTotalEl.textContent = Crumb.formatCurrency(subtotal - discount);
        }
    },

    openCartDrawer() {
        this.renderCartUI();
        const drawer = document.getElementById('cart-drawer-overlay');
        if (drawer) drawer.classList.add('active');
        document.body.style.overflow = 'hidden';
    },

    closeCartDrawer() {
        const drawer = document.getElementById('cart-drawer-overlay');
        if (drawer) drawer.classList.remove('active');
        document.body.style.overflow = '';
    }
};

document.addEventListener('DOMContentLoaded', () => {
    ProductCatalog.init();
});
