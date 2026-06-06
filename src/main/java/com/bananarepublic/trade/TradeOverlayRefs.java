package com.bananarepublic.trade;

import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class TradeOverlayRefs {
    public final StackPane tradeOverlayRoot;
    public final StackPane tradeOverlayCenterLayer;
    public final Group tradeModalScaleGroup;
    public final Pane popupOverlay;
    public final Pane tradePopupContainer;
    public final Button tradeChoiceBankButton;
    public final Button tradeChoicePlayerButton;
    public final Button tradeChoiceHarborButton;
    public final AnchorPane tradeUiPane;
    public final Label tradeErrorLabel;
    public final Label playerTradeMessageLabel;
    public final HBox tradeActionButtonsBox;
    public final Label bankTradeMessageLabel;
    public final Button bankTradeCancelButton;
    public final Button bankTradeConfirmButton;

    public TradeOverlayRefs(
            StackPane tradeOverlayRoot,
            StackPane tradeOverlayCenterLayer,
            Group tradeModalScaleGroup,
            Pane popupOverlay,
            Pane tradePopupContainer,
            Button tradeChoiceBankButton,
            Button tradeChoicePlayerButton,
            Button tradeChoiceHarborButton,
            AnchorPane tradeUiPane,
            Label tradeErrorLabel,
            Label playerTradeMessageLabel,
            HBox tradeActionButtonsBox,
            Label bankTradeMessageLabel,
            Button bankTradeCancelButton,
            Button bankTradeConfirmButton) {
        this.tradeOverlayRoot = tradeOverlayRoot;
        this.tradeOverlayCenterLayer = tradeOverlayCenterLayer;
        this.tradeModalScaleGroup = tradeModalScaleGroup;
        this.popupOverlay = popupOverlay;
        this.tradePopupContainer = tradePopupContainer;
        this.tradeChoiceBankButton = tradeChoiceBankButton;
        this.tradeChoicePlayerButton = tradeChoicePlayerButton;
        this.tradeChoiceHarborButton = tradeChoiceHarborButton;
        this.tradeUiPane = tradeUiPane;
        this.tradeErrorLabel = tradeErrorLabel;
        this.playerTradeMessageLabel = playerTradeMessageLabel;
        this.tradeActionButtonsBox = tradeActionButtonsBox;
        this.bankTradeMessageLabel = bankTradeMessageLabel;
        this.bankTradeCancelButton = bankTradeCancelButton;
        this.bankTradeConfirmButton = bankTradeConfirmButton;
    }
}
