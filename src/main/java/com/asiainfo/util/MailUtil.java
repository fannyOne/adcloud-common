package com.asiainfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by SS on 2017/4/18.
 */
@Component
public class MailUtil {

    static String MAIL_SMTP;

    static String MAIL_USERNAME;

    static String MAIL_PASSWORD;

    public static void commonMailSend(String from,
                                      String mailto,
                                      String[] copyto,
                                      String mailContent,
                                      String mailSubject,
                                      String username,
                                      String password) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(mailto);

        if (copyto != null && copyto.length > 0) {
            msg.setCc(copyto);
        }

        msg.setSubject(mailSubject);
        msg.setText(mailContent);

        JavaMailSenderImpl mail = new JavaMailSenderImpl();

        mail.setHost(MAIL_SMTP);

        // 如果邮件服务器需要验证
        mail.setUsername(username);
        mail.setPassword(password);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        mail.setJavaMailProperties(props);
        mail.setDefaultEncoding("UTF-8");

        mail.send(msg);
    }

    public static void simpleMailSend(String mailto,
                                      String[] copyto,
                                      String mailContent,
                                      String mailSubject) {
        commonMailSend(MAIL_USERNAME,
            mailto,
            copyto,
            mailContent,
            mailSubject,
            MAIL_USERNAME,
            MAIL_PASSWORD
        );
    }

    public static void main(String args[]) {
        /*MAIL_SMTP = "mailhq.zj.chinamobile.com";
        MAIL_USERNAME = "ADCloud-Gitlab@zj.chinamobile.com";
        MAIL_PASSWORD = "12345678";
        sendLog("40424115@QQ.COM",null,"123","456");*/
    }

    @Value("${mail.smtp.url}")
    public void setMAIL_SMTP(String MAIL_SMTP) {
        this.MAIL_SMTP = MAIL_SMTP;
    }

    @Value("${mail.username}")
    public void setMAIL_USERNAME(String MAIL_USERNAME) {
        this.MAIL_USERNAME = MAIL_USERNAME;
    }

    @Value("${mail.password}")
    public void setMAIL_PASSWORD(String MAIL_PASSWORD) {
        this.MAIL_PASSWORD = MAIL_PASSWORD;
    }
}
