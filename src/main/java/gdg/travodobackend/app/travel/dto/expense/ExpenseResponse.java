package gdg.travodobackend.app.travel.dto.expense;

import gdg.travodobackend.app.travel.entity.Expense;
import gdg.travodobackend.app.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExpenseResponse(
        Long id,
        Long tripId,
        LocalDate date,
        Integer dayIndex,
        String title,
        String memo,
        Integer amount,
        String currency,
        ExpensePayerDto payer,
        List<Long> participants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getTrip().getId(),
                e.getDate(),
                e.getDayIndex(),
                e.getTitle(),
                e.getMemo(),
                e.getAmount(),
                e.getCurrency(),
                new ExpensePayerDto(e.getPayer().getId(), e.getPayer().getNickname()),
                e.getParticipants().stream().map(User::getId).toList(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
