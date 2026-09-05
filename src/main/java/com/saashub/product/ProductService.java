package com.saashub.product;

import com.saashub.common.exception.BusinessException;
import com.saashub.common.exception.ResourceNotFoundException;
import com.saashub.multitenancy.TenantContext;
import com.saashub.product.dto.CreateProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product createProduct(CreateProductRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        String sku = request.getSku().trim().toUpperCase();

        if (productRepository.existsByTenantIdAndSku(tenantId, sku)) {
            throw new BusinessException("Product with SKU already exists in this organization: " + sku, HttpStatus.CONFLICT);
        }

        Product product = Product.builder()
                .sku(sku)
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .active(true)
                .build();
        product.setTenantId(tenantId);

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        return productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return productRepository.findAllByTenantIdAndActiveTrue(tenantId, pageable);
    }
}
