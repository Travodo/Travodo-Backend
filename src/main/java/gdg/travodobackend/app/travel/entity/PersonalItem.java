package gdg.travodobackend.app.travel.entity;

import gdg.travodobackend.app.travel.entity.Trip;
import gdg.travodobackend.app.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 여행의 개인 준비물인지
    @ManyToOne(fetch = FetchType.LAZY)
    private Trip trip;

    // 어떤 유저의 개인 준비물인지
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String name;

    private boolean checked;

    public void update(String name) {
        this.name = name;
    }

    public void toggle(boolean checked) {
        this.checked = checked;
    }
}
