package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    // 여행 생성 (POST /trips)
    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TripCreateRequest request
    ) {
        return ResponseEntity.ok(tripService.createTrip(userId, request));
    }

    // 여행 상세 조회 (GET /trips/{tripId})
    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(tripService.getTripDetail(userId, tripId));
    }

    // 초대 코드 재발급 (POST /trips/{tripId}/invite-code)
    @PostMapping("/{tripId}/invite-code")
    public ResponseEntity<Map<String, String>> regenerateInviteCode(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        String code = tripService.regenerateInviteCode(userId, tripId);
        return ResponseEntity.ok(Map.of("inviteCode", code));
    }

    // 초대 코드로 여행 참가 (POST /trips/join)
    @PostMapping("/join")
    public ResponseEntity<TripResponse> joinTrip(
            @AuthenticationPrincipal Long userId,
            @RequestBody TripJoinRequest request
    ) {
        return ResponseEntity.ok(tripService.joinTrip(userId, request));
    }

    // 여행 상태 변경 (PATCH /trips/{tripId}/status)
    @PatchMapping("/{tripId}/status")
    public ResponseEntity<TripResponse> updateTripStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @RequestBody TripStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(tripService.updateTripStatus(userId, tripId, request));
    }

    // 월별 여행 조회 (GET /trips/calendar?year=2025&month=9)
    @GetMapping("/calendar")
    public ResponseEntity<TripCalendarResponse> getTripsByMonth(
            @AuthenticationPrincipal Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(tripService.getTripsByMonth(userId, year, month));
    }

    // 다가오는 여행 목록 (GET /trips/upcoming)
    @GetMapping("/upcoming")
    public ResponseEntity<List<TripResponse>> getUpcomingTrips(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(tripService.getUpcomingTrips(userId));
    }

    // 여행자 목록 조회
    @GetMapping("/{tripId}/members")
    public ResponseEntity<List<TripMemberResponse>> getTripMembers(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(tripService.getTripMembers(userId, tripId));
    }

    @GetMapping("/me/trips")
    public ResponseEntity<List<PastTripResponse>> getMyTrips(
            @AuthenticationPrincipal Long userId,
            @RequestParam String status
    ) {
        if ("PAST".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(tripService.getPastTrips(userId));
        }
        throw new IllegalArgumentException("지원하지 않는 status 값입니다");
    }

    // 진행중인 여행 조회
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
