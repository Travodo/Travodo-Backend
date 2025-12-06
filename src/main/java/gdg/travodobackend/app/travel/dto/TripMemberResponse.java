package gdg.travodobackend.app.travel.dto;

public record TripMemberResponse(
        Long userId,
        String nickname,
        boolean isLeader
) {}
