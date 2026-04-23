package com.rbac.service;

import com.rbac.dto.AssignmentResponse;
import com.rbac.dto.RoleRequest;
import com.rbac.dto.RoleResponse;
import com.rbac.entity.Permission;
import com.rbac.entity.Role;
import com.rbac.entity.RolePermission;
import com.rbac.exception.RbacExceptions;
import com.rbac.repository.PermissionRepo;
import com.rbac.repository.RolePermissionRepo;
import com.rbac.repository.RoleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepo roleRepository;

    @Mock
    private PermissionRepo permissionRepository;

    @Mock
    private RolePermissionRepo rolePermissionRepository;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {

        testRole = Role.builder().id(1L).name("ADMIN").build();
        testPermission = Permission.builder().id(1L).name("MANAGE_USERS").build();

    }

    @Test
    void createRole_Success() {

        RoleRequest request = new RoleRequest();
        request.setName("admin");
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        RoleResponse response = roleService.createRole(request);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ADMIN", response.getName());
        verify(roleRepository, times(1)).save(any(Role.class));

    }

    @Test
    void createRole_DuplicateResourceException() {

        RoleRequest request = new RoleRequest();
        request.setName("admin");
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);
        assertThrows(RbacExceptions.DuplicateResourceException.class,
                () -> roleService.createRole(request));
        verify(roleRepository, never()).save(any(Role.class));

    }

    @Test
    void assignPermissionToRole_Success() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(1L, 1L)).thenReturn(false);
        when(rolePermissionRepository.save(any(RolePermission.class))).thenReturn(new RolePermission());
        AssignmentResponse response = roleService.assignPermissionToRole(1L, 1L);
        assertNotNull(response);
        assertEquals("Permission assigned successfully", response.getMessage());
        verify(rolePermissionRepository, times(1)).save(any(RolePermission.class));

    }

    @Test
    void assignPermissionToRole_RoleNotFound() {

        when(roleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RbacExceptions.ResourceNotFoundException.class, () -> roleService.assignPermissionToRole(1L, 1L));
        verify(permissionRepository, never()).findById(anyLong());

    }

    @Test
    void assignPermissionToRole_PermissionNotFound() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RbacExceptions.ResourceNotFoundException.class,
                () -> roleService.assignPermissionToRole(1L, 1L));

    }

    @Test
    void assignPermissionToRole_AlreadyAssigned() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(1L, 1L)).thenReturn(true);
        assertThrows(RbacExceptions.DuplicateResourceException.class,
                () -> roleService.assignPermissionToRole(1L, 1L));

    }

    @Test
    void getAllRoles_Success() {

        when(roleRepository.findAll()).thenReturn(List.of(testRole));
        List<RoleResponse> responses = roleService.getAllRoles();
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("ADMIN", responses.getFirst().getName());

    }

    @Test
    void getRoleById_Success() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        RoleResponse response = roleService.getRoleById(1L);
        assertNotNull(response);
        assertEquals("ADMIN", response.getName());

    }

    @Test
    void getRoleById_NotFound() {

        when(roleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RbacExceptions.ResourceNotFoundException.class, () -> roleService.getRoleById(1L));

    }
}
