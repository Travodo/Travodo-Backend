package gdg.travodobackend.app.travel.dto.activity;

import gdg.travodobackend.app.travel.entity.ActivityStatus;
import jakarta.validation.constraints.NotNull;

public record ActivityStatusUpdateRequest(
        @NotNull ActivityStatus status
) {}
