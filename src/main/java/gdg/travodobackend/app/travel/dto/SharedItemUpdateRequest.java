package gdg.travodobackend.app.travel.dto;

public record SharedItemUpdateRequest(
        String name,
        Boolean checked
) {}
