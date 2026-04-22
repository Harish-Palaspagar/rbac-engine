package com.rbac.controller;

import com.rbac.dto.PermissionRequest;
import com.rbac.dto.PermissionResponse;
import com.rbac.service.PermissionService;
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
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'MANAGE_PERMISSIONS')")
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody PermissionRequest request) {

        log.debug("POST /permissions → name={}", request.getName());
        PermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'MANAGE_PERMISSIONS')")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {

        return ResponseEntity.ok(permissionService.getAllPermissions());

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'MANAGE_PERMISSIONS')")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Long id) {

        return ResponseEntity.ok(permissionService.getPermissionById(id));

    }

}
