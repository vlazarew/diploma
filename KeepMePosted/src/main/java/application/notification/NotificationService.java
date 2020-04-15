package application.notification;

import application.data.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import application.service.UserService;

import java.util.List;

@Component
@PropertySource("classpath:telegram.properties")
public class NotificationService {

    private final UserService userService;
    private final JavaMailSender javaMailSender;

    @Value("${bot.email.subject}")
    private String emailSubject;

    @Value("${bot.email.from}")
    private String emailFrom;

    @Value("${bot.email.to}")
    private String emailTo;

    public NotificationService(UserService userService, JavaMailSender javaMailSender) {
        this.userService = userService;
        this.javaMailSender = javaMailSender;
    }

    @Scheduled(fixedRate = 10000)
    public void sendNewApplication() {
        List<User> users = userService.findNewUsers();
        if (users.size() == 0) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        users.forEach(user -> stringBuilder
                .append("Телефон: ")
                .append(user.getPhone())
                .append("\r\n")
                .append("Email: ")
                .append("\r\n")
                .append(user.getEmail())
                .append("\r\n\r\n"));

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(emailTo);
        message.setFrom(emailFrom);
        message.setSubject(emailSubject);
        message.setText(stringBuilder.toString());

        javaMailSender.send(message);
    }
}
