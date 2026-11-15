package com.affordablegifts.backend.order;

import com.affordablegifts.backend.catalog.packaging.Packaging;
import com.affordablegifts.backend.catalog.packaging.PackagingRepository;
import com.affordablegifts.backend.catalog.product.PackPrice;
import com.affordablegifts.backend.catalog.product.Product;
import com.affordablegifts.backend.catalog.product.ProductRepository;
import com.affordablegifts.backend.common.NotFoundException;
import com.affordablegifts.backend.delivery.DeliveryService;
import com.affordablegifts.backend.order.CustomerOrder.Pricing;
import com.affordablegifts.backend.order.OrderDtos.AddressRequest;
import com.affordablegifts.backend.order.OrderDtos.OrderResponse;
import com.affordablegifts.backend.order.OrderDtos.PlaceOrderRequest;
import com.affordablegifts.backend.settings.StoreSettingsService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final PackagingRepository packagingRepository;
    private final ProductRepository productRepository;
    private final DeliveryService delivery;
    private final StoreSettingsService settings;

    public OrderService(OrderRepository orders, PackagingRepository packagingRepository,
                        ProductRepository productRepository, DeliveryService delivery,
                        StoreSettingsService settings) {
        this.orders = orders;
        this.packagingRepository = packagingRepository;
        this.productRepository = productRepository;
        this.delivery = delivery;
        this.settings = settings;
    }

    /** Result of placing an order; {@code created} is false when a retry hit an existing order. */
    public record PlacedOrder(OrderResponse order, boolean created) {
    }

    @Transactional
    public PlacedOrder place(PlaceOrderRequest request) {
        // Same requestId = same checkout attempt (double click, retry): return the original.
        Optional<CustomerOrder> existing = orders.findByRequestId(request.requestId());
        if (existing.isPresent()) {
            return new PlacedOrder(OrderResponse.from(existing.get()), false);
        }

        Packaging packaging = packagingRepository.findById(request.packagingId())
                .filter(Packaging::isActive)
                .orElseThrow(() -> new NotFoundException(
                        "Packaging '" + request.packagingId() + "' is not available"));

        if (request.productIds().size() != packaging.getSlotCount()) {
            throw new IllegalArgumentException("%s needs exactly %d products, one per slot"
                    .formatted(packaging.getName(), packaging.getSlotCount()));
        }

        List<OrderItem> items = buildItems(request.productIds(), packaging.getSlotCapacity());

        int quantity = request.quantity();
        BigDecimal setPrice = items.stream().map(OrderItem::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal productsSubtotal = setPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal minimum = settings.get().getMinOrderValue();
        if (productsSubtotal.compareTo(minimum) < 0) {
            throw new IllegalArgumentException(
                    "Products worth at least Rs %s are required (you have Rs %s)"
                            .formatted(minimum.toPlainString(), productsSubtotal.toPlainString()));
        }

        BigDecimal packagingTotal = packaging.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal deliveryCharge = delivery.quote(request.address().pincode()).charge();
        BigDecimal total = productsSubtotal.add(packagingTotal).add(deliveryCharge);
        int totalWeight = packaging.getSlotCount() * packaging.getSlotCapacity() * quantity;

        AddressRequest a = request.address();
        CustomerOrder order = new CustomerOrder(
                "AG-" + orders.nextOrderNumber(),
                request.requestId(),
                packaging.getId(),
                packaging.getName(),
                new Pricing(packaging.getPrice(), quantity, productsSubtotal, packagingTotal,
                        deliveryCharge, total, totalWeight),
                request.customer().name().trim(),
                request.customer().phone(),
                blankToNull(request.customer().email()),
                new DeliveryAddress(a.house().trim(), a.street().trim(), a.area().trim(),
                        a.city().trim(), a.state().trim(), a.pincode(), blankToNull(a.instructions())),
                items);

        return new PlacedOrder(OrderResponse.from(orders.save(order)), true);
    }

    private List<OrderItem> buildItems(List<String> productIds, int slotCapacity) {
        Map<String, Product> byId = productRepository.findAllById(productIds.stream().distinct().toList())
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<OrderItem> items = new ArrayList<>();
        for (int slot = 0; slot < productIds.size(); slot++) {
            String productId = productIds.get(slot);
            Product product = byId.get(productId);
            if (product == null || !product.isActive()) {
                throw new IllegalArgumentException("Product '" + productId + "' is not available");
            }
            PackPrice pack = product.getPacks().stream()
                    .filter(p -> p.getWeight() == slotCapacity)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            product.getName() + " has no " + slotCapacity + "g pack"));
            items.add(new OrderItem(slot, product.getId(), product.getName(), pack.getWeight(), pack.getPrice()));
        }
        return items;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
