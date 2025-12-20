package gdg.travodobackend.app.travel.dto.trip;

import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripMember;
import gdg.travodobackend.app.travel.entity.TripStatus;

import java.time.LocalDate;
import java.util.List;

public record TripResponse(
        Long id,
        String name,
        String place,
        LocalDate startDate,
        LocalDate endDate,
        TripStatus status,
        Integer dDay,
        String color,
        List<TripMemberInfo> members  // 동행자 정보 추가
) {
    public static TripResponse from(Trip trip) {
        List<TripMemberInfo> members = trip.getMembers().stream()
                .map(member -> TripMemberInfo.builder()
                        .id(member.getUser().getId())
                        .nickname(member.getUser().getNickname())
                        .isLeader(member.isLeader())
                        .build())
                .toList();

        return new TripResponse(
                trip.getId(),
                trip.getName(),
                trip.getPlace(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getStatus(),
                calculateDDay(trip),
                trip.getColor(),
                members
        );
    }

    private static Integer calculateDDay(Trip trip) {
        return (int) (trip.getStartDate().toEpochDay() - LocalDate.now().toEpochDay());
    }
}
