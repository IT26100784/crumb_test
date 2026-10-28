/**
 * CRUMB BAKERY - COMMON UTILITIES & APP CORE
 * Component: common
 * 
 * WHY: Provides unified API client, notification system (toasts),
 * Dark/Light theme switching, session persistence, and global UI helpers.
 */

const Crumb = {
    // Bakery configuration (Sri Lanka Locale & Hotline)
    config: {
        apiBase: '/api',
        currencySymbol: 'LKR ',
        whatsappNumber: '+94728909091', // Standard bakery hotline
        whatsappMessage: 'Hi Crumb Bakery Sri Lanka! I would like to inquire about ordering delicious artisan cakes and pastries.'
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
     * Formats numeric price to Sri Lankan Rupees string (e.g., LKR 4,500.00).
     */
    formatCurrency(amount) {
        const val = typeof amount === 'number' ? amount : parseFloat(amount) || 0;
        return `${this.config.currencySymbol}${val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
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
        // Default to dark luxury artisan bakery theme matching brand aesthetic
        const activeTheme = savedTheme || 'dark';
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
    },

    /**
     * =========================================================================
     * CUSTOM DARK LUXURY GLASS CALENDAR & DATE PICKER
     * =========================================================================
     */
    initCustomDatePickers() {
        document.querySelectorAll('input[type="date"]').forEach(input => {
            if (input.dataset.customPickerAttached) return;
            input.dataset.customPickerAttached = 'true';

            // Wrap input in a relative container with a calendar icon
            const wrapper = document.createElement('div');
            wrapper.className = 'custom-date-wrapper';
            input.parentNode.insertBefore(wrapper, input);
            wrapper.appendChild(input);

            const calIcon = document.createElement('i');
            calIcon.className = 'fa-solid fa-calendar-days custom-date-icon';
            wrapper.appendChild(calIcon);

            // Create floating calendar popup
            const pickerPopup = document.createElement('div');
            pickerPopup.className = 'glass-calendar-popup';
            pickerPopup.style.display = 'none';
            document.body.appendChild(pickerPopup);

            let currentViewDate = new Date();

            const renderCalendar = () => {
                const year = currentViewDate.getFullYear();
                const month = currentViewDate.getMonth();
                const monthNames = [
                    'January', 'February', 'March', 'April', 'May', 'June',
                    'July', 'August', 'September', 'October', 'November', 'December'
                ];

                const firstDay = new Date(year, month, 1).getDay();
                // Adjust for Monday start (0=Sun -> 6, 1=Mon -> 0)
                const startDayOffset = (firstDay === 0 ? 6 : firstDay - 1);
                const daysInMonth = new Date(year, month + 1, 0).getDate();

                const minDateStr = input.min || new Date().toISOString().split('T')[0];
                const selectedVal = input.value;

                let html = `
                    <div class="calendar-header">
                        <button type="button" class="cal-nav-btn prev-month"><i class="fa-solid fa-chevron-left"></i></button>
                        <div class="cal-month-year">${monthNames[month]} ${year}</div>
                        <button type="button" class="cal-nav-btn next-month"><i class="fa-solid fa-chevron-right"></i></button>
                    </div>
                    <div class="cal-weekdays">
                        <span>Mo</span><span>Tu</span><span>We</span><span>Th</span><span>Fr</span><span>Sa</span><span>Su</span>
                    </div>
                    <div class="cal-days-grid">
                `;

                // Empty leading days
                for (let i = 0; i < startDayOffset; i++) {
                    html += `<div class="cal-day empty"></div>`;
                }

                // Days of the month
                for (let day = 1; day <= daysInMonth; day++) {
                    const monthStr = String(month + 1).padStart(2, '0');
                    const dayStr = String(day).padStart(2, '0');
                    const isoDateStr = `${year}-${monthStr}-${dayStr}`;

                    const isDisabled = (isoDateStr < minDateStr);
                    const isSelected = (isoDateStr === selectedVal);
                    const isToday = (isoDateStr === new Date().toISOString().split('T')[0]);

                    let classes = ['cal-day'];
                    if (isDisabled) classes.push('disabled');
                    if (isSelected) classes.push('selected');
                    if (isToday) classes.push('today');

                    html += `
                        <div class="${classes.join(' ')}" data-date="${isoDateStr}">
                            ${day}
                        </div>
                    `;
                }

                html += `</div>`;
                pickerPopup.innerHTML = html;

                // Bind calendar header nav
                pickerPopup.querySelector('.prev-month').addEventListener('click', (e) => {
                    e.stopPropagation();
                    currentViewDate.setMonth(currentViewDate.getMonth() - 1);
                    renderCalendar();
                });

                pickerPopup.querySelector('.next-month').addEventListener('click', (e) => {
                    e.stopPropagation();
                    currentViewDate.setMonth(currentViewDate.getMonth() + 1);
                    renderCalendar();
                });

                // Bind day clicks
                pickerPopup.querySelectorAll('.cal-day:not(.disabled):not(.empty)').forEach(cell => {
                    cell.addEventListener('click', (e) => {
                        e.stopPropagation();
                        const pickedDate = cell.dataset.date;
                        input.value = pickedDate;
                        input.dispatchEvent(new Event('change', { bubbles: true }));
                        input.dispatchEvent(new Event('input', { bubbles: true }));
                        closeCalendar();
                    });
                });
            };

            const openCalendar = () => {
                // Parse existing input value or min date
                const initVal = input.value || input.min;
                if (initVal) {
                    const parts = initVal.split('-');
                    if (parts.length === 3) {
                        currentViewDate = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, 1);
                    }
                }
                renderCalendar();

                const rect = input.getBoundingClientRect();
                pickerPopup.style.top = `${rect.bottom + window.scrollY + 6}px`;
                pickerPopup.style.left = `${Math.max(10, rect.left + window.scrollX)}px`;
                pickerPopup.style.display = 'block';
                pickerPopup.classList.add('active');
            };

            const closeCalendar = () => {
                pickerPopup.style.display = 'none';
                pickerPopup.classList.remove('active');
            };

            // Open on click
            input.addEventListener('click', (e) => {
                e.preventDefault();
                openCalendar();
            });

            calIcon.addEventListener('click', (e) => {
                e.preventDefault();
                openCalendar();
            });

            // Prevent native browser popup
            input.addEventListener('keydown', (e) => {
                if (e.key === ' ' || e.key === 'Enter') {
                    e.preventDefault();
                    openCalendar();
                }
            });

            // Close on outside click
            document.addEventListener('click', (e) => {
                if (!wrapper.contains(e.target) && !pickerPopup.contains(e.target)) {
                    closeCalendar();
                }
            });
        });
    },

    /**
     * =========================================================================
     * CUSTOM DARK LUXURY GLASS SELECT DROPDOWNS
     * =========================================================================
     */
    initCustomSelects(container = document) {
        if (!container || !container.querySelectorAll) return;
        container.querySelectorAll('select.form-select, select.sort-select, select.status-select').forEach(select => {
            if (select.dataset.customSelectAttached) return;
            select.dataset.customSelectAttached = 'true';

            // Hide native select visually while keeping in DOM for form submission & events
            select.style.display = 'none';

            const isCompact = select.classList.contains('status-select');
            const wrapper = document.createElement('div');
            wrapper.className = 'custom-select-wrapper' + (isCompact ? ' custom-select-compact' : '');
            select.parentNode.insertBefore(wrapper, select);
            wrapper.appendChild(select);

            // Trigger button
            const trigger = document.createElement('button');
            trigger.type = 'button';
            trigger.className = 'custom-select-trigger';
            const selectedOption = select.options[select.selectedIndex] || select.options[0];
            trigger.innerHTML = `
                <span class="custom-select-label">${selectedOption ? selectedOption.text : 'Select'}</span>
                <i class="fa-solid fa-chevron-down select-chevron"></i>
            `;
            wrapper.appendChild(trigger);

            // Dropdown menu
            const menu = document.createElement('div');
            menu.className = 'custom-select-menu';

            const buildOptions = () => {
                menu.innerHTML = Array.from(select.options).map((opt, idx) => `
                    <div class="custom-select-option ${opt.selected ? 'active' : ''}" data-value="${opt.value}" data-index="${idx}">
                        <span>${opt.text}</span>
                        <i class="fa-solid fa-check select-check"></i>
                    </div>
                `).join('');

                menu.querySelectorAll('.custom-select-option').forEach(item => {
                    item.addEventListener('click', (e) => {
                        e.stopPropagation();
                        const val = item.dataset.value;
                        select.value = val;
                        trigger.querySelector('.custom-select-label').textContent = item.querySelector('span').textContent;
                        
                        menu.querySelectorAll('.custom-select-option').forEach(o => o.classList.remove('active'));
                        item.classList.add('active');

                        select.dispatchEvent(new Event('change', { bubbles: true }));
                        if (typeof select.onchange === 'function') {
                            select.onchange.call(select, new Event('change', { bubbles: true }));
                        }
                        closeMenu();
                    });
                });
            };

            buildOptions();
            wrapper.appendChild(menu);

            const toggleMenu = () => {
                const isOpen = menu.classList.contains('active');
                if (isOpen) {
                    closeMenu();
                } else {
                    document.querySelectorAll('.custom-select-menu.active').forEach(m => m.classList.remove('active'));
                    document.querySelectorAll('.custom-select-trigger.active').forEach(t => t.classList.remove('active'));
                    document.querySelectorAll('.custom-select-wrapper.open').forEach(w => w.classList.remove('open'));
                    
                    // Position detection: if near bottom, open upward
                    const rect = trigger.getBoundingClientRect();
                    const spaceBelow = window.innerHeight - rect.bottom;
                    if (spaceBelow < 230 && rect.top > 230) {
                        menu.classList.add('open-upward');
                    } else {
                        menu.classList.remove('open-upward');
                    }

                    menu.classList.add('active');
                    trigger.classList.add('active');
                    wrapper.classList.add('open');
                }
            };

            const closeMenu = () => {
                menu.classList.remove('active');
                trigger.classList.remove('active');
                wrapper.classList.remove('open');
            };

            trigger.addEventListener('click', (e) => {
                e.stopPropagation();
                toggleMenu();
            });

            document.addEventListener('click', (e) => {
                if (!wrapper.contains(e.target)) {
                    closeMenu();
                }
            });

            // Allow external sync when select.value is changed programmatically
            select.addEventListener('refreshCustomSelect', () => {
                const curOpt = select.options[select.selectedIndex];
                if (curOpt) {
                    trigger.querySelector('.custom-select-label').textContent = curOpt.text;
                    menu.querySelectorAll('.custom-select-option').forEach(o => {
                        o.classList.toggle('active', o.dataset.value === select.value);
                    });
                }
            });
        });
    }
};

// Global event listener for modal background clicks and escape key
document.addEventListener('DOMContentLoaded', () => {
    Crumb.initTheme();
    Crumb.initWhatsApp();
    Crumb.initBackToTop();
    Crumb.initScrollReveal();
    Crumb.initCustomDatePickers();
    Crumb.initCustomSelects();

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
