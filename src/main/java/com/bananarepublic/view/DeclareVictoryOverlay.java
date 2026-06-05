package com.bananarepublic.view;

import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DeclareVictoryOverlay {

    private static final double ASSET_WIDTH = 1998.0;
    private static final double ASSET_HEIGHT = 1124.0;

    @FXML private StackPane overlayRoot;
    @FXML private StackPane centerLayer;
    @FXML private Group modalScaleGroup;
    @FXML private Pane popupContainer;
    @FXML private ImageView popupImageView;
    @FXML private Pane fireworksLayer;
    @FXML private ImageView minionDanceImageView;
    @FXML private ImageView minionGuitarImageView;

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

    private final Random fireworksRandom = new Random();
    private final List<NodeAnimationBundle> activeFireworkBursts = new ArrayList<>();
    private Timeline fireworksSpawner;
    private AudioClip hoorayAudio;
    private TranslateTransition danceMotion;
    private TranslateTransition guitarMotion;
    private ScaleTransition danceBobMotion;
    private ScaleTransition guitarBobMotion;

    private Supplier<Integer> currentPlayerIndexSupplier = () -> 0;
    private Supplier<List<VictoryLeaderboardEntry>> leaderboardSupplier = ArrayList::new;
    private Consumer<String> informationCallback = msg -> {};
    private Runnable mainMenuAction = () -> {};
    private BooleanSupplier saveAction = () -> true;

    private static class NodeAnimationBundle {
        final Circle circle;
        final FadeTransition fadeTransition;
        final TranslateTransition translateTransition;
        final ScaleTransition scaleTransition;

        NodeAnimationBundle(Circle circle,
                            FadeTransition fadeTransition,
                            TranslateTransition translateTransition,
                            ScaleTransition scaleTransition) {
            this.circle = circle;
            this.fadeTransition = fadeTransition;
            this.translateTransition = translateTransition;
            this.scaleTransition = scaleTransition;
        }
    }

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
        preloadVictoryAudio();
        setupVictoryMotion();
        setupFireworksSpawner();
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
        startVictoryEffects();
        System.out.println("Declare Victory success: winner = " + winner.playerName);
        return true;
    }

    public void hideDeclareVictoryScreen() {
        if (overlayRoot == null) return;
        stopVictoryEffects();
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

    private void preloadVictoryAudio() {
        try {
            var resource = getClass().getResource("/com/bananarepublic/sound/hooray.mp3");
            if (resource != null) {
                hoorayAudio = new AudioClip(resource.toExternalForm());
                hoorayAudio.setCycleCount(AudioClip.INDEFINITE);
                hoorayAudio.setVolume(0.8);
            }
        } catch (Exception ignored) {
            hoorayAudio = null;
        }
    }

    private void setupVictoryMotion() {
        if (minionDanceImageView != null) {
            danceMotion = new TranslateTransition(Duration.seconds(2.8), minionDanceImageView);
            danceMotion.setFromX(-18);
            danceMotion.setToX(18);
            danceMotion.setAutoReverse(true);
            danceMotion.setCycleCount(Animation.INDEFINITE);
            danceMotion.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

            danceBobMotion = new ScaleTransition(Duration.seconds(1.4), minionDanceImageView);
            danceBobMotion.setFromY(0.98);
            danceBobMotion.setToY(1.05);
            danceBobMotion.setAutoReverse(true);
            danceBobMotion.setCycleCount(Animation.INDEFINITE);
            danceBobMotion.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        }

        if (minionGuitarImageView != null) {
            guitarMotion = new TranslateTransition(Duration.seconds(3.4), minionGuitarImageView);
            guitarMotion.setFromX(18);
            guitarMotion.setToX(-18);
            guitarMotion.setAutoReverse(true);
            guitarMotion.setCycleCount(Animation.INDEFINITE);
            guitarMotion.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

            guitarBobMotion = new ScaleTransition(Duration.seconds(1.7), minionGuitarImageView);
            guitarBobMotion.setFromY(1.02);
            guitarBobMotion.setToY(0.96);
            guitarBobMotion.setAutoReverse(true);
            guitarBobMotion.setCycleCount(Animation.INDEFINITE);
            guitarBobMotion.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        }
    }

    private void setupFireworksSpawner() {
        fireworksSpawner = new Timeline(new KeyFrame(Duration.seconds(0.85), event -> spawnFireworksBurst()));
        fireworksSpawner.setCycleCount(Animation.INDEFINITE);
    }

    private void startVictoryEffects() {
        if (hoorayAudio != null) {
            hoorayAudio.stop();
            hoorayAudio.play();
        }
        if (danceMotion != null) danceMotion.playFromStart();
        if (danceBobMotion != null) danceBobMotion.playFromStart();
        if (guitarMotion != null) guitarMotion.playFromStart();
        if (guitarBobMotion != null) guitarBobMotion.playFromStart();
        if (fireworksSpawner != null) fireworksSpawner.playFromStart();
        spawnFireworksBurst();
    }

    private void stopVictoryEffects() {
        if (hoorayAudio != null) {
            hoorayAudio.stop();
        }
        if (fireworksSpawner != null) {
            fireworksSpawner.stop();
        }
        if (danceMotion != null) {
            danceMotion.stop();
        }
        if (danceBobMotion != null) {
            danceBobMotion.stop();
        }
        if (guitarMotion != null) {
            guitarMotion.stop();
        }
        if (guitarBobMotion != null) {
            guitarBobMotion.stop();
        }
        if (fireworksLayer != null) {
            fireworksLayer.getChildren().clear();
        }
        activeFireworkBursts.clear();
    }

    private void spawnFireworksBurst() {
        if (fireworksLayer == null) return;

        double width = fireworksLayer.getWidth();
        double height = fireworksLayer.getHeight();
        if ((width <= 1.0 || height <= 1.0) && fireworksLayer.getScene() != null) {
            width = fireworksLayer.getScene().getWidth();
            height = fireworksLayer.getScene().getHeight();
        }
        width = Math.max(width, 1.0);
        height = Math.max(height, 1.0);

        double centerX = width * (0.2 + fireworksRandom.nextDouble() * 0.6);
        double centerY = height * (0.09 + fireworksRandom.nextDouble() * 0.22);
        int particleCount = 14 + fireworksRandom.nextInt(6);
        double baseHue = fireworksRandom.nextDouble() * 360.0;

        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2.0 * i) / particleCount + fireworksRandom.nextDouble() * 0.22;
            double distance = 45.0 + fireworksRandom.nextDouble() * 70.0;
            double radius = 4.0 + fireworksRandom.nextDouble() * 4.5;
            Circle particle = new Circle(radius, Color.hsb((baseHue + i * 8.0) % 360.0, 0.9, 1.0));
            particle.setCenterX(centerX);
            particle.setCenterY(centerY);
            particle.setOpacity(0.95);
            fireworksLayer.getChildren().add(particle);

            TranslateTransition travel = new TranslateTransition(Duration.seconds(1.2 + fireworksRandom.nextDouble() * 0.6), particle);
            travel.setByX(Math.cos(angle) * distance);
            travel.setByY(Math.sin(angle) * distance * 0.75);
            travel.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(1.2 + fireworksRandom.nextDouble() * 0.6), particle);
            scale.setFromX(0.7);
            scale.setFromY(0.7);
            scale.setToX(1.8);
            scale.setToY(1.8);

            FadeTransition fade = new FadeTransition(Duration.seconds(1.2 + fireworksRandom.nextDouble() * 0.6), particle);
            fade.setFromValue(0.95);
            fade.setToValue(0.0);

            NodeAnimationBundle burst = new NodeAnimationBundle(particle, fade, travel, scale);
            activeFireworkBursts.add(burst);

            fade.setOnFinished(event -> removeFireworkBurst(burst));
            travel.play();
            scale.play();
            fade.play();
        }
        trimExpiredBursts();
    }

    private void removeFireworkBurst(NodeAnimationBundle burst) {
        activeFireworkBursts.remove(burst);
        if (fireworksLayer != null) {
            fireworksLayer.getChildren().remove(burst.circle);
        }
    }

    private void trimExpiredBursts() {
        if (activeFireworkBursts.isEmpty()) return;
        Iterator<NodeAnimationBundle> iterator = activeFireworkBursts.iterator();
        while (iterator.hasNext()) {
            NodeAnimationBundle burst = iterator.next();
            if (burst.circle.getOpacity() <= 0.01) {
                iterator.remove();
            }
        }
    }
}
