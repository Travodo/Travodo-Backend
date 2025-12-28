package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.todo.TodoCreateRequest;
import gdg.travodobackend.app.travel.dto.todo.TodoResponse;
import gdg.travodobackend.app.travel.dto.todo.TodoUpdateRequest;
import gdg.travodobackend.app.travel.entity.Todo;
import gdg.travodobackend.app.travel.entity.TodoStatus;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.repository.TodoRepository;
import gdg.travodobackend.app.travel.repository.TripMemberRepository;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ㅁㄴㅇㄹ
@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {
    private final TodoRepository todoRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    private void validateTripMember(Long tripId, Long userId) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new RuntimeException("여행 멤버만 Todo 리스트에 접근할 수 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodos(Long tripId, Long userId) {
        validateTripMember(tripId, userId);

        return todoRepository.findByTripId(tripId).stream()
                .map(TodoResponse::from)
                .toList();
    }

    public TodoResponse createTodo(
            Long userId, Long tripId, TodoCreateRequest request) {
        validateTripMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        Todo todo = Todo.builder()
                .trip(trip)
                .title(request.title())
                .status(TodoStatus.UNDONE)
                .assignee(null)
                .build();

        todoRepository.save(todo);

        return TodoResponse.from(todo);
    }

    public TodoResponse updateTodo(
            Long userId, Long tripId, Long todoId, TodoUpdateRequest request
    ) {
        validateTripMember(tripId, userId);

        Todo todo = todoRepository
                .findByIdAndTripId(todoId, tripId)
                .orElseThrow(() -> new RuntimeException("todo 항목을 찾을 수 없습니다."));

        if (request.title() != null) {
            todo.updateTitle(request.title());
        }
        if (request.status() != null) {
            todo.updateStatus(request.status());
        }

        return TodoResponse.from(todo);
    }

    public TodoResponse assignTodo(Long userId, Long tripId, Long todoId) {
        validateTripMember(tripId, userId);

        Todo todo = todoRepository
                .findByIdAndTripId(todoId, tripId)
                .orElseThrow(() -> new RuntimeException("todo 항목을 찾을 수 없습니다."));

        if (todo.getAssignee() != null) {
            throw new RuntimeException("이미 할당된 todo 항목입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        todo.assign(user);

        return TodoResponse.from(todo);
    }

    public TodoResponse unassignTodo(Long userId, Long tripId, Long todoId) {
        validateTripMember(tripId, userId);

        Todo todo = todoRepository
                .findByIdAndTripId(todoId, tripId)
                .orElseThrow(() -> new RuntimeException("todo 항목을 찾을 수 없습니다."));

        if (todo.getAssignee() == null) {
            return TodoResponse.from(todo);
        }

        if (!todo.getAssignee().getId().equals(userId)) {
            throw new RuntimeException("본인이 맡은 준비물만 해제할 수 있습니다.");
        }

        todo.unassign();

        return TodoResponse.from(todo);
    }

    public void deleteTodo(Long userId, Long tripId, Long todoId) {
        validateTripMember(tripId, userId);

        Todo todo = todoRepository
                .findByIdAndTripId(todoId, tripId)
                .orElseThrow(() -> new RuntimeException("todo 항목을 찾을 수 없습니다."));

        todoRepository.delete(todo);
    }
}
