package com.rbac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/healthz")
    public ResponseEntity<Map<String, String>> healthz() {

        return ResponseEntity.ok(Map.of(
                "release", "healthz-v2",
                "status", "ok",
                "service", "dynamic-rbac"
        ));

    }

}
