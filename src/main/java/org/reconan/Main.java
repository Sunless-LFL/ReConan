package org.reconan;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.reconan.ui.Banner;
import org.reconan.ui.SplashScreen;
import org.reconan.database.DatabaseConnection;

/**
 * Main entry point of the application.
 */
public class Main extends Application {
    @Override
    public void start(Stage stage) {
        // Configure stage properties
        setupStage(stage);

        // Print console banner
        Banner.print();

        // Verify database connection
        DatabaseConnection.checkConnection();

        // Display splash screen
        SplashScreen.show(stage);
    }

    // Set window title and icon
    private void setupStage(Stage stage) {
        stage.setTitle("ReConan");
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/icon.png")));
    }

    // Launch JavaFX application
    public static void main(String[] args) {
        launch();
    }
}