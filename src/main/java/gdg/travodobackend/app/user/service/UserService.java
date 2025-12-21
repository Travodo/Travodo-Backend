package gdg.travodobackend.app.user.service;

import gdg.travodobackend.app.auth.service.AuthService;
import gdg.travodobackend.app.upload.service.S3Service;
import gdg.travodobackend.app.user.dto.UserResponse;
import gdg.travodobackend.app.user.dto.UserUpdateRequest;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return convertToResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 닉네임 변경 요청이 있고, 현재 닉네임과 다르면 중복 체크
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            String oldNickname = user.getNickname();
            authService.checkNicknameDuplicate(request.getNickname());
            user.updateNickname(request.getNickname());
            log.info("사용자 {} 닉네임 변경: {} -> {}", userId, oldNickname, request.getNickname());
        }

        // 프로필 정보 업데이트 (이름, 생년월일, 성별, 연락처)
        user.updateProfile(request.getName(), request.getBirthDate(), request.getGender(), request.getPhoneNumber());

        return convertToResponse(user);
    }

    @Transactional
    public UserResponse updateProfileImage(Long userId, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없습니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        try {
            // 기존 프로필 이미지가 있으면 S3에서 삭제하는 로직은 선택사항
            // (S3Service에 delete 메서드가 없으므로 일단 새 이미지만 업로드)
            
            // S3에 프로필 이미지 업로드
            String profileImageUrl = s3Service.uploadImage(imageFile, "profile");
            user.updateProfileImageUrl(profileImageUrl);
            
            log.info("사용자 {} 프로필 이미지 업로드 완료: {}", userId, profileImageUrl);
            return convertToResponse(user);
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new IllegalArgumentException("프로필 이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public UserResponse deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.deleteProfileImage();
        log.info("사용자 {} 프로필 이미지 삭제", userId);
        return convertToResponse(user);
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .emailVerified(user.getEmailVerified())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .active(user.getActive())
                .profileImageUrl(user.getProfileImageUrl())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

