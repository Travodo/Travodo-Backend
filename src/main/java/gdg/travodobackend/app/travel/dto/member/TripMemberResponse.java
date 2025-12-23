package gdg.travodobackend.app.travel.dto.member;

public record TripMemberResponse(
        Long userId,
        String nickname,
        boolean isLeader
) {}
