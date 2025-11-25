package gdg.travodobackend.app.travel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    public void updateInviteCode(String newCode) {
        this.inviteCode = newCode;
    }

}
