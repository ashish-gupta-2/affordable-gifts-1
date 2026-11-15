package com.affordablegifts.backend.catalog.product;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String emoji;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_packs", joinColumns = @JoinColumn(name = "product_id"))
    private List<PackPrice> packs = new ArrayList<>();

    protected Product() {
    }

    public Product(String id, String name, ProductCategory category, String emoji,
                   int displayOrder, List<PackPrice> packs) {
        this.id = id;
        this.name = name;
        this.category = category.label();
        this.emoji = emoji;
        this.displayOrder = displayOrder;
        this.packs = new ArrayList<>(packs);
    }

    public void update(String name, ProductCategory category, String emoji,
                       int displayOrder, boolean active, List<PackPrice> packs) {
        this.name = name;
        this.category = category.label();
        this.emoji = emoji;
        this.displayOrder = displayOrder;
        this.active = active;
        this.packs.clear();
        this.packs.addAll(packs);
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public ProductCategory getCategory() { return ProductCategory.fromLabel(category); }
    public String getEmoji() { return emoji; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
    public List<PackPrice> getPacks() { return packs; }
}
