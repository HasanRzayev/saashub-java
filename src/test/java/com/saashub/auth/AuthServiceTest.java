package com.saashub.auth;

import com.saashub.auth.dto.AuthResponse;
import com.saashub.auth.dto.TenantRegistrationRequest;
import com.saashub.auth.security.JwtTokenProvider;
import com.saashub.tenant.SubscriptionPlan;
import com.saashub.tenant.Tenant;
import com.saashub.tenant.TenantRepository;
import com.saashub.user.Role;
import com.saashub.user.User;
import com.saashub.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerTenant_Success() {
        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .organizationName("Acme Corp")
                .tenantSlug("acme-corp")
                .ownerEmail("owner@acme.internal")
                .ownerFullName("Alice Owner")
                .password("StrongPass123")
                .plan(SubscriptionPlan.ENTERPRISE)
                .build();

        when(tenantRepository.existsBySlug("acme-corp")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(100L);
            return u;
        });
        when(tokenProvider.generateAccessToken(eq(100L), eq("owner@acme.internal"), eq("acme-corp"), eq("OWNER")))
                .thenReturn("access-token-xyz");
        when(tokenProvider.generateRefreshToken(eq(100L), eq("acme-corp")))
                .thenReturn("refresh-token-xyz");

        AuthResponse response = authService.registerTenant(request);

        assertNotNull(response);
        assertEquals("acme-corp", response.getTenantSlug());
        assertEquals("owner@acme.internal", response.getEmail());
        assertEquals(Role.OWNER, response.getRole());
        assertEquals("access-token-xyz", response.getAccessToken());

        verify(tenantRepository, times(1)).save(any(Tenant.class));
        verify(userRepository, times(1)).save(any(User.class));
    }
}
