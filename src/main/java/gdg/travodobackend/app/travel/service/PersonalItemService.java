package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.item.personal.PersonalItemCreateRequest;
import gdg.travodobackend.app.travel.dto.item.personal.PersonalItemResponse;
import gdg.travodobackend.app.travel.dto.item.personal.PersonalItemUpdateRequest;
import gdg.travodobackend.app.travel.entity.PersonalItem;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.repository.PersonalItemRepository;
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
public class PersonalItemService {

    private final PersonalItemRepository personalItemRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    /**
     * 공통: 여행 멤버인지 검증
     */
    private void validateTripMember(Long tripId, Long userId) {
        boolean isMember = tripMemberRepository.existsByTripIdAndUserId(tripId, userId);
        if (!isMember) {
            throw new RuntimeException("여행 멤버만 개인 준비물에 접근할 수 있습니다.");
        }
    }

    /**
     * 개인 준비물 전체 조회 (GET /trips/{tripId}/personal-items)
     * 현재 로그인한 userId 기준
     */
    @Transactional(readOnly = true)
    public List<PersonalItemResponse> getMyItems(Long userId, Long tripId) {
        validateTripMember(tripId, userId);

        return personalItemRepository.findByTripIdAndUserId(tripId, userId).stream()
                .map(PersonalItemResponse::from)
                .toList();
    }

    /**
     * 개인 준비물 생성 (POST /trips/{tripId}/personal-items)
     */
    public PersonalItemResponse createItem(Long userId, Long tripId, PersonalItemCreateRequest request) {
        validateTripMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        PersonalItem item = PersonalItem.builder()
                .trip(trip)
                .user(user)
                .name(request.name())
                .checked(false)
                .build();

        personalItemRepository.save(item);

        return PersonalItemResponse.from(item);
    }

    /**
     * 개인 준비물 수정 (PATCH /trips/{tripId}/personal-items/{itemId})
     */
    public PersonalItemResponse updateItem(Long userId, Long tripId, Long itemId, PersonalItemUpdateRequest request) {
        validateTripMember(tripId, userId);

        PersonalItem item = personalItemRepository
                .findByIdAndTripIdAndUserId(itemId, tripId, userId)
                .orElseThrow(() -> new RuntimeException("개인 준비물을 찾을 수 없습니다."));

        if (request.name() != null) {
            item.update(request.name());
        }
        if (request.checked() != null) {
            item.toggle(request.checked());
        }

        return PersonalItemResponse.from(item);
    }

    /**
     * 개인 준비물 삭제 (DELETE /trips/{tripId}/personal-items/{itemId})
     */
    public void deleteItem(Long userId, Long tripId, Long itemId) {
        validateTripMember(tripId, userId);

        PersonalItem item = personalItemRepository
                .findByIdAndTripIdAndUserId(itemId, tripId, userId)
                .orElseThrow(() -> new RuntimeException("개인 준비물을 찾을 수 없습니다."));

        personalItemRepository.delete(item);
    }
}
