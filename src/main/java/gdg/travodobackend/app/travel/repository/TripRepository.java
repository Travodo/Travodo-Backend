package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    Optional<Trip> findByInviteCode(String inviteCode);

    // 다가오는 여행 (UPCOMING) 조회
    @Query("""
            select distinct t
            from Trip t
            join t.members m
            where m.user.id = :userId
              and t.status = :status
            order by t.startDate asc
            """)
    List<Trip> findUpcomingTripsByUserId(
            @Param("userId") Long userId,
            @Param("status") TripStatus status
    );

    // 특정 기간과 겹치는 여행(달력용) 조회
    @Query("""
            select distinct t
            from Trip t
            join t.members m
            where m.user.id = :userId
              and t.endDate >= :from
              and t.startDate <= :to
            order by t.startDate asc
            """)
    List<Trip> findTripsByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
