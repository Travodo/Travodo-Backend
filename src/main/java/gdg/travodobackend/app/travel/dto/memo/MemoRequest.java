package gdg.travodobackend.app.travel.dto.memo;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoRequest {

    @Size(max = 200, message = "제목은 200자 이하로 입력해주세요")
    private String title;

    private String content;
}

