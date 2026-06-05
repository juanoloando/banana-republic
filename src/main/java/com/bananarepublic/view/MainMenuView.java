package com.bananarepublic.view;

import com.bananarepublic.BananaRepublic;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainMenuView {

    @FXML private StackPane rootPane;
    @FXML private VBox menuButtons;
    @FXML private Pane minionLayer;

    @FXML private ImageView floatMinionView;
    @FXML private ImageView duckMinionView;
    @FXML private ImageView snorkelMinionView;

    @FXML private ImageView waterMinionView;

    @FXML private ImageView angelMinionFly;
    @FXML private ImageView birdMinionFly;
    @FXML private ImageView witchMinionFly;

    private AudioClip whooshAudio;
    private final Random rng = new Random();
    private Timeline flyScheduler;
    private final List<TranslateTransition> activeFlyTransitions = new ArrayList<>();

    @FXML
    private void initialize() {
        menuButtons.translateYProperty().bind(rootPane.heightProperty().multiply(0.305));
        menuButtons.scaleXProperty().bind(Bindings.min(
                1.0,
                Bindings.min(
                        rootPane.widthProperty().divide(1280.0),
                        rootPane.heightProperty().divide(720.0)
                )
        ));
        menuButtons.scaleYProperty().bind(menuButtons.scaleXProperty());

        minionLayer.prefWidthProperty().bind(rootPane.widthProperty());
        minionLayer.prefHeightProperty().bind(rootPane.heightProperty());

        setupMinionSize(floatMinionView, 0.08);
        setupMinionSize(duckMinionView, 0.08);
        setupMinionSize(snorkelMinionView, 0.08);
        setupMinionSize(waterMinionView, 0.09);

        // Water minion diposisikan di sudut kanan bawah tulisan BANANA republic
        positionMinion(waterMinionView,  0.650, 0.450);

        // duck, float, snorkel ukurannya sama dan posisinya dirapatkan di atas air (bawah kanan)
        positionMinion(duckMinionView,   0.760, 0.850);
        positionMinion(floatMinionView,  0.810, 0.850);
        positionMinion(snorkelMinionView,0.860, 0.850);

        // Mengurangi ukuran amplitudo agar efeknya goyang sedikit sesuai referensi
        startBobAnimation(floatMinionView,   5.0, 1.85, 0.0);
        startBobAnimation(duckMinionView,    4.0, 2.10, 0.6);
        startBobAnimation(snorkelMinionView, 4.5, 1.65, 1.2);
        startBobAnimation(waterMinionView,   6.0, 2.00, 0.3);

        preloadWhoosh();
        scheduleFlyingMinions();
    }

    private void setupMinionSize(ImageView view, double sizeRatio) {
        view.fitWidthProperty().bind(rootPane.widthProperty().multiply(sizeRatio));
        view.setPreserveRatio(true);
    }

    private void positionMinion(ImageView view, double xFrac, double yFrac) {
        view.layoutXProperty().bind(minionLayer.widthProperty().multiply(xFrac)
                .subtract(view.fitWidthProperty().divide(2.0)));
        view.layoutYProperty().bind(minionLayer.heightProperty().multiply(yFrac)
                .subtract(Bindings.createDoubleBinding(
                        () -> view.getLayoutBounds().getHeight() / 2.0, 
                        view.layoutBoundsProperty())));
    }


    private void startBobAnimation(ImageView view, double amplitude, double durationSecs, double phaseDelay) {
        TranslateTransition bob = new TranslateTransition(Duration.seconds(durationSecs), view);
        bob.setFromY(-amplitude);
        bob.setToY(amplitude);
        bob.setAutoReverse(true);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.setInterpolator(Interpolator.EASE_BOTH);

        RotateTransition wobble = new RotateTransition(Duration.seconds(durationSecs * 1.3), view);
        wobble.setFromAngle(-2.0);
        wobble.setToAngle(2.0);
        wobble.setAutoReverse(true);
        wobble.setCycleCount(Animation.INDEFINITE);
        wobble.setInterpolator(Interpolator.EASE_BOTH);

        if (phaseDelay > 0) {
            PauseTransition delay = new PauseTransition(Duration.seconds(phaseDelay));
            delay.setOnFinished(e -> { bob.play(); wobble.play(); });
            delay.play();
        } else {
            bob.play();
            wobble.play();
        }
    }

    private void preloadWhoosh() {
        try {
            var res = getClass().getResource("/com/bananarepublic/sound/whoosh.mp3");
            if (res != null) {
                whooshAudio = new AudioClip(res.toExternalForm());
                whooshAudio.setVolume(0.65);
            }
        } catch (Exception ignored) {
            whooshAudio = null;
        }
    }

    private void scheduleFlyingMinions() {
        double delay = 3.5 + rng.nextDouble() * 4.5;
        flyScheduler = new Timeline(new KeyFrame(Duration.seconds(delay), e -> {
            launchRandomFlyingMinion();
            scheduleFlyingMinions(); // reschedule recursively
        }));
        flyScheduler.setCycleCount(1);
        flyScheduler.play();
    }

    private void launchRandomFlyingMinion() {
        ImageView[] candidates = { angelMinionFly, birdMinionFly, witchMinionFly };
        List<ImageView> available = new ArrayList<>();
        for (ImageView m : candidates) {
            if (!m.isVisible()) available.add(m);
        }
        if (available.isEmpty()) return;
        launchFlyingMinion(available.get(rng.nextInt(available.size())));
    }

    
    private void launchFlyingMinion(ImageView view) {
        double layerW = minionLayer.getWidth();
        double layerH = minionLayer.getHeight();
        if (layerW < 10 || layerH < 10) {
            layerW = rootPane.getWidth();
            layerH = rootPane.getHeight();
        }
        if (layerW < 10 || layerH < 10) return;

        double startY = layerH * (0.06 + rng.nextDouble() * 0.10);
        view.setLayoutX(-view.getFitWidth() - 20);
        view.setLayoutY(startY);
        view.setTranslateX(0);
        view.setTranslateY(0);
        view.setVisible(true);
        view.setOpacity(1.0);

        double flyDuration = 3.8 + rng.nextDouble() * 2.0;
        double totalDist   = layerW + view.getFitWidth() * 3;

        TranslateTransition fly = new TranslateTransition(Duration.seconds(flyDuration), view);
        activeFlyTransitions.add(fly);
        fly.setFromX(0);
        fly.setToX(totalDist);
        fly.setInterpolator(Interpolator.LINEAR);
        fly.setOnFinished(e -> {
            view.setVisible(false);
            view.setTranslateX(0);
            activeFlyTransitions.remove(fly);
        });

        if (whooshAudio != null) whooshAudio.play();
        fly.play();
    }

    private void stopAnimations() {
        if (flyScheduler != null) {
            flyScheduler.stop();
        }
        if (whooshAudio != null && whooshAudio.isPlaying()) {
            whooshAudio.stop();
        }
        for (TranslateTransition fly : activeFlyTransitions) {
            fly.stop();
            if (fly.getNode() != null) {
                fly.getNode().setVisible(false);
            }
        }
        activeFlyTransitions.clear();
    }

    @FXML
    private void onNewGameClicked() throws Exception {
        stopAnimations();
        BananaRepublic.showLobby();
    }

    @FXML
    private void onLoadGameClicked() throws Exception {
        stopAnimations();
        BananaRepublic.showLoadGame();
    }

    @FXML
    private void onExitClicked() {
        System.exit(0);
    }
}
