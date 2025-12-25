package gdg.travodobackend.app.upload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:travodo-s3}")
    private String bucketName;

    @Value("${aws.s3.base-url:}")
    private String baseUrl;

    @Value("${aws.s3.region:ap-northeast-2}")
    private String region;

    /**
     * 이미지 파일을 S3에 업로드하고 URL을 반환
     *
     * @param file 업로드할 이미지 파일
     * @param folder S3 내 저장할 폴더 경로 (예: "community", "profile")
     * @return 업로드된 이미지의 URL
     * @throws IllegalArgumentException 파일이 null이거나 유효하지 않은 경우
     * @throws IOException 파일 읽기 실패 시
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (s3Client == null) {
            throw new IllegalStateException("S3 클라이언트가 초기화되지 않았습니다. S3 설정을 확인해주세요.");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 검증
        validateImageFile(file);

        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (extension == null || extension.isBlank()) {
            extension = extensionFromContentType(file.getContentType());
        }
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다. (Content-Type 또는 파일명을 확인해주세요)");
        }
        String fileName = folder + "/" + UUID.randomUUID() + "." + extension;

        try {
            // S3에 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드된 이미지 URL 생성
            String imageUrl = generateImageUrl(fileName);

            log.info("이미지 업로드 성공: {} -> {}", originalFilename, imageUrl);
            return imageUrl;

        } catch (S3Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new IOException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 여러 이미지 파일을 S3에 업로드하고 URL 목록을 반환
     *
     * @param files 업로드할 이미지 파일 목록
     * @param folder S3 내 저장할 폴더 경로
     * @return 업로드된 이미지 URL 목록
     */
    public java.util.List<String> uploadImages(java.util.List<MultipartFile> files, String folder) {
        return files.stream()
                .map(file -> {
                    try {
                        return uploadImage(file, folder);
                    } catch (IOException e) {
                        log.error("이미지 업로드 실패: {}", e.getMessage(), e);
                        throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
                    }
                })
                .toList();
    }

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        // 파일 크기 검증 (기본 30MB 제한)
        long maxSize = 30L * 1024 * 1024; // 30MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 파일 크기는 30MB를 초과할 수 없습니다.");
        }

        // 파일 확장자/Content-Type 검증
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (extension != null) {
            extension = extension.toLowerCase();
        }

        // Content-Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 허용 확장자: iOS 원본(heic/heif)도 지원
        java.util.List<String> allowedExtensions = java.util.List.of(
                "jpg", "jpeg", "png", "gif", "webp", "heic", "heif"
        );

        // 확장자가 있으면 확장자로 검증, 없으면 Content-Type으로 검증
        if (extension != null && !extension.isBlank()) {
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp, heic, heif만 가능)");
            }
        } else {
            String inferred = extensionFromContentType(contentType);
            if (inferred == null || !allowedExtensions.contains(inferred)) {
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (Content-Type: " + contentType + ")");
            }
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Content-Type 기반으로 파일 확장자 추론
     * - "image/jpeg" -> "jpg"
     * - "image/png" -> "png"
     * - "image/heic" -> "heic"
     * - "image/heif" -> "heif"
     */
    private String extensionFromContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) return "";
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/heic" -> "heic";
            case "image/heif" -> "heif";
            // 일부 단말/라이브러리에서 heic가 "image/heic-sequence" 등으로 올 수도 있어 보수적으로 처리
            default -> contentType.toLowerCase().startsWith("image/heic") ? "heic"
                    : contentType.toLowerCase().startsWith("image/heif") ? "heif"
                    : "";
        };
    }

    /**
     * 업로드된 이미지의 URL 생성
     */
    private String generateImageUrl(String fileName) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            // base-url이 설정되어 있으면 사용
            return baseUrl.endsWith("/") ? baseUrl + fileName : baseUrl + "/" + fileName;
        } else {
            // base-url이 없으면 S3 URL 직접 생성
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        }
    }
}
