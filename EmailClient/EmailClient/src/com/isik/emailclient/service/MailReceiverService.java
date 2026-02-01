package com.isik.emailclient.service;

import com.isik.emailclient.model.EmailMessage;
import com.isik.emailclient.model.HostAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

//@author berk
public class MailReceiverService {

    public List<EmailMessage> fetchEmails(String folderName) throws Exception {
        List<EmailMessage> emailList = new ArrayList<>();

        // load host settings
        ConfigService configService = new ConfigService();
        HostAccount account = configService.loadAccount();

        if (account == null) {
            throw new Exception("Please save host settings first!");
        }

        // connection
        Properties props = new Properties();
        String protocol = account.getProtocol(); 
        props.put("mail.store.protocol", protocol);
        props.put("mail." + protocol + ".host", account.getIncomingHost());
        props.put("mail." + protocol + ".port", String.valueOf(account.getIncomingPort()));
        props.put("mail." + protocol + ".ssl.enable", "true");

        // server connection
        Session session = Session.getInstance(props);
        Store store = session.getStore(protocol);
        store.connect(account.getIncomingHost(), account.getEmailAddress(), account.getPassword());
        
        // folder
        Folder folder = store.getFolder(folderName);
        
        if (!folder.exists()) {
            throw new Exception("Klasör bulunamadı: " + folderName);
        }

        folder.open(Folder.READ_ONLY);

        // get messages
        int totalMessages = folder.getMessageCount();
        if (totalMessages > 0) {
            int start = Math.max(1, totalMessages - 20); 
            Message[] messages = folder.getMessages(start, totalMessages);

            // messages from recent to past
            for (int i = messages.length - 1; i >= 0; i--) {
                Message msg = messages[i];
                try {
                    String sender = (msg.getFrom() != null && msg.getFrom().length > 0) ? msg.getFrom()[0].toString() : "Unknown";
                    String subject = (msg.getSubject() != null) ? msg.getSubject() : "(No Subject)";
                    boolean hasAttachment = checkAttachment(msg);
                    
                    Object content = "Loading...";
                    try { content = getTextFromMessage(msg); } catch(Exception e) { content = "Error reading content"; }

                    emailList.add(new EmailMessage(sender, subject, msg.getSentDate(), hasAttachment, content));
                } catch (Exception e) {
                    System.err.println("Mesaj okunurken hata: " + e.getMessage());
                }
            }
        }

        folder.close(false);
        store.close();

        return emailList;
    }
    
    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) return message.getContent().toString();
        else if (message.isMimeType("multipart/*")) return getTextFromMimeMultipart((Multipart) message.getContent());
        return "";
    }

    private String getTextFromMimeMultipart(Multipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            MimeBodyPart bodyPart = (MimeBodyPart) mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
                break; 
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append(html.replaceAll("\\<.*?\\>", "")); // HTML etiketlerini temizle
            } else if (bodyPart.getContent() instanceof Multipart){
                result.append(getTextFromMimeMultipart((Multipart)bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private boolean checkAttachment(Message msg) throws Exception {
        if (msg.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) msg.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                MimeBodyPart part = (MimeBodyPart) mp.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) return true;
            }
        }
        return false;
    }
}
