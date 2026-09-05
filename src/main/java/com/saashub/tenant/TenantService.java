package com.saashub.tenant;

import com.saashub.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "tenants", key = "#slug")
    public Tenant getTenantBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + slug));
    }

    @Transactional
    public Tenant updateSubscriptionPlan(String slug, SubscriptionPlan newPlan) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + slug));
        tenant.setPlan(newPlan);
        return tenantRepository.save(tenant);
    }
}
