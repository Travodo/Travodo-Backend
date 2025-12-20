package gdg.travodobackend.app.travel.dto;

import java.util.List;

public record ActivityDayResponse(
        Long tripId,
        String date,
        List<ActivityResponse> activities
) {}
