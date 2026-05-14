package org.reconan.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Displays a splash screen with logo animation.
 */
public class SplashScreen {
    // Show splash screen with fade and scale animation
    public static void show(Stage stage) {
        // Load and configure logo
        ImageView logo = new ImageView(
                new Image(SplashScreen.class.getResourceAsStream("/icon.png"))
        );
        logo.setOpacity(0);
        logo.setScaleX(0.5);
        logo.setScaleY(0.5);

        // Fade-in animation
        FadeTransition fade = new FadeTransition(Duration.seconds(2), logo);
        fade.setFromValue(0);
        fade.setToValue(1);

        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.seconds(2), logo);
        scale.setToX(1);
        scale.setToY(1);

        // Combine animations
        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.play();

        // Set scene and display stage
        StackPane root = new StackPane(logo);
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }
}