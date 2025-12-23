package gdg.travodobackend.app.travel.dto.activity;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ActivityCreateRequest(

        @NotBlank(message = "활동 제목은 필수입니다.")
        String title,

        @NotNull(message = "활동 시간은 필수입니다.")
        @Future(message = "활동 시간은 미래여야 합니다.")
        LocalDateTime time
) {}
