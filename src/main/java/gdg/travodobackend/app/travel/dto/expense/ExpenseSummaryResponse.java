package gdg.travodobackend.app.travel.dto.expense;

import java.util.List;

public record ExpenseSummaryResponse(
        Long tripId,
        String currency,
        Integer totalAmount,
        List<ExpenseSummaryDayDto> days
) {}
