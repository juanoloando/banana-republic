package com.bananarepublic.view;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.resource.ResourceType;

import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BuyExperimentCardOverlay {

    private static final double POPUP_WIDTH = 887.0;
    private static final double POPUP_HEIGHT = 1136.0;
    private static final double CANCEL_X = 396.0;
    private static final double CANCEL_Y = 1013.0;
    private static final double CANCEL_W = 199.0;
    private static final double CANCEL_H = 52.0;
    private static final double BUY_X = 624.0;
    private static final double BUY_Y = 1013.0;
    private static final double BUY_W = 199.0;
    private static final double BUY_H = 52.0;

    @FXML private StackPane overlayRoot;
    @FXML private Pane overlayBackdrop;
    @FXML private StackPane centerLayer;
    @FXML private Group modalScaleGroup;
    @FXML private Pane popupContainer;
    @FXML private ImageView popupImageView;
    @FXML private HBox requiredResourceRow;
    @FXML private HBox yourResourceRow;
    @FXML private Label buyExperimentMessageLabel;
    @FXML private Button buyExperimentCancelButton;
    @FXML private Button buyExperimentBuyButton;

    private Supplier<Boolean> phaseAllowedSupplier = () -> true;
    private Function<ResourceType, Integer> playerResourceSupplier = resourceType -> 0;
    private BooleanSupplier purchaseCostApplier = () -> false;
    private Consumer<String> informationCallback = msg -> {};
    private Consumer<String> logCallback = msg -> {};
    private final Random random = new Random();
    private int experimentDeckSize = 10; // TODO: replace with real Experiment deck size from engine.

    @FXML
    public void initialize() {
        if (popupImageView != null) {
            popupImageView.setMouseTransparent(true);
        }
        if (buyExperimentCancelButton != null) {
            buyExperimentCancelButton.setLayoutX(CANCEL_X);
            buyExperimentCancelButton.setLayoutY(CANCEL_Y);
            buyExperimentCancelButton.setPrefWidth(CANCEL_W);
            buyExperimentCancelButton.setPrefHeight(CANCEL_H);
        }
        if (buyExperimentBuyButton != null) {
            buyExperimentBuyButton.setLayoutX(BUY_X);
            buyExperimentBuyButton.setLayoutY(BUY_Y);
            buyExperimentBuyButton.setPrefWidth(BUY_W);
            buyExperimentBuyButton.setPrefHeight(BUY_H);
        }
        if (overlayRoot != null) {
            overlayRoot.widthProperty().addListener((obs, oldVal, newVal) -> normalizePopupScale());
            overlayRoot.heightProperty().addListener((obs, oldVal, newVal) -> normalizePopupScale());
        }
        clearMessage();
        renderRequiredResources();
        renderYourResources();
        normalizePopupScale();
    }

    public void configure(Supplier<Boolean> phaseAllowedSupplier,
                          Function<ResourceType, Integer> playerResourceSupplier,
                          BooleanSupplier purchaseCostApplier,
                          Consumer<String> informationCallback,
                          Consumer<String> logCallback) {
        if (phaseAllowedSupplier != null) this.phaseAllowedSupplier = phaseAllowedSupplier;
        if (playerResourceSupplier != null) this.playerResourceSupplier = playerResourceSupplier;
        if (purchaseCostApplier != null) this.purchaseCostApplier = purchaseCostApplier;
        if (informationCallback != null) this.informationCallback = informationCallback;
        if (logCallback != null) this.logCallback = logCallback;
        renderYourResources();
    }

    public void showOverlay() {
        System.out.println("Buy Experiment Card popup opened");
        clearMessage();
        renderRequiredResources();
        renderYourResources();
        overlayRoot.setVisible(true);
        overlayRoot.setManaged(true);
        overlayRoot.toFront();
        normalizePopupScale();
    }

    public void hideOverlay() {
        overlayRoot.setVisible(false);
        overlayRoot.setManaged(false);
        clearMessage();
    }

    @FXML
    private void onBuyExperimentCancelClicked() {
        System.out.println("Buy Experiment Card cancel clicked");
        hideOverlay();
    }

    @FXML
    private void onBuyExperimentCardClicked() {
        System.out.println("Buy Experiment Card buy clicked");

        String validation = validatePurchase();
        if (validation != null) {
            showError(validation);
            System.out.println("Experiment Card purchase failed: " + validation);
            return;
        }

        boolean applied = purchaseCostApplier.getAsBoolean();
        if (!applied) {
            String message = "Not enough resources. You need 1 Ore, 1 Banana, and 1 Wheat.";
            showError(message);
            System.out.println("Experiment Card purchase failed: " + message);
            return;
        }

        experimentDeckSize = Math.max(0, experimentDeckSize - 1);
        String drawnCard = drawDummyExperimentCard();
        String successMessage = "Experiment Card purchased successfully! You received: " + drawnCard + ".";
        logCallback.accept(successMessage);
        System.out.println("Experiment Card purchase successful");

        hideOverlay();
        informationCallback.accept(successMessage);
    }

    private String validatePurchase() {
        if (!phaseAllowedSupplier.get()) {
            return "You can only buy Experiment Card during Trade/Build phase.";
        }
        if (experimentDeckSize <= 0) {
            return "Experiment Card deck is empty.";
        }

        int ore = Math.max(0, playerResourceSupplier.apply(ResourceType.ORE));
        int banana = Math.max(0, playerResourceSupplier.apply(ResourceType.BANANA));
        int wheat = Math.max(0, playerResourceSupplier.apply(ResourceType.WHEAT));

        if (ore < 1 && banana < 1 && wheat < 1) {
            return "Not enough resources. You need 1 Ore, 1 Banana, and 1 Wheat.";
        }
        if (ore < 1) return "Not enough Ore. You need 1 Ore.";
        if (banana < 1) return "Not enough Banana. You need 1 Banana.";
        if (wheat < 1) return "Not enough Wheat. You need 1 Wheat.";
        return null;
    }

    private String drawDummyExperimentCard() {
        List<String> dummyCards = List.of("Guard", "Innovation", "Secret Achievement Point");
        return dummyCards.get(random.nextInt(dummyCards.size()));
    }

    private void renderRequiredResources() {
        if (requiredResourceRow == null) return;
        requiredResourceRow.getChildren().clear();
        requiredResourceRow.getChildren().addAll(
                createResourceItem(ResourceType.ORE, "x1"),
                createPlusLabel(),
                createResourceItem(ResourceType.BANANA, "x1"),
                createPlusLabel(),
                createResourceItem(ResourceType.WHEAT, "x1")
        );
    }

    private void renderYourResources() {
        if (yourResourceRow == null) return;
        yourResourceRow.getChildren().clear();
        yourResourceRow.getChildren().addAll(
                createResourceItem(ResourceType.ORE, "x" + Math.max(0, playerResourceSupplier.apply(ResourceType.ORE))),
                createResourceItem(ResourceType.BANANA, "x" + Math.max(0, playerResourceSupplier.apply(ResourceType.BANANA))),
                createResourceItem(ResourceType.WHEAT, "x" + Math.max(0, playerResourceSupplier.apply(ResourceType.WHEAT)))
        );
    }

    private VBox createResourceItem(ResourceType resourceType, String quantityText) {
        VBox item = new VBox(4);
        item.getStyleClass().add("buy-experiment-resource-item");
        item.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView(loadResourceImage(resourceType));
        imageView.setFitWidth(96);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false);
        imageView.setMouseTransparent(true);

        Label qty = new Label(quantityText);
        qty.getStyleClass().add("buy-experiment-resource-qty");
        qty.setAlignment(Pos.CENTER);

        item.getChildren().addAll(imageView, qty);
        return item;
    }

    private Label createPlusLabel() {
        Label plus = new Label("+");
        plus.getStyleClass().add("buy-experiment-plus-label");
        plus.setAlignment(Pos.CENTER);
        return plus;
    }

    private Image loadResourceImage(ResourceType resourceType) {
        String path = switch (resourceType) {
            case ORE -> "/com/bananarepublic/images/game_revision/inventory_ore.png";
            case BANANA -> "/com/bananarepublic/images/game_revision/inventory_banana.png";
            case WHEAT -> "/com/bananarepublic/images/game_revision/inventory_wheat.png";
            default -> "/com/bananarepublic/images/game_revision/inventory_ore.png";
        };
        return new Image(getClass().getResource(path).toExternalForm());
    }

    private void showError(String message) {
        buyExperimentMessageLabel.setText(message);
        buyExperimentMessageLabel.setVisible(true);
        buyExperimentMessageLabel.setManaged(true);
    }

    private void clearMessage() {
        if (buyExperimentMessageLabel == null) return;
        buyExperimentMessageLabel.setText("");
        buyExperimentMessageLabel.setVisible(false);
        buyExperimentMessageLabel.setManaged(false);
    }

    private void normalizePopupScale() {
        if (popupContainer == null || overlayRoot == null) return;
        double rootW = Math.max(overlayRoot.getWidth(), 1.0);
        double rootH = Math.max(overlayRoot.getHeight(), 1.0);

        double maxPopupWidth = rootW * 0.65;
        double maxPopupHeight = rootH * 0.75;
        double scale = Math.min(maxPopupWidth / POPUP_WIDTH, maxPopupHeight / POPUP_HEIGHT);
        scale = Math.min(scale, 1.0);

        if (scale <= 0 || Double.isNaN(scale) || Double.isInfinite(scale)) {
            scale = 1.0;
        }

        popupContainer.setScaleX(scale);
        popupContainer.setScaleY(scale);
    }
}
