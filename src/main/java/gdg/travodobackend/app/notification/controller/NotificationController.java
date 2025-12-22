package gdg.travodobackend.app.notification.controller;

import gdg.travodobackend.app.notification.dto.DeviceTokenRegisterRequest;
import gdg.travodobackend.app.notification.dto.NotificationResponse;
import gdg.travodobackend.app.notification.service.DeviceTokenService;
import gdg.travodobackend.app.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "알림",
        description = "알림 조회, 읽음 처리 및 디바이스 토큰 등록 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;

    @PostMapping("/device")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "디바이스 토큰 등록",
            description = """
                    푸시 알림 수신을 위해 사용자의 디바이스 토큰을 등록합니다.
                    
                    - 로그인된 사용자만 등록 가능합니다.
                    - 동일한 디바이스 토큰은 중복 등록되지 않습니다.
                    - 추후 FCM 푸시 알림 전송을 위한 기반 API입니다.
                    """
    )
    public void registerDeviceToken(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DeviceTokenRegisterRequest request
    ) {
        deviceTokenService.register(userId, request);
    }

    @GetMapping
    @Operation(
            summary = "내 알림 목록 조회",
            description = """
                    로그인한 사용자의 알림 목록을 최신순으로 조회합니다.
                    
                    - 본인에게 수신된 알림만 조회됩니다.
                    - 읽음 여부(isRead) 정보를 함께 반환합니다.
                    """
    )
    public List<NotificationResponse> getMyNotifications(
            @AuthenticationPrincipal Long userId
    ) {
        return notificationService.getMyNotifications(userId);
    }

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "알림 읽음 처리",
            description = """
                    특정 알림을 읽음 상태로 변경합니다.
                    
                    - 본인에게 수신된 알림만 읽음 처리할 수 있습니다.
                    - 이미 읽은 알림을 다시 요청해도 문제없이 처리됩니다.
                    - 성공 시 응답 본문 없이 204 상태 코드를 반환합니다.
                    """
    )
    public void markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(userId, notificationId);
    }
}
