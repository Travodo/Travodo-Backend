package gdg.travodobackend.app.travel.dto;

/**
 * name, checked 둘 다 선택적으로 바꿀 수 있게
 */
public record PersonalItemUpdateRequest(
        String name,
        Boolean checked
) {}
