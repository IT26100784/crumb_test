/**
 * CRUMB BAKERY - STATIC PROTOTYPE INTERACTION MODULE (VERSION 1)
 * Purpose: Initial UI prototype supporting dark/light mode toggle,
 * responsive navigation, modal dialogs, and custom inputs before backend integration.
 */

window.Crumb = {
    config: {
        currencySymbol: 'LKR ',
        whatsappNumber: '+94728909091'
    },

    /**
     * Format numbers into Sri Lankan Rupee currency format (e.g., LKR 6,500.00).
     */
    formatCurrency(amount) {
        const val = Number(amount) || 0;
        return `${this.config.currencySymbol}${val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    },

    /**
     * Show animated toast notification popup.
     */
    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let icon = 'fa-info-circle';
        if (type === 'success') icon = 'fa-check-circle';
        else if (type === 'error') icon = 'fa-exclamation-circle';
        else if (type === 'warning') icon = 'fa-triangle-exclamation';

        toast.innerHTML = `
            <i class="fa-solid ${icon}"></i>
            <span>${message}</span>
        `;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'toastSlideOut 0.3s forwards';
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    },

    /**
     * Modal Helper: Open modal by ID
     */
    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    },

    /**
     * Modal Helper: Close modal by ID
     */
    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    },

    /**
     * Dark/Light Mode Theme Handler
     */
    initTheme() {
        const savedTheme = localStorage.getItem('crumb-theme');
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        
        const theme = savedTheme || (prefersDark ? 'dark' : 'light');
        document.documentElement.setAttribute('data-theme', theme);
        this.updateThemeToggleIcon(theme);
    },

    toggleTheme() {
        const current = document.documentElement.getAttribute('data-theme') || 'dark';
        const nextTheme = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', nextTheme);
        localStorage.setItem('crumb-theme', nextTheme);
        this.updateThemeToggleIcon(nextTheme);
    },

    updateThemeToggleIcon(theme) {
        document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
            const icon = btn.querySelector('i');
            if (icon) {
                icon.className = theme === 'dark' ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
            }
        });
    },

    /**
     * Floating WhatsApp Action Button
     */
    initWhatsApp() {
        document.querySelectorAll('.whatsapp-link').forEach(link => {
            link.href = `https://wa.me/${this.config.whatsappNumber.replace(/[^0-9]/g, '')}`;
        });
    },

    /**
     * Back to Top Smooth Scroll Button
     */
    initBackToTop() {
        const btn = document.getElementById('back-to-top-btn');
        if (!btn) return;

        window.addEventListener('scroll', () => {
            if (window.scrollY > 400) {
                btn.classList.add('visible');
            } else {
                btn.classList.remove('visible');
            }
        }, { passive: true });

        btn.addEventListener('click', () => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    },

    /**
     * Scroll Reveal Animations
     */
    initScrollReveal() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('revealed');
                }
            });
        }, { threshold: 0.1 });

        document.querySelectorAll('.reveal').forEach(el => observer.observe(el));
    },

    /**
     * Custom Dark Glass Date Picker
     */
    initCustomDatePickers() {
        document.querySelectorAll('input[type="date"]').forEach(input => {
            if (input.dataset.customPickerAttached) return;
            input.dataset.customPickerAttached = 'true';
            input.readOnly = true;

            const wrapper = document.createElement('div');
            wrapper.className = 'custom-date-wrapper';
            input.parentNode.insertBefore(wrapper, input);
            wrapper.appendChild(input);

            const iconBtn = document.createElement('span');
            iconBtn.className = 'custom-date-icon';
            iconBtn.innerHTML = '<i class="fa-regular fa-calendar-days"></i>';
            wrapper.appendChild(iconBtn);

            const popup = document.createElement('div');
            popup.className = 'glass-calendar-popup';
            wrapper.appendChild(popup);

            let viewDate = new Date();
            let selectedDate = input.value ? new Date(input.value) : new Date();

            const renderCalendar = () => {
                const year = viewDate.getFullYear();
                const month = viewDate.getMonth();
                const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
                
                const firstDayIndex = new Date(year, month, 1).getDay();
                const daysInMonth = new Date(year, month + 1, 0).getDate();

                let html = `
                    <div class="calendar-header">
                        <button type="button" class="cal-nav-btn prev-month"><i class="fa-solid fa-chevron-left"></i></button>
                        <span class="cal-month-title">${monthNames[month]} ${year}</span>
                        <button type="button" class="cal-nav-btn next-month"><i class="fa-solid fa-chevron-right"></i></button>
                    </div>
                    <div class="calendar-weekdays">
                        <span>Su</span><span>Mo</span><span>Tu</span><span>We</span><span>Th</span><span>Fr</span><span>Sa</span>
                    </div>
                    <div class="calendar-days-grid">
                `;

                for (let i = 0; i < firstDayIndex; i++) {
                    html += `<div class="cal-day empty"></div>`;
                }

                const today = new Date();
                today.setHours(0, 0, 0, 0);

                for (let d = 1; d <= daysInMonth; d++) {
                    const thisDate = new Date(year, month, d);
                    const isPast = thisDate < today;
                    const isSelected = selectedDate && (thisDate.toDateString() === selectedDate.toDateString());

                    let cls = 'cal-day';
                    if (isPast) cls += ' disabled';
                    if (isSelected) cls += ' selected';

                    html += `<div class="${cls}" data-date="${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}">${d}</div>`;
                }

                html += `</div>`;
                popup.innerHTML = html;

                popup.querySelector('.prev-month').addEventListener('click', (e) => {
                    e.stopPropagation();
                    viewDate.setMonth(viewDate.getMonth() - 1);
                    renderCalendar();
                });

                popup.querySelector('.next-month').addEventListener('click', (e) => {
                    e.stopPropagation();
                    viewDate.setMonth(viewDate.getMonth() + 1);
                    renderCalendar();
                });

                popup.querySelectorAll('.cal-day:not(.disabled):not(.empty)').forEach(dayEl => {
                    dayEl.addEventListener('click', (e) => {
                        e.stopPropagation();
                        const pickedStr = dayEl.dataset.date;
                        input.value = pickedStr;
                        selectedDate = new Date(pickedStr);
                        popup.classList.remove('active');
                        input.dispatchEvent(new Event('change', { bubbles: true }));
                    });
                });
            };

            const togglePopup = () => {
                const isOpen = popup.classList.contains('active');
                document.querySelectorAll('.glass-calendar-popup.active').forEach(p => p.classList.remove('active'));
                if (!isOpen) {
                    renderCalendar();
                    popup.classList.add('active');
                }
            };

            wrapper.addEventListener('click', (e) => {
                if (!popup.contains(e.target)) {
                    togglePopup();
                }
            });

            document.addEventListener('click', (e) => {
                if (!wrapper.contains(e.target)) {
                    popup.classList.remove('active');
                }
            });
        });
    },

    /**
     * Custom Dark Glass Select Dropdowns
     */
    initCustomSelects(container = document) {
        if (!container || !container.querySelectorAll) return;
        container.querySelectorAll('select.form-select, select.sort-select, select.status-select').forEach(select => {
            if (select.dataset.customSelectAttached) return;
            select.dataset.customSelectAttached = 'true';

            select.style.display = 'none';

            const isCompact = select.classList.contains('status-select');
            const wrapper = document.createElement('div');
            wrapper.className = 'custom-select-wrapper' + (isCompact ? ' custom-select-compact' : '');
            select.parentNode.insertBefore(wrapper, select);
            wrapper.appendChild(select);

            const trigger = document.createElement('button');
            trigger.type = 'button';
            trigger.className = 'custom-select-trigger';
            const selectedOption = select.options[select.selectedIndex] || select.options[0];
            trigger.innerHTML = `
                <span class="custom-select-label">${selectedOption ? selectedOption.text : 'Select'}</span>
                <i class="fa-solid fa-chevron-down select-chevron"></i>
            `;
            wrapper.appendChild(trigger);

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
        });
    }
};

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
        btn.addEventListener('click', () => {
            const modal = btn.closest('.modal-overlay');
            if (modal) Crumb.closeModal(modal.id);
        });
    });

    // Auth modal open buttons (Guest Sign In button)
    document.querySelectorAll('.open-auth-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            Crumb.openModal('auth-modal');
        });
    });

    // Swiping Auth Tabs
    const tabBtns = document.querySelectorAll('.auth-tab-btn');
    const sliderPill = document.getElementById('auth-slider-pill');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.dataset.tab;
            tabBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            if (sliderPill) {
                sliderPill.style.transform = (target === 'register-tab') ? 'translateX(100%)' : 'translateX(0)';
            }

            document.querySelectorAll('.auth-tab-content').forEach(pane => {
                pane.style.display = (pane.id === target) ? 'block' : 'none';
            });
        });
    });

    // Mock cart trigger
    const cartTrigger = document.querySelector('.cart-trigger-btn');
    if (cartTrigger) {
        cartTrigger.addEventListener('click', () => {
            const drawer = document.getElementById('cart-drawer-overlay');
            if (drawer) drawer.classList.add('active');
        });
    }
    const closeCart = document.getElementById('close-cart-btn');
    if (closeCart) {
        closeCart.addEventListener('click', () => {
            const drawer = document.getElementById('cart-drawer-overlay');
            if (drawer) drawer.classList.remove('active');
        });
    }
});
