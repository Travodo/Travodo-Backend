package gdg.travodobackend.app.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoUpdateEvent {
    private String type; // "update", "create", "delete"
    private Long memoId;
    private Long tripId;
    private Long userId;
    private String title;
    private String content;
    private Long timestamp;
}

