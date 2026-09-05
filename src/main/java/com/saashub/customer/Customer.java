package com.saashub.customer;

import com.saashub.multitenancy.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_cust_tenant_email", columnList = "tenant_id, email", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String company;
}
