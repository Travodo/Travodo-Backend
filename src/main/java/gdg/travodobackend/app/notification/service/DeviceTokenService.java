package gdg.travodobackend.app.notification.service;

import gdg.travodobackend.app.notification.dto.DeviceTokenRegisterRequest;
import gdg.travodobackend.app.notification.entity.DeviceToken;
import gdg.travodobackend.app.notification.repository.DeviceTokenRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void register(Long userId, DeviceTokenRegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (deviceTokenRepository.findByUserAndDeviceToken(user, request.getDeviceToken()).isPresent()) {
            return;
        }

        DeviceToken deviceToken = DeviceToken.builder()
                .user(user)
                .deviceToken(request.getDeviceToken())
                .platform(request.getPlatform())
                .build();

        deviceTokenRepository.save(deviceToken);
    }
}
