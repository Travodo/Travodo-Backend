package gdg.travodobackend.app.travel.dto;

import gdg.travodobackend.app.travel.entity.TodoStatus;

public record TodoUpdateRequest(
        String title,
        TodoStatus status
) {
}
