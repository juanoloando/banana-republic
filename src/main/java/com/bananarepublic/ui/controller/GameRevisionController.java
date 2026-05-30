package com.bananarepublic.ui.controller;

import com.bananarepublic.BananaRepublic;
import com.bananarepublic.session.GameSession;
import com.bananarepublic.ui.renderer.HexRevisionRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import model.board.*;
import model.card.CardCategory;
import model.player.Player;
import model.resource.ResourceType;
import persistence.SaveData;

import java.util.function.BiConsumer;

import java.util.*;

public class GameRevisionController {

    private static final double DESIGN_WIDTH  = 1920.0;
    private static final double DESIGN_HEIGHT = 1080.0;

    // ── FXML bindings ─────────────────────────────────────────────────
    @FXML private StackPane rootPane;
    @FXML private AnchorPane designPane;
    @FXML private Pane boardPane;

    // Top bar
    @FXML private Label turnLabel;
    @FXML private Label timerLabel;
    @FXML private Button logButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;

    // Player rows
    @FXML private Pane player1Row;
    @FXML private Pane player2Row;
    @FXML private Pane player3Row;
    @FXML private Pane player4Row;

    @FXML private ImageView player1ProfileImage;
    @FXML private ImageView player2ProfileImage;
    @FXML private ImageView player3ProfileImage;
    @FXML private ImageView player4ProfileImage;

    @FXML private Label player1NameLabel;
    @FXML private Label player2NameLabel;
    @FXML private Label player3NameLabel;
    @FXML private Label player4NameLabel;

    @FXML private Label player1VpLabel;
    @FXML private Label player2VpLabel;
    @FXML private Label player3VpLabel;
    @FXML private Label player4VpLabel;

    @FXML private Label player1OutpostLabel;
    @FXML private Label player2OutpostLabel;
    @FXML private Label player3OutpostLabel;
    @FXML private Label player4OutpostLabel;

    @FXML private Label player1LabLabel;
    @FXML private Label player2LabLabel;
    @FXML private Label player3LabLabel;
    @FXML private Label player4LabLabel;

    @FXML private Label player1KnightLabel;
    @FXML private Label player2KnightLabel;
    @FXML private Label player3KnightLabel;
    @FXML private Label player4KnightLabel;

    @FXML private Label player1PipeLabel;
    @FXML private Label player2PipeLabel;
    @FXML private Label player3PipeLabel;
    @FXML private Label player4PipeLabel;

    // Inventory
    @FXML private HBox inventoryResourceRow;
    @FXML private HBox inventoryCardRow;

    // Info panel (LOG / BANK)
    @FXML private StackPane infoPanelContainer;
    @FXML private Pane      logPanel;
    @FXML private ScrollPane logScrollPane;
    @FXML private VBox       logBox;
    @FXML private Pane      bankPanel;
    @FXML private HBox       bankCardRow;

    // Popup
    @FXML private Pane popupOverlay;
    @FXML private ImageView popupImageView;
    @FXML private Button popupCloseButton;

    // Build popups
    @FXML private Pane pipePopupPane;
    @FXML private Pane buildingPopupPane;

    // ── State ─────────────────────────────────────────────────────────
    private final HexRevisionRenderer hexRevisionRenderer = new HexRevisionRenderer();
    private final Map<ResourceType, Label> resourceBadgeLabels = new EnumMap<>(ResourceType.class);

    // Build mode state
    private enum BuildType { NONE, OUTPOST, LAB, PIPE }
    private BuildType pendingBuildType = BuildType.NONE;
    private int pendingBuildId = -1;
    private final Map<Integer, HBox> playerCardBoxes = new HashMap<>();
    private int playerCount = 4;
    private int activePlayerIndex = 0;
    private Font arcadeFont;
    private Font introFont;
    // Currently visible info panel ("log", "bank", or "" for none)
    private String activeInfoPanel = "";

    // ── Timer state ──────────────────────────────────────────────────
    private static final int DEFAULT_TURN_SECONDS = 90;
    private Timeline timerTimeline;
    private int remainingSeconds = DEFAULT_TURN_SECONDS;

    // ── Dice state ────────────────────────────────────────────────────
    private Timeline diceTimeline;
    private ImageView diceView1;
    private ImageView diceView2;
    private boolean diceRolled = false;
    private static final double DICE_SIZE   = 78.0;
    private static final double DICE_X1     = 608.0;  // position of die 1 on boardPane
    private static final double DICE_X2     = 700.0;  // position of die 2 on boardPane
    private static final double DICE_Y      = 868.0;

    // ── Lifecycle ────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupResponsiveScaling();
        loadFonts();
        applyFontsToLabels();
        loadPlayerNames();
        setupPlayerCardBoxes();
        renderHexBoard();
        initInventory();
        initBankStock();
        updatePhaseDisplay(false);
        hidePopup();
        startTurnTimer(DEFAULT_TURN_SECONDS);
    }

    private void setupResponsiveScaling() {
        designPane.scaleXProperty().bind(rootPane.widthProperty().divide(DESIGN_WIDTH));
        designPane.scaleYProperty().bind(rootPane.heightProperty().divide(DESIGN_HEIGHT));
    }

    private void loadFonts() {
        var arcadeStream = getClass().getResourceAsStream("/com/bananarepublic/fonts/ARCADE_N.TTF");
        var introStream  = getClass().getResourceAsStream("/com/bananarepublic/fonts/Intro Book Alt.otf");
        if (arcadeStream != null) arcadeFont = Font.loadFont(arcadeStream, 14);
        if (introStream  != null) introFont  = Font.loadFont(introStream,  14);
    }

    private void applyFontsToLabels() {
        if (arcadeFont != null) {
            String family = arcadeFont.getFamily();
            setFontFamily(turnLabel,   family, 27);
            setFontFamily(logButton,   family, 14);
            setFontFamily(saveButton,  family, 14);
            setFontFamily(loadButton,  family, 14);
        }
        if (introFont != null && timerLabel != null) {
            setFontFamily(timerLabel, introFont.getFamily(), 26);
        }
    }

    private void setFontFamily(javafx.scene.Node node, String family, double size) {
        if (node instanceof Label l) l.setFont(Font.font(family, size));
        else if (node instanceof Button b) b.setFont(Font.font(family, size));
    }

    // ── Player names ─────────────────────────────────────────────────
    private void loadPlayerNames() {
        List<String> names = GameSession.getPlayerNames();
        String[] defaults = {"Player 1", "Player 2", "Player 3", "Player 4"};
        Label[] nameLabels = {player1NameLabel, player2NameLabel,
                              player3NameLabel, player4NameLabel};
        ImageView[] profiles = {player1ProfileImage, player2ProfileImage,
                                player3ProfileImage, player4ProfileImage};
        String[] profilePaths = {
            "/com/bananarepublic/images/game_revision/profile1.png",
            "/com/bananarepublic/images/game_revision/profile2.png",
            "/com/bananarepublic/images/game_revision/profile3.png",
            "/com/bananarepublic/images/game_revision/profile4.png"
        };

        for (int i = 0; i < 4; i++) {
            String name = names.size() > i ? names.get(i) : defaults[i];
            nameLabels[i].setText(name);
            if (arcadeFont != null) nameLabels[i].setFont(Font.font(arcadeFont.getFamily(), 10));
            try {
                profiles[i].setImage(new Image(
                    getClass().getResource(profilePaths[i]).toExternalForm()));
            } catch (Exception ignored) {}
        }

        int count = names.isEmpty() ? 4 : names.size();
        configurePlayerCount(count);
        updateTurnDisplay(names.isEmpty() ? defaults[0] : names.get(0));
    }

    public void configurePlayerCount(int count) {
        this.playerCount = count;
        boolean show4 = count >= 4;
        player4Row.setVisible(show4);
        player4Row.setManaged(show4);
    }

    public void updateTurnDisplay(String playerName) {
        turnLabel.setText(playerName.toUpperCase());
        // Let the label grow to fit the text — prefWidth in FXML may be too narrow
        turnLabel.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        turnLabel.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
    }

    public void updatePhaseDisplay(boolean isTrade) {
        // phaseLabel removed — phase indicated by background image only
    }

    // ── Board rendering ──────────────────────────────────────────────
    private void renderHexBoard() {
        SaveData loadedSaveData = GameSession.getLoadedSaveData();

        if (loadedSaveData != null
                && loadedSaveData.mapConfig != null
                && !loadedSaveData.mapConfig.isEmpty()) {

            List<HexTile> tiles = new ArrayList<>();
            for (SaveData.TileData tileData : loadedSaveData.mapConfig) {
                TerrainType tt = parseTerrainType(tileData.terrainType);
                if (tt == null) continue;
                tiles.add(new HexTile(new HexCoordinate(tileData.q, tileData.r),
                                      tt, tileData.tokenNumber));
            }
            HexCoordinate robberPos =
                    new HexCoordinate(loadedSaveData.robberQ, loadedSaveData.robberR);

            Board board = new StandardMapGenerator().generateBoard();
            hexRevisionRenderer.render(boardPane, tiles, robberPos, board);
            return;
        }

        Board board = new StandardMapGenerator().generateBoard();
        HexCoordinate robberPos = board.getAllTiles().stream()
                .filter(t -> t.getTerrainType() == TerrainType.DESERT)
                .map(HexTile::getCoordinate)
                .findFirst()
                .orElse(new HexCoordinate(0, 0));

        hexRevisionRenderer.render(boardPane, board.getAllTiles(), robberPos, board);
    }

    private TerrainType parseTerrainType(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return TerrainType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ── Inventory & Player Status Cards ──────────────────────────────

    /** Adds a small HBox to each player row Pane for their card display. */
    private void setupPlayerCardBoxes() {
        Pane[] rows = {player1Row, player2Row, player3Row, player4Row};
        for (int i = 0; i < rows.length; i++) {
            HBox box = new HBox(3);
            box.setLayoutX(310);
            box.setLayoutY(44);
            box.setPrefHeight(44);
            box.setAlignment(Pos.CENTER_LEFT);
            rows[i].getChildren().add(box);
            playerCardBoxes.put(i + 1, box);
        }
    }

    /** Called during initialize. Starts empty for new game; loads from SaveData if available. */
    private void initInventory() {
        inventoryResourceRow.getChildren().clear();
        inventoryCardRow.getChildren().clear();
        resourceBadgeLabels.clear();

        SaveData saveData = GameSession.getLoadedSaveData();
        if (saveData != null && saveData.players != null) {
            int limit = Math.min(saveData.players.size(), 4);
            for (int i = 0; i < limit; i++) {
                SaveData.PlayerData pd = saveData.players.get(i);
                if (i == 0) {
                    // Active player inventory (first player for now)
                    refreshExperimentInventory(
                        pd.knightCardsInHand,
                        pd.progressCardsInHand,
                        pd.victoryPointCardsInHand
                    );
                }
                refreshPlayerStatusCards(i + 1,
                    pd.knightCardsInHand,
                    pd.progressCardsInHand,
                    pd.victoryPointCardsInHand
                );
            }
        }
        // Resources start empty — engine will call refreshInventory() when distributed
    }

    // ── Public API — called by bridge layer / GameEngineAdapter ──────

    /**
     * Refresh active player's resource inventory from a Player object.
     * Call from FX thread or wrap with Platform.runLater.
     */
    public void refreshInventory(Player player) {
        Map<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            int count = player.getResourceCount(type);
            if (count > 0) counts.put(type, count);
        }
        Platform.runLater(() -> refreshResourceCards(counts));
    }

    /**
     * Rebuild resource inventory row — shows only cards with count > 0,
     * hides cards that reach 0 (traded/used).
     */
    public void refreshResourceCards(Map<ResourceType, Integer> counts) {
        inventoryResourceRow.getChildren().clear();
        resourceBadgeLabels.clear();
        for (ResourceType type : ResourceType.values()) {
            int count = counts.getOrDefault(type, 0);
            if (count > 0) {
                StackPane card = buildResourceCard(type, count);
                resourceBadgeLabels.put(type, (Label) card.getUserData());
                inventoryResourceRow.getChildren().add(card);
            }
        }
    }

    /**
     * Rebuild experiment/special card row — shows only cards with count > 0.
     * Call from FX thread or wrap with Platform.runLater.
     */
    public void refreshExperimentInventory(int knightCards, int progressCards, int vpCards) {
        inventoryCardRow.getChildren().clear();
        addExperimentToInventory(knightCards,   CardCategory.KNIGHT);
        addExperimentToInventory(progressCards, CardCategory.PROGRESS);
        addExperimentToInventory(vpCards,       CardCategory.VICTORY_POINT);
    }

    private void addExperimentToInventory(int count, CardCategory category) {
        if (count <= 0) return;
        inventoryCardRow.getChildren().add(buildExperimentCard(category, count));
    }

    /**
     * Refresh card display in a player's status row (index 1-based).
     * Shows/hides small card thumbnails based on count.
     * Call from FX thread or wrap with Platform.runLater.
     */
    public void refreshPlayerStatusCards(int playerIndex,
                                          int knightCards, int progressCards, int vpCards) {
        HBox box = playerCardBoxes.get(playerIndex);
        if (box == null) return;
        box.getChildren().clear();
        addStatusCard(box, knightCards,   CardCategory.KNIGHT);
        addStatusCard(box, progressCards, CardCategory.PROGRESS);
        addStatusCard(box, vpCards,       CardCategory.VICTORY_POINT);
    }

    private void addStatusCard(HBox box, int count, CardCategory category) {
        if (count <= 0) return;
        StackPane card = buildSmallCard(category, count);
        box.getChildren().add(card);
    }

    /** Small card variant for player status rows. */
    private StackPane buildSmallCard(CardCategory category, int count) {
        ImageView img = new ImageView(loadCardImage(category));
        img.setFitWidth(26);
        img.setFitHeight(36);
        img.setPreserveRatio(false);
        img.setSmooth(true);

        StackPane root = new StackPane(img);
        root.setPrefSize(26, 36);
        root.setMaxSize(26, 36);

        if (count > 1) {
            Label badge = new Label(String.valueOf(count));
            badge.getStyleClass().add("gr-badge-label");
            if (arcadeFont != null) badge.setFont(Font.font(arcadeFont.getFamily(), 9));
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            badge.setTranslateX(2);
            badge.setTranslateY(2);
            root.getChildren().add(badge);
        }
        return root;
    }

    /**
     * Resource card: inventory PNG (which already contains the full card design)
     * overlaid with a name label at the bottom and optional pentagon badge at top-left.
     * card.getUserData() → badge Label for count updates.
     */
    private StackPane buildResourceCard(ResourceType type, int count) {
        // The inventory PNG is the card itself (89×139, with rounded corners)
        ImageView cardImage = new ImageView(loadInventoryImage(type));
        cardImage.setFitWidth(86);
        cardImage.setFitHeight(134);
        cardImage.setPreserveRatio(false);
        cardImage.setSmooth(true);

        // Name label overlaid at the bottom of the card
        Label nameLabel = new Label(resourceDisplayName(type));
        nameLabel.getStyleClass().add("gr-resource-name");
        if (arcadeFont != null) nameLabel.setFont(Font.font(arcadeFont.getFamily(), 10));
        nameLabel.setStyle("-fx-text-fill: #f5e8c0; -fx-font-weight: bold;");

        // Count badge — plain text, top-left, visible only when count > 1
        Label badge = new Label(count > 1 ? String.valueOf(count) : "");
        badge.setVisible(count > 1);
        badge.getStyleClass().add("gr-badge-label");
        if (arcadeFont != null) badge.setFont(Font.font(arcadeFont.getFamily(), 10));

        StackPane root = new StackPane();
        root.setPrefSize(86, 134);  // card size — edit here
        root.setMaxSize(86, 134);

        root.getChildren().add(cardImage);

        // Name overlaid at bottom-center of card
        StackPane.setAlignment(nameLabel, Pos.BOTTOM_CENTER);
        nameLabel.setStyle(nameLabel.getStyle() + " -fx-padding: 0 0 6px 0;");
        root.getChildren().add(nameLabel);

        // Count text at top-left
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        badge.setTranslateX(5);  // horizontal offset from card left edge
        badge.setTranslateY(4);  // vertical offset from card top edge
        root.getChildren().add(badge);

        root.setUserData(badge);
        return root;
    }

    private StackPane buildExperimentCard(CardCategory category, int count) {
        ImageView cardImage = new ImageView(loadCardImage(category));
        cardImage.setFitWidth(72);   // experiment card width — edit here
        cardImage.setFitHeight(100); // experiment card height — edit here
        cardImage.setPreserveRatio(false);
        cardImage.setSmooth(true);

        StackPane root = new StackPane();
        root.setPrefSize(72, 100);
        root.setMaxSize(72, 100);
        root.getChildren().add(cardImage);

        if (count > 1) {
            Label badge = new Label(String.valueOf(count));
            badge.getStyleClass().add("gr-badge-label");
            if (arcadeFont != null) badge.setFont(Font.font(arcadeFont.getFamily(), 10));
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            badge.setTranslateX(4);  // horizontal offset
            badge.setTranslateY(3);  // vertical offset
            root.getChildren().add(badge);
        }

        return root;
    }

    private Image loadInventoryImage(ResourceType type) {
        String path = switch (type) {
            case WOOD   -> "/com/bananarepublic/images/game_revision/inventory_log.png";
            case BRICK  -> "/com/bananarepublic/images/game_revision/inventory_break.png";
            case WHEAT  -> "/com/bananarepublic/images/game_revision/inventory_wheat.png";
            case ORE    -> "/com/bananarepublic/images/game_revision/inventory_ore.png";
            case BANANA -> "/com/bananarepublic/images/game_revision/inventory_banana.png";
        };
        return new Image(getClass().getResource(path).toExternalForm());
    }

    private Image loadCardImage(CardCategory category) {
        String path = switch (category) {
            case KNIGHT        -> "/com/bananarepublic/images/game_revision/card_knight.png";
            case PROGRESS      -> "/com/bananarepublic/images/game_revision/card_progress.png";
            case VICTORY_POINT -> "/com/bananarepublic/images/game_revision/card_vp.png";
            default            -> "/com/bananarepublic/images/game_revision/card_progress.png";
        };
        return new Image(getClass().getResource(path).toExternalForm());
    }

    private String resourceDisplayName(ResourceType type) {
        return switch (type) {
            case WOOD   -> "log";
            case BRICK  -> "brick";
            case WHEAT  -> "wheat";
            case ORE    -> "ore";
            case BANANA -> "banana";
        };
    }

    // ── Info Panel (LOG / BANK) ──────────────────────────────────────

    /** Toggle a named panel. If already active, close it. */
    private void toggleInfoPanel(String name) {
        if (activeInfoPanel.equals(name)) {
            showInfoPanel("");
        } else {
            showInfoPanel(name);
        }
    }

    private void showInfoPanel(String name) {
        activeInfoPanel = name;
        setNodeVisible(logPanel,  "log".equals(name));
        setNodeVisible(bankPanel, "bank".equals(name));
    }

    private void setNodeVisible(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // ── LOG panel ────────────────────────────────────────────────────

    /**
     * Append a log entry. Can be called from any thread.
     * Format mirrors the backend game log.
     */
    public void appendLog(String message) {
        Platform.runLater(() -> {
            Label entry = new Label(message);
            entry.setWrapText(true);
            entry.getStyleClass().add("gr-log-entry");
            if (arcadeFont != null) entry.setFont(Font.font(arcadeFont.getFamily(), 10));
            logBox.getChildren().add(entry);
            // Auto-scroll to bottom
            logScrollPane.layout();
            logScrollPane.setVvalue(1.0);
        });
    }

    // ── BANK panel ───────────────────────────────────────────────────

    // Bank stock: starts at 19 each (ResourceBank.INITIAL_COUNT)
    private final Map<ResourceType, Integer> bankStock = new EnumMap<>(ResourceType.class);

    private void initBankStock() {
        for (ResourceType type : ResourceType.values()) {
            bankStock.put(type, 19); // ResourceBank.INITIAL_COUNT
        }
    }

    /** Rebuild bank card row — cards with count > 0 shown, count = 0 hidden (bank ran out). */
    private void buildBankCards() {
        bankCardRow.getChildren().clear();
        for (ResourceType type : ResourceType.values()) {
            int count = bankStock.getOrDefault(type, 0);
            if (count > 0) {
                bankCardRow.getChildren().add(buildResourceCard(type, count));
            }
        }
    }

    /**
     * Refresh bank panel from a Bank object (model.resource.Bank).
     * Call from FX thread or wrap with Platform.runLater.
     */
    public void refreshBankPanel(model.resource.Bank bank) {
        for (ResourceType type : ResourceType.values()) {
            bankStock.put(type, bank.getAvailable(type));
        }
        Platform.runLater(() -> {
            if ("bank".equals(activeInfoPanel)) buildBankCards();
        });
    }

    /**
     * Refresh bank panel from a stock map (ResourceType → count).
     * Useful when Bank object is not directly available.
     */
    public void refreshBankPanel(Map<ResourceType, Integer> stock) {
        bankStock.putAll(stock);
        Platform.runLater(() -> {
            if ("bank".equals(activeInfoPanel)) buildBankCards();
        });
    }

    // ── Timer ────────────────────────────────────────────────────────

    /**
     * Start (or restart) the turn countdown timer.
     * Called automatically at turn start and by bridge layer.
     */
    public void startTurnTimer(int seconds) {
        stopTimer();
        remainingSeconds = seconds;
        updateTimerDisplay();
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds <= 0) {
                stopTimer();
                appendLog("⏱ Waktu habis — giliran berakhir otomatis.");
                performEndTurn();
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    public void stopTimer() {
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
    }

    private void updateTimerDisplay() {
        if (timerLabel == null) return;
        int m = remainingSeconds / 60;
        int s = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
    }

    // ── Dice ─────────────────────────────────────────────────────────

    private void startDiceAnimation() {
        setupDiceViews();
        diceRolled = true;

        // 80 ms per frame × 25 frames ≈ 2 seconds of animation
        diceTimeline = new Timeline(new KeyFrame(Duration.millis(80), e -> {
            if (diceView1 != null) diceView1.setImage(loadDiceImage(randomFace()));
            if (diceView2 != null) diceView2.setImage(loadDiceImage(randomFace()));
        }));
        diceTimeline.setCycleCount(25);
        diceTimeline.setOnFinished(e -> finalizeDiceRoll());
        diceTimeline.play();
    }

    private void setupDiceViews() {
        clearDiceDisplay();
        diceView1 = makeDiceView(DICE_X1, DICE_Y);
        diceView2 = makeDiceView(DICE_X2, DICE_Y);
        diceView1.setImage(loadDiceImage(1));
        diceView2.setImage(loadDiceImage(1));
        boardPane.getChildren().addAll(diceView1, diceView2);
    }

    private ImageView makeDiceView(double x, double y) {
        ImageView iv = new ImageView();
        iv.setFitWidth(DICE_SIZE);
        iv.setFitHeight(DICE_SIZE);
        iv.setLayoutX(x);
        iv.setLayoutY(y);
        iv.setSmooth(true);
        iv.setUserData("dice-display");
        return iv;
    }

    private void finalizeDiceRoll() {
        int f1 = randomFace();
        int f2 = randomFace();
        if (diceView1 != null) diceView1.setImage(loadDiceImage(f1));
        if (diceView2 != null) diceView2.setImage(loadDiceImage(f2));
        appendLog("🎲 " + f1 + " + " + f2 + " = " + (f1 + f2));
        // TODO: notify bridge layer → GameEngine.rollDice(f1, f2)
    }

    private void clearDiceDisplay() {
        boardPane.getChildren().removeIf(n -> "dice-display".equals(n.getUserData()));
        diceView1 = null;
        diceView2 = null;
    }

    private static int randomFace() {
        return (int) (Math.random() * 6) + 1;
    }

    private Image loadDiceImage(int face) {
        String path = "/com/bananarepublic/images/game_revision/dice" + face + ".png";
        return new Image(getClass().getResource(path).toExternalForm());
    }

    // ── End Turn ─────────────────────────────────────────────────────

    /** Core end-turn logic: stop timer, clear state, advance player, restart timer. */
    private void performEndTurn() {
        stopTimer();
        exitBuildMode();
        clearDiceDisplay();
        diceRolled = false;
        hideBuildPopup();
        advanceToNextPlayer();
        startTurnTimer(DEFAULT_TURN_SECONDS);
    }

    private void advanceToNextPlayer() {
        List<String> names = GameSession.getPlayerNames();
        if (names.isEmpty()) return;
        activePlayerIndex = (activePlayerIndex + 1) % names.size();
        String nextName = names.get(activePlayerIndex);
        updateTurnDisplay(nextName);
        updatePhaseDisplay(false);

        // Clear inventory for next player (engine will populate via refreshInventory)
        inventoryResourceRow.getChildren().clear();
        inventoryCardRow.getChildren().clear();
        resourceBadgeLabels.clear();

        appendLog("─── Giliran " + nextName + " ───");
    }

    // ── Build mode ───────────────────────────────────────────────────

    /**
     * Shows red circles on all valid build locations for the current player.
     * Called by the bridge layer when it's the player's TRADE_BUILD phase.
     *
     * @param settlementIds  intersections where a new outpost can be placed
     * @param labIds         intersections where an existing outpost can be upgraded to lab
     * @param pipeIds        path IDs where a pipe can be placed
     * @param board          current board state
     */
    public void enterBuildMode(Set<Integer> settlementIds,
                                Set<Integer> labIds,
                                Set<Integer> pipeIds,
                                Board board) {
        hexRevisionRenderer.renderBuildableLocations(
            boardPane, settlementIds, labIds, pipeIds, board,
            this::onIntersectionCircleClicked,
            this::onPathCircleClicked
        );
    }

    /** Removes all build circles and closes any open build popup. */
    public void exitBuildMode() {
        hexRevisionRenderer.clearBuildableLocations(boardPane);
        hideBuildPopup();
        pendingBuildType = BuildType.NONE;
        pendingBuildId   = -1;
    }

    private void onIntersectionCircleClicked(int intersectionId, boolean isLab) {
        pendingBuildType = isLab ? BuildType.LAB : BuildType.OUTPOST;
        pendingBuildId   = intersectionId;
        showBuildPopup(false); // building popup
    }

    private void onPathCircleClicked(int pathId) {
        pendingBuildType = BuildType.PIPE;
        pendingBuildId   = pathId;
        showBuildPopup(true); // pipe popup
    }

    private void showBuildPopup(boolean isPipe) {
        hideBuildPopup();
        Pane popup = isPipe ? pipePopupPane : buildingPopupPane;
        popup.setVisible(true);
        popup.setManaged(true);
        popup.toFront();
    }

    private void hideBuildPopup() {
        pipePopupPane.setVisible(false);
        pipePopupPane.setManaged(false);
        buildingPopupPane.setVisible(false);
        buildingPopupPane.setManaged(false);
    }

    @FXML
    private void onCloseBuildPopup() {
        hideBuildPopup();
        pendingBuildType = BuildType.NONE;
        pendingBuildId   = -1;
        // Build circles remain — player can still choose to build something else
    }

    /**
     * Called when the BUILD button inside a popup is clicked.
     * Delegates to the bridge layer via the appropriate GameEngine call.
     * Override/extend this when connecting to the engine.
     */
    @FXML
    public void onBuildConfirmed() {
        switch (pendingBuildType) {
            case OUTPOST -> appendLog("Build outpost at intersection " + pendingBuildId + " — TODO: connect to GameEngine.buildSettlement()");
            case LAB     -> appendLog("Build lab at intersection " + pendingBuildId     + " — TODO: connect to GameEngine.upgradeLaboratory()");
            case PIPE    -> appendLog("Build pipe at path "        + pendingBuildId     + " — TODO: connect to GameEngine.buildPipe()");
            default      -> {}
        }
        hideBuildPopup();
        // Circles are cleared after a successful build by the engine callback;
        // for now just exit build mode to prevent duplicate builds.
        exitBuildMode();
    }

    // ── Popup ────────────────────────────────────────────────────────
    private void hidePopup() {
        popupOverlay.setVisible(false);
        popupOverlay.setManaged(false);
        popupImageView.setVisible(false);
        popupImageView.setManaged(false);
        popupCloseButton.setVisible(false);
        popupCloseButton.setManaged(false);
    }

    private void showPopupImage(String imagePath) {
        try {
            popupImageView.setImage(
                new Image(getClass().getResource(imagePath).toExternalForm()));
        } catch (Exception ignored) {}
        popupOverlay.setVisible(true);
        popupOverlay.setManaged(true);
        popupImageView.setVisible(true);
        popupImageView.setManaged(true);
        popupCloseButton.setVisible(true);
        popupCloseButton.setManaged(true);
        popupOverlay.toFront();
        popupImageView.toFront();
        popupCloseButton.toFront();
    }

    // ── Button handlers ──────────────────────────────────────────────
    @FXML
    private void onRollDiceClicked() {
        if (diceRolled && (diceTimeline == null || diceTimeline.getStatus() == Animation.Status.STOPPED)) {
            return; // already rolled and animation finished — ignore
        }
        if (diceTimeline != null && diceTimeline.getStatus() == Animation.Status.RUNNING) {
            return; // animation already running
        }
        startDiceAnimation();
    }
    @FXML private void onTradeClicked()    { TradeDialogController.showDialog(); }
    @FXML private void onBuyCardClicked()  { /* TODO: connect to GameEngine */ }

    @FXML
    private void onLogClicked() {
        toggleInfoPanel("log");
    }

    @FXML
    private void onBankClicked() {
        if (!activeInfoPanel.equals("bank")) {
            buildBankCards();
        }
        toggleInfoPanel("bank");
    }
    @FXML private void onDeclareVictoryClicked() { /* TODO: victory check */ }
    @FXML
    public void onEndTurnClicked() {
        // If dice is still spinning, stop immediately and resolve
        if (diceTimeline != null && diceTimeline.getStatus() == Animation.Status.RUNNING) {
            diceTimeline.stop();
            finalizeDiceRoll();
        }
        performEndTurn();
    }

    @FXML
    private void onSaveClicked() {
        /* TODO: connect to SaveLoadService */
    }

    @FXML
    private void onLoadGameClicked() {
        try {
            BananaRepublic.showLoadGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClosePopupClicked() {
        hidePopup();
    }

    @FXML
    private void onExitToMainMenuClicked() {
        hidePopup();
        try {
            BananaRepublic.showMainMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
