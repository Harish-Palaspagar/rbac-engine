package com.rbac.config;

import com.rbac.entity.Permission;
import com.rbac.entity.Role;
import com.rbac.entity.User;
import com.rbac.repository.PermissionRepo;
import com.rbac.repository.RolePermissionRepo;
import com.rbac.repository.RoleRepo;
import com.rbac.repository.UserRepo;
import com.rbac.repository.UserRoleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultDataSeederTest {

    private final UserRepo userRepository = mock(UserRepo.class);
    private final RoleRepo roleRepository = mock(RoleRepo.class);
    private final PermissionRepo permissionRepository = mock(PermissionRepo.class);
    private final UserRoleRepo userRoleRepository = mock(UserRoleRepo.class);
    private final RolePermissionRepo rolePermissionRepository = mock(RolePermissionRepo.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private DefaultDataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DefaultDataSeeder(
                userRepository,
                roleRepository,
                permissionRepository,
                userRoleRepository,
                rolePermissionRepository,
                passwordEncoder
        );

        ReflectionTestUtils.setField(seeder, "adminUsername", "admin");
        ReflectionTestUtils.setField(seeder, "adminPassword", "admin123");
        ReflectionTestUtils.setField(seeder, "user1Username", "user1");
        ReflectionTestUtils.setField(seeder, "user1Password", "user123");
        ReflectionTestUtils.setField(seeder, "user2Username", "user2");
        ReflectionTestUtils.setField(seeder, "user2Password", "user123");

        when(roleRepository.findByName(any())).thenReturn(Optional.empty());
        when(permissionRepository.findByName(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(anyLong(), anyLong())).thenReturn(false);
        when(userRoleRepository.existsByUserIdAndRoleId(anyLong(), anyLong())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            if (role.getId() == null) {
                role.setId("ADMIN".equals(role.getName()) ? 1L : 2L);
            }
            return role;
        });

        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission permission = invocation.getArgument(0);
            if (permission.getId() == null) {
                permission.setId((long) permission.getName().hashCode());
            }
            return permission;
        });

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId((long) user.getUsername().hashCode());
            }
            return user;
        });
    }

    @Test
    void run_SeedsDefaultRolesPermissionsAndUsers() throws Exception {
        seeder.run(new DefaultApplicationArguments(new String[0]));

        verify(roleRepository, times(2)).save(any(Role.class));
        verify(permissionRepository, times(5)).save(any(Permission.class));
        verify(userRepository, times(3)).save(any(User.class));
        verify(rolePermissionRepository, times(5)).save(any());
        verify(userRoleRepository, times(2)).save(any());
        verify(passwordEncoder).encode("admin123");
        verify(passwordEncoder, times(2)).encode("user123");
    }

    @Test
    void run_SkipsExistingSeedData() throws Exception {
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        Role userRole = Role.builder().id(2L).name("USER").build();
        Permission manageRoles = Permission.builder().id(10L).name("MANAGE_ROLES").build();
        Permission managePermissions = Permission.builder().id(11L).name("MANAGE_PERMISSIONS").build();
        Permission assignPermissions = Permission.builder().id(12L).name("ASSIGN_PERMISSIONS").build();
        Permission assignRoles = Permission.builder().id(13L).name("ASSIGN_ROLES").build();
        Permission accessSecureData = Permission.builder().id(14L).name("ACCESS_SECURE_DATA").build();
        User admin = User.builder().id(100L).username("admin").password("encoded").build();
        User user1 = User.builder().id(101L).username("user1").password("encoded").build();
        User user2 = User.builder().id(102L).username("user2").password("encoded").build();

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(permissionRepository.findByName("MANAGE_ROLES")).thenReturn(Optional.of(manageRoles));
        when(permissionRepository.findByName("MANAGE_PERMISSIONS")).thenReturn(Optional.of(managePermissions));
        when(permissionRepository.findByName("ASSIGN_PERMISSIONS")).thenReturn(Optional.of(assignPermissions));
        when(permissionRepository.findByName("ASSIGN_ROLES")).thenReturn(Optional.of(assignRoles));
        when(permissionRepository.findByName("ACCESS_SECURE_DATA")).thenReturn(Optional.of(accessSecureData));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(anyLong(), anyLong())).thenReturn(true);
        when(userRoleRepository.existsByUserIdAndRoleId(anyLong(), anyLong())).thenReturn(true);

        seeder.run(new DefaultApplicationArguments(new String[0]));

        verify(roleRepository, never()).save(any(Role.class));
        verify(permissionRepository, never()).save(any(Permission.class));
        verify(userRepository, never()).save(any(User.class));
        verify(rolePermissionRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
