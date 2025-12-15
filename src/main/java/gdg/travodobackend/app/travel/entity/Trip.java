package gdg.travodobackend.app.travel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String place;
    private LocalDate startDate;
    private LocalDate endDate;
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    private TripStatus status; // UPCOMING/ONGOING/FINISHED

    private String color;

    @OneToMany(mappedBy = "trip")
    private List<TripMember> members = new ArrayList<>();
    private LocalDateTime inviteCodeExpiresAt;

    // 여행 상태 변경 메서드
    public void updateStatus(TripStatus status) {
        this.status = status;
    }

    // 초대코드 유효성 검사
    public boolean isInviteCodeExpired() {
        return inviteCodeExpiresAt != null && LocalDateTime.now().isAfter(inviteCodeExpiresAt);
    }

    // 만료시간 설정 메서드
    public void updateInviteCode(String newCode, LocalDateTime expiresAt) {
        this.inviteCode = newCode;
        this.inviteCodeExpiresAt = expiresAt;
    }


}
