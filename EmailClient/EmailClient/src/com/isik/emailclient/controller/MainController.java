package com.isik.emailclient.controller;

import com.isik.emailclient.model.EmailMessage;
import com.isik.emailclient.model.HostAccount;
import com.isik.emailclient.service.ConfigService;
import com.isik.emailclient.service.MailReceiverService;
import com.isik.emailclient.util.AlertHelper;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

//@author sertac
public class MainController implements Initializable {
    
    @FXML private TreeView<String> folderTree;           // left menu
    @FXML private TableView<EmailMessage> emailTable;    // middle table
    
    @FXML private TableColumn<EmailMessage, String> colSender;
    @FXML private TableColumn<EmailMessage, String> colSubject;
    @FXML private TableColumn<EmailMessage, java.util.Date> colDate;
    @FXML private TableColumn<EmailMessage, Boolean> colAttachment;

    @FXML private TextArea messageContentArea;           // bottom area
    @FXML private Label lblStatus;                       // left bottom status
    @FXML private ComboBox<String> cmbHosts;             // right top Host selection


    private ObservableList<EmailMessage> emailList = FXCollections.observableArrayList();
    private String currentFolder = null; // null at default (because of refresh alert)

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupTable();
            setupFolderTree();
            updateHostInfo();
        } catch (Exception e) {
            AlertHelper.showErrorMessage("Init Error", "Arayüz yüklenirken hata: " + e.getMessage());
        }
    }

    // table
    private void setupTable() {
        colSender.setCellValueFactory(cell -> cell.getValue().senderProperty());
        colSubject.setCellValueFactory(cell -> cell.getValue().subjectProperty());
        colDate.setCellValueFactory(cell -> cell.getValue().dateProperty());
        colAttachment.setCellValueFactory(cell -> cell.getValue().hasAttachmentProperty());

        // date format (dd.MM.yyyy HH:mm)
        colDate.setCellFactory(column -> {
            return new TableCell<EmailMessage, java.util.Date>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                @Override
                protected void updateItem(java.util.Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(format.format(item));
                    }
                }
            };
        });

        emailTable.setItems(emailList);

        // When you select from the table, show the content below
        emailTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    messageContentArea.setText(newVal.getContent().toString());
                } catch (Exception e) {
                    messageContentArea.setText("İçerik yüklenemedi.");
                }
            }
        });
    }

    // folder tree (GMAIL compatible)
    private void setupFolderTree() {
        try {
            TreeItem<String> root = new TreeItem<>("Root");
            root.setExpanded(true);
            
            // Gmail folder names
            TreeItem<String> inboxItem = new TreeItem<>("INBOX");
            TreeItem<String> sentItem = new TreeItem<>("[Gmail]/Gönderilmiş Postalar");
            TreeItem<String> trashItem = new TreeItem<>("[Gmail]/Çöp kutusu");

            root.getChildren().addAll(inboxItem, sentItem, trashItem);
            
            folderTree.setRoot(root);
            folderTree.setShowRoot(false);
            
            folderTree.getSelectionModel().clearSelection();
            
            // folder tree listener
            folderTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    String selectedFolder = newVal.getValue();
                    System.out.println("Klasör değişti: " + selectedFolder);
                    
                    this.currentFolder = selectedFolder;
                    handleRefreshAction();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Folder Tree Error: " + e.getMessage());
        }
    }

    // host info
    private void updateHostInfo() {
        try {
            ConfigService config = new ConfigService();
            HostAccount account = config.loadAccount();
            
            if (account != null) {
                lblStatus.setText("Current host: " + account.getIncomingHost() + " (" + account.getEmailAddress() + ")");
                cmbHosts.getItems().clear();
                cmbHosts.getItems().add(account.getEmailAddress());
                cmbHosts.getSelectionModel().selectFirst();
            } else {
                lblStatus.setText("Current host: (Not connected)");
                cmbHosts.getItems().clear();
                cmbHosts.setPromptText("Select your host");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    // buttons
    @FXML
    private void handleRefreshAction() {
        // security control
        if (currentFolder == null) {
            AlertHelper.showErrorMessage("Warning", "Please select a folder from the left menu first!");
            return;
        }

        // feedback notification
        AlertHelper.showInfoMessage("Refreshing", "Fetching emails from " + currentFolder + "...\n(Click OK to start)");

        // backend
        Task<List<EmailMessage>> task = new Task<List<EmailMessage>>() {
            @Override
            protected List<EmailMessage> call() throws Exception {
                MailReceiverService receiver = new MailReceiverService();
                return receiver.fetchEmails(currentFolder);
            }
        };
        
        task.setOnSucceeded(e -> {
            emailList.clear();
            List<EmailMessage> newEmails = task.getValue();
            emailList.addAll(newEmails);
            
            if (newEmails.isEmpty()) {
                AlertHelper.showInfoMessage("Completed", "No emails found in " + currentFolder);
            } else {
                System.out.println("Refresh tamamlandı: " + newEmails.size() + " mail yüklendi.");
            }
        });

        task.setOnFailed(e -> {
            Throwable error = task.getException();
            AlertHelper.showErrorMessage("Connection Error", error.getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleReplyAction() {
        EmailMessage selectedMessage = emailTable.getSelectionModel().getSelectedItem();
        if (selectedMessage == null) {
            AlertHelper.showErrorMessage("Selection Error", "Please select an email to reply.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/isik/emailclient/view/ComposeWindow.fxml"));
            Parent root = loader.load();
            
            ComposeController controller = loader.getController();
            controller.initReplyData(selectedMessage.getSender(), selectedMessage.getSubject());
            
            Stage stage = new Stage();
            stage.setTitle("Reply");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleComposeAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/isik/emailclient/view/ComposeWindow.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Compose New Email");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewHostAction() {
        try {
             FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/isik/emailclient/view/HostSettings.fxml"));
             Parent root = loader.load();
             Stage stage = new Stage();
             stage.setTitle("Host Settings");
             stage.setScene(new Scene(root));
             stage.showAndWait();
             updateHostInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDeleteHostAction() {
        try {
            ConfigService config = new ConfigService();
            HostAccount account = config.loadAccount();
            
            if (account == null) {
                AlertHelper.showErrorMessage("Error", "No host selected or settings file is missing.");
                return;
            }
            
            boolean confirmed = AlertHelper.showConfirmationDialog(
                "Delete Host", 
                "Are you sure about deleting Host: " + account.getEmailAddress() + "?"
            );
            
            if (confirmed) {
                config.deleteAccount();
                
                // clear
                updateHostInfo(); // if already delted -> "(Not connected)" 
                emailList.clear();
                messageContentArea.clear();
                folderTree.getSelectionModel().clearSelection();
                currentFolder = null;
                
                AlertHelper.showInfoMessage("Success", "Host deleted successfully.");
            }
            
        } catch (Exception e) {
            AlertHelper.showErrorMessage("Delete Error", "Could not delete host: " + e.getMessage());
        }
    }
}
