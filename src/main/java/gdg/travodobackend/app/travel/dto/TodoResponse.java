package gdg.travodobackend.app.travel.dto;

import gdg.travodobackend.app.travel.entity.Todo;

public record TodoResponse(
        Long id,
        String title,
        gdg.travodobackend.app.travel.entity.TodoStatus status,
        Long assigneeId,
        String assigneeName
){
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getStatus(),
                todo.getAssignee() != null ? todo.getAssignee().getId() : null,
                todo.getAssignee() != null ? todo.getAssignee().getNickname() : null
        );
    }
}
