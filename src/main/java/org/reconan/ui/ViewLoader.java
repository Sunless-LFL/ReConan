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
        loadViewWithController(fxmlPath, title);
    }

    /**
     * Loads a view from FXML, sets it as the current scene, and returns the controller.
     * @param fxmlPath Path to the FXML file relative to resources.
     * @param title Title for the stage.
     * @param <T> The type of the controller.
     * @return The controller instance.
     */
    public static <T> T loadViewWithController(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(ViewLoader.class.getResource(fxmlPath)));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Load global CSS
            String cssPath = "/css/styles.css";
            if (ViewLoader.class.getResource(cssPath) != null) {
                scene.getStylesheets().add(Objects.requireNonNull(ViewLoader.class.getResource(cssPath)).toExternalForm());
            }

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen(); // Always center the window
            primaryStage.show();
            
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
}
