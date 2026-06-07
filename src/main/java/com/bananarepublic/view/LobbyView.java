package com.bananarepublic.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bananarepublic.BananaRepublic;
import com.bananarepublic.controller.GameSession;
import com.bananarepublic.view.util.DialogUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class LobbyView {
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
    private Button player1BotButton;

    @FXML
    private Button player2BotButton;

    @FXML
    private Button player3BotButton;

    @FXML
    private Button player4BotButton;

    @FXML
    private Node player4RowCover;

    @FXML
    private VBox automationInfoBox;

    private final ToggleGroup playerCountGroup = new ToggleGroup();
    private final Map<Integer, File> selectedBotPluginFiles = new HashMap<>();

    private File environmentDataFile;

    @FXML
    public void initialize() {
        setupResponsiveScaling();
        setupPlayerCountToggle();
        setupDefaultNames();
        setupPlayerNameListeners();
        setupDefaultLobbyInfo();
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

    private void setupPlayerNameListeners() {
        player1NameField.textProperty().addListener((obs, oldName, newName) -> updateAutomationInfo());
        player2NameField.textProperty().addListener((obs, oldName, newName) -> updateAutomationInfo());
        player3NameField.textProperty().addListener((obs, oldName, newName) -> updateAutomationInfo());
        player4NameField.textProperty().addListener((obs, oldName, newName) -> updateAutomationInfo());
    }

    private void setupDefaultLobbyInfo() {
        environmentFileLabel.setText(NO_FILE_CHOSEN_TEXT);
        environmentFileLabel.setTooltip(null);
        updateAutomationInfo();
    }

    private void updatePlayerCount(int count) {
        updatePlayerRowsVisibility();
        clearInactivePlayerData();
        updateBotButtonsState();
        updateAutomationInfo();
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
    }

    @FXML
    private void onPlayer1BotClicked() {
        onPlayerBotButtonClicked(0);
    }

    @FXML
    private void onPlayer2BotClicked() {
        onPlayerBotButtonClicked(1);
    }

    @FXML
    private void onPlayer3BotClicked() {
        onPlayerBotButtonClicked(2);
    }

    @FXML
    private void onPlayer4BotClicked() {
        onPlayerBotButtonClicked(3);
    }

    private void onPlayerBotButtonClicked(int playerIndex) {
        System.out.println("Bot button clicked for player " + (playerIndex + 1));

        if (playerIndex >= getSelectedPlayerCount()) {
            return;
        }

        chooseBotPluginForPlayer(playerIndex);
    }

    private void chooseBotPluginForPlayer(int playerIndex) {
        File selectedFile = chooseJarFile("Choose Automation Intelligence Plugin");
        if (selectedFile == null) {
            return;
        }

        selectedBotPluginFiles.put(playerIndex, selectedFile);
        String playerName = getPlayerNameField(playerIndex).getText().trim();

        System.out.println("Selected bot for " + playerName + ": " + selectedFile.getName());

        updateBotButtonsState();
        updateAutomationInfo();
    }

    @FXML
    private void onStartGameClicked() throws Exception {
        int playerCount = getSelectedPlayerCount();

        if (!isValidPlayerSetup(playerCount)) {
            DialogUtil.showWarning("Session Setup", "Player name tidak boleh kosong.");
            return;
        }

        List<String> names = getActivePlayerNames();
        if (!areSelectedBotFilesValid(names)) {
            return;
        }

        Map<Integer, String> botPluginPaths = getActiveBotPluginPaths();

        GameSession.setPlayerNames(names);
        GameSession.setPlayerBotPluginPaths(botPluginPaths);
        GameSession.clearLoadedSaveData();

        // Buat GameEngine + GameController via GameFactory
        com.bananarepublic.controller.GameController gc =
            com.bananarepublic.controller.GameFactory.build();
        GameSession.setGameController(gc);

        System.out.println("Start new game with " + playerCount + " players.");
        System.out.println("Players: " + names);
        System.out.println("Starting game with bot plugins: " + botPluginPaths);

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
        return DialogUtil.showOpenFile(title, rootPane.getScene().getWindow(),
                new FileChooser.ExtensionFilter("JAR Plugin Files", "*.jar"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
    }

    private int getSelectedPlayerCount() {
        if (playerCountGroup.getSelectedToggle() == null) {
            return 3;
        }

        return (int) playerCountGroup.getSelectedToggle().getUserData();
    }

    private List<String> getActivePlayerNames() {
        int playerCount = getSelectedPlayerCount();
        List<String> names = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            names.add(getPlayerNameField(i).getText().trim());
        }

        return names;
    }

    private Map<Integer, String> getActiveBotPluginPaths() {
        int playerCount = getSelectedPlayerCount();
        Map<Integer, String> botPluginPaths = new LinkedHashMap<>();

        for (int i = 0; i < playerCount; i++) {
            File botFile = selectedBotPluginFiles.get(i);
            if (botFile != null) {
                botPluginPaths.put(i, botFile.getAbsolutePath());
            }
        }

        return botPluginPaths;
    }

    private boolean areSelectedBotFilesValid(List<String> names) {
        for (int i = 0; i < names.size(); i++) {
            File botFile = selectedBotPluginFiles.get(i);
            if (botFile != null && !botFile.exists()) {
                DialogUtil.showWarning("Session Setup", "Bot plugin file for " + names.get(i) + " cannot be found.");
                return false;
            }
        }

        return true;
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

    private void updateBotButtonsState() {
        int playerCount = getSelectedPlayerCount();

        for (int i = 0; i < 4; i++) {
            Button botButton = getBotButton(i);
            boolean active = i < playerCount;
            boolean selected = selectedBotPluginFiles.containsKey(i);

            botButton.setDisable(!active);
            botButton.setVisible(active);
            botButton.setManaged(active);
            botButton.setOpacity(1.0);
            botButton.getStyleClass().remove("robot-button-selected");

            if (selected) {
                botButton.getStyleClass().add("robot-button-selected");
            }
        }
    }

    private void updateAutomationInfo() {
        automationInfoBox.getChildren().clear();
        int playerCount = getSelectedPlayerCount();

        for (int i = 0; i < playerCount; i++) {
            String playerName = getPlayerNameField(i).getText().trim();
            File botFile = selectedBotPluginFiles.get(i);
            String botName = botFile == null ? "No bot" : shortenFileName(botFile.getName());
            Label botInfoLabel = new Label(playerName + ": " + botName);

            botInfoLabel.setMaxWidth(Double.MAX_VALUE);
            botInfoLabel.getStyleClass().add("automation-info-text");
            automationInfoBox.getChildren().add(botInfoLabel);
        }
    }

    private void updatePlayerRowsVisibility() {
        int playerCount = getSelectedPlayerCount();

        for (int i = 0; i < 4; i++) {
            boolean active = i < playerCount;
            TextField playerNameField = getPlayerNameField(i);

            playerNameField.setDisable(!active);
            playerNameField.setVisible(active);
            playerNameField.setManaged(active);
            playerNameField.setOpacity(1.0);
        }

        boolean hidePlayer4Row = playerCount == 3;
        player4RowCover.setVisible(hidePlayer4Row);
        player4RowCover.setManaged(hidePlayer4Row);
    }

    private void clearInactivePlayerData() {
        int playerCount = getSelectedPlayerCount();

        for (int i = playerCount; i < 4; i++) {
            if (selectedBotPluginFiles.remove(i) != null) {
                System.out.println("Cleared bot for inactive player " + (i + 1));
            }
        }
    }

    private TextField getPlayerNameField(int playerIndex) {
        return switch (playerIndex) {
            case 0 -> player1NameField;
            case 1 -> player2NameField;
            case 2 -> player3NameField;
            case 3 -> player4NameField;
            default -> throw new IllegalArgumentException("Unknown player index: " + playerIndex);
        };
    }

    private Button getBotButton(int playerIndex) {
        return switch (playerIndex) {
            case 0 -> player1BotButton;
            case 1 -> player2BotButton;
            case 2 -> player3BotButton;
            case 3 -> player4BotButton;
            default -> throw new IllegalArgumentException("Unknown player index: " + playerIndex);
        };
    }

    private String shortenFileName(String fileName) {
        int maxLength = 24;

        if (fileName.length() <= maxLength) {
            return fileName;
        }

        return fileName.substring(0, 10) + "..." + fileName.substring(fileName.length() - 10);
    }

}
