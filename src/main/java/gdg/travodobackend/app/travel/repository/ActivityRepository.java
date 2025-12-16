package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByIdAndTripId(Long id, Long tripId);
}
