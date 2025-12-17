package gdg.travodobackend.app.travel.entity;

import gdg.travodobackend.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Trip trip;

    private Integer dayIndex;     // N일차
    private LocalDate date;       // YYYY-MM-DD

    private String title;
    private String memo;

    private Integer amount;
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    private User payer;

    @ManyToMany
    @JoinTable(
            name = "expense_participants",
            joinColumns = @JoinColumn(name = "expense_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> participants = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(String title, String memo, Integer amount, User payer, List<User> participants) {
        this.title = title;
        this.memo = memo;
        this.amount = amount;
        this.payer = payer;
        this.participants = participants;
    }
}
