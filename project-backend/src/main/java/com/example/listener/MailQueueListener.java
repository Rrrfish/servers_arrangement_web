package com.example.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {
    @Resource
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String username;

    @RabbitHandler
    public void sendMail(Map<String, Object> map) {
        String email = (String) map.get("email");
        Integer code = (Integer) map.get("code");
        String type = (String) map.get("type");
        SimpleMailMessage message = switch (type) {
            case "register" ->
                this.createSimpleMailMessage("[验证码]服务器管理系统账户注册",
                        "您正在注册服务器管理系统的账户，如果不是你的操作，回复JZ也不能阻止该用户登录你的账户\n验证码： " + code +"， 有效时间3分钟",
                        email);

            case "reset" ->
                this.createSimpleMailMessage("[验证码]服务器管理系统账户密码重置",
                        "您正在重置您的的密码\n验证码： " + code +"， 有效时间3分钟",
                        email);

            default -> null;
        };
        if(message == null) {return;}
        mailSender.send(message);
    }

    private SimpleMailMessage createSimpleMailMessage(String subject, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }

}
