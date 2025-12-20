package gdg.travodobackend.app.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotBlank(message = "댓글 내용을 입력해주세요")
    private String content;

    private Long parentId;  // 대댓글인 경우 부모 댓글 ID
}
