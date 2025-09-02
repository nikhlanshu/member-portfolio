package org.orioz.memberportfolio.repositories;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.orioz.memberportfolio.auth.properties.SendCommunication;
import org.orioz.memberportfolio.comm.dto.TemplateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Properties;

@Repository
public class SendCommunicationRepository {

    @Autowired
    private TemplateEngine templateEngine;

    public Mono<Void> sendEmail(TemplateProvider templateProvider) {
        return Mono.fromRunnable(() -> {
            try {
                // 1️⃣ Process Thymeleaf template
                String htmlContent = processTemplate(templateProvider);

                // 2️⃣ Setup JavaMail session
                Properties props = new Properties();
                SendCommunication.SmtpConfig smtp = templateProvider.smtpConfig();
                props.put("mail.smtp.auth", String.valueOf(smtp.isAuth()));
                props.put("mail.smtp.starttls.enable", smtp.isEnable());
                props.put("mail.smtp.host", smtp.getHost());
                props.put("mail.smtp.port", String.valueOf(smtp.getMailPort()));

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtp.getUsername(), smtp.getPassword());
                    }
                });

                // 3️⃣ Prepare message
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(smtp.getUsername()));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(templateProvider.toEmail()));
                message.setSubject(templateProvider.template().getSubject());
                message.setContent(htmlContent, templateProvider.smtpConfig().getContent());

                // 4️⃣ Send email
                Transport.send(message);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to send email", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private String processTemplate(TemplateProvider templateProvider) {
        String templatePath = templateProvider.template().getTemplatePath();

        // Remove only "templates/" prefix, not "email/"
        if (templatePath.startsWith("templates/")) {
            templatePath = templatePath.substring("templates/".length());
        }

        // Remove ".html" suffix
        if (templatePath.endsWith(".html")) {
            templatePath = templatePath.substring(0, templatePath.length() - ".html".length());
        }

        Context context = new Context();
        context.setVariables(templateProvider.variables());
        return templateEngine.process(templatePath, context);
    }
}
