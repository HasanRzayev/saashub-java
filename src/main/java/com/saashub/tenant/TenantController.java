package com.saashub.tenant;

import com.saashub.common.dto.ApiResponse;
import com.saashub.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Tenant organization metadata and subscription endpoints")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/current")
    @Operation(summary = "Get current authenticated tenant organization info")
    public ResponseEntity<ApiResponse<Tenant>> getCurrentTenant() {
        String currentTenantSlug = TenantContext.getCurrentTenant();
        Tenant tenant = tenantService.getTenantBySlug(currentTenantSlug);
        return ResponseEntity.ok(ApiResponse.ok(tenant));
    }

    @PatchMapping("/subscription")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Update subscription plan (Tenant Owner only)")
    public ResponseEntity<ApiResponse<Tenant>> updatePlan(@RequestParam SubscriptionPlan plan) {
        String currentTenantSlug = TenantContext.getCurrentTenant();
        Tenant updated = tenantService.updateSubscriptionPlan(currentTenantSlug, plan);
        return ResponseEntity.ok(ApiResponse.ok("Subscription updated", updated));
    }
}
