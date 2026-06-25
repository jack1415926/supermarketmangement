package com.supermarket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "ok",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
