package com.rbac.service;

import com.rbac.dto.PermissionRequest;
import com.rbac.dto.PermissionResponse;
import com.rbac.entity.Permission;
import com.rbac.exception.RbacExceptions;
import com.rbac.repository.PermissionRepo;
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
class PermissionServiceTest {

    @Mock
    private PermissionRepo permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission testPermission;

    @BeforeEach
    void setUp() {

        testPermission = Permission.builder().id(1L).name("MANAGE_USERS").build();

    }

    @Test
    void createPermission_Success() {

        PermissionRequest request = new PermissionRequest();
        request.setName("manage_users");
        when(permissionRepository.existsByName("MANAGE_USERS")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);
        PermissionResponse response = permissionService.createPermission(request);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("MANAGE_USERS", response.getName());
        verify(permissionRepository, times(1)).save(any(Permission.class));

    }

    @Test
    void createPermission_DuplicateResourceException() {

        PermissionRequest request = new PermissionRequest();
        request.setName("manage_users");
        when(permissionRepository.existsByName("MANAGE_USERS")).thenReturn(true);
        assertThrows(RbacExceptions.DuplicateResourceException.class, () -> permissionService.createPermission(request));
        verify(permissionRepository, never()).save(any(Permission.class));

    }

    @Test
    void getAllPermissions_Success() {

        when(permissionRepository.findAll()).thenReturn(List.of(testPermission));
        List<PermissionResponse> responses = permissionService.getAllPermissions();
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("MANAGE_USERS", responses.getFirst().getName());

    }

    @Test
    void getPermissionById_Success() {

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));
        PermissionResponse response = permissionService.getPermissionById(1L);
        assertNotNull(response);
        assertEquals("MANAGE_USERS", response.getName());

    }

    @Test
    void getPermissionById_NotFound() {

        when(permissionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RbacExceptions.ResourceNotFoundException.class,
                () -> permissionService.getPermissionById(1L));

    }

}
