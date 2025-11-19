package gdg.travodobackend.app.auth.service;

import gdg.travodobackend.app.auth.dto.AuthResponse;
import gdg.travodobackend.app.user.entity.AuthProvider;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import gdg.travodobackend.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse socialLogin(AuthProvider provider, String accessToken, String email, String nickname) {
        // 소셜 로그인 아직 미구현
        throw new UnsupportedOperationException("소셜 로그인은 아직 구현되지 않았습니다. " + provider + " 로그인을 구현해주세요.");
    }

    /**
     * 소셜 로그인 사용자 생성
     * 
     * @param provider 로그인 제공자
     * @param providerId 소셜 로그인 제공자의 사용자 ID
     * @param email 이메일
     * @param nickname 닉네임
     * @return 생성된 User 엔티티
     */
    private User createSocialUser(AuthProvider provider, String providerId, String email, String nickname) {
        // 닉네임 중복 확인 및 처리
        String finalNickname = nickname;
        int suffix = 1;
        while (userRepository.existsByNickname(finalNickname)) {
            finalNickname = nickname + "_" + suffix;
            suffix++;
        }

        User user = User.builder()
                .email(email)
                .password(null)  // 소셜 로그인 사용자는 비밀번호 없음
                .nickname(finalNickname)
                .emailVerified(true)  // 소셜 로그인은 이메일 인증 완료로 간주
                .provider(provider)
                .providerId(providerId)
                .active(true)
                .build();

        return userRepository.save(user);
    }
}

