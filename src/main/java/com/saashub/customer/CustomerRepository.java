package com.saashub.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdAndTenantId(Long id, String tenantId);
    Page<Customer> findAllByTenantId(String tenantId, Pageable pageable);
    boolean existsByTenantIdAndEmail(String tenantId, String email);
}
