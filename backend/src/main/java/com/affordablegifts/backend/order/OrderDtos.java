package com.affordablegifts.backend.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CustomerRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Pattern(regexp = "[6-9][0-9]{9}", message = "must be a 10-digit Indian mobile number")
            String phone,
            @Email @Size(max = 160) String email) {
    }

    public record AddressRequest(
            @NotBlank @Size(max = 120) String house,
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 120) String area,
            @NotBlank @Size(max = 80) String city,
            @NotBlank @Size(max = 80) String state,
            @NotBlank @Pattern(regexp = "[1-9][0-9]{5}", message = "must be a 6-digit PIN code") String pincode,
            @Size(max = 300) String instructions) {
    }

    /**
     * What the browser sends. Note there are no prices: the server looks every
     * price up itself. {@code productIds} lists one product per slot, in slot order.
     */
    public record PlaceOrderRequest(
            @NotBlank @Size(max = 64) String requestId,
            @NotBlank String packagingId,
            @NotNull @Min(1) @Max(50) Integer quantity,
            @NotEmpty @Size(max = 20) List<@NotBlank String> productIds,
            @NotNull @Valid CustomerRequest customer,
            @NotNull @Valid AddressRequest address) {
    }

    public record OrderItemResponse(int slot, String productId, String productName, int weight,
                                    BigDecimal unitPrice) {
    }

    public record OrderResponse(String orderNumber, OrderStatus status, String paymentStatus,
                                String packagingName, BigDecimal packagingUnitPrice, int quantity,
                                List<OrderItemResponse> items, BigDecimal productsSubtotal,
                                BigDecimal packagingTotal, BigDecimal deliveryCharge, BigDecimal total,
                                int totalWeightGrams, String customerName, String customerPhone,
                                String addressLine, Instant createdAt) {

        static OrderResponse from(CustomerOrder o) {
            DeliveryAddress a = o.getAddress();
            String addressLine = String.join(", ", a.getHouse(), a.getStreet(), a.getArea(),
                    a.getCity(), a.getState() + " " + a.getPincode());
            List<OrderItemResponse> items = o.getItems().stream()
                    .map(i -> new OrderItemResponse(i.getSlotIndex(), i.getProductId(),
                            i.getProductName(), i.getWeight(), i.getUnitPrice()))
                    .toList();
            return new OrderResponse(o.getOrderNumber(), o.getStatus(), o.getPaymentStatus(),
                    o.getPackagingName(), o.getPackagingUnitPrice(), o.getQuantity(), items,
                    o.getProductsSubtotal(), o.getPackagingTotal(), o.getDeliveryCharge(), o.getTotal(),
                    o.getTotalWeightGrams(), o.getCustomerName(), o.getCustomerPhone(), addressLine,
                    o.getCreatedAt());
        }
    }
}
