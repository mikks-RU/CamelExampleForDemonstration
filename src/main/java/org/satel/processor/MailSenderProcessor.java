package org.satel.processor;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;

@Component
public class MailSenderProcessor implements Processor {

    private final Properties properties = new Properties();

    @Override
    public void process(Exchange exchange) throws Exception {
        String fileName = exchange.getProperty("fileName", String.class);
        String fileExt = exchange.getProperty("fileExt", String.class);
        File file = null;
        String bodyMessage;

        if ("txt".equals(fileExt)) {
            bodyMessage = exchange.getIn().getBody(String.class);
        } else {
            bodyMessage = "read file: " + fileName;
            file = exchange.getIn().getBody(File.class);
        }

        properties.load(this.getClass().getClassLoader().getResourceAsStream("email.properties"));

        preparedAndSendMessage(file, bodyMessage, properties);

    }

    private void preparedAndSendMessage(File file, String bodyMessage, Properties properties) throws Exception {

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("mail.smtp.user"),
                        properties.getProperty("mail.smtp.password"));
            }
        });

        try {
            Message message = new MimeMessage(session);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(formatEmailMessage(bodyMessage));
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            if (file != null) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);
                multipart.addBodyPart(attachmentPart);
            }
            message.setContent(multipart);
            message.setFrom(new InternetAddress(properties.getProperty("mail.from.address")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(properties.getProperty("mail.to.address")));
            message.setSubject("Техподдержка Сател. Номер заявки: ".concat(UUID.randomUUID().toString()));

            Transport transport = session.getTransport();
            transport.connect(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.password"));
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatEmailMessage(String bodyMessage) {

        return new StringBuilder()
                .append("Поступило сообщение:\n\n").append(bodyMessage)
                .append("\n\nДанное сообщение автоматически сгенерировано.").append("\n")
                .append("Телефон техподдержки : ")
                .append(properties.getProperty("technical.support.service.mobile"))
                .toString();
    }

}


