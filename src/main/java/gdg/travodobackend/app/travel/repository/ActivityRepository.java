package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByIdAndTripId(Long id, Long tripId);

    List<Activity> findAllByTripIdAndTimeBetween(
            Long tripId,
            LocalDateTime start,
            LocalDateTime end
    );
    List<Activity> findAllByTripId(Long tripId);

    void deleteByTripId(Long tripId);
}
