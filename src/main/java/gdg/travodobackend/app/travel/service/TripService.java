package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.dto.TripInviteCodeResponse;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripMember;
import gdg.travodobackend.app.travel.entity.TripStatus;
import gdg.travodobackend.app.travel.repository.ActivityRepository;
import gdg.travodobackend.app.travel.repository.ExpenseRepository;
import gdg.travodobackend.app.travel.repository.MemoRepository;
import gdg.travodobackend.app.travel.repository.PersonalItemRepository;
import gdg.travodobackend.app.travel.repository.SharedItemRepository;
import gdg.travodobackend.app.travel.repository.TripMemberRepository;
import gdg.travodobackend.app.travel.repository.TripMemberLocationRepository;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import gdg.travodobackend.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripMemberLocationRepository tripMemberLocationRepository;
    private final SharedItemRepository sharedItemRepository;
    private final PersonalItemRepository personalItemRepository;
    private final ActivityRepository activityRepository;
    private final ExpenseRepository expenseRepository;
    private final MemoRepository memoRepository;
    private final UserRepository userRepository;

    private static final List<String> TRIP_COLORS = List.of(
            "#EE8787", "#FFD2C2", "#EAAF4F", "#FFE386",
            "#A4C664", "#B8CDFF", "#769FFF", "#506CAD"
    );

    private String pickRandomColor() {
        return TRIP_COLORS.get(
                (int) (Math.random() * TRIP_COLORS.size())
        );
    }

    // 여행 생성
    public TripCreateResponse createTrip(Long userId, TripCreateRequest request) {

        if (request.isDateInvalid()) {
            throw new RuntimeException("시작 날짜는 종료 날짜보다 늦을 수 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String inviteCode = generateUniqueInviteCode(); // 중복 방지 코드 생성
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        Trip trip = Trip.builder()
                .name(request.name())
                .place(request.place())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(TripStatus.UPCOMING)
                .inviteCode(inviteCode)
                .inviteCodeExpiresAt(expiresAt)
                .color(pickRandomColor())
                .maxMembers(request.maxMembers())
                .build();
        tripRepository.save(trip);

        TripMember leader = TripMember.builder()
                .trip(trip)
                .user(user)
                .isLeader(true)
                .build();
        tripMemberRepository.save(leader);

        return new TripCreateResponse(
                TripResponse.from(trip),
                inviteCode
        );
    }

    // 초대코드 재발급
    public String regenerateInviteCode(Long userId, Long tripId) {

        TripMember member = tripMemberRepository
                .findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new ForbiddenException("여행 멤버만 초대 코드를 재발급할 수 있습니다."));

        if (!member.isLeader()) {
            throw new ForbiddenException("여행 방장만 초대 코드를 재발급할 수 있습니다.");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        String newCode = generateUniqueInviteCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        trip.updateInviteCode(newCode, expiresAt);

        return newCode;
    }

    // 초대코드 조회 (재발급 없이 현재 코드 반환)
    @Transactional(readOnly = true)
    public TripInviteCodeResponse getInviteCode(Long userId, Long tripId) {
        TripMember member = tripMemberRepository
                .findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new ForbiddenException("여행 멤버만 초대 코드를 조회할 수 있습니다."));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        boolean expired = trip.isInviteCodeExpired();
        boolean canRegenerate = member.isLeader();

        return new TripInviteCodeResponse(
                trip.getInviteCode(),
                trip.getInviteCodeExpiresAt(),
                expired,
                canRegenerate
        );
    }

    // 여행 참가
    @Transactional
    public TripResponse joinTrip(Long userId, TripJoinRequest request) {

        Trip trip = tripRepository.findByInviteCodeForUpdate(request.inviteCode())
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        if (trip.isInviteCodeExpired()) {
            throw new RuntimeException("초대코드가 만료되었습니다.");
        }

        if (trip.isFull()) {
            throw new RuntimeException("여행 인원이 가득 찼습니다.");
        }

        if (tripMemberRepository.existsByTripIdAndUserId(trip.getId(), userId)) {
            throw new RuntimeException("이미 참가한 여행입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TripMember member = TripMember.builder()
                .trip(trip)
                .user(user)
                .isLeader(false)
                .build();

        tripMemberRepository.save(member);

        return TripResponse.from(trip);
    }

    // 여행 상세 조회
    @Transactional(readOnly = true)
    public TripResponse getTripDetail(Long userId, Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        boolean isMember = tripMemberRepository.existsByTripIdAndUserId(tripId, userId);
        if (!isMember) {
            throw new RuntimeException("여행에 참여하지 않은 사용자는 조회할 수 없습니다.");
        }

        return TripResponse.from(trip);
    }

    // 여행 상태 변경
    public TripResponse updateTripStatus(Long userId, Long tripId, TripStatusUpdateRequest request) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        boolean isMember = tripMemberRepository.existsByTripIdAndUserId(tripId, userId);
        if (!isMember) {
            throw new RuntimeException("여행에 참여하지 않은 사용자는 상태를 변경할 수 없습니다.");
        }

        trip.updateStatus(request.status());
        tripRepository.save(trip);

        return TripResponse.from(trip);
    }

    // 월별 여행 조회
    @Transactional(readOnly = true)
    public TripCalendarResponse getTripsByMonth(Long userId, int year, int month) {

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = YearMonth.of(year, month).atEndOfMonth();

        List<Trip> trips = tripRepository.findTripsByUserIdAndPeriod(
                userId, monthStart, monthEnd);

        List<TripResponse> responses = trips.stream()
                .map(TripResponse::from)
                .toList();

        return new TripCalendarResponse(year, month, responses);
    }

    // 다가오는 여행 조회
    @Transactional(readOnly = true)
    public List<TripResponse> getUpcomingTrips(Long userId) {
        return tripRepository.findUpcomingTripsByUserId(userId, TripStatus.UPCOMING)
                .stream()
                .map(TripResponse::from)
                .toList();
    }

    // 중복 없는 초대 코드 생성
    private String generateUniqueInviteCode() {
        String code;
        do {
            code = String.valueOf((int)(Math.random() * 90000) + 10000);
        } while (tripRepository.findByInviteCodeForUpdate(code).isPresent());
        return code;
    }

    public List<TripMemberResponse> getTripMembers(Long userId, Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다."));

        boolean isMember = tripMemberRepository.existsByTripIdAndUserId(tripId, userId);
        if (!isMember) {
            throw new RuntimeException("여행에 참여하지 않은 사용자는 조회할 수 없습니다.");
        }

        return trip.getMembers().stream()
                .map(tm -> new TripMemberResponse(
                        tm.getUser().getId(),
                        tm.getUser().getNickname(),
                        tm.isLeader()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PastTripResponse> getPastTrips(Long userId) {

        return tripRepository.findTripsByUserIdAndStatus(
                        userId, TripStatus.FINISHED
                ).stream()
                .map(trip -> new PastTripResponse(
                        trip.getId(),
                        trip.getName(),
                        trip.getPlace(),
                        trip.getStartDate(),
                        trip.getEndDate(),
                        trip.getMembers().size(),
                        trip.getColor()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CurrentTripResponse getCurrentTrip(Long userId) {

        return tripMemberRepository
                .findByUserIdAndTripStatus(userId, TripStatus.ONGOING)
                .map(tripMember -> {
                    Trip trip = tripMember.getTrip();
                    return new CurrentTripResponse(
                            trip.getId(),
                            trip.getName(),
                            trip.getStatus(),
                            trip.getStartDate(),
                            trip.getEndDate()
                    );
                })
                .orElseThrow(() ->
                        new RuntimeException("아직 진행 중인 여행이 없습니다.")
                );
    }

    /**
     * 여행 삭제 (trip 자체 삭제)
     * - 현재 구현상 Trip과 연관된 엔티티들이 cascade remove 설정이 없으므로
     *   FK 충돌을 피하기 위해 하위 데이터를 먼저 정리한 뒤 Trip을 삭제합니다.
     * - 기본 정책: 여행 방장만 삭제 가능
     */
    public void deleteTrip(Long userId, Long tripId) {
        TripMember member = tripMemberRepository
                .findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new ForbiddenException("여행 멤버만 여행을 삭제할 수 있습니다."));

        if (!member.isLeader()) {
            throw new ForbiddenException("여행 방장만 여행을 삭제할 수 있습니다.");
        }

        // 존재 확인 (삭제 대상이 없으면 404 성격이지만 현재 예외 정책에 맞춰 RuntimeException 유지)
        tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // 하위 데이터 정리 (FK 순서 고려)
        tripMemberLocationRepository.deleteByTripId(tripId);
        sharedItemRepository.deleteByTripId(tripId);
        personalItemRepository.deleteByTripId(tripId);
        activityRepository.deleteByTripId(tripId);
        expenseRepository.deleteParticipantsByTripId(tripId);
        expenseRepository.deleteByTripId(tripId);
        memoRepository.deleteByTripId(tripId);
        tripMemberRepository.deleteByTripId(tripId);

        tripRepository.deleteById(tripId);
    }

}
