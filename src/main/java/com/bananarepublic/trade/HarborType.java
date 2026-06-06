package com.bananarepublic.trade;

public enum HarborType {
    GENERAL,
    BANANA,
    WOOD,
    WHEAT,
    ORE,
    BRICK;

    public TradeResource specialResource() {
        return switch (this) {
            case GENERAL -> null;
            case BANANA -> TradeResource.BANANA;
            case WOOD -> TradeResource.WOOD;
            case WHEAT -> TradeResource.WHEAT;
            case ORE -> TradeResource.ORE;
            case BRICK -> TradeResource.BRICK;
        };
    }

    public int requiredAmount() {
        return this == GENERAL ? 3 : 2;
    }
}
