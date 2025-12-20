package gdg.travodobackend.app.travel.dto;

import gdg.travodobackend.app.travel.entity.Expense;

public record ExpenseDayItemDto(
        Long id,
        String title,
        String memo,
        Integer amount,
        ExpensePayerDto payer
) {
    public static ExpenseDayItemDto from(Expense e) {
        return new ExpenseDayItemDto(
                e.getId(),
                e.getTitle(),
                e.getMemo(),
                e.getAmount(),
                new ExpensePayerDto(e.getPayer().getId(), e.getPayer().getNickname())
        );
    }
}
