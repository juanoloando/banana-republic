package com.bananarepublic.trade;

public class PlayerNegotiationState {
    private TradeOffer activeOffer;
    private TradeOffer activeCounterOffer;
    private int proposerIndex = -1;
    private int targetIndex = -1;

    public TradeOffer getActiveOffer() {
        return activeOffer == null ? null : activeOffer.copy();
    }

    public void setActiveOffer(TradeOffer activeOffer) {
        this.activeOffer = activeOffer == null ? null : activeOffer.copy();
    }

    public TradeOffer getActiveCounterOffer() {
        return activeCounterOffer == null ? null : activeCounterOffer.copy();
    }

    public void setActiveCounterOffer(TradeOffer activeCounterOffer) {
        this.activeCounterOffer = activeCounterOffer == null ? null : activeCounterOffer.copy();
    }

    public int getProposerIndex() {
        return proposerIndex;
    }

    public void setProposerIndex(int proposerIndex) {
        this.proposerIndex = proposerIndex;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public void clear() {
        activeOffer = null;
        activeCounterOffer = null;
        proposerIndex = -1;
        targetIndex = -1;
    }
}
