package gdg.travodobackend.app.travel.dto;

import gdg.travodobackend.app.travel.entity.PersonalItem;

public record PersonalItemResponse(
        Long id,
        String name,
        boolean checked
) {
    public static PersonalItemResponse from(PersonalItem item) {
        return new PersonalItemResponse(
                item.getId(),
                item.getName(),
                item.isChecked()
        );
    }
}
