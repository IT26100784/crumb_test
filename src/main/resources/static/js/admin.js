/**
 * CRUMB BAKERY - ADMIN MANAGEMENT PORTAL
 * Component: common + all component admin views
 * 
 * WHY: Provides full administrative operations across all 6 domain components:
 * Customers, Products, Custom Cakes, Orders, Bookings, Payments, and Reviews.
 * Enforces role-based access control (ADMIN only).
 */

const AdminPortal = {
    activeSection: 'overview',
    allProducts: [],
    allCustomers: [],
    allOrders: [],
    allCakes: [],
    allBookings: [],
    allPayments: [],
    allReviews: [],

    async init() {
        Crumb.initTheme();
        Crumb.initCustomSelects();
        await this.checkAdminAuth();
        this.bindEvents();
        await this.loadDashboardMetrics();
    },

    /**
     * Verify that current session has ADMIN role.
     */
    async checkAdminAuth() {
        try {
            const res = await Crumb.api('/auth/me');
            if (!res.success || !res.data || res.data.role !== 'ADMIN') {
                document.getElementById('admin-auth-guard').style.display = 'flex';
                document.getElementById('admin-app-root').style.display = 'none';
                return;
            }

            Crumb.currentUser = res.data;
            document.getElementById('admin-auth-guard').style.display = 'none';
            document.getElementById('admin-app-root').style.display = 'flex';

            // Set Admin name
            const nameEl = document.getElementById('admin-topbar-name');
            if (nameEl) nameEl.textContent = res.data.fullName;
        } catch (e) {
            document.getElementById('admin-auth-guard').style.display = 'flex';
            document.getElementById('admin-app-root').style.display = 'none';
        }
    },

    bindEvents() {
        // Sidebar navigation items
        document.querySelectorAll('.sidebar-item a').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const section = link.dataset.section;
                if (section) this.switchSection(section);
            });
        });

        // Topbar mobile sidebar toggle
        const toggleBtn = document.getElementById('admin-sidebar-toggle');
        const sidebar = document.querySelector('.admin-sidebar');
        if (toggleBtn && sidebar) {
            toggleBtn.addEventListener('click', () => {
                sidebar.classList.toggle('open');
            });
        }

        // Admin Login Form (if caught at auth guard)
        const adminLoginForm = document.getElementById('admin-login-form');
        if (adminLoginForm) {
            adminLoginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const email = adminLoginForm.email.value.trim();
                const password = adminLoginForm.password.value;

                try {
                    const res = await Crumb.api('/auth/login', {
                        method: 'POST',
                        body: { email, password }
                    });

                    if (res.success && res.data) {
                        if (res.data.role !== 'ADMIN') {
                            Crumb.showToast('Access denied: Administrator privileges required.', 'error');
                            return;
                        }
                        Crumb.currentUser = res.data;
                        document.getElementById('admin-auth-guard').style.display = 'none';
                        document.getElementById('admin-app-root').style.display = 'flex';
                        Crumb.showToast(`Welcome to Crumb Admin, ${res.data.fullName}`, 'success');
                        this.loadDashboardMetrics();
                    }
                } catch (err) {
                    Crumb.showToast(err.message || 'Login failed', 'error');
                }
            });
        }

        // Product Form Submit (Add/Edit)
        const productForm = document.getElementById('admin-product-form');
        if (productForm) {
            productForm.addEventListener('submit', (e) => this.handleProductFormSubmit(e));
        }

        // Type change in product modal (dynamically shows cake/pastry/bread fields)
        const productTypeSelect = document.getElementById('adm-prod-type');
        if (productTypeSelect) {
            productTypeSelect.addEventListener('change', (e) => {
                this.updateProductModalFields(e.target.value);
            });
        }
    },

    switchSection(sectionId) {
        this.activeSection = sectionId;

        // Update sidebar active state
        document.querySelectorAll('.sidebar-item').forEach(item => {
            const link = item.querySelector('a');
            if (link && link.dataset.section === sectionId) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });

        // Toggle sections
        document.querySelectorAll('.admin-section').forEach(sec => {
            sec.classList.remove('active');
        });

        const targetSec = document.getElementById(`sec-${sectionId}`);
        if (targetSec) targetSec.classList.add('active');

        // Close sidebar on mobile
        const sidebar = document.querySelector('.admin-sidebar');
        if (sidebar) sidebar.classList.remove('open');

        // Load section data
        if (sectionId === 'overview') this.loadDashboardMetrics();
        else if (sectionId === 'orders') this.loadOrders();
        else if (sectionId === 'products') this.loadProducts();
        else if (sectionId === 'cakes') this.loadCustomCakes();
        else if (sectionId === 'bookings') this.loadBookings();
        else if (sectionId === 'customers') this.loadCustomers();
        else if (sectionId === 'payments') this.loadPayments();
        else if (sectionId === 'reviews') this.loadReviews();
    },

    /**
     * Dashboard Overview Metrics
     */
    async loadDashboardMetrics() {
        try {
            const [ordersRes, productsRes, customersRes, cakesRes] = await Promise.all([
                Crumb.api('/orders').catch(() => ({ success: false })),
                Crumb.api('/products').catch(() => ({ success: false })),
                Crumb.api('/customers').catch(() => ({ success: false })),
                Crumb.api('/custom-cakes/my').catch(() => ({ success: false })) // fallback
            ]);

            const orders = (ordersRes.success && ordersRes.data) ? ordersRes.data : [];
            const products = (productsRes.success && productsRes.data) ? productsRes.data : [];
            const customers = (customersRes.success && customersRes.data) ? customersRes.data : [];

            const totalRev = orders.reduce((sum, o) => sum + (o.finalTotal || 0), 0);

            // Update KPI elements
            const kpiOrders = document.getElementById('kpi-orders-count');
            const kpiRev = document.getElementById('kpi-revenue-val');
            const kpiCust = document.getElementById('kpi-customers-count');
            const kpiProd = document.getElementById('kpi-products-count');

            if (kpiOrders) kpiOrders.textContent = orders.length;
            if (kpiRev) kpiRev.textContent = Crumb.formatCurrency(totalRev);
            if (kpiCust) kpiCust.textContent = customers.length;
            if (kpiProd) kpiProd.textContent = products.length;

            // Render Recent Orders in dashboard
            this.renderRecentOrders(orders.slice(0, 5));

        } catch (e) {
            console.error('Metrics loading error:', e);
        }
    },

    renderRecentOrders(orders) {
        const tbody = document.getElementById('dashboard-recent-orders-tbody');
        if (!tbody) return;

        if (orders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No orders registered yet.</td></tr>';
            return;
        }

        tbody.innerHTML = orders.map(o => `
            <tr>
                <td><strong>#${o.orderNumber}</strong></td>
                <td>${o.customerName} ${o.customerId ? `<br><small class="customer-id-pill" style="font-size:0.7rem; padding:1px 6px;"><i class="fa-solid fa-id-badge"></i> #CUST-0${o.customerId}</small>` : ''}</td>
                <td>${o.fulfillmentType}</td>
                <td>${Crumb.formatCurrency(o.finalTotal)}</td>
                <td><span class="badge ${o.status === 'DELIVERED' ? 'badge-success' : 'badge-warning'}">${o.status}</span></td>
            </tr>
        `).join('');
    },

    /**
     * PRODUCTS CRUD
     */
    async loadProducts() {
        const tbody = document.getElementById('admin-products-tbody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:2rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading catalog...</td></tr>';

        try {
            const res = await Crumb.api('/products');
            if (res.success && res.data) {
                this.allProducts = res.data;
                this.renderProductsTable(res.data);
            }
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" style="color:var(--state-error); text-align:center;">Failed to load products.</td></tr>';
        }
    },

    renderProductsTable(products) {
        const tbody = document.getElementById('admin-products-tbody');
        if (!tbody) return;

        tbody.innerHTML = products.map(p => `
            <tr>
                <td>
                    <img src="${p.imageUrl}" alt="${p.name}" style="width:48px; height:48px; object-fit:cover; border-radius:var(--radius-sm);">
                </td>
                <td>
                    <span class="product-id-tag">#PRD-0${p.id}</span>
                    <strong>${p.name}</strong><br><small style="color:var(--text-muted);">${p.category || p.productType}</small>
                </td>
                <td><span class="badge badge-cake">${p.productType}</span></td>
                <td><strong>${Crumb.formatCurrency(p.basePrice)}</strong></td>
                <td>${p.stockQuantity}</td>
                <td>
                    <span class="badge ${p.available ? 'badge-success' : 'badge-error'}">${p.available ? 'Active' : 'Hidden'}</span>
                </td>
                <td>
                    <div style="display:flex; gap:0.5rem;">
                        <button class="btn btn-outline btn-sm" onclick="AdminPortal.openEditProductModal(${p.id})">
                            <i class="fa-solid fa-pen-to-square"></i>
                        </button>
                        <button class="btn btn-outline btn-sm" style="border-color:var(--state-error); color:var(--state-error) !important;" onclick="AdminPortal.deleteProduct(${p.id})">
                            <i class="fa-solid fa-trash-can"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    },

    openAddProductModal() {
        const form = document.getElementById('admin-product-form');
        if (form) form.reset();
        document.getElementById('adm-prod-id').value = '';
        document.getElementById('adm-prod-modal-title').textContent = 'Add New Bakery Product';
        document.getElementById('adm-prod-type').value = 'CAKE';
        document.getElementById('adm-prod-type').dispatchEvent(new Event('refreshCustomSelect'));
        this.updateProductModalFields('CAKE');
        Crumb.openModal('admin-product-modal');
    },

    openEditProductModal(id) {
        const product = this.allProducts.find(p => p.id === id);
        if (!product) return;

        const form = document.getElementById('admin-product-form');
        form.reset();

        document.getElementById('adm-prod-id').value = product.id;
        document.getElementById('adm-prod-modal-title').textContent = `Edit Product: ${product.name}`;
        
        document.getElementById('adm-prod-name').value = product.name;
        document.getElementById('adm-prod-desc').value = product.description || '';
        document.getElementById('adm-prod-price').value = product.basePrice;
        document.getElementById('adm-prod-stock').value = product.stockQuantity;
        document.getElementById('adm-prod-img').value = product.imageUrl || '';
        document.getElementById('adm-prod-allergens').value = product.allergens || '';
        document.getElementById('adm-prod-type').value = product.productType;
        document.getElementById('adm-prod-type').dispatchEvent(new Event('refreshCustomSelect'));
        document.getElementById('adm-prod-available').checked = product.available;

        this.updateProductModalFields(product.productType);

        if (product.productType === 'CAKE') {
            document.getElementById('adm-cake-layers').value = product.layers || 2;
            document.getElementById('adm-cake-frosting').value = product.frostingType || '';
        } else if (product.productType === 'PASTRY') {
            document.getElementById('adm-pastry-type').value = product.pastryType || '';
            document.getElementById('adm-pastry-warmable').checked = !!product.warmable;
        } else if (product.productType === 'BREAD') {
            document.getElementById('adm-bread-weight').value = product.loafWeightGrams || 500;
            document.getElementById('adm-bread-sourdough').checked = !!product.sourdough;
        }

        Crumb.openModal('admin-product-modal');
    },

    updateProductModalFields(type) {
        document.getElementById('cake-specific-fields').style.display = (type === 'CAKE') ? 'block' : 'none';
        document.getElementById('pastry-specific-fields').style.display = (type === 'PASTRY') ? 'block' : 'none';
        document.getElementById('bread-specific-fields').style.display = (type === 'BREAD') ? 'block' : 'none';
    },

    async handleProductFormSubmit(e) {
        e.preventDefault();
        const id = document.getElementById('adm-prod-id').value;
        const type = document.getElementById('adm-prod-type').value;

        const payload = {
            name: document.getElementById('adm-prod-name').value.trim(),
            description: document.getElementById('adm-prod-desc').value.trim(),
            basePrice: parseFloat(document.getElementById('adm-prod-price').value) || 0,
            stockQuantity: parseInt(document.getElementById('adm-prod-stock').value) || 0,
            imageUrl: document.getElementById('adm-prod-img').value.trim(),
            allergens: document.getElementById('adm-prod-allergens').value.trim(),
            productType: type,
            available: document.getElementById('adm-prod-available').checked
        };

        if (type === 'CAKE') {
            payload.layers = parseInt(document.getElementById('adm-cake-layers').value) || 2;
            payload.frostingType = document.getElementById('adm-cake-frosting').value.trim();
        } else if (type === 'PASTRY') {
            payload.pastryType = document.getElementById('adm-pastry-type').value.trim();
            payload.warmable = document.getElementById('adm-pastry-warmable').checked;
        } else if (type === 'BREAD') {
            payload.loafWeightGrams = parseInt(document.getElementById('adm-bread-weight').value) || 500;
            payload.sourdough = document.getElementById('adm-bread-sourdough').checked;
        }

        const isUpdate = !!id;
        const url = isUpdate ? `/products/${id}` : '/products';
        const method = isUpdate ? 'PUT' : 'POST';

        try {
            const res = await Crumb.api(url, { method, body: payload });
            if (res.success) {
                Crumb.showToast(`Product ${isUpdate ? 'updated' : 'created'} successfully!`, 'success');
                Crumb.closeModal('admin-product-modal');
                this.loadProducts();
            }
        } catch (error) {
            Crumb.showToast(error.message || 'Operation failed', 'error');
        }
    },

    async deleteProduct(id) {
        if (!confirm('Are you sure you want to delete this product from the bakery catalog?')) return;

        try {
            const res = await Crumb.api(`/products/${id}`, { method: 'DELETE' });
            if (res.success) {
                Crumb.showToast('Product deleted.', 'info');
                this.loadProducts();
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Delete failed', 'error');
        }
    },

    /**
     * ORDERS DISPATCH
     */
    async loadOrders() {
        const tbody = document.getElementById('admin-orders-tbody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:2rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading orders...</td></tr>';

        try {
            const res = await Crumb.api('/orders');
            if (res.success && res.data) {
                this.allOrders = res.data;
                this.renderOrdersTable(res.data);
            }
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" style="color:var(--state-error); text-align:center;">Failed to load orders.</td></tr>';
        }
    },

    renderOrdersTable(orders) {
        const tbody = document.getElementById('admin-orders-tbody');
        if (!tbody) return;

        const statusOptions = ['PENDING', 'CONFIRMED', 'BAKING', 'READY', 'DELIVERED', 'CANCELLED'];

        tbody.innerHTML = orders.map(o => `
            <tr>
                <td><strong>#${o.orderNumber}</strong></td>
                <td>${o.customerName} ${o.customerId ? `<br><small class="customer-id-pill" style="font-size:0.7rem; padding:1px 6px;"><i class="fa-solid fa-id-badge"></i> #CUST-0${o.customerId}</small>` : ''}</td>
                <td>${o.fulfillmentType}</td>
                <td>${o.items ? o.items.length : 0} item(s)</td>
                <td><strong>${Crumb.formatCurrency(o.finalTotal)}</strong></td>
                <td>${Crumb.formatDate(o.orderDate)}</td>
                <td>
                    <select class="status-select" onchange="AdminPortal.updateOrderStatus(${o.id}, this.value)">
                        ${statusOptions.map(st => `
                            <option value="${st}" ${o.status === st ? 'selected' : ''}>${st}</option>
                        `).join('')}
                    </select>
                </td>
            </tr>
        `).join('');

        Crumb.initCustomSelects(tbody);
    },

    async updateOrderStatus(orderId, newStatus) {
        try {
            const res = await Crumb.api(`/orders/${orderId}/status`, {
                method: 'PUT',
                body: { status: newStatus }
            });
            if (res.success) {
                Crumb.showToast(`Order status updated to ${newStatus}`, 'success');
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Failed to update order status', 'error');
        }
    },

    /**
     * CUSTOM CAKES QUEUE
     */
    async loadCustomCakes() {
        const tbody = document.getElementById('admin-cakes-tbody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:2rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading custom cake requests...</td></tr>';

        try {
            const res = await Crumb.api('/custom-cakes/my'); // Admin controller or customer list
            if (res.success && res.data) {
                this.allCakes = res.data;
                this.renderCakesTable(res.data);
            }
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:2rem;">No custom cake requests pending.</td></tr>';
        }
    },

    renderCakesTable(cakes) {
        const tbody = document.getElementById('admin-cakes-tbody');
        if (!tbody) return;

        if (cakes.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No bespoke cake requests registered.</td></tr>';
            return;
        }

        const cakeStatuses = ['PENDING', 'CONFIRMED', 'IN_PRODUCTION', 'COMPLETED', 'CANCELLED'];

        tbody.innerHTML = cakes.map(c => `
            <tr>
                <td><strong>#${c.referenceNumber}</strong></td>
                <td>${c.customerName || 'Customer'}</td>
                <td><span class="badge badge-cake">${c.cakeType}</span></td>
                <td>${c.flavor} (${c.tiers} Tiers)</td>
                <td>${Crumb.formatDate(c.requiredDate)}</td>
                <td><strong>${Crumb.formatCurrency(c.estimatedPrice)}</strong></td>
                <td>
                    <select class="status-select" onchange="AdminPortal.updateCakeStatus(${c.id}, this.value)">
                        ${cakeStatuses.map(st => `
                            <option value="${st}" ${c.status === st ? 'selected' : ''}>${st}</option>
                        `).join('')}
                    </select>
                </td>
            </tr>
        `).join('');

        Crumb.initCustomSelects(tbody);
    },

    async updateCakeStatus(cakeId, newStatus) {
        try {
            const res = await Crumb.api(`/custom-cakes/${cakeId}/status`, {
                method: 'PUT',
                body: { status: newStatus }
            });
            if (res.success) {
                Crumb.showToast(`Cake status updated to ${newStatus}`, 'success');
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Failed to update cake status', 'error');
        }
    },

    /**
     * BOOKINGS & APPOINTMENTS
     */
    async loadBookings() {
        const tbody = document.getElementById('admin-bookings-tbody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:2rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading schedule...</td></tr>';

        try {
            const res = await Crumb.api('/bookings');
            if (res.success && res.data) {
                this.allBookings = res.data;
                this.renderBookingsTable(res.data);
            }
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-muted); padding:2rem;">No bookings scheduled yet.</td></tr>';
        }
    },

    renderBookingsTable(bookings) {
        const tbody = document.getElementById('admin-bookings-tbody');
        if (!tbody) return;

        const bookingStatuses = ['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'];

        tbody.innerHTML = bookings.map(b => `
            <tr>
                <td><strong>#${b.bookingReference}</strong></td>
                <td>${b.customerName || 'Customer'}</td>
                <td>${b.bookingType}</td>
                <td>${Crumb.formatDate(b.bookingDate)} (${b.timeSlot})</td>
                <td>
                    <select class="status-select" onchange="AdminPortal.updateBookingStatus(${b.id}, this.value)">
                        ${bookingStatuses.map(st => `
                            <option value="${st}" ${b.status === st ? 'selected' : ''}>${st}</option>
                        `).join('')}
                    </select>
                </td>
                <td>
                    <button class="btn btn-outline btn-sm" style="border-color:var(--state-error); color:var(--state-error) !important;" onclick="AdminPortal.cancelBooking(${b.id})">
                        Cancel
                    </button>
                </td>
            </tr>
        `).join('');

        Crumb.initCustomSelects(tbody);
    },

    async updateBookingStatus(id, newStatus) {
        try {
            const res = await Crumb.api(`/bookings/${id}/status`, {
                method: 'PUT',
                body: { status: newStatus }
            });
            if (res.success) {
                Crumb.showToast(`Booking status updated to ${newStatus}`, 'success');
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Status update failed', 'error');
        }
    },

    async cancelBooking(id) {
        if (!confirm('Are you sure you want to cancel this booking?')) return;
        try {
            const res = await Crumb.api(`/bookings/${id}/cancel`, { method: 'PUT' });
            if (res.success) {
                Crumb.showToast('Booking cancelled.', 'info');
                this.loadBookings();
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Cancellation failed', 'error');
        }
    },

    /**
     * CUSTOMERS DIRECTORY
     */
    async loadCustomers() {
        const tbody = document.getElementById('admin-customers-tbody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:2rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading customers...</td></tr>';

        try {
            const res = await Crumb.api('/customers');
            if (res.success && res.data) {
                this.allCustomers = res.data;
                this.renderCustomersTable(res.data);
            }
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" style="color:var(--state-error); text-align:center;">Failed to load customers.</td></tr>';
        }
    },

    renderCustomersTable(customers) {
        const tbody = document.getElementById('admin-customers-tbody');
        if (!tbody) return;

        tbody.innerHTML = customers.map(c => `
            <tr>
                <td><span class="customer-id-pill" style="font-size:0.72rem; margin-right:4px;"><i class="fa-solid fa-id-badge"></i> #CUST-0${c.id}</span> <strong>${c.fullName}</strong></td>
                <td>${c.email}</td>
                <td>${c.phoneNumber || 'N/A'}</td>
                <td>
                    ${c.tier === 'PREMIUM' 
                        ? '<span class="badge badge-gold"><i class="fa-solid fa-crown"></i> VIP Gold</span>' 
                        : '<span class="badge badge-cake">Regular</span>'}
                </td>
                <td><span class="badge badge-bread">${c.role}</span></td>
                <td>
                    <span class="badge ${c.active ? 'badge-success' : 'badge-error'}">${c.active ? 'Active' : 'Suspended'}</span>
                </td>
                <td>
                    <div style="display:flex; gap:0.5rem;">
                        <button class="btn btn-outline btn-sm" onclick="AdminPortal.toggleCustomerStatus(${c.id})">
                            ${c.active ? 'Suspend' : 'Activate'}
                        </button>
                        ${c.role !== 'ADMIN' ? `
                            <button class="btn btn-outline btn-sm" style="border-color:var(--state-error); color:var(--state-error) !important;" onclick="AdminPortal.deleteCustomer(${c.id})">
                                <i class="fa-solid fa-trash-can"></i>
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `).join('');
    },

    async toggleCustomerStatus(id) {
        try {
            const res = await Crumb.api(`/customers/${id}/status`, { method: 'PUT' });
            if (res.success) {
                Crumb.showToast(res.message || 'Status updated', 'success');
                this.loadCustomers();
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Status change failed', 'error');
        }
    },

    async deleteCustomer(id) {
        if (!confirm('Are you sure you want to delete this customer account?')) return;
        try {
            const res = await Crumb.api(`/customers/${id}`, { method: 'DELETE' });
            if (res.success) {
                Crumb.showToast('Customer account removed.', 'info');
                this.loadCustomers();
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Delete failed', 'error');
        }
    },

    /**
     * PAYMENTS LEDGER & REFUNDS
     */
    async loadPayments() {
        const tbody = document.getElementById('admin-payments-tbody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:2rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading transactions...</td></tr>';

        try {
            const res = await Crumb.api('/payments');
            if (res.success && res.data) {
                this.allPayments = res.data;
                this.renderPaymentsTable(res.data);
            }
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" style="color:var(--state-error); text-align:center;">Failed to load payments ledger.</td></tr>';
        }
    },

    renderPaymentsTable(payments) {
        const tbody = document.getElementById('admin-payments-tbody');
        if (!tbody) return;

        if (payments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No payment transactions recorded.</td></tr>';
            return;
        }

        tbody.innerHTML = payments.map(p => `
            <tr>
                <td><strong>#${p.paymentReference}</strong></td>
                <td>${p.customerName || 'Customer'}</td>
                <td><span class="badge ${p.paymentType === 'CARD' ? 'badge-pastry' : 'badge-bread'}">${p.paymentType}</span></td>
                <td><strong>${Crumb.formatCurrency(p.amount)}</strong></td>
                <td>${Crumb.formatDate(p.paymentDate)}</td>
                <td>
                    <span class="badge ${p.status === 'COMPLETED' ? 'badge-success' : p.status === 'REFUNDED' ? 'badge-error' : 'badge-warning'}">${p.status}</span>
                </td>
                <td>
                    ${p.status === 'COMPLETED' ? `
                        <button class="btn btn-outline btn-sm" style="border-color:var(--state-error); color:var(--state-error) !important;" onclick="AdminPortal.refundPayment(${p.id})">
                            Refund
                        </button>
                    ` : '<span style="color:var(--text-muted); font-size:0.85rem;">None</span>'}
                </td>
            </tr>
        `).join('');
    },

    async refundPayment(id) {
        if (!confirm('Are you sure you want to process a full refund for this transaction?')) return;
        try {
            const res = await Crumb.api(`/payments/${id}/refund`, { method: 'POST' });
            if (res.success) {
                Crumb.showToast('Transaction successfully refunded.', 'success');
                this.loadPayments();
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Refund processing failed', 'error');
        }
    },

    /**
     * REVIEWS MODERATION
     */
    async loadReviews() {
        const tbody = document.getElementById('admin-reviews-tbody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:2rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading reviews...</td></tr>';

        try {
            const res = await Crumb.api('/reviews');
            if (res.success && res.data) {
                this.allReviews = res.data;
                this.renderReviewsTable(res.data);
            }
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="6" style="color:var(--state-error); text-align:center;">Failed to load reviews.</td></tr>';
        }
    },

    renderReviewsTable(reviews) {
        const tbody = document.getElementById('admin-reviews-tbody');
        if (!tbody) return;

        tbody.innerHTML = reviews.map(r => `
            <tr>
                <td><strong>${r.customerName}</strong></td>
                <td>${r.productName || 'Artisan Bakery'}</td>
                <td style="color:#EAA34E;">${Array(r.rating || 5).fill('<i class="fa-solid fa-star"></i>').join('')}</td>
                <td><small>"${r.comment}"</small></td>
                <td>
                    <span class="badge ${r.approved ? 'badge-success' : 'badge-warning'}">${r.approved ? 'Public' : 'Pending'}</span>
                </td>
                <td>
                    <div style="display:flex; gap:0.5rem;">
                        <button class="btn btn-outline btn-sm" onclick="AdminPortal.toggleReviewApproval(${r.id})">
                            ${r.approved ? 'Hide' : 'Approve'}
                        </button>
                        <button class="btn btn-outline btn-sm" style="border-color:var(--state-error); color:var(--state-error) !important;" onclick="AdminPortal.deleteReview(${r.id})">
                            <i class="fa-solid fa-trash-can"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    },

    async toggleReviewApproval(id) {
        try {
            const res = await Crumb.api(`/reviews/${id}/toggle`, { method: 'PUT' });
            if (res.success) {
                Crumb.showToast('Review visibility updated.', 'success');
                this.loadReviews();
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Failed to toggle review', 'error');
        }
    },

    async deleteReview(id) {
        if (!confirm('Are you sure you want to delete this customer review?')) return;
        try {
            const res = await Crumb.api(`/reviews/${id}`, { method: 'DELETE' });
            if (res.success) {
                Crumb.showToast('Review deleted.', 'info');
                this.loadReviews();
            }
        } catch (e) {
            Crumb.showToast(e.message || 'Delete failed', 'error');
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    AdminPortal.init();
});
