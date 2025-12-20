package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.PersonalItemCreateRequest;
import gdg.travodobackend.app.travel.dto.PersonalItemResponse;
import gdg.travodobackend.app.travel.dto.PersonalItemUpdateRequest;
import gdg.travodobackend.app.travel.service.PersonalItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/personal-items")
public class PersonalItemController {

    private final PersonalItemService personalItemService;

    // 개인 준비물 전체 조회
    @GetMapping
    public ResponseEntity<List<PersonalItemResponse>> getMyItems(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                personalItemService.getMyItems(userId, tripId)
        );
    }

    // 개인 준비물 생성 
    @PostMapping
    public ResponseEntity<PersonalItemResponse> createItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody PersonalItemCreateRequest request
    ) {
        return ResponseEntity.ok(
                personalItemService.createItem(userId, tripId, request)
        );
    }

    // 개인 준비물 수정
    @PatchMapping("/{itemId}")
    public ResponseEntity<PersonalItemResponse> updateItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long itemId,
            @Valid @RequestBody PersonalItemUpdateRequest request
    ) {
        return ResponseEntity.ok(
                personalItemService.updateItem(userId, tripId, itemId, request)
        );
    }

    // 개인 준비물 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long itemId
    ) {
        personalItemService.deleteItem(userId, tripId, itemId);
        return ResponseEntity.noContent().build();
    }
}
