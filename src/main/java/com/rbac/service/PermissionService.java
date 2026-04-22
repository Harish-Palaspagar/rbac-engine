package com.rbac.service;

import com.rbac.dto.PermissionRequest;
import com.rbac.dto.PermissionResponse;
import com.rbac.entity.Permission;
import com.rbac.exception.RbacExceptions;
import com.rbac.repository.PermissionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepo permissionRepository;

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {

        String name = request.getName().toUpperCase().trim();
        if (permissionRepository.existsByName(name)) {
            throw new RbacExceptions.DuplicateResourceException("Permission already exists: " + name);
        }
        Permission permission = Permission.builder().name(name).build();
        Permission saved = permissionRepository.save(permission);
        log.info("Created permission: id={} name={}", saved.getId(), saved.getName());
        return toDto(saved);

    }

    public List<PermissionResponse> getAllPermissions() {

        return permissionRepository.findAll().stream()
                .map(this::toDto)
                .toList();

    }

    public PermissionResponse getPermissionById(Long id) {

        return permissionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RbacExceptions.ResourceNotFoundException("Permission not found: " + id));

    }

    private PermissionResponse toDto(Permission permission) {

        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .build();

    }

}