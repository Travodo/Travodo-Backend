package gdg.travodobackend.app.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEmailService {

    private final JavaMailSender mailSender;

    public void sendSharedItemNotificationMail(
            String to,
            String title,
            String message
    ) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("gdgoctravodo@gmail.com");
        mail.setTo(to);
        mail.setSubject(title);
        mail.setText(message);
        mailSender.send(mail);
    }
}
