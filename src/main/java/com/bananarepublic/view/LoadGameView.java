package com.bananarepublic.view;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.bananarepublic.BananaRepublic;
import com.bananarepublic.controller.GameSession;

import javafx.fxml.FXML;
import com.bananarepublic.view.util.DialogUtil;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import persistence.JsonSaveLoadService;
import persistence.SaveCorruptedException;
import persistence.SaveData;
import persistence.SaveLoadService;

public class LoadGameView {
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

    private final SaveLoadService saveLoadService = new JsonSaveLoadService();

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
        selectedFile = DialogUtil.showOpenFile(
                "Choose Saved Game File",
                selectedFileLabel.getScene().getWindow(),
                new FileChooser.ExtensionFilter("JSON Save Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        if (selectedFile != null) {
            selectedFileLabel.setText(shortenFileName(selectedFile.getName()));
            selectedFileLabel.setTooltip(new Tooltip(selectedFile.getAbsolutePath()));
        }
    }

    @FXML
    private void onLoadGameClicked() {
        if (selectedFile == null) {
            DialogUtil.showWarning("Load Game", "Please choose a save file first.");
            return;
        }

        if (!saveLoadService.isValidSave(selectedFile)) {
            DialogUtil.showWarning("Load Game", "Selected file is not a valid JSON save file.");
            return;
        }

        try {
            SaveData saveData = saveLoadService.load(selectedFile);

            List<String> loadedNamesFromSave = saveData.players
                    .stream()
                    .map(playerData -> playerData.name)
                    .toList();

            GameSession.setPlayerNames(loadedNamesFromSave);
            GameSession.setLoadedSaveData(saveData);
            GameSession.clearPausedGameTimer(); // new game load → no frozen timer

            // Buat GameEngine dari save data agar phase events bisa di-fire
            com.bananarepublic.controller.GameController gc =
                com.bananarepublic.controller.GameFactory.buildFromSave(saveData);
            GameSession.setGameController(gc);

            System.out.println("Load game from: " + selectedFile.getAbsolutePath());
            System.out.println("Loaded players: " + loadedNamesFromSave);

            BananaRepublic.showGame();
        } catch (IOException e) {
            DialogUtil.showError("Load Game", "Failed to read save file:\n" + e.getMessage());
        } catch (SaveCorruptedException e) {
            DialogUtil.showError("Load Game", "Save file is corrupted:\n" + e.getMessage());
        } catch (Exception e) {
            DialogUtil.showError("Load Game", "Failed to load game:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCancelClicked() throws Exception {
        // If a game is already running, return to it instead of main menu
        if (GameSession.hasLoadedSaveData() || !GameSession.getPlayerNames().isEmpty()) {
            BananaRepublic.showGame();
        } else {
            BananaRepublic.showMainMenu();
        }
    }

    private String shortenFileName(String fileName) {
        int maxLength = 24;

        if (fileName.length() <= maxLength) {
            return fileName;
        }

        return fileName.substring(0, 10) + "..." + fileName.substring(fileName.length() - 10);
    }
}