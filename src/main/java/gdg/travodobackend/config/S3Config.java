package gdg.travodobackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
public class S3Config {

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Bean
    public S3Client s3Client() {
        try {
            // 환경 변수에서 자격 증명을 가져오거나, 빈 값이면 기본 자격 증명 체인 사용
            if (accessKey != null && !accessKey.isEmpty() && 
                secretKey != null && !secretKey.isEmpty()) {
                log.info("S3Client 빈 생성: Access Key를 사용한 자격 증명");
                AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
                
                return S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                        .build();
            } else {
                log.info("S3Client 빈 생성: 기본 자격 증명 체인 사용 (IAM 역할 또는 환경 변수 AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY)");
                // 환경 변수나 IAM 역할에서 자격 증명을 자동으로 가져옴
                // AWS SDK는 기본적으로 AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY 환경 변수를 찾음
                return S3Client.builder()
                        .region(Region.of(region))
                        .build();
            }
        } catch (Exception e) {
            // S3 설정 실패 시에도 애플리케이션이 시작되도록 null 반환
            // HealthCheckController에서 null 체크를 하므로 안전함
            log.warn("S3Client 빈 생성 실패: {}. S3 기능은 사용할 수 없습니다.", e.getMessage());
            return null;
        }
    }
}

