package com.rbac.security;

import com.rbac.entity.User;
import com.rbac.repository.RolePermissionRepo;
import com.rbac.repository.UserRoleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicPermissionEvaluatorTest {

    @Mock
    private UserRoleRepo userRoleRepository;

    @Mock
    private RolePermissionRepo rolePermissionRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DynamicPermissionEvaluator evaluator;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).username("admin").password("pass").enabled(true).build();
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void hasPermission_Granted() {

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findRoleIdsByUserId(1L)).thenReturn(List.of(1L));
        when(rolePermissionRepository.findPermissionNamesByRoleIds(List.of(1L)))
                .thenReturn(List.of("ACCESS_SECURE_DATA"));
        boolean result = evaluator.hasPermission(authentication, null, "ACCESS_SECURE_DATA");
        assertTrue(result);

    }

    @Test
    void hasPermission_Denied_MissingPermission() {

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findRoleIdsByUserId(1L)).thenReturn(List.of(1L));
        when(rolePermissionRepository.findPermissionNamesByRoleIds(List.of(1L)))
                .thenReturn(List.of("OTHER_PERMISSION"));
        boolean result = evaluator.hasPermission(authentication, null, "ACCESS_SECURE_DATA");
        assertFalse(result);

    }

    @Test
    void hasPermission_Denied_NotCustomUserDetails() {

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        boolean result = evaluator.hasPermission(authentication, null, "ACCESS_SECURE_DATA");
        assertFalse(result);

    }

    @Test
    void hasPermission_Denied_PermissionNotString() {

        boolean result = evaluator.hasPermission(authentication, null, 999);
        assertFalse(result);

    }

    @Test
    void hasPermission_WithTargetId_DelegatesToMainMethod() {

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRoleRepository.findRoleIdsByUserId(1L)).thenReturn(List.of(1L));
        when(rolePermissionRepository.findPermissionNamesByRoleIds(List.of(1L)))
                .thenReturn(List.of("ACCESS_SECURE_DATA"));
        boolean result = evaluator
                .hasPermission(authentication, 1L, "SomeType", "ACCESS_SECURE_DATA");
        assertTrue(result);

    }

}
