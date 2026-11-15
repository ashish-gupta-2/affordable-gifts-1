package com.affordablegifts.backend.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** The single row of store-wide configuration (id is always 1). */
@Entity
@Table(name = "store_settings")
public class StoreSettings {

    @Id
    private short id;

    @Column(name = "min_order_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "local_delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal localDeliveryCharge;

    @Column(name = "india_delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal indiaDeliveryCharge;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected StoreSettings() {
    }

    public void update(BigDecimal minOrderValue, BigDecimal localDeliveryCharge,
                       BigDecimal indiaDeliveryCharge) {
        this.minOrderValue = minOrderValue;
        this.localDeliveryCharge = localDeliveryCharge;
        this.indiaDeliveryCharge = indiaDeliveryCharge;
        this.updatedAt = Instant.now();
    }

    public BigDecimal getMinOrderValue() { return minOrderValue; }
    public BigDecimal getLocalDeliveryCharge() { return localDeliveryCharge; }
    public BigDecimal getIndiaDeliveryCharge() { return indiaDeliveryCharge; }
}
