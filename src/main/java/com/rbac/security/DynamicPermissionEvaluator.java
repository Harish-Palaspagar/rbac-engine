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

        if (!(permission instanceof String requiredPermission)) return false;
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) return false;
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userDetails.getUserId());
        List<String> grantedPermissions = rolePermissionRepository.findPermissionNamesByRoleIds(roleIds);
        return grantedPermissions.contains(requiredPermission);

    }

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {

        return hasPermission(authentication, targetType, permission);

    }

}
