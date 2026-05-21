package engine.phase;

import engine.GameEngine;
import engine.GameStateImpl;

public interface PhaseHandler {

    void handle(GameStateImpl state, GameEngine engine);

    TurnPhaseType getHandledPhase();
}
