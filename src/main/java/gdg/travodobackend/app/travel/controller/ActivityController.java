package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(
        name = "여행 활동",
        description = "여행 중 하루 단위 일정(활동)을 관리하는 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(
            summary = "DAY별 여행 활동 조회",
            description = """
                특정 날짜의 여행 활동 목록을 조회합니다.
                - 진행 중(ONGOING) 상태의 여행에서만 조회 가능합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "400", description = "진행 중인 여행이 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ActivityDayResponse> getActivitiesByDate(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "조회 날짜 (YYYY-MM-DD)", example = "2025-12-20")
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(
                activityService.getActivitiesByDate(userId, tripId, date)
        );
    }

    @Operation(
            summary = "여행 활동 생성",
            description = "여행 일정에 새로운 활동을 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ActivityResponse> create(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Valid @RequestBody ActivityCreateRequest request
    ) {
        return ResponseEntity.ok(
                activityService.create(userId, tripId, request)
        );
    }

    @Operation(
            summary = "여행 활동 수정",
            description = "이미 등록된 여행 활동의 제목과 시간을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
    })
    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> update(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "활동 ID", example = "10")
            @PathVariable Long activityId,

            @Valid @RequestBody ActivityUpdateRequest request
    ) {
        return ResponseEntity.ok(
                activityService.update(userId, tripId, activityId, request)
        );
    }

    @Operation(
            summary = "여행 활동 상태 변경",
            description = "여행 활동의 상태를 변경합니다. (PENDING / DONE)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
    })
    @PatchMapping("/{activityId}/status")
    public ResponseEntity<ActivityResponse> updateStatus(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "활동 ID", example = "10")
            @PathVariable Long activityId,

            @Valid @RequestBody ActivityStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                activityService.updateStatus(userId, tripId, activityId, request)
        );
    }

    @Operation(
            summary = "여행 활동 삭제",
            description = "등록된 여행 활동을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
    })
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "활동 ID", example = "10")
            @PathVariable Long activityId
    ) {
        activityService.delete(userId, tripId, activityId);
        return ResponseEntity.noContent().build();
    }
}
