package engine.phase;

import engine.GameEngine;
import engine.GameStateImpl;
import engine.TurnPhase;
import model.player.Player;

import java.util.List;

public class SetupPhaseHandler implements PhaseHandler {

    @Override
    public void handle(GameStateImpl state, GameEngine engine) {
        List<Player> players = state.getAllPlayers();
        int playerCount = players.size();
        int setupIndex = state.getSetupTurnIndex();
        boolean secondRound = state.isSetupSecondRoundStarted();

        if (!secondRound) {
            int nextIndex = setupIndex + 1;
            if (nextIndex >= playerCount) {
                state.setSetupSecondRoundStarted(true);
                state.setSetupTurnIndex(playerCount - 1);
                state.setCurrentPlayerIndex(playerCount - 1);
                state.setCurrentPhase(TurnPhase.SETUP_PLACE_SECOND);
            } else {
                state.setSetupTurnIndex(nextIndex);
                state.setCurrentPlayerIndex(nextIndex);
            }
        } else {
            int nextIndex = setupIndex - 1;
            if (nextIndex < 0) {
                state.setCurrentPlayerIndex(0);
                state.setCurrentPhase(TurnPhase.ROLL_DICE);
                state.incrementRoundNumber();
            } else {
                state.setSetupTurnIndex(nextIndex);
                state.setCurrentPlayerIndex(nextIndex);
            }
        }
    }

    @Override
    public TurnPhaseType getHandledPhase() {
        return TurnPhaseType.SETUP;
    }
}
