package org.reconan.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.reconan.graph.GraphManager;
import org.reconan.model.Entity;
import org.reconan.model.EntityType;
import org.reconan.model.Investigation;
import org.reconan.ui.ViewLoader;

import java.io.File;
import java.util.Optional;

/**
 * Controller for the Investigation Workspace view.
 */
public class InvestigationWorkspaceController {

    @FXML
    private Label investigationNameLabel;

    @FXML
    private ListView<String> entityPaletteList;

    @FXML
    private StackPane graphPane;

    @FXML
    private ListView<String> propertiesList;

    private Investigation currentInvestigation;
    private GraphManager graphManager;
    private int entityIdCounter = 1; // Temporary ID counter for unsaved entities

    @FXML
    public void initialize() {
        System.out.println("Terminal: Initializing Investigation Workspace view...");

        // Initialize palettes
        for (EntityType type : EntityType.values()) {
            entityPaletteList.getItems().add(type.getLabel());
        }

        // Initialize Graph Manager
        graphManager = new GraphManager();
        graphManager.initializeView(graphPane);
        graphManager.setSelectionListener(this::handleEntitySelection);

        // Setup Drag and Drop
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        // Source: Palette
        entityPaletteList.setOnDragDetected(event -> {
            String selected = entityPaletteList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Dragboard db = entityPaletteList.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(selected);
                db.setContent(content);
                event.consume();
            }
        });

        // Target: Graph Pane
        graphPane.setOnDragOver(event -> {
            if (event.getGestureSource() != graphPane && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        graphPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                createNewEntityAt(db.getString(), event.getX(), event.getY());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void createNewEntityAt(String typeLabel, double x, double y) {
        EntityType type = findTypeByLabel(typeLabel);
        if (type == null)
            return;

        String value = "";
        if (type == EntityType.IMAGE) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image for Entity");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp"));
            File selectedFile = fileChooser.showOpenDialog(graphPane.getScene().getWindow());
            if (selectedFile != null) {
                value = selectedFile.getAbsolutePath();
            } else {
                return; // Cancelled
            }
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New " + type.getLabel());
            dialog.setHeaderText("Enter value for " + type.getLabel());
            dialog.setContentText("Value:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                value = result.get();
            } else {
                return; // Cancelled or empty
            }
        }

        Entity entity = new Entity(type, value);
        entity.setId(entityIdCounter++);
        if (currentInvestigation != null) {
            entity.setInvestigationId(currentInvestigation.getId());
        }

        graphManager.addEntity(entity, x, y);
    }

    private EntityType findTypeByLabel(String label) {
        for (EntityType type : EntityType.values()) {
            if (type.getLabel().equals(label))
                return type;
        }
        return null;
    }

    private void handleEntitySelection(Entity entity) {
        System.out.println("Terminal: Entity selected: " + entity.getValue());
        propertiesList.getItems().clear();
        propertiesList.getItems().add("Type: " + entity.getType().getLabel());
        propertiesList.getItems().add("Value: " + entity.getValue());
        propertiesList.getItems().add("ID: " + entity.getId());

        entity.getProperties().forEach((k, v) -> {
            propertiesList.getItems().add(k + ": " + v);
        });
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

    private boolean autoLayoutEnabled = true;

    @FXML
    private void handleToggleLayout() {
        autoLayoutEnabled = !autoLayoutEnabled;
        System.out.println("Terminal: Automatic Layout " + (autoLayoutEnabled ? "Enabled" : "Disabled"));
        graphManager.setAutomaticLayout(autoLayoutEnabled);
    }

    @FXML
    private void handleBackToMenu() {
        System.out.println("Terminal: Returning to Main Menu from Workspace...");
        ViewLoader.loadView("/fxml/main_menu.fxml", "ReConan - Main Menu");
    }
}
