package gdg.travodobackend.app.travel.dto.trip;

import gdg.travodobackend.app.travel.entity.TripStatus;

import java.time.LocalDate;

public record CurrentTripResponse(
        Long id,
        String name,
        TripStatus status,
        LocalDate startDate,
        LocalDate endDate
) {}
