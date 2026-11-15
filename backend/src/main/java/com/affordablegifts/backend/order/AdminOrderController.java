package com.affordablegifts.backend.order;

import com.affordablegifts.backend.common.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Order management for the admin console (guarded by the admin key interceptor). */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    public record OrderSummary(String orderNumber, OrderStatus status, String paymentStatus,
                               String customerName, String customerPhone, String packagingName,
                               int quantity, BigDecimal total, Instant createdAt) {
        static OrderSummary from(CustomerOrder o) {
            return new OrderSummary(o.getOrderNumber(), o.getStatus(), o.getPaymentStatus(),
                    o.getCustomerName(), o.getCustomerPhone(), o.getPackagingName(),
                    o.getQuantity(), o.getTotal(), o.getCreatedAt());
        }
    }

    public record OrderDetail(OrderDtos.OrderResponse order, String customerEmail,
                              String deliveryInstructions) {
        static OrderDetail from(CustomerOrder o) {
            return new OrderDetail(OrderDtos.OrderResponse.from(o), o.getCustomerEmail(),
                    o.getAddress().getInstructions());
        }
    }

    /** Both fields are optional, but at least one must be present. */
    public record UpdateOrderRequest(
            OrderStatus status,
            @Pattern(regexp = "PENDING|PAID", message = "must be PENDING or PAID") String paymentStatus) {
    }

    private final OrderRepository orders;

    public AdminOrderController(OrderRepository orders) {
        this.orders = orders;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<OrderSummary> list(@RequestParam(required = false) OrderStatus status) {
        List<CustomerOrder> found = status == null
                ? orders.findAllByOrderByCreatedAtDesc()
                : orders.findByStatusOrderByCreatedAtDesc(status);
        return found.stream().map(OrderSummary::from).toList();
    }

    @GetMapping("/{orderNumber}")
    @Transactional(readOnly = true)
    public OrderDetail get(@PathVariable String orderNumber) {
        return OrderDetail.from(find(orderNumber));
    }

    @PatchMapping("/{orderNumber}")
    @Transactional
    public OrderDetail update(@PathVariable String orderNumber,
                              @Valid @RequestBody UpdateOrderRequest request) {
        if (request.status() == null && request.paymentStatus() == null) {
            throw new IllegalArgumentException("Provide a status or a paymentStatus");
        }
        CustomerOrder order = find(orderNumber);

        if (request.status() != null && request.status() != order.getStatus()) {
            boolean closed = order.getStatus() == OrderStatus.DELIVERED
                    || order.getStatus() == OrderStatus.CANCELLED;
            if (closed) {
                throw new IllegalArgumentException(
                        "Order is already " + order.getStatus() + " and can no longer change status");
            }
            order.changeStatus(request.status());
        }
        if (request.paymentStatus() != null) {
            order.changePaymentStatus(request.paymentStatus());
        }
        return OrderDetail.from(order);
    }

    private CustomerOrder find(String orderNumber) {
        return orders.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order '" + orderNumber + "' not found"));
    }
}
