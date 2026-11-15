package com.affordablegifts.backend.catalog.packaging;

import com.affordablegifts.backend.catalog.packaging.PackagingDtos.AdminPackagingResponse;
import com.affordablegifts.backend.catalog.packaging.PackagingDtos.CreatePackagingRequest;
import com.affordablegifts.backend.catalog.packaging.PackagingDtos.PackagingResponse;
import com.affordablegifts.backend.catalog.packaging.PackagingDtos.UpdatePackagingRequest;
import com.affordablegifts.backend.common.ConflictException;
import com.affordablegifts.backend.common.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PackagingService {

    private final PackagingRepository repository;

    public PackagingService(PackagingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PackagingResponse> listActive() {
        return repository.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(PackagingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminPackagingResponse> listAll() {
        return repository.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .map(AdminPackagingResponse::from).toList();
    }

    @Transactional
    public AdminPackagingResponse create(CreatePackagingRequest r) {
        if (repository.existsById(r.id())) {
            throw new ConflictException("Packaging '" + r.id() + "' already exists");
        }
        validateJarCover(r.kind(), r.slotCount(), r.slotCapacity());
        Packaging packaging = new Packaging(r.id(), r.name(), r.kind(), r.description(),
                r.price(), r.slotCount(), r.slotCapacity(), orZero(r.displayOrder()));
        return AdminPackagingResponse.from(repository.save(packaging));
    }

    @Transactional
    public AdminPackagingResponse update(String id, UpdatePackagingRequest r) {
        Packaging packaging = find(id);
        validateJarCover(r.kind(), r.slotCount(), r.slotCapacity());
        packaging.update(r.name(), r.kind(), r.description(), r.price(),
                r.slotCount(), r.slotCapacity(), orZero(r.displayOrder()), r.active());
        return AdminPackagingResponse.from(packaging);
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    /** Soft delete: past orders will still reference the packaging. */
    @Transactional
    public void deactivate(String id) {
        find(id).deactivate();
    }

    private Packaging find(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Packaging '" + id + "' not found"));
    }

    /**
     * Jar sets sit in one outer cover that holds either two 200g jars or
     * three 100g jars; anything else cannot physically fit.
     */
    private static void validateJarCover(PackagingKind kind, int slotCount, int slotCapacity) {
        if (kind != PackagingKind.JAR) {
            return;
        }
        boolean twoByTwoHundred = slotCount == 2 && slotCapacity == 200;
        boolean threeByHundred = slotCount == 3 && slotCapacity == 100;
        if (!twoByTwoHundred && !threeByHundred) {
            throw new IllegalArgumentException(
                    "A jar set must be 2 x 200g jars or 3 x 100g jars");
        }
    }
}
