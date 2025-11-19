package gdg.travodobackend.app.user.entity;

/**
 * 소셜 로그인 제공자 타입
 * 향후 카카오, 구글 로그인 추가 시 사용
 */
public enum AuthProvider {
    EMAIL,      // 이메일 로그인
    KAKAO,      // 카카오 로그인 (향후 추가 예정)
    GOOGLE      // 구글 로그인 (향후 추가 예정)
}

