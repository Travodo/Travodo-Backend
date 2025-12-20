package gdg.travodobackend.app.travel.dto;

import java.time.LocalDate;

public record ExpenseSummaryDayDto(
        Integer dayIndex,
        LocalDate date,
        Integer amount
) {}
