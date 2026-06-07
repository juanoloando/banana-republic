package com.bananarepublic.controller;

import com.bananarepublic.trade.TradeOffer;

import engine.DiceResult;
import engine.TurnPhase;
import model.board.HexCoordinate;
import model.card.ExperimentCard;
import model.player.Player;
import model.resource.ResourceType;


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


    void onBotPlayerTradeProposed(TradeOffer offer);
}
