package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(
        name = "여행",
        description = "여행 생성, 참여, 조회 및 상태 관리 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    @Operation(
            summary = "여행 생성",
            description = "새로운 여행을 생성하고 생성자를 여행 방장으로 등록합니다."
    )
    @ApiResponse(responseCode = "200", description = "여행 생성 성공")
    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(
            @AuthenticationPrincipal Long userId,
            @RequestBody TripCreateRequest request
    ) {
        return ResponseEntity.ok(tripService.createTrip(userId, request));
    }

    @Operation(
            summary = "초대 코드 재발급",
            description = "여행 방장이 기존 초대 코드를 재발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "403", description = "여행 방장이 아님")
    })
    @PostMapping("/{tripId}/invite-code")
    public ResponseEntity<Map<String, String>> regenerateInviteCode(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId
    ) {
        String code = tripService.regenerateInviteCode(userId, tripId);
        return ResponseEntity.ok(Map.of("inviteCode", code));
    }

    @Operation(
            summary = "초대 코드로 여행 참가",
            description = "초대 코드를 이용해 여행에 참가합니다."
    )
    @PostMapping("/join")
    public ResponseEntity<TripResponse> joinTrip(
            @AuthenticationPrincipal Long userId,
            @RequestBody TripJoinRequest request
    ) {
        return ResponseEntity.ok(tripService.joinTrip(userId, request));
    }

    @Operation(
            summary = "여행 상태 변경",
            description = "여행 상태를 UPCOMING / ONGOING / FINISHED 중 하나로 변경합니다."
    )
    @PatchMapping("/{tripId}/status")
    public ResponseEntity<TripResponse> updateTripStatus(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,
            @RequestBody TripStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(tripService.updateTripStatus(userId, tripId, request));
    }

    @Operation(
            summary = "월별 여행 조회",
            description = "특정 연도와 월에 해당하는 여행 목록을 조회합니다. (달력용)"
    )
    @GetMapping("/calendar")
    public ResponseEntity<TripCalendarResponse> getTripsByMonth(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "연도", example = "2025")
            @RequestParam int year,
            @Parameter(description = "월 (1~12)", example = "9")
            @RequestParam int month
    ) {
        return ResponseEntity.ok(tripService.getTripsByMonth(userId, year, month));
    }

    @Operation(
            summary = "다가오는 여행 목록 조회",
            description = "현재 로그인한 사용자의 다가오는 여행(UPCOMING)을 조회합니다."
    )
    @GetMapping("/upcoming")
    public ResponseEntity<List<TripResponse>> getUpcomingTrips(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(tripService.getUpcomingTrips(userId));
    }

    @Operation(
            summary = "여행 멤버 목록 조회",
            description = "특정 여행에 참여한 멤버 목록을 조회합니다."
    )
    @GetMapping("/{tripId}/members")
    public ResponseEntity<List<TripMemberResponse>> getTripMembers(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(tripService.getTripMembers(userId, tripId));
    }

    @Operation(
            summary = "지난 여행 목록 조회",
            description = "완료된 여행(FINISHED) 목록을 조회합니다."
    )
    @GetMapping("/me/trips")
    public ResponseEntity<List<PastTripResponse>> getMyTrips(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회 타입 (PAST만 지원)", example = "PAST")
            @RequestParam String status
    ) {
        if ("PAST".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(tripService.getPastTrips(userId));
        }
        throw new IllegalArgumentException("지원하지 않는 status 값입니다");
    }

    @Operation(
            summary = "현재 진행중인 여행 조회",
            description = "로그인한 사용자의 현재 진행중인 여행을 조회합니다."
    )
    @GetMapping("/current")
    public Map<String, CurrentTripResponse> getCurrentTrip(
            @AuthenticationPrincipal Long userId
    ) {
        return Map.of(
                "trip",
                tripService.getCurrentTrip(userId)
        );
    }
}
