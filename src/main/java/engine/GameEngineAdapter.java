package engine;

import model.board.HexCoordinate;
import model.card.ExperimentCard;
import model.player.Player;
import model.resource.ResourceType;

public abstract class GameEngineAdapter implements GameEngineListener {

    @Override public void onDiceRolled(DiceResult result) {}
    @Override public void onResourcesDistributed(Player activePlayer) {}
    @Override public void onResourcesDiscarded(Player player) {}
    @Override public void onResourceStolen(Player thief, Player victim, ResourceType stolenResource) {}
    @Override public void onRobberMoved(HexCoordinate newPosition) {}
    @Override public void onBuildingPlaced(Player player, int intersectionId, String buildingType) {}
    @Override public void onPipePlaced(Player player, int pathId) {}
    @Override public void onCardBought(Player player, ExperimentCard card) {}
    @Override public void onCardPlayed(Player player, ExperimentCard card) {}
    @Override public void onKnightPlayed(Player player) {}
    @Override public void onTurnChanged(Player newActivePlayer) {}
    @Override public void onPhaseChanged(TurnPhase newPhase) {}
    @Override public void onTimerStarted(int durationSeconds) {}
    @Override public void onTimerTick(int secondsRemaining) {}
    @Override public void onTimerExpired(Player playerWhoTimedOut) {}
    @Override public void onGameWon(Player winner) {}
}
