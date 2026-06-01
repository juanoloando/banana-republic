package com.bananarepublic.cards;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import model.resource.ResourceType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class UseCardOverlayManager {
    private static final double DESIGN_WIDTH = 1920.0;
    private static final double DESIGN_HEIGHT = 1080.0;
    private static final double POPUP_W = 887.0;
    private static final double POPUP_H = 1136.0;
    private static final double MAX_POPUP_WIDTH_RATIO = 0.65;
    private static final double MAX_POPUP_HEIGHT_RATIO = 0.75;

    private static final double CANCEL_X = 395.0;
    private static final double CANCEL_Y = 1012.0;
    private static final double CANCEL_W = 200.0;
    private static final double CANCEL_H = 53.0;

    private static final double CONFIRM_X = 624.0;
    private static final double CONFIRM_Y = 1012.0;
    private static final double CONFIRM_W = 200.0;
    private static final double CONFIRM_H = 53.0;

    private final Pane hostPane;
    private final CardDataProvider dataProvider;
    private final CardEffectService cardEffectService;
    private final CardTemplateRenderer cardTemplateRenderer;
    private final Runnable inventoryRefreshCallback;

    private Pane overlayRoot;
    private Pane popupPane;
    private ScrollPane cardsScrollPane;
    private FlowPane cardsFlowPane;
    private Label emptyCardsLabel;
    private Label errorLabel;
    private HBox monopolySelectionBox;

    private CardType selectedCardType;
    private ResourceType selectedMonopolyResource;
    private final Map<ResourceType, Button> monopolyButtons = new EnumMap<>(ResourceType.class);

    public UseCardOverlayManager(Pane hostPane,
                                 CardDataProvider dataProvider,
                                 CardEffectService cardEffectService,
                                 Runnable inventoryRefreshCallback) {
        this.hostPane = hostPane;
        this.dataProvider = dataProvider;
        this.cardEffectService = cardEffectService;
        this.inventoryRefreshCallback = inventoryRefreshCallback;
        this.cardTemplateRenderer = new CardTemplateRenderer();
    }

    public void showUseCardPopup() {
        ensureOverlayBuilt();
        selectedCardType = null;
        selectedMonopolyResource = null;
        updateMonopolySelectionState();
        errorLabel.setText("");
        renderAvailableCards();
        overlayRoot.setVisible(true);
        overlayRoot.toFront();
        normalizePopupScale();
    }

    public void hideUseCardPopup() {
        if (overlayRoot != null) {
            overlayRoot.setVisible(false);
        }
        selectedCardType = null;
        selectedMonopolyResource = null;
        updateMonopolySelectionState();
    }

    private void ensureOverlayBuilt() {
        if (overlayRoot != null) return;

        overlayRoot = new Pane();
        overlayRoot.setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        overlayRoot.setLayoutX(0);
        overlayRoot.setLayoutY(0);
        overlayRoot.setStyle("-fx-background-color: rgba(0,0,0,0.60);");
        overlayRoot.setVisible(false);
        overlayRoot.setPickOnBounds(true);
        overlayRoot.setUserData("use-card-overlay");

        popupPane = new Pane();
        popupPane.setPrefSize(POPUP_W, POPUP_H);
        popupPane.setLayoutX((DESIGN_WIDTH - POPUP_W) / 2.0);
        popupPane.setLayoutY((DESIGN_HEIGHT - POPUP_H) / 2.0);

        ImageView popupBg = new ImageView(new Image(
                getClass().getResource("/com/bananarepublic/images/usecard/Use Card.png").toExternalForm()));
        popupBg.setFitWidth(POPUP_W);
        popupBg.setFitHeight(POPUP_H);
        popupBg.setPreserveRatio(false);

        cardsFlowPane = new FlowPane();
        cardsFlowPane.setHgap(18);
        cardsFlowPane.setVgap(18);
        cardsFlowPane.setPadding(new Insets(8));
        cardsFlowPane.setPrefWrapLength(720);

        cardsScrollPane = new ScrollPane(cardsFlowPane);
        cardsScrollPane.setLayoutX(56);
        cardsScrollPane.setLayoutY(450);
        cardsScrollPane.setPrefSize(775, 440);
        cardsScrollPane.setFitToWidth(true);
        cardsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cardsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        emptyCardsLabel = new Label("No playable cards available.");
        emptyCardsLabel.setLayoutX(56);
        emptyCardsLabel.setLayoutY(452);
        emptyCardsLabel.setPrefWidth(775);
        emptyCardsLabel.setAlignment(Pos.TOP_LEFT);
        emptyCardsLabel.setStyle("-fx-text-fill: #3f2d17; -fx-font-size: 14px; -fx-font-weight: bold;");
        emptyCardsLabel.setVisible(false);
        emptyCardsLabel.setManaged(false);

        monopolySelectionBox = new HBox(8);
        monopolySelectionBox.setAlignment(Pos.CENTER);
        monopolySelectionBox.setLayoutX(95);
        monopolySelectionBox.setLayoutY(915);
        monopolySelectionBox.setPrefWidth(695);
        for (ResourceType type : ResourceType.values()) {
            Button button = new Button(type.name());
            button.setOnAction(evt -> {
                selectedMonopolyResource = type;
                updateMonopolySelectionState();
            });
            button.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3f2d17;");
            monopolyButtons.put(type, button);
            monopolySelectionBox.getChildren().add(button);
        }

        errorLabel = new Label();
        errorLabel.setLayoutX(85);
        errorLabel.setLayoutY(968);
        errorLabel.setPrefWidth(720);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setStyle("-fx-text-fill: #b7261e; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button cancelButton = new Button();
        cancelButton.setLayoutX(CANCEL_X);
        cancelButton.setLayoutY(CANCEL_Y);
        cancelButton.setPrefSize(CANCEL_W, CANCEL_H);
        cancelButton.setOpacity(0.0);
        cancelButton.setOnAction(evt -> hideUseCardPopup());

        Button confirmButton = new Button();
        confirmButton.setLayoutX(CONFIRM_X);
        confirmButton.setLayoutY(CONFIRM_Y);
        confirmButton.setPrefSize(CONFIRM_W, CONFIRM_H);
        confirmButton.setOpacity(0.0);
        confirmButton.setOnAction(evt -> onConfirmUseCard());

        popupPane.getChildren().addAll(
                popupBg,
                cardsScrollPane,
                emptyCardsLabel,
                monopolySelectionBox,
                errorLabel,
                cancelButton,
                confirmButton
        );
        overlayRoot.getChildren().add(popupPane);
        hostPane.getChildren().add(overlayRoot);
        hostPane.widthProperty().addListener((obs, oldVal, newVal) -> normalizePopupScale());
        hostPane.heightProperty().addListener((obs, oldVal, newVal) -> normalizePopupScale());
    }

    private void onConfirmUseCard() {
        if (selectedCardType == null) {
            errorLabel.setText("Please select a card first.");
            return;
        }
        CardUseResult result = cardEffectService.useCard(selectedCardType, selectedMonopolyResource);
        if (!result.isSuccess()) {
            errorLabel.setText(result.getMessage());
            return;
        }
        errorLabel.setText("");
        dataProvider.appendLog(result.getMessage());
        inventoryRefreshCallback.run();
        renderAvailableCards();
        if (result.isClosePopup()) {
            hideUseCardPopup();
        }
    }

    private void renderAvailableCards() {
        cardsFlowPane.getChildren().clear();
        int currentPlayerIndex = dataProvider.getCurrentPlayerIndex();
        PlayerCardInventory inventory = dataProvider.getCardInventory(currentPlayerIndex);
        if (inventory == null) {
            errorLabel.setText("Card inventory is not available.");
            return;
        }

        List<CardType> ownedCards = inventory.getOwnedExperimentCards();
        ownedCards.removeIf(type -> CardDefinitions.get(type).getPlayabilityType() != PlayabilityType.PLAYABLE);
        if (ownedCards.isEmpty()) {
            cardsScrollPane.setVisible(false);
            cardsScrollPane.setManaged(false);
            emptyCardsLabel.setVisible(true);
            emptyCardsLabel.setManaged(true);
            return;
        }
        cardsScrollPane.setVisible(true);
        cardsScrollPane.setManaged(true);
        emptyCardsLabel.setVisible(false);
        emptyCardsLabel.setManaged(false);

        for (CardType type : ownedCards) {
            CardDefinition definition = CardDefinitions.get(type);
            int quantity = inventory.getQuantity(type);
            StackPane cardNode = cardTemplateRenderer.buildCardNode(
                    definition, quantity, type == selectedCardType, false);
            cardNode.setOnMouseClicked(evt -> {
                selectedCardType = type;
                if (type != CardType.MONOPOLY) {
                    selectedMonopolyResource = null;
                }
                updateMonopolySelectionState();
                renderAvailableCards();
            });
            cardsFlowPane.getChildren().add(cardNode);
        }
    }

    private void normalizePopupScale() {
        if (popupPane == null || hostPane == null) return;
        double rootW = Math.max(hostPane.getWidth(), 1.0);
        double rootH = Math.max(hostPane.getHeight(), 1.0);

        double maxPopupWidth = rootW * MAX_POPUP_WIDTH_RATIO;
        double maxPopupHeight = rootH * MAX_POPUP_HEIGHT_RATIO;
        double scale = Math.min(maxPopupWidth / POPUP_W, maxPopupHeight / POPUP_H);
        scale = Math.min(scale, 1.0);

        if (scale <= 0 || Double.isNaN(scale) || Double.isInfinite(scale)) {
            scale = 1.0;
        }

        popupPane.setScaleX(scale);
        popupPane.setScaleY(scale);
    }

    private void updateMonopolySelectionState() {
        boolean monopolySelected = selectedCardType == CardType.MONOPOLY;
        monopolySelectionBox.setVisible(monopolySelected);
        monopolySelectionBox.setManaged(monopolySelected);

        for (Map.Entry<ResourceType, Button> entry : monopolyButtons.entrySet()) {
            boolean selected = monopolySelected && entry.getKey() == selectedMonopolyResource;
            entry.getValue().setStyle(selected
                    ? "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #7a4a23;"
                    : "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3f2d17;");
        }
    }
}
