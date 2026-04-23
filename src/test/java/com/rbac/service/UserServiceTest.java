package com.rbac.service;

import com.rbac.dto.AssignmentResponse;
import com.rbac.entity.Role;
import com.rbac.entity.User;
import com.rbac.entity.UserRole;
import com.rbac.exception.RbacExceptions;
import com.rbac.repository.RoleRepo;
import com.rbac.repository.UserRepo;
import com.rbac.repository.UserRoleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepository;

    @Mock
    private RoleRepo roleRepository;

    @Mock
    private UserRoleRepo userRoleRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    private Role testRole;

    @BeforeEach
    void setUp() {

        testUser = User.builder().id(1L).username("testuser").build();
        testRole = Role.builder().id(1L).name("ADMIN").build();

    }

    @Test
    void assignRoleToUser_Success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRoleRepository.existsByUserIdAndRoleId(1L, 1L)).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());
        AssignmentResponse response = userService.assignRoleToUser(1L, 1L);
        assertNotNull(response);
        assertEquals("Role assigned successfully", response.getMessage());
        verify(userRoleRepository, times(1)).save(any(UserRole.class));

    }

    @Test
    void assignRoleToUser_UserNotFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RbacExceptions.ResourceNotFoundException.class, () -> userService.assignRoleToUser(1L, 1L));
        verify(roleRepository, never()).findById(anyLong());

    }

    @Test
    void assignRoleToUser_RoleNotFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RbacExceptions.ResourceNotFoundException.class,
                () -> userService.assignRoleToUser(1L, 1L));

    }

    @Test
    void assignRoleToUser_AlreadyAssigned() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRoleRepository.existsByUserIdAndRoleId(1L, 1L)).thenReturn(true);
        assertThrows(RbacExceptions.DuplicateResourceException.class,
                () -> userService.assignRoleToUser(1L, 1L));
        verify(userRoleRepository, never()).save(any(UserRole.class));

    }

}
