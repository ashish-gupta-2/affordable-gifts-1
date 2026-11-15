package com.affordablegifts.backend.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreSettingsService {

    private static final short SINGLETON_ID = 1;

    private final StoreSettingsRepository repository;

    public StoreSettingsService(StoreSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public StoreSettings get() {
        return repository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("store_settings row is missing"));
    }

    @Transactional
    public StoreSettings update(java.math.BigDecimal minOrderValue,
                                java.math.BigDecimal localDeliveryCharge,
                                java.math.BigDecimal indiaDeliveryCharge) {
        StoreSettings settings = get();
        settings.update(minOrderValue, localDeliveryCharge, indiaDeliveryCharge);
        return settings;
    }
}
