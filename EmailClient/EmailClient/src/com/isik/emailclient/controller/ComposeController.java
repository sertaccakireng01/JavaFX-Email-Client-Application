package com.isik.emailclient.controller;

import com.isik.emailclient.service.MailSenderService;
import com.isik.emailclient.util.AlertHelper;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

//@author sertac
public class ComposeController implements Initializable {

    @FXML private TextField txtTo;
    @FXML private TextField txtSubject;
    @FXML private TextArea txtMessage;
    @FXML private Label lblAttachment;

    private File selectedAttachment = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    //* Autofills to and subject when Reply button pressed
    
    public void initReplyData(String to, String subject) {
        txtTo.setText(to);
        txtSubject.setText("Re: " + subject);
        txtMessage.requestFocus(); // cursor focus to message box
    }

    @FXML
    private void handleAttachAction() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Attachment");
            Stage stage = (Stage) txtTo.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            
            if (file != null) {
                selectedAttachment = file;
                lblAttachment.setText(file.getName());
            }
        } catch (Exception e) {
            AlertHelper.showErrorMessage("Attachment Error", "Could not attach file: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendAction() {
        String to = txtTo.getText();
        String subject = txtSubject.getText();
        String body = txtMessage.getText();
        
        if (to.isEmpty()) {
            AlertHelper.showErrorMessage("Validation Error", "Recipient (To) is required.");
            return;
        }

        // backend
        Task<Void> sendTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                MailSenderService sender = new MailSenderService();
                sender.sendEmail(to, subject, body, selectedAttachment);
                return null;
            }
        };

        sendTask.setOnSucceeded(e -> {
            AlertHelper.showInfoMessage("Success", "Email sent successfully!");
            Stage stage = (Stage) txtTo.getScene().getWindow();
            stage.close();
        });

        sendTask.setOnFailed(e -> {
            Throwable error = sendTask.getException();
            error.printStackTrace();
            AlertHelper.showErrorMessage("Sending Failed", "Could not send email:\n" + error.getMessage());
        });
        
        new Thread(sendTask).start();
    }
}
