package org.reconan.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

/**
 * Utility for loading FXML views and managing application scenes.
 */
public class ViewLoader {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Loads a view from FXML and sets it as the current scene.
     * @param fxmlPath Path to the FXML file relative to resources.
     * @param title Title for the stage.
     */
    public static void loadView(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(ViewLoader.class.getResource(fxmlPath)));
            Scene scene = new Scene(root);
            
            // Load global CSS
            String cssPath = "/css/styles.css";
            if (ViewLoader.class.getResource(cssPath) != null) {
                scene.getStylesheets().add(Objects.requireNonNull(ViewLoader.class.getResource(cssPath)).toExternalForm());
            }

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
