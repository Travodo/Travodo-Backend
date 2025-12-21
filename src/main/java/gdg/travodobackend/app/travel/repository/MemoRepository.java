package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.Memo;
import gdg.travodobackend.app.travel.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
    
    List<Memo> findByTripAndDeletedFalseOrderByUpdatedAtDesc(Trip trip);
    
    Optional<Memo> findByIdAndDeletedFalse(Long id);
    
    List<Memo> findByTripIdAndDeletedFalse(Long tripId);
}

