package gdg.travodobackend.app.travel.dto.trip;

public record TripMemberResponse(
        Long userId,
        String nickname,
        boolean isLeader
) {}
