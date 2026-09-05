package com.saashub.auth;

import com.saashub.auth.dto.AuthResponse;
import com.saashub.auth.dto.LoginRequest;
import com.saashub.auth.dto.TenantRegistrationRequest;
import com.saashub.auth.security.JwtTokenProvider;
import com.saashub.common.exception.BusinessException;
import com.saashub.common.exception.ResourceNotFoundException;
import com.saashub.tenant.SubscriptionPlan;
import com.saashub.tenant.Tenant;
import com.saashub.tenant.TenantRepository;
import com.saashub.user.Role;
import com.saashub.user.User;
import com.saashub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse registerTenant(TenantRegistrationRequest request) {
        String slug = request.getTenantSlug().toLowerCase().trim();

        if (tenantRepository.existsBySlug(slug)) {
            throw new BusinessException("Tenant slug already exists: " + slug, HttpStatus.CONFLICT);
        }

        Tenant tenant = Tenant.builder()
                .slug(slug)
                .name(request.getOrganizationName().trim())
                .plan(request.getPlan() != null ? request.getPlan() : SubscriptionPlan.STARTER)
                .active(true)
                .build();

        tenantRepository.save(tenant);

        User owner = User.builder()
                .email(request.getOwnerEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getOwnerFullName().trim())
                .role(Role.OWNER)
                .enabled(true)
                .build();
        owner.setTenantId(slug);

        User savedOwner = userRepository.save(owner);

        String accessToken = tokenProvider.generateAccessToken(savedOwner.getId(), savedOwner.getEmail(), slug, savedOwner.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(savedOwner.getId(), slug);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedOwner.getId())
                .email(savedOwner.getEmail())
                .fullName(savedOwner.getFullName())
                .tenantSlug(slug)
                .role(savedOwner.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String slug = request.getTenantSlug().toLowerCase().trim();
        String email = request.getEmail().toLowerCase().trim();

        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + slug));

        if (!tenant.isActive()) {
            throw new BusinessException("Tenant subscription is inactive or suspended", HttpStatus.FORBIDDEN);
        }

        User user = userRepository.findByTenantIdAndEmail(slug, email)
                .orElseThrow(() -> new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), slug, user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), slug);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .tenantSlug(slug)
                .role(user.getRole())
                .build();
    }
}
