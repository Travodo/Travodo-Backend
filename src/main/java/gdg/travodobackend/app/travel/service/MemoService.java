package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.entity.Memo;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.repository.MemoRepository;
import gdg.travodobackend.app.travel.repository.TripMemberRepository;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoService {

    private final MemoRepository memoRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final MemoWebSocketService webSocketService;

    /**
     * 여행의 모든 메모 조회
     */
    public MemoListResponse getMemos(Long userId, Long tripId) {
        // 여행 멤버 확인
        validateTripMember(userId, tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다"));

        List<Memo> memos = memoRepository.findByTripAndDeletedFalseOrderByUpdatedAtDesc(trip);
        
        List<MemoResponse> memoResponses = memos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return MemoListResponse.builder()
                .memos(memoResponses)
                .totalCount(memoResponses.size())
                .build();
    }

    /**
     * 메모 상세 조회
     */
    public MemoResponse getMemo(Long userId, Long tripId, Long memoId) {
        // 여행 멤버 확인
        validateTripMember(userId, tripId);

        Memo memo = memoRepository.findByIdAndDeletedFalse(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다"));

        if (!memo.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("해당 여행의 메모가 아닙니다");
        }

        return convertToResponse(memo);
    }

    /**
     * 메모 생성
     */
    @Transactional
    public MemoResponse createMemo(Long userId, Long tripId, MemoRequest request) {
        // 여행 멤버 확인
        validateTripMember(userId, tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Memo memo = Memo.builder()
                .trip(trip)
                .author(user)
                .title(request.getTitle())
                .content(request.getContent())
                .lastEditedBy(userId)
                .build();

        Memo savedMemo = memoRepository.save(memo);
        log.info("메모 생성 완료: tripId={}, memoId={}, userId={}", tripId, savedMemo.getId(), userId);
        
        // 실시간 브로드캐스트
        webSocketService.broadcastMemoCreate(
                tripId, 
                savedMemo.getId(), 
                userId, 
                savedMemo.getTitle(), 
                savedMemo.getContent()
        );
        
        return convertToResponse(savedMemo);
    }

    /**
     * 메모 수정
     */
    @Transactional
    public MemoResponse updateMemo(Long userId, Long tripId, Long memoId, MemoRequest request) {
        // 여행 멤버 확인
        validateTripMember(userId, tripId);

        Memo memo = memoRepository.findByIdAndDeletedFalse(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다"));

        if (!memo.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("해당 여행의 메모가 아닙니다");
        }

        memo.update(request.getTitle(), request.getContent(), userId);
        log.info("메모 수정 완료: tripId={}, memoId={}, userId={}", tripId, memoId, userId);
        
        // 실시간 브로드캐스트
        webSocketService.broadcastMemoUpdate(
                tripId, 
                memoId, 
                userId, 
                memo.getTitle(), 
                memo.getContent()
        );
        
        return convertToResponse(memo);
    }

    /**
     * 메모 삭제
     */
    @Transactional
    public void deleteMemo(Long userId, Long tripId, Long memoId) {
        // 여행 멤버 확인
        validateTripMember(userId, tripId);

        Memo memo = memoRepository.findByIdAndDeletedFalse(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다"));

        if (!memo.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("해당 여행의 메모가 아닙니다");
        }

        memo.delete();
        log.info("메모 삭제 완료: tripId={}, memoId={}, userId={}", tripId, memoId, userId);
        
        // 실시간 브로드캐스트
        webSocketService.broadcastMemoDelete(tripId, memoId, userId);
    }

    /**
     * 여행 멤버인지 확인
     */
    private void validateTripMember(Long userId, Long tripId) {
        boolean isMember = tripMemberRepository.existsByTripIdAndUserId(tripId, userId);
        if (!isMember) {
            throw new IllegalArgumentException("여행 멤버만 접근할 수 있습니다");
        }
    }

    /**
     * Memo 엔티티를 MemoResponse로 변환
     */
    private MemoResponse convertToResponse(Memo memo) {
        User author = memo.getAuthor();
        
        MemoResponse.AuthorInfo authorInfo = MemoResponse.AuthorInfo.builder()
                .id(author.getId())
                .nickname(author.getNickname())
                .profileImageUrl(author.getProfileImageUrl())
                .build();

        return MemoResponse.builder()
                .id(memo.getId())
                .tripId(memo.getTrip().getId())
                .author(authorInfo)
                .title(memo.getTitle())
                .content(memo.getContent())
                .lastEditedBy(memo.getLastEditedBy())
                .lastEditedAt(memo.getLastEditedAt())
                .createdAt(memo.getCreatedAt())
                .updatedAt(memo.getUpdatedAt())
                .build();
    }
}

