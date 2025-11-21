package gdg.travodobackend.app.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", length = 255)
    private String password;  // 소셜 로그인 사용자는 null 가능

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider provider = AuthProvider.EMAIL;  // 로그인 제공자 (EMAIL, KAKAO)

    @Column(name = "provider_id", length = 100)
    private String providerId;  // 소셜 로그인 제공자의 사용자 ID (예: 카카오 ID)

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updatePassword(String password) {
        this.password = password;
    }

    public void deactivate() {
        this.active = false;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }
    
    /**
     * 소셜 로그인 계정 연결 (providerId 추가)
     * 이메일 로그인 사용자가 소셜 로그인을 연동할 때 사용
     */
    public void linkSocialProvider(String providerId) {
        this.providerId = providerId;
    }
}

