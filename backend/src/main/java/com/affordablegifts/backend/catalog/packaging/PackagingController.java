package com.affordablegifts.backend.catalog.packaging;

import com.affordablegifts.backend.catalog.packaging.PackagingDtos.PackagingResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packaging")
public class PackagingController {

    private final PackagingService service;

    public PackagingController(PackagingService service) {
        this.service = service;
    }

    @GetMapping
    public List<PackagingResponse> list() {
        return service.listActive();
    }
}
