package com.saashub.auth;

import com.saashub.auth.dto.AuthResponse;
import com.saashub.auth.dto.LoginRequest;
import com.saashub.auth.dto.TenantRegistrationRequest;
import com.saashub.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for tenant onboarding and multi-tenant user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-tenant")
    @Operation(summary = "Register organization tenant and owner account")
    public ResponseEntity<ApiResponse<AuthResponse>> registerTenant(@Valid @RequestBody TenantRegistrationRequest request) {
        AuthResponse response = authService.registerTenant(request);
        return new ResponseEntity<>(ApiResponse.ok("Organization registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user within a specific tenant organization")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}
