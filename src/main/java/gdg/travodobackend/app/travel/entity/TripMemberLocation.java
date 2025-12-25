package gdg.travodobackend.app.travel.entity;

import gdg.travodobackend.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"trip_id", "user_id"})
        }
)
public class TripMemberLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    private double latitude;
    private double longitude;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String color;

    public void update(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * polling 최적화용
     */
    public boolean updateIfChanged(double latitude, double longitude) {
        if (Double.compare(this.latitude, latitude) == 0 &&
                Double.compare(this.longitude, longitude) == 0) {
            return false;
        }
        update(latitude, longitude);
        return true;
    }

    // 최초 1회만 색상 지정
    public void assignColorIfAbsent() {
        if (this.color == null) {
            this.color = MapColor.random();
        }
    }

    public void assignColor(String color) {
        this.color = color;
    }
}
