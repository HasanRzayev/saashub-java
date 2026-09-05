package com.saashub.auth.security;

import com.saashub.multitenancy.TenantContext;
import com.saashub.user.User;
import com.saashub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new UsernameNotFoundException("No tenant context available");
        }
        User user = userRepository.findByTenantIdAndEmail(tenantId, email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email + " for tenant: " + tenantId));
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByIdAndTenant(Long id, String tenantId) {
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id + " for tenant: " + tenantId));
        return UserPrincipal.create(user);
    }
}
