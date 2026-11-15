package com.affordablegifts.backend.order;

import com.affordablegifts.backend.order.OrderDtos.OrderResponse;
import com.affordablegifts.backend.order.OrderDtos.PlaceOrderRequest;
import com.affordablegifts.backend.order.OrderService.PlacedOrder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    /** 201 for a new order, 200 when the same requestId was already placed. */
    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        PlacedOrder placed = service.place(request);
        return ResponseEntity.status(placed.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(placed.order());
    }
}
