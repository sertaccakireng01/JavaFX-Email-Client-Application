package com.isik.emailclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//@author berk & sertac
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/isik/emailclient/view/MainWindow.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            
            stage.setTitle("Işık Email Client");
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Application failed to start.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
