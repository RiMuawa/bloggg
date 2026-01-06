package edu.fdzcxy.bloggg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;
    public NotificationService(JavaMailSender mailSender) { this.mailSender = mailSender; }

    @Value("${spring.mail.from:${spring.mail.username}}")
    private String fromAddress;

    public void sendUpdateEmail(String to, String subject, String body) {
        sendUpdateEmail(to, subject, body, null);
    }

    /**
     * 发送更新通知邮件，包含总结内容
     * @param to 收件人
     * @param subject 邮件主题
     * @param body 邮件正文
     * @param summary 内容总结，如果为null则不包含
     */
    public void sendUpdateEmail(String to, String subject, String body, String summary) {
        sendUpdateEmail(to, subject, body, null, summary);
    }

    /**
     * 发送更新通知邮件，包含总结内容
     * @param to 收件人
     * @param subject 邮件主题
     * @param body 邮件正文
     * @param addedContent 新增的原始内容（已废弃，不再显示在邮件中）
     * @param summary 内容总结，如果为null则不包含
     */
    public void sendUpdateEmail(String to, String subject, String body, String addedContent, String summary) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(to);
        msg.setSubject(subject);
        
        // 构建邮件正文
        StringBuilder emailBody = new StringBuilder(body);
        
        // 如果存在总结，则追加
        if (summary != null && !summary.isBlank()) {
            emailBody.append("\n\n");
            emailBody.append("=== 更新内容总结 ===\n");
            emailBody.append(summary);
        }
        
        msg.setText(emailBody.toString());
        mailSender.send(msg);
    }
}

