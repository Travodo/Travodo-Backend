package gdg.travodobackend.app.travel.dto.trip;
//여행 생성 Response
public record TripCreateResponse(
        TripResponse trip,
        String inviteCode
) {}
