package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.activity.*;
import gdg.travodobackend.app.travel.entity.*;
import gdg.travodobackend.app.travel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;

    private void validateTripMember(Long tripId, Long userId) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new RuntimeException("여행 멤버만 활동을 관리할 수 있습니다.");
        }
    }

    public ActivityResponse create(
            Long userId, Long tripId, ActivityCreateRequest request
    ) {
        validateTripMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        Activity activity = Activity.builder()
                .trip(trip)
                .title(request.title())
                .time(request.time())
                .status(ActivityStatus.PENDING)
                .build();

        activityRepository.save(activity);

        return ActivityResponse.from(activity);
    }

    public ActivityResponse update(
            Long userId, Long tripId, Long activityId, ActivityUpdateRequest request
    ) {
        validateTripMember(tripId, userId);

        Activity activity = activityRepository
                .findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new RuntimeException("활동을 찾을 수 없습니다."));

        activity.update(request.title(), request.time());

        return ActivityResponse.from(activity);
    }

    public void delete(Long userId, Long tripId, Long activityId) {
        validateTripMember(tripId, userId);

        Activity activity = activityRepository
                .findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new RuntimeException("활동을 찾을 수 없습니다."));

        activityRepository.delete(activity);
    }

    public ActivityResponse updateStatus(
            Long userId, Long tripId, Long activityId, ActivityStatusUpdateRequest request
    ) {
        validateTripMember(tripId, userId);

        Activity activity = activityRepository
                .findByIdAndTripId(activityId, tripId)
                .orElseThrow(() -> new RuntimeException("활동을 찾을 수 없습니다."));

        activity.updateStatus(request.status());

        return ActivityResponse.from(activity);
    }

    @Transactional(readOnly = true)
    public ActivityDayResponse getActivitiesByDate(
            Long userId, Long tripId, LocalDate date
    ) {
        validateTripMember(tripId, userId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        if (trip.getStatus() != TripStatus.ONGOING) {
            throw new RuntimeException("진행 중인 여행에서만 오늘 일정을 조회할 수 있습니다.");
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<ActivityResponse> activities = activityRepository
                .findAllByTripIdAndTimeBetween(tripId, start, end)
                .stream()
                .map(ActivityResponse::from)
                .toList();

        return new ActivityDayResponse(
                tripId,
                date.toString(),
                activities
        );
    }

}
