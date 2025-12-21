package gdg.travodobackend.app.user.dto;

import gdg.travodobackend.app.user.entity.Gender;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(max = 50, message = "닉네임은 50자 이하로 입력해주세요")
    private String nickname;

    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요")
    private String name;

    private LocalDate birthDate;

    private Gender gender;

    @Size(max = 20, message = "연락처는 20자 이하로 입력해주세요")
    private String phoneNumber;
}

