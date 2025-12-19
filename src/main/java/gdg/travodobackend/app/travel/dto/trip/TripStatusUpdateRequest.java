package gdg.travodobackend.app.travel.dto.trip;

import gdg.travodobackend.app.travel.entity.TripStatus;

// 여행 상태 변경 요청 DTO
public record TripStatusUpdateRequest(
        TripStatus status
) {}
