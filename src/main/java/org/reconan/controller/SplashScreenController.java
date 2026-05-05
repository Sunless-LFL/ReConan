package org.reconan.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.reconan.ui.ViewLoader;

/**
 * Controller for the SplashScreen view.
 * Handles startup animations and transitions to the main application view.
 */
public class SplashScreenController {

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView logoView;

    @FXML
    public void initialize() {
        // Initial state
        logoView.setOpacity(0);
        logoView.setScaleX(0.5);
        logoView.setScaleY(0.5);

        // Fade-in animation
        FadeTransition fade = new FadeTransition(Duration.seconds(2), logoView);
        fade.setFromValue(0);
        fade.setToValue(1);

        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.seconds(2), logoView);
        scale.setToX(1);
        scale.setToY(1);

        // Combine and play animations
        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.setOnFinished(event -> {
            // Transition to main menu
            ViewLoader.loadView("/fxml/main_menu.fxml", "ReConan - Main Menu");
        });
        animation.play();
    }
}
