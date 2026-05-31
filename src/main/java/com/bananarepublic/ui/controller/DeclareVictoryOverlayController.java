package com.bananarepublic.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DeclareVictoryOverlayController {

    private static final double ASSET_WIDTH = 1998.0;
    private static final double ASSET_HEIGHT = 1124.0;

    @FXML private StackPane overlayRoot;
    @FXML private StackPane centerLayer;
    @FXML private Group modalScaleGroup;
    @FXML private Pane popupContainer;
    @FXML private ImageView popupImageView;

    @FXML private ImageView winnerProfileImageView;
    @FXML private Label winnerNameLabel;
    @FXML private Label winnerPointsLabel;

    @FXML private ImageView row1ProfileImageView;
    @FXML private ImageView row2ProfileImageView;
    @FXML private ImageView row3ProfileImageView;
    @FXML private ImageView row4ProfileImageView;

    @FXML private Label row1NameLabel;
    @FXML private Label row2NameLabel;
    @FXML private Label row3NameLabel;
    @FXML private Label row4NameLabel;

    @FXML private Label row1PointsLabel;
    @FXML private Label row2PointsLabel;
    @FXML private Label row3PointsLabel;
    @FXML private Label row4PointsLabel;

    @FXML private Button victorySaveButton;
    @FXML private Button victoryMainMenuButton;

    private Supplier<Integer> currentPlayerIndexSupplier = () -> 0;
    private Supplier<List<VictoryLeaderboardEntry>> leaderboardSupplier = ArrayList::new;
    private Consumer<String> informationCallback = msg -> {};
    private Runnable mainMenuAction = () -> {};
    private BooleanSupplier saveAction = () -> true;

    public static class VictoryLeaderboardEntry {
        public final int playerIndex;
        public final String playerName;
        public final int victoryPoints;
        public final Image profileImage;

        public VictoryLeaderboardEntry(int playerIndex, String playerName, int victoryPoints, Image profileImage) {
            this.playerIndex = playerIndex;
            this.playerName = playerName;
            this.victoryPoints = victoryPoints;
            this.profileImage = profileImage;
        }
    }

    @FXML
    public void initialize() {
        if (popupImageView != null) {
            popupImageView.setMouseTransparent(true);
        }
        if (overlayRoot != null) {
            overlayRoot.widthProperty().addListener((obs, oldVal, newVal) -> normalizePopupScale());
            overlayRoot.heightProperty().addListener((obs, oldVal, newVal) -> normalizePopupScale());
        }
        hideDeclareVictoryScreen();
        normalizePopupScale();
    }

    public void configure(Supplier<Integer> currentPlayerIndexSupplier,
                          Supplier<List<VictoryLeaderboardEntry>> leaderboardSupplier,
                          Consumer<String> informationCallback,
                          Runnable mainMenuAction,
                          BooleanSupplier saveAction) {
        if (currentPlayerIndexSupplier != null) this.currentPlayerIndexSupplier = currentPlayerIndexSupplier;
        if (leaderboardSupplier != null) this.leaderboardSupplier = leaderboardSupplier;
        if (informationCallback != null) this.informationCallback = informationCallback;
        if (mainMenuAction != null) this.mainMenuAction = mainMenuAction;
        if (saveAction != null) this.saveAction = saveAction;
    }

    public boolean showDeclareVictoryScreen() {
        List<VictoryLeaderboardEntry> entries = getLeaderboardEntries();
        if (entries.isEmpty()) {
            informationCallback.accept("Cannot determine leaderboard data.");
            return false;
        }

        int currentPlayerIndex = currentPlayerIndexSupplier.get();
        VictoryLeaderboardEntry currentPlayerEntry = entries.stream()
                .filter(entry -> entry.playerIndex == currentPlayerIndex)
                .findFirst()
                .orElse(entries.get(0));

        int currentVp = Math.max(0, currentPlayerEntry.victoryPoints);
        System.out.println("Current player VP: " + currentVp);
        if (!canDeclareVictory(currentVp)) {
            System.out.println("Declare Victory failed: not enough VP");
            informationCallback.accept(
                    "You only have " + currentVp + " Victory Points. You need 10 Victory Points to declare victory."
            );
            return false;
        }

        VictoryLeaderboardEntry winner = renderWinner(entries, currentPlayerEntry.playerIndex);
        renderLeaderboard(entries);

        overlayRoot.setVisible(true);
        overlayRoot.setManaged(true);
        overlayRoot.toFront();
        normalizePopupScale();
        System.out.println("Declare Victory success: winner = " + winner.playerName);
        return true;
    }

    public void hideDeclareVictoryScreen() {
        if (overlayRoot == null) return;
        overlayRoot.setVisible(false);
        overlayRoot.setManaged(false);
    }

    private boolean canDeclareVictory(int currentPlayerVp) {
        return currentPlayerVp >= 10;
    }

    private List<VictoryLeaderboardEntry> getLeaderboardEntries() {
        List<VictoryLeaderboardEntry> rawEntries = leaderboardSupplier.get();
        List<VictoryLeaderboardEntry> safeEntries = rawEntries == null ? new ArrayList<>() : new ArrayList<>(rawEntries);
        safeEntries.removeIf(Objects::isNull);
        safeEntries.sort(
                Comparator.comparingInt((VictoryLeaderboardEntry e) -> e.victoryPoints).reversed()
                        .thenComparingInt(e -> e.playerIndex)
        );
        return safeEntries;
    }

    private VictoryLeaderboardEntry renderWinner(List<VictoryLeaderboardEntry> sortedEntries, int currentPlayerIndex) {
        VictoryLeaderboardEntry winner = sortedEntries.stream()
                .max(Comparator.comparingInt((VictoryLeaderboardEntry e) -> e.victoryPoints)
                        .thenComparingInt(e -> e.playerIndex == currentPlayerIndex ? 1 : 0))
                .orElse(sortedEntries.get(0));

        winnerProfileImageView.setImage(winner.profileImage);
        winnerNameLabel.setText(winner.playerName);
        winnerPointsLabel.setText(String.valueOf(winner.victoryPoints));
        return winner;
    }

    private void renderLeaderboard(List<VictoryLeaderboardEntry> sortedEntries) {
        ImageView[] profileViews = {row1ProfileImageView, row2ProfileImageView, row3ProfileImageView, row4ProfileImageView};
        Label[] nameLabels = {row1NameLabel, row2NameLabel, row3NameLabel, row4NameLabel};
        Label[] pointLabels = {row1PointsLabel, row2PointsLabel, row3PointsLabel, row4PointsLabel};

        for (int i = 0; i < 4; i++) {
            if (i >= sortedEntries.size()) {
                profileViews[i].setVisible(false);
                profileViews[i].setManaged(false);
                nameLabels[i].setText("");
                pointLabels[i].setText("");
                continue;
            }

            VictoryLeaderboardEntry entry = sortedEntries.get(i);
            profileViews[i].setVisible(true);
            profileViews[i].setManaged(true);
            profileViews[i].setImage(entry.profileImage);
            nameLabels[i].setText(entry.playerName);
            pointLabels[i].setText(String.valueOf(Math.max(0, entry.victoryPoints)));
        }
    }

    @FXML
    private void onVictoryMainMenuClicked() {
        System.out.println("Victory main menu clicked");
        hideDeclareVictoryScreen();
        try {
            mainMenuAction.run();
        } catch (Exception e) {
            informationCallback.accept("Failed to open main menu.");
        }
    }

    @FXML
    private void onVictorySaveClicked() {
        System.out.println("Victory save clicked");
        boolean success;
        try {
            success = saveAction.getAsBoolean();
        } catch (Exception e) {
            success = false;
        }
        if (success) {
            informationCallback.accept("Game saved successfully.");
        } else {
            informationCallback.accept("Failed to save game.");
        }
    }

    private void normalizePopupScale() {
        if (modalScaleGroup == null || overlayRoot == null) return;
        double rootW = overlayRoot.getWidth();
        double rootH = overlayRoot.getHeight();
        if ((rootW <= 1.0 || rootH <= 1.0) && overlayRoot.getScene() != null) {
            rootW = overlayRoot.getScene().getWidth();
            rootH = overlayRoot.getScene().getHeight();
        }
        rootW = Math.max(rootW, 1.0);
        rootH = Math.max(rootH, 1.0);

        // Keep safe margins so the full background never touches viewport edges.
        double maxW = rootW * 0.88;
        double maxH = rootH * 0.88;
        double scale = Math.min(maxW / ASSET_WIDTH, maxH / ASSET_HEIGHT);
        scale = Math.min(scale, 1.0);

        if (scale <= 0 || Double.isNaN(scale) || Double.isInfinite(scale)) {
            scale = 1.0;
        }
        modalScaleGroup.setScaleX(scale);
        modalScaleGroup.setScaleY(scale);
    }
}
