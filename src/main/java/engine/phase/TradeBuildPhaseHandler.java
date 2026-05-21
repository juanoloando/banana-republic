package engine.phase;

import engine.GameEngine;
import engine.GameStateImpl;
import engine.TurnPhase;

public class TradeBuildPhaseHandler implements PhaseHandler {

    @Override
    public void handle(GameStateImpl state, GameEngine engine) {
        engine.stopTradeBuildTimer();

        int nextIndex = (state.getCurrentPlayerIndex() + 1) % state.getAllPlayers().size();
        state.setCurrentPlayerIndex(nextIndex);
        state.setCurrentPhase(TurnPhase.ROLL_DICE);

        if (nextIndex == 0) {
            state.incrementRoundNumber();
        }
    }

    @Override
    public TurnPhaseType getHandledPhase() {
        return TurnPhaseType.TRADE_BUILD;
    }
}
