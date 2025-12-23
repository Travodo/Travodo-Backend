package gdg.travodobackend.app.travel.dto.todo;

import gdg.travodobackend.app.travel.entity.TodoStatus;

public record TodoUpdateRequest(
        // ㅁㄴㅇㄹ
        String title,
        TodoStatus status
) {
}
