package com.affordablegifts.backend.delivery;

import com.affordablegifts.backend.settings.StoreSettings;
import com.affordablegifts.backend.settings.StoreSettingsService;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Delivery pricing by PIN code. Deliberately simple for the first release:
 * Bengaluru (560xxx) gets the local rate and every other valid PIN gets the
 * India-wide rate. Real per-PIN serviceability comes with a courier integration.
 */
@Service
public class DeliveryService {

    private static final Pattern PINCODE = Pattern.compile("[1-9][0-9]{5}");
    private static final String BENGALURU_PREFIX = "560";

    public record Quote(String pincode, String zone, BigDecimal charge) {
    }

    private final StoreSettingsService settingsService;

    public DeliveryService(StoreSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public Quote quote(String pincode) {
        if (pincode == null || !PINCODE.matcher(pincode).matches()) {
            throw new IllegalArgumentException("PIN code must be 6 digits");
        }
        StoreSettings settings = settingsService.get();
        if (pincode.startsWith(BENGALURU_PREFIX)) {
            return new Quote(pincode, "local", settings.getLocalDeliveryCharge());
        }
        return new Quote(pincode, "india", settings.getIndiaDeliveryCharge());
    }
}
