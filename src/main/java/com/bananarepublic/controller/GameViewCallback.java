package com.bananarepublic.controller;

import engine.DiceResult;
import engine.TurnPhase;
import model.board.HexCoordinate;
import model.card.ExperimentCard;
import model.player.Player;
import model.resource.ResourceType;

/**
 * Interface callback yang diimplementasi oleh GameRevisionController.
 * GameController meneruskan semua engine events lewat interface ini
 * sehingga GameController tidak perlu tahu detail implementasi UI.
 */
public interface GameViewCallback {

    void onPhaseChanged(TurnPhase newPhase);
    void onDiceRolled(DiceResult result);
    void onTurnChanged(Player newActivePlayer);
    void onBuildingPlaced(Player player, int intersectionId, String buildingType);
    void onPipePlaced(Player player, int pathId);
    void onResourcesDistributed(Player activePlayer);
    void onRobberMoved(HexCoordinate newPosition);
    void onResourcesDiscarded(Player player);
    void onResourceStolen(Player thief, Player victim, ResourceType stolenResource);
    void onCardBought(Player player, ExperimentCard card);
    void onCardPlayed(Player player, ExperimentCard card);
    void onKnightPlayed(Player player);
    void onTimerTick(int secondsRemaining);
    void onTimerExpired(Player playerWhoTimedOut);
    void onGameWon(Player winner);
}
