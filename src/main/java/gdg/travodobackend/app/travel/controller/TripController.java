package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.TripCreateRequest;
import gdg.travodobackend.app.travel.dto.TripCreateResponse;
import gdg.travodobackend.app.travel.dto.TripJoinRequest;
import gdg.travodobackend.app.travel.dto.TripResponse;
import gdg.travodobackend.app.travel.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(
            @AuthenticationPrincipal Long userId,
            @RequestBody TripCreateRequest request
    ) {
        return ResponseEntity.ok(tripService.createTrip(userId, request));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(tripService.getTripDetail(userId, tripId));
    }

    @PostMapping("/{tripId}/invite-code")
    public ResponseEntity<Map<String, String>> regenerateInviteCode(
            @PathVariable Long tripId
    ) {
        String code = tripService.regenerateInviteCode(tripId);
        return ResponseEntity.ok(Map.of("inviteCode", code));
    }

    @PostMapping("/join")
    public ResponseEntity<TripResponse> joinTrip(
            @AuthenticationPrincipal Long userId,
            @RequestBody TripJoinRequest request
    ) {
        return ResponseEntity.ok(tripService.joinTrip(userId, request));
    }
}
