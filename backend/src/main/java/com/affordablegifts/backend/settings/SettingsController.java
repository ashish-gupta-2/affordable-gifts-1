package com.affordablegifts.backend.settings;

import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The configuration values the storefront needs to display. */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    public record PublicSettings(BigDecimal minOrderValue) {
    }

    private final StoreSettingsService service;

    public SettingsController(StoreSettingsService service) {
        this.service = service;
    }

    @GetMapping
    public PublicSettings get() {
        return new PublicSettings(service.get().getMinOrderValue());
    }
}
