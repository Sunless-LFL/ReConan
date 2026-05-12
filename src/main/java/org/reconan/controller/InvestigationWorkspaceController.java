package org.reconan.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import org.reconan.model.EntityType;
import org.reconan.model.Investigation;
import org.reconan.ui.ViewLoader;

/**
 * Controller for the Investigation Workspace view.
 */
public class InvestigationWorkspaceController {

    @FXML
    private Label investigationNameLabel;

    @FXML
    private ListView<String> entityPaletteList;

    @FXML
    private Pane graphPane;

    @FXML
    private ListView<String> propertiesList;

    private Investigation currentInvestigation;

    @FXML
    public void initialize() {
        System.out.println("Terminal: Initializing Investigation Workspace view...");
        // Initialize palettes
        for (EntityType type : EntityType.values()) {
            entityPaletteList.getItems().add(type.getLabel());
        }
    }

    /**
     * Initializes the workspace with the selected investigation context.
     *
     * @param investigation The investigation context.
     */
    public void initData(Investigation investigation) {
        this.currentInvestigation = investigation;
        if (investigation != null) {
            System.out.println("Terminal: Loading data for investigation: " + investigation.getName());
            investigationNameLabel.setText("Investigation: " + investigation.getName());
            // Here we would eventually load the graph data for this investigation
        }
    }

    @FXML
    private void handleBackToMenu() {
        System.out.println("Terminal: Returning to Main Menu from Workspace...");
        ViewLoader.loadView("/fxml/main_menu.fxml", "ReConan - Main Menu");
    }
}
