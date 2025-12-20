package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.SharedItemCreateRequest;
import gdg.travodobackend.app.travel.dto.SharedItemResponse;
import gdg.travodobackend.app.travel.dto.SharedItemUpdateRequest;
import gdg.travodobackend.app.travel.service.SharedItemService;
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
        name = "공동 준비물",
        description = "여행 구성원들이 함께 관리하는 공동 준비물을 관리하는 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/shared-items")
public class SharedItemController {

    private final SharedItemService sharedItemService;

    @Operation(
            summary = "공동 준비물 목록 조회",
            description = "특정 여행의 모든 공동 준비물 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<List<SharedItemResponse>> getItems(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                sharedItemService.getItems(userId, tripId)
        );
    }

    @Operation(
            summary = "공동 준비물 생성",
            description = "여행에 새로운 공동 준비물을 추가합니다. (초기 담당자 없음)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 올바르지 않음"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님")
    })
    @PostMapping
    public ResponseEntity<SharedItemResponse> createItem(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Valid @RequestBody SharedItemCreateRequest request
    ) {
        return ResponseEntity.status(201)
                .body(sharedItemService.createItem(userId, tripId, request));
    }

    @Operation(
            summary = "공동 준비물 수정",
            description = "공동 준비물의 이름 또는 체크 상태를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "공동 준비물을 찾을 수 없음")
    })
    @PatchMapping("/{itemId}")
    public ResponseEntity<SharedItemResponse> updateItem(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "공동 준비물 ID", example = "10")
            @PathVariable Long itemId,

            @Valid @RequestBody SharedItemUpdateRequest request
    ) {
        return ResponseEntity.ok(
                sharedItemService.updateItem(userId, tripId, itemId, request)
        );
    }

    @Operation(
            summary = "공동 준비물 담당자 지정",
            description = "공동 준비물에 현재 로그인한 사용자를 담당자로 지정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "담당자 지정 성공"),
            @ApiResponse(responseCode = "400", description = "이미 담당자가 지정됨"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "공동 준비물을 찾을 수 없음")
    })
    @PatchMapping("/{itemId}/assign")
    public ResponseEntity<SharedItemResponse> assignItem(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "공동 준비물 ID", example = "10")
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(
                sharedItemService.assignItem(userId, tripId, itemId)
        );
    }

    @Operation(
            summary = "공동 준비물 담당자 해제",
            description = "본인이 담당 중인 공동 준비물의 담당자를 해제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "담당자 해제 성공"),
            @ApiResponse(responseCode = "403", description = "본인 담당 준비물이 아님"),
            @ApiResponse(responseCode = "404", description = "공동 준비물을 찾을 수 없음")
    })
    @PatchMapping("/{itemId}/unassign")
    public ResponseEntity<SharedItemResponse> unassignItem(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "공동 준비물 ID", example = "10")
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(
                sharedItemService.unassignItem(userId, tripId, itemId)
        );
    }

    @Operation(
            summary = "공동 준비물 삭제",
            description = "등록된 공동 준비물을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "공동 준비물을 찾을 수 없음")
    })
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "공동 준비물 ID", example = "10")
            @PathVariable Long itemId
    ) {
        sharedItemService.deleteItem(userId, tripId, itemId);
        return ResponseEntity.noContent().build();
    }
}
