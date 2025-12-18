package gdg.travodobackend.app.travel.dto;

import java.time.LocalDate;
import java.util.List;

public record ExpenseDayResponse(
        Long tripId,
        LocalDate date,
        Integer dayIndex,
        Integer totalAmount,
        String currency,
        List<ExpenseDayItemDto> items
) {}
