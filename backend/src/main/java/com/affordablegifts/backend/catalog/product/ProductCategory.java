package com.affordablegifts.backend.catalog.product;

import com.fasterxml.jackson.annotation.JsonValue;

/** Wire values match the frontend's `Product["category"]` union. */
public enum ProductCategory {
    DRY_FRUITS("Dry Fruits"),
    SNACKS("Snacks");

    private final String label;

    ProductCategory(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    public static ProductCategory fromLabel(String label) {
        for (ProductCategory category : values()) {
            if (category.label.equals(label)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + label);
    }
}
