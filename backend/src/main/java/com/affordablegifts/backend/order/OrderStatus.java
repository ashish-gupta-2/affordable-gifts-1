package com.affordablegifts.backend.order;

/** Shipping stages from the PRD. Admins move orders along these by hand for now. */
public enum OrderStatus {
    ORDER_RECEIVED,
    PAYMENT_CONFIRMED,
    PREPARING,
    PACKED,
    DISPATCHED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
