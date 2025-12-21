package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.MemoListResponse;
import gdg.travodobackend.app.travel.dto.MemoRequest;
import gdg.travodobackend.app.travel.dto.MemoResponse;
import gdg.travodobackend.app.travel.service.MemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "여행 메모",
        description = "여행 중 실시간으로 작성할 수 있는 메모 기능 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/memos")
public class MemoController {

    private final MemoService memoService;

    @Operation(
            summary = "메모 목록 조회",
            description = "특정 여행의 모든 메모를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<MemoListResponse> getMemos(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(memoService.getMemos(userId, tripId));
    }

    @Operation(
            summary = "메모 상세 조회",
            description = "특정 메모의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "메모를 찾을 수 없음")
    })
    @GetMapping("/{memoId}")
    public ResponseEntity<MemoResponse> getMemo(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,
            @Parameter(description = "메모 ID", example = "1")
            @PathVariable Long memoId
    ) {
        return ResponseEntity.ok(memoService.getMemo(userId, tripId, memoId));
    }

    @Operation(
            summary = "메모 생성",
            description = "새로운 메모를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<MemoResponse> createMemo(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,
            @Valid @RequestBody MemoRequest request
    ) {
        MemoResponse response = memoService.createMemo(userId, tripId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "메모 수정",
            description = "기존 메모를 수정합니다. 실시간으로 다른 사용자에게 동기화됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "메모를 찾을 수 없음")
    })
    @PutMapping("/{memoId}")
    public ResponseEntity<MemoResponse> updateMemo(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,
            @Parameter(description = "메모 ID", example = "1")
            @PathVariable Long memoId,
            @Valid @RequestBody MemoRequest request
    ) {
        return ResponseEntity.ok(memoService.updateMemo(userId, tripId, memoId, request));
    }

    @Operation(
            summary = "메모 삭제",
            description = "메모를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "메모를 찾을 수 없음")
    })
    @DeleteMapping("/{memoId}")
    public ResponseEntity<Void> deleteMemo(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,
            @Parameter(description = "메모 ID", example = "1")
            @PathVariable Long memoId
    ) {
        memoService.deleteMemo(userId, tripId, memoId);
        return ResponseEntity.noContent().build();
    }
}

