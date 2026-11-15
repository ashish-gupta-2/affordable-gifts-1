package com.affordablegifts.backend.catalog.packaging;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class PackagingDtos {

    private PackagingDtos() {
    }

    /** Shape consumed by the storefront (matches the frontend `Packaging` type). */
    public record PackagingResponse(String id, String name, PackagingKind kind, String description,
                                    BigDecimal price, int slotCount, int slotCapacity) {
        static PackagingResponse from(Packaging p) {
            return new PackagingResponse(p.getId(), p.getName(), p.getKind(), p.getDescription(),
                    p.getPrice(), p.getSlotCount(), p.getSlotCapacity());
        }
    }

    public record AdminPackagingResponse(String id, String name, PackagingKind kind, String description,
                                         BigDecimal price, int slotCount, int slotCapacity,
                                         boolean active, int displayOrder) {
        static AdminPackagingResponse from(Packaging p) {
            return new AdminPackagingResponse(p.getId(), p.getName(), p.getKind(), p.getDescription(),
                    p.getPrice(), p.getSlotCount(), p.getSlotCapacity(), p.isActive(), p.getDisplayOrder());
        }
    }

    public record CreatePackagingRequest(
            @NotBlank @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*", message = "lowercase letters, digits and hyphens only")
            @Size(max = 64) String id,
            @NotBlank @Size(max = 120) String name,
            @NotNull PackagingKind kind,
            @NotBlank @Size(max = 200) String description,
            @NotNull @DecimalMin(value = "0.00", message = "must not be negative")
            @Digits(integer = 8, fraction = 2, message = "at most 2 decimal places") BigDecimal price,
            @NotNull @Min(value = 1, message = "must be at least 1") Integer slotCount,
            @NotNull @Min(value = 1, message = "must be at least 1 gram") Integer slotCapacity,
            Integer displayOrder) {
    }

    public record UpdatePackagingRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull PackagingKind kind,
            @NotBlank @Size(max = 200) String description,
            @NotNull @DecimalMin(value = "0.00", message = "must not be negative")
            @Digits(integer = 8, fraction = 2, message = "at most 2 decimal places") BigDecimal price,
            @NotNull @Min(value = 1, message = "must be at least 1") Integer slotCount,
            @NotNull @Min(value = 1, message = "must be at least 1 gram") Integer slotCapacity,
            Integer displayOrder,
            @NotNull Boolean active) {
    }
}
