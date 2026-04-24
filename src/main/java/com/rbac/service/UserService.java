package com.rbac.service;

import com.rbac.dto.AssignmentResponse;
import com.rbac.dto.UserCreateRequest;
import com.rbac.dto.UserResponse;
import com.rbac.entity.Role;
import com.rbac.entity.User;
import com.rbac.entity.UserRole;
import com.rbac.exception.RbacExceptions;
import com.rbac.repository.RoleRepo;
import com.rbac.repository.UserRepo;
import com.rbac.repository.UserRoleRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepository;
    private final RoleRepo roleRepository;
    private final UserRoleRepo userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreateRequest request) {

        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    throw new RbacExceptions.DuplicateResourceException(
                            "User already exists: " + request.getUsername());
                });
        User savedUser = userRepository.save(User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build());
        log.info("Created user '{}'", savedUser.getUsername());
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .enabled(savedUser.isEnabled())
                .build();

    }

    public AssignmentResponse assignRoleToUser(Long userId, Long roleId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RbacExceptions.ResourceNotFoundException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RbacExceptions.ResourceNotFoundException("Role not found: " + roleId));
        if (userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new RbacExceptions.DuplicateResourceException(
                    "Role '" + role.getName() + "' is already assigned to user '" + user.getUsername() + "'");
        }
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();
        userRoleRepository.save(userRole);
        log.info("Assigned role '{}' to user '{}'", role.getName(), user.getUsername());
        return AssignmentResponse.builder()
                .message("Role assigned successfully")
                .data("User: " + user.getUsername() + " → Role: " + role.getName())
                .build();

    }

}
