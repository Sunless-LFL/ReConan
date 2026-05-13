package org.reconan.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.reconan.ui.ViewLoader;

/**
 * Controller for the About screen.
 */
public class AboutController {

    @FXML
    private StackPane zoomOverlay;

    @FXML
    private ImageView zoomedImage;

    private double lastMouseX;
    private double lastMouseY;

    @FXML
    public void initialize() {
        // Setup Zoom (Mouse Wheel)
        zoomOverlay.addEventHandler(ScrollEvent.SCROLL, event -> {
            if (zoomOverlay.isVisible()) {
                double delta = event.getDeltaY();
                double zoomFactor = 1.05;
                if (delta < 0) zoomFactor = 2.0 - zoomFactor;

                double newScaleX = zoomedImage.getScaleX() * zoomFactor;
                double newScaleY = zoomedImage.getScaleY() * zoomFactor;

                // Limit zoom
                if (newScaleX > 0.5 && newScaleX < 10) {
                    zoomedImage.setScaleX(newScaleX);
                    zoomedImage.setScaleY(newScaleY);
                }
                event.consume();
            }
        });

        // Setup Panning (Mouse Drag)
        zoomedImage.setOnMousePressed(event -> {
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
            event.consume();
        });

        zoomedImage.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - lastMouseX;
            double deltaY = event.getSceneY() - lastMouseY;

            zoomedImage.setTranslateX(zoomedImage.getTranslateX() + deltaX);
            zoomedImage.setTranslateY(zoomedImage.getTranslateY() + deltaY);

            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
            event.consume();
        });
    }

    @FXML
    private void handleBack() {
        System.out.println("Terminal: Returning to Main Menu...");
        ViewLoader.loadView("/fxml/main_menu.fxml", "ReConan - Main Menu");
    }

    @FXML
    private void handleZoomImage(MouseEvent event) {
        System.out.println("Terminal: Zooming image...");
        ImageView clickedImage = (ImageView) event.getSource();
        zoomedImage.setImage(clickedImage.getImage());
        
        // Reset state
        zoomedImage.setScaleX(1.0);
        zoomedImage.setScaleY(1.0);
        zoomedImage.setTranslateX(0);
        zoomedImage.setTranslateY(0);
        
        zoomOverlay.setVisible(true);
    }

    @FXML
    private void handleCloseZoom() {
        System.out.println("Terminal: Closing zoomed image...");
        zoomOverlay.setVisible(false);
    }

    @FXML
    private void handleGithubLink() {
        openUrl("https://github.com/SBAI-Youness/ReConan");
    }

    @FXML
    private void handleWebsiteLink() {
        openUrl("https://sbai-youness.github.io/ReConan/");
    }

    private void openUrl(String url) {
        System.out.println("Terminal: Opening URL: " + url);
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
