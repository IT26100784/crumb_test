/**
 * CRUMB BAKERY - CUSTOMER & AUTHENTICATION MODULE
 * Component: customer
 * 
 * WHY: Manages customer registration, login session, tabbed auth modal,
 * personal profile updates, and VIP Gold tier upgrade polymorphism.
 */

const CustomerAuth = {
    /**
     * Initialize customer auth module on page load.
     * Checks current session against /api/auth/me.
     */
    async init() {
        this.bindEvents();
        await this.checkCurrentSession();
    },

    /**
     * Binds form submit and modal toggle events.
     */
    bindEvents() {
        // Modal tab switching (Sign In vs Sign Up)
        const tabBtns = document.querySelectorAll('.auth-tab-btn');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const target = btn.dataset.tab;
                tabBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');

                document.querySelectorAll('.auth-tab-content').forEach(pane => {
                    pane.style.display = (pane.id === target) ? 'block' : 'none';
                });
            });
        });

        // Sign In Form Submit
        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Sign Up Form Submit
        const registerForm = document.getElementById('register-form');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }

        // Profile Form Submit
        const profileForm = document.getElementById('profile-form');
        if (profileForm) {
            profileForm.addEventListener('submit', (e) => this.handleProfileUpdate(e));
        }

        // VIP Upgrade Button
        const upgradeBtn = document.getElementById('upgrade-vip-btn');
        if (upgradeBtn) {
            upgradeBtn.addEventListener('click', () => this.handleVipUpgrade());
        }

        // Auth trigger buttons (Header "Sign In" button)
        document.querySelectorAll('.open-auth-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                this.openAuthModal(btn.dataset.defaultTab || 'login-tab');
            });
        });

        // Logout buttons
        document.querySelectorAll('.logout-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                this.handleLogout();
            });
        });

        // Profile Modal Open trigger
        document.querySelectorAll('.open-profile-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                this.openProfileModal();
            });
        });
    },

    /**
     * Open the Auth Modal and switch to the specified tab.
     */
    openAuthModal(tabId = 'login-tab') {
        Crumb.openModal('auth-modal');
        const tabBtn = document.querySelector(`.auth-tab-btn[data-tab="${tabId}"]`);
        if (tabBtn) tabBtn.click();
    },

    /**
     * Check if visitor has an active session on server.
     */
    async checkCurrentSession() {
        try {
            const res = await Crumb.api('/auth/me');
            if (res.success && res.data) {
                Crumb.currentUser = res.data;
                this.renderUserUI(res.data);
            } else {
                Crumb.currentUser = null;
                this.renderGuestUI();
            }
        } catch (error) {
            Crumb.currentUser = null;
            this.renderGuestUI();
        }
    },

    /**
     * Updates header navigation when user is logged in.
     */
    renderUserUI(user) {
        document.querySelectorAll('.guest-only').forEach(el => el.style.display = 'none');
        document.querySelectorAll('.user-only').forEach(el => el.style.display = 'flex');

        // Set user name and badges
        document.querySelectorAll('.user-display-name').forEach(el => {
            el.textContent = user.fullName ? user.fullName.split(' ')[0] : 'Foodie';
        });

        const isVip = (user.customerType === 'PREMIUM' || user.tier === 'PREMIUM');
        document.querySelectorAll('.vip-badge-display').forEach(el => {
            el.innerHTML = isVip
                ? '<span class="badge badge-gold"><i class="fa-solid fa-crown"></i> VIP Gold</span>'
                : '<span class="badge badge-cake">Regular Foodie</span>';
        });

        // Admin Link visibility
        const isAdmin = user.role === 'ADMIN';
        document.querySelectorAll('.admin-only').forEach(el => {
            el.style.display = isAdmin ? 'block' : 'none';
        });
    },

    /**
     * Reset header navigation to guest state.
     */
    renderGuestUI() {
        document.querySelectorAll('.guest-only').forEach(el => el.style.display = 'flex');
        document.querySelectorAll('.user-only').forEach(el => el.style.display = 'none');
        document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'none');
    },

    /**
     * Handle Sign In form submission.
     */
    async handleLogin(e) {
        e.preventDefault();
        const form = e.target;
        const email = form.email.value.trim();
        const password = form.password.value;

        if (!email || !password) {
            Crumb.showToast('Please enter both email and password.', 'warning');
            return;
        }

        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Signing In...';

        try {
            const res = await Crumb.api('/auth/login', {
                method: 'POST',
                body: { email, password }
            });

            if (res.success && res.data) {
                Crumb.currentUser = res.data;
                this.renderUserUI(res.data);
                Crumb.closeModal('auth-modal');
                form.reset();
                Crumb.showToast(`Welcome back, ${res.data.fullName}!`, 'success');

                // If user is Admin, prompt them or give shortcut to Admin Portal
                if (res.data.role === 'ADMIN') {
                    setTimeout(() => {
                        Crumb.showToast('Logged in as Administrator. Admin Portal is now available in your menu.', 'info');
                    }, 1200);
                }
            }
        } catch (error) {
            Crumb.showToast(error.message || 'Login failed. Please check your credentials.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Sign In to Crumb';
        }
    },

    /**
     * Handle Sign Up form submission.
     */
    async handleRegister(e) {
        e.preventDefault();
        const form = e.target;
        const fullName = form.fullName.value.trim();
        const email = form.email.value.trim();
        const phoneNumber = form.phoneNumber.value.trim();
        const deliveryAddress = form.deliveryAddress.value.trim();
        const password = form.password.value;
        const confirmPassword = form.confirmPassword.value;
        const vipUpgrade = form.vipOptIn ? form.vipOptIn.checked : false;

        if (password !== confirmPassword) {
            Crumb.showToast('Passwords do not match.', 'error');
            return;
        }
        if (password.length < 6) {
            Crumb.showToast('Password must be at least 6 characters long.', 'warning');
            return;
        }

        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Creating Account...';

        try {
            const res = await Crumb.api('/auth/register', {
                method: 'POST',
                body: {
                    fullName,
                    email,
                    phone: phoneNumber,
                    address: deliveryAddress,
                    password,
                    customerType: vipUpgrade ? 'PREMIUM' : 'REGULAR'
                }
            });

            if (res.success && res.data) {
                Crumb.currentUser = res.data;
                this.renderUserUI(res.data);
                Crumb.closeModal('auth-modal');
                form.reset();
                Crumb.showToast(`Account created! Welcome to Crumb, ${res.data.fullName}.`, 'success');
            }
        } catch (error) {
            Crumb.showToast(error.message || 'Registration failed. Email might already exist.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa-solid fa-user-plus"></i> Create My Account';
        }
    },

    /**
     * Handle Logout.
     */
    async handleLogout() {
        try {
            await Crumb.api('/auth/logout', { method: 'POST' });
        } catch (e) {
            // Proceed anyway
        }
        Crumb.currentUser = null;
        this.renderGuestUI();
        Crumb.showToast('You have been logged out successfully.', 'info');
    },

    /**
     * Open and populate Profile Modal.
     */
    openProfileModal() {
        if (!Crumb.currentUser) {
            this.openAuthModal();
            return;
        }

        const user = Crumb.currentUser;
        const form = document.getElementById('profile-form');
        if (form) {
            form.fullName.value = user.fullName || '';
            form.email.value = user.email || '';
            form.phoneNumber.value = user.phone || user.phoneNumber || '';
            form.deliveryAddress.value = user.address || user.deliveryAddress || '';
            if (form.password) form.password.value = '';
        }

        // Show VIP perks info
        const isVip = (user.customerType === 'PREMIUM' || user.tier === 'PREMIUM');
        const vipUpgradeContainer = document.getElementById('profile-vip-upgrade-box');
        if (vipUpgradeContainer) {
            vipUpgradeContainer.style.display = isVip ? 'none' : 'block';
        }

        const vipStatusText = document.getElementById('profile-tier-badge');
        if (vipStatusText) {
            vipStatusText.innerHTML = isVip
                ? '<span class="badge badge-gold"><i class="fa-solid fa-crown"></i> VIP Gold Member (10% Off All Orders + Free Delivery)</span>'
                : '<span class="badge badge-cake">Regular Customer Tier</span>';
        }

        Crumb.openModal('profile-modal');
    },

    /**
     * Handle Customer Profile Update.
     */
    async handleProfileUpdate(e) {
        e.preventDefault();
        if (!Crumb.currentUser) return;

        const form = e.target;
        const id = Crumb.currentUser.id;
        const fullName = form.fullName.value.trim();
        const phoneNumber = form.phoneNumber.value.trim();
        const deliveryAddress = form.deliveryAddress.value.trim();
        const password = form.password ? form.password.value : '';

        const payload = {
            fullName,
            phone: phoneNumber,
            address: deliveryAddress
        };
        if (password) payload.newPassword = password;

        try {
            const res = await Crumb.api(`/customers/${id}`, {
                method: 'PUT',
                body: payload
            });

            if (res.success && res.data) {
                Crumb.currentUser = res.data;
                this.renderUserUI(res.data);
                Crumb.showToast('Profile updated successfully!', 'success');
                Crumb.closeModal('profile-modal');
            }
        } catch (error) {
            Crumb.showToast(error.message || 'Failed to update profile.', 'error');
        }
    },

    /**
     * Handle VIP Gold Tier Upgrade (Dynamic Polymorphism demonstration).
     */
    async handleVipUpgrade() {
        if (!Crumb.currentUser) return;
        const id = Crumb.currentUser.id;

        try {
            const res = await Crumb.api(`/customers/${id}/upgrade`, {
                method: 'POST'
            });

            if (res.success && res.data) {
                Crumb.currentUser = res.data;
                this.renderUserUI(res.data);
                this.openProfileModal(); // Refresh modal view
                Crumb.showToast('🌟 VIP Gold Membership unlocked! Enjoy 10% off and complimentary delivery!', 'success');
            }
        } catch (error) {
            Crumb.showToast(error.message || 'VIP upgrade failed.', 'error');
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    CustomerAuth.init();
});
