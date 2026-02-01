package com.isik.emailclient.controller;

import com.isik.emailclient.model.HostAccount;
import com.isik.emailclient.service.ConfigService;
import com.isik.emailclient.util.AlertHelper;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

//@author sertac
public class SettingsController implements Initializable {

    @FXML private RadioButton radioImap;
    @FXML private RadioButton radioPop3;
    @FXML private ToggleGroup protocolGroup;
    
    @FXML private TextField txtIncomingPort;
    @FXML private TextField txtOutgoingPort;
    
    @FXML private TextField txtIncomingHost;
    @FXML private TextField txtOutgoingHost;
    
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    private ConfigService configService = new ConfigService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadCurrentSettings();
        } catch (Exception e) {
            AlertHelper.showErrorMessage("Load Error", "Could not load existing settings.");
        }
    }

    private void loadCurrentSettings() throws Exception {
        HostAccount account = configService.loadAccount();
        if (account != null) {
            txtEmail.setText(account.getEmailAddress());
            txtPassword.setText(account.getPassword());
            txtIncomingHost.setText(account.getIncomingHost());
            txtOutgoingHost.setText(account.getOutgoingHost());
            txtIncomingPort.setText(String.valueOf(account.getIncomingPort()));
            txtOutgoingPort.setText(String.valueOf(account.getOutgoingPort()));
            
            if ("pop3".equalsIgnoreCase(account.getProtocol())) {
                radioPop3.setSelected(true);
            } else {
                radioImap.setSelected(true);
            }
        } else {
            // Default values for convenience (e.g., Gmail)
            txtIncomingPort.setText("993");
            txtOutgoingPort.setText("465");
            radioImap.setSelected(true);
        }
    }

    @FXML
    private void handleSaveAction() {
        try {
            // Validation (Boş alan kontrolü)
            if (txtEmail.getText().isEmpty() || txtPassword.getText().isEmpty()) {
                AlertHelper.showErrorMessage("Validation Error", "Email and Password are required!");
                return;
            }

            // Duplicate Control
            HostAccount existingAccount = configService.loadAccount();
            if (existingAccount != null && existingAccount.getEmailAddress().equalsIgnoreCase(txtEmail.getText())) {
                AlertHelper.showErrorMessage("Warning", "Host is already entered!");
                return;
            }
            

            // create model
            String protocol = radioPop3.isSelected() ? "pop3" : "imap";
            
            HostAccount account = new HostAccount(
                txtEmail.getText(),
                txtPassword.getText(),
                protocol,
                txtIncomingHost.getText(),
                Integer.parseInt(txtIncomingPort.getText()),
                txtOutgoingHost.getText(),
                Integer.parseInt(txtOutgoingPort.getText())
            );

            // save
            configService.saveAccount(account);
            
            AlertHelper.showInfoMessage("Success", "Settings saved successfully!");
            
            // close window
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            AlertHelper.showErrorMessage("Input Error", "Ports must be numbers.");
        } catch (Exception e) {
            AlertHelper.showErrorMessage("Save Error", "Could not save settings:\n" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClearAction() {
        // Form alanlarını temizle
        txtEmail.clear();
        txtPassword.clear();
        txtIncomingHost.clear();
        txtOutgoingHost.clear();
        txtIncomingPort.clear();
        txtOutgoingPort.clear();
        radioImap.setSelected(true);
    }

    @FXML
    private void handleDeleteAction() {
        try {
            configService.deleteAccount();
            
            // clear the form
            handleClearAction();
            
            AlertHelper.showInfoMessage("Deleted", "Host settings deleted successfully.");
            
            // close window
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.close();
            
        } catch (Exception e) {
            AlertHelper.showErrorMessage("Error", "Could not delete settings.");
        }
    }
}
