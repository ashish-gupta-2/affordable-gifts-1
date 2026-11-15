package com.affordablegifts.backend.catalog.product;

import com.affordablegifts.backend.catalog.product.ProductDtos.AdminProductResponse;
import com.affordablegifts.backend.catalog.product.ProductDtos.CreateProductRequest;
import com.affordablegifts.backend.catalog.product.ProductDtos.PackRequest;
import com.affordablegifts.backend.catalog.product.ProductDtos.ProductResponse;
import com.affordablegifts.backend.catalog.product.ProductDtos.UpdateProductRequest;
import com.affordablegifts.backend.common.ConflictException;
import com.affordablegifts.backend.common.NotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listActive() {
        return repository.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminProductResponse> listAll() {
        return repository.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .map(AdminProductResponse::from).toList();
    }

    @Transactional
    public AdminProductResponse create(CreateProductRequest request) {
        if (repository.existsById(request.id())) {
            throw new ConflictException("Product '" + request.id() + "' already exists");
        }
        Product product = new Product(request.id(), request.name(), request.category(),
                request.emoji(), orZero(request.displayOrder()), toPacks(request.packs()));
        return AdminProductResponse.from(repository.save(product));
    }

    @Transactional
    public AdminProductResponse update(String id, UpdateProductRequest request) {
        Product product = find(id);
        product.update(request.name(), request.category(), request.emoji(),
                orZero(request.displayOrder()), request.active(), toPacks(request.packs()));
        return AdminProductResponse.from(product);
    }

    /** Soft delete: past orders will still reference the product. */
    @Transactional
    public void deactivate(String id) {
        find(id).deactivate();
    }

    private Product find(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product '" + id + "' not found"));
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static List<PackPrice> toPacks(List<PackRequest> requests) {
        Set<Integer> seen = new HashSet<>();
        for (PackRequest request : requests) {
            if (!seen.add(request.weight())) {
                throw new IllegalArgumentException("Duplicate pack size " + request.weight() + "g");
            }
        }
        return requests.stream().map(r -> new PackPrice(r.weight(), r.price())).toList();
    }
}
