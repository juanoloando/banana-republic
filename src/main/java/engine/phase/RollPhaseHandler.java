package engine.phase;

import engine.DiceResult;
import engine.GameEngine;
import engine.GameStateImpl;
import engine.TurnPhase;

public class RollPhaseHandler implements PhaseHandler {

    @Override
    public void handle(GameStateImpl state, GameEngine engine) {
        DiceResult result = engine.getLastDiceResult();
        if (result == null) return;

        if (result.isRobberActivated()) {
            state.setCurrentPhase(TurnPhase.ROBBER_DISCARD);
        } else {
            engine.distributeResources(result.getTotal());
            state.setCurrentPhase(TurnPhase.TRADE_BUILD);
            engine.startTradeBuildTimer();
        }
    }

    @Override
    public TurnPhaseType getHandledPhase() {
        return TurnPhaseType.ROLL_DICE;
    }
}
