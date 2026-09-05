package com.saashub.product;

import com.saashub.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Tenant-isolated product catalog and inventory endpoints")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create product for current tenant organization")
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productService.createProduct(request);
        return new ResponseEntity<>(ApiResponse.ok("Product created", product), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID (Strict tenant isolation)")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.ok(product));
    }

    @GetMapping
    @Operation(summary = "Get paginated products for current tenant")
    public ResponseEntity<ApiResponse<Page<Product>>> getProducts(@PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.getProducts(pageable);
        return ResponseEntity.ok(ApiResponse.ok(products));
    }
}
