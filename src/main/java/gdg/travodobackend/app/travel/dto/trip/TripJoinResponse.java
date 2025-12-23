package gdg.travodobackend.app.travel.dto.trip;

import gdg.travodobackend.app.travel.entity.TripMember;

public record TripJoinResponse(
        Long userId,
        String nickname,
        boolean isLeader
) {
    public static TripJoinResponse from(TripMember tm) {
        return new TripJoinResponse(
                tm.getUser().getId(),
                tm.getUser().getNickname(),
                tm.isLeader()
        );
    }
}
