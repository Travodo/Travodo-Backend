package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.PersonalItemCreateRequest;
import gdg.travodobackend.app.travel.dto.PersonalItemResponse;
import gdg.travodobackend.app.travel.dto.PersonalItemUpdateRequest;
import gdg.travodobackend.app.travel.service.PersonalItemService;
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
        name = "개인 준비물",
        description = "여행에서 개인이 챙겨야 할 준비물을 관리하는 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/personal-items")
public class PersonalItemController {

    private final PersonalItemService personalItemService;

    @Operation(
            summary = "개인 준비물 목록 조회",
            description = "특정 여행에서 로그인한 사용자의 개인 준비물 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<List<PersonalItemResponse>> getMyItems(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                personalItemService.getMyItems(userId, tripId)
        );
    }

    @Operation(
            summary = "개인 준비물 생성",
            description = "여행에 개인 준비물을 새로 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 올바르지 않음"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님")
    })
    @PostMapping
    public ResponseEntity<PersonalItemResponse> createItem(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Valid @RequestBody PersonalItemCreateRequest request
    ) {
        return ResponseEntity.ok(
                personalItemService.createItem(userId, tripId, request)
        );
    }

    @Operation(
            summary = "개인 준비물 수정",
            description = "개인 준비물의 이름 또는 체크 상태를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "개인 준비물을 찾을 수 없음")
    })
    @PatchMapping("/{itemId}")
    public ResponseEntity<PersonalItemResponse> updateItem(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "개인 준비물 ID", example = "10")
            @PathVariable Long itemId,

            @Valid @RequestBody PersonalItemUpdateRequest request
    ) {
        return ResponseEntity.ok(
                personalItemService.updateItem(userId, tripId, itemId, request)
        );
    }

    @Operation(
            summary = "개인 준비물 삭제",
            description = "등록된 개인 준비물을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "개인 준비물을 찾을 수 없음")
    })
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "개인 준비물 ID", example = "10")
            @PathVariable Long itemId
    ) {
        personalItemService.deleteItem(userId, tripId, itemId);
        return ResponseEntity.noContent().build();
    }
}
