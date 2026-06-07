package com.bananarepublic.view;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class RollDiceManualOverlay extends BaseOverlay {

    private static final double POPUP_WIDTH = 887.0;
    private static final double POPUP_HEIGHT = 722.0;
    private static final double CANCEL_X = 230.0;
    private static final double CANCEL_Y = 574.0;
    private static final double CANCEL_W = 199.0;
    private static final double CANCEL_H = 52.0;
    private static final double ROLL_X = 471.0;
    private static final double ROLL_Y = 572.0;
    private static final double ROLL_W = 199.0;
    private static final double ROLL_H = 52.0;

    @FXML private StackPane centerLayer;
    @FXML private Pane popupContainer;
    @FXML private ImageView popupImageView;
    @FXML private ComboBox<Integer> dice1ComboBox;
    @FXML private ComboBox<Integer> dice2ComboBox;
    @FXML private ImageView dice1ImageView;
    @FXML private ImageView dice2ImageView;
    @FXML private Label diceManualErrorLabel;
    @FXML private Button cancelButton;
    @FXML private Button rollDiceButton;

    private Supplier<Boolean> phaseAllowedSupplier = () -> true;
    private BiConsumer<Integer, Integer> onRollDiceCallback = (a, b) -> {};

    @FXML
    public void initialize() {
        if (popupImageView != null) {
            popupImageView.setMouseTransparent(true);
        }
        if (cancelButton != null) {
            cancelButton.setLayoutX(CANCEL_X);
            cancelButton.setLayoutY(CANCEL_Y);
            cancelButton.setPrefWidth(CANCEL_W);
            cancelButton.setPrefHeight(CANCEL_H);
        }
        if (rollDiceButton != null) {
            rollDiceButton.setLayoutX(ROLL_X);
            rollDiceButton.setLayoutY(ROLL_Y);
            rollDiceButton.setPrefWidth(ROLL_W);
            rollDiceButton.setPrefHeight(ROLL_H);
        }
        if (dice1ComboBox != null) {
            dice1ComboBox.getItems().setAll(1, 2, 3, 4, 5, 6);
            dice1ComboBox.getStyleClass().add("dice-manual-combo");
            dice1ComboBox.setValue(1);
            dice1ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateDiceImage(dice1ImageView, newVal);
                    System.out.println("Dice 1 selected: " + newVal);
                }
            });
        }
        if (dice2ComboBox != null) {
            dice2ComboBox.getItems().setAll(1, 2, 3, 4, 5, 6);
            dice2ComboBox.getStyleClass().add("dice-manual-combo");
            dice2ComboBox.setValue(1);
            dice2ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateDiceImage(dice2ImageView, newVal);
                    System.out.println("Dice 2 selected: " + newVal);
                }
            });
        }
        if (dice1ImageView != null) {
            dice1ImageView.getStyleClass().add("dice-manual-image");
            updateDiceImage(dice1ImageView, 1);
        }
        if (dice2ImageView != null) {
            dice2ImageView.getStyleClass().add("dice-manual-image");
            updateDiceImage(dice2ImageView, 1);
        }
        initializeOverlayListeners();
        clearError();
        normalizePopupScale();
    }

    public void configure(Supplier<Boolean> phaseAllowedSupplier,
                          BiConsumer<Integer, Integer> onRollDiceCallback) {
        if (phaseAllowedSupplier != null) this.phaseAllowedSupplier = phaseAllowedSupplier;
        if (onRollDiceCallback != null) this.onRollDiceCallback = onRollDiceCallback;
    }

    public void showOverlay() {
        clearError();
        if (dice1ComboBox != null && dice1ComboBox.getValue() == null) {
            dice1ComboBox.setValue(1);
        }
        if (dice2ComboBox != null && dice2ComboBox.getValue() == null) {
            dice2ComboBox.setValue(1);
        }
        updateDiceImage(dice1ImageView, dice1ComboBox == null ? 1 : dice1ComboBox.getValue());
        updateDiceImage(dice2ImageView, dice2ComboBox == null ? 1 : dice2ComboBox.getValue());
        showBase();
    }

    public void hideOverlay() {
        hideBase();
        clearError();
    }

    @FXML
    private void onCancelClicked() {
        System.out.println("Manual dice popup cancelled");
        hideOverlay();
    }

    @FXML
    private void onRollDiceClicked() {
        if (!phaseAllowedSupplier.get()) {
            showError("You can only roll manually during Roll Dice phase.");
            return;
        }
        Integer d1 = dice1ComboBox == null ? null : dice1ComboBox.getValue();
        Integer d2 = dice2ComboBox == null ? null : dice2ComboBox.getValue();
        if (d1 == null) {
            showError("Please select Dice 1.");
            return;
        }
        if (d2 == null) {
            showError("Please select Dice 2.");
            return;
        }
        clearError();
        onRollDiceCallback.accept(d1, d2);
        hideOverlay();
    }

    private void showError(String message) {
        diceManualErrorLabel.setText(message);
        diceManualErrorLabel.setVisible(true);
        diceManualErrorLabel.setManaged(true);
        diceManualErrorLabel.setAlignment(Pos.CENTER);
    }

    private void clearError() {
        if (diceManualErrorLabel == null) return;
        diceManualErrorLabel.setText("");
        diceManualErrorLabel.setVisible(false);
        diceManualErrorLabel.setManaged(false);
    }

    private void updateDiceImage(ImageView view, int value) {
        if (view == null) return;
        int clamped = Math.max(1, Math.min(6, value));
        String path = "/com/bananarepublic/images/game_revision/dice" + clamped + ".png";
        view.setImage(new Image(getClass().getResource(path).toExternalForm()));
    }

    @Override
    protected void normalizePopupScale() {
        if (modalScaleGroup == null || overlayRoot == null) return;
        double rootW = Math.max(overlayRoot.getWidth(), 1.0);
        double rootH = Math.max(overlayRoot.getHeight(), 1.0);
        double scale = Math.min((rootW * 0.62) / POPUP_WIDTH, (rootH * 0.70) / POPUP_HEIGHT);
        scale = Math.min(scale, 1.0);
        if (scale <= 0 || Double.isNaN(scale) || Double.isInfinite(scale)) {
            scale = 1.0;
        }
        modalScaleGroup.setScaleX(scale);
        modalScaleGroup.setScaleY(scale);
    }
}
