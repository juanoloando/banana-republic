package engine;

import model.board.HexCoordinate;
import model.card.ExperimentCard;
import model.player.Player;
import model.resource.ResourceType;

public interface GameEngineListener {

    void onDiceRolled(DiceResult result);

    void onResourcesDistributed(Player activePlayer);

    void onResourcesDiscarded(Player player);

    void onResourceStolen(Player thief, Player victim, ResourceType stolenResource);

    void onRobberMoved(HexCoordinate newPosition);

    void onBuildingPlaced(Player player, int intersectionId, String buildingType);

    void onPipePlaced(Player player, int pathId);

    void onCardBought(Player player, ExperimentCard card);

    void onCardPlayed(Player player, ExperimentCard card);

    void onKnightPlayed(Player player);

    void onTurnChanged(Player newActivePlayer);

    void onPhaseChanged(TurnPhase newPhase);

    void onTimerStarted(int durationSeconds);

    void onTimerTick(int secondsRemaining);

    void onTimerExpired(Player playerWhoTimedOut);

    void onGameWon(Player winner);
}
