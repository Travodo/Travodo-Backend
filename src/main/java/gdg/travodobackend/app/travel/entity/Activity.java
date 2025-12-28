package gdg.travodobackend.app.travel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Trip trip;

    private String title;

    private LocalDateTime time;

    @Enumerated(EnumType.STRING)
    private ActivityStatus status;

    public void update(String title) {
        this.title = title;
    }

    public void updateStatus(ActivityStatus status) {
        this.status = status;
    }

    private String placeName;
    private Double latitude;
    private Double longitude;
}
