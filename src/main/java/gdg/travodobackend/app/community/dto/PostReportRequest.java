package gdg.travodobackend.app.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReportRequest {

    @NotBlank(message = "신고 사유를 입력해주세요")
    @Size(max = 500, message = "신고 사유는 500자 이하로 입력해주세요")
    private String reason;
}
