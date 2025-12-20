package gdg.travodobackend.app.travel.dto;

import java.time.LocalDateTime;

public record MemberLocationResponse(
        Long memberId,
        String nickname,
        double latitude,
        double longitude,
        LocalDateTime updatedAt
) {}
