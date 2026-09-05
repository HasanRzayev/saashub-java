package com.saashub.auth.dto;

import com.saashub.tenant.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationRequest {

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "Tenant slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Tenant slug must contain only lowercase letters, numbers, and hyphens")
    private String tenantSlug;

    @NotBlank(message = "Owner full name is required")
    private String ownerFullName;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Invalid email format")
    private String ownerEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    private SubscriptionPlan plan;
}
