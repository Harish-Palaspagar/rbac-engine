package com.rbac.config;

import com.rbac.entity.Permission;
import com.rbac.entity.Role;
import com.rbac.entity.RolePermission;
import com.rbac.entity.User;
import com.rbac.entity.UserRole;
import com.rbac.repository.PermissionRepo;
import com.rbac.repository.RolePermissionRepo;
import com.rbac.repository.RoleRepo;
import com.rbac.repository.UserRepo;
import com.rbac.repository.UserRoleRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DefaultDataSeeder implements ApplicationRunner {

    private static final List<String> ADMIN_PERMISSIONS = List.of(
            "MANAGE_ROLES",
            "MANAGE_PERMISSIONS",
            "ASSIGN_PERMISSIONS",
            "ASSIGN_ROLES"
    );
    private static final String USER_PERMISSION = "ACCESS_SECURE_DATA";

    private final UserRepo userRepository;
    private final RoleRepo roleRepository;
    private final PermissionRepo permissionRepository;
    private final UserRoleRepo userRoleRepository;
    private final RolePermissionRepo rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.username}")
    private String adminUsername;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.user1.username}")
    private String user1Username;

    @Value("${app.seed.user1.password}")
    private String user1Password;

    @Value("${app.seed.user2.username}")
    private String user2Username;

    @Value("${app.seed.user2.password}")
    private String user2Password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role adminRole = ensureRole("ADMIN");
        Role userRole = ensureRole("USER");

        ADMIN_PERMISSIONS.stream()
                .map(this::ensurePermission)
                .forEach(permission -> ensureRolePermission(adminRole, permission));

        Permission userPermission = ensurePermission(USER_PERMISSION);
        ensureRolePermission(userRole, userPermission);

        User admin = ensureUser(adminUsername, adminPassword);
        User user1 = ensureUser(user1Username, user1Password);
        ensureUser(user2Username, user2Password);

        ensureUserRole(admin, adminRole);
        ensureUserRole(user1, userRole);

        log.info("Default RBAC seed data is ready");
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }

    private Permission ensurePermission(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
    }

    private User ensureUser(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(rawPassword))
                        .enabled(true)
                        .build()));
    }

    private void ensureRolePermission(Role role, Permission permission) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            rolePermissionRepository.save(RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .build());
        }
    }

    private void ensureUserRole(User user, Role role) {
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            userRoleRepository.save(UserRole.builder()
                    .user(user)
                    .role(role)
                    .build());
        }
    }
}
