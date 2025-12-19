package gdg.travodobackend.app.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripMemberInfo {
    private Long id;
    private String nickname;
    private Boolean isLeader;
}
