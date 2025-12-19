package gdg.travodobackend.app.travel.dto.trip;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TripCreateRequest(

        @NotBlank(message = "여행 이름은 필수입니다.")
        String name,

        @NotBlank(message = "여행 장소는 필수입니다.")
        String place,

        @NotNull(message = "여행 시작 날짜는 필수입니다.")
        @FutureOrPresent(message = "시작 날짜는 오늘 또는 미래여야 합니다.")
        LocalDate startDate,

        @NotNull(message = "여행 종료 날짜는 필수입니다.")
        @FutureOrPresent(message = "종료 날짜는 오늘 또는 미래여야 합니다.")
        LocalDate endDate

) {

    public boolean isDateInvalid() {
        return endDate.isBefore(startDate);
    }
}
