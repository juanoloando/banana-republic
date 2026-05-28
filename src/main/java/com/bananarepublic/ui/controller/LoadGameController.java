package com.bananarepublic.ui.controller;

import java.io.File;

import com.bananarepublic.BananaRepublic;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

public class LoadGameController {
    private static final double DESIGN_WIDTH = 1280.0;
    private static final double DESIGN_HEIGHT = 720.0;
    private static final String NO_FILE_CHOSEN_TEXT = "No File Chosen";

    @FXML
    private StackPane rootPane;

    @FXML
    private AnchorPane designPane;

    @FXML
    private Label selectedFileLabel;

    private File selectedFile;

    @FXML
    public void initialize() {
        setupResponsiveScaling();
        selectedFileLabel.setText(NO_FILE_CHOSEN_TEXT);
    }

    private void setupResponsiveScaling() {
        designPane.scaleXProperty().bind(rootPane.widthProperty().divide(DESIGN_WIDTH));
        designPane.scaleYProperty().bind(rootPane.heightProperty().divide(DESIGN_HEIGHT));
    }

    @FXML
    private void onChooseFileClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Saved Game File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Save Files", "*.json", "*.ser"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        selectedFile = fileChooser.showOpenDialog(selectedFileLabel.getScene().getWindow());

        if (selectedFile != null) {
            selectedFileLabel.setText(shortenFileName(selectedFile.getName()));
            selectedFileLabel.setTooltip(new javafx.scene.control.Tooltip(selectedFile.getAbsolutePath()));
        }
    }

    @FXML
    private void onLoadGameClicked() throws Exception {
        if (selectedFile == null) {
            showWarning("Please choose a save file first.");
            return;
        }

        System.out.println("Load game from: " + selectedFile.getAbsolutePath());

        // TODO: panggil SaveLoadService dari anggota 3 / GameFactory dari anggota 5.
        // Untuk sementara langsung masuk game screen dulu.
        BananaRepublic.showGame();
    }

    @FXML
    private void onCancelClicked() throws Exception {
        BananaRepublic.showMainMenu();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Load Game");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String shortenFileName(String fileName) {
        int maxLength = 24;
        if (fileName.length() <= maxLength) {
            return fileName;
        }
        return fileName.substring(0, 10) + "..." + fileName.substring(fileName.length() - 10);
    }
}
