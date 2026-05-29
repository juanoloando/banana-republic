package com.bananarepublic.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bananarepublic.BananaRepublic;
import com.bananarepublic.session.GameSession;
import com.bananarepublic.ui.renderer.HexRenderer;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.board.HexCoordinate;
import model.board.HexTile;
import model.board.StandardMapGenerator;
import model.board.TerrainType;
import persistence.SaveData;

public class GameController {
    private static final String SETTINGS_POPUP_IMAGE_PATH = "/com/bananarepublic/images/game/popup_settings.png";
    private static final String PHASE_BUILD_IMAGE_PATH = "/com/bananarepublic/images/game/status_build.png";
    private static final String PHASE_TRADE_IMAGE_PATH = "/com/bananarepublic/images/game/status_trade.png";
    private static final String CARD_KNIGHT_IMAGE_PATH = "/com/bananarepublic/images/game/card_knight.png";
    private static final String CARD_PROGRESS_IMAGE_PATH = "/com/bananarepublic/images/game/card_progress.png";
    private static final String CARD_VP_IMAGE_PATH = "/com/bananarepublic/images/game/card_vp.png";
    private static final String CARD_CUSTOM_IMAGE_PATH = "/com/bananarepublic/images/game/card_progress.png";
    private static final double EXPERIMENT_CARD_WIDTH = 70.0;
    private static final double EXPERIMENT_CARD_HEIGHT = 94.0;
    private static final double EXPERIMENT_CARD_SPACING = 8.0;
    private static final double EXPERIMENT_CARDS_START_X = 356.0;
    private static final double PLAYERS_CONTENT_BASE_WIDTH = 340.0;
    private static final double PLAYERS_CONTENT_RIGHT_PADDING = 24.0;
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
    private AnchorPane playersBoardContentPane;

    @FXML
    private Pane boardPane;

    @FXML
    private Label timerLabel;

    @FXML
    private Label turnValueLabel;

    @FXML
    private ImageView phaseStatusImageView;

    @FXML
    private Button player4Button;

    @FXML
    private ImageView player4ProfileImage;

    @FXML
    private Pane player4DisabledOverlay;

    @FXML
    private Pane player4RowPane;

    @FXML
    private ScrollPane playersBoardScrollPane;

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

    @FXML
    private HBox player1ExperimentCardsBox;

    @FXML
    private HBox player2ExperimentCardsBox;

    @FXML
    private HBox player3ExperimentCardsBox;

    @FXML
    private HBox player4ExperimentCardsBox;

    private int playerCount = 4;
    private final HexRenderer hexRenderer = new HexRenderer();

    @FXML
    public void initialize() {
        setupResponsiveScaling();
        setupInitialUI();
        loadPlayerNames();
        refreshExperimentCardsDisplay();
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
        setTurnIndicator("Player 1");
        setPhaseBuild();
        setupInitialPlayerStats();
        renderHexBoard();
        clearAllExperimentCardRows();
        hidePopup();
    }

    private void renderHexBoard() {
        SaveData loadedSaveData = GameSession.getLoadedSaveData();

        if (loadedSaveData != null && loadedSaveData.mapConfig != null && !loadedSaveData.mapConfig.isEmpty()) {
            List<HexTile> tiles = new ArrayList<>();
            for (SaveData.TileData tileData : loadedSaveData.mapConfig) {
                TerrainType terrainType = parseTerrainType(tileData.terrainType);
                if (terrainType == null) {
                    continue;
                }

                HexCoordinate coordinate = new HexCoordinate(tileData.q, tileData.r);
                tiles.add(new HexTile(coordinate, terrainType, tileData.tokenNumber));
            }

            HexCoordinate robberPosition = new HexCoordinate(loadedSaveData.robberQ, loadedSaveData.robberR);
            hexRenderer.render(boardPane, tiles, robberPosition);
            return;
        }

        var generatedBoard = new StandardMapGenerator().generateBoard();
        HexCoordinate robberPosition = generatedBoard.getAllTiles().stream()
                .filter(tile -> tile.getTerrainType() == TerrainType.DESERT)
                .map(tile -> tile.getCoordinate())
                .findFirst()
                .orElse(new HexCoordinate(0, 0));

        hexRenderer.render(boardPane, generatedBoard.getAllTiles(), robberPosition);
    }

    private TerrainType parseTerrainType(String terrainRawValue) {
        if (terrainRawValue == null || terrainRawValue.isBlank()) {
            return null;
        }

        try {
            return TerrainType.valueOf(terrainRawValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            appendLog("Unknown terrain type in save data: " + terrainRawValue);
            return null;
        }
    }

    private void clearAllExperimentCardRows() {
        player1ExperimentCardsBox.getChildren().clear();
        player2ExperimentCardsBox.getChildren().clear();
        player3ExperimentCardsBox.getChildren().clear();
        player4ExperimentCardsBox.getChildren().clear();
        playersBoardContentPane.setPrefWidth(PLAYERS_CONTENT_BASE_WIDTH);
        playersBoardScrollPane.setHvalue(0.0);
        playersBoardScrollPane.setPannable(false);
        playersBoardScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void refreshExperimentCardsDisplay() {
        clearAllExperimentCardRows();

        SaveData saveData = GameSession.getLoadedSaveData();
        if (saveData == null || saveData.players == null) {
            return;
        }

        int playerLimit = Math.min(saveData.players.size(), 4);
        int maxCardsInRow = 0;
        for (int i = 0; i < playerLimit; i++) {
            SaveData.PlayerData playerData = saveData.players.get(i);
            int totalCards = Math.max(0, playerData.knightCardsInHand)
                    + Math.max(0, playerData.progressCardsInHand)
                    + Math.max(0, playerData.victoryPointCardsInHand)
                    + Math.max(0, playerData.customCardsInHand);
            maxCardsInRow = Math.max(maxCardsInRow, totalCards);

            setExperimentCardsForPlayer(
                    i + 1,
                    playerData.knightCardsInHand,
                    playerData.progressCardsInHand,
                    playerData.victoryPointCardsInHand,
                    playerData.customCardsInHand
            );
        }

        adjustPlayersBoardContentWidth(maxCardsInRow);
        updatePlayersScrollMode(maxCardsInRow);
    }

    private void adjustPlayersBoardContentWidth(int maxCardsInRow) {
        if (maxCardsInRow <= 0) {
            playersBoardContentPane.setPrefWidth(PLAYERS_CONTENT_BASE_WIDTH);
            return;
        }

        double cardsTotalWidth = (maxCardsInRow * EXPERIMENT_CARD_WIDTH)
                + ((maxCardsInRow - 1) * EXPERIMENT_CARD_SPACING);
        double requiredWidth = EXPERIMENT_CARDS_START_X + cardsTotalWidth + PLAYERS_CONTENT_RIGHT_PADDING;
        playersBoardContentPane.setPrefWidth(Math.max(PLAYERS_CONTENT_BASE_WIDTH, requiredWidth));
    }

    private void updatePlayersScrollMode(int maxCardsInRow) {
        boolean hasAnyCards = maxCardsInRow > 0;
        playersBoardScrollPane.setPannable(hasAnyCards);
        playersBoardScrollPane.setHbarPolicy(
                hasAnyCards ? ScrollPane.ScrollBarPolicy.AS_NEEDED : ScrollPane.ScrollBarPolicy.NEVER
        );

        if (!hasAnyCards) {
            playersBoardScrollPane.setHvalue(0.0);
        }
    }

    private void setExperimentCardsForPlayer(
            int playerIndex,
            int knightCount,
            int progressCount,
            int victoryPointCount,
            int customCount
    ) {
        HBox container = getExperimentCardBox(playerIndex);
        if (container == null) {
            return;
        }

        List<ImageView> cardViews = new ArrayList<>();
        addCardImageViews(cardViews, CARD_KNIGHT_IMAGE_PATH, knightCount);
        addCardImageViews(cardViews, CARD_PROGRESS_IMAGE_PATH, progressCount);
        addCardImageViews(cardViews, CARD_VP_IMAGE_PATH, victoryPointCount);
        addCardImageViews(cardViews, CARD_CUSTOM_IMAGE_PATH, customCount);

        container.getChildren().setAll(cardViews);
    }

    private void addCardImageViews(List<ImageView> cardViews, String imagePath, int count) {
        if (count <= 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            cardViews.add(createExperimentCardImageView(imagePath));
        }
    }

    private ImageView createExperimentCardImageView(String imagePath) {
        ImageView cardImageView = new ImageView(
                new Image(BananaRepublic.class.getResource(imagePath).toExternalForm())
        );
        cardImageView.setFitWidth(EXPERIMENT_CARD_WIDTH);
        cardImageView.setFitHeight(EXPERIMENT_CARD_HEIGHT);
        cardImageView.setPreserveRatio(true);
        cardImageView.setSmooth(true);
        cardImageView.setMouseTransparent(true);
        cardImageView.getStyleClass().add("experiment-card-slot");
        return cardImageView;
    }

    private HBox getExperimentCardBox(int playerIndex) {
        switch (playerIndex) {
            case 1:
                return player1ExperimentCardsBox;
            case 2:
                return player2ExperimentCardsBox;
            case 3:
                return player3ExperimentCardsBox;
            case 4:
                return player4ExperimentCardsBox;
            default:
                return null;
        }
    }

    public void setTurnIndicator(String turnText) {
        turnValueLabel.setText(turnText);
    }

    public void setPhaseBuild() {
        phaseStatusImageView.setImage(
                new Image(BananaRepublic.class.getResource(PHASE_BUILD_IMAGE_PATH).toExternalForm())
        );
    }

    public void setPhaseTrade() {
        phaseStatusImageView.setImage(
                new Image(BananaRepublic.class.getResource(PHASE_TRADE_IMAGE_PATH).toExternalForm())
        );
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

        // Turn indicator mengikuti nama player pertama dari start game/session.
        setTurnIndicator(player1NameLabel.getText());

        int count = names.isEmpty() ? 4 : names.size();
        configurePlayerCount(count);
    }

    public void configurePlayerCount(int count) {
        this.playerCount = count;

        boolean hasFourthPlayer = count >= 4;

        player4RowPane.setVisible(hasFourthPlayer);
        player4RowPane.setManaged(hasFourthPlayer);

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
    private void onSaveGameClicked() {
        appendLog("Save game clicked.");
        // TODO: sambungkan ke persistence save ketika GameState sudah terhubung di controller.
    }

    @FXML
    private void onLoadGameClicked() {
        appendLog("Load game clicked.");
        try {
            BananaRepublic.showLoadGame();
        } catch (Exception e) {
            appendLog("Failed to open load game: " + e.getMessage());
        }
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
        InventoryDialogController.showDialog(playerIndex, getPlayerNameForIndex(playerIndex));
        appendLog("Opened inventory for player " + playerIndex + ".");
    }

    private String getPlayerNameForIndex(int playerIndex) {
        return switch (playerIndex) {
            case 1 -> player1NameLabel.getText();
            case 2 -> player2NameLabel.getText();
            case 3 -> player3NameLabel.getText();
            case 4 -> player4NameLabel.getText();
            default -> "Player " + playerIndex;
        };
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

