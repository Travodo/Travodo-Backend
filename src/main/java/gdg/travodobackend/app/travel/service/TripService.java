package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.*;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripMember;
import gdg.travodobackend.app.travel.entity.TripStatus;
import gdg.travodobackend.app.travel.repository.PersonalItemRepository;
import gdg.travodobackend.app.travel.repository.TripMemberRepository;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final PersonalItemRepository personalItemRepository;

    // 여행 생성
    public TripCreateResponse createTrip(Long userId, TripCreateRequest request) {

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
    public String regenerateInviteCode(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        String newCode = generateUniqueInviteCode(); // 중복 방지 코드 생성
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        trip.updateInviteCode(newCode,expiresAt);
        tripRepository.save(trip);

        return newCode;
    }

    // 여행 참가
    public TripResponse joinTrip(Long userId, TripJoinRequest request) {

        Trip trip = tripRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        if (trip.isInviteCodeExpired()) {
            throw new RuntimeException("초대코드가 만료되었습니다. 새 코드를 요청하세요.");
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
        } while (tripRepository.findByInviteCode(code).isPresent());
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
}
