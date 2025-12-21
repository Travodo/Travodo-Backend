package gdg.travodobackend.app.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoListResponse {
    private List<MemoResponse> memos;
    private int totalCount;
}

