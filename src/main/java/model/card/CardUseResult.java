package model.card;

public class CardUseResult {
    private final boolean success;
    private final String message;
    private final boolean closePopup;
    private final boolean requiresFollowUp;

    public CardUseResult(boolean success, String message, boolean closePopup, boolean requiresFollowUp) {
        this.success = success;
        this.message = message;
        this.closePopup = closePopup;
        this.requiresFollowUp = requiresFollowUp;
    }

    public static CardUseResult success(String message, boolean closePopup, boolean requiresFollowUp) {
        return new CardUseResult(true, message, closePopup, requiresFollowUp);
    }

    public static CardUseResult fail(String message) {
        return new CardUseResult(false, message, false, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isClosePopup() {
        return closePopup;
    }

    public boolean isRequiresFollowUp() {
        return requiresFollowUp;
    }
}

