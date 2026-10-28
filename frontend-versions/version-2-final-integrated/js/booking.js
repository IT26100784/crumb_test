/**
 * CRUMB BAKERY - BOOKING & SCHEDULING MODULE
 * Component: booking
 * 
 * WHY: Manages kitchen preparation slot availability, capacity checking,
 * pickup/delivery appointment scheduling, and booking cancellations.
 */

const BookingModule = {
    selectedDate: '',
    selectedSlot: '10:00 AM - 12:00 PM',
    bookingType: 'PICKUP',

    init() {
        this.bindEvents();
    },

    bindEvents() {
        // Date selection change in scheduling forms
        const dateInputs = document.querySelectorAll('.booking-date-picker');
        dateInputs.forEach(input => {
            // Set min to tomorrow
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            input.min = tomorrow.toISOString().split('T')[0];
            
            input.addEventListener('change', (e) => {
                this.selectedDate = e.target.value;
                this.checkDateAvailability(this.selectedDate);
            });
        });
    },

    /**
     * Check time slot capacity against REST API.
     */
    async checkDateAvailability(dateStr) {
        if (!dateStr) return;
        const slotSelect = document.getElementById('checkout-slot') || document.getElementById('booking-slot-select');
        if (!slotSelect) return;

        slotSelect.disabled = true;
        slotSelect.innerHTML = '<option>Checking bakery capacity...</option>';

        try {
            const res = await Crumb.api(`/bookings/availability?date=${dateStr}`);
            if (res.success && res.data) {
                const availability = res.data;
                const standardSlots = [
                    '08:00 AM - 10:00 AM (Early Bake)',
                    '10:00 AM - 12:00 PM (Morning Peak)',
                    '12:00 PM - 02:00 PM (Afternoon Fresh)',
                    '02:00 PM - 04:00 PM (Tea Time Batch)',
                    '04:00 PM - 06:00 PM (Evening Pickup)'
                ];

                slotSelect.innerHTML = standardSlots.map(slot => {
                    const isFullyBooked = availability.fullyBooked;
                    return `<option value="${slot}" ${isFullyBooked ? 'disabled' : ''}>${slot} ${isFullyBooked ? '(Full)' : '(Available)'}</option>`;
                }).join('');
            }
        } catch (error) {
            slotSelect.innerHTML = `
                <option value="10:00 AM - 12:00 PM">10:00 AM - 12:00 PM (Available)</option>
                <option value="02:00 PM - 04:00 PM">02:00 PM - 04:00 PM (Available)</option>
            `;
        } finally {
            slotSelect.disabled = false;
        }
    },

    /**
     * Load Customer Bookings for Account modal.
     */
    async loadCustomerBookings() {
        const container = document.getElementById('customer-bookings-list');
        if (!container) return;

        container.innerHTML = '<div style="text-align:center; padding:1.5rem;"><i class="fa-solid fa-spinner fa-spin"></i> Loading appointments...</div>';

        try {
            const res = await Crumb.api('/bookings/my');
            if (res.success && res.data) {
                if (res.data.length === 0) {
                    container.innerHTML = '<p style="text-align:center; color: var(--text-muted); padding: 1.5rem;">No active bookings found.</p>';
                    return;
                }

                container.innerHTML = res.data.map(b => `
                    <div style="background: var(--bg-surface-alt); padding: 1rem; border-radius: var(--radius-md); margin-bottom: 0.75rem; border: 1px solid var(--border-color);">
                        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 0.35rem;">
                            <strong>#${b.bookingReference}</strong>
                            <span class="badge ${b.status === 'CONFIRMED' ? 'badge-success' : 'badge-warning'}">${b.status}</span>
                        </div>
                        <div style="font-size: 0.85rem; color: var(--text-secondary);">
                            <div><i class="fa-solid fa-calendar"></i> ${Crumb.formatDate(b.bookingDate)} &bull; ${b.timeSlot}</div>
                            <div><i class="fa-solid fa-truck"></i> ${b.bookingType}</div>
                        </div>
                    </div>
                `).join('');
            }
        } catch (error) {
            container.innerHTML = '<p style="color:var(--state-error); padding: 1rem;">Failed to load bookings.</p>';
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    BookingModule.init();
});
