package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripMemberRepository extends JpaRepository<TripMember, Long> {
    boolean existsByTripIdAndUserId(Long tripId, Long userId);
}
