package gdg.travodobackend.app.travel.dto;

import jakarta.validation.constraints.NotBlank;

public record PersonalItemCreateRequest(

        @NotBlank(message = "개인 준비물 이름은 필수입니다.")
        String name
) {}  
