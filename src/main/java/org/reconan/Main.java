package org.reconan;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.reconan.ui.ViewLoader;
import org.reconan.util.ConsoleBanner;
import org.reconan.database.DatabaseConnection;

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