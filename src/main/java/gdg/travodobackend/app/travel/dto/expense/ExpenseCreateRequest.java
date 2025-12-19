package gdg.travodobackend.app.travel.dto.expense;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ExpenseCreateRequest(

        @NotNull(message = "date는 필수입니다.")
        LocalDate date,

        @NotNull(message = "dayIndex는 필수입니다.")
        @Positive(message = "dayIndex는 1 이상이어야 합니다.")
        Integer dayIndex,

        @NotBlank(message = "title은 필수입니다.")
        String title,

        String memo,

        @NotNull(message = "amount는 필수입니다.")
        @Positive(message = "amount는 1 이상이어야 합니다.")
        Integer amount,

        @NotBlank(message = "currency는 필수입니다.")
        String currency,

        @NotNull(message = "payerId는 필수입니다.")
        Long payerId,

        @NotEmpty(message = "participantIds는 1명 이상이어야 합니다.")
        List<Long> participantIds
) {}
