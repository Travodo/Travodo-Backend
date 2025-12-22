package gdg.travodobackend.app.travel.dto;

import jakarta.validation.constraints.NotBlank;

// ㅁㄴㅇㄹ
public record TodoCreateRequest(
        @NotBlank(message = "todo의 이름은 필수입니다.")
        String title
) {
}
