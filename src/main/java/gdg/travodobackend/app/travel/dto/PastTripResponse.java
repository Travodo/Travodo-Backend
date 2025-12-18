package gdg.travodobackend.app.travel.dto;

import java.time.LocalDate;

public record PastTripResponse(
        Long tripId,
        String name,
        String place,
        LocalDate startDate,
        LocalDate endDate,
        int memberCount,
        String color
) {
}
