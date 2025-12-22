package gdg.travodobackend.app.notification.repository;

import gdg.travodobackend.app.notification.entity.DeviceToken;
import gdg.travodobackend.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByUserAndDeviceToken(User user, String deviceToken);
}
