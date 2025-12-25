package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.TripMember;
import gdg.travodobackend.app.travel.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripMemberRepository extends JpaRepository<TripMember, Long> {

    boolean existsByTripIdAndUserId(Long tripId, Long userId);
    Optional<TripMember> findByTripIdAndUserId(Long tripId, Long userId);
    Optional<TripMember> findByUserIdAndTripStatus(Long userId, TripStatus status);

    void deleteByTripId(Long tripId);
}
