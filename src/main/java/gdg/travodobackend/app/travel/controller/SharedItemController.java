package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.SharedItemCreateRequest;
import gdg.travodobackend.app.travel.dto.SharedItemResponse;
import gdg.travodobackend.app.travel.dto.SharedItemUpdateRequest;
import gdg.travodobackend.app.travel.service.SharedItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/shared-items")
public class SharedItemController {

    private final SharedItemService sharedItemService;

    // 공동 준비물 전체 조회
    @GetMapping
    public ResponseEntity<List<SharedItemResponse>> getItems(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                sharedItemService.getItems(userId, tripId)
        );
    }

    // 공동 준비물 생성
    @PostMapping
    public ResponseEntity<SharedItemResponse> createItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody SharedItemCreateRequest request
    ) {
        return ResponseEntity.status(201)
                .body(sharedItemService.createItem(userId, tripId, request));
    }

    // 공동 준비물 수정
    @PatchMapping("/{itemId}")
    public ResponseEntity<SharedItemResponse> updateItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long itemId,
            @Valid @RequestBody SharedItemUpdateRequest request
    ) {
        return ResponseEntity.ok(
                sharedItemService.updateItem(userId, tripId, itemId, request)
        );
    }

    // 담당자 지정
    @PatchMapping("/{itemId}/assign")
    public ResponseEntity<SharedItemResponse> assignItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(
                sharedItemService.assignItem(userId, tripId, itemId)
        );
    }

    // 담당자 해제
    @PatchMapping("/{itemId}/unassign")
    public ResponseEntity<SharedItemResponse> unassignItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(
                sharedItemService.unassignItem(userId, tripId, itemId)
        );
    }

    // 공동 준비물 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long itemId
    ) {
        sharedItemService.deleteItem(userId, tripId, itemId);
        return ResponseEntity.noContent().build();
    }
}
