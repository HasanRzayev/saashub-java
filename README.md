# SaaSHub - Multi-Tenant Business Management Platform

SaaSHub is an enterprise-grade multi-tenant software-as-a-service (SaaS) backend platform designed with Java 17 and Spring Boot 3. The architecture enforces strict tenant data boundary isolation, tenant context propagation via `ThreadLocal`, cryptographically signed JWT tenant claims, granular Role-Based Access Control (RBAC), and distributed Redis caching.

---

## Architecture and Tenant Isolation Strategy

SaaSHub utilizes a Shared Database, Discriminator Column (`tenant_id`) pattern with automated runtime tenant filtering to prevent Insecure Direct Object Reference (IDOR) vulnerabilities:

```text
                     Request: Authorization: Bearer JWT (contains tenantId claim)
                                            |
                                            v
                              +---------------------------+
                              |   JwtAuthenticationFilter |
                              +-------------+-------------+
                                            |
                                            v
                              +---------------------------+
                              | TenantContext.setTenant() | (ThreadLocal)
                              +-------------+-------------+
                                            |
                 +--------------------------+--------------------------+
                 v                                                     v
      +---------------------+                               +---------------------+
      |  Organization Alpha |                               |  Organization Beta  |
      |  (tenant_id = 100)  |                               |  (tenant_id = 200)  |
      +----------+----------+                               +----------+----------+
                 |                                                     |
                 v                                                     v
      SELECT * FROM orders                                  SELECT * FROM orders
      WHERE tenant_id = '100'                               WHERE tenant_id = '200'
```

---

## Technical Highlights

### 1. Robust Multi-Tenant Boundary Enforcement
- Tenant identification is cryptographically encapsulated within the JWT access token.
- `JwtAuthenticationFilter` resolves and attaches the active tenant context to `TenantContext` (`ThreadLocal`), ensuring safe concurrent request processing.
- Repository layer interfaces enforce tenant boundaries (e.g., `findByIdAndTenantId(id, tenantId)`), guaranteeing that users from Organization A cannot view or alter records from Organization B.

### 2. BaseTenantEntity Mapping
- Domain models (`User`, `Customer`, `Product`, `Order`, `OrderItem`) inherit from `BaseTenantEntity`:
  ```java
  @MappedSuperclass
  public abstract class BaseTenantEntity {
      @Column(name = "tenant_id", nullable = false, updatable = false)
      private String tenantId;
  }
  ```
- `@PrePersist` hooks automatically assign the active tenant identifier from `TenantContext`.

### 3. Role-Based Access Control (RBAC)
- `OWNER`: Full administrative authority over tenant subscriptions, configurations, and users.
- `ADMIN`: User management, customer relations, product catalog, and order execution.
- `MANAGER`: Product management, customer relations, and order operations.
- `EMPLOYEE`: Sales order processing and customer querying.

---

## Technology Stack

- Language: Java 17
- Framework: Spring Boot 3.3.3
- Security: Spring Security 6, JWT (jjwt 0.12.6), BCrypt
- Persistence: Spring Data JPA, Hibernate, PostgreSQL 16
- Caching: Redis 7, Spring Cache Abstraction
- Documentation: SpringDoc OpenAPI 3 (Swagger UI)
- Containerization: Docker, Docker Compose
- Testing: JUnit 5, Mockito, Spring Security Test
- CI/CD: GitHub Actions

---

## Project Structure

```text
saashub/
├── src/
│   ├── main/
│   │   ├── java/com/saashub/
│   │   │   ├── SaashubApplication.java
│   │   │   ├── auth/             # Multi-tenant registration & JWT authentication
│   │   │   ├── common/           # Unified API responses & global error handler
│   │   │   ├── config/           # Security, Redis, OpenAPI configs
│   │   │   ├── customer/         # Tenant-isolated CRM & customer management
│   │   │   ├── multitenancy/     # TenantContext, BaseTenantEntity, Filters
│   │   │   ├── order/            # Sales orders & line items
│   │   │   ├── product/          # Product catalog & stock inventory
│   │   │   ├── tenant/           # Organization metadata & subscription plans
│   │   │   └── user/             # Multi-tenant user repository & roles
│   │   └── resources/
│   │       └── application.yml
│   └── test/                     # Multi-tenant boundary & security test suite
├── .github/workflows/ci.yml
├── docker-compose.yml
├── Dockerfile
├── render.yaml
└── pom.xml
```

---

## Local Deployment & Execution

### Run with Docker Compose
```bash
docker compose up --build
```

Access points:
- Application API: `http://localhost:8082`
- Swagger UI Documentation: `http://localhost:8082/swagger-ui.html`
- PostgreSQL: `localhost:5434`
- Redis: `localhost:6381`

---

## REST API Endpoints

### Onboarding & Authentication
- `POST /api/auth/register-tenant` - Onboard a new tenant organization and primary owner account.
- `POST /api/auth/login` - Authenticate tenant user against specified tenant slug.

### Tenant Management
- `GET /api/tenants/current` - Retrieve current organization details and subscription level.
- `PATCH /api/tenants/subscription` - Update organization tier (Owner role required).

### Customers & CRM
- `POST /api/customers` - Create customer record for current tenant.
- `GET /api/customers/{id}` - Retrieve customer record (Tenant isolated).
- `GET /api/customers` - Paginated customer list.

### Product Catalog
- `POST /api/products` - Create product in organization catalog.
- `GET /api/products/{id}` - Retrieve product details.
- `GET /api/products` - Paginated active products.

### Orders
- `POST /api/orders` - Create sales order with automatic stock reservation.
- `GET /api/orders/{id}` - Retrieve order details.
- `GET /api/orders` - Paginated organization order history.

---

## Testing

Run the automated test suite:
```bash
mvn clean test
```
Tests verify:
- Cross-tenant data isolation and protection against unauthorized cross-tenant data access.
- Tenant onboarding and authentication token lifecycle.
- Sales order creation and stock decrement logic.
