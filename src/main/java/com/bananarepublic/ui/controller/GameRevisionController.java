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
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import model.board.*;
import model.card.CardCategory;
import model.player.Player;
import model.resource.ResourceType;
import persistence.SaveData;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
    @FXML private Button diceButton;
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
    @FXML private StackPane tradeOverlayRoot;
    @FXML private StackPane tradeOverlayCenterLayer;
    @FXML private Group tradeModalScaleGroup;
    @FXML private Pane popupOverlay;
    @FXML private Pane tradePopupContainer;
    @FXML private ImageView popupImageView;
    @FXML private Button popupCloseButton;
    @FXML private Button tradeChoiceBankButton;
    @FXML private Button tradeChoicePlayerButton;
    @FXML private Button tradeChoiceHarborButton;
    @FXML private AnchorPane tradeUiPane;
    @FXML private Label tradeErrorLabel;
    @FXML private Label playerTradeMessageLabel;
    @FXML private HBox tradeActionButtonsBox;
    @FXML private Label bankTradeMessageLabel;
    @FXML private Label bankBananaQtyLabel;
    @FXML private Label bankWoodQtyLabel;
    @FXML private Label bankWheatQtyLabel;
    @FXML private Label bankOreQtyLabel;
    @FXML private Label bankBrickQtyLabel;
    @FXML private Label youBananaQtyLabel;
    @FXML private Label youWoodQtyLabel;
    @FXML private Label youWheatQtyLabel;
    @FXML private Label youOreQtyLabel;
    @FXML private Label youBrickQtyLabel;
    @FXML private Label bankBananaCheck;
    @FXML private Label bankWoodCheck;
    @FXML private Label bankWheatCheck;
    @FXML private Label bankOreCheck;
    @FXML private Label bankBrickCheck;
    @FXML private Label youBananaCheck;
    @FXML private Label youWoodCheck;
    @FXML private Label youWheatCheck;
    @FXML private Label youOreCheck;
    @FXML private Label youBrickCheck;
    @FXML private Button bankBananaSelectButton;
    @FXML private Button bankWoodSelectButton;
    @FXML private Button bankWheatSelectButton;
    @FXML private Button bankOreSelectButton;
    @FXML private Button bankBrickSelectButton;
    @FXML private Button youBananaSelectButton;
    @FXML private Button youWoodSelectButton;
    @FXML private Button youWheatSelectButton;
    @FXML private Button youOreSelectButton;
    @FXML private Button youBrickSelectButton;
    @FXML private Button bankTradeCancelButton;
    @FXML private Button bankTradeConfirmButton;
    @FXML private StackPane buyExperimentOverlay;
    @FXML private BuyExperimentCardOverlayController buyExperimentOverlayController;
    @FXML private StackPane rollDiceManualOverlay;
    @FXML private RollDiceManualOverlayController rollDiceManualOverlayController;
    @FXML private StackPane declareVictoryOverlay;
    @FXML private DeclareVictoryOverlayController declareVictoryOverlayController;

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
    private enum DiceRollMode { NONE, AUTOMATIC, MANUAL }
    private DiceRollMode diceRollMode = DiceRollMode.NONE;
    private static final double DICE_SIZE   = 78.0;
    private static final double DICE_X1     = 608.0;  // position of die 1 on boardPane
    private static final double DICE_X2     = 700.0;  // position of die 2 on boardPane
    private static final double DICE_Y      = 868.0;
    private static final double POPUP_ASSET_WIDTH = 1048.0;
    private static final double POPUP_ASSET_HEIGHT = 753.0;
    private static final double POPUP_CLOSE_CENTER_X = 959.0;
    private static final double POPUP_CLOSE_CENTER_Y = 153.0;
    private static final double POPUP_CLOSE_HITBOX_SIZE = 60.0;
    private static final double TRADE_CHOICE_BANK_X = 228.0;
    private static final double TRADE_CHOICE_BANK_Y = 383.0;
    private static final double TRADE_CHOICE_BANK_W = 175.0;
    private static final double TRADE_CHOICE_BANK_H = 74.0;
    private static final double TRADE_CHOICE_PLAYER_X = 457.0;
    private static final double TRADE_CHOICE_PLAYER_Y = 381.0;
    private static final double TRADE_CHOICE_PLAYER_W = 175.0;
    private static final double TRADE_CHOICE_PLAYER_H = 75.0;
    private static final double TRADE_CHOICE_HARBOR_X = 674.0;
    private static final double TRADE_CHOICE_HARBOR_Y = 384.0;
    private static final double TRADE_CHOICE_HARBOR_W = 173.0;
    private static final double TRADE_CHOICE_HARBOR_H = 73.0;
    private static final double BANK_POPUP_ASSET_WIDTH = 887.0;
    private static final double BANK_POPUP_ASSET_HEIGHT = 1121.0;
    private static final double BANK_RESOURCE_CENTER_X_BASE = 168.0;
    private static final double[] BANK_RESOURCE_CENTER_X_STEPS = {0.0, 142.0, 277.0, 418.0, 563.0};
    private static final double BANK_CARD_WIDTH = 120.0;
    private static final double BANK_CARD_HEIGHT = 182.0;
    private static final double BANK_TOP_CARD_Y = 224.0;
    private static final double BANK_BOTTOM_CARD_Y = 716.0;
    private static final double BANK_CARD_QTY_OFFSET_Y = 137.0;
    private static final double BANK_CANCEL_BUTTON_X = 231.0;
    private static final double BANK_CANCEL_BUTTON_Y = 995.0;
    private static final double BANK_CANCEL_BUTTON_W = 198.0;
    private static final double BANK_CANCEL_BUTTON_H = 52.0;
    private static final double BANK_CONFIRM_BUTTON_X = 458.0;
    private static final double BANK_CONFIRM_BUTTON_Y = 995.0;
    private static final double BANK_CONFIRM_BUTTON_W = 198.0;
    private static final double BANK_CONFIRM_BUTTON_H = 52.0;
    private static final double BANK_MESSAGE_X = 140.0;
    private static final double BANK_MESSAGE_Y = 940.0;
    private static final double BANK_MESSAGE_W = 610.0;
    private static final double BANK_MESSAGE_H = 30.0;
    private static final double BANK_TRADE_X_OFFSET = 0.0;
    private static final double BANK_CARD_SELECT_W = 96.0;
    private static final double BANK_CARD_SELECT_H = 140.0;
    private static final double BANK_CIRCLE_Y_TOP = 409.0;
    private static final double BANK_CIRCLE_Y_BOTTOM = 900.0;
    private static final double BANK_CIRCLE_W = 30.0;
    private static final double BANK_CIRCLE_H = 30.0;
    private static final double BANK_QTY_Y_TOP = 366.0;
    private static final double BANK_QTY_Y_BOTTOM = 859.0;
    private static final double BANK_QTY_W = 34.0;
    private static final double BANK_QTY_H = 20.0;
    private static final double PLAYER_POPUP_ASSET_WIDTH = 889.0;
    private static final double PLAYER_POPUP_ASSET_HEIGHT = 1142.0;
    private static final double PLAYER_RESOURCE_CENTER_X_BASE = 130.0;
    private static final double[] PLAYER_RESOURCE_CENTER_X_STEPS = {0.0, 142.0, 277.0, 418.0, 563.0};
    private static final double PLAYER_CARD_WIDTH = 96.0;
    private static final double PLAYER_CARD_HEIGHT = 140.0;
    private static final double PLAYER_GIVE_CARD_Y = 232.0;
    private static final double PLAYER_RECEIVE_CARD_Y = 544.0;
    private static final double PLAYER_CONTROL_Y_GIVE = 406.0;
    private static final double PLAYER_CONTROL_Y_RECEIVE = 718.0;
    private static final double PLAYER_TARGET_ROW_Y = 830.0;
    private static final double PLAYER_TARGET_ROW_START_X = 82.0;
    private static final double PLAYER_TARGET_CARD_W = 122.0;
    private static final double PLAYER_TARGET_CARD_H = 118.0;
    private static final double PLAYER_TARGET_GAP = 24.0;
    private static final double PLAYER_TARGET_IMAGE_W = 66.0;
    private static final double PLAYER_TARGET_IMAGE_H = 66.0;
    private static final double PLAYER_QTY_BUTTON_W = 30.0;
    private static final double PLAYER_QTY_BUTTON_H = 30.0;
    private static final double PLAYER_QTY_LABEL_W = 40.0;
    private static final double PLAYER_QTY_LABEL_H = 30.0;
    private static final double PLAYER_QTY_CONTROL_GAP = 8.0;
    private static final double PLAYER_MESSAGE_W = 500.0;
    private static final double PLAYER_MESSAGE_H = 32.0;
    private static final double PLAYER_MESSAGE_GAP_ABOVE_BUTTONS = 42.0;
    private static final int PLAYER_RECEIVE_DEFAULT_MAX_WITHOUT_TARGET = 19;
    private static final double COUNTER_POPUP_ASSET_WIDTH = 897.0;
    private static final double COUNTER_POPUP_ASSET_HEIGHT = 1134.0;
    private static final double COUNTER_RESOURCE_CENTER_X_BASE = 130.0;
    private static final double[] COUNTER_RESOURCE_CENTER_X_STEPS = {0.0, 142.0, 277.0, 418.0, 563.0};
    private static final double COUNTER_CARD_WIDTH = 96.0;
    private static final double COUNTER_CARD_HEIGHT = 140.0;
    private static final double COUNTER_GIVE_CARD_Y = 238.0;
    private static final double COUNTER_RECEIVE_CARD_Y = 550.0;
    private static final double COUNTER_CONTROL_Y_GIVE = 412.0;
    private static final double COUNTER_CONTROL_Y_RECEIVE = 724.0;
    private static final double COUNTER_RESPOND_TO_CARD_X = 102.0;
    private static final double COUNTER_RESPOND_TO_CARD_Y = 838.0;
    private static final double COUNTER_RESPOND_TO_CARD_W = 122.0;
    private static final double COUNTER_RESPOND_TO_CARD_H = 118.0;
    private static final double COUNTER_RESPOND_TO_IMAGE_W = 66.0;
    private static final double COUNTER_RESPOND_TO_IMAGE_H = 66.0;
    private static final double COUNTER_QTY_BUTTON_W = 30.0;
    private static final double COUNTER_QTY_BUTTON_H = 30.0;
    private static final double COUNTER_QTY_LABEL_W = 40.0;
    private static final double COUNTER_QTY_LABEL_H = 30.0;
    private static final double COUNTER_QTY_CONTROL_GAP = 8.0;
    private static final double COUNTER_MESSAGE_W = 500.0;
    private static final double COUNTER_MESSAGE_H = 32.0;
    private static final double COUNTER_MESSAGE_GAP_ABOVE_BUTTONS = 42.0;
    private static final double COUNTER_CANCEL_BUTTON_X = 231.0;
    private static final double COUNTER_CANCEL_BUTTON_Y = 995.0;
    private static final double COUNTER_CANCEL_BUTTON_W = 198.0;
    private static final double COUNTER_CANCEL_BUTTON_H = 52.0;
    private static final double COUNTER_SEND_BUTTON_X = 458.0;
    private static final double COUNTER_SEND_BUTTON_Y = 995.0;
    private static final double COUNTER_SEND_BUTTON_W = 198.0;
    private static final double COUNTER_SEND_BUTTON_H = 52.0;
    private static final double HARBOR_POPUP_ASSET_WIDTH = 890.0;
    private static final double HARBOR_POPUP_ASSET_HEIGHT = 1124.0;
    private static final double HARBOR_BUTTON_START_X = 72.0;
    private static final double HARBOR_BUTTON_Y = 258.0;
    private static final double HARBOR_BUTTON_W = 116.0;
    private static final double HARBOR_BUTTON_H = 48.0;
    private static final double HARBOR_BUTTON_GAP = 10.0;
    private static final double HARBOR_RESOURCE_CENTER_X_BASE = 130.0;
    private static final double[] HARBOR_RESOURCE_CENTER_X_STEPS = {0.0, 142.0, 277.0, 418.0, 563.0};
    private static final double HARBOR_CARD_WIDTH = 96.0;
    private static final double HARBOR_CARD_HEIGHT = 140.0;
    private static final double HARBOR_GIVE_CARD_Y = 392.0;
    private static final double HARBOR_RECEIVE_CARD_Y = 696.0;
    private static final double HARBOR_CARD_QTY_OFFSET_Y = 146.0;
    private static final double HARBOR_CHECK_Y_GIVE = 573.0;
    private static final double HARBOR_CHECK_Y_RECEIVE = 877.0;
    private static final double HARBOR_CHECK_W = 30.0;
    private static final double HARBOR_CHECK_H = 30.0;
    private static final double HARBOR_CANCEL_BUTTON_X = 399.0;
    private static final double HARBOR_CANCEL_BUTTON_Y = 1001.0;
    private static final double HARBOR_CANCEL_BUTTON_W = 199.0;
    private static final double HARBOR_CANCEL_BUTTON_H = 52.0;
    private static final double HARBOR_CONFIRM_BUTTON_X = 627.0;
    private static final double HARBOR_CONFIRM_BUTTON_Y = 1001.0;
    private static final double HARBOR_CONFIRM_BUTTON_W = 199.0;
    private static final double HARBOR_CONFIRM_BUTTON_H = 52.0;
    private static final double HARBOR_MESSAGE_W = 620.0;
    private static final double HARBOR_MESSAGE_H = 30.0;
    private static final double HARBOR_MESSAGE_GAP_ABOVE_BUTTONS = 42.0;
    private static final String BANK_CARD_SELECTED_STYLE =
            "-fx-border-color: #7a4a23; -fx-border-width: 3; -fx-border-radius: 12; "
            + "-fx-background-color: rgba(122,74,35,0.10); -fx-background-radius: 12;";
    private static final String TRADE_CHOICE_IMAGE_PATH = "/com/bananarepublic/images/trade/Trade Choice.png";
    private static final String TRADE_BANK_IMAGE_PATH = "/com/bananarepublic/images/trade/Bank Trade.png";
    private static final String TRADE_PLAYER_IMAGE_PATH = "/com/bananarepublic/images/trade/Player Trade.png";
    private static final String TRADE_HARBOR_IMAGE_PATH = "/com/bananarepublic/images/trade/Harbor Trade.png";
    private static final String TRADE_OFFER_IMAGE_PATH = "/com/bananarepublic/images/trade/Trade Offer.png";
    private static final String TRADE_COUNTER_IMAGE_PATH = "/com/bananarepublic/images/trade/Counter Offer.png";
    private static final String TRADE_REVIEW_COUNTER_IMAGE_PATH = "/com/bananarepublic/images/trade/Review Counter.png";
    private static final String TRADE_INFORMATION_IMAGE_PATH = "/com/bananarepublic/images/trade/information.png";
    private static final String PLAYER_PROFILE_BASE_PATH = "/com/bananarepublic/images/trade/profilepictPlayer%d.png";
    private static final double TRADE_OFFER_ASSET_WIDTH = 926.0;
    private static final double TRADE_OFFER_ASSET_HEIGHT = 1124.0;
    private static final double TRADE_OFFER_FROM_X = 92.0;
    private static final double TRADE_OFFER_FROM_Y = 266.0;
    private static final double TRADE_OFFER_FROM_CARD_W = 122.0;
    private static final double TRADE_OFFER_FROM_CARD_H = 118.0;
    private static final double TRADE_OFFER_FROM_IMAGE_W = 66.0;
    private static final double TRADE_OFFER_FROM_IMAGE_H = 66.0;
    private static final double TRADE_OFFER_GIVE_ROW_X = 88.0;
    private static final double TRADE_OFFER_GIVE_ROW_Y = 470.0;
    private static final double TRADE_OFFER_WANT_ROW_X = 88.0;
    private static final double TRADE_OFFER_WANT_ROW_Y = 760.0;
    private static final double TRADE_OFFER_RESOURCE_CARD_W = 104.0;
    private static final double TRADE_OFFER_RESOURCE_CARD_H = 146.0;
    private static final double TRADE_OFFER_RESOURCE_GAP = 16.0;
    // Traced from Trade Offer.png lower buttons region.
    private static final double TRADE_OFFER_ACTION_X_START = 49.0;
    private static final double TRADE_OFFER_ACTION_Y = 982.0;
    private static final double TRADE_OFFER_ACTION_BUTTON_W = 250.0;
    private static final double TRADE_OFFER_ACTION_BUTTON_H = 63.0;
    private static final double TRADE_OFFER_ACTION_GAP = 24.0;
    private static final double TRADE_OFFER_ACTION_REJECT_X = 49.0;
    private static final double TRADE_OFFER_ACTION_ACCEPT_X = 326.0;
    private static final double TRADE_OFFER_ACTION_COUNTER_X = 598.0;
    private static final String HARBOR_BUTTON_BASE_PATH = "/com/bananarepublic/images/trade/%s";
    private static final List<ResourceType> TRADE_RESOURCE_ORDER = List.of(
            ResourceType.BANANA,
            ResourceType.WOOD,
            ResourceType.WHEAT,
            ResourceType.ORE,
            ResourceType.BRICK
    );

    private enum TradeMode {
        NONE,
        CHOICE,
        PLAYER_TRADE,
        BANK_TRADE,
        HARBOR_TRADE,
        TRADE_OFFER_REVIEW,
        COUNTER_OFFER,
        REVIEW_COUNTER,
        INFORMATION
    }

    private enum InformationCloseAction {
        RETURN_TO_GAME,
        RETURN_TO_TRADE_CHOICE,
        RETURN_TO_BANK_TRADE
    }

    private enum TradeHarborType {
        GENERAL,
        BANANA,
        WOOD,
        BRICK,
        WHEAT,
        ORE
    }

    private enum ResourceTypeUI {
        BANANA,
        WOOD,
        WHEAT,
        ORE,
        BRICK
    }

    private static class TradeOfferState {
        int proposerIndex;
        int receiverIndex;
        EnumMap<ResourceType, Integer> give = new EnumMap<>(ResourceType.class);
        EnumMap<ResourceType, Integer> receive = new EnumMap<>(ResourceType.class);
        boolean counter;
    }

    private TradeMode currentTradeMode = TradeMode.NONE;
    private TradeOfferState currentOffer;
    private int currentProposerIndex = 0;
    private int currentReceiverIndex = 1;
    private int selectedTargetIndex = -1;
    private ResourceType selectedBankGiveResource;
    private ResourceType selectedBankReceiveResource;
    private TradeHarborType selectedHarborType;
    private ResourceType selectedHarborGiveResource;
    private ResourceType selectedHarborReceiveResource;
    private Set<TradeHarborType> availableHarbors = new HashSet<>();
    private final EnumMap<ResourceType, Integer> giveResourceQuantities = new EnumMap<>(ResourceType.class);
    private final EnumMap<ResourceType, Integer> receiveResourceQuantities = new EnumMap<>(ResourceType.class);
    private final EnumMap<ResourceType, Integer> counterGiveQuantities = new EnumMap<>(ResourceType.class);
    private final EnumMap<ResourceType, Integer> counterReceiveQuantities = new EnumMap<>(ResourceType.class);
    private final List<EnumMap<ResourceType, Integer>> playerResourceStocks = new ArrayList<>();
    private int counterProposerIndex = -1;
    private int counterResponderIndex = -1;
    private ResourceTypeUI selectedBankReceiveResourceUi;
    private ResourceTypeUI selectedPlayerGiveResourceUi;
    private final EnumMap<ResourceTypeUI, Integer> dummyPlayerResources = new EnumMap<>(ResourceTypeUI.class);
    private final EnumMap<ResourceTypeUI, Integer> dummyBankResources = new EnumMap<>(ResourceTypeUI.class);
    private final List<String> dummyTradeNames = List.of("Kebin", "Stewart", "Bob", "Toto");
    private InformationCloseAction informationCloseAction = InformationCloseAction.RETURN_TO_GAME;
    private double currentPopupLogicalWidth = POPUP_ASSET_WIDTH;
    private double currentPopupLogicalHeight = POPUP_ASSET_HEIGHT;

    // ── Lifecycle ────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupResponsiveScaling();
        setupTradeModalRoot();
        setupBuyExperimentOverlay();
        setupRollDiceManualOverlay();
        setupDeclareVictoryOverlay();
        loadFonts();
        applyFontsToLabels();
        loadPlayerNames();
        setupPlayerCardBoxes();
        renderHexBoard();
        initInventory();
        initBankStock();
        initializeTradeState();
        setupBankTradeClickZones();
        configureTradeModalSize(POPUP_ASSET_WIDTH, POPUP_ASSET_HEIGHT);
        updatePhaseDisplay(false);
        hidePopup();
        startTurnTimer(DEFAULT_TURN_SECONDS);
    }

    private void setupTradeModalRoot() {
        if (popupOverlay != null && tradeOverlayRoot != null) {
            popupOverlay.prefWidthProperty().bind(tradeOverlayRoot.widthProperty());
            popupOverlay.prefHeightProperty().bind(tradeOverlayRoot.heightProperty());
        }
        if (rootPane != null) {
            rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (tradeOverlayRoot != null && tradeOverlayRoot.isVisible()) {
                    normalizeTradePopupScale();
                }
            });
            rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
                if (tradeOverlayRoot != null && tradeOverlayRoot.isVisible()) {
                    normalizeTradePopupScale();
                }
            });
        }
    }

    private void setupBuyExperimentOverlay() {
        if (buyExperimentOverlayController == null) return;
        buyExperimentOverlayController.configure(
                this::isBuyExperimentAllowedPhase,
                resourceType -> getResourceAmount(activePlayerIndex, resourceType),
                this::applyBuyExperimentCardCost,
                message -> showInformationPopup(message, InformationCloseAction.RETURN_TO_GAME),
                this::appendLog
        );
    }

    private boolean isBuyExperimentAllowedPhase() {
        // TODO: connect to real phase state from bridge/engine.
        return true;
    }

    private boolean applyBuyExperimentCardCost() {
        if (getResourceAmount(activePlayerIndex, ResourceType.ORE) < 1) return false;
        if (getResourceAmount(activePlayerIndex, ResourceType.BANANA) < 1) return false;
        if (getResourceAmount(activePlayerIndex, ResourceType.WHEAT) < 1) return false;

        addResourceAmount(activePlayerIndex, ResourceType.ORE, -1);
        addResourceAmount(activePlayerIndex, ResourceType.BANANA, -1);
        addResourceAmount(activePlayerIndex, ResourceType.WHEAT, -1);
        addBankResourceAmount(ResourceType.ORE, 1);
        addBankResourceAmount(ResourceType.BANANA, 1);
        addBankResourceAmount(ResourceType.WHEAT, 1);
        syncTradeStateBackToSaveData();
        initializeBankTradeDummyData();
        return true;
    }

    private void setupRollDiceManualOverlay() {
        if (rollDiceManualOverlayController == null) return;
        rollDiceManualOverlayController.configure(
                this::isManualDiceAllowedPhase,
                this::rollDiceWithValues
        );
    }

    private boolean isManualDiceAllowedPhase() {
        // TODO: connect to real phase state from bridge/engine.
        return true;
    }

    private void setupDeclareVictoryOverlay() {
        if (declareVictoryOverlayController == null) return;
        declareVictoryOverlayController.configure(
                () -> activePlayerIndex,
                this::buildVictoryLeaderboardEntries,
                message -> showInformationPopup(message, InformationCloseAction.RETURN_TO_GAME),
                () -> {
                    try {
                        BananaRepublic.showMainMenu();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                this::saveGameFromVictoryOverlay
        );
    }

    private boolean saveGameFromVictoryOverlay() {
        // TODO: connect to real persistence save service.
        return true;
    }

    private List<DeclareVictoryOverlayController.VictoryLeaderboardEntry> buildVictoryLeaderboardEntries() {
        List<DeclareVictoryOverlayController.VictoryLeaderboardEntry> entries = new ArrayList<>();
        int totalPlayers = Math.max(1, Math.min(4, Math.max(playerCount, GameSession.getPlayerNames().size())));

        int[] labelVictoryPoints = {
                parseVictoryPointsFromLabel(player1VpLabel),
                parseVictoryPointsFromLabel(player2VpLabel),
                parseVictoryPointsFromLabel(player3VpLabel),
                parseVictoryPointsFromLabel(player4VpLabel)
        };
        boolean hasRealVictoryPointData = false;
        for (int i = 0; i < totalPlayers; i++) {
            if (labelVictoryPoints[i] > 0) {
                hasRealVictoryPointData = true;
                break;
            }
        }

        int[] dummyVictoryPoints = {10, 8, 6, 4};
        for (int i = 0; i < totalPlayers; i++) {
            int playerIndex = i;
            int oneBasedIndex = i + 1;
            String playerName = getPlayerNameForIndex(oneBasedIndex);
            int victoryPoints = hasRealVictoryPointData ? labelVictoryPoints[i] : dummyVictoryPoints[i];
            Image profileImage = loadVictoryProfileImage(oneBasedIndex);
            entries.add(new DeclareVictoryOverlayController.VictoryLeaderboardEntry(
                    playerIndex,
                    playerName,
                    Math.max(0, victoryPoints),
                    profileImage
            ));
        }
        return entries;
    }

    private int parseVictoryPointsFromLabel(Label label) {
        if (label == null) return 0;
        String raw = label.getText();
        if (raw == null || raw.isBlank()) return 0;
        String digitsOnly = raw.replaceAll("[^0-9]", "");
        if (digitsOnly.isBlank()) return 0;
        try {
            return Math.max(0, Integer.parseInt(digitsOnly));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private Image loadVictoryProfileImage(int oneBasedIndex) {
        String path = "/com/bananarepublic/images/declare_victory/profilepictPlayer" + oneBasedIndex + ".png";
        try {
            return new Image(getClass().getResource(path).toExternalForm());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setupBankTradeClickZones() {
        Button[] buttons = {
                bankBananaSelectButton, bankWoodSelectButton, bankWheatSelectButton, bankOreSelectButton, bankBrickSelectButton,
                youBananaSelectButton, youWoodSelectButton, youWheatSelectButton, youOreSelectButton, youBrickSelectButton,
                bankTradeCancelButton, bankTradeConfirmButton
        };
        for (Button button : buttons) {
            button.setFocusTraversable(false);
            button.setPickOnBounds(true);
        }

        Label[] checks = {
                bankBananaCheck, bankWoodCheck, bankWheatCheck, bankOreCheck, bankBrickCheck,
                youBananaCheck, youWoodCheck, youWheatCheck, youOreCheck, youBrickCheck
        };
        for (Label check : checks) {
            check.setMouseTransparent(true);
            check.setText("");
            check.setStyle(BANK_CARD_SELECTED_STYLE);
        }
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

    // ── Trade overlay state + flows ─────────────────────────────────
    private void initializeTradeState() {
        playerResourceStocks.clear();
        ensurePlayerStockCapacity(Math.max(playerCount, 1));

        SaveData saveData = GameSession.getLoadedSaveData();
        if (saveData != null) {
            if (saveData.players != null) {
                ensurePlayerStockCapacity(Math.max(playerCount, saveData.players.size()));
                for (int i = 0; i < Math.min(playerResourceStocks.size(), saveData.players.size()); i++) {
                    SaveData.PlayerData playerData = saveData.players.get(i);
                    if (playerData.resources == null) continue;
                    EnumMap<ResourceType, Integer> stock = playerResourceStocks.get(i);
                    for (Map.Entry<String, Integer> entry : playerData.resources.entrySet()) {
                        ResourceType type = parseResourceType(entry.getKey());
                        if (type != null) {
                            stock.put(type, Math.max(0, entry.getValue() == null ? 0 : entry.getValue()));
                        }
                    }
                }
            }

            if (saveData.bank != null) {
                for (Map.Entry<String, Integer> entry : saveData.bank.entrySet()) {
                    ResourceType type = parseResourceType(entry.getKey());
                    if (type != null) {
                        bankStock.put(type, Math.max(0, entry.getValue() == null ? 0 : entry.getValue()));
                    }
                }
            }
        }

        // UI-first fallback for trade screen testing when engine resources are not populated yet.
        if (shouldUseDummyTradeData()) {
            applyDummyTradeData();
        }

        initializeBankTradeDummyData();

        resetTradeDraftQuantities();
        selectedTargetIndex = -1;
        selectedBankGiveResource = null;
        selectedBankReceiveResource = null;
        selectedHarborType = null;
        selectedHarborGiveResource = null;
        selectedHarborReceiveResource = null;
        currentOffer = null;
        currentTradeMode = TradeMode.NONE;
        currentProposerIndex = Math.max(0, Math.min(activePlayerIndex, playerResourceStocks.size() - 1));
        currentReceiverIndex = playerResourceStocks.size() > 1
                ? (currentProposerIndex + 1) % playerResourceStocks.size()
                : currentProposerIndex;
    }

    private void initializeBankTradeDummyData() {
        dummyPlayerResources.clear();
        dummyBankResources.clear();

        for (ResourceTypeUI resourceTypeUI : ResourceTypeUI.values()) {
            dummyPlayerResources.put(resourceTypeUI, 0);
            dummyBankResources.put(resourceTypeUI, 0);
        }

        // TODO: replace with real GameState/Player/Bank adapter once engine API is ready.
        dummyPlayerResources.put(ResourceTypeUI.BANANA, getResourceAmount(activePlayerIndex, ResourceType.BANANA));
        dummyPlayerResources.put(ResourceTypeUI.WOOD, getResourceAmount(activePlayerIndex, ResourceType.WOOD));
        dummyPlayerResources.put(ResourceTypeUI.WHEAT, getResourceAmount(activePlayerIndex, ResourceType.WHEAT));
        dummyPlayerResources.put(ResourceTypeUI.ORE, getResourceAmount(activePlayerIndex, ResourceType.ORE));
        dummyPlayerResources.put(ResourceTypeUI.BRICK, getResourceAmount(activePlayerIndex, ResourceType.BRICK));

        dummyBankResources.put(ResourceTypeUI.BANANA, getBankResourceAmount(ResourceType.BANANA));
        dummyBankResources.put(ResourceTypeUI.WOOD, getBankResourceAmount(ResourceType.WOOD));
        dummyBankResources.put(ResourceTypeUI.WHEAT, getBankResourceAmount(ResourceType.WHEAT));
        dummyBankResources.put(ResourceTypeUI.ORE, getBankResourceAmount(ResourceType.ORE));
        dummyBankResources.put(ResourceTypeUI.BRICK, getBankResourceAmount(ResourceType.BRICK));
    }

    private boolean shouldUseDummyTradeData() {
        boolean anyPlayerHasResources = false;
        for (EnumMap<ResourceType, Integer> stock : playerResourceStocks) {
            for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
                if (stock.getOrDefault(resourceType, 0) > 0) {
                    anyPlayerHasResources = true;
                    break;
                }
            }
            if (anyPlayerHasResources) break;
        }

        boolean bankConfigured = false;
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            if (bankStock.getOrDefault(resourceType, 0) > 0) {
                bankConfigured = true;
                break;
            }
        }

        return !anyPlayerHasResources || !bankConfigured;
    }

    private void applyDummyTradeData() {
        ensurePlayerStockCapacity(4);

        setDummyPlayerStock(0, 5, 4, 3, 2, 4);
        setDummyPlayerStock(1, 2, 3, 1, 4, 2);
        setDummyPlayerStock(2, 1, 5, 3, 1, 0);
        setDummyPlayerStock(3, 4, 1, 2, 2, 3);

        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            bankStock.put(resourceType, 15);
        }
    }

    private void setDummyPlayerStock(int index, int banana, int wood, int wheat, int ore, int brick) {
        if (index < 0 || index >= playerResourceStocks.size()) return;
        EnumMap<ResourceType, Integer> stock = playerResourceStocks.get(index);
        stock.put(ResourceType.BANANA, banana);
        stock.put(ResourceType.WOOD, wood);
        stock.put(ResourceType.WHEAT, wheat);
        stock.put(ResourceType.ORE, ore);
        stock.put(ResourceType.BRICK, brick);
    }

    private void ensurePlayerStockCapacity(int size) {
        while (playerResourceStocks.size() < size) {
            playerResourceStocks.add(newZeroResourceMap());
        }
    }

    private EnumMap<ResourceType, Integer> newZeroResourceMap() {
        EnumMap<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        for (ResourceType resourceType : ResourceType.values()) {
            map.put(resourceType, 0);
        }
        return map;
    }

    private ResourceType parseResourceType(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "BANANA" -> ResourceType.BANANA;
            case "WOOD", "LOG" -> ResourceType.WOOD;
            case "WHEAT" -> ResourceType.WHEAT;
            case "ORE" -> ResourceType.ORE;
            case "BRICK", "BREAK" -> ResourceType.BRICK;
            default -> null;
        };
    }

    private void showTradeChoice() {
        System.out.println("[TradeUI] showTradeChoice called");
        hideAllTradeControls();
        currentTradeMode = TradeMode.CHOICE;
        showPopupImage(TRADE_CHOICE_IMAGE_PATH);
        setTradeOverlayContentVisible(false);
        clearTradeError();
    }

    private void showTradeOverlayBase(String imagePath, TradeMode mode) {
        hideAllTradeControls();
        currentTradeMode = mode;
        showPopupImage(imagePath);
        setTradeOverlayContentVisible(true);
        tradeUiPane.getChildren().clear();
        tradeActionButtonsBox.getChildren().clear();
        clearTradeError();
    }

    private void setTradeOverlayContentVisible(boolean visible) {
        tradeUiPane.setVisible(visible);
        tradeUiPane.setManaged(visible);
        tradeActionButtonsBox.setVisible(visible);
        tradeActionButtonsBox.setManaged(visible);
        if (!visible) {
            tradeUiPane.getChildren().clear();
            tradeActionButtonsBox.getChildren().clear();
            clearTradeError();
        }
    }

    private void hideAllTradeControls() {
        setTradeChoiceButtonsVisible(false);
        setTradeOverlayContentVisible(false);
        setBankTradeControlsVisible(false);
        clearPlayerTradeMessage();
        clearTradeError();
    }

    private void hideTradeOverlay() {
        hidePopup();
        currentTradeMode = TradeMode.NONE;
        clearTradeError();
    }

    private void clearCurrentTradeOffer() {
        currentOffer = null;
    }

    private void resetTemporaryTradeState() {
        selectedTargetIndex = -1;
        selectedBankGiveResource = null;
        selectedBankReceiveResource = null;
        selectedBankReceiveResourceUi = null;
        selectedPlayerGiveResourceUi = null;
        selectedHarborType = null;
        selectedHarborGiveResource = null;
        selectedHarborReceiveResource = null;
        counterProposerIndex = -1;
        counterResponderIndex = -1;
        counterGiveQuantities.clear();
        counterReceiveQuantities.clear();
        resetTradeDraftQuantities();
        clearPlayerTradeMessage();
        clearTradeError();
    }

    private void hideInformationPopup() {
        hidePopup();
    }

    private void onInformationCloseClicked() {
        hideInformationPopup();
        clearCurrentTradeOffer();
        resetTemporaryTradeState();

        InformationCloseAction action = informationCloseAction;
        informationCloseAction = InformationCloseAction.RETURN_TO_GAME;

        if (action == InformationCloseAction.RETURN_TO_TRADE_CHOICE) {
            showTradeChoice();
            return;
        }
        if (action == InformationCloseAction.RETURN_TO_BANK_TRADE) {
            showBankTrade();
            return;
        }
        System.out.println("Returning to main game from Review Counter");
        hideTradeOverlay();
    }

    private void clearTradeError() {
        tradeErrorLabel.setText("");
        tradeErrorLabel.setVisible(false);
        tradeErrorLabel.setManaged(false);
    }

    private void showTradeError(String message) {
        tradeErrorLabel.setText(message);
        tradeErrorLabel.setVisible(true);
        tradeErrorLabel.setManaged(true);
    }

    private void clearPlayerTradeMessage() {
        if (playerTradeMessageLabel == null) return;
        playerTradeMessageLabel.setText("");
        playerTradeMessageLabel.setVisible(false);
        playerTradeMessageLabel.setManaged(false);
    }

    private void showPlayerTradeMessage(String message, boolean success) {
        if (playerTradeMessageLabel == null) return;
        playerTradeMessageLabel.getStyleClass().remove("trade-success-label");
        if (success) {
            playerTradeMessageLabel.getStyleClass().add("trade-success-label");
        }
        playerTradeMessageLabel.setAlignment(Pos.CENTER);
        playerTradeMessageLabel.setText(message);
        playerTradeMessageLabel.setVisible(true);
        playerTradeMessageLabel.setManaged(true);
        playerTradeMessageLabel.toFront();
    }

    private void showInformationPopup(String message, InformationCloseAction closeAction) {
        System.out.println("Information popup shown: " + message);
        hideAllTradeControls();
        currentTradeMode = TradeMode.INFORMATION;
        informationCloseAction = closeAction == null
                ? InformationCloseAction.RETURN_TO_GAME
                : closeAction;

        showPopupImage(TRADE_INFORMATION_IMAGE_PATH);
        setTradeOverlayContentVisible(true);
        tradeUiPane.getChildren().clear();
        hideTradeActions();
        setBankTradeActionButtonsVisible(false);
        clearPlayerTradeMessage();
        clearTradeError();

        popupCloseButton.setVisible(false);
        popupCloseButton.setManaged(false);
        popupCloseButton.setDisable(true);
        popupCloseButton.setMouseTransparent(true);

        double scaleX = currentPopupLogicalWidth / POPUP_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / POPUP_ASSET_HEIGHT;

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("trade-info-message-label");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setPrefWidth(Math.max(320.0 * scaleX, currentPopupLogicalWidth - (220.0 * scaleX)));
        messageLabel.setPrefHeight(170.0 * scaleY);
        messageLabel.setLayoutX((currentPopupLogicalWidth - messageLabel.getPrefWidth()) / 2.0);
        messageLabel.setLayoutY(250.0 * scaleY);

        Button closeButton = new Button();
        closeButton.getStyleClass().add("trade-click-zone");
        closeButton.setOpacity(0.0);
        closeButton.setPrefWidth(180.0 * scaleX);
        closeButton.setPrefHeight(56.0 * scaleY);
        closeButton.setLayoutX((currentPopupLogicalWidth - closeButton.getPrefWidth()) / 2.0);
        closeButton.setLayoutY(620.0 * scaleY);
        closeButton.setOnAction(e -> onInformationCloseClicked());

        tradeUiPane.getChildren().addAll(messageLabel, closeButton);
        tradeUiPane.toFront();
    }

    private void setBankTradeControlsVisible(boolean visible) {
        Label[] labels = {
                bankTradeMessageLabel,
                bankBananaQtyLabel, bankWoodQtyLabel, bankWheatQtyLabel, bankOreQtyLabel, bankBrickQtyLabel,
                youBananaQtyLabel, youWoodQtyLabel, youWheatQtyLabel, youOreQtyLabel, youBrickQtyLabel,
                bankBananaCheck, bankWoodCheck, bankWheatCheck, bankOreCheck, bankBrickCheck,
                youBananaCheck, youWoodCheck, youWheatCheck, youOreCheck, youBrickCheck
        };
        Button[] buttons = {
                bankBananaSelectButton, bankWoodSelectButton, bankWheatSelectButton, bankOreSelectButton, bankBrickSelectButton,
                youBananaSelectButton, youWoodSelectButton, youWheatSelectButton, youOreSelectButton, youBrickSelectButton,
                bankTradeCancelButton, bankTradeConfirmButton
        };

        for (Label label : labels) {
            label.setVisible(visible);
            label.setManaged(visible);
        }
        for (Button button : buttons) {
            button.setVisible(visible);
            button.setManaged(visible);
            button.setDisable(!visible);
            button.setMouseTransparent(!visible);
        }
    }

    private Button createTradeActionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("trade-click-zone");
        button.setPrefWidth(150);
        button.setPrefHeight(42);
        button.setOnAction(e -> {
            System.out.println("[TradeUI] clicked action: " + text);
            action.run();
        });
        return button;
    }

    private void setTradeActions(Node... nodes) {
        tradeActionButtonsBox.getChildren().setAll(nodes);
        tradeActionButtonsBox.setVisible(true);
        tradeActionButtonsBox.setManaged(true);
        tradeActionButtonsBox.setMouseTransparent(false);
        positionTradeActionButtons(nodes.length);
        for (Node node : nodes) {
            if (node instanceof Button button) {
                button.setDisable(false);
                button.setMouseTransparent(false);
            }
        }
        tradeActionButtonsBox.toFront();
    }

    private void positionTradeActionButtons(int buttonCount) {
        if (buttonCount <= 0) return;

        if (currentTradeMode == TradeMode.TRADE_OFFER_REVIEW || currentTradeMode == TradeMode.REVIEW_COUNTER) {
            tradeActionButtonsBox.setLayoutX(TRADE_OFFER_ACTION_X_START);
            tradeActionButtonsBox.setLayoutY(TRADE_OFFER_ACTION_Y);
            tradeActionButtonsBox.setSpacing(TRADE_OFFER_ACTION_GAP);
            for (Node node : tradeActionButtonsBox.getChildren()) {
                if (node instanceof Button button) {
                    button.setPrefWidth(TRADE_OFFER_ACTION_BUTTON_W);
                    button.setPrefHeight(TRADE_OFFER_ACTION_BUTTON_H);
                    button.setOpacity(0.0);
                }
            }
            System.out.println("[TradeUI] offer action hitbox start=("
                    + TRADE_OFFER_ACTION_X_START + "," + TRADE_OFFER_ACTION_Y
                    + "), size=" + TRADE_OFFER_ACTION_BUTTON_W + "x" + TRADE_OFFER_ACTION_BUTTON_H
                    + ", gap=" + TRADE_OFFER_ACTION_GAP);
            return;
        }

        double boxWidth = tradeActionButtonsBox.getPrefWidth() > 0 ? tradeActionButtonsBox.getPrefWidth() : 532.0;
        tradeActionButtonsBox.setLayoutX((currentPopupLogicalWidth - boxWidth) / 2.0);
        tradeActionButtonsBox.setLayoutY(Math.max(0.0, currentPopupLogicalHeight - 120.0));
    }

    private void hideTradeActions() {
        tradeActionButtonsBox.getChildren().clear();
        tradeActionButtonsBox.setVisible(false);
        tradeActionButtonsBox.setManaged(false);
    }

    private VBox createTradeContentRoot(String title, String subtitle) {
        VBox root = new VBox(14);
        root.setLayoutX(60);
        root.setLayoutY(110);
        root.setPrefWidth(928);
        root.setPrefHeight(540);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("trade-title-label");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("trade-subtitle-label");
        root.getChildren().addAll(titleLabel, subtitleLabel);
        return root;
    }

    private String displayResource(ResourceType resourceType) {
        return switch (resourceType) {
            case BANANA -> "Banana";
            case WOOD -> "Wood";
            case WHEAT -> "Wheat";
            case ORE -> "Ore";
            case BRICK -> "Brick";
        };
    }

    private String getPlayerNameForIndex(int oneBasedIndex) {
        List<String> names = GameSession.getPlayerNames();
        if (oneBasedIndex >= 1 && oneBasedIndex <= names.size()) {
            return names.get(oneBasedIndex - 1);
        }
        if (oneBasedIndex >= 1 && oneBasedIndex <= dummyTradeNames.size()) {
            return dummyTradeNames.get(oneBasedIndex - 1);
        }
        return "Player " + oneBasedIndex;
    }

    private int getResourceAmount(int playerIndex, ResourceType resourceType) {
        if (playerIndex < 0 || playerIndex >= playerResourceStocks.size()) return 0;
        return playerResourceStocks.get(playerIndex).getOrDefault(resourceType, 0);
    }

    private int getBankResourceAmount(ResourceType resourceType) {
        return bankStock.getOrDefault(resourceType, 0);
    }

    private void addResourceAmount(int playerIndex, ResourceType resourceType, int delta) {
        if (playerIndex < 0 || playerIndex >= playerResourceStocks.size()) return;
        EnumMap<ResourceType, Integer> stock = playerResourceStocks.get(playerIndex);
        int next = Math.max(0, stock.getOrDefault(resourceType, 0) + delta);
        stock.put(resourceType, next);
    }

    private void addBankResourceAmount(ResourceType resourceType, int delta) {
        int next = Math.max(0, bankStock.getOrDefault(resourceType, 0) + delta);
        bankStock.put(resourceType, next);
    }

    private void resetTradeDraftQuantities() {
        giveResourceQuantities.clear();
        receiveResourceQuantities.clear();
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            giveResourceQuantities.put(resourceType, 0);
            receiveResourceQuantities.put(resourceType, 0);
        }
    }

    private int sumResourceQuantities(Map<ResourceType, Integer> quantities) {
        int sum = 0;
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            sum += Math.max(0, quantities.getOrDefault(resourceType, 0));
        }
        return sum;
    }

    private boolean playerHasEnough(int playerIndex, Map<ResourceType, Integer> needed) {
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int ask = needed.getOrDefault(resourceType, 0);
            if (ask > getResourceAmount(playerIndex, resourceType)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasSharedResourceSelection() {
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            if (giveResourceQuantities.getOrDefault(resourceType, 0) > 0
                    && receiveResourceQuantities.getOrDefault(resourceType, 0) > 0) {
                return true;
            }
        }
        return false;
    }

    private String formatResourceBundle(Map<ResourceType, Integer> bundle) {
        List<String> parts = new ArrayList<>();
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int qty = bundle.getOrDefault(resourceType, 0);
            if (qty > 0) {
                parts.add(qty + " " + displayResource(resourceType));
            }
        }
        return parts.isEmpty() ? "-" : String.join(", ", parts);
    }

    private void syncTradeStateBackToSaveData() {
        SaveData saveData = GameSession.getLoadedSaveData();
        if (saveData == null) return;

        if (saveData.players != null) {
            int limit = Math.min(saveData.players.size(), playerResourceStocks.size());
            for (int i = 0; i < limit; i++) {
                SaveData.PlayerData playerData = saveData.players.get(i);
                if (playerData.resources == null) {
                    playerData.resources = new HashMap<>();
                }
                for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
                    playerData.resources.put(
                            displayResource(resourceType).toLowerCase(Locale.ROOT),
                            playerResourceStocks.get(i).getOrDefault(resourceType, 0)
                    );
                }
            }
        }

        if (saveData.bank == null) {
            saveData.bank = new HashMap<>();
        }
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            saveData.bank.put(
                    displayResource(resourceType).toLowerCase(Locale.ROOT),
                    bankStock.getOrDefault(resourceType, 0)
            );
        }

        if (activePlayerIndex >= 0 && activePlayerIndex < playerResourceStocks.size()) {
            refreshResourceCards(playerResourceStocks.get(activePlayerIndex));
        }
        if ("bank".equals(activeInfoPanel)) {
            buildBankCards();
        }
    }

    private ImageView createResourceIcon(ResourceType resourceType) {
        ImageView icon = new ImageView(loadInventoryImage(resourceType));
        icon.setFitWidth(68);
        icon.setFitHeight(98);
        icon.setPreserveRatio(false);
        icon.setSmooth(true);
        return icon;
    }

    private VBox buildSelectionSection(
            String title,
            ResourceType selectedResource,
            Consumer<ResourceType> onSelect,
            Predicate<ResourceType> disabledPredicate
    ) {
        VBox section = new VBox(8);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("trade-section-label");
        section.getChildren().add(titleLabel);

        HBox cardRow = new HBox(12);
        HBox markRow = new HBox(12);
        cardRow.setAlignment(Pos.CENTER_LEFT);
        markRow.setAlignment(Pos.CENTER_LEFT);

        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            VBox cell = new VBox(4);
            cell.setAlignment(Pos.CENTER);
            cell.setPrefWidth(84);

            Button selectButton = createTradeActionButton(displayResource(resourceType), () -> onSelect.accept(resourceType));
            selectButton.setGraphic(createResourceIcon(resourceType));
            selectButton.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
            selectButton.setPrefWidth(84);
            selectButton.setPrefHeight(120);

            Label marker = new Label(selectedResource == resourceType ? "x" : "o");
            marker.getStyleClass().add("trade-player-name");
            marker.setPrefWidth(84);
            marker.setAlignment(Pos.CENTER);

            if (disabledPredicate.test(resourceType)) {
                selectButton.setDisable(true);
                cell.getStyleClass().add("trade-disabled");
            }
            if (selectedResource == resourceType) {
                cell.getStyleClass().add("trade-selected");
            }

            cell.getChildren().add(selectButton);
            cardRow.getChildren().add(cell);
            markRow.getChildren().add(marker);
        }

        section.getChildren().addAll(cardRow, markRow);
        return section;
    }

    private VBox buildQuantitySection(
            String title,
            EnumMap<ResourceType, Integer> quantities,
            Function<ResourceType, Integer> maxProvider,
            Runnable onChanged
    ) {
        VBox section = new VBox(8);
        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("trade-section-label");
        section.getChildren().add(sectionTitle);

        HBox cards = new HBox(12);
        HBox controls = new HBox(12);
        cards.setAlignment(Pos.CENTER_LEFT);
        controls.setAlignment(Pos.CENTER_LEFT);

        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int value = quantities.getOrDefault(resourceType, 0);
            int max = Math.max(0, maxProvider.apply(resourceType));

            Button card = createTradeActionButton("", () -> {});
            card.setGraphic(createResourceIcon(resourceType));
            card.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
            card.setPrefWidth(84);
            card.setPrefHeight(120);
            card.setDisable(true);
            if (max <= 0) {
                card.getStyleClass().add("trade-disabled");
            }
            cards.getChildren().add(card);

            HBox control = new HBox(4);
            control.setAlignment(Pos.CENTER);
            control.setPrefWidth(84);

            Button minus = createTradeActionButton("-", () -> {
                int current = quantities.getOrDefault(resourceType, 0);
                if (current > 0) {
                    quantities.put(resourceType, current - 1);
                    onChanged.run();
                }
            });
            minus.setPrefSize(24, 24);
            minus.setDisable(value <= 0);

            Label qty = new Label(String.valueOf(value));
            qty.getStyleClass().add("trade-qty-label");
            qty.setPrefWidth(24);
            qty.setAlignment(Pos.CENTER);

            Button plus = createTradeActionButton("+", () -> {
                int current = quantities.getOrDefault(resourceType, 0);
                if (current < max) {
                    quantities.put(resourceType, current + 1);
                    onChanged.run();
                }
            });
            plus.setPrefSize(24, 24);
            plus.setDisable(value >= max);

            control.getChildren().addAll(minus, qty, plus);
            controls.getChildren().add(control);
        }

        section.getChildren().addAll(cards, controls);

        return section;
    }

    private void showPlayerTrade() {
        System.out.println("[TradeUI] Player Trade opened");
        showTradeOverlayBase(TRADE_PLAYER_IMAGE_PATH, TradeMode.PLAYER_TRADE);
        hideTradeActions();
        setBankTradeActionButtonsVisible(false);
        clearTradeError();
        clearPlayerTradeMessage();

        int totalPlayers = getTradePlayerCount();
        ensurePlayerStockCapacity(Math.max(totalPlayers, 1));
        currentProposerIndex = Math.max(0, Math.min(activePlayerIndex, playerResourceStocks.size() - 1));
        selectedTargetIndex = -1;

        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            giveResourceQuantities.put(resourceType, getResourceAmount(currentProposerIndex, resourceType));
            receiveResourceQuantities.put(resourceType, 0);
        }

        renderPlayerTradeUi();
    }

    private void renderPlayerTradeUi() {
        if (currentTradeMode != TradeMode.PLAYER_TRADE) return;

        tradeUiPane.getChildren().clear();
        tradeUiPane.setVisible(true);
        tradeUiPane.setManaged(true);

        double scaleX = currentPopupLogicalWidth / PLAYER_POPUP_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / PLAYER_POPUP_ASSET_HEIGHT;

        for (int i = 0; i < TRADE_RESOURCE_ORDER.size(); i++) {
            ResourceType resourceType = TRADE_RESOURCE_ORDER.get(i);
            double centerX = (PLAYER_RESOURCE_CENTER_X_BASE + PLAYER_RESOURCE_CENTER_X_STEPS[i]) * scaleX;

            tradeUiPane.getChildren().addAll(
                    createPlayerTradeCardNode(resourceType, centerX, PLAYER_GIVE_CARD_Y * scaleY, scaleX, scaleY),
                    createPlayerTradeQuantityControl(resourceType, true, centerX, PLAYER_CONTROL_Y_GIVE * scaleY, scaleX, scaleY),
                    createPlayerTradeCardNode(resourceType, centerX, PLAYER_RECEIVE_CARD_Y * scaleY, scaleX, scaleY),
                    createPlayerTradeQuantityControl(resourceType, false, centerX, PLAYER_CONTROL_Y_RECEIVE * scaleY, scaleX, scaleY)
            );
        }

        tradeUiPane.getChildren().addAll(createPlayerTargetNodes(scaleX, scaleY));
        tradeUiPane.getChildren().addAll(createPlayerTradeActionButtons(scaleX, scaleY));

        double messageWidth = Math.min(PLAYER_MESSAGE_W * scaleX, currentPopupLogicalWidth - (80 * scaleX));
        double messageX = (currentPopupLogicalWidth - messageWidth) / 2.0;
        double buttonTopY = BANK_CANCEL_BUTTON_Y * scaleY;
        double messageY = buttonTopY - (PLAYER_MESSAGE_GAP_ABOVE_BUTTONS * scaleY);
        playerTradeMessageLabel.setLayoutX(messageX);
        playerTradeMessageLabel.setLayoutY(messageY);
        playerTradeMessageLabel.setPrefWidth(messageWidth);
        playerTradeMessageLabel.setPrefHeight(PLAYER_MESSAGE_H * scaleY);
        playerTradeMessageLabel.setAlignment(Pos.CENTER);
        tradeUiPane.toFront();
        playerTradeMessageLabel.toFront();
        popupCloseButton.toFront();
    }

    private Node createPlayerTradeCardNode(ResourceType resourceType, double centerX, double y, double scaleX, double scaleY) {
        double cardW = PLAYER_CARD_WIDTH * scaleX;
        double cardH = PLAYER_CARD_HEIGHT * scaleY;

        ImageView cardImage = createResourceIcon(resourceType);
        cardImage.setFitWidth(cardW);
        cardImage.setFitHeight(cardH);
        cardImage.setLayoutX(centerX - cardW / 2.0);
        cardImage.setLayoutY(y);
        cardImage.getStyleClass().add("trade-resource-card");
        cardImage.setMouseTransparent(true);
        return cardImage;
    }

    private Node createPlayerTradeQuantityControl(ResourceType resourceType, boolean giveSide,
                                                  double centerX, double y, double scaleX, double scaleY) {
        int value = (giveSide ? giveResourceQuantities : receiveResourceQuantities).getOrDefault(resourceType, 0);
        int max = giveSide ? getResourceAmount(currentProposerIndex, resourceType) : getPlayerTradeReceiveMax(resourceType);
        double buttonW = PLAYER_QTY_BUTTON_W * scaleX;
        double buttonH = PLAYER_QTY_BUTTON_H * scaleY;
        double labelW = PLAYER_QTY_LABEL_W * scaleX;
        double labelH = PLAYER_QTY_LABEL_H * scaleY;
        double gap = PLAYER_QTY_CONTROL_GAP * scaleX;

        Button minusButton = new Button("-");
        minusButton.getStyleClass().add("trade-small-button");
        minusButton.setPrefWidth(buttonW);
        minusButton.setPrefHeight(buttonH);
        minusButton.setDisable(value <= 0);
        minusButton.setOnAction(e -> {
            if (giveSide) {
                decreaseGiveResource(resourceType);
            } else {
                decreaseReceiveResource(resourceType);
            }
        });

        Label qtyLabel = new Label(String.valueOf(value));
        qtyLabel.getStyleClass().add("trade-quantity-label");
        qtyLabel.setPrefWidth(labelW);
        qtyLabel.setPrefHeight(labelH);
        qtyLabel.setAlignment(Pos.CENTER);

        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("trade-small-button");
        plusButton.setPrefWidth(buttonW);
        plusButton.setPrefHeight(buttonH);
        plusButton.setDisable(value >= max);
        plusButton.setOnAction(e -> {
            if (giveSide) {
                increaseGiveResource(resourceType);
            } else {
                increaseReceiveResource(resourceType);
            }
        });

        HBox control = new HBox(gap, minusButton, qtyLabel, plusButton);
        control.setAlignment(Pos.CENTER);
        double controlWidth = buttonW + labelW + buttonW + (2 * gap);
        control.setLayoutX(centerX - (controlWidth / 2.0));
        control.setLayoutY(y);
        return control;
    }

    private List<Node> createPlayerTargetNodes(double scaleX, double scaleY) {
        List<Node> nodes = new ArrayList<>();
        int totalPlayers = getTradePlayerCount();
        List<String> names = resolveTradePlayerNames(totalPlayers);
        int targetCount = Math.max(0, totalPlayers - 1);
        if (targetCount <= 0) {
            return nodes;
        }

        double cardW = PLAYER_TARGET_CARD_W * scaleX;
        double cardH = PLAYER_TARGET_CARD_H * scaleY;
        double gap = PLAYER_TARGET_GAP * scaleX;
        double rowY = PLAYER_TARGET_ROW_Y * scaleY;
        double startX = PLAYER_TARGET_ROW_START_X * scaleX;

        int visibleSlot = 0;
        for (int i = 0; i < totalPlayers; i++) {
            if (i == currentProposerIndex) {
                continue;
            }

            double baseX = startX + (visibleSlot * (cardW + gap));
            double baseY = rowY;
            visibleSlot++;

            StackPane targetPane = new StackPane();
            targetPane.setPrefWidth(cardW);
            targetPane.setPrefHeight(cardH);
            targetPane.setLayoutX(baseX);
            targetPane.setLayoutY(baseY);
            targetPane.getStyleClass().add("trade-target-card");

            VBox content = new VBox(6 * scaleY);
            content.setAlignment(Pos.CENTER);
            content.setPickOnBounds(false);

            ImageView targetImage;
            try {
                targetImage = new ImageView(
                        new Image(getClass().getResource(String.format(PLAYER_PROFILE_BASE_PATH, i + 1)).toExternalForm())
                );
            } catch (Exception ignored) {
                targetImage = new ImageView();
            }
            targetImage.setFitWidth(PLAYER_TARGET_IMAGE_W * scaleX);
            targetImage.setFitHeight(PLAYER_TARGET_IMAGE_H * scaleY);
            targetImage.setPreserveRatio(true);
            targetImage.setSmooth(true);
            targetImage.setMouseTransparent(true);

            Label nameLabel = new Label(i < names.size() ? names.get(i) : getPlayerNameForIndex(i + 1));
            nameLabel.getStyleClass().add("trade-target-name");
            nameLabel.setAlignment(Pos.CENTER);
            nameLabel.setPrefWidth(cardW - (12 * scaleX));
            nameLabel.setWrapText(false);
            nameLabel.setMouseTransparent(true);

            content.getChildren().addAll(targetImage, nameLabel);

            Button selectButton = new Button();
            selectButton.getStyleClass().add("trade-click-zone");
            selectButton.setOpacity(0.0);
            selectButton.setPrefWidth(cardW);
            selectButton.setPrefHeight(cardH);
            final int targetIndex = i;
            selectButton.setOnAction(e -> selectTargetPlayer(targetIndex));

            if (selectedTargetIndex == i) {
                targetPane.getStyleClass().add("trade-target-selected");
            }

            targetPane.getChildren().addAll(content, selectButton);
            nodes.add(targetPane);
        }
        return nodes;
    }

    private List<String> resolveTradePlayerNames(int totalPlayers) {
        List<String> names = new ArrayList<>();
        List<String> sessionNames = GameSession.getPlayerNames();
        for (int i = 0; i < totalPlayers; i++) {
            if (i < sessionNames.size() && sessionNames.get(i) != null && !sessionNames.get(i).isBlank()) {
                names.add(sessionNames.get(i));
            } else if (i < dummyTradeNames.size()) {
                names.add(dummyTradeNames.get(i));
            } else {
                names.add("Player " + (i + 1));
            }
        }
        return names;
    }

    private int getTradePlayerCount() {
        int sessionCount = GameSession.getPlayerNames().size();
        if (sessionCount >= 3 && sessionCount <= 4) {
            return sessionCount;
        }
        if (playerCount >= 3 && playerCount <= 4) {
            return playerCount;
        }
        return 4;
    }

    private List<Node> createPlayerTradeActionButtons(double scaleX, double scaleY) {
        List<Node> nodes = new ArrayList<>();

        Button cancelButton = new Button();
        cancelButton.getStyleClass().add("trade-click-zone");
        cancelButton.setOpacity(0.0);
        cancelButton.setLayoutX(BANK_CANCEL_BUTTON_X * scaleX);
        cancelButton.setLayoutY(BANK_CANCEL_BUTTON_Y * scaleY);
        cancelButton.setPrefWidth(BANK_CANCEL_BUTTON_W * scaleX);
        cancelButton.setPrefHeight(BANK_CANCEL_BUTTON_H * scaleY);
        cancelButton.setOnAction(e -> onPlayerTradeCancelClicked());

        Button confirmButton = new Button();
        confirmButton.getStyleClass().add("trade-click-zone");
        confirmButton.setOpacity(0.0);
        confirmButton.setLayoutX(BANK_CONFIRM_BUTTON_X * scaleX);
        confirmButton.setLayoutY(BANK_CONFIRM_BUTTON_Y * scaleY);
        confirmButton.setPrefWidth(BANK_CONFIRM_BUTTON_W * scaleX);
        confirmButton.setPrefHeight(BANK_CONFIRM_BUTTON_H * scaleY);
        confirmButton.setOnAction(e -> onPlayerTradeConfirmClicked());

        nodes.add(cancelButton);
        nodes.add(confirmButton);
        return nodes;
    }

    private int getPlayerTradeReceiveMax(ResourceType resourceType) {
        if (selectedTargetIndex < 0 || selectedTargetIndex >= playerResourceStocks.size()) {
            return PLAYER_RECEIVE_DEFAULT_MAX_WITHOUT_TARGET;
        }
        return getResourceAmount(selectedTargetIndex, resourceType);
    }

    private void increaseGiveResource(ResourceType resourceType) {
        int current = giveResourceQuantities.getOrDefault(resourceType, 0);
        int max = getResourceAmount(currentProposerIndex, resourceType);
        if (current < max) {
            giveResourceQuantities.put(resourceType, current + 1);
            System.out.println("[TradeUI] Give " + displayResource(resourceType) + " changed to " + (current + 1));
            clearPlayerTradeMessage();
            updatePlayerTradeQuantityLabels();
        }
    }

    private void decreaseGiveResource(ResourceType resourceType) {
        int current = giveResourceQuantities.getOrDefault(resourceType, 0);
        if (current > 0) {
            giveResourceQuantities.put(resourceType, current - 1);
            System.out.println("[TradeUI] Give " + displayResource(resourceType) + " changed to " + (current - 1));
            clearPlayerTradeMessage();
            updatePlayerTradeQuantityLabels();
        }
    }

    private void increaseReceiveResource(ResourceType resourceType) {
        int current = receiveResourceQuantities.getOrDefault(resourceType, 0);
        int max = getPlayerTradeReceiveMax(resourceType);
        if (current < max) {
            receiveResourceQuantities.put(resourceType, current + 1);
            System.out.println("[TradeUI] Receive " + displayResource(resourceType) + " changed to " + (current + 1));
            clearPlayerTradeMessage();
            updatePlayerTradeQuantityLabels();
        }
    }

    private void decreaseReceiveResource(ResourceType resourceType) {
        int current = receiveResourceQuantities.getOrDefault(resourceType, 0);
        if (current > 0) {
            receiveResourceQuantities.put(resourceType, current - 1);
            System.out.println("[TradeUI] Receive " + displayResource(resourceType) + " changed to " + (current - 1));
            clearPlayerTradeMessage();
            updatePlayerTradeQuantityLabels();
        }
    }

    private void selectTargetPlayer(int playerIndex) {
        if (playerIndex == currentProposerIndex) return;
        if (playerIndex < 0 || playerIndex >= playerResourceStocks.size()) return;

        selectedTargetIndex = playerIndex;
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int current = receiveResourceQuantities.getOrDefault(resourceType, 0);
            int max = getPlayerTradeReceiveMax(resourceType);
            if (current > max) {
                receiveResourceQuantities.put(resourceType, max);
            }
        }
        System.out.println("Target selected: " + getPlayerNameForIndex(playerIndex + 1));
        updateTargetPlayerDisplay();
        clearPlayerTradeMessage();
    }

    private void updatePlayerTradeQuantityLabels() {
        renderPlayerTradeUi();
    }

    private void updateTargetPlayerDisplay() {
        renderPlayerTradeUi();
    }

    private String validatePlayerTrade() {
        if (selectedTargetIndex < 0 || selectedTargetIndex >= playerResourceStocks.size()
                || selectedTargetIndex == currentProposerIndex) {
            return "Please select a target player.";
        }
        if (sumResourceQuantities(giveResourceQuantities) <= 0) {
            return "Please select at least one resource to give.";
        }
        if (sumResourceQuantities(receiveResourceQuantities) <= 0) {
            return "Please select at least one resource to receive.";
        }
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            if (giveResourceQuantities.getOrDefault(resourceType, 0) > 0
                    && receiveResourceQuantities.getOrDefault(resourceType, 0) > 0) {
                return "You cannot trade the same resource.";
            }
        }
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int askGive = giveResourceQuantities.getOrDefault(resourceType, 0);
            if (askGive > getResourceAmount(currentProposerIndex, resourceType)) {
                return "Not enough " + displayResource(resourceType) + ".";
            }
        }
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int askReceive = receiveResourceQuantities.getOrDefault(resourceType, 0);
            if (askReceive > getResourceAmount(selectedTargetIndex, resourceType)) {
                return "Target player does not have enough " + displayResource(resourceType) + ".";
            }
        }
        return null;
    }

    private void onPlayerTradeCancelClicked() {
        showTradeChoice();
    }

    private void onPlayerTradeConfirmClicked() {
        System.out.println("[TradeUI] Player trade confirm clicked");
        String validationMessage = validatePlayerTrade();
        if (validationMessage != null) {
            showPlayerTradeMessage(validationMessage, false);
            return;
        }

        clearPlayerTradeMessage();
        currentReceiverIndex = selectedTargetIndex;

        TradeOfferState offer = new TradeOfferState();
        offer.proposerIndex = currentProposerIndex;
        offer.receiverIndex = selectedTargetIndex;
        offer.give.putAll(giveResourceQuantities);
        offer.receive.putAll(receiveResourceQuantities);
        offer.counter = false;
        currentOffer = offer;

        System.out.println("[TradeUI] Player trade valid, opening review");
        showTradeOfferReview();
    }

    private void showTradeOfferReview() {
        if (currentOffer == null) {
            showTradeChoice();
            return;
        }
        System.out.println("Trade Offer opened");

        showTradeOverlayBase(TRADE_OFFER_IMAGE_PATH, TradeMode.TRADE_OFFER_REVIEW);
        tradeUiPane.getChildren().clear();

        double scaleX = currentPopupLogicalWidth / TRADE_OFFER_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / TRADE_OFFER_ASSET_HEIGHT;

        tradeUiPane.getChildren().add(createTradeOfferFromCard(currentOffer.proposerIndex, scaleX, scaleY));
        tradeUiPane.getChildren().add(createTradeOfferResourceRow(
                currentOffer.give,
                TRADE_OFFER_GIVE_ROW_X * scaleX,
                TRADE_OFFER_GIVE_ROW_Y * scaleY,
                scaleX,
                scaleY
        ));
        tradeUiPane.getChildren().add(createTradeOfferResourceRow(
                currentOffer.receive,
                TRADE_OFFER_WANT_ROW_X * scaleX,
                TRADE_OFFER_WANT_ROW_Y * scaleY,
                scaleX,
                scaleY
        ));
        tradeUiPane.toFront();

        Runnable onReject = () -> {
            String proposerName = getPlayerNameForIndex(currentOffer.proposerIndex + 1);
            String targetName = getPlayerNameForIndex(currentOffer.receiverIndex + 1);
            System.out.println("Trade Offer rejected");
            appendLog(targetName + " rejected " + proposerName + "'s trade offer.");
            showInformationPopup("Trade offer rejected.", InformationCloseAction.RETURN_TO_GAME);
        };
        Runnable onAccept = () -> {
            String proposerName = getPlayerNameForIndex(currentOffer.proposerIndex + 1);
            String targetName = getPlayerNameForIndex(currentOffer.receiverIndex + 1);
            if (!canExecuteOffer(currentOffer)) {
                appendLog("Trade failed because resources were insufficient.");
                showInformationPopup(
                        "Trade failed. One player no longer has enough resources.",
                        InformationCloseAction.RETURN_TO_GAME
                );
                return;
            }
            System.out.println("Trade Offer accepted");
            executeOffer(currentOffer);
            syncTradeStateBackToSaveData();
            appendLog(targetName + " accepted " + proposerName + "'s trade offer.");
            showInformationPopup(
                    "Trade successful! " + proposerName + " traded resources with " + targetName + ".",
                    InformationCloseAction.RETURN_TO_GAME
            );
        };
        Runnable onCounter = () -> {
            System.out.println("Counter Offer clicked");
            showCounterOffer();
        };
        renderTradeOfferBottomButtons(onReject, onAccept, onCounter);
    }

    private Node createTradeOfferFromCard(int proposerIndex, double scaleX, double scaleY) {
        StackPane fromCard = new StackPane();
        fromCard.setLayoutX(TRADE_OFFER_FROM_X * scaleX);
        fromCard.setLayoutY(TRADE_OFFER_FROM_Y * scaleY);
        fromCard.setPrefWidth(TRADE_OFFER_FROM_CARD_W * scaleX);
        fromCard.setPrefHeight(TRADE_OFFER_FROM_CARD_H * scaleY);
        fromCard.getStyleClass().add("trade-target-card");

        VBox content = new VBox(6 * scaleY);
        content.setAlignment(Pos.CENTER);
        content.setPickOnBounds(false);

        ImageView profile = new ImageView();
        try {
            profile.setImage(new Image(getClass().getResource(
                    String.format(PLAYER_PROFILE_BASE_PATH, proposerIndex + 1)).toExternalForm()));
        } catch (Exception ignored) {
        }
        profile.setFitWidth(TRADE_OFFER_FROM_IMAGE_W * scaleX);
        profile.setFitHeight(TRADE_OFFER_FROM_IMAGE_H * scaleY);
        profile.setPreserveRatio(true);
        profile.setSmooth(true);
        profile.setMouseTransparent(true);

        Label name = new Label(getPlayerNameForIndex(proposerIndex + 1));
        name.getStyleClass().add("trade-target-name");
        name.setAlignment(Pos.CENTER);
        name.setPrefWidth(TRADE_OFFER_FROM_CARD_W * scaleX - (12 * scaleX));
        name.setMouseTransparent(true);

        content.getChildren().addAll(profile, name);
        fromCard.getChildren().add(content);
        return fromCard;
    }

    private Node createTradeOfferResourceRow(Map<ResourceType, Integer> quantities,
                                             double startX,
                                             double y,
                                             double scaleX,
                                             double scaleY) {
        HBox row = new HBox(TRADE_OFFER_RESOURCE_GAP * scaleX);
        row.getStyleClass().add("trade-offer-resource-row");
        row.setLayoutX(startX);
        row.setLayoutY(y);
        row.setAlignment(Pos.CENTER_LEFT);

        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int quantity = Math.max(0, quantities.getOrDefault(resourceType, 0));
            if (quantity <= 0) {
                continue;
            }

            VBox item = new VBox(6 * scaleY);
            item.getStyleClass().add("trade-offer-resource-item");
            item.setAlignment(Pos.CENTER);
            item.setPrefWidth(TRADE_OFFER_RESOURCE_CARD_W * scaleX);

            ImageView card = createResourceIcon(resourceType);
            card.setFitWidth(TRADE_OFFER_RESOURCE_CARD_W * scaleX);
            card.setFitHeight(TRADE_OFFER_RESOURCE_CARD_H * scaleY);
            card.setMouseTransparent(true);

            Label qty = new Label("x" + quantity);
            qty.getStyleClass().add("trade-offer-quantity-label");
            qty.setAlignment(Pos.CENTER);
            qty.setPrefWidth(TRADE_OFFER_RESOURCE_CARD_W * scaleX);
            qty.setMouseTransparent(true);

            item.getChildren().addAll(card, qty);
            row.getChildren().add(item);
        }

        if (row.getChildren().isEmpty()) {
            Label empty = new Label("-");
            empty.getStyleClass().add("trade-offer-quantity-label");
            row.getChildren().add(empty);
        }
        return row;
    }

    private boolean canExecuteOffer(TradeOfferState offer) {
        if (offer == null) return false;
        return playerHasEnough(offer.proposerIndex, offer.give)
                && playerHasEnough(offer.receiverIndex, offer.receive);
    }

    private void showCounterOffer() {
        showTradeOverlayBase(TRADE_COUNTER_IMAGE_PATH, TradeMode.COUNTER_OFFER);
        hideTradeActions();
        setBankTradeActionButtonsVisible(false);
        clearTradeError();
        clearCounterOfferMessage();

        if (currentOffer == null) {
            showTradeChoice();
            return;
        }

        counterProposerIndex = currentOffer.receiverIndex;
        counterResponderIndex = currentOffer.proposerIndex;
        currentProposerIndex = counterProposerIndex;
        currentReceiverIndex = counterResponderIndex;

        counterGiveQuantities.clear();
        counterReceiveQuantities.clear();
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            counterGiveQuantities.put(resourceType, getResourceAmount(counterProposerIndex, resourceType));
            counterReceiveQuantities.put(resourceType, 0);
        }

        System.out.println("Counter Offer opened");
        renderCounterOfferUi();
    }

    private void renderCounterOfferUi() {
        if (currentTradeMode != TradeMode.COUNTER_OFFER) return;

        tradeUiPane.getChildren().clear();
        tradeUiPane.setVisible(true);
        tradeUiPane.setManaged(true);

        double scaleX = currentPopupLogicalWidth / COUNTER_POPUP_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / COUNTER_POPUP_ASSET_HEIGHT;

        for (int i = 0; i < TRADE_RESOURCE_ORDER.size(); i++) {
            ResourceType resourceType = TRADE_RESOURCE_ORDER.get(i);
            double centerX = (COUNTER_RESOURCE_CENTER_X_BASE + COUNTER_RESOURCE_CENTER_X_STEPS[i]) * scaleX;

            tradeUiPane.getChildren().addAll(
                    createCounterOfferCardNode(resourceType, centerX, COUNTER_GIVE_CARD_Y * scaleY, scaleX, scaleY),
                    createCounterOfferQuantityControl(resourceType, true, centerX, COUNTER_CONTROL_Y_GIVE * scaleY, scaleX, scaleY),
                    createCounterOfferCardNode(resourceType, centerX, COUNTER_RECEIVE_CARD_Y * scaleY, scaleX, scaleY),
                    createCounterOfferQuantityControl(resourceType, false, centerX, COUNTER_CONTROL_Y_RECEIVE * scaleY, scaleX, scaleY)
            );
        }

        tradeUiPane.getChildren().add(createCounterRespondToNode(scaleX, scaleY));
        tradeUiPane.getChildren().addAll(createCounterOfferActionButtons(scaleX, scaleY));
        updateCounterOfferMessagePlacement(scaleX, scaleY);

        tradeUiPane.toFront();
        playerTradeMessageLabel.toFront();
        popupCloseButton.toFront();
    }

    private Node createCounterOfferCardNode(ResourceType resourceType, double centerX, double y, double scaleX, double scaleY) {
        double cardW = COUNTER_CARD_WIDTH * scaleX;
        double cardH = COUNTER_CARD_HEIGHT * scaleY;

        ImageView cardImage = createResourceIcon(resourceType);
        cardImage.setFitWidth(cardW);
        cardImage.setFitHeight(cardH);
        cardImage.setLayoutX(centerX - cardW / 2.0);
        cardImage.setLayoutY(y);
        cardImage.getStyleClass().add("trade-resource-card");
        cardImage.setMouseTransparent(true);
        return cardImage;
    }

    private Node createCounterOfferQuantityControl(ResourceType resourceType,
                                                   boolean giveSide,
                                                   double centerX,
                                                   double y,
                                                   double scaleX,
                                                   double scaleY) {
        int value = (giveSide ? counterGiveQuantities : counterReceiveQuantities).getOrDefault(resourceType, 0);
        int max = giveSide
                ? getResourceAmount(counterProposerIndex, resourceType)
                : getResourceAmount(counterResponderIndex, resourceType);
        double buttonW = COUNTER_QTY_BUTTON_W * scaleX;
        double buttonH = COUNTER_QTY_BUTTON_H * scaleY;
        double labelW = COUNTER_QTY_LABEL_W * scaleX;
        double labelH = COUNTER_QTY_LABEL_H * scaleY;
        double gap = COUNTER_QTY_CONTROL_GAP * scaleX;

        Button minusButton = new Button("-");
        minusButton.getStyleClass().add("trade-small-button");
        minusButton.setPrefWidth(buttonW);
        minusButton.setPrefHeight(buttonH);
        minusButton.setDisable(value <= 0);
        minusButton.setOnAction(e -> {
            if (giveSide) {
                decreaseCounterGiveResource(resourceType);
            } else {
                decreaseCounterReceiveResource(resourceType);
            }
        });

        Label qtyLabel = new Label(String.valueOf(value));
        qtyLabel.getStyleClass().add("trade-quantity-label");
        qtyLabel.setPrefWidth(labelW);
        qtyLabel.setPrefHeight(labelH);
        qtyLabel.setAlignment(Pos.CENTER);

        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("trade-small-button");
        plusButton.setPrefWidth(buttonW);
        plusButton.setPrefHeight(buttonH);
        plusButton.setDisable(value >= max);
        plusButton.setOnAction(e -> {
            if (giveSide) {
                increaseCounterGiveResource(resourceType);
            } else {
                increaseCounterReceiveResource(resourceType);
            }
        });

        HBox control = new HBox(gap, minusButton, qtyLabel, plusButton);
        control.setAlignment(Pos.CENTER);
        double controlWidth = buttonW + labelW + buttonW + (2 * gap);
        control.setLayoutX(centerX - (controlWidth / 2.0));
        control.setLayoutY(y);
        return control;
    }

    private Node createCounterRespondToNode(double scaleX, double scaleY) {
        StackPane respondPane = new StackPane();
        double cardW = COUNTER_RESPOND_TO_CARD_W * scaleX;
        double cardH = COUNTER_RESPOND_TO_CARD_H * scaleY;
        respondPane.setPrefWidth(cardW);
        respondPane.setPrefHeight(cardH);
        respondPane.setLayoutX(COUNTER_RESPOND_TO_CARD_X * scaleX);
        respondPane.setLayoutY(COUNTER_RESPOND_TO_CARD_Y * scaleY);
        respondPane.getStyleClass().add("trade-target-card");

        VBox content = new VBox(6 * scaleY);
        content.setAlignment(Pos.CENTER);
        content.setPickOnBounds(false);

        ImageView profile = new ImageView();
        try {
            profile.setImage(new Image(getClass().getResource(
                    String.format(PLAYER_PROFILE_BASE_PATH, counterResponderIndex + 1)).toExternalForm()));
        } catch (Exception ignored) {
        }
        profile.setFitWidth(COUNTER_RESPOND_TO_IMAGE_W * scaleX);
        profile.setFitHeight(COUNTER_RESPOND_TO_IMAGE_H * scaleY);
        profile.setPreserveRatio(true);
        profile.setSmooth(true);
        profile.setMouseTransparent(true);

        Label name = new Label(getPlayerNameForIndex(counterResponderIndex + 1));
        name.getStyleClass().add("trade-target-name");
        name.setAlignment(Pos.CENTER);
        name.setPrefWidth(cardW - (12 * scaleX));
        name.setMouseTransparent(true);

        content.getChildren().addAll(profile, name);
        respondPane.getChildren().add(content);
        return respondPane;
    }

    private List<Node> createCounterOfferActionButtons(double scaleX, double scaleY) {
        List<Node> nodes = new ArrayList<>();

        Button cancelButton = new Button();
        cancelButton.getStyleClass().add("trade-click-zone");
        cancelButton.setOpacity(0.0);
        cancelButton.setLayoutX(COUNTER_CANCEL_BUTTON_X * scaleX);
        cancelButton.setLayoutY(COUNTER_CANCEL_BUTTON_Y * scaleY);
        cancelButton.setPrefWidth(COUNTER_CANCEL_BUTTON_W * scaleX);
        cancelButton.setPrefHeight(COUNTER_CANCEL_BUTTON_H * scaleY);
        cancelButton.setOnAction(e -> onCounterOfferCancelClicked());

        Button sendButton = new Button();
        sendButton.getStyleClass().add("trade-click-zone");
        sendButton.setOpacity(0.0);
        sendButton.setLayoutX(COUNTER_SEND_BUTTON_X * scaleX);
        sendButton.setLayoutY(COUNTER_SEND_BUTTON_Y * scaleY);
        sendButton.setPrefWidth(COUNTER_SEND_BUTTON_W * scaleX);
        sendButton.setPrefHeight(COUNTER_SEND_BUTTON_H * scaleY);
        sendButton.setOnAction(e -> onCounterOfferSendClicked());

        nodes.add(cancelButton);
        nodes.add(sendButton);
        return nodes;
    }

    private void updateCounterOfferMessagePlacement(double scaleX, double scaleY) {
        double messageWidth = Math.min(COUNTER_MESSAGE_W * scaleX, currentPopupLogicalWidth - (80 * scaleX));
        double messageX = (currentPopupLogicalWidth - messageWidth) / 2.0;
        double buttonTopY = COUNTER_CANCEL_BUTTON_Y * scaleY;
        double messageY = buttonTopY - (COUNTER_MESSAGE_GAP_ABOVE_BUTTONS * scaleY);

        playerTradeMessageLabel.setLayoutX(messageX);
        playerTradeMessageLabel.setLayoutY(messageY);
        playerTradeMessageLabel.setPrefWidth(messageWidth);
        playerTradeMessageLabel.setPrefHeight(COUNTER_MESSAGE_H * scaleY);
        playerTradeMessageLabel.setAlignment(Pos.CENTER);
    }

    private void increaseCounterGiveResource(ResourceType resourceType) {
        int current = counterGiveQuantities.getOrDefault(resourceType, 0);
        int max = getResourceAmount(counterProposerIndex, resourceType);
        if (current < max) {
            int next = current + 1;
            counterGiveQuantities.put(resourceType, next);
            System.out.println("Counter Give " + displayResource(resourceType) + " changed to " + next);
            clearCounterOfferMessage();
            updateCounterOfferQuantityLabels();
        }
    }

    private void decreaseCounterGiveResource(ResourceType resourceType) {
        int current = counterGiveQuantities.getOrDefault(resourceType, 0);
        if (current > 0) {
            int next = current - 1;
            counterGiveQuantities.put(resourceType, next);
            System.out.println("Counter Give " + displayResource(resourceType) + " changed to " + next);
            clearCounterOfferMessage();
            updateCounterOfferQuantityLabels();
        }
    }

    private void increaseCounterReceiveResource(ResourceType resourceType) {
        int current = counterReceiveQuantities.getOrDefault(resourceType, 0);
        int max = getResourceAmount(counterResponderIndex, resourceType);
        if (current < max) {
            int next = current + 1;
            counterReceiveQuantities.put(resourceType, next);
            System.out.println("Counter Receive " + displayResource(resourceType) + " changed to " + next);
            clearCounterOfferMessage();
            updateCounterOfferQuantityLabels();
        }
    }

    private void decreaseCounterReceiveResource(ResourceType resourceType) {
        int current = counterReceiveQuantities.getOrDefault(resourceType, 0);
        if (current > 0) {
            int next = current - 1;
            counterReceiveQuantities.put(resourceType, next);
            System.out.println("Counter Receive " + displayResource(resourceType) + " changed to " + next);
            clearCounterOfferMessage();
            updateCounterOfferQuantityLabels();
        }
    }

    private void updateCounterOfferQuantityLabels() {
        renderCounterOfferUi();
    }

    private void updateCounterRespondToDisplay() {
        renderCounterOfferUi();
    }

    private void showCounterOfferMessage(String message) {
        if (playerTradeMessageLabel == null) return;
        playerTradeMessageLabel.getStyleClass().remove("trade-success-label");
        playerTradeMessageLabel.setText(message);
        playerTradeMessageLabel.setVisible(true);
        playerTradeMessageLabel.setManaged(true);
        playerTradeMessageLabel.setAlignment(Pos.CENTER);
        playerTradeMessageLabel.toFront();
    }

    private void clearCounterOfferMessage() {
        if (playerTradeMessageLabel == null) return;
        playerTradeMessageLabel.setText("");
        playerTradeMessageLabel.setVisible(false);
        playerTradeMessageLabel.setManaged(false);
    }

    private void onCounterOfferCancelClicked() {
        System.out.println("Counter Offer cancelled, returning to Trade Offer");
        clearCounterOfferMessage();
        showTradeOfferReview();
    }

    private void onCounterOfferSendClicked() {
        System.out.println("Counter Offer send clicked");
        String validationMessage = validateCounterOffer();
        if (validationMessage != null) {
            showCounterOfferMessage(validationMessage);
            return;
        }

        clearCounterOfferMessage();

        TradeOfferState offer = new TradeOfferState();
        offer.proposerIndex = counterProposerIndex;
        offer.receiverIndex = counterResponderIndex;
        offer.give.putAll(counterGiveQuantities);
        offer.receive.putAll(counterReceiveQuantities);
        offer.counter = true;
        currentOffer = offer;

        System.out.println("Counter Offer valid, opening Review Counter");
        showReviewCounter();
    }

    private String validateCounterOffer() {
        if (sumResourceQuantities(counterGiveQuantities) <= 0) {
            return "Please select at least one resource to give.";
        }
        if (sumResourceQuantities(counterReceiveQuantities) <= 0) {
            return "Please select at least one resource to receive.";
        }

        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            if (counterGiveQuantities.getOrDefault(resourceType, 0) > 0
                    && counterReceiveQuantities.getOrDefault(resourceType, 0) > 0) {
                return "You cannot trade the same resource.";
            }
        }

        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int askGive = counterGiveQuantities.getOrDefault(resourceType, 0);
            if (askGive > getResourceAmount(counterProposerIndex, resourceType)) {
                return "Not enough " + displayResource(resourceType) + ".";
            }
        }

        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int askReceive = counterReceiveQuantities.getOrDefault(resourceType, 0);
            if (askReceive > getResourceAmount(counterResponderIndex, resourceType)) {
                return "Responder does not have enough " + displayResource(resourceType) + ".";
            }
        }
        return null;
    }

    private void onConfirmCounterOffer() {
        onCounterOfferSendClicked();
    }

    private void showReviewCounter() {
        if (currentOffer == null) {
            showTradeChoice();
            return;
        }
        System.out.println("Review Counter opened");

        showTradeOverlayBase(TRADE_REVIEW_COUNTER_IMAGE_PATH, TradeMode.REVIEW_COUNTER);
        hideTradeActions();
        setBankTradeActionButtonsVisible(false);
        clearTradeError();
        clearCounterOfferMessage();

        tradeUiPane.getChildren().clear();
        double scaleX = currentPopupLogicalWidth / TRADE_OFFER_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / TRADE_OFFER_ASSET_HEIGHT;

        tradeUiPane.getChildren().add(renderReviewCounterFrom(scaleX, scaleY));
        tradeUiPane.getChildren().add(renderReviewCounterGiveResources(scaleX, scaleY));
        tradeUiPane.getChildren().add(renderReviewCounterReceiveResources(scaleX, scaleY));
        renderTradeOfferBottomButtons(
                this::onReviewCounterRejectClicked,
                this::onReviewCounterAcceptClicked,
                this::onReviewCounterCounterOfferClicked
        );
        tradeUiPane.toFront();
        popupCloseButton.toFront();
    }

    private Node renderReviewCounterFrom(double scaleX, double scaleY) {
        return createTradeOfferFromCard(currentOffer.proposerIndex, scaleX, scaleY);
    }

    private Node renderReviewCounterGiveResources(double scaleX, double scaleY) {
        return createTradeOfferResourceRow(
                currentOffer.give,
                TRADE_OFFER_GIVE_ROW_X * scaleX,
                TRADE_OFFER_GIVE_ROW_Y * scaleY,
                scaleX,
                scaleY
        );
    }

    private Node renderReviewCounterReceiveResources(double scaleX, double scaleY) {
        return createTradeOfferResourceRow(
                currentOffer.receive,
                TRADE_OFFER_WANT_ROW_X * scaleX,
                TRADE_OFFER_WANT_ROW_Y * scaleY,
                scaleX,
                scaleY
        );
    }

    private void onReviewCounterAcceptClicked() {
        if (currentOffer == null) {
            hideTradeOverlay();
            return;
        }
        String proposerName = getPlayerNameForIndex(currentOffer.proposerIndex + 1);
        String targetName = getPlayerNameForIndex(currentOffer.receiverIndex + 1);

        if (!canExecuteOffer(currentOffer)) {
            System.out.println("Review Counter accepted");
            appendLog("Counter trade failed because resources were insufficient.");
            clearCurrentTradeOffer();
            resetTemporaryTradeState();
            showInformationPopup(
                    "Counter trade failed. One player no longer has enough resources.",
                    InformationCloseAction.RETURN_TO_GAME
            );
            return;
        }

        executeOffer(currentOffer);
        syncTradeStateBackToSaveData();
        System.out.println("Review Counter accepted");
        appendLog(targetName + " accepted " + proposerName + "'s counter offer.");
        clearCurrentTradeOffer();
        resetTemporaryTradeState();
        showInformationPopup(
                "Counter trade successful! " + proposerName + " traded resources with " + targetName + ".",
                InformationCloseAction.RETURN_TO_GAME
        );
    }

    private void onReviewCounterRejectClicked() {
        if (currentOffer == null) {
            hideTradeOverlay();
            return;
        }
        String proposerName = getPlayerNameForIndex(currentOffer.proposerIndex + 1);
        String targetName = getPlayerNameForIndex(currentOffer.receiverIndex + 1);
        System.out.println("Review Counter rejected");
        appendLog(targetName + " rejected " + proposerName + "'s counter offer.");
        clearCurrentTradeOffer();
        resetTemporaryTradeState();
        showInformationPopup("Counter offer rejected.", InformationCloseAction.RETURN_TO_GAME);
    }

    private void onReviewCounterCounterOfferClicked() {
        if (currentOffer == null) {
            hideTradeOverlay();
            return;
        }
        String proposerName = getPlayerNameForIndex(currentOffer.proposerIndex + 1);
        String targetName = getPlayerNameForIndex(currentOffer.receiverIndex + 1);
        System.out.println("Review Counter countered again");
        appendLog(targetName + " created a new counter offer for " + proposerName + ".");
        System.out.println("Opening Counter Offer from Review Counter");
        showCounterOffer();
    }

    private void renderTradeOfferBottomButtons(Runnable onReject, Runnable onAccept, Runnable onCounter) {
        hideTradeActions();

        double scaleX = currentPopupLogicalWidth / TRADE_OFFER_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / TRADE_OFFER_ASSET_HEIGHT;

        Button rejectButton = createTradeOfferBottomButton(
                TRADE_OFFER_ACTION_REJECT_X * scaleX,
                TRADE_OFFER_ACTION_Y * scaleY,
                TRADE_OFFER_ACTION_BUTTON_W * scaleX,
                TRADE_OFFER_ACTION_BUTTON_H * scaleY,
                "REJECT",
                onReject
        );
        Button acceptButton = createTradeOfferBottomButton(
                TRADE_OFFER_ACTION_ACCEPT_X * scaleX,
                TRADE_OFFER_ACTION_Y * scaleY,
                TRADE_OFFER_ACTION_BUTTON_W * scaleX,
                TRADE_OFFER_ACTION_BUTTON_H * scaleY,
                "ACCEPT",
                onAccept
        );
        Button counterButton = createTradeOfferBottomButton(
                TRADE_OFFER_ACTION_COUNTER_X * scaleX,
                TRADE_OFFER_ACTION_Y * scaleY,
                TRADE_OFFER_ACTION_BUTTON_W * scaleX,
                TRADE_OFFER_ACTION_BUTTON_H * scaleY,
                "COUNTER OFFER",
                onCounter
        );
        tradeUiPane.getChildren().addAll(rejectButton, acceptButton, counterButton);
        rejectButton.toFront();
        acceptButton.toFront();
        counterButton.toFront();
    }

    private Button createTradeOfferBottomButton(double x,
                                                double y,
                                                double w,
                                                double h,
                                                String debugName,
                                                Runnable action) {
        Button button = new Button();
        button.getStyleClass().add("trade-click-zone");
        button.setOpacity(0.0);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefWidth(w);
        button.setPrefHeight(h);
        button.setDisable(false);
        button.setMouseTransparent(false);
        button.setOnAction(e -> {
            System.out.println("[TradeUI] clicked action: " + debugName);
            action.run();
        });
        return button;
    }

    private void showBankTrade() {
        System.out.println("[TradeUI] showBankTrade called");
        showTradeOverlayBase(TRADE_BANK_IMAGE_PATH, TradeMode.BANK_TRADE);
        setBankTradeControlsVisible(false); // old static controls are intentionally disabled.
        setBankTradeActionButtonsVisible(true);
        renderBankTradeDynamicUi();
        showBankTradeMessage("", false);
        bringBankTradeControlsToFront();
    }

    private void setBankTradeActionButtonsVisible(boolean visible) {
        bankTradeCancelButton.setVisible(visible);
        bankTradeCancelButton.setManaged(visible);
        bankTradeCancelButton.setDisable(!visible);
        bankTradeCancelButton.setMouseTransparent(!visible);

        bankTradeConfirmButton.setVisible(visible);
        bankTradeConfirmButton.setManaged(visible);
        bankTradeConfirmButton.setDisable(!visible);
        bankTradeConfirmButton.setMouseTransparent(!visible);
    }

    private void bringBankTradeControlsToFront() {
        tradeUiPane.toFront();
        tradeErrorLabel.toFront();
        bankTradeCancelButton.toFront();
        bankTradeConfirmButton.toFront();
        popupCloseButton.toFront();
    }

    private void showBankTradeMessage(String message, boolean success) {
        bankTradeMessageLabel.setText("");
        bankTradeMessageLabel.setVisible(false);
        bankTradeMessageLabel.setManaged(false);

        if (message == null || message.isBlank()) {
            tradeErrorLabel.setText("");
            tradeErrorLabel.setVisible(false);
            tradeErrorLabel.setManaged(false);
            return;
        }
        tradeErrorLabel.setText(message);
        tradeErrorLabel.setStyle(success
                ? "-fx-text-fill: #1a8917; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center;"
                : "-fx-text-fill: #b7261e; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center;");
        tradeErrorLabel.setVisible(true);
        tradeErrorLabel.setManaged(true);
        tradeErrorLabel.toFront();
    }

    private void updateBankTradeQuantityLabels() {
        if (currentTradeMode == TradeMode.BANK_TRADE) {
            renderBankTradeDynamicUi();
        }
    }

    private void updateBankTradeChecklist() {
        if (currentTradeMode == TradeMode.BANK_TRADE) {
            renderBankTradeDynamicUi();
        }
    }

    private void renderBankTradeDynamicUi() {
        if (currentTradeMode != TradeMode.BANK_TRADE) return;
        double scaleX = currentPopupLogicalWidth / BANK_POPUP_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / BANK_POPUP_ASSET_HEIGHT;
        double avgScale = (scaleX + scaleY) / 2.0;
        double xOffset = BANK_TRADE_X_OFFSET * scaleX;

        tradeUiPane.getChildren().clear();
        tradeUiPane.setVisible(true);
        tradeUiPane.setManaged(true);

        ResourceTypeUI[] order = ResourceTypeUI.values();
        for (int i = 0; i < order.length; i++) {
            ResourceTypeUI resourceTypeUI = order[i];
            ResourceType modelType = toModelResource(resourceTypeUI);
            if (modelType == null) continue;

            double centerAssetX = BANK_RESOURCE_CENTER_X_BASE + BANK_RESOURCE_CENTER_X_STEPS[i];
            double centerX = centerAssetX * scaleX + xOffset;
            double topCardY = BANK_TOP_CARD_Y * scaleY;
            double bottomCardY = BANK_BOTTOM_CARD_Y * scaleY;
            double cardW = BANK_CARD_SELECT_W * scaleX;
            double cardH = BANK_CARD_SELECT_H * scaleY;

            ImageView bankCardImage = createResourceIcon(modelType);
            bankCardImage.setFitWidth(cardW);
            bankCardImage.setFitHeight(cardH);
            bankCardImage.setLayoutX(centerX - cardW / 2.0);
            bankCardImage.setLayoutY(topCardY);
            bankCardImage.setMouseTransparent(true);

            ImageView youCardImage = createResourceIcon(modelType);
            youCardImage.setFitWidth(cardW);
            youCardImage.setFitHeight(cardH);
            youCardImage.setLayoutX(centerX - cardW / 2.0);
            youCardImage.setLayoutY(bottomCardY);
            youCardImage.setMouseTransparent(true);

            Button bankSelect = new Button();
            bankSelect.getStyleClass().add("trade-click-zone");
            bankSelect.setOpacity(0.0);
            bankSelect.setLayoutX(centerX - cardW / 2.0);
            bankSelect.setLayoutY(topCardY);
            bankSelect.setPrefWidth(cardW);
            bankSelect.setPrefHeight(cardH);
            bankSelect.setOnAction(e -> onBankResourceClicked(resourceTypeUI));
            bankSelect.setFocusTraversable(false);

            Button youSelect = new Button();
            youSelect.getStyleClass().add("trade-click-zone");
            youSelect.setOpacity(0.0);
            youSelect.setLayoutX(centerX - cardW / 2.0);
            youSelect.setLayoutY(bottomCardY);
            youSelect.setPrefWidth(cardW);
            youSelect.setPrefHeight(cardH);
            youSelect.setOnAction(e -> onYouResourceClicked(resourceTypeUI));
            youSelect.setFocusTraversable(false);

            Label bankQty = new Label(String.valueOf(dummyBankResources.getOrDefault(resourceTypeUI, 0)));
            bankQty.setStyle("-fx-text-fill: #2f2010; -fx-font-size: 18px; -fx-font-weight: bold;");
            bankQty.setAlignment(Pos.CENTER);
            bankQty.setLayoutX(centerX - (BANK_QTY_W * avgScale) / 2.0);
            bankQty.setLayoutY(BANK_QTY_Y_TOP * scaleY);
            bankQty.setPrefWidth(BANK_QTY_W * avgScale);
            bankQty.setPrefHeight(BANK_QTY_H * avgScale);
            bankQty.setMouseTransparent(true);

            Label youQty = new Label(String.valueOf(dummyPlayerResources.getOrDefault(resourceTypeUI, 0)));
            youQty.setStyle("-fx-text-fill: #2f2010; -fx-font-size: 18px; -fx-font-weight: bold;");
            youQty.setAlignment(Pos.CENTER);
            youQty.setLayoutX(centerX - (BANK_QTY_W * avgScale) / 2.0);
            youQty.setLayoutY(BANK_QTY_Y_BOTTOM * scaleY);
            youQty.setPrefWidth(BANK_QTY_W * avgScale);
            youQty.setPrefHeight(BANK_QTY_H * avgScale);
            youQty.setMouseTransparent(true);

            boolean bankSelected = selectedBankReceiveResourceUi == resourceTypeUI;
            boolean youSelected = selectedPlayerGiveResourceUi == resourceTypeUI;

            Label bankCircle = new Label(bankSelected ? "●" : "○");
            bankCircle.setStyle(bankSelected
                    ? "-fx-text-fill: #7a4a23; -fx-font-size: 24px; -fx-font-weight: bold;"
                    : "-fx-text-fill: #21150c; -fx-font-size: 24px;");
            bankCircle.setAlignment(Pos.CENTER);
            bankCircle.setLayoutX(centerX - (BANK_CIRCLE_W * avgScale) / 2.0);
            bankCircle.setLayoutY(BANK_CIRCLE_Y_TOP * scaleY);
            bankCircle.setPrefWidth(BANK_CIRCLE_W * avgScale);
            bankCircle.setPrefHeight(BANK_CIRCLE_H * avgScale);
            bankCircle.setMouseTransparent(true);

            Label youCircle = new Label(youSelected ? "●" : "○");
            youCircle.setStyle(youSelected
                    ? "-fx-text-fill: #7a4a23; -fx-font-size: 24px; -fx-font-weight: bold;"
                    : "-fx-text-fill: #21150c; -fx-font-size: 24px;");
            youCircle.setAlignment(Pos.CENTER);
            youCircle.setLayoutX(centerX - (BANK_CIRCLE_W * avgScale) / 2.0);
            youCircle.setLayoutY(BANK_CIRCLE_Y_BOTTOM * scaleY);
            youCircle.setPrefWidth(BANK_CIRCLE_W * avgScale);
            youCircle.setPrefHeight(BANK_CIRCLE_H * avgScale);
            youCircle.setMouseTransparent(true);

            tradeUiPane.getChildren().addAll(
                    bankCardImage, youCardImage,
                    bankSelect, youSelect,
                    bankQty, youQty,
                    bankCircle, youCircle
            );
        }

        bankTradeCancelButton.setLayoutX(BANK_CANCEL_BUTTON_X * scaleX);
        bankTradeCancelButton.setLayoutY(BANK_CANCEL_BUTTON_Y * scaleY);
        bankTradeCancelButton.setPrefWidth(BANK_CANCEL_BUTTON_W * scaleX);
        bankTradeCancelButton.setPrefHeight(BANK_CANCEL_BUTTON_H * scaleY);
        bankTradeCancelButton.setOpacity(0.0);
        bankTradeCancelButton.setVisible(true);
        bankTradeCancelButton.setManaged(true);
        bankTradeCancelButton.setDisable(false);
        bankTradeCancelButton.setMouseTransparent(false);

        bankTradeConfirmButton.setLayoutX(BANK_CONFIRM_BUTTON_X * scaleX);
        bankTradeConfirmButton.setLayoutY(BANK_CONFIRM_BUTTON_Y * scaleY);
        bankTradeConfirmButton.setPrefWidth(BANK_CONFIRM_BUTTON_W * scaleX);
        bankTradeConfirmButton.setPrefHeight(BANK_CONFIRM_BUTTON_H * scaleY);
        bankTradeConfirmButton.setOpacity(0.0);
        bankTradeConfirmButton.setVisible(true);
        bankTradeConfirmButton.setManaged(true);
        bankTradeConfirmButton.setDisable(false);
        bankTradeConfirmButton.setMouseTransparent(false);

        tradeErrorLabel.setLayoutX(BANK_MESSAGE_X * scaleX);
        tradeErrorLabel.setLayoutY(BANK_MESSAGE_Y * scaleY);
        tradeErrorLabel.setPrefWidth(BANK_MESSAGE_W * scaleX);
        tradeErrorLabel.setPrefHeight(BANK_MESSAGE_H * scaleY);

        bringBankTradeControlsToFront();
    }

    private void setLabelVisible(Label label, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
    }

    private void onBankResourceClicked(ResourceTypeUI resourceType) {
        selectedBankReceiveResourceUi = (selectedBankReceiveResourceUi == resourceType) ? null : resourceType;
        System.out.println("Bank selected: " + resourceType);
        updateBankTradeChecklist();
        showBankTradeMessage("", false);
    }

    private void onYouResourceClicked(ResourceTypeUI resourceType) {
        selectedPlayerGiveResourceUi = (selectedPlayerGiveResourceUi == resourceType) ? null : resourceType;
        System.out.println("You selected: " + resourceType);
        updateBankTradeChecklist();
        showBankTradeMessage("", false);
    }

    private String validateBankTrade() {
        if (selectedBankReceiveResourceUi == null) {
            return "Please select a resource from bank.";
        }
        if (selectedPlayerGiveResourceUi == null) {
            return "Please select a resource from you.";
        }
        if (selectedBankReceiveResourceUi == selectedPlayerGiveResourceUi) {
            return "You cannot trade the same resource.";
        }

        int playerAmount = dummyPlayerResources.getOrDefault(selectedPlayerGiveResourceUi, 0);
        if (playerAmount < 4) {
            String name = displayResource(selectedPlayerGiveResourceUi);
            return "Not enough " + name + ". You need 4 " + name + ".";
        }

        int bankAmount = dummyBankResources.getOrDefault(selectedBankReceiveResourceUi, 0);
        if (bankAmount < 1) {
            return "Bank does not have enough " + displayResource(selectedBankReceiveResourceUi) + ".";
        }

        return null;
    }

    private void applyBankTradeToMainStocks() {
        ResourceType give = toModelResource(selectedPlayerGiveResourceUi);
        ResourceType receive = toModelResource(selectedBankReceiveResourceUi);
        if (give == null || receive == null) return;

        int givePlayer = dummyPlayerResources.getOrDefault(selectedPlayerGiveResourceUi, 0);
        int giveBank = dummyBankResources.getOrDefault(selectedPlayerGiveResourceUi, 0);
        int receivePlayer = dummyPlayerResources.getOrDefault(selectedBankReceiveResourceUi, 0);
        int receiveBank = dummyBankResources.getOrDefault(selectedBankReceiveResourceUi, 0);

        addResourceAmount(activePlayerIndex, give, givePlayer - getResourceAmount(activePlayerIndex, give));
        addBankResourceAmount(give, giveBank - getBankResourceAmount(give));
        addResourceAmount(activePlayerIndex, receive, receivePlayer - getResourceAmount(activePlayerIndex, receive));
        addBankResourceAmount(receive, receiveBank - getBankResourceAmount(receive));
        syncTradeStateBackToSaveData();
    }

    private ResourceType toModelResource(ResourceTypeUI resourceTypeUI) {
        if (resourceTypeUI == null) return null;
        return switch (resourceTypeUI) {
            case BANANA -> ResourceType.BANANA;
            case WOOD -> ResourceType.WOOD;
            case WHEAT -> ResourceType.WHEAT;
            case ORE -> ResourceType.ORE;
            case BRICK -> ResourceType.BRICK;
        };
    }

    private String displayResource(ResourceTypeUI resourceTypeUI) {
        return switch (resourceTypeUI) {
            case BANANA -> "Banana";
            case WOOD -> "Wood";
            case WHEAT -> "Wheat";
            case ORE -> "Ore";
            case BRICK -> "Brick";
        };
    }

    @FXML
    private void onBankBananaSelected() { onBankResourceClicked(ResourceTypeUI.BANANA); }
    @FXML
    private void onBankWoodSelected() { onBankResourceClicked(ResourceTypeUI.WOOD); }
    @FXML
    private void onBankWheatSelected() { onBankResourceClicked(ResourceTypeUI.WHEAT); }
    @FXML
    private void onBankOreSelected() { onBankResourceClicked(ResourceTypeUI.ORE); }
    @FXML
    private void onBankBrickSelected() { onBankResourceClicked(ResourceTypeUI.BRICK); }

    @FXML
    private void onYouBananaSelected() { onYouResourceClicked(ResourceTypeUI.BANANA); }
    @FXML
    private void onYouWoodSelected() { onYouResourceClicked(ResourceTypeUI.WOOD); }
    @FXML
    private void onYouWheatSelected() { onYouResourceClicked(ResourceTypeUI.WHEAT); }
    @FXML
    private void onYouOreSelected() { onYouResourceClicked(ResourceTypeUI.ORE); }
    @FXML
    private void onYouBrickSelected() { onYouResourceClicked(ResourceTypeUI.BRICK); }

    @FXML
    private void onBankTradeCancelClicked() {
        System.out.println("Bank trade cancel clicked");
        selectedBankReceiveResourceUi = null;
        selectedPlayerGiveResourceUi = null;
        showBankTradeMessage("", false);
        showTradeChoice();
    }

    @FXML
    private void onBankTradeConfirmClicked() {
        System.out.println("Bank trade confirm clicked");
        String error = validateBankTrade();
        if (error != null) {
            showBankTradeMessage(error, false);
            bringBankTradeControlsToFront();
            return;
        }
        if (!hasSufficientStockForBankTrade()) {
            appendLog("Bank trade failed because resources were insufficient.");
            showInformationPopup(
                    "Bank trade failed. Resource stock is no longer sufficient.",
                    InformationCloseAction.RETURN_TO_GAME
            );
            return;
        }

        dummyPlayerResources.put(selectedPlayerGiveResourceUi,
                dummyPlayerResources.getOrDefault(selectedPlayerGiveResourceUi, 0) - 4);
        dummyBankResources.put(selectedPlayerGiveResourceUi,
                dummyBankResources.getOrDefault(selectedPlayerGiveResourceUi, 0) + 4);
        dummyBankResources.put(selectedBankReceiveResourceUi,
                dummyBankResources.getOrDefault(selectedBankReceiveResourceUi, 0) - 1);
        dummyPlayerResources.put(selectedBankReceiveResourceUi,
                dummyPlayerResources.getOrDefault(selectedBankReceiveResourceUi, 0) + 1);

        applyBankTradeToMainStocks();
        updateBankTradeQuantityLabels();

        String successMessage = "Bank trade successful! You traded 4 "
                + displayResource(selectedPlayerGiveResourceUi)
                + " for 1 "
                + displayResource(selectedBankReceiveResourceUi) + ".";
        appendLog(successMessage);

        selectedBankReceiveResourceUi = null;
        selectedPlayerGiveResourceUi = null;
        updateBankTradeChecklist();
        showInformationPopup(successMessage, InformationCloseAction.RETURN_TO_GAME);
    }

    private boolean hasSufficientStockForBankTrade() {
        if (selectedPlayerGiveResourceUi == null || selectedBankReceiveResourceUi == null) {
            return false;
        }
        ResourceType give = toModelResource(selectedPlayerGiveResourceUi);
        ResourceType receive = toModelResource(selectedBankReceiveResourceUi);
        if (give == null || receive == null) {
            return false;
        }
        return getResourceAmount(activePlayerIndex, give) >= 4
                && getBankResourceAmount(receive) >= 1;
    }

    private Set<TradeHarborType> getAccessibleHarborsForPlayer(int playerIndex) {
        SaveData saveData = GameSession.getLoadedSaveData();
        if (saveData == null || saveData.harbors == null || saveData.players == null || playerIndex >= saveData.players.size()) {
            return new HashSet<>(Set.of(TradeHarborType.GENERAL, TradeHarborType.BANANA, TradeHarborType.WOOD));
        }

        SaveData.PlayerData playerData = saveData.players.get(playerIndex);
        if (playerData.buildings == null || playerData.buildings.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Integer> ownedIntersections = new HashSet<>();
        for (SaveData.BuildingData buildingData : playerData.buildings) {
            ownedIntersections.add(buildingData.intersectionId);
        }

        Set<TradeHarborType> accessible = new HashSet<>();
        for (SaveData.HarborData harborData : saveData.harbors) {
            if (ownedIntersections.contains(harborData.intersectionIdA)
                    || ownedIntersections.contains(harborData.intersectionIdB)) {
                TradeHarborType harborType = parseHarborType(harborData.harborType);
                if (harborType != null) {
                    accessible.add(harborType);
                }
            }
        }
        if (accessible.isEmpty()) {
            accessible.add(TradeHarborType.GENERAL);
            accessible.add(TradeHarborType.BANANA);
            accessible.add(TradeHarborType.WOOD);
        }
        return accessible;
    }

    private TradeHarborType parseHarborType(String rawHarborType) {
        if (rawHarborType == null) {
            return null;
        }
        String normalized = rawHarborType.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("THREE") || normalized.contains("GENERAL")) {
            return TradeHarborType.GENERAL;
        }
        if (normalized.contains("BANANA")) return TradeHarborType.BANANA;
        if (normalized.contains("WOOD")) return TradeHarborType.WOOD;
        if (normalized.contains("BRICK")) return TradeHarborType.BRICK;
        if (normalized.contains("WHEAT")) return TradeHarborType.WHEAT;
        if (normalized.contains("ORE")) return TradeHarborType.ORE;
        return null;
    }

    private ResourceType getSpecialHarborResource(TradeHarborType harborType) {
        return switch (harborType) {
            case BANANA -> ResourceType.BANANA;
            case WOOD -> ResourceType.WOOD;
            case BRICK -> ResourceType.BRICK;
            case WHEAT -> ResourceType.WHEAT;
            case ORE -> ResourceType.ORE;
            default -> null;
        };
    }

    private String getHarborButtonAssetName(TradeHarborType harborType) {
        return switch (harborType) {
            case GENERAL -> "harborGeneralButton.png";
            case BANANA -> "harborBananaButton.png";
            case WOOD -> "harborWoodButton.png";
            case BRICK -> "harborBrickButton.png";
            case WHEAT -> "harborWheatButton.png";
            case ORE -> "harborOreButton.png";
        };
    }

    private void showHarborTrade() {
        System.out.println("Harbor Trade opened");
        showTradeOverlayBase(TRADE_HARBOR_IMAGE_PATH, TradeMode.HARBOR_TRADE);
        hideTradeActions();
        setBankTradeActionButtonsVisible(false);
        clearHarborTradeSelection();

        availableHarbors = getAccessibleHarborsForPlayer(activePlayerIndex);
        if (availableHarbors == null) {
            availableHarbors = new HashSet<>();
        }

        clearHarborTradeMessage();
        renderHarborTradeUi();

        if (availableHarbors.isEmpty()) {
            showHarborTradeMessage("You do not have access to any harbor.", false);
        }
    }

    private void renderHarborTradeUi() {
        if (currentTradeMode != TradeMode.HARBOR_TRADE) return;

        tradeUiPane.getChildren().clear();
        tradeUiPane.setVisible(true);
        tradeUiPane.setManaged(true);

        double scaleX = currentPopupLogicalWidth / HARBOR_POPUP_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / HARBOR_POPUP_ASSET_HEIGHT;

        updateHarborButtons(scaleX, scaleY);
        updateHarborGiveCards(scaleX, scaleY);
        updateHarborReceiveCards(scaleX, scaleY);
        updateHarborQuantityLabels(scaleX, scaleY);
        updateHarborCheckmarks(scaleX, scaleY);
        renderHarborActionButtons(scaleX, scaleY);
        updateHarborMessagePlacement(scaleX, scaleY);

        tradeUiPane.toFront();
        tradeErrorLabel.toFront();
        popupCloseButton.toFront();
    }

    private void clearHarborTradeSelection() {
        selectedHarborType = null;
        selectedHarborGiveResource = null;
        selectedHarborReceiveResource = null;
    }

    private int getHarborRequiredAmount(TradeHarborType harborType) {
        return harborType == TradeHarborType.GENERAL ? 3 : 2;
    }

    private boolean isHarborGiveResourceEnabled(ResourceType resourceType) {
        if (selectedHarborType == null) return false;
        if (selectedHarborType == TradeHarborType.GENERAL) return true;
        ResourceType specialResource = getSpecialHarborResource(selectedHarborType);
        return specialResource == resourceType;
    }

    private boolean isHarborReceiveResourceEnabled(ResourceType resourceType) {
        if (selectedHarborType == null) return false;
        if (selectedHarborGiveResource != null && selectedHarborGiveResource == resourceType) return false;
        return getBankResourceAmount(resourceType) > 0;
    }

    private void updateHarborButtons(double scaleX, double scaleY) {
        TradeHarborType[] order = {
                TradeHarborType.GENERAL,
                TradeHarborType.BANANA,
                TradeHarborType.WOOD,
                TradeHarborType.WHEAT,
                TradeHarborType.ORE,
                TradeHarborType.BRICK
        };

        for (int i = 0; i < order.length; i++) {
            TradeHarborType harborType = order[i];
            boolean available = availableHarbors.contains(harborType);
            boolean selected = selectedHarborType == harborType;

            double x = (HARBOR_BUTTON_START_X + i * (HARBOR_BUTTON_W + HARBOR_BUTTON_GAP)) * scaleX;
            double y = HARBOR_BUTTON_Y * scaleY;
            double w = HARBOR_BUTTON_W * scaleX;
            double h = HARBOR_BUTTON_H * scaleY;

            ImageView buttonImage = null;
            try {
                String assetPath = String.format(HARBOR_BUTTON_BASE_PATH, getHarborButtonAssetName(harborType));
                buttonImage = new ImageView(new Image(getClass().getResource(assetPath).toExternalForm()));
            } catch (Exception ignored) {}
            if (buttonImage != null) {
                buttonImage.setFitWidth(w);
                buttonImage.setFitHeight(h);
                buttonImage.setLayoutX(x);
                buttonImage.setLayoutY(y);
                buttonImage.setPreserveRatio(false);
                buttonImage.setMouseTransparent(true);
                if (!available) {
                    buttonImage.getStyleClass().add("trade-disabled");
                }
                if (selected) {
                    buttonImage.getStyleClass().add("trade-selected");
                }
                tradeUiPane.getChildren().add(buttonImage);
            }

            Button button = new Button();
            button.getStyleClass().add("trade-click-zone");
            button.setOpacity(0.0);
            button.setLayoutX(x);
            button.setLayoutY(y);
            button.setPrefWidth(w);
            button.setPrefHeight(h);
            button.setDisable(!available);
            button.setMouseTransparent(!available);
            final TradeHarborType selectedType = harborType;
            button.setOnAction(e -> selectHarbor(selectedType));
            tradeUiPane.getChildren().add(button);
        }
    }

    private void updateHarborGiveCards(double scaleX, double scaleY) {
        double cardW = HARBOR_CARD_WIDTH * scaleX;
        double cardH = HARBOR_CARD_HEIGHT * scaleY;

        for (int i = 0; i < TRADE_RESOURCE_ORDER.size(); i++) {
            ResourceType resourceType = TRADE_RESOURCE_ORDER.get(i);
            double centerX = (HARBOR_RESOURCE_CENTER_X_BASE + HARBOR_RESOURCE_CENTER_X_STEPS[i]) * scaleX;
            double cardX = centerX - cardW / 2.0;
            double cardY = HARBOR_GIVE_CARD_Y * scaleY;
            boolean enabled = isHarborGiveResourceEnabled(resourceType);
            boolean selected = selectedHarborGiveResource == resourceType;

            ImageView card = createResourceIcon(resourceType);
            card.setFitWidth(cardW);
            card.setFitHeight(cardH);
            card.setLayoutX(cardX);
            card.setLayoutY(cardY);
            card.setMouseTransparent(true);
            card.getStyleClass().add("trade-resource-card");
            if (!enabled) {
                card.getStyleClass().add("trade-disabled");
            }
            if (selected) {
                card.getStyleClass().add("trade-selected");
            }
            tradeUiPane.getChildren().add(card);

            Button clickZone = new Button();
            clickZone.getStyleClass().add("trade-click-zone");
            clickZone.setOpacity(0.0);
            clickZone.setLayoutX(cardX);
            clickZone.setLayoutY(cardY);
            clickZone.setPrefWidth(cardW);
            clickZone.setPrefHeight(cardH);
            clickZone.setDisable(!enabled);
            clickZone.setMouseTransparent(!enabled);
            clickZone.setOnAction(e -> selectHarborGiveResource(resourceType));
            tradeUiPane.getChildren().add(clickZone);
        }
    }

    private void updateHarborReceiveCards(double scaleX, double scaleY) {
        double cardW = HARBOR_CARD_WIDTH * scaleX;
        double cardH = HARBOR_CARD_HEIGHT * scaleY;

        for (int i = 0; i < TRADE_RESOURCE_ORDER.size(); i++) {
            ResourceType resourceType = TRADE_RESOURCE_ORDER.get(i);
            double centerX = (HARBOR_RESOURCE_CENTER_X_BASE + HARBOR_RESOURCE_CENTER_X_STEPS[i]) * scaleX;
            double cardX = centerX - cardW / 2.0;
            double cardY = HARBOR_RECEIVE_CARD_Y * scaleY;
            boolean enabled = isHarborReceiveResourceEnabled(resourceType);
            boolean selected = selectedHarborReceiveResource == resourceType;

            ImageView card = createResourceIcon(resourceType);
            card.setFitWidth(cardW);
            card.setFitHeight(cardH);
            card.setLayoutX(cardX);
            card.setLayoutY(cardY);
            card.setMouseTransparent(true);
            card.getStyleClass().add("trade-resource-card");
            if (!enabled) {
                card.getStyleClass().add("trade-disabled");
            }
            if (selected) {
                card.getStyleClass().add("trade-selected");
            }
            tradeUiPane.getChildren().add(card);

            Button clickZone = new Button();
            clickZone.getStyleClass().add("trade-click-zone");
            clickZone.setOpacity(0.0);
            clickZone.setLayoutX(cardX);
            clickZone.setLayoutY(cardY);
            clickZone.setPrefWidth(cardW);
            clickZone.setPrefHeight(cardH);
            clickZone.setDisable(!enabled);
            clickZone.setMouseTransparent(!enabled);
            clickZone.setOnAction(e -> selectHarborReceiveResource(resourceType));
            tradeUiPane.getChildren().add(clickZone);
        }
    }

    private void updateHarborQuantityLabels(double scaleX, double scaleY) {
        double qtyW = 34.0 * ((scaleX + scaleY) / 2.0);
        double qtyH = 20.0 * ((scaleX + scaleY) / 2.0);
        double giveQtyY = (HARBOR_GIVE_CARD_Y + HARBOR_CARD_QTY_OFFSET_Y) * scaleY;
        double receiveQtyY = (HARBOR_RECEIVE_CARD_Y + HARBOR_CARD_QTY_OFFSET_Y) * scaleY;

        for (int i = 0; i < TRADE_RESOURCE_ORDER.size(); i++) {
            ResourceType resourceType = TRADE_RESOURCE_ORDER.get(i);
            double centerX = (HARBOR_RESOURCE_CENTER_X_BASE + HARBOR_RESOURCE_CENTER_X_STEPS[i]) * scaleX;

            Label giveQty = new Label(String.valueOf(getResourceAmount(activePlayerIndex, resourceType)));
            giveQty.getStyleClass().add("trade-quantity-label");
            giveQty.setAlignment(Pos.CENTER);
            giveQty.setPrefWidth(qtyW);
            giveQty.setPrefHeight(qtyH);
            giveQty.setLayoutX(centerX - qtyW / 2.0);
            giveQty.setLayoutY(giveQtyY);
            giveQty.setMouseTransparent(true);
            tradeUiPane.getChildren().add(giveQty);

            Label receiveQty = new Label(String.valueOf(getBankResourceAmount(resourceType)));
            receiveQty.getStyleClass().add("trade-quantity-label");
            receiveQty.setAlignment(Pos.CENTER);
            receiveQty.setPrefWidth(qtyW);
            receiveQty.setPrefHeight(qtyH);
            receiveQty.setLayoutX(centerX - qtyW / 2.0);
            receiveQty.setLayoutY(receiveQtyY);
            receiveQty.setMouseTransparent(true);
            tradeUiPane.getChildren().add(receiveQty);
        }
    }

    private void updateHarborCheckmarks(double scaleX, double scaleY) {
        for (int i = 0; i < TRADE_RESOURCE_ORDER.size(); i++) {
            ResourceType resourceType = TRADE_RESOURCE_ORDER.get(i);
            double centerX = (HARBOR_RESOURCE_CENTER_X_BASE + HARBOR_RESOURCE_CENTER_X_STEPS[i]) * scaleX;

            Label giveCheck = new Label(selectedHarborGiveResource == resourceType ? "◉" : "○");
            giveCheck.getStyleClass().add("trade-checkmark");
            giveCheck.setPrefWidth(HARBOR_CHECK_W * scaleX);
            giveCheck.setPrefHeight(HARBOR_CHECK_H * scaleY);
            giveCheck.setAlignment(Pos.CENTER);
            giveCheck.setLayoutX(centerX - (HARBOR_CHECK_W * scaleX) / 2.0);
            giveCheck.setLayoutY(HARBOR_CHECK_Y_GIVE * scaleY);
            giveCheck.setMouseTransparent(true);
            tradeUiPane.getChildren().add(giveCheck);

            Label receiveCheck = new Label(selectedHarborReceiveResource == resourceType ? "◉" : "○");
            receiveCheck.getStyleClass().add("trade-checkmark");
            receiveCheck.setPrefWidth(HARBOR_CHECK_W * scaleX);
            receiveCheck.setPrefHeight(HARBOR_CHECK_H * scaleY);
            receiveCheck.setAlignment(Pos.CENTER);
            receiveCheck.setLayoutX(centerX - (HARBOR_CHECK_W * scaleX) / 2.0);
            receiveCheck.setLayoutY(HARBOR_CHECK_Y_RECEIVE * scaleY);
            receiveCheck.setMouseTransparent(true);
            tradeUiPane.getChildren().add(receiveCheck);
        }
    }

    private void renderHarborActionButtons(double scaleX, double scaleY) {
        Button cancelButton = new Button();
        cancelButton.getStyleClass().add("trade-click-zone");
        cancelButton.setOpacity(0.0);
        cancelButton.setLayoutX(HARBOR_CANCEL_BUTTON_X * scaleX);
        cancelButton.setLayoutY(HARBOR_CANCEL_BUTTON_Y * scaleY);
        cancelButton.setPrefWidth(HARBOR_CANCEL_BUTTON_W * scaleX);
        cancelButton.setPrefHeight(HARBOR_CANCEL_BUTTON_H * scaleY);
        cancelButton.setOnAction(e -> onHarborTradeCancelClicked());

        Button confirmButton = new Button();
        confirmButton.getStyleClass().add("trade-click-zone");
        confirmButton.setOpacity(0.0);
        confirmButton.setLayoutX(HARBOR_CONFIRM_BUTTON_X * scaleX);
        confirmButton.setLayoutY(HARBOR_CONFIRM_BUTTON_Y * scaleY);
        confirmButton.setPrefWidth(HARBOR_CONFIRM_BUTTON_W * scaleX);
        confirmButton.setPrefHeight(HARBOR_CONFIRM_BUTTON_H * scaleY);
        confirmButton.setOnAction(e -> onHarborTradeConfirmClicked());

        tradeUiPane.getChildren().addAll(cancelButton, confirmButton);
    }

    private void updateHarborMessagePlacement(double scaleX, double scaleY) {
        double messageWidth = Math.min(HARBOR_MESSAGE_W * scaleX, currentPopupLogicalWidth - (80 * scaleX));
        double messageX = (currentPopupLogicalWidth - messageWidth) / 2.0;
        double buttonTopY = HARBOR_CANCEL_BUTTON_Y * scaleY;
        double messageY = buttonTopY - (HARBOR_MESSAGE_GAP_ABOVE_BUTTONS * scaleY);

        tradeErrorLabel.setLayoutX(messageX);
        tradeErrorLabel.setLayoutY(messageY);
        tradeErrorLabel.setPrefWidth(messageWidth);
        tradeErrorLabel.setPrefHeight(HARBOR_MESSAGE_H * scaleY);
    }

    private void selectHarbor(TradeHarborType harborType) {
        if (!availableHarbors.contains(harborType)) {
            showHarborTradeMessage("You do not have access to this harbor.", false);
            return;
        }
        selectedHarborType = harborType;
        selectedHarborGiveResource = null;
        selectedHarborReceiveResource = null;
        clearHarborTradeMessage();
        System.out.println("Harbor selected: " + harborType.name());
        renderHarborTradeUi();
    }

    private void selectHarborGiveResource(ResourceType resourceType) {
        if (!isHarborGiveResourceEnabled(resourceType)) {
            return;
        }
        selectedHarborGiveResource = selectedHarborGiveResource == resourceType ? null : resourceType;
        if (selectedHarborGiveResource != null && selectedHarborReceiveResource == selectedHarborGiveResource) {
            selectedHarborReceiveResource = null;
        }
        clearHarborTradeMessage();
        System.out.println("Harbor give selected: " + displayResource(resourceType).toUpperCase(Locale.ROOT));
        renderHarborTradeUi();
    }

    private void selectHarborReceiveResource(ResourceType resourceType) {
        if (!isHarborReceiveResourceEnabled(resourceType)) {
            return;
        }
        selectedHarborReceiveResource = selectedHarborReceiveResource == resourceType ? null : resourceType;
        clearHarborTradeMessage();
        System.out.println("Harbor receive selected: " + displayResource(resourceType).toUpperCase(Locale.ROOT));
        renderHarborTradeUi();
    }

    private String validateHarborTrade() {
        if (selectedHarborType == null) {
            return "Please select a harbor.";
        }
        if (!availableHarbors.contains(selectedHarborType)) {
            return "You do not have access to this harbor.";
        }
        if (selectedHarborGiveResource == null) {
            return "Please select a resource to give.";
        }
        if (selectedHarborReceiveResource == null) {
            return "Please select a resource to receive.";
        }
        if (selectedHarborGiveResource == selectedHarborReceiveResource) {
            return "You cannot trade the same resource.";
        }

        ResourceType specialResource = getSpecialHarborResource(selectedHarborType);
        if (specialResource != null && selectedHarborGiveResource != specialResource) {
            return "This harbor only accepts " + displayResource(specialResource) + ".";
        }

        int requiredAmount = getHarborRequiredAmount(selectedHarborType);
        int playerHave = getResourceAmount(activePlayerIndex, selectedHarborGiveResource);
        if (playerHave < requiredAmount) {
            String resourceName = displayResource(selectedHarborGiveResource);
            return "Not enough " + resourceName + ". You need " + requiredAmount + " " + resourceName + ".";
        }

        if (getBankResourceAmount(selectedHarborReceiveResource) < 1) {
            return "Bank does not have enough " + displayResource(selectedHarborReceiveResource) + ".";
        }
        return null;
    }

    private void showHarborTradeMessage(String message, boolean success) {
        if (message == null || message.isBlank()) {
            clearHarborTradeMessage();
            return;
        }
        tradeErrorLabel.setText(message);
        tradeErrorLabel.setStyle(success
                ? "-fx-text-fill: #1a8917; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center;"
                : "-fx-text-fill: #b7261e; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center;");
        tradeErrorLabel.setVisible(true);
        tradeErrorLabel.setManaged(true);
        tradeErrorLabel.toFront();
    }

    private void clearHarborTradeMessage() {
        tradeErrorLabel.setText("");
        tradeErrorLabel.setVisible(false);
        tradeErrorLabel.setManaged(false);
    }

    private void onHarborTradeConfirmClicked() {
        System.out.println("Harbor confirm clicked");
        String error = validateHarborTrade();
        if (error != null) {
            showHarborTradeMessage(error, false);
            return;
        }

        int requiredAmount = getHarborRequiredAmount(selectedHarborType);
        ResourceType give = selectedHarborGiveResource;
        ResourceType receive = selectedHarborReceiveResource;

        addResourceAmount(activePlayerIndex, give, -requiredAmount);
        addBankResourceAmount(give, requiredAmount);
        addBankResourceAmount(receive, -1);
        addResourceAmount(activePlayerIndex, receive, 1);
        syncTradeStateBackToSaveData();

        String successMessage = "Harbor trade successful! You traded "
                + requiredAmount + " " + displayResource(give)
                + " for 1 " + displayResource(receive) + ".";
        appendLog(successMessage);
        System.out.println("Harbor trade successful");

        clearHarborTradeSelection();
        clearHarborTradeMessage();
        showInformationPopup(successMessage, InformationCloseAction.RETURN_TO_GAME);
    }

    private void onHarborTradeCancelClicked() {
        System.out.println("Harbor cancel clicked");
        clearHarborTradeSelection();
        clearHarborTradeMessage();
        showTradeChoice();
    }

    private void executeOffer(TradeOfferState offer) {
        for (ResourceType resourceType : TRADE_RESOURCE_ORDER) {
            int giveValue = offer.give.getOrDefault(resourceType, 0);
            int receiveValue = offer.receive.getOrDefault(resourceType, 0);

            if (giveValue > 0) {
                addResourceAmount(offer.proposerIndex, resourceType, -giveValue);
                addResourceAmount(offer.receiverIndex, resourceType, giveValue);
            }
            if (receiveValue > 0) {
                addResourceAmount(offer.receiverIndex, resourceType, -receiveValue);
                addResourceAmount(offer.proposerIndex, resourceType, receiveValue);
            }
        }
        syncTradeStateBackToSaveData();
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

    private boolean canRollDiceNow() {
        if (diceRolled && (diceTimeline == null || diceTimeline.getStatus() == Animation.Status.STOPPED)) {
            return false;
        }
        if (diceTimeline != null && diceTimeline.getStatus() == Animation.Status.RUNNING) {
            return false;
        }
        return true;
    }

    private void rollDiceRandom() {
        if (diceRollMode != DiceRollMode.NONE) {
            showDiceRollMethodBlockedInfo(false);
            return;
        }
        if (!canRollDiceNow()) return;
        diceRollMode = DiceRollMode.AUTOMATIC;
        System.out.println("Dice button left clicked: random roll");
        startDiceAnimation();
    }

    private void rollDiceWithValues(int dice1, int dice2) {
        if (diceRollMode != DiceRollMode.NONE) {
            showDiceRollMethodBlockedInfo(true);
            return;
        }
        if (!canRollDiceNow()) return;
        int d1 = Math.max(1, Math.min(6, dice1));
        int d2 = Math.max(1, Math.min(6, dice2));
        setupDiceViews();
        diceRolled = true;
        diceRollMode = DiceRollMode.MANUAL;
        applyDiceRoll(d1, d2, true);
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
        applyDiceRoll(f1, f2, false);
    }
    private void applyDiceRoll(int f1, int f2, boolean manual) {
        if (diceView1 != null) diceView1.setImage(loadDiceImage(f1));
        if (diceView2 != null) diceView2.setImage(loadDiceImage(f2));
        if (manual) {
            System.out.println("Manual dice rolled: " + f1 + " + " + f2 + " = " + (f1 + f2));
            appendLog("Manual dice rolled: " + f1 + " + " + f2 + " = " + (f1 + f2) + ".");
        } else {
            appendLog("Dice rolled: " + f1 + " + " + f2 + " = " + (f1 + f2) + ".");
        }
        // TODO: notify bridge layer -> GameEngine.rollDice(f1, f2)
    }

    private void showDiceRollMethodBlockedInfo(boolean manualRequested) {
        String message;
        if (manualRequested) {
            message = diceRollMode == DiceRollMode.AUTOMATIC
                    ? "You already rolled automatically this turn. Manual roll is no longer available."
                    : "You already rolled manually this turn.";
        } else {
            message = diceRollMode == DiceRollMode.MANUAL
                    ? "You already rolled manually this turn. Automatic roll is no longer available."
                    : "You already rolled automatically this turn.";
        }
        showInformationPopup(message, InformationCloseAction.RETURN_TO_GAME);
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
        diceRollMode = DiceRollMode.NONE;
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
        if (tradeOverlayRoot != null) {
            tradeOverlayRoot.setVisible(false);
            tradeOverlayRoot.setManaged(false);
        }
        popupOverlay.setVisible(false);
        popupOverlay.setManaged(false);
        tradePopupContainer.setVisible(false);
        tradePopupContainer.setManaged(false);
        tradePopupContainer.setClip(new Rectangle(currentPopupLogicalWidth, currentPopupLogicalHeight));
        popupImageView.setVisible(false);
        popupImageView.setManaged(false);
        popupCloseButton.setVisible(false);
        popupCloseButton.setManaged(false);
        setTradeChoiceButtonsVisible(false);
        setBankTradeControlsVisible(false);
        setTradeOverlayContentVisible(false);
        clearPlayerTradeMessage();
        if (tradeModalScaleGroup != null) {
            tradeModalScaleGroup.setScaleX(1.0);
            tradeModalScaleGroup.setScaleY(1.0);
        }
    }

    private void positionPopupCloseButton() {
        double closeCenterX = currentPopupLogicalWidth * (POPUP_CLOSE_CENTER_X / POPUP_ASSET_WIDTH);
        double closeCenterY = currentPopupLogicalHeight * (POPUP_CLOSE_CENTER_Y / POPUP_ASSET_HEIGHT);

        popupCloseButton.setPrefWidth(POPUP_CLOSE_HITBOX_SIZE);
        popupCloseButton.setPrefHeight(POPUP_CLOSE_HITBOX_SIZE);
        popupCloseButton.setLayoutX(closeCenterX - POPUP_CLOSE_HITBOX_SIZE / 2.0);
        popupCloseButton.setLayoutY(closeCenterY - POPUP_CLOSE_HITBOX_SIZE / 2.0);
    }

    private void positionTradeChoiceButtons() {
        double scaleX = currentPopupLogicalWidth / POPUP_ASSET_WIDTH;
        double scaleY = currentPopupLogicalHeight / POPUP_ASSET_HEIGHT;

        placeTradeChoiceButton(tradeChoiceBankButton, scaleX, scaleY,
                TRADE_CHOICE_BANK_X, TRADE_CHOICE_BANK_Y, TRADE_CHOICE_BANK_W, TRADE_CHOICE_BANK_H);
        placeTradeChoiceButton(tradeChoicePlayerButton, scaleX, scaleY,
                TRADE_CHOICE_PLAYER_X, TRADE_CHOICE_PLAYER_Y, TRADE_CHOICE_PLAYER_W, TRADE_CHOICE_PLAYER_H);
        placeTradeChoiceButton(tradeChoiceHarborButton, scaleX, scaleY,
                TRADE_CHOICE_HARBOR_X, TRADE_CHOICE_HARBOR_Y, TRADE_CHOICE_HARBOR_W, TRADE_CHOICE_HARBOR_H);
    }

    private void placeTradeChoiceButton(Button button, double scaleX, double scaleY,
                                        double assetX, double assetY, double assetW, double assetH) {
        button.setLayoutX(assetX * scaleX);
        button.setLayoutY(assetY * scaleY);
        button.setPrefWidth(assetW * scaleX);
        button.setPrefHeight(assetH * scaleY);
    }

    private double[] getPopupImageDrawnBounds() {
        Image image = popupImageView.getImage();
        if (image == null) return null;

        double fitWidth = popupImageView.getFitWidth();
        double fitHeight = popupImageView.getFitHeight();
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        if (imageWidth <= 0 || imageHeight <= 0) return null;

        double drawnWidth;
        double drawnHeight;
        if (popupImageView.isPreserveRatio()) {
            double scale = Math.min(fitWidth / imageWidth, fitHeight / imageHeight);
            drawnWidth = imageWidth * scale;
            drawnHeight = imageHeight * scale;
        } else {
            drawnWidth = fitWidth;
            drawnHeight = fitHeight;
        }

        double drawnX = popupImageView.getLayoutX() + (fitWidth - drawnWidth) / 2.0;
        double drawnY = popupImageView.getLayoutY() + (fitHeight - drawnHeight) / 2.0;
        return new double[] { drawnX, drawnY, drawnWidth, drawnHeight };
    }

    private void positionBankTradeControls() {
        double[] drawnBounds = getPopupImageDrawnBounds();
        if (drawnBounds == null) return;

        double drawnX = drawnBounds[0];
        double drawnY = drawnBounds[1];
        double drawnWidth = drawnBounds[2];
        double drawnHeight = drawnBounds[3];
        double scaleX = drawnWidth / BANK_POPUP_ASSET_WIDTH;
        double scaleY = drawnHeight / BANK_POPUP_ASSET_HEIGHT;
        double avgScale = (scaleX + scaleY) / 2.0;
        double xOffset = BANK_TRADE_X_OFFSET * scaleX;

        Label[] bankQtyLabels = {
                bankBananaQtyLabel, bankWoodQtyLabel, bankWheatQtyLabel, bankOreQtyLabel, bankBrickQtyLabel
        };
        Label[] youQtyLabels = {
                youBananaQtyLabel, youWoodQtyLabel, youWheatQtyLabel, youOreQtyLabel, youBrickQtyLabel
        };
        Label[] bankChecks = {
                bankBananaCheck, bankWoodCheck, bankWheatCheck, bankOreCheck, bankBrickCheck
        };
        Label[] youChecks = {
                youBananaCheck, youWoodCheck, youWheatCheck, youOreCheck, youBrickCheck
        };
        Button[] bankSelectButtons = {
                bankBananaSelectButton, bankWoodSelectButton, bankWheatSelectButton, bankOreSelectButton, bankBrickSelectButton
        };
        Button[] youSelectButtons = {
                youBananaSelectButton, youWoodSelectButton, youWheatSelectButton, youOreSelectButton, youBrickSelectButton
        };

        double qtyWidth = Math.max(20.0, 34.0 * avgScale);
        double qtyHeight = Math.max(14.0, 20.0 * avgScale);
        double cardWidth = BANK_CARD_WIDTH * scaleX;
        double cardHeight = BANK_CARD_HEIGHT * scaleY;

        for (int i = 0; i < BANK_RESOURCE_CENTER_X_STEPS.length; i++) {
            double centerAssetX = BANK_RESOURCE_CENTER_X_BASE + BANK_RESOURCE_CENTER_X_STEPS[i];
            double topCenterX = drawnX + centerAssetX * scaleX + xOffset;
            double bottomCenterX = drawnX + centerAssetX * scaleX + xOffset;
            double topCardX = topCenterX - cardWidth / 2.0;
            double bottomCardX = bottomCenterX - cardWidth / 2.0;
            double topCardY = drawnY + BANK_TOP_CARD_Y * scaleY;
            double bottomCardY = drawnY + BANK_BOTTOM_CARD_Y * scaleY;

            Label topQty = bankQtyLabels[i];
            topQty.setPrefWidth(qtyWidth);
            topQty.setPrefHeight(qtyHeight);
            topQty.setLayoutX(topCenterX - qtyWidth / 2.0);
            topQty.setLayoutY(topCardY + BANK_CARD_QTY_OFFSET_Y * scaleY);

            Label bottomQty = youQtyLabels[i];
            bottomQty.setPrefWidth(qtyWidth);
            bottomQty.setPrefHeight(qtyHeight);
            bottomQty.setLayoutX(bottomCenterX - qtyWidth / 2.0);
            bottomQty.setLayoutY(bottomCardY + BANK_CARD_QTY_OFFSET_Y * scaleY);

            Label topCheck = bankChecks[i];
            topCheck.setPrefWidth(cardWidth);
            topCheck.setPrefHeight(cardHeight);
            topCheck.setLayoutX(topCardX);
            topCheck.setLayoutY(topCardY);

            Label bottomCheck = youChecks[i];
            bottomCheck.setPrefWidth(cardWidth);
            bottomCheck.setPrefHeight(cardHeight);
            bottomCheck.setLayoutX(bottomCardX);
            bottomCheck.setLayoutY(bottomCardY);

            Button topSelectButton = bankSelectButtons[i];
            topSelectButton.setPrefWidth(cardWidth);
            topSelectButton.setPrefHeight(cardHeight);
            topSelectButton.setLayoutX(topCardX);
            topSelectButton.setLayoutY(topCardY);

            Button bottomSelectButton = youSelectButtons[i];
            bottomSelectButton.setPrefWidth(cardWidth);
            bottomSelectButton.setPrefHeight(cardHeight);
            bottomSelectButton.setLayoutX(bottomCardX);
            bottomSelectButton.setLayoutY(bottomCardY);
        }

        bankTradeCancelButton.setLayoutX(drawnX + BANK_CANCEL_BUTTON_X * scaleX);
        bankTradeCancelButton.setLayoutY(drawnY + BANK_CANCEL_BUTTON_Y * scaleY);
        bankTradeCancelButton.setPrefWidth(BANK_CANCEL_BUTTON_W * scaleX);
        bankTradeCancelButton.setPrefHeight(BANK_CANCEL_BUTTON_H * scaleY);

        bankTradeConfirmButton.setLayoutX(drawnX + BANK_CONFIRM_BUTTON_X * scaleX);
        bankTradeConfirmButton.setLayoutY(drawnY + BANK_CONFIRM_BUTTON_Y * scaleY);
        bankTradeConfirmButton.setPrefWidth(BANK_CONFIRM_BUTTON_W * scaleX);
        bankTradeConfirmButton.setPrefHeight(BANK_CONFIRM_BUTTON_H * scaleY);

        bankTradeMessageLabel.setLayoutX(drawnX + BANK_MESSAGE_X * scaleX);
        bankTradeMessageLabel.setLayoutY(drawnY + BANK_MESSAGE_Y * scaleY);
        bankTradeMessageLabel.setPrefWidth(BANK_MESSAGE_W * scaleX);
        bankTradeMessageLabel.setPrefHeight(BANK_MESSAGE_H * scaleY);
    }

    private void setTradeChoiceButtonsVisible(boolean visible) {
        tradeChoiceBankButton.setVisible(visible);
        tradeChoiceBankButton.setManaged(visible);
        tradeChoicePlayerButton.setVisible(visible);
        tradeChoicePlayerButton.setManaged(visible);
        tradeChoiceHarborButton.setVisible(visible);
        tradeChoiceHarborButton.setManaged(visible);
    }

    private void showPopupImage(String imagePath) {
        Image popupImage = null;
        try {
            popupImage = new Image(getClass().getResource(imagePath).toExternalForm());
            popupImageView.setImage(popupImage);
        } catch (Exception ignored) {}
        if (popupImage != null && popupImage.getWidth() > 0 && popupImage.getHeight() > 0) {
            configureTradeModalSize(popupImage.getWidth(), popupImage.getHeight());
        } else {
            configureTradeModalSize(POPUP_ASSET_WIDTH, POPUP_ASSET_HEIGHT);
        }

        centerTradePopupContainer();
        normalizeTradePopupScale();
        if (tradeOverlayRoot != null) {
            tradeOverlayRoot.setVisible(true);
            tradeOverlayRoot.setManaged(true);
            tradeOverlayRoot.toFront();
        }
        popupOverlay.setVisible(true);
        popupOverlay.setManaged(true);
        tradePopupContainer.setVisible(true);
        tradePopupContainer.setManaged(true);
        popupImageView.setVisible(true);
        popupImageView.setManaged(true);
        popupImageView.setMouseTransparent(true);
        popupCloseButton.setVisible(true);
        popupCloseButton.setManaged(true);
        popupCloseButton.setDisable(false);
        popupCloseButton.setMouseTransparent(false);
        positionPopupCloseButton();
        boolean isTradeChoice = TRADE_CHOICE_IMAGE_PATH.equals(imagePath);
        setTradeChoiceButtonsVisible(isTradeChoice);
        if (isTradeChoice) {
            positionTradeChoiceButtons();
        }

        popupOverlay.toFront();
        if (tradeOverlayCenterLayer != null) {
            tradeOverlayCenterLayer.toFront();
        }
        tradePopupContainer.toFront();
        popupImageView.toFront();
        if (isTradeChoice) {
            tradeChoiceBankButton.toFront();
            tradeChoicePlayerButton.toFront();
            tradeChoiceHarborButton.toFront();
        }
        if (tradeUiPane.isVisible()) {
            tradeUiPane.toFront();
            tradeActionButtonsBox.toFront();
            tradeErrorLabel.toFront();
        }
        popupCloseButton.toFront();
    }

    private void centerTradePopupContainer() {
        tradePopupContainer.setTranslateX(0.0);
        tradePopupContainer.setTranslateY(0.0);
    }

    private void configureTradeModalSize(double logicalWidth, double logicalHeight) {
        currentPopupLogicalWidth = logicalWidth > 0 ? logicalWidth : POPUP_ASSET_WIDTH;
        currentPopupLogicalHeight = logicalHeight > 0 ? logicalHeight : POPUP_ASSET_HEIGHT;

        tradePopupContainer.setPrefWidth(currentPopupLogicalWidth);
        tradePopupContainer.setPrefHeight(currentPopupLogicalHeight);
        tradePopupContainer.setMinWidth(currentPopupLogicalWidth);
        tradePopupContainer.setMinHeight(currentPopupLogicalHeight);
        tradePopupContainer.setMaxWidth(currentPopupLogicalWidth);
        tradePopupContainer.setMaxHeight(currentPopupLogicalHeight);

        popupImageView.setLayoutX(0);
        popupImageView.setLayoutY(0);
        popupImageView.setFitWidth(currentPopupLogicalWidth);
        popupImageView.setFitHeight(currentPopupLogicalHeight);
        popupImageView.setPreserveRatio(false);

        tradeUiPane.setLayoutX(0);
        tradeUiPane.setLayoutY(0);
        tradeUiPane.setPrefWidth(currentPopupLogicalWidth);
        tradeUiPane.setPrefHeight(currentPopupLogicalHeight);
        tradeUiPane.setMinWidth(currentPopupLogicalWidth);
        tradeUiPane.setMinHeight(currentPopupLogicalHeight);
        tradeUiPane.setMaxWidth(currentPopupLogicalWidth);
        tradeUiPane.setMaxHeight(currentPopupLogicalHeight);

        tradeUiPane.setClip(new Rectangle(currentPopupLogicalWidth, currentPopupLogicalHeight));
        tradePopupContainer.setClip(new Rectangle(currentPopupLogicalWidth, currentPopupLogicalHeight));
    }

    private void normalizeTradePopupScale() {
        if (tradeModalScaleGroup == null) return;

        double rootW = rootPane == null ? DESIGN_WIDTH : Math.max(rootPane.getWidth(), 1.0);
        double rootH = rootPane == null ? DESIGN_HEIGHT : Math.max(rootPane.getHeight(), 1.0);
        double logicalW = tradePopupContainer.getPrefWidth() > 0 ? tradePopupContainer.getPrefWidth() : POPUP_ASSET_WIDTH;
        double logicalH = tradePopupContainer.getPrefHeight() > 0 ? tradePopupContainer.getPrefHeight() : POPUP_ASSET_HEIGHT;

        double scale = Math.min((rootW * 0.80) / logicalW, (rootH * 0.82) / logicalH);
        scale = Math.min(scale, 1.0);

        if (scale <= 0 || Double.isNaN(scale) || Double.isInfinite(scale)) {
            tradeModalScaleGroup.setScaleX(1.0);
            tradeModalScaleGroup.setScaleY(1.0);
            return;
        }
        tradeModalScaleGroup.setScaleX(scale);
        tradeModalScaleGroup.setScaleY(scale);
        System.out.println("[TradeUI] modal size: " + logicalW + " x " + logicalH);
        System.out.println("[TradeUI] scale: " + scale);
    }

    // ── Button handlers ──────────────────────────────────────────────`r`n    @FXML
        @FXML
    private void onDiceButtonMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            event.consume();
            System.out.println("Dice button right clicked: manual roll popup");
            if (diceRollMode != DiceRollMode.NONE) {
                showDiceRollMethodBlockedInfo(true);
                return;
            }
            if (rollDiceManualOverlayController != null) {
                rollDiceManualOverlayController.showOverlay();
            }
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            rollDiceRandom();
        }
    }

    @FXML
    private void onRollDiceClicked() {
        rollDiceRandom();
    }

    @FXML
    private void onTradeClicked() {
        System.out.println("[TradeUI] Trade button clicked");
        currentProposerIndex = Math.max(0, Math.min(activePlayerIndex, Math.max(playerResourceStocks.size() - 1, 0)));
        currentReceiverIndex = playerResourceStocks.size() > 1
                ? (currentProposerIndex + 1) % playerResourceStocks.size()
                : currentProposerIndex;
        selectedTargetIndex = -1;
        resetTradeDraftQuantities();
        clearTradeError();
        showTradeChoice();
        appendLog("Trade menu opened.");
    }

    @FXML
    private void onTradeChoiceBankClicked() {
        System.out.println("[TradeUI] Trade choice -> BANK");
        selectedBankReceiveResourceUi = null;
        selectedPlayerGiveResourceUi = null;
        initializeBankTradeDummyData();
        selectedBankGiveResource = null;
        selectedBankReceiveResource = null;
        showBankTrade();
        appendLog("Bank trade selected.");
    }

    @FXML
    private void onTradeChoicePlayerClicked() {
        System.out.println("[TradeUI] Trade choice -> PLAYER");
        selectedTargetIndex = -1;
        resetTradeDraftQuantities();
        showPlayerTrade();
        appendLog("Player trade selected.");
    }

    @FXML
    private void onTradeChoiceHarborClicked() {
        System.out.println("[TradeUI] Trade choice -> HARBOR");
        selectedHarborType = null;
        selectedHarborGiveResource = null;
        selectedHarborReceiveResource = null;
        showHarborTrade();
        appendLog("Harbor trade selected.");
    }
    @FXML
    private void onBuyCardClicked()  {
        if (buyExperimentOverlayController == null) return;
        buyExperimentOverlayController.showOverlay();
    }

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
    @FXML
    private void onDeclareVictoryClicked() {
        System.out.println("Declare Victory clicked");
        if (declareVictoryOverlayController == null) {
            showInformationPopup("Declare Victory screen is not available.", InformationCloseAction.RETURN_TO_GAME);
            return;
        }
        declareVictoryOverlayController.showDeclareVictoryScreen();
    }
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
        System.out.println("[TradeUI] Close popup clicked");
        if (currentTradeMode == TradeMode.INFORMATION) {
            onInformationCloseClicked();
            return;
        }
        hideTradeOverlay();
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
