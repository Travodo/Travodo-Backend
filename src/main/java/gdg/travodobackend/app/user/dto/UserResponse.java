package gdg.travodobackend.app.user.dto;

import gdg.travodobackend.app.user.entity.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private Boolean emailVerified;
    private AuthProvider provider;
    private String providerId;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

