package gdg.travodobackend.healthCheck.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 작동하는지 확인합니다. (배포 환경용)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Travodo Backend Server is running!");
        return ResponseEntity.ok(response);
    }
}

