package com.bananarepublic.view;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;

public abstract class BaseOverlay {

    @FXML protected StackPane overlayRoot;
    @FXML protected Group modalScaleGroup;

    protected void initializeOverlayListeners() {
        if (overlayRoot != null) {
            overlayRoot.widthProperty().addListener((obs, oldVal, newVal) -> normalizePopupScale());
            overlayRoot.heightProperty().addListener((obs, oldVal, newVal) -> normalizePopupScale());
        }
    }

    protected void showBase() {
        overlayRoot.setVisible(true);
        overlayRoot.setManaged(true);
        overlayRoot.toFront();
        normalizePopupScale();
    }

    protected void hideBase() {
        overlayRoot.setVisible(false);
        overlayRoot.setManaged(false);
    }

    protected abstract void normalizePopupScale();
}
