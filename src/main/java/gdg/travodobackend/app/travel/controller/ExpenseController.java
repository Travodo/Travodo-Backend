package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.service.ExpenseService;
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

import java.time.LocalDate;

@Tag(
        name = "지출 메모 / 정산",
        description = "여행 중 발생한 지출을 기록하고 일자별·전체 정산을 제공하는 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
            summary = "DAY별 지출 목록 조회",
            description = """
                특정 여행의 지출을 하루 단위로 조회합니다.
                - date 또는 dayIndex 중 하나만 전달해야 합니다.
                - 둘 다 전달하거나 둘 다 비우면 오류가 발생합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (date/dayIndex 조건 위반)"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ExpenseDayResponse> getDayExpenses(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "조회 날짜 (YYYY-MM-DD)", example = "2025-12-20")
            @RequestParam(required = false) LocalDate date,

            @Parameter(description = "여행 일차 index (0부터 시작)", example = "0")
            @RequestParam(required = false) Integer dayIndex
    ) {
        return ResponseEntity.ok(
                expenseService.getDayExpenses(userId, tripId, date, dayIndex)
        );
    }

    @Operation(
            summary = "지출 생성",
            description = "여행 중 발생한 새로운 지출을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행 또는 사용자 없음")
    })
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Valid @RequestBody ExpenseCreateRequest request
    ) {
        return ResponseEntity.ok(
                expenseService.create(userId, tripId, request)
        );
    }

    @Operation(
            summary = "지출 수정",
            description = "이미 등록된 지출의 금액, 결제자, 참여자 등의 정보를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "지출을 찾을 수 없음")
    })
    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> update(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "지출 ID", example = "10")
            @PathVariable Long expenseId,

            @Valid @RequestBody ExpenseUpdateRequest request
    ) {
        return ResponseEntity.ok(
                expenseService.update(userId, tripId, expenseId, request)
        );
    }

    @Operation(
            summary = "지출 삭제",
            description = "등록된 지출을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "지출을 찾을 수 없음")
    })
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId,

            @Parameter(description = "지출 ID", example = "10")
            @PathVariable Long expenseId
    ) {
        expenseService.delete(userId, tripId, expenseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "여행 전체 지출 요약",
            description = "여행 전체 기간의 총 지출 금액과 일자별 지출 합계를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "여행 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음")
    })
    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponse> summary(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "여행 ID", example = "1")
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                expenseService.getSummary(userId, tripId)
        );
    }
}
