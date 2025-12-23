package gdg.travodobackend.app.travel.dto.item.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SharedItemCreateRequest(

        @NotBlank(message = "공동 준비물 이름은 필수입니다.")
        String name,

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1 이상이어야 합니다.")
        Integer quantity
) {}
