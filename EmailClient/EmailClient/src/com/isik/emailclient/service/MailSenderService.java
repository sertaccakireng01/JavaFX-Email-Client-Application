package com.isik.emailclient.service;

import com.isik.emailclient.model.HostAccount;
import java.io.File;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

//@author berk
public class MailSenderService {

    public void sendEmail(String toAddress, String subject, String messageContent, File attachment) throws Exception {
        
        // host settings
        ConfigService configService = new ConfigService();
        HostAccount account = configService.loadAccount();
        
        if (account == null) {
            throw new Exception("No account settings found! Please save settings first.");
        }

        // SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", account.getOutgoingHost());
        props.put("mail.smtp.port", String.valueOf(account.getOutgoingPort()));
        
        // SSL (for 465 port)
        if (account.getOutgoingPort() == 465) {
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        // log in
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account.getEmailAddress(), account.getPassword());
            }
        });

        // create message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(account.getEmailAddress()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
        message.setSubject(subject);

        // Multipart
        Multipart multipart = new MimeMultipart();

        // text part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(messageContent);
        multipart.addBodyPart(textPart);

        // attachment
        if (attachment != null && attachment.exists()) {
            MimeBodyPart attachPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment);
            attachPart.setDataHandler(new DataHandler(source));
            attachPart.setFileName(attachment.getName());
            multipart.addBodyPart(attachPart);
        }

        message.setContent(multipart);
        
        // send
        Transport.send(message);
    }
}
