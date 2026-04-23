package com.rbac.controller;

import com.rbac.dto.AssignmentResponse;
import com.rbac.dto.RoleRequest;
import com.rbac.dto.RoleResponse;
import com.rbac.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'MANAGE_ROLES')")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {

        log.debug("POST /roles → name={}", request.getName());
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission(null, 'ASSIGN_PERMISSIONS')")
    public ResponseEntity<AssignmentResponse> assignPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        log.debug("POST /roles/{}/permissions/{}", roleId, permissionId);
        return ResponseEntity.ok(roleService.assignPermissionToRole(roleId, permissionId));

    }


    @GetMapping
    @PreAuthorize("hasPermission(null, 'MANAGE_ROLES')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {

        return ResponseEntity.ok(roleService.getAllRoles());

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'MANAGE_ROLES')")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {

        return ResponseEntity.ok(roleService.getRoleById(id));

    }

}

