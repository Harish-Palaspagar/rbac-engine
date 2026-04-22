package com.rbac.service;

import com.rbac.dto.AssignmentResponse;
import com.rbac.entity.Role;
import com.rbac.entity.User;
import com.rbac.entity.UserRole;
import com.rbac.exception.RbacExceptions;
import com.rbac.repository.RoleRepo;
import com.rbac.repository.UserRepo;
import com.rbac.repository.UserRoleRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepository;
    private final RoleRepo roleRepository;
    private final UserRoleRepo userRoleRepository;

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
