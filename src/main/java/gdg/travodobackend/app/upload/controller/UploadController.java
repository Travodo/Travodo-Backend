package gdg.travodobackend.app.upload.controller;

import gdg.travodobackend.app.upload.dto.ImageUploadResponse;
import gdg.travodobackend.app.upload.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "이미지 업로드", description = "S3 이미지 업로드 관련 API")
public class UploadController {

    private final S3Service s3Service;

    /**
     * 단일 이미지 업로드
     */
    @PostMapping("/image")
    @Operation(summary = "단일 이미지 업로드", description = "이미지 파일을 S3에 업로드하고 URL을 반환합니다")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @Parameter(description = "업로드할 이미지 파일")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "저장할 폴더 (기본값: community)")
            @RequestParam(value = "folder", defaultValue = "community") String folder,
            Authentication authentication) {
        
        try {
            String imageUrl = s3Service.uploadImage(file, folder);
            ImageUploadResponse response = ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 다중 이미지 업로드
     */
    @PostMapping("/images")
    @Operation(summary = "다중 이미지 업로드", description = "여러 이미지 파일을 S3에 업로드하고 URL 목록을 반환합니다")
    public ResponseEntity<ImageUploadResponse> uploadImages(
            @Parameter(description = "업로드할 이미지 파일 목록")
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "저장할 폴더 (기본값: community)")
            @RequestParam(value = "folder", defaultValue = "community") String folder,
            Authentication authentication) {
        
        try {
            List<String> imageUrls = s3Service.uploadImages(files, folder);
            ImageUploadResponse response = ImageUploadResponse.builder()
                    .imageUrls(imageUrls)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
