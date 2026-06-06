package com.bananarepublic.trade;

import java.util.function.Consumer;

public class TradeOverlayManager {
    private final TradeOverlayRefs refs;
    private final Runnable showTradeChoiceCallback;
    private final Runnable showBankTradeCallback;
    private final Runnable showPlayerTradeCallback;
    private final Runnable showHarborTradeCallback;
    private final Runnable hideTradeOverlayCallback;
    private final Consumer<String> logCallback;

    public TradeOverlayManager(
            TradeOverlayRefs refs,
            Runnable showTradeChoiceCallback,
            Runnable showBankTradeCallback,
            Runnable showPlayerTradeCallback,
            Runnable showHarborTradeCallback,
            Runnable hideTradeOverlayCallback,
            Consumer<String> logCallback) {
        this.refs = refs;
        this.showTradeChoiceCallback = showTradeChoiceCallback;
        this.showBankTradeCallback = showBankTradeCallback;
        this.showPlayerTradeCallback = showPlayerTradeCallback;
        this.showHarborTradeCallback = showHarborTradeCallback;
        this.hideTradeOverlayCallback = hideTradeOverlayCallback;
        this.logCallback = logCallback;
    }

    public TradeOverlayRefs getRefs() {
        return refs;
    }

    public void showTradeChoice() {
        showTradeChoiceCallback.run();
    }

    public void showBankTrade() {
        showBankTradeCallback.run();
        logCallback.accept("Bank trade selected.");
    }

    public void showPlayerTrade() {
        showPlayerTradeCallback.run();
        logCallback.accept("Player trade selected.");
    }

    public void showHarborTrade() {
        showHarborTradeCallback.run();
        logCallback.accept("Harbor trade selected.");
    }

    public void hideTradeOverlay() {
        hideTradeOverlayCallback.run();
    }
}
