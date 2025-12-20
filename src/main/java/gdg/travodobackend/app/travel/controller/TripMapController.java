package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.LocationUpdateRequest;
import gdg.travodobackend.app.travel.dto.MapPointsResponse;
import gdg.travodobackend.app.travel.dto.MemberLocationResponse;
import gdg.travodobackend.app.travel.service.TripMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripMapController {

    private final TripMapService tripMapService;

    /**
     * 내 위치 업데이트
     */
    @PostMapping("/{tripId}/location")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyLocation(
            @PathVariable Long tripId,
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid LocationUpdateRequest request
    ) {
        tripMapService.updateMyLocation(userId, tripId, request);
    }

    /**
     * 동행자 위치 조회
     */
    @GetMapping("/{tripId}/members/locations")
    public List<MemberLocationResponse> getMemberLocations(
            @PathVariable Long tripId,
            @AuthenticationPrincipal Long userId
    ) {
        return tripMapService.getMemberLocations(userId, tripId);
    }

    /**
     * 지도용 POI 조회
     */
    @GetMapping("/{tripId}/map-points")
    public MapPointsResponse getMapPoints(
            @PathVariable Long tripId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Long userId
    ) {
        return tripMapService.getMapPoints(userId, tripId, date);
    }
}
