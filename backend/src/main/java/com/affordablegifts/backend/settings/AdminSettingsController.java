package com.affordablegifts.backend.settings;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    public record AdminSettings(
            @NotNull @DecimalMin(value = "0.00", message = "must not be negative")
            @Digits(integer = 8, fraction = 2, message = "at most 2 decimal places") BigDecimal minOrderValue,
            @NotNull @DecimalMin(value = "0.00", message = "must not be negative")
            @Digits(integer = 8, fraction = 2, message = "at most 2 decimal places") BigDecimal localDeliveryCharge,
            @NotNull @DecimalMin(value = "0.00", message = "must not be negative")
            @Digits(integer = 8, fraction = 2, message = "at most 2 decimal places") BigDecimal indiaDeliveryCharge) {

        static AdminSettings from(StoreSettings s) {
            return new AdminSettings(s.getMinOrderValue(), s.getLocalDeliveryCharge(),
                    s.getIndiaDeliveryCharge());
        }
    }

    private final StoreSettingsService service;

    public AdminSettingsController(StoreSettingsService service) {
        this.service = service;
    }

    @GetMapping
    public AdminSettings get() {
        return AdminSettings.from(service.get());
    }

    @PutMapping
    public AdminSettings update(@Valid @RequestBody AdminSettings request) {
        return AdminSettings.from(service.update(request.minOrderValue(),
                request.localDeliveryCharge(), request.indiaDeliveryCharge()));
    }
}
