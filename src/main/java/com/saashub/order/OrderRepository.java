package com.saashub.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndTenantId(Long id, String tenantId);
    Optional<Order> findByTenantIdAndOrderNumber(String tenantId, String orderNumber);
    Page<Order> findAllByTenantId(String tenantId, Pageable pageable);
}
