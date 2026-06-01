package com.bananarepublic.ui.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bananarepublic.BananaRepublic;
import com.bananarepublic.session.GameSession;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

public class LobbyController {
    private static final double DESIGN_WIDTH = 1280.0;
    private static final double DESIGN_HEIGHT = 720.0;
    private static final String NO_FILE_CHOSEN_TEXT = "No File Chosen";

    @FXML
    private StackPane rootPane;

    @FXML
    private AnchorPane designPane;

    @FXML
    private ToggleButton threePlayerToggle;

    @FXML
    private ToggleButton fourPlayerToggle;

    @FXML
    private TextField player1NameField;

    @FXML
    private TextField player2NameField;

    @FXML
    private TextField player3NameField;

    @FXML
    private TextField player4NameField;

    @FXML
    private Label environmentFileLabel;

    @FXML
    private Label automationFileLabel;

    private final ToggleGroup playerCountGroup = new ToggleGroup();

    private File environmentDataFile;
    private File automationIntelligenceFile;

    @FXML
    public void initialize() {
        setupResponsiveScaling();
        setupPlayerCountToggle();
        setupDefaultNames();
        setupDefaultPluginLabels();
    }

    private void setupResponsiveScaling() {
        designPane.scaleXProperty().bind(rootPane.widthProperty().divide(DESIGN_WIDTH));
        designPane.scaleYProperty().bind(rootPane.heightProperty().divide(DESIGN_HEIGHT));
    }

    private void setupPlayerCountToggle() {
        threePlayerToggle.setToggleGroup(playerCountGroup);
        fourPlayerToggle.setToggleGroup(playerCountGroup);

        threePlayerToggle.setUserData(3);
        fourPlayerToggle.setUserData(4);

        threePlayerToggle.setSelected(true);
        updatePlayerCount(3);

        playerCountGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
                return;
            }

            updatePlayerCount((int) newToggle.getUserData());
        });
    }

    private void setupDefaultNames() {
        player1NameField.setText("Kebin");
        player2NameField.setText("Stewart");
        player3NameField.setText("Bob");
        player4NameField.setText("Toto");
    }

    private void setupDefaultPluginLabels() {
        environmentFileLabel.setText(NO_FILE_CHOSEN_TEXT);
        automationFileLabel.setText(NO_FILE_CHOSEN_TEXT);
        environmentFileLabel.setTooltip(null);
        automationFileLabel.setTooltip(null);
    }

    private void updatePlayerCount(int count) {
        boolean useFourPlayers = count == 4;

        player4NameField.setDisable(!useFourPlayers);
        player4NameField.setOpacity(useFourPlayers ? 1.0 : 0.45);
    }

    @FXML
    private void onEnvironmentDataClicked() {
        File selectedFile = chooseJarFile("Choose Environment Data Plugin");
        if (selectedFile == null) {
            return;
        }

        environmentDataFile = selectedFile;
        environmentFileLabel.setText(shortenFileName(selectedFile.getName()));
        environmentFileLabel.setTooltip(new Tooltip(selectedFile.getAbsolutePath()));

        System.out.println("Environment Data plugin selected: " + selectedFile.getAbsolutePath());

        // TODO: sambungkan ke MapPluginLoader dari anggota 3.
    }

    @FXML
    private void onAutomationIntelligenceClicked() {
        File selectedFile = chooseJarFile("Choose Automation Intelligence Plugin");
        if (selectedFile == null) {
            return;
        }

        automationIntelligenceFile = selectedFile;
        automationFileLabel.setText(shortenFileName(selectedFile.getName()));
        automationFileLabel.setTooltip(new Tooltip(selectedFile.getAbsolutePath()));

        System.out.println("Automation Intelligence plugin selected: " + selectedFile.getAbsolutePath());

        // TODO: sambungkan ke BotPluginLoader dari anggota 3.
    }

    @FXML
    private void onStartGameClicked() throws Exception {
        int playerCount = getSelectedPlayerCount();

        if (!isValidPlayerSetup(playerCount)) {
            showWarning("Player name tidak boleh kosong.");
            return;
        }

        List<String> names = new ArrayList<>();
        names.add(player1NameField.getText().trim());
        names.add(player2NameField.getText().trim());
        names.add(player3NameField.getText().trim());

        if (playerCount == 4) {
            names.add(player4NameField.getText().trim());
        }

        GameSession.setPlayerNames(names);
        GameSession.clearLoadedSaveData();

        // Buat GameEngine + GameController via GameFactory
        com.bananarepublic.controller.GameController gc =
            com.bananarepublic.GameFactory.build();
        GameSession.setGameController(gc);

        System.out.println("Start new game with " + playerCount + " players.");
        System.out.println("Players: " + names);

        BananaRepublic.showGame();
    }

    @FXML
    private void onCancelClicked() throws Exception {
        BananaRepublic.showMainMenu();
    }

    @FXML
    private void onSettingsClicked() {
        System.out.println("Settings clicked from lobby.");
    }

    private File chooseJarFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JAR Plugin Files", "*.jar"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        return fileChooser.showOpenDialog(rootPane.getScene().getWindow());
    }

    private int getSelectedPlayerCount() {
        if (playerCountGroup.getSelectedToggle() == null) {
            return 3;
        }

        return (int) playerCountGroup.getSelectedToggle().getUserData();
    }

    private boolean isValidPlayerSetup(int playerCount) {
        if (isBlank(player1NameField.getText())) return false;
        if (isBlank(player2NameField.getText())) return false;
        if (isBlank(player3NameField.getText())) return false;

        return playerCount != 4 || !isBlank(player4NameField.getText());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String getFilePathOrDefault(File file, String defaultValue) {
        return file == null ? defaultValue : file.getAbsolutePath();
    }

    private String shortenFileName(String fileName) {
        int maxLength = 24;

        if (fileName.length() <= maxLength) {
            return fileName;
        }

        return fileName.substring(0, 10) + "..." + fileName.substring(fileName.length() - 10);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Session Setup");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}