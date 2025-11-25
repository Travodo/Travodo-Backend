package gdg.travodobackend.app.travel.service;

import gdg.travodobackend.app.travel.dto.TripCreateRequest;
import gdg.travodobackend.app.travel.dto.TripCreateResponse;
import gdg.travodobackend.app.travel.dto.TripJoinRequest;
import gdg.travodobackend.app.travel.dto.TripResponse;
import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.travel.entity.TripMember;
import gdg.travodobackend.app.travel.entity.TripStatus;
import gdg.travodobackend.app.travel.exception.TripNotFoundException;
import gdg.travodobackend.app.travel.repository.TripMemberRepository;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    public TripCreateResponse createTrip(Long userId, TripCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String inviteCode = generateInviteCode();

        Trip trip = Trip.builder()
                .name(request.name())
                .place(request.place())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(TripStatus.UPCOMING)
                .inviteCode(inviteCode)
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

    public String regenerateInviteCode(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        String newCode = generateInviteCode();
        trip.updateInviteCode(newCode);
        tripRepository.save(trip);

        return newCode;
    }

    public TripResponse joinTrip(Long userId, TripJoinRequest request) {

        Trip trip = tripRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        if (tripMemberRepository.existsByTripIdAndUserId(trip.getId(), userId))
            throw new RuntimeException("이미 참가한 여행입니다.");

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

    public TripResponse getTripDetail(Long userId, Long tripId) {
        // 1) 여행 존재 여부 확인
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("여행을 찾을 수 없습니다."));

        // 2) (선택) 유저가 참가한 여행인지 검증
        boolean isMember = tripMemberRepository.existsByTripIdAndUserId(tripId, userId);
        if (!isMember) {
            throw new TripNotFoundException("여행에 참여하지 않은 사용자는 조회할 수 없습니다.");
        }

        // 3) TripResponse 형태로 매핑하여 반환
        return TripResponse.from(trip);
    }


    private String generateInviteCode() {
        return String.valueOf((int)(Math.random() * 90000) + 10000); // 5자리
    }
}
