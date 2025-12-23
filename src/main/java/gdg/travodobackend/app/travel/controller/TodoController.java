package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.TodoCreateRequest;
import gdg.travodobackend.app.travel.dto.TodoResponse;
import gdg.travodobackend.app.travel.dto.TodoUpdateRequest;
import gdg.travodobackend.app.travel.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Todo",
        description = "여행별 Todo(준비물/할 일) 관리 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/todo")
public class TodoController {

    private final TodoService todoService;

    @Operation(
            summary = "Todo 목록 조회",
            description = "해당 여행에 속한 Todo 목록을 조회합니다. 여행 멤버만 접근할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행이 존재하지 않음")
    })
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getTodos(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                todoService.getTodos(tripId, userId)
        );
    }

    @Operation(
            summary = "Todo 생성",
            description = "여행에 새로운 Todo 항목을 추가합니다. 초기 상태는 UNDONE입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Todo 생성 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행이 존재하지 않음")
    })
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,
            @Valid @RequestBody TodoCreateRequest request
    ) {
        return ResponseEntity.status(201)
                .body(todoService.createTodo(userId, tripId, request));
    }

    @Operation(
            summary = "Todo 수정",
            description = "Todo 제목 또는 상태를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo 수정 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "Todo가 존재하지 않음")
    })
    @PatchMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,
            @Parameter(description = "Todo ID", example = "10")
            @PathVariable Long todoId,
            @Valid @RequestBody TodoUpdateRequest request
    ) {
        return ResponseEntity.ok(
                todoService.updateTodo(userId, tripId, todoId, request)
        );
    }

    @Operation(
            summary = "Todo 담당자 지정",
            description = "Todo를 현재 로그인한 사용자에게 할당합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo 담당자 지정 성공"),
            @ApiResponse(responseCode = "400", description = "이미 담당자가 존재함"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님")
    })
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

    @Operation(
            summary = "Todo 담당자 해제",
            description = "본인이 담당한 Todo만 해제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo 담당자 해제 성공"),
            @ApiResponse(responseCode = "403", description = "본인이 담당한 Todo가 아님")
    })
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

    @Operation(
            summary = "Todo 삭제",
            description = "Todo 항목을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todo 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "Todo가 존재하지 않음")
    })
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long todoId
    ) {
        todoService.deleteTodo(userId, tripId, todoId);
        return ResponseEntity.noContent().build();
    }
}
