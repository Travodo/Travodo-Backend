package gdg.travodobackend.app.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    private String imageUrl;  // 단일 이미지 업로드 시
    private List<String> imageUrls;  // 다중 이미지 업로드 시
}
