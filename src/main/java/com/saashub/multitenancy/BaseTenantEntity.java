package com.saashub.multitenancy;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseTenantEntity implements Serializable {

    @Column(name = "tenant_id", nullable = false, length = 64, updatable = false)
    private String tenantId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersistTenant() {
        if (this.tenantId == null) {
            String current = TenantContext.getCurrentTenant();
            if (current != null) {
                this.tenantId = current;
            }
        }
    }
}
