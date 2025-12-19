package gdg.travodobackend.app.travel.dto.map;

public record MapPointDto(
        String type,

        Long memberId,
        String nickname,

        Long activityId,
        String title,
        String status,

        Double latitude,
        Double longitude
) {

    public static MapPointDto member(
            Long memberId, String nickname,
            Double latitude, Double longitude
    ) {
        return new MapPointDto(
                "MEMBER",
                memberId, nickname,
                null, null, null,
                latitude, longitude
        );
    }

    public static MapPointDto activity(
            Long activityId, String title, String status,
            Double latitude, Double longitude
    ) {
        return new MapPointDto(
                "ACTIVITY",
                null, null,
                activityId, title, status,
                latitude, longitude
        );
    }
}

