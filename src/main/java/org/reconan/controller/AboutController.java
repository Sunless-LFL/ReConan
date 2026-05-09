package org.reconan.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import org.reconan.ui.ViewLoader;

/**
 * Controller for the About screen.
 */
public class AboutController {

    @FXML
    private StackPane zoomOverlay;

    @FXML
    private ImageView zoomedImage;

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
