/**
 * CRUMB BAKERY - ORDER & CHECKOUT MODULE
 * Component: order
 * 
 * WHY: Manages checkout flow (delivery vs pickup), cart order placement,
 * real-time order status tracking with visual timeline, and customer order history.
 */

window.OrderModule = {
    selectedFulfillment: 'DELIVERY',
    selectedPaymentMethod: 'CARD',

    init() {
        this.bindEvents();
    },

    bindEvents() {
        // Fulfillment toggle (Pickup vs Delivery)
        document.querySelectorAll('.fulfillment-toggle-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.fulfillment-toggle-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.selectedFulfillment = btn.dataset.method || 'DELIVERY';

                const addressGroup = document.getElementById('checkout-address-group');
                const deliveryFeeRow = document.getElementById('checkout-delivery-fee-row');
                
                if (addressGroup) {
                    addressGroup.style.display = (this.selectedFulfillment === 'DELIVERY') ? 'block' : 'none';
                }
                if (deliveryFeeRow) {
                    deliveryFeeRow.style.display = (this.selectedFulfillment === 'DELIVERY') ? 'flex' : 'none';
                }

                this.recalculateCheckoutTotals();
            });
        });

        // Payment Method toggle (Card vs Cash)
        document.querySelectorAll('.payment-method-card').forEach(card => {
            card.addEventListener('click', () => {
                document.querySelectorAll('.payment-method-card').forEach(c => c.classList.remove('active'));
                card.classList.add('active');
                this.selectedPaymentMethod = card.dataset.payment || 'CARD';

                const cardFields = document.getElementById('card-details-fields');
                if (cardFields) {
                    cardFields.style.display = (this.selectedPaymentMethod === 'CARD') ? 'block' : 'none';
                }
            });
        });

        // Tracking Form submission
        const trackForm = document.getElementById('order-tracking-form');
        if (trackForm) {
            trackForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const code = trackForm.orderNumber.value.trim();
                if (code) this.trackOrder(code);
            });
        }

        // Checkout submit button
        const placeOrderBtn = document.getElementById('place-order-btn');
        if (placeOrderBtn) {
            placeOrderBtn.addEventListener('click', () => this.submitCheckout());
        }
    },

    /**
     * Open Checkout Modal and pre-populate contact details.
     */
    openCheckoutModal() {
        if (!Crumb.currentUser) {
            Crumb.showToast('Please sign in or create an account to proceed with your order.', 'warning');
            CustomerAuth.openAuthModal();
            return;
        }

        const user = Crumb.currentUser;
        const nameInput = document.getElementById('checkout-name');
        const phoneInput = document.getElementById('checkout-phone');
        const addressInput = document.getElementById('checkout-address');
        const dateInput = document.getElementById('checkout-date');

        if (nameInput) nameInput.value = user.fullName || '';
        if (phoneInput) phoneInput.value = user.phone || user.phoneNumber || '';
        if (addressInput) addressInput.value = user.address || user.deliveryAddress || '';

        // Default tomorrow
        if (dateInput) {
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            dateInput.min = tomorrow.toISOString().split('T')[0];
            dateInput.value = tomorrow.toISOString().split('T')[0];
        }

        this.renderCheckoutItems();
        this.recalculateCheckoutTotals();
        Crumb.openModal('checkout-modal');
    },

    renderCheckoutItems() {
        const container = document.getElementById('checkout-items-summary');
        if (!container) return;

        const cart = ProductCatalog.cart;
        container.innerHTML = cart.map(item => `
            <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.9rem; margin-bottom: 0.45rem;">
                <span><span class="product-id-tag">#PRD-0${item.id}</span> ${item.quantity}x ${item.name}</span>
                <span style="font-weight: 700;">${Crumb.formatCurrency(item.basePrice * item.quantity)}</span>
            </div>
        `).join('');
    },

    recalculateCheckoutTotals() {
        const cart = ProductCatalog.cart;
        const subtotal = cart.reduce((sum, item) => sum + (item.basePrice * item.quantity), 0);
        
        let deliveryFee = 0;
        const isVip = Crumb.currentUser && (Crumb.currentUser.customerType === 'PREMIUM' || Crumb.currentUser.tier === 'PREMIUM');
        
        if (this.selectedFulfillment === 'DELIVERY') {
            deliveryFee = isVip ? 0.0 : 450.0; // VIP gets waived delivery fee, standard Sri Lanka delivery LKR 450.00
        }

        const discount = isVip ? (subtotal * 0.10) : 0;
        const finalTotal = Math.max(0, subtotal - discount + deliveryFee);

        const subtotalEl = document.getElementById('checkout-subtotal-val');
        const discountEl = document.getElementById('checkout-discount-val');
        const deliveryEl = document.getElementById('checkout-delivery-val');
        const totalEl = document.getElementById('checkout-total-val');

        if (subtotalEl) subtotalEl.textContent = Crumb.formatCurrency(subtotal);
        if (discountEl) discountEl.textContent = isVip ? `-${Crumb.formatCurrency(discount)} (VIP 10%)` : 'LKR 0.00';
        if (deliveryEl) deliveryEl.textContent = deliveryFee === 0 ? 'FREE' : Crumb.formatCurrency(deliveryFee);
        if (totalEl) totalEl.textContent = Crumb.formatCurrency(finalTotal);
    },

    /**
     * Submit Order and create payment transaction.
     */
    async submitCheckout() {
        const cart = ProductCatalog.cart;
        if (cart.length === 0) {
            Crumb.showToast('Your cart is empty.', 'error');
            return;
        }

        const nameInput = document.getElementById('checkout-name');
        const phoneInput = document.getElementById('checkout-phone');
        const addressInput = document.getElementById('checkout-address');
        const notesInput = document.getElementById('checkout-notes');
        const dateInput = document.getElementById('checkout-date');
        const slotInput = document.getElementById('checkout-slot');

        const address = (this.selectedFulfillment === 'DELIVERY') ? (addressInput ? addressInput.value.trim() : '') : 'Bakery Pickup Counter';
        if (this.selectedFulfillment === 'DELIVERY' && !address) {
            Crumb.showToast('Please provide a delivery address.', 'warning');
            return;
        }

        const payload = {
            orderType: this.selectedFulfillment,
            deliveryAddress: address,
            contactPhone: phoneInput ? phoneInput.value.trim() : '',
            orderNotes: notesInput ? notesInput.value.trim() : '',
            specialDeliveryInstructions: notesInput ? notesInput.value.trim() : '',
            pickupTimeSlot: slotInput ? slotInput.value : '',
            items: cart.map(item => ({
                productId: item.id,
                quantity: item.quantity
            }))
        };

        const placeOrderBtn = document.getElementById('place-order-btn');
        placeOrderBtn.disabled = true;
        placeOrderBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Confirming Order...';

        try {
            // 1. Create Order
            const orderRes = await Crumb.api('/orders', {
                method: 'POST',
                body: payload
            });

            if (!orderRes.success || !orderRes.data) {
                throw new Error(orderRes.message || 'Failed to place order.');
            }

            const order = orderRes.data;

            // 2. Process Payment
            let cardHolder = '';
            let lastFour = '';
            if (this.selectedPaymentMethod === 'CARD') {
                const cardNameInput = document.getElementById('card-holder-name');
                const cardNumberInput = document.getElementById('card-number-input');
                cardHolder = cardNameInput ? cardNameInput.value.trim() : 'Valued Customer';
                const rawNum = cardNumberInput ? cardNumberInput.value.replace(/\s/g, '') : '4242';
                lastFour = rawNum.slice(-4) || '4242';
            }

            const paymentPayload = {
                orderId: order.id,
                amount: order.totalAmount || order.finalTotal || order.subtotal,
                paymentType: this.selectedPaymentMethod,
                cardHolderName: cardHolder,
                cardLastFour: lastFour,
                billingAddress: address
            };

            await Crumb.api('/payments', {
                method: 'POST',
                body: paymentPayload
            }).catch(err => {
                console.warn('Payment recording note:', err.message);
            });

            // 3. Clear cart & close checkout
            ProductCatalog.clearCart();
            Crumb.closeModal('checkout-modal');

            // 4. Display Receipt Modal
            this.showOrderReceipt(order);
            Crumb.showToast(`Order confirmed! Reference: ${order.orderNumber}`, 'success');

        } catch (error) {
            Crumb.showToast(error.message || 'Error occurred while placing order.', 'error');
        } finally {
            placeOrderBtn.disabled = false;
            placeOrderBtn.innerHTML = '<i class="fa-solid fa-bag-shopping"></i> Complete & Place Order';
        }
    },

    showOrderReceipt(order) {
        const modal = document.getElementById('order-receipt-modal');
        if (!modal) return;

        document.getElementById('receipt-order-no').textContent = order.orderNumber;
        document.getElementById('receipt-date').textContent = Crumb.formatDate(order.createdAt || order.orderDate);
        
        const custId = order.customerId || (Crumb.currentUser ? Crumb.currentUser.id : null);
        const custIdBadge = custId ? `<span class="customer-id-pill" style="font-size: 0.72rem; margin-left: 6px;"><i class="fa-solid fa-id-badge"></i> #CUST-0${custId}</span>` : '';
        document.getElementById('receipt-customer-name').innerHTML = `${order.customerName || 'Valued Customer'} ${custIdBadge}`;
        
        document.getElementById('receipt-fulfillment').textContent = order.orderType || order.fulfillmentType || 'DELIVERY';
        document.getElementById('receipt-address').textContent = order.deliveryAddress || 'Bakery Pickup Counter';
        document.getElementById('receipt-total').textContent = Crumb.formatCurrency(order.totalAmount || order.finalTotal);

        const itemsBody = document.getElementById('receipt-items-body');
        if (itemsBody && order.items) {
            itemsBody.innerHTML = order.items.map(item => `
                <tr>
                    <td style="padding: 6px 0;"><span class="product-id-tag" style="font-size: 0.7rem;">#PRD-0${item.productId || item.id}</span> ${item.productName}</td>
                    <td style="text-align: center;">${item.quantity}</td>
                    <td style="text-align: right;">${Crumb.formatCurrency(item.subtotal)}</td>
                </tr>
            `).join('');
        }

        Crumb.openModal('order-receipt-modal');
    },

    /**
     * Live Tracking Search
     */
    async trackOrder(orderNumber) {
        const container = document.getElementById('tracking-result-panel');
        if (!container) return;

        container.style.display = 'block';
        container.innerHTML = `
            <div style="text-align: center; padding: 2rem;">
                <i class="fa-solid fa-spinner fa-spin" style="font-size: 2rem; color: var(--color-accent);"></i>
                <p style="margin-top: 0.5rem;">Locating order ${orderNumber}...</p>
            </div>
        `;

        try {
            const res = await Crumb.api(`/orders/track/${orderNumber}`);
            if (res.success && res.data) {
                this.renderTrackingTimeline(res.data);
            }
        } catch (error) {
            container.innerHTML = `
                <div style="text-align: center; padding: 2rem; color: var(--state-error);">
                    <i class="fa-solid fa-circle-exclamation" style="font-size: 2rem; margin-bottom: 0.5rem;"></i>
                    <h4>Order Not Found</h4>
                    <p>No order matching "${orderNumber}". Please double-check your receipt code.</p>
                </div>
            `;
        }
    },

    renderTrackingTimeline(order) {
        const container = document.getElementById('tracking-result-panel');
        if (!container) return;

        const stages = ['PENDING', 'CONFIRMED', 'BAKING', 'READY', 'DELIVERED'];
        const currentIdx = stages.indexOf(order.status);
        const isCancelled = order.status === 'CANCELLED';

        const fulfillment = order.orderType || order.fulfillmentType || 'DELIVERY';

        const stageTitles = {
            PENDING: 'Order Placed',
            CONFIRMED: 'Kitchen Confirmed',
            BAKING: 'Baking in Oven',
            READY: fulfillment === 'PICKUP' ? 'Ready for Pickup' : 'Out for Delivery',
            DELIVERED: fulfillment === 'PICKUP' ? 'Picked Up' : 'Delivered'
        };

        const progressPercent = isCancelled ? 0 : Math.max(0, (currentIdx / (stages.length - 1)) * 100);

        container.innerHTML = `
            <div class="timeline-card">
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding-bottom: 1rem;">
                    <div>
                        <h3 style="margin-bottom: 4px;">Order #${order.orderNumber}</h3>
                        <p style="font-size: 0.85rem;">Placed on ${Crumb.formatDate(order.createdAt || order.orderDate)} &bull; ${fulfillment} ${order.customerId ? `&bull; <span class="customer-id-pill" style="font-size: 0.72rem;"><i class="fa-solid fa-id-badge"></i> #CUST-0${order.customerId}</span>` : ''}</p>
                    </div>
                    <div>
                        <span class="badge ${isCancelled ? 'badge-error' : 'badge-success'}" style="font-size: 0.9rem; padding: 0.4rem 1rem;">
                            ${order.status}
                        </span>
                    </div>
                </div>

                ${isCancelled ? `
                    <div style="padding: 2rem; text-align: center; color: var(--state-error);">
                        <i class="fa-solid fa-ban" style="font-size: 2.5rem; margin-bottom: 0.75rem;"></i>
                        <h4>This order has been cancelled</h4>
                    </div>
                ` : `
                    <div class="order-stepper">
                        <div class="stepper-progress" style="width: ${progressPercent}%;"></div>
                        ${stages.map((st, idx) => {
                            let stateClass = '';
                            if (idx < currentIdx) stateClass = 'completed';
                            else if (idx === currentIdx) stateClass = 'active';

                            return `
                                <div class="step-node ${stateClass}">
                                    <div class="step-circle">
                                        <i class="fa-solid ${idx < currentIdx ? 'fa-check' : 'fa-cookie'}"></i>
                                    </div>
                                    <span class="step-title">${stageTitles[st]}</span>
                                </div>
                            `;
                        }).join('')}
                    </div>
                `}

                <div style="background: var(--bg-surface-alt); border-radius: var(--radius-md); padding: 1.25rem; margin-top: 1.5rem;">
                    <h5 style="margin-bottom: 0.75rem;">Order Summary</h5>
                    ${order.items.map(i => `
                        <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.88rem; margin-bottom: 0.4rem;">
                            <span><span class="product-id-tag" style="font-size: 0.7rem;">#PRD-0${i.productId || i.id}</span> ${i.quantity}x ${i.productName}</span>
                            <span>${Crumb.formatCurrency(i.subtotal)}</span>
                        </div>
                    `).join('')}
                    <div style="border-top: 1px solid var(--border-color); padding-top: 0.6rem; margin-top: 0.6rem; display: flex; justify-content: space-between; font-weight: 800;">
                        <span>Total Paid</span>
                        <span style="color: var(--color-primary);">${Crumb.formatCurrency(order.totalAmount || order.finalTotal)}</span>
                    </div>
                </div>

                ${order.status === 'PENDING' ? `
                    <div style="margin-top: 1.5rem; text-align: right;">
                        <button class="btn btn-outline" style="border-color: var(--state-error); color: var(--state-error) !important;" onclick="OrderModule.cancelPendingOrder(${order.id})">
                            <i class="fa-solid fa-xmark"></i> Cancel Order
                        </button>
                    </div>
                ` : ''}
            </div>
        `;
    },

    async cancelPendingOrder(orderId) {
        if (!confirm('Are you sure you wish to cancel this pending order?')) return;

        try {
            const res = await Crumb.api(`/orders/${orderId}/cancel`, { method: 'PUT' });
            if (res.success && res.data) {
                Crumb.showToast('Order has been cancelled.', 'info');
                this.renderTrackingTimeline(res.data);
            }
        } catch (error) {
            Crumb.showToast(error.message || 'Unable to cancel order.', 'error');
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    OrderModule.init();
});
