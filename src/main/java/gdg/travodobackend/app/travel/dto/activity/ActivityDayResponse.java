package gdg.travodobackend.app.travel.dto.activity;

import java.util.List;

public record ActivityDayResponse(
        Long tripId,
        String date,
        List<ActivityResponse> activities
) {}
