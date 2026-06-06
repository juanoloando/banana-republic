package com.bananarepublic.trade;

public class TradeResult {
    private final boolean success;
    private final String message;

    private TradeResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static TradeResult success(String message) {
        return new TradeResult(true, message);
    }

    public static TradeResult failure(String message) {
        return new TradeResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
