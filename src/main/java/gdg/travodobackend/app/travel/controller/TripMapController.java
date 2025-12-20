package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.LocationUpdateRequest;
import gdg.travodobackend.app.travel.dto.MapPointsResponse;
import gdg.travodobackend.app.travel.dto.MemberLocationResponse;
import gdg.travodobackend.app.travel.service.TripMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "지도 / 위치",
        description = "여행 중 사용자 위치 갱신 및 동행자 위치/지도용 포인트(POI)를 조회하는 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripMapController {

    private final TripMapService tripMapService;

    @Operation(
            summary = "내 위치 업데이트",
            description = "현재 로그인한 사용자의 위치(위도/경도)를 해당 여행에 저장합니다. " +
                    "좌표가 이전과 동일하면 저장하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "갱신 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 올바르지 않음"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @PostMapping("/{tripId}/location")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyLocation(
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @AuthenticationPrincipal Long userId,

            @Valid @RequestBody LocationUpdateRequest request
    ) {
        tripMapService.updateMyLocation(userId, tripId, request);
    }

    @Operation(
            summary = "동행자 위치 조회",
            description = "해당 여행에 참여 중인 동행자들의 최신 위치(위도/경도)와 갱신 시간을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @GetMapping("/{tripId}/members/locations")
    public List<MemberLocationResponse> getMemberLocations(
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @AuthenticationPrincipal Long userId
    ) {
        return tripMapService.getMemberLocations(userId, tripId);
    }

    @Operation(
            summary = "지도용 포인트(POI) 조회",
            description = "지도에 표시할 포인트 목록을 조회합니다.\n" +
                    "- MEMBER: 동행자 현재 위치\n" +
                    "- ACTIVITY: 지정한 날짜의 여행 활동(일정) 위치"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "date 파라미터 형식 오류"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @GetMapping("/{tripId}/map-points")
    public MapPointsResponse getMapPoints(
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "조회 날짜 (YYYY-MM-DD)", example = "2025-12-21")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @AuthenticationPrincipal Long userId
    ) {
        return tripMapService.getMapPoints(userId, tripId, date);
    }
}
