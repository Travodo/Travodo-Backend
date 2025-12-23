package gdg.travodobackend.app.travel.dto.trip;

import java.util.List;

// 월별 여행 조회 응답 DTO
public record TripCalendarResponse(
        int year,
        int month,
        List<TripResponse> trips
) {}
