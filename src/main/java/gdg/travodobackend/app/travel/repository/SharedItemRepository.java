package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.SharedItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedItemRepository extends JpaRepository<SharedItem, Long> {

    List<SharedItem> findByTripId(Long tripId);

    Optional<SharedItem> findByIdAndTripId(Long id, Long tripId);
}
