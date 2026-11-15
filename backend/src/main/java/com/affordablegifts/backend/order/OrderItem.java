package com.affordablegifts.backend.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** One filled slot of the box, with the name and price as they were at order time. */
@Embeddable
public class OrderItem {

    @Column(name = "slot_index", nullable = false)
    private int slotIndex;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "weight_grams", nullable = false)
    private int weight;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItem() {
    }

    public OrderItem(int slotIndex, String productId, String productName, int weight, BigDecimal unitPrice) {
        this.slotIndex = slotIndex;
        this.productId = productId;
        this.productName = productName;
        this.weight = weight;
        this.unitPrice = unitPrice;
    }

    public int getSlotIndex() { return slotIndex; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getWeight() { return weight; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
