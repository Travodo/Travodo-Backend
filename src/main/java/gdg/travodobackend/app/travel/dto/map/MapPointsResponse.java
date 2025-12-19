package gdg.travodobackend.app.travel.dto.map;

import java.util.List;

public record MapPointsResponse(
        Long tripId,
        String date, // "YYYY-MM-DD"
        List<MapPointDto> points
) {}
