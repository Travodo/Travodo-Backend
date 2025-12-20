package gdg.travodobackend.app.upload.service;

import gdg.travodobackend.config.S3Config;
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
        // 파일 크기 검증 (10MB 제한)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        java.util.List<String> allowedExtensions = java.util.List.of("jpg", "jpeg", "png", "gif", "webp");

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
        }

        // Content-Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
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
