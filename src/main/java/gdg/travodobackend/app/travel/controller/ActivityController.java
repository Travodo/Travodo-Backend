package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    // 생성
    @PostMapping
    public ResponseEntity<ActivityResponse> create(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody ActivityCreateRequest request
    ) {
        return ResponseEntity.ok(
                activityService.create(userId, tripId, request)
        );
    }

    // 수정
    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityUpdateRequest request
    ) {
        return ResponseEntity.ok(
                activityService.update(userId, tripId, activityId, request)
        );
    }

    // 삭제
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long activityId
    ) {
        activityService.delete(userId, tripId, activityId);
        return ResponseEntity.noContent().build();
    }

    // 상태 변경
    @PatchMapping("/{activityId}/status")
    public ResponseEntity<ActivityResponse> updateStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                activityService.updateStatus(userId, tripId, activityId, request)
        );
    }
}
