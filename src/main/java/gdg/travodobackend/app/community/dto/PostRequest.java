package gdg.travodobackend.app.community.dto;

import gdg.travodobackend.app.community.entity.TravelTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 200, message = "제목은 200자 이하로 입력해주세요")
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    private Long tripId;  // 선택 사항

    @NotEmpty(message = "여행 유형 태그를 선택해주세요")
    private List<TravelTag> tags;

    private List<String> imageUrls;  // 이미지 URL 목록

    private String thumbnailUrl;  // 썸네일 URL
}

