package com.affordablegifts.backend.catalog.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public final class ProductDtos {

    private ProductDtos() {
    }

    /** Shape consumed by the storefront (matches the frontend `Product` type). */
    public record ProductResponse(String id, String name, ProductCategory category,
                                  String emoji, List<PackResponse> packs) {
        static ProductResponse from(Product product) {
            return new ProductResponse(product.getId(), product.getName(), product.getCategory(),
                    product.getEmoji(), packsOf(product));
        }
    }

    public record AdminProductResponse(String id, String name, ProductCategory category,
                                       String emoji, boolean active, int displayOrder,
                                       List<PackResponse> packs) {
        static AdminProductResponse from(Product product) {
            return new AdminProductResponse(product.getId(), product.getName(), product.getCategory(),
                    product.getEmoji(), product.isActive(), product.getDisplayOrder(), packsOf(product));
        }
    }

    public record PackResponse(int weight, BigDecimal price) {
    }

    public record PackRequest(
            @NotNull @Min(value = 1, message = "must be at least 1 gram") Integer weight,
            @NotNull @DecimalMin(value = "0.00", message = "must not be negative")
            @Digits(integer = 8, fraction = 2, message = "at most 2 decimal places") BigDecimal price) {
    }

    public record CreateProductRequest(
            @NotBlank @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*", message = "lowercase letters, digits and hyphens only")
            @Size(max = 64) String id,
            @NotBlank @Size(max = 120) String name,
            @NotNull ProductCategory category,
            @NotBlank @Size(max = 16) String emoji,
            Integer displayOrder,
            @NotEmpty List<@Valid PackRequest> packs) {
    }

    public record UpdateProductRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull ProductCategory category,
            @NotBlank @Size(max = 16) String emoji,
            Integer displayOrder,
            @NotNull Boolean active,
            @NotEmpty List<@Valid PackRequest> packs) {
    }

    private static List<PackResponse> packsOf(Product product) {
        return product.getPacks().stream()
                .sorted((a, b) -> Integer.compare(a.getWeight(), b.getWeight()))
                .map(pack -> new PackResponse(pack.getWeight(), pack.getPrice()))
                .toList();
    }
}
