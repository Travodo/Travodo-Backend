package gdg.travodobackend.app.travel.dto;

import jakarta.validation.constraints.NotBlank;

public record TodoCreateRequest(
        @NotBlank(message = "todo의 이름은 필수입니다.")
        String title
) {
}
