/**
 * CRUMB BAKERY - PAYMENT & REVIEWS MODULE
 * Component: payment
 * 
 * WHY: Handles card validation, cash settlements, customer testimonials showcase,
 * and 5-star interactive rating submissions.
 */

const PaymentReviewModule = {
    selectedRating: 5,

    init() {
        this.bindEvents();
        this.fetchTestimonials();
    },

    bindEvents() {
        // Interactive Star Rating selection in review modal
        document.querySelectorAll('.star-rating-select i').forEach(star => {
            star.addEventListener('click', () => {
                const rating = parseInt(star.dataset.val) || 5;
                this.selectedRating = rating;
                this.updateStarRatingUI(rating);
            });

            star.addEventListener('mouseenter', () => {
                const rating = parseInt(star.dataset.val) || 5;
                this.updateStarRatingUI(rating);
            });
        });

        const starContainer = document.querySelector('.star-rating-select');
        if (starContainer) {
            starContainer.addEventListener('mouseleave', () => {
                this.updateStarRatingUI(this.selectedRating);
            });
        }

        // Review form submission
        const reviewForm = document.getElementById('submit-review-form');
        if (reviewForm) {
            reviewForm.addEventListener('submit', (e) => this.handleReviewSubmit(e));
        }

        // Open Review Modal button
        document.querySelectorAll('.open-review-modal-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                if (!Crumb.currentUser) {
                    Crumb.showToast('Please sign in to share your feedback with our bakery community.', 'warning');
                    CustomerAuth.openAuthModal();
                    return;
                }
                Crumb.openModal('review-modal');
            });
        });
    },

    updateStarRatingUI(val) {
        document.querySelectorAll('.star-rating-select i').forEach(star => {
            const starVal = parseInt(star.dataset.val);
            if (starVal <= val) {
                star.className = 'fa-solid fa-star';
                star.style.color = '#EAA34E';
            } else {
                star.className = 'fa-regular fa-star';
                star.style.color = 'var(--text-muted)';
            }
        });
    },

    /**
     * Fetch public testimonials for storefront showcase.
     */
    async fetchTestimonials() {
        const grid = document.getElementById('testimonials-grid');
        if (!grid) return;

        try {
            const res = await Crumb.api('/reviews/testimonials');
            if (res.success && res.data && res.data.length > 0) {
                grid.innerHTML = res.data.map(r => `
                    <div class="testimonial-card reveal">
                        <div class="testimonial-rating">
                            ${Array(r.rating || 5).fill('<i class="fa-solid fa-star"></i>').join('')}
                        </div>
                        <p class="testimonial-text">"${r.comment}"</p>
                        <div class="testimonial-author">
                            <div class="author-avatar">${r.customerName ? r.customerName[0] : 'C'}</div>
                            <div>
                                <h5 style="font-size: 0.95rem; margin-bottom: 2px;">${r.customerName}</h5>
                                <span style="font-size: 0.78rem; color: var(--text-muted);"><i class="fa-solid fa-circle-check" style="color: var(--state-success);"></i> Verified Foodie</span>
                            </div>
                        </div>
                    </div>
                `).join('');
                Crumb.initScrollReveal();
            }
        } catch (e) {
            console.warn('Testimonials load note:', e.message);
        }
    },

    /**
     * Submit review to REST API.
     */
    async handleReviewSubmit(e) {
        e.preventDefault();
        if (!Crumb.currentUser) return;

        const form = e.target;
        const comment = form.comment.value.trim();
        const productSelect = document.getElementById('review-product-select');
        const productId = (productSelect && productSelect.value) ? parseInt(productSelect.value) : 1;

        if (!comment) {
            Crumb.showToast('Please enter your review comments.', 'warning');
            return;
        }

        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Submitting...';

        try {
            const res = await Crumb.api('/reviews', {
                method: 'POST',
                body: {
                    productId: productId,
                    rating: this.selectedRating,
                    comment: comment
                }
            });

            if (res.success) {
                Crumb.showToast('Thank you! Your feedback has been submitted.', 'success');
                Crumb.closeModal('review-modal');
                form.reset();
                this.selectedRating = 5;
                this.updateStarRatingUI(5);
                this.fetchTestimonials();
            }
        } catch (error) {
            Crumb.showToast(error.message || 'Failed to submit review.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Submit Feedback';
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    PaymentReviewModule.init();
});
