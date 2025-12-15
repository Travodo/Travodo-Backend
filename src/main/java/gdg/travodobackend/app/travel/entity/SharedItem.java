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
public class SharedItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Trip trip;

    // 누가 가져올지 (미지정 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private User assignee;

    private String name;

    private boolean checked;

    public void assign(User user) {
        this.assignee = user;
    }

    public void unassign() {
        this.assignee = null;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateChecked(boolean checked) {
        this.checked = checked;
    }
}
