package org.reconan.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
import org.reconan.graph.GraphManager;
import org.reconan.model.Entity;
import org.reconan.model.EntityType;
import org.reconan.model.Investigation;
import org.reconan.model.Relationship;
import org.reconan.repository.EntityRepository;
import org.reconan.repository.RelationshipRepository;
import org.reconan.repository.InvestigationRepository;
import org.reconan.ui.ViewLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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

    @FXML
    private StackPane zoomOverlay;

    @FXML
    private ImageView zoomedImage;

    private double lastMouseX;
    private double lastMouseY;

    private Entity selectedEntity;
    private Entity sourceEntity;
    private boolean linkingMode = false;

    private Investigation currentInvestigation;
    private GraphManager graphManager;
    private int entityIdCounter = 1;

    private final EntityRepository entityRepository = new EntityRepository();
    private final RelationshipRepository relationshipRepository = new RelationshipRepository();
    private final InvestigationRepository investigationRepository = new InvestigationRepository();

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
        graphManager.setEdgeSelectionListener(this::handleRelationshipRename);

        // Setup Drag and Drop
        setupDragAndDrop();

        // Hide action panel initially
        actionPanel.setVisible(false);
        actionPanel.setManaged(false);

        setupZoomAndPanning();
    }

    private void setupZoomAndPanning() {
        // Mouse Wheel Zoom
        zoomOverlay.addEventHandler(ScrollEvent.SCROLL, event -> {
            if (zoomOverlay.isVisible()) {
                double delta = event.getDeltaY();
                double zoomFactor = 1.05;
                if (delta < 0)
                    zoomFactor = 2.0 - zoomFactor;

                double newScaleX = zoomedImage.getScaleX() * zoomFactor;
                double newScaleY = zoomedImage.getScaleY() * zoomFactor;

                if (newScaleX > 0.5 && newScaleX < 10) {
                    zoomedImage.setScaleX(newScaleX);
                    zoomedImage.setScaleY(newScaleY);
                }
                event.consume();
            }
        });

        // Mouse Drag Panning
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

    private void handleRelationshipRename(Relationship relationship) {
        TextInputDialog dialog = new TextInputDialog(relationship.getLabel());
        dialog.setTitle("Rename Relationship");
        dialog.setHeaderText("Update label for this connection");
        dialog.setContentText("New Label:");
        styleDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newLabel -> {
            if (!newLabel.trim().isEmpty()) {
                relationship.setLabel(newLabel.toUpperCase());
                graphManager.updateView();
                System.out.println("Terminal: Relationship renamed to: " + newLabel);
            }
        });
    }

    private void handleEntitySelection(Entity entity) {
        if (linkingMode && sourceEntity != null && entity != sourceEntity) {
            completeLink(entity);
            return;
        }

        this.selectedEntity = entity;
        System.out.println("Terminal: Entity selected: " + entity.getValue());

        detailsContainer.getChildren().clear();

        // Show action panel
        actionPanel.setVisible(true);
        actionPanel.setManaged(true);

        addDetailLabel("Type", entity.getType().getLabel());
        addDetailLabel("Value", entity.getValue());
        addDetailLabel("ID", String.valueOf(entity.getId()));

        if (entity.getType() == EntityType.IMAGE) {
            try {
                File file = new File(entity.getValue());
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    ImageView imageView = new ImageView(img);
                    imageView.setFitWidth(180);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle(
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0); -fx-cursor: hand;");

                    // Click to Zoom
                    imageView.setOnMouseClicked(event -> {
                        zoomedImage.setImage(img);
                        zoomedImage.setScaleX(1.0);
                        zoomedImage.setScaleY(1.0);
                        zoomedImage.setTranslateX(0);
                        zoomedImage.setTranslateY(0);
                        zoomOverlay.setVisible(true);
                    });

                    VBox imgContainer = new VBox(new Label("Preview (Click to Zoom):"), imageView);
                    imgContainer.setSpacing(5);
                    imgContainer.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
                    detailsContainer.getChildren().add(imgContainer);
                }
            } catch (Exception e) {
                System.err.println("Terminal: Error loading entity image: " + e.getMessage());
            }
        }

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
    private void handlePrepareLink() {
        if (selectedEntity == null)
            return;

        this.linkingMode = true;
        this.sourceEntity = selectedEntity;

        System.out.println("Terminal: Linking mode active. Select target node.");

        // Visual feedback
        Label tip = new Label("Select target node...");
        tip.setStyle("-fx-text-fill: #ffeb3b; -fx-font-style: italic;");
        detailsContainer.getChildren().add(0, tip);
    }

    private void completeLink(Entity targetEntity) {
        TextInputDialog dialog = new TextInputDialog("RELATED_TO");
        dialog.setTitle("Create Connection");
        dialog.setHeaderText("Link " + sourceEntity.getValue() + " -> " + targetEntity.getValue());
        dialog.setContentText("Relationship Label:");
        styleDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(label -> {
            String finalLabel = label.trim().isEmpty() ? "RELATED_TO" : label.toUpperCase();
            Relationship rel = new Relationship(sourceEntity.getId(), targetEntity.getId(), finalLabel);
            if (currentInvestigation != null) {
                rel.setInvestigationId(currentInvestigation.getId());
            }
            graphManager.addRelationship(rel);
            System.out.println("Terminal: Relationship created: " + finalLabel);
        });

        // Reset mode
        linkingMode = false;
        sourceEntity = null;
        handleEntitySelection(targetEntity); // Refresh sidebar
    }

    @FXML
    private void handleUpdateEntity() {
        if (selectedEntity == null)
            return;

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
        if (selectedEntity == null)
            return;

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

            // Load existing entities and relationships AFTER initialization
            Platform.runLater(() -> loadInvestigationData(investigation.getId()));
        }
    }

    private void loadInvestigationData(int investigationId) {
        // Load Entities
        Collection<Entity> entities = entityRepository.findByInvestigationId(investigationId);
        for (Entity entity : entities) {
            graphManager.addEntity(entity);
            // Update counter to avoid ID collision
            if (entity.getId() >= entityIdCounter) {
                entityIdCounter = entity.getId() + 1;
            }
        }

        // Load Relationships
        Collection<Relationship> relationships = relationshipRepository.findByInvestigationId(investigationId);
        for (Relationship rel : relationships) {
            graphManager.addRelationship(rel);
        }
    }

    @FXML
    private void handleToggleLayout() {
        if (graphManager != null) {
            graphManager.toggleAutomaticLayout();
        }
    }

    @FXML
    private void handleCloseZoom() {
        zoomOverlay.setVisible(false);
    }

    @FXML
    private void handleRenameInvestigation() {
        if (currentInvestigation == null) return;

        TextInputDialog dialog = new TextInputDialog(currentInvestigation.getName());
        dialog.setTitle("Rename Investigation");
        dialog.setHeaderText("Current Name: " + currentInvestigation.getName());
        dialog.setContentText("Enter New Name:");
        styleDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                currentInvestigation.setName(newName);
                investigationNameLabel.setText("Investigation: " + newName);
                
                if (investigationRepository.update(currentInvestigation)) {
                    System.out.println("Terminal: Investigation renamed to: " + newName);
                } else {
                    System.err.println("Terminal: Failed to update investigation name in DB.");
                }
            }
        });
    }

    @FXML
    private void handleBackToMenu() {
        if (currentInvestigation == null) {
            ViewLoader.loadView("/fxml/main_menu.fxml", "ReConan - Main Menu");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Investigation");
        alert.setHeaderText("Do you want to save the changes to: " + currentInvestigation.getName() + "?");
        alert.setContentText("Select your action:");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType dontSaveButton = new ButtonType("Don't Save");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());

        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);
        styleDialog(alert);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == saveButton) {
                saveCurrentInvestigation();
                returnToMainMenu();
            } else if (result.get() == dontSaveButton) {
                returnToMainMenu();
            }
            // If Cancel, do nothing
        }
    }

    private void saveCurrentInvestigation() {
        if (currentInvestigation != null) {
            int invId = currentInvestigation.getId();

            // 0. Clear relationships first to avoid FK constraint violations when deleting
            // entities
            relationshipRepository.deleteAll(invId);

            // 1. Save Entities and get ID Map (OldID -> NewID)
            Collection<Entity> entities = graphManager.getEntities();
            Map<Integer, Integer> idMap = entityRepository.saveAll(invId, new ArrayList<>(entities));

            // 2. Update Relationship IDs using the Map
            Collection<Relationship> relationships = graphManager.getRelationships();
            for (Relationship rel : relationships) {
                Integer newSourceId = idMap.get(rel.getSourceId());
                Integer newTargetId = idMap.get(rel.getTargetId());

                if (newSourceId != null)
                    rel.setSourceId(newSourceId);
                if (newTargetId != null)
                    rel.setTargetId(newTargetId);
            }

            // 3. Save Relationships
            relationshipRepository.saveAll(invId, new ArrayList<>(relationships));

            System.out.println("Terminal: Investigation saved: " + currentInvestigation.getName());
        }
    }

    private void returnToMainMenu() {
        System.out.println("Terminal: Returning to Main Menu...");
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