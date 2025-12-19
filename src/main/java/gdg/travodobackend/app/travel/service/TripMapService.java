package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.map.LocationUpdateRequest;
import gdg.travodobackend.app.travel.dto.map.MapPointDto;
import gdg.travodobackend.app.travel.dto.map.MapPointsResponse;
import gdg.travodobackend.app.travel.dto.map.MemberLocationResponse;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripMemberLocation;
import gdg.travodobackend.app.travel.repository.ActivityRepository;
import gdg.travodobackend.app.travel.repository.TripMemberLocationRepository;
import gdg.travodobackend.app.travel.repository.TripMemberRepository;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripMapService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripMemberLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    private void validateTripMember(Long tripId, Long userId) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new RuntimeException("여행 멤버만 조회/갱신할 수 있습니다.");
        }
    }

    /**
     * 내 위치 업데이트
     */
    public void updateMyLocation(Long userId, Long tripId, LocationUpdateRequest req) {

        validateTripMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TripMemberLocation location = locationRepository
                .findByTripAndUser(trip, user)
                .orElseGet(() -> TripMemberLocation.builder()
                        .trip(trip)
                        .user(user)
                        .build()
                );

        boolean changed = location.updateIfChanged(
                req.latitude(),
                req.longitude()
        );

        if (changed) {
            locationRepository.save(location);
        }
    }

    /**
     * 동행자 위치 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MemberLocationResponse> getMemberLocations(Long userId, Long tripId) {

        validateTripMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        return locationRepository.findAllByTrip(trip).stream()
                .map(loc -> new MemberLocationResponse(
                        loc.getUser().getId(),
                        loc.getUser().getNickname(),
                        loc.getLatitude(),
                        loc.getLongitude(),
                        loc.getUpdatedAt()
                ))
                .toList();
    }

    /**
     * 지도용 마커(POI) 목록 조회
     * - MEMBER: 동행자 현재 위치
     * - ACTIVITY: 오늘 일정 장소
     */
    @Transactional(readOnly = true)
    public MapPointsResponse getMapPoints(
            Long userId,
            Long tripId,
            LocalDate date
    ) {

        validateTripMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        List<MapPointDto> points = new ArrayList<>();

        locationRepository.findAllByTrip(trip).forEach(loc ->
                points.add(MapPointDto.member(
                        loc.getUser().getId(),
                        loc.getUser().getNickname(),
                        loc.getLatitude(),
                        loc.getLongitude()
                ))
        );

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        activityRepository
                .findAllByTripIdAndTimeBetween(tripId, start, end)
                .forEach(activity ->
                        points.add(MapPointDto.activity(
                                activity.getId(),
                                activity.getTitle(),
                                activity.getStatus().name(),
                                activity.getLatitude(),
                                activity.getLongitude()
                        ))
                );

        return new MapPointsResponse(
                tripId,
                date.toString(),
                points
        );
    }
}
