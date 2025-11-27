package gdg.travodobackend.healthCheck.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket:}")
    private String bucketName;

    public HealthCheckController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 작동하는지 확인합니다. (배포 환경용)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Travodo Backend Server is running!");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "S3 버킷 연결 테스트", description = "S3 버킷 연결 상태를 확인합니다.")
    @GetMapping("/bucket")
    public ResponseEntity<Map<String, Object>> bucketConnectionCheck() {
        Map<String, Object> response = new HashMap<>();
        
        // S3 연결 테스트
        Map<String, Object> connectionTest = checkS3Connection();
        
        response.put("status", connectionTest.get("status"));
        response.put("timestamp", LocalDateTime.now());
        response.put("bucket", bucketName);
        response.put("connection", connectionTest);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "S3 이미지 업로드 테스트", 
        description = "Swagger에서 파일을 업로드하여 S3 이미지 업로드 기능을 테스트합니다. 업로드 후 테스트 파일은 자동으로 삭제됩니다.",
        requestBody = @RequestBody(
            description = "업로드할 이미지 파일",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
    )
    @PostMapping(value = "/bucket/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> bucketUploadTest(
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        // 파일 유효성 검사
        if (file == null || file.isEmpty()) {
            response.put("status", "DOWN");
            response.put("message", "파일이 제공되지 않았습니다.");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }
        
        // 이미지 파일 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("status", "DOWN");
            response.put("message", "이미지 파일만 업로드 가능합니다.");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }
        
        // 이미지 업로드 테스트
        Map<String, Object> uploadTest = testImageUpload(file);
        
        response.put("status", uploadTest.get("status"));
        response.put("timestamp", LocalDateTime.now());
        response.put("bucket", bucketName);
        response.put("originalFilename", file.getOriginalFilename());
        response.put("contentType", contentType);
        response.put("size", file.getSize());
        response.put("upload", uploadTest);
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> checkS3Connection() {
        Map<String, Object> s3Status = new HashMap<>();
        
        try {
            if (bucketName == null || bucketName.isEmpty()) {
                s3Status.put("status", "UNKNOWN");
                s3Status.put("message", "S3 bucket name is not configured");
                return s3Status;
            }

            // S3 버킷 존재 여부 확인
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.headBucket(headBucketRequest);
            
            s3Status.put("status", "UP");
            s3Status.put("message", "S3 connection successful");
            
        } catch (S3Exception e) {
            s3Status.put("status", "DOWN");
            s3Status.put("message", "S3 connection failed: " + e.getMessage());
            s3Status.put("error", e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "Unknown error");
            
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            String simplifiedMessage = errorMessage;
            
            // 자격 증명 관련 오류인지 확인
            if (errorMessage != null && errorMessage.contains("Unable to load credentials")) {
                simplifiedMessage = "AWS 자격 증명이 설정되지 않았습니다. 환경 변수 AWS_ACCESS_KEY_ID와 AWS_SECRET_ACCESS_KEY를 설정하거나, application.yml에 aws.s3.access-key와 aws.s3.secret-key를 설정해주세요.";
            } else if (errorMessage != null && errorMessage.length() > 500) {
                // 너무 긴 오류 메시지는 간단하게
                simplifiedMessage = "S3 연결 오류가 발생했습니다. 자격 증명 및 네트워크 설정을 확인해주세요.";
            }
            
            s3Status.put("status", "DOWN");
            s3Status.put("message", simplifiedMessage);
            s3Status.put("error", e.getClass().getSimpleName());
            s3Status.put("errorDetail", errorMessage != null && errorMessage.length() <= 500 ? errorMessage : "오류 메시지가 너무 깁니다. 로그를 확인해주세요.");
        }
        
        return s3Status;
    }

    private Map<String, Object> testImageUpload(MultipartFile file) {
        Map<String, Object> uploadStatus = new HashMap<>();
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String testKey = "health-check/test-" + UUID.randomUUID() + extension;
        
        try {
            if (bucketName == null || bucketName.isEmpty()) {
                uploadStatus.put("status", "UNKNOWN");
                uploadStatus.put("message", "S3 bucket name is not configured");
                return uploadStatus;
            }

            // 파일 데이터 읽기
            byte[] fileData = file.getBytes();
            String contentType = file.getContentType();

            // S3에 이미지 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(testKey)
                    .contentType(contentType != null ? contentType : "image/png")
                    .build();

            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(fileData));
            
            uploadStatus.put("status", "UP");
            uploadStatus.put("message", "Image upload successful");
            uploadStatus.put("testKey", testKey);
            uploadStatus.put("uploadedSize", fileData.length);
            
            // 테스트 파일 삭제
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(testKey)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
                uploadStatus.put("cleanup", "success");
            } catch (Exception e) {
                uploadStatus.put("cleanup", "failed");
                uploadStatus.put("cleanupError", e.getMessage());
            }
            
        } catch (S3Exception e) {
            uploadStatus.put("status", "DOWN");
            uploadStatus.put("message", "Image upload failed: " + e.getMessage());
            uploadStatus.put("error", e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "Unknown error");
            
        } catch (IOException e) {
            uploadStatus.put("status", "DOWN");
            uploadStatus.put("message", "File read error: " + e.getMessage());
            uploadStatus.put("error", e.getClass().getSimpleName());
            
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            String simplifiedMessage = errorMessage;
            
            // 자격 증명 관련 오류인지 확인
            if (errorMessage != null && errorMessage.contains("Unable to load credentials")) {
                simplifiedMessage = "AWS 자격 증명이 설정되지 않았습니다. 환경 변수 AWS_ACCESS_KEY_ID와 AWS_SECRET_ACCESS_KEY를 설정하거나, application.yml에 aws.s3.access-key와 aws.s3.secret-key를 설정해주세요.";
            } else if (errorMessage != null && errorMessage.length() > 500) {
                // 너무 긴 오류 메시지는 간단하게
                simplifiedMessage = "S3 업로드 오류가 발생했습니다. 자격 증명 및 네트워크 설정을 확인해주세요.";
            }
            
            uploadStatus.put("status", "DOWN");
            uploadStatus.put("message", simplifiedMessage);
            uploadStatus.put("error", e.getClass().getSimpleName());
            uploadStatus.put("errorDetail", errorMessage != null && errorMessage.length() <= 500 ? errorMessage : "오류 메시지가 너무 깁니다. 로그를 확인해주세요.");
        }
        
        return uploadStatus;
    }
}

