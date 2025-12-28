package gdg.travodobackend.app.travel.dto.activity;

import jakarta.validation.constraints.NotBlank;

public record ActivityUpdateRequest(

        @NotBlank(message = "활동 제목은 필수입니다.")
        String title

) {}
