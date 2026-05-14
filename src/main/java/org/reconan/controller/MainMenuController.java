package org.reconan.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.reconan.model.Investigation;
import org.reconan.repository.InvestigationRepository;
import org.reconan.ui.ViewLoader;

import java.util.List;
import java.util.Optional;

/**
 * Controller for the Main Menu view.
 */
public class MainMenuController {

    private final InvestigationRepository investigationRepository = new InvestigationRepository();

    @FXML
    private void handleCreateInvestigation() {
        System.out.println("Terminal: Opening Create Investigation menu...");
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create Investigation");
        dialog.setHeaderText("Enter investigation details");
        styleDialog(dialog);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(nameField.getText(), descField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(nameDesc -> {
            String name = nameDesc.getKey();
            String desc = nameDesc.getValue();
            
            if (name == null || name.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Investigation name cannot be empty.");
                styleDialog(alert);
                alert.showAndWait();
                return;
            }

            Investigation newInv = new Investigation(name, desc);
            Investigation createdInv = investigationRepository.create(newInv);

            if (createdInv != null) {
                openWorkspace(createdInv);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create investigation in database.");
                styleDialog(alert);
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleOpenInvestigation() {
        System.out.println("Terminal: Opening Open Investigation menu...");
        List<Investigation> investigations = investigationRepository.findAll();
        
        if (investigations.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No investigations found. Please create one first.");
            styleDialog(alert);
            alert.showAndWait();
            return;
        }

        ChoiceDialog<Investigation> dialog = new ChoiceDialog<>(investigations.get(0), investigations);
        dialog.setTitle("Open Investigation");
        dialog.setHeaderText("Select an investigation to open:");
        dialog.setContentText("Investigation:");
        styleDialog(dialog);

        Optional<Investigation> result = dialog.showAndWait();
        result.ifPresent(this::openWorkspace);
    }

    private void openWorkspace(Investigation investigation) {
        System.out.println("Terminal: Opening Investigation Workspace for: " + investigation.getName());
        InvestigationWorkspaceController controller = ViewLoader.loadViewWithController("/fxml/investigation_workspace.fxml", "ReConan - " + investigation.getName());
        if (controller != null) {
            controller.initData(investigation);
        }
    }

    @FXML
    private void handleAbout() {
        System.out.println("Terminal: Opening About ReConan menu...");
        ViewLoader.loadView("/fxml/about.fxml", "ReConan - About");
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
