package org.reconan.controller;

import javafx.fxml.FXML;
import org.reconan.ui.ViewLoader;

/**
 * Controller for the Main Menu view.
 */
public class MainMenuController {

    @FXML
    private void handleCreateInvestigation() {
        System.out.println("Create Investigation clicked");
        // Logic to create investigation
    }

    @FXML
    private void handleOpenInvestigation() {
        System.out.println("Open Investigation clicked");
        // Logic to open investigation
    }

    @FXML
    private void handleAbout() {
        ViewLoader.loadView("/fxml/about.fxml", "ReConan - About");
    }
}
