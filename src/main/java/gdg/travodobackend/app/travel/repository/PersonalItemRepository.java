package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.PersonalItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalItemRepository extends JpaRepository<PersonalItem, Long> {

    // 특정 여행 + 특정 유저의 개인 준비물 전체 조회
    List<PersonalItem> findByTripIdAndUserId(Long tripId, Long userId);

    // 특정 여행 + 특정 유저의 개인 준비물 중 하나
    Optional<PersonalItem> findByIdAndTripIdAndUserId(Long id, Long tripId, Long userId);

    // 여행 삭제 시 전체 정리용
    void deleteByTripId(Long tripId);
}
