package com.rbac.controller;

import com.rbac.dto.AssignmentResponse;
import com.rbac.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasPermission(null, 'ASSIGN_ROLES')")
    public ResponseEntity<AssignmentResponse> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        log.debug("POST /users/{}/roles/{}", userId, roleId);
        return ResponseEntity.ok(userService.assignRoleToUser(userId, roleId));

    }

}