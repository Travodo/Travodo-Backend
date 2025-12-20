package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.entity.Expense;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.repository.ExpenseRepository;
import gdg.travodobackend.app.travel.repository.TripMemberRepository;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    private void validateTripMember(Long tripId, Long userId) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new RuntimeException("여행 멤버만 지출을 관리할 수 있습니다.");
        }
    }

    private void validateTripMemberForTargetUsers(Long tripId, Long payerId, List<Long> participantIds) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, payerId)) {
            throw new RuntimeException("결제자는 해당 여행의 멤버여야 합니다.");
        }
        for (Long pid : participantIds) {
            if (!tripMemberRepository.existsByTripIdAndUserId(tripId, pid)) {
                throw new RuntimeException("참여자에 여행 멤버가 아닌 사용자가 포함되어 있습니다. userId=" + pid);
            }
        }
    }

    @Transactional(readOnly = true)
    public ExpenseDayResponse getDayExpenses(Long userId, Long tripId, LocalDate date, Integer dayIndex) {
        validateTripMember(tripId, userId);

        if (date == null && dayIndex == null) {
            throw new IllegalArgumentException("date 또는 dayIndex 중 하나는 필수입니다.");
        }
        if (date != null && dayIndex != null) {
            throw new IllegalArgumentException("date와 dayIndex를 동시에 보낼 수 없습니다.");
        }

        List<Expense> expenses;
        LocalDate resolvedDate = null;
        Integer resolvedDayIndex = null;

        if (date != null) {
            expenses = expenseRepository.findByTripIdAndDate(tripId, date);
            resolvedDate = date;
            resolvedDayIndex = expenses.isEmpty() ? null : expenses.get(0).getDayIndex();
        } else {
            expenses = expenseRepository.findByTripIdAndDayIndex(tripId, dayIndex);
            resolvedDayIndex = dayIndex;
            resolvedDate = expenses.isEmpty() ? null : expenses.get(0).getDate();
        }

        int total = expenses.stream().mapToInt(Expense::getAmount).sum();
        String currency = expenses.isEmpty() ? "KRW" : expenses.get(0).getCurrency();

        List<ExpenseDayItemDto> items = expenses.stream()
                .map(ExpenseDayItemDto::from)
                .toList();

        return new ExpenseDayResponse(
                tripId,
                resolvedDate,
                resolvedDayIndex,
                total,
                currency,
                items
        );
    }

    public ExpenseResponse create(Long userId, Long tripId, ExpenseCreateRequest request) {
        validateTripMember(tripId, userId);
        validateTripMemberForTargetUsers(tripId, request.payerId(), request.participantIds());

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        User payer = userRepository.findById(request.payerId())
                .orElseThrow(() -> new RuntimeException("결제자를 찾을 수 없습니다."));

        List<User> participants = userRepository.findAllById(request.participantIds());

        Expense expense = Expense.builder()
                .trip(trip)
                .dayIndex(request.dayIndex())
                .date(request.date())
                .title(request.title())
                .memo(request.memo())
                .amount(request.amount())
                .currency(request.currency())
                .payer(payer)
                .participants(participants)
                .build();

        expenseRepository.save(expense);
        return ExpenseResponse.from(expense);
    }

    public ExpenseResponse update(Long userId, Long tripId, Long expenseId, ExpenseUpdateRequest request) {
        validateTripMember(tripId, userId);
        validateTripMemberForTargetUsers(tripId, request.payerId(), request.participantIds());

        Expense expense = expenseRepository.findByIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new RuntimeException("지출을 찾을 수 없습니다."));

        User payer = userRepository.findById(request.payerId())
                .orElseThrow(() -> new RuntimeException("결제자를 찾을 수 없습니다."));

        List<User> participants = userRepository.findAllById(request.participantIds());

        expense.update(request.title(), request.memo(), request.amount(), payer, participants);

        return ExpenseResponse.from(expense);
    }

    public void delete(Long userId, Long tripId, Long expenseId) {
        validateTripMember(tripId, userId);

        Expense expense = expenseRepository.findByIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new RuntimeException("지출을 찾을 수 없습니다."));

        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getSummary(Long userId, Long tripId) {
        validateTripMember(tripId, userId);

        // 여행 존재 검증(명확한 에러)
        tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        String currency = "KRW";
        Long totalLong = expenseRepository.totalAmountByTrip(tripId);
        int total = totalLong == null ? 0 : totalLong.intValue();

        List<ExpenseSummaryDayDto> days = expenseRepository.summarizeByTrip(tripId).stream()
                .map(r -> new ExpenseSummaryDayDto(
                        r.getDayIndex(),
                        r.getDate(),
                        r.getAmount() == null ? 0 : r.getAmount().intValue()
                ))
                .toList();

        return new ExpenseSummaryResponse(tripId, currency, total, days);
    }
}
