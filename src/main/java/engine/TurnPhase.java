package engine;

public enum TurnPhase {
    SETUP_PLACE_FIRST,
    SETUP_PLACE_SECOND,
    ROLL_DICE,
    ROBBER_MOVE,
    ROBBER_DISCARD,
    TRADE_BUILD,
    GAME_OVER;

    public boolean isSetup() {
        return this == SETUP_PLACE_FIRST || this == SETUP_PLACE_SECOND;
    }
}
