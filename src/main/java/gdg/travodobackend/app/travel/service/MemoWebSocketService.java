package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.memo.MemoUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메모 업데이트 이벤트를 실시간으로 브로드캐스트
     */
    public void broadcastMemoUpdate(Long tripId, MemoUpdateEvent event) {
        String destination = "/topic/trips/" + tripId + "/memos";
        messagingTemplate.convertAndSend(destination, event);
        log.debug("메모 업데이트 브로드캐스트: tripId={}, type={}, memoId={}", 
                tripId, event.getType(), event.getMemoId());
    }

    /**
     * 메모 생성 이벤트 브로드캐스트
     */
    public void broadcastMemoCreate(Long tripId, Long memoId, Long userId, String title, String content) {
        MemoUpdateEvent event = MemoUpdateEvent.builder()
                .type("create")
                .memoId(memoId)
                .tripId(tripId)
                .userId(userId)
                .title(title)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
        broadcastMemoUpdate(tripId, event);
    }

    /**
     * 메모 수정 이벤트 브로드캐스트
     */
    public void broadcastMemoUpdate(Long tripId, Long memoId, Long userId, String title, String content) {
        MemoUpdateEvent event = MemoUpdateEvent.builder()
                .type("update")
                .memoId(memoId)
                .tripId(tripId)
                .userId(userId)
                .title(title)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
        broadcastMemoUpdate(tripId, event);
    }

    /**
     * 메모 삭제 이벤트 브로드캐스트
     */
    public void broadcastMemoDelete(Long tripId, Long memoId, Long userId) {
        MemoUpdateEvent event = MemoUpdateEvent.builder()
                .type("delete")
                .memoId(memoId)
                .tripId(tripId)
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();
        broadcastMemoUpdate(tripId, event);
    }
}

