package gdg.travodobackend.app.notification.service;

import gdg.travodobackend.app.notification.dto.NotificationResponse;
import gdg.travodobackend.app.notification.entity.Notification;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripMember;
import gdg.travodobackend.app.notification.repository.NotificationRepository;
import gdg.travodobackend.app.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEmailService notificationEmailService;

    /**
     * 공동 준비물 추가 알림 생성
     */
    @Transactional
    public void createSharedItemAddedNotification(
            Trip trip,
            User actor,
            String itemName
    ) {
        for (TripMember member : trip.getMembers()) {
            User receiver = member.getUser();

            if (receiver.getId().equals(actor.getId())) continue;

            Notification notification = Notification.builder()
                    .receiver(receiver)
                    .title("공동 준비물이 추가되었어요")
                    .message("공동 준비물 '" + itemName + "' 이(가) 추가되었습니다.")
                    .build();

            notificationRepository.save(notification);

            // 이메일은 실패해도 알림 생성에 영향 없음
            try {
                notificationEmailService.sendSharedItemNotificationMail(
                        receiver.getEmail(),
                        notification.getTitle(),
                        notification.getMessage()
                );
            } catch (Exception e) {
                // 로그만 남기고 무시 (정책 A)
                log.warn("이메일 발송 실패: {}", receiver.getEmail(), e);
            }
        }
    }
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
        }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다"));

        if (!notification.getReceiver().getId().equals(userId)) {
            throw new IllegalStateException("본인의 알림만 읽을 수 있습니다");
        }

        notification.markAsRead();
    }
}
