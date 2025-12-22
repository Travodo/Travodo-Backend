package gdg.travodobackend.app.travel.entity;

import gdg.travodobackend.app.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// ㅁㄴㅇㄹ
public class Todo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Trip trip;


    private String title;

    @ManyToOne
    @JoinColumn
    private User assignee;

    private TodoStatus status;

    public void assign(User user) {this.assignee = user;}

    public void unassign() {this.assignee = null;}

    public void updateTitle(String title) {this.title = title;}

    public void updateStatus(TodoStatus status) {this.status = status;}
}
