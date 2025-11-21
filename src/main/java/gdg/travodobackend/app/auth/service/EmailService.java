package gdg.travodobackend.app.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void sendVerificationEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Travodo] 이메일 인증 코드");
        message.setText("안녕하세요.\n\n" +
                "Travodo 이메일 인증 코드입니다.\n\n" +
                "인증 코드: " + code + "\n\n" +
                "이 코드는 10분간 유효합니다.\n\n" +
                "본인이 요청한 것이 아니라면 이 메일을 무시하세요.");
        mailSender.send(message);
    }
}

