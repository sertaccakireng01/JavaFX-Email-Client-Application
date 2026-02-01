package com.isik.emailclient.model;

import java.util.Date;
import javafx.beans.property.*;

//@author berk & sertac
public class EmailMessage {
    
    private final StringProperty sender;
    private final StringProperty subject;
    private final ObjectProperty<Date> date;
    private final BooleanProperty hasAttachment;
    private final ObjectProperty<Object> content; // Holds message body (String or Multipart)

    // Constructor
    public EmailMessage(String sender, String subject, Date date, boolean hasAttachment, Object content) {
        // Defensive: handle null values to avoid crashes
        this.sender = new SimpleStringProperty(sender != null ? sender : "");
        this.subject = new SimpleStringProperty(subject != null ? subject : "(No Subject)");
        this.date = new SimpleObjectProperty<>(date != null ? date : new Date());
        this.hasAttachment = new SimpleBooleanProperty(hasAttachment);
        this.content = new SimpleObjectProperty<>(content != null ? content : "");
    }

    // getters for properties
    public StringProperty senderProperty() { return sender; }
    public StringProperty subjectProperty() { return subject; }
    public ObjectProperty<Date> dateProperty() { return date; }
    public BooleanProperty hasAttachmentProperty() { return hasAttachment; }

    // standard getters
    public Object getContent() { return content.get(); }
    public String getSender() { return sender.get(); }
    public String getSubject() { return subject.get(); }
    public Date getDate() { return date.get(); }
    public boolean hasAttachment() { return hasAttachment.get(); }
    
    // for debugging purposes
    @Override
    public String toString() {
        return "From: " + sender.get() + ", Subj: " + subject.get();
    }
}
