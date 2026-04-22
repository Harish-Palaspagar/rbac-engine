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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepo roleRepository;
    private final PermissionRepo permissionRepository;
    private final RolePermissionRepo rolePermissionRepository;

    @Transactional
    public RoleResponse createRole(RoleRequest request) {

        String name = request.getName().toUpperCase().trim();
        if (roleRepository.existsByName(name)) {
            throw new RbacExceptions.DuplicateResourceException("Role already exists: " + name);
        }
        Role role = Role.builder().name(name).build();
        Role saved = roleRepository.save(role);
        log.info("Created role: id={} name={}", saved.getId(), saved.getName());
        return toDto(saved);

    }

    @Transactional
    public AssignmentResponse assignPermissionToRole(Long roleId, Long permissionId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RbacExceptions.ResourceNotFoundException("Role not found: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RbacExceptions.ResourceNotFoundException("Permission not found: " + permissionId));
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new RbacExceptions.DuplicateResourceException(
                    "Permission '" + permission.getName() + "' is already assigned to role '" + role.getName() + "'");
        }
        RolePermission rp = RolePermission.builder()
                .role(role)
                .permission(permission)
                .build();
        rolePermissionRepository.save(rp);
        log.info("Assigned permission '{}' to role '{}'", permission.getName(), role.getName());
        return AssignmentResponse.builder()
                .message("Permission assigned successfully")
                .data("Role: " + role.getName() + " → Permission: " + permission.getName())
                .build();

    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {

        return roleRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RbacExceptions.ResourceNotFoundException("Role not found: " + id));

    }

    private RoleResponse toDto(Role role) {

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();

    }

}
