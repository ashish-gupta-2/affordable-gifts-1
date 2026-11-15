package com.affordablegifts.backend.order;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A placed order. Everything here is a snapshot taken at checkout; it has no
 * links to the live catalogue so later price or name changes never alter it.
 */
@Entity
@Table(name = "orders")
public class CustomerOrder {

    @Id
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "request_id", nullable = false, unique = true)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ORDER_RECEIVED;

    // No payment gateway yet: orders are accepted unpaid and flagged PENDING.
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "PENDING";

    @Column(name = "packaging_id", nullable = false)
    private String packagingId;

    @Column(name = "packaging_name", nullable = false)
    private String packagingName;

    @Column(name = "packaging_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal packagingUnitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "products_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal productsSubtotal;

    @Column(name = "packaging_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal packagingTotal;

    @Column(name = "delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "total_weight_grams", nullable = false)
    private int totalWeightGrams;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Embedded
    private DeliveryAddress address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @OrderBy("slotIndex")
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    /** Pricing figures are computed by the service, never taken from the client. */
    public record Pricing(BigDecimal packagingUnitPrice, int quantity, BigDecimal productsSubtotal,
                          BigDecimal packagingTotal, BigDecimal deliveryCharge, BigDecimal total,
                          int totalWeightGrams) {
    }

    public CustomerOrder(String orderNumber, String requestId, String packagingId, String packagingName,
                         Pricing pricing, String customerName, String customerPhone,
                         String customerEmail, DeliveryAddress address, List<OrderItem> items) {
        this.id = UUID.randomUUID();
        this.orderNumber = orderNumber;
        this.requestId = requestId;
        this.packagingId = packagingId;
        this.packagingName = packagingName;
        this.packagingUnitPrice = pricing.packagingUnitPrice();
        this.quantity = pricing.quantity();
        this.productsSubtotal = pricing.productsSubtotal();
        this.packagingTotal = pricing.packagingTotal();
        this.deliveryCharge = pricing.deliveryCharge();
        this.total = pricing.total();
        this.totalWeightGrams = pricing.totalWeightGrams();
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.address = address;
        this.items = new ArrayList<>(items);
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void changePaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getRequestId() { return requestId; }
    public OrderStatus getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPackagingId() { return packagingId; }
    public String getPackagingName() { return packagingName; }
    public BigDecimal getPackagingUnitPrice() { return packagingUnitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getProductsSubtotal() { return productsSubtotal; }
    public BigDecimal getPackagingTotal() { return packagingTotal; }
    public BigDecimal getDeliveryCharge() { return deliveryCharge; }
    public BigDecimal getTotal() { return total; }
    public int getTotalWeightGrams() { return totalWeightGrams; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerEmail() { return customerEmail; }
    public DeliveryAddress getAddress() { return address; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
