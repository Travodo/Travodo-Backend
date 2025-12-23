package gdg.travodobackend.app.travel.dto.item.shared;

import gdg.travodobackend.app.travel.entity.SharedItem;

public record SharedItemResponse(
        Long id,
        String name,
        boolean checked,
        Long assigneeId,
        String assigneeName   // null 이면 "미지정"
) {
    public static SharedItemResponse from(SharedItem item) {
        return new SharedItemResponse(
                item.getId(),
                item.getName(),
                item.isChecked(),
                item.getAssignee() != null ? item.getAssignee().getId() : null,
                item.getAssignee() != null ? item.getAssignee().getNickname() : null
        );
    }
}
