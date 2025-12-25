package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripMemberLocation;
import gdg.travodobackend.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripMemberLocationRepository extends JpaRepository<TripMemberLocation, Long> {
    Optional<TripMemberLocation> findByTripAndUser(Trip trip, User user);
    List<TripMemberLocation> findAllByTrip(Trip trip);

    void deleteByTripId(Long tripId);
    @Query("select l.color from TripMemberLocation l where l.trip = :trip")
    List<String> findUsedColorsByTrip(@Param("trip") Trip trip);
}
