/**
 * CRUMB BAKERY - COMMON UTILITIES & APP CORE
 * Component: common
 * 
 * WHY: Provides unified API client, notification system (toasts),
 * Dark/Light theme switching, session persistence, and global UI helpers.
 */

const Crumb = {
    // Bakery configuration
    config: {
        apiBase: '/api',
        currencySymbol: '$',
        whatsappNumber: '+94771234567', // Standard bakery hotline
        whatsappMessage: 'Hi Crumb Bakery! I would like to inquire about ordering delicious artisan cakes and pastries.'
    },

    // Current authenticated user state cached in memory
    currentUser: null,

    /**
     * Centralized HTTP client wrapper.
     * Automatically handles JSON headers, session cookies, and standardized error parsing.
     */
    async api(endpoint, options = {}) {
        const url = endpoint.startsWith('http') ? endpoint : `${this.config.apiBase}${endpoint}`;
        
        const defaultHeaders = {
            'Accept': 'application/json'
        };

        if (options.body && !(options.body instanceof FormData)) {
            defaultHeaders['Content-Type'] = 'application/json';
            options.body = JSON.stringify(options.body);
        }

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...options.headers
            },
            credentials: 'same-origin' // Ensures HTTP session cookie is passed
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMessage = (data && data.message) ? data.message : `HTTP ${response.status} Request Failed`;
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            console.error(`[Crumb API Error] ${endpoint}:`, error.message);
            throw error;
        }
    },

    /**
     * Show animated toast notification on screen.
     * @param {string} message - Feedback message
     * @param {'success'|'error'|'warning'|'info'} type - Toast variant
     * @param {string} title - Optional title heading
     */
    showToast(message, type = 'info', title = null) {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        const iconMap = {
            success: 'fa-circle-check',
            error: 'fa-circle-exclamation',
            warning: 'fa-triangle-exclamation',
            info: 'fa-circle-info'
        };

        const defaultTitleMap = {
            success: 'Success',
            error: 'Error',
            warning: 'Attention',
            info: 'Information'
        };

        toast.innerHTML = `
            <i class="fa-solid ${iconMap[type] || 'fa-bell'} toast-icon"></i>
            <div class="toast-content">
                <div class="toast-title">${title || defaultTitleMap[type]}</div>
                <div>${message}</div>
            </div>
            <button class="toast-close" aria-label="Close">&times;</button>
        `;

        toast.querySelector('.toast-close').addEventListener('click', () => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 300);
        });

        container.appendChild(toast);

        // Auto dismiss after 4 seconds
        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(100%)';
                setTimeout(() => toast.remove(), 300);
            }
        }, 4200);
    },

    /**
     * Formats numeric price to currency string (e.g., $34.50).
     */
    formatCurrency(amount) {
        const val = typeof amount === 'number' ? amount : parseFloat(amount) || 0;
        return `${this.config.currencySymbol}${val.toFixed(2)}`;
    },

    /**
     * Format ISO datetime to friendly date.
     */
    formatDate(dateString) {
        if (!dateString) return 'N/A';
        const d = new Date(dateString);
        return d.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    },

    /**
     * Theme Manager (Dark / Light mode).
     * Saves preference to localStorage and respects OS preference.
     */
    initTheme() {
        const savedTheme = localStorage.getItem('crumb_theme');
        const systemPrefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        
        const activeTheme = savedTheme || (systemPrefersDark ? 'dark' : 'light');
        document.documentElement.setAttribute('data-theme', activeTheme);
        this.updateThemeIcons(activeTheme);

        // System theme listener
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
                if (!localStorage.getItem('crumb_theme')) {
                    const newTheme = e.matches ? 'dark' : 'light';
                    document.documentElement.setAttribute('data-theme', newTheme);
                    this.updateThemeIcons(newTheme);
                }
            });
        }
    },

    toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('crumb_theme', newTheme);
        this.updateThemeIcons(newTheme);
        this.showToast(`Switched to ${newTheme} mode`, 'info');
    },

    updateThemeIcons(theme) {
        document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
            const icon = btn.querySelector('i');
            if (icon) {
                if (theme === 'dark') {
                    icon.className = 'fa-solid fa-sun';
                } else {
                    icon.className = 'fa-solid fa-moon';
                }
            }
        });
    },

    /**
     * Initialize Floating WhatsApp Button
     */
    initWhatsApp() {
        const encodedMsg = encodeURIComponent(this.config.whatsappMessage);
        const link = `https://wa.me/${this.config.whatsappNumber.replace(/[^0-9]/g, '')}?text=${encodedMsg}`;
        
        document.querySelectorAll('.whatsapp-link').forEach(el => {
            el.href = link;
            el.target = '_blank';
            el.rel = 'noopener noreferrer';
        });
    },

    /**
     * Back to Top Button Controller
     */
    initBackToTop() {
        const btn = document.getElementById('back-to-top-btn');
        if (!btn) return;

        window.addEventListener('scroll', () => {
            if (window.scrollY > 300) {
                btn.classList.add('show');
            } else {
                btn.classList.remove('show');
            }
        });

        btn.addEventListener('click', () => {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    },

    /**
     * Scroll Reveal Animation Observer
     */
    initScrollReveal() {
        const elements = document.querySelectorAll('.reveal');
        if (!('IntersectionObserver' in window)) {
            elements.forEach(el => el.classList.add('revealed'));
            return;
        }

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('revealed');
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });

        elements.forEach(el => observer.observe(el));
    },

    /**
     * Open Modal by ID
     */
    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    },

    /**
     * Close Modal by ID
     */
    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    },

    /**
     * Close all active modals
     */
    closeAllModals() {
        document.querySelectorAll('.modal-overlay.active').forEach(modal => {
            modal.classList.remove('active');
        });
        document.body.style.overflow = '';
    }
};

// Global event listener for modal background clicks and escape key
document.addEventListener('DOMContentLoaded', () => {
    Crumb.initTheme();
    Crumb.initWhatsApp();
    Crumb.initBackToTop();
    Crumb.initScrollReveal();

    // Theme toggle buttons click
    document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
        btn.addEventListener('click', () => Crumb.toggleTheme());
    });

    // Close modal on background click
    document.querySelectorAll('.modal-overlay').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                Crumb.closeModal(modal.id);
            }
        });
    });

    // Close modal on close button click
    document.querySelectorAll('.modal-close, [data-modal-close]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const modal = btn.closest('.modal-overlay');
            if (modal) {
                Crumb.closeModal(modal.id);
            }
        });
    });

    // Close on Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            Crumb.closeAllModals();
        }
    });
});
