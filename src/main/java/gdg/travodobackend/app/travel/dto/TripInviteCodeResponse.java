package gdg.travodobackend.app.travel.dto;

import java.time.LocalDateTime;

public record TripInviteCodeResponse(
        String inviteCode,
        LocalDateTime expiresAt,
        boolean expired,
        boolean canRegenerate
) {
}


