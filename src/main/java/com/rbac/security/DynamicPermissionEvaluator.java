package com.rbac.security;

import com.rbac.repository.RolePermissionRepo;
import com.rbac.repository.UserRoleRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicPermissionEvaluator implements PermissionEvaluator {

    private final UserRoleRepo userRoleRepository;
    private final RolePermissionRepo rolePermissionRepository;

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {

        if (!authentication.isAuthenticated()) {
            log.debug("Permission check failed: unauthenticated request");
            return false;
        }
        if (!(permission instanceof String requiredPermission)) {
            log.warn("Permission must be a String, got: {}", permission);
            return false;
        }
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            log.warn("Principal is not CustomUserDetails: {}", authentication.getPrincipal());
            return false;
        }
        Long userId = userDetails.getUserId();
        log.debug("Evaluating permission '{}' for userId={}", requiredPermission, userId);
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);

        if (roleIds.isEmpty()) {
            log.debug("No roles found for userId={} — denying '{}'", userId, requiredPermission);
            return false;
        }
        List<String> grantedPermissions = rolePermissionRepository
                .findPermissionNamesByRoleIds(roleIds);
        boolean granted = grantedPermissions.contains(requiredPermission);
        log.debug("userId={} roleIds={} grantedPermissions={} required='{}' → {}",
                userId, roleIds, grantedPermissions, requiredPermission,
                granted ? "GRANTED" : "DENIED");
        return granted;

    }

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {

        return hasPermission(authentication, targetType, permission);

    }

}
