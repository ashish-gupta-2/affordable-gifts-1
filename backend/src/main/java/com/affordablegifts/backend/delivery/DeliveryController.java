package com.affordablegifts.backend.delivery;

import com.affordablegifts.backend.delivery.DeliveryService.Quote;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery-quote")
public class DeliveryController {

    private final DeliveryService service;

    public DeliveryController(DeliveryService service) {
        this.service = service;
    }

    @GetMapping
    public Quote quote(@RequestParam String pincode) {
        return service.quote(pincode);
    }
}
