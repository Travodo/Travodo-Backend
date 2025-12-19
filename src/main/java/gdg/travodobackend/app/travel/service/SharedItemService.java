package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.item.shared.SharedItemCreateRequest;
import gdg.travodobackend.app.travel.dto.item.shared.SharedItemResponse;
import gdg.travodobackend.app.travel.dto.item.shared.SharedItemUpdateRequest;
import gdg.travodobackend.app.travel.entity.SharedItem;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.repository.SharedItemRepository;
import gdg.travodobackend.app.travel.repository.TripMemberRepository;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedItemService {

    private final SharedItemRepository sharedItemRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    /**
     * 여행 멤버 검증
     */
    private void validateTripMember(Long tripId, Long userId) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new RuntimeException("여행 멤버만 공동 준비물에 접근할 수 있습니다.");
        }
    }

    /**
     * 공동 준비물 전체 조회
     */
    @Transactional(readOnly = true)
    public List<SharedItemResponse> getItems(Long userId, Long tripId) {
        validateTripMember(tripId, userId);

        return sharedItemRepository.findByTripId(tripId).stream()
                .map(SharedItemResponse::from)
                .toList();
    }

    /**
     * 공동 준비물 생성 (담당자 미지정)
     */
    public SharedItemResponse createItem(
            Long userId, Long tripId, SharedItemCreateRequest request
    ) {
        validateTripMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        SharedItem item = SharedItem.builder()
                .trip(trip)
                .name(request.name())
                .checked(false)
                .assignee(null)
                .build();

        sharedItemRepository.save(item);

        return SharedItemResponse.from(item);
    }

    /**
     * 공동 준비물 수정 (이름 / 체크)
     */
    public SharedItemResponse updateItem(
            Long userId, Long tripId, Long itemId, SharedItemUpdateRequest request
    ) {
        validateTripMember(tripId, userId);

        SharedItem item = sharedItemRepository
                .findByIdAndTripId(itemId, tripId)
                .orElseThrow(() -> new RuntimeException("공동 준비물을 찾을 수 없습니다."));

        if (request.name() != null) {
            item.updateName(request.name());
        }
        if (request.checked() != null) {
            item.updateChecked(request.checked());
        }

        return SharedItemResponse.from(item);
    }

    /**
     * 담당자 지정
     */
    public SharedItemResponse assignItem(Long userId, Long tripId, Long itemId) {
        validateTripMember(tripId, userId);

        SharedItem item = sharedItemRepository
                .findByIdAndTripId(itemId, tripId)
                .orElseThrow(() -> new RuntimeException("공동 준비물을 찾을 수 없습니다."));

        // 이미 담당자 있으면 차단
        if (item.getAssignee() != null) {
            throw new RuntimeException("이미 담당자가 지정된 준비물입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        item.assign(user);

        return SharedItemResponse.from(item);
    }

    /**
     * 담당자 해제
     */
    public SharedItemResponse unassignItem(Long userId, Long tripId, Long itemId) {
        validateTripMember(tripId, userId);

        SharedItem item = sharedItemRepository
                .findByIdAndTripId(itemId, tripId)
                .orElseThrow(() -> new RuntimeException("공동 준비물을 찾을 수 없습니다."));

        // 본인만 해제 가능
        if (item.getAssignee() == null) {
            return SharedItemResponse.from(item);
        }

        if (!item.getAssignee().getId().equals(userId)) {
            throw new RuntimeException("본인이 맡은 준비물만 해제할 수 있습니다.");
        }

        item.unassign();

        return SharedItemResponse.from(item);
    }

    /**
     * 공동 준비물 삭제
     */
    public void deleteItem(Long userId, Long tripId, Long itemId) {
        validateTripMember(tripId, userId);

        SharedItem item = sharedItemRepository
                .findByIdAndTripId(itemId, tripId)
                .orElseThrow(() -> new RuntimeException("공동 준비물을 찾을 수 없습니다."));

        sharedItemRepository.delete(item);
    }
}
