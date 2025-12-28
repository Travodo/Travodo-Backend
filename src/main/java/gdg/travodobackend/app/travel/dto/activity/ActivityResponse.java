package gdg.travodobackend.app.travel.dto.activity;

import gdg.travodobackend.app.travel.entity.Activity;

import java.time.LocalDateTime;

public record ActivityResponse(
        Long id,
        String title,
        String status
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getTitle(),
                activity.getStatus().name()
        );
    }
}
