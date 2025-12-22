package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.TodoCreateRequest;
import gdg.travodobackend.app.travel.dto.TodoResponse;
import gdg.travodobackend.app.travel.dto.TodoUpdateRequest;
import gdg.travodobackend.app.travel.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/todo")
public class TodoController {
    private final TodoService todoService;
    // ㅁㄴㅇㄹ
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getTodos(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                todoService.getTodos(userId, tripId)
        );
    }


    @PostMapping("/{todoId}")
    public ResponseEntity<TodoResponse> createTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody TodoCreateRequest request
            ) {
        return ResponseEntity.status(201)
                .body(todoService.createTodo(userId, tripId, request));
    }

    @PatchMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoUpdateRequest request
    ) {
        return ResponseEntity.ok(
                todoService.updateTodo(userId, tripId, todoId, request)
        );
    }

    @PatchMapping("/{todoId}/assign")
    public ResponseEntity<TodoResponse> assignTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long todoId
    ) {
        return ResponseEntity.ok(
                todoService.assignTodo(userId, tripId, todoId)
        );
    }

    @PatchMapping("/{todoId}/unassign")
    public ResponseEntity<TodoResponse> unassignTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long todoId
    ) {
        return ResponseEntity.ok(
                todoService.unassignTodo(userId, tripId, todoId)
        );
    }

    @DeleteMapping("/{todoId}")
    public ResponseEntity<TodoResponse> deleteTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long todoId
    ) {
        todoService.deleteTodo(userId, tripId, todoId);
        return ResponseEntity.noContent().build();
    }
}
