package com.affordablegifts.backend.catalog.packaging;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackagingRepository extends JpaRepository<Packaging, String> {

    List<Packaging> findByActiveTrueOrderByDisplayOrderAscNameAsc();

    List<Packaging> findAllByOrderByDisplayOrderAscNameAsc();
}
