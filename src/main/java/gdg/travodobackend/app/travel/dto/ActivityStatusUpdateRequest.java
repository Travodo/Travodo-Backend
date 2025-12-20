package gdg.travodobackend.app.travel.dto;

import gdg.travodobackend.app.travel.entity.ActivityStatus;
import jakarta.validation.constraints.NotNull;

public record ActivityStatusUpdateRequest(
        @NotNull ActivityStatus status
) {}
