package gdg.travodobackend.app.travel.dto;

import java.time.LocalDate;
//여행 생성 요청 DTO
public record TripCreateRequest(
        String name,
        String place,
        LocalDate startDate,
        LocalDate endDate
) {}
