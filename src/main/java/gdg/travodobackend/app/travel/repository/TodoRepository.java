package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByTripId(Long tripId);

    Optional<Todo> findByIdAndTripId(Long id, Long tripId);
}
