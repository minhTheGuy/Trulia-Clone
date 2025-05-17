/**
 * Stripe Configuration
 * 
 * This file contains Stripe keys for payment processing.
 * In a production environment, these should be stored in environment variables
 * and not committed to version control.
 */

const STRIPE_CONFIG = {
    // Public API Key - Can be safely used on the client
    PUBLISHABLE_KEY: 'pk_test_51ROgtQR5Ad8YeKfV6ls2aEK1yDnUksGvlEn1Zbj9usi0faG2Rte3SgpNAJgZVwWt7BZCqUVOGxakVtwJHOQ9qYgK00GzDVFDiP',
    
    // Secret API Key - NEVER use this on the client!
    // This should only be used on the server side
    // Included here for reference only
    SECRET_KEY: 'sk_test_51ROgtQR5Ad8YeKfV6oScczTb7mFnprPUphDnhGpoBcIZpzvU513fVt04txNYCwTqKcR29xUSuY0SpP3olQdbxy5J00IBmgBepn'
};

export default STRIPE_CONFIG; 