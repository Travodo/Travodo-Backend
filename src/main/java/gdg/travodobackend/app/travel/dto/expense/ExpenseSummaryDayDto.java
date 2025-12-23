package gdg.travodobackend.app.travel.dto.expense;

import java.time.LocalDate;

public record ExpenseSummaryDayDto(
        Integer dayIndex,
        LocalDate date,
        Integer amount
) {}
