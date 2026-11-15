package com.affordablegifts.backend.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {

    Optional<CustomerOrder> findByRequestId(String requestId);

    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    List<CustomerOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query(value = "select nextval('order_number_seq')", nativeQuery = true)
    long nextOrderNumber();
}
