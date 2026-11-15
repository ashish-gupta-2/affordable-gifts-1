package com.affordablegifts.backend.catalog.packaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "packaging_options")
public class Packaging {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String kind;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "slot_count", nullable = false)
    private int slotCount;

    @Column(name = "slot_capacity", nullable = false)
    private int slotCapacity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Packaging() {
    }

    public Packaging(String id, String name, PackagingKind kind, String description,
                     BigDecimal price, int slotCount, int slotCapacity, int displayOrder) {
        this.id = id;
        this.name = name;
        this.kind = kind.wire();
        this.description = description;
        this.price = price;
        this.slotCount = slotCount;
        this.slotCapacity = slotCapacity;
        this.displayOrder = displayOrder;
    }

    public void update(String name, PackagingKind kind, String description, BigDecimal price,
                       int slotCount, int slotCapacity, int displayOrder, boolean active) {
        this.name = name;
        this.kind = kind.wire();
        this.description = description;
        this.price = price;
        this.slotCount = slotCount;
        this.slotCapacity = slotCapacity;
        this.displayOrder = displayOrder;
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public PackagingKind getKind() { return PackagingKind.fromWire(kind); }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getSlotCount() { return slotCount; }
    public int getSlotCapacity() { return slotCapacity; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
}
