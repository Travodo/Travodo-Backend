package gdg.travodobackend.app.travel.controller;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    // DAY별 지출 목록 조회 (date 또는 dayIndex)
    @GetMapping
    public ResponseEntity<ExpenseDayResponse> getDayExpenses(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer dayIndex
    ) {
        return ResponseEntity.ok(
                expenseService.getDayExpenses(userId, tripId, date, dayIndex)
        );
    }

    // 생성
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody ExpenseCreateRequest request
    ) {
        return ResponseEntity.ok(
                expenseService.create(userId, tripId, request)
        );
    }

    // 수정
    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request
    ) {
        return ResponseEntity.ok(
                expenseService.update(userId, tripId, expenseId, request)
        );
    }

    // 삭제
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long expenseId
    ) {
        expenseService.delete(userId, tripId, expenseId);
        return ResponseEntity.noContent().build();
    }

    // 여행 전체 지출 요약
    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponse> summary(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ResponseEntity.ok(
                expenseService.getSummary(userId, tripId)
        );
    }
}
