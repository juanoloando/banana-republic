package com.bananarepublic.ui.controller;

import java.util.List;
import java.util.Optional;

import com.bananarepublic.BananaRepublic;
import com.bananarepublic.session.GameSession;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameController {
    private static final String SETTINGS_POPUP_IMAGE_PATH = "/com/bananarepublic/images/game/popup_settings.png";
    private static final double DESIGN_WIDTH = 1920.0;
    private static final double DESIGN_HEIGHT = 1148.0;
    private static final double POPUP_ASSET_WIDTH = 1048.0;
    private static final double POPUP_ASSET_HEIGHT = 753.0;
    private static final double POPUP_CLOSE_CENTER_X = 938.0;
    private static final double POPUP_CLOSE_CENTER_Y = 118.0;
    private static final double POPUP_EXIT_CENTER_X = 110.0;
    private static final double POPUP_EXIT_CENTER_Y = 165.0;

    @FXML
    private StackPane rootPane;

    @FXML
    private AnchorPane designPane;

    @FXML
    private Pane boardPane;

    @FXML
    private Label timerLabel;

    @FXML
    private Button player4Button;

    @FXML
    private ImageView player4ProfileImage;

    @FXML
    private Pane player4DisabledOverlay;

    @FXML
    private ScrollPane logbookScrollPane;

    @FXML
    private VBox logbookBox;

    @FXML
    private Pane popupOverlay;

    @FXML
    private ImageView popupImageView;

    @FXML
    private Button popupCloseButton;

    @FXML
    private Button popupExitToMenuButton;

    @FXML
    private Label player1NameLabel;

    @FXML
    private Label player2NameLabel;

    @FXML
    private Label player3NameLabel;

    @FXML
    private Label player4NameLabel;

    @FXML
    private Label player1VpValueLabel;
    @FXML
    private Label player1OutpostValueLabel;
    @FXML
    private Label player1LabValueLabel;
    @FXML
    private Label player1KnightValueLabel;
    @FXML
    private Label player1PipeValueLabel;

    @FXML
    private Label player2VpValueLabel;
    @FXML
    private Label player2OutpostValueLabel;
    @FXML
    private Label player2LabValueLabel;
    @FXML
    private Label player2KnightValueLabel;
    @FXML
    private Label player2PipeValueLabel;

    @FXML
    private Label player3VpValueLabel;
    @FXML
    private Label player3OutpostValueLabel;
    @FXML
    private Label player3LabValueLabel;
    @FXML
    private Label player3KnightValueLabel;
    @FXML
    private Label player3PipeValueLabel;

    @FXML
    private Label player4VpValueLabel;
    @FXML
    private Label player4OutpostValueLabel;
    @FXML
    private Label player4LabValueLabel;
    @FXML
    private Label player4KnightValueLabel;
    @FXML
    private Label player4PipeValueLabel;

    private int playerCount = 4;

    @FXML
    public void initialize() {
        setupResponsiveScaling();
        setupInitialUI();
        loadPlayerNames();
        appendLog("Game screen initialized.");

        if (GameSession.hasLoadedSaveData()) {
            appendLog("Loaded game state restored.");
        } else {
            appendLog("New game started.");
        }
    }

    private void setupResponsiveScaling() {
        designPane.scaleXProperty().bind(rootPane.widthProperty().divide(DESIGN_WIDTH));
        designPane.scaleYProperty().bind(rootPane.heightProperty().divide(DESIGN_HEIGHT));
    }

    private void setupInitialUI() {
        timerLabel.setText("01:30");
        setupInitialPlayerStats();
        hidePopup();
    }

    private void setupInitialPlayerStats() {
        setPlayerStats(1, "0", "0", "0", "0", "0");
        setPlayerStats(2, "0", "0", "0", "0", "0");
        setPlayerStats(3, "0", "0", "0", "0", "0");
        setPlayerStats(4, "0", "0", "0", "0", "0");
    }

    private void setPlayerStats(
            int playerIndex,
            String vp,
            String outpost,
            String lab,
            String knight,
            String longestPipe
    ) {
        switch (playerIndex) {
            case 1:
                player1VpValueLabel.setText(vp);
                player1OutpostValueLabel.setText(outpost);
                player1LabValueLabel.setText(lab);
                player1KnightValueLabel.setText(knight);
                player1PipeValueLabel.setText(longestPipe);
                break;
            case 2:
                player2VpValueLabel.setText(vp);
                player2OutpostValueLabel.setText(outpost);
                player2LabValueLabel.setText(lab);
                player2KnightValueLabel.setText(knight);
                player2PipeValueLabel.setText(longestPipe);
                break;
            case 3:
                player3VpValueLabel.setText(vp);
                player3OutpostValueLabel.setText(outpost);
                player3LabValueLabel.setText(lab);
                player3KnightValueLabel.setText(knight);
                player3PipeValueLabel.setText(longestPipe);
                break;
            case 4:
                player4VpValueLabel.setText(vp);
                player4OutpostValueLabel.setText(outpost);
                player4LabValueLabel.setText(lab);
                player4KnightValueLabel.setText(knight);
                player4PipeValueLabel.setText(longestPipe);
                break;
            default:
                break;
        }
    }

    private void setLabelGroupVisible(boolean visible, Label... labels) {
        for (Label label : labels) {
            label.setVisible(visible);
            label.setManaged(visible);
        }
    }

    private void loadPlayerNames() {
        List<String> names = GameSession.getPlayerNames();

        player1NameLabel.setText(names.size() > 0 ? names.get(0) : "Player 1");
        player2NameLabel.setText(names.size() > 1 ? names.get(1) : "Player 2");
        player3NameLabel.setText(names.size() > 2 ? names.get(2) : "Player 3");
        player4NameLabel.setText(names.size() > 3 ? names.get(3) : "Player 4");

        int count = names.isEmpty() ? 4 : names.size();
        configurePlayerCount(count);
    }

    public void configurePlayerCount(int count) {
        this.playerCount = count;

        boolean hasFourthPlayer = count >= 4;

        player4Button.setDisable(!hasFourthPlayer);
        player4Button.setVisible(hasFourthPlayer);
        player4Button.setManaged(hasFourthPlayer);

        player4ProfileImage.setVisible(hasFourthPlayer);
        player4ProfileImage.setManaged(hasFourthPlayer);

        player4NameLabel.setVisible(hasFourthPlayer);
        player4NameLabel.setManaged(hasFourthPlayer);

        setLabelGroupVisible(
                hasFourthPlayer,
                player4VpValueLabel,
                player4OutpostValueLabel,
                player4LabValueLabel,
                player4KnightValueLabel,
                player4PipeValueLabel
        );

        player4DisabledOverlay.setVisible(false);
        player4DisabledOverlay.setManaged(false);
    }

    @FXML
    private void onDiceButtonClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            openManualDiceDialog();
            return;
        }

        rollDiceRandom();
    }

    private void rollDiceRandom() {
        int die1 = (int) (Math.random() * 6) + 1;
        int die2 = (int) (Math.random() * 6) + 1;
        int total = die1 + die2;

        appendLog("Rolled dice: " + die1 + " + " + die2 + " = " + total);

        // TODO: panggil GameEngine.rollDice()
    }

    private void openManualDiceDialog() {
        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Manual Dice");
        dialog.setHeaderText("Insert manual dice value");

        ButtonType rollButtonType = new ButtonType("Roll", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rollButtonType, ButtonType.CANCEL);

        Spinner<Integer> die1Spinner = new Spinner<>(1, 6, 1);
        Spinner<Integer> die2Spinner = new Spinner<>(1, 6, 1);

        VBox content = new VBox(
                10,
                new Label("Die 1"),
                die1Spinner,
                new Label("Die 2"),
                die2Spinner
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == rollButtonType) {
                return new int[]{die1Spinner.getValue(), die2Spinner.getValue()};
            }
            return null;
        });

        Optional<int[]> result = dialog.showAndWait();

        result.ifPresent(values -> {
            int total = values[0] + values[1];
            appendLog("Manual dice: " + values[0] + " + " + values[1] + " = " + total);

            // TODO: panggil GameEngine.manualRollDice(values[0], values[1])
        });
    }

    @FXML
    private void onTradeClicked() {
        TradeDialogController.showDialog();
        appendLog("Opened trade dialog.");
    }

    @FXML
    private void onBankClicked() {
        showPopupImage("/com/bananarepublic/images/game/popup_bank.png");
        appendLog("Opened bank resource information.");
    }

    @FXML
    private void onBuyExperimentCardClicked() {
        appendLog("Buy experiment card clicked.");

        // TODO: nanti buka ExperimentCard dialog.
    }

    @FXML
    private void onDeclareVictoryClicked() {
        appendLog("Declare victory clicked.");

        // TODO: nanti buka Victory dialog.
    }

    @FXML
    private void onEndTurnClicked() {
        appendLog("End turn clicked.");

        // TODO: panggil GameEngine.endTurn()
    }

    @FXML
    private void onSettingsClicked() {
        showPopupImage(SETTINGS_POPUP_IMAGE_PATH);
        appendLog("Opened settings.");
    }

    @FXML
    private void onPlayer1Clicked() {
        showPlayerInventory(1);
    }

    @FXML
    private void onPlayer2Clicked() {
        showPlayerInventory(2);
    }

    @FXML
    private void onPlayer3Clicked() {
        showPlayerInventory(3);
    }

    @FXML
    private void onPlayer4Clicked() {
        if (playerCount < 4) {
            return;
        }

        showPlayerInventory(4);
    }

    private void showPlayerInventory(int playerIndex) {
        showPopupImage("/com/bananarepublic/images/game/popup_inventory.png");
        appendLog("Opened inventory for player " + playerIndex + ".");
    }

    public void showResourceTheftPopup(String resourceName) {
        showPopupImage("/com/bananarepublic/images/game/popup_resource_theft.png");
        appendLog("Resource theft result: " + resourceName);
    }

    public void showRobberEffectPopup() {
        showPopupImage("/com/bananarepublic/images/game/popup_robber_effect.png");
        appendLog("Nimon Ungu effect activated.");
    }

    private void showPopupImage(String imagePath) {
        Image image = new Image(BananaRepublic.class.getResource(imagePath).toExternalForm());
        popupImageView.setImage(image);
        alignPopupCloseHitbox();
        alignPopupExitHitbox();

        popupOverlay.setVisible(true);
        popupOverlay.setManaged(true);

        popupImageView.setVisible(true);
        popupImageView.setManaged(true);

        popupCloseButton.setVisible(true);
        popupCloseButton.setManaged(true);

        boolean isSettingsPopup = SETTINGS_POPUP_IMAGE_PATH.equals(imagePath);
        popupExitToMenuButton.setVisible(isSettingsPopup);
        popupExitToMenuButton.setManaged(isSettingsPopup);

        popupOverlay.toFront();
        popupImageView.toFront();
        if (isSettingsPopup) {
            popupExitToMenuButton.toFront();
        }
        popupCloseButton.toFront();
    }

    private void alignPopupCloseHitbox() {
        double scaleX = popupImageView.getFitWidth() / POPUP_ASSET_WIDTH;
        double scaleY = popupImageView.getFitHeight() / POPUP_ASSET_HEIGHT;

        double closeCenterX = popupImageView.getLayoutX() + (POPUP_CLOSE_CENTER_X * scaleX);
        double closeCenterY = popupImageView.getLayoutY() + (POPUP_CLOSE_CENTER_Y * scaleY);

        double hitboxWidth = popupCloseButton.getPrefWidth();
        double hitboxHeight = popupCloseButton.getPrefHeight();

        popupCloseButton.setLayoutX(closeCenterX - (hitboxWidth / 2.0));
        popupCloseButton.setLayoutY(closeCenterY - (hitboxHeight / 2.0));
    }

    private void alignPopupExitHitbox() {
        double scaleX = popupImageView.getFitWidth() / POPUP_ASSET_WIDTH;
        double scaleY = popupImageView.getFitHeight() / POPUP_ASSET_HEIGHT;

        double exitCenterX = popupImageView.getLayoutX() + (POPUP_EXIT_CENTER_X * scaleX);
        double exitCenterY = popupImageView.getLayoutY() + (POPUP_EXIT_CENTER_Y * scaleY);

        double hitboxWidth = popupExitToMenuButton.getPrefWidth();
        double hitboxHeight = popupExitToMenuButton.getPrefHeight();

        popupExitToMenuButton.setLayoutX(exitCenterX - (hitboxWidth / 2.0));
        popupExitToMenuButton.setLayoutY(exitCenterY - (hitboxHeight / 2.0));
    }

    @FXML
    private void onExitToMainMenuClicked() {
        hidePopup();
        appendLog("Exit to main menu from settings.");
        try {
            BananaRepublic.showMainMenu();
        } catch (Exception e) {
            appendLog("Failed to open main menu: " + e.getMessage());
        }
    }

    @FXML
    private void onClosePopupClicked() {
        hidePopup();
    }

    private void hidePopup() {
        popupOverlay.setVisible(false);
        popupOverlay.setManaged(false);

        popupImageView.setVisible(false);
        popupImageView.setManaged(false);

        popupCloseButton.setVisible(false);
        popupCloseButton.setManaged(false);

        popupExitToMenuButton.setVisible(false);
        popupExitToMenuButton.setManaged(false);
    }

    private void appendLog(String message) {
        Label label = new Label("- " + message);
        label.setWrapText(true);
        label.setMaxWidth(452);
        label.getStyleClass().add("logbook-entry");

        logbookBox.getChildren().add(label);

        Platform.runLater(() -> logbookScrollPane.setVvalue(1.0));
    }
}

