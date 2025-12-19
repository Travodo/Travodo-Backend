package gdg.travodobackend.app.travel.dto.trip;

import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripStatus;

import java.time.LocalDate;

public record TripResponse(
        Long id,
        String name,
        String place,
        LocalDate startDate,
        LocalDate endDate,
        TripStatus status,
        Integer dDay,
        String color,
        Integer maxMembers
) {
    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getName(),
                trip.getPlace(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getStatus(),
                calculateDDay(trip),
                trip.getColor(),
                trip.getMaxMembers()
        );
    }

    private static Integer calculateDDay(Trip trip) {
        return (int) (trip.getStartDate().toEpochDay() - LocalDate.now().toEpochDay());
    }
}
