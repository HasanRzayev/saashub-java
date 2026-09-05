package com.saashub.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndTenantId(Long id, String tenantId);
    Optional<Product> findByTenantIdAndSku(String tenantId, String sku);
    Page<Product> findAllByTenantIdAndActiveTrue(String tenantId, Pageable pageable);
    boolean existsByTenantIdAndSku(String tenantId, String sku);
}
