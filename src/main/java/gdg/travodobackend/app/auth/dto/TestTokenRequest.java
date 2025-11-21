package gdg.travodobackend.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestTokenRequest {
    @NotBlank(message = "테스트 비밀번호는 필수입니다")
    private String password;
    
    private String email;  //특정 사용자의 토큰을 발급받으려면 이메일 입력
}

