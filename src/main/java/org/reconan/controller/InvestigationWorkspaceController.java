package org.reconan.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
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
    private VBox detailsContainer;

    @FXML
    private VBox actionPanel;

    private Entity selectedEntity;

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
        
        // Hide action panel initially
        actionPanel.setVisible(false);
        actionPanel.setManaged(false);
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
            styleDialog(dialog);

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
        this.selectedEntity = entity;
        System.out.println("Terminal: Entity selected: " + entity.getValue());
        
        detailsContainer.getChildren().clear();
        
        // Show action panel
        actionPanel.setVisible(true);
        actionPanel.setManaged(true);
        
        addDetailLabel("Type", entity.getType().getLabel());
        addDetailLabel("Value", entity.getValue());
        addDetailLabel("ID", String.valueOf(entity.getId()));
        
        entity.getProperties().forEach(this::addDetailLabel);
    }

    private void addDetailLabel(String key, String value) {
        Label keyLabel = new Label(key + ":");
        keyLabel.getStyleClass().add("property-key");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("property-value");
        valueLabel.setWrapText(true);
        
        VBox pair = new VBox(keyLabel, valueLabel);
        pair.setSpacing(2);
        detailsContainer.getChildren().add(pair);
    }

    @FXML
    private void handleUpdateEntity() {
        if (selectedEntity == null) return;

        TextInputDialog dialog = new TextInputDialog(selectedEntity.getValue());
        dialog.setTitle("Update Entity");
        dialog.setHeaderText("Update value for " + selectedEntity.getType().getLabel());
        dialog.setContentText("New Value:");
        styleDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newValue -> {
            if (!newValue.trim().isEmpty()) {
                selectedEntity.setValue(newValue);
                graphManager.updateEntity(selectedEntity);
                handleEntitySelection(selectedEntity); // Refresh sidebar
            }
        });
    }

    @FXML
    private void handleDeleteEntity() {
        if (selectedEntity == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Entity");
        alert.setHeaderText("Are you sure you want to delete this entity?");
        alert.setContentText("This action cannot be undone.");
        styleDialog(alert);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            graphManager.removeEntity(selectedEntity);
            detailsContainer.getChildren().clear();
            actionPanel.setVisible(false);
            actionPanel.setManaged(false);
            selectedEntity = null;
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

    private void styleDialog(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Terminal: Could not load styles.css for dialog.");
        }
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        
        try {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            System.err.println("Terminal: Could not load icon for dialog.");
        }
    }
}
