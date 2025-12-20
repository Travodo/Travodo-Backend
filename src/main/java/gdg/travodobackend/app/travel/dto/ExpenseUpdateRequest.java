package gdg.travodobackend.app.travel.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record ExpenseUpdateRequest(

        @NotBlank(message = "title은 필수입니다.")
        String title,

        String memo,

        @NotNull(message = "amount는 필수입니다.")
        @Positive(message = "amount는 1 이상이어야 합니다.")
        Integer amount,

        @NotNull(message = "payerId는 필수입니다.")
        Long payerId,

        @NotEmpty(message = "participantIds는 1명 이상이어야 합니다.")
        List<Long> participantIds
) {}
