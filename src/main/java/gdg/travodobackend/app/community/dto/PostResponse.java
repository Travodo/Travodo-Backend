package gdg.travodobackend.app.community.dto;

import gdg.travodobackend.app.community.entity.TravelTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private AuthorInfo author;
    private String title;
    private String content;
    private Long tripId;
    private List<TravelTag> tags;
    private List<String> imageUrls;
    private String thumbnailUrl;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;  // 현재 사용자가 좋아요를 눌렀는지 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

