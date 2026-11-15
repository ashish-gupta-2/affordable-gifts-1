package com.affordablegifts.backend.catalog.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** One pack size of a product and what it sells for. */
@Embeddable
public class PackPrice {

    @Column(name = "weight_grams", nullable = false)
    private int weight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected PackPrice() {
    }

    public PackPrice(int weight, BigDecimal price) {
        this.weight = weight;
        this.price = price;
    }

    public int getWeight() { return weight; }
    public BigDecimal getPrice() { return price; }
}
