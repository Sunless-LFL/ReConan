package org.reconan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.reconan.ui.ViewLoader;
import org.reconan.util.ConsoleBanner;
import org.reconan.database.DatabaseConnection;
import org.reconan.database.DatabaseManager;

import java.sql.SQLException;

/**
 * Main entry point of the application.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // Initialize ViewLoader
        ViewLoader.setPrimaryStage(stage);
        
        // Configure stage properties
        setupStage(stage);

        // Print console banner
        ConsoleBanner.print();

        // Verify database connection
        DatabaseConnection.checkConnection();

        // Initialize Database Schema
        try {
            DatabaseManager.getInstance().initialize();
        } catch (SQLException e) {
            System.err.println("SQL Server: Failed to initialize database schema: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Initialization Error");
            alert.setHeaderText("Could not initialize the database schema");
            alert.setContentText("Please check your database connection and permissions.\n\nError: " + e.getMessage());
            alert.showAndWait();
            Platform.exit();
            return;
        }

        // Load splash screen view
        ViewLoader.loadView("/fxml/splash_screen.fxml", "ReConan - Loading");
    }

    // Set window title and icon
    private void setupStage(Stage stage) {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
    }

    // Launch JavaFX application
    public static void main(String[] args) {
        launch();
    }
}