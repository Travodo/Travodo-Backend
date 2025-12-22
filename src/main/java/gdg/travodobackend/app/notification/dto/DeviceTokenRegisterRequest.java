package gdg.travodobackend.app.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DeviceTokenRegisterRequest {

    @NotBlank
    private String deviceToken;

    @NotBlank
    private String platform;
}
