package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.activity.*;
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
import java.util.List;

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
            summary = "여행 활동 목록 조회",
            description = """
            여행의 모든 활동(Activity)을 조회합니다.
            - 여행 상태와 관계없이 조회 가능합니다.
            - 날짜/시간 기준 조회는 사용하지 않습니다.
            """
    )
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getActivities(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                activityService.getActivities(userId, tripId)
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
    public ResponseEntity<Boolean> updateStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
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
