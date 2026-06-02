package com.bananarepublic.view;

import com.bananarepublic.BananaRepublic;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuView {
    @FXML
    private StackPane rootPane;

    @FXML
    private VBox menuButtons;

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
    }

    @FXML
    private void onNewGameClicked() throws Exception {
        BananaRepublic.showLobby();
    }

    @FXML
    private void onLoadGameClicked() throws Exception {
        BananaRepublic.showLoadGame();
    }

    @FXML
    private void onExitClicked() {
        System.exit(0);
    }
}
