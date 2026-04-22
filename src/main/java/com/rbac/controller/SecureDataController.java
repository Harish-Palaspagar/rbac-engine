package com.rbac.controller;

import com.rbac.dto.SecureDataResponse;
import com.rbac.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SecureDataController {

    @GetMapping("/secure-data")
    @PreAuthorize("hasPermission(null, 'ACCESS_SECURE_DATA')")
    public ResponseEntity<SecureDataResponse> getSecureData(
            @AuthenticationPrincipal CustomUserDetails principal) {

        log.debug("Secure data accessed by: {}", principal.getUsername());
        SecureDataResponse response = SecureDataResponse.builder()
                .message("Access granted to secure resource")
                .accessedBy(principal.getUsername())
                .timestamp(System.currentTimeMillis())
                .note("This endpoint is guarded by the ACCESS_SECURE_DATA permission, " +
                        "evaluated dynamically from the database at runtime.")
                .build();
        return ResponseEntity.ok(response);

    }

}