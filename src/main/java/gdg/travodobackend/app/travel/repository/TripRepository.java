package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByInviteCode(String inviteCode);
}


