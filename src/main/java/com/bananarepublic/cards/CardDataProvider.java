package com.bananarepublic.cards;

import model.resource.ResourceType;

import java.util.List;

public interface CardDataProvider {
    int getCurrentPlayerIndex();
    String getPlayerName(int playerIndex);
    List<String> getPlayerNames();
    PlayerCardInventory getCardInventory(int playerIndex);
    int getPlayerVictoryPoints(int playerIndex);
    int getVictoryThreshold();
    boolean isRoadBuildingEffectInProgress(int playerIndex);

    CardUseResult playCard(CardType type, ResourceType monopolyResource);

    void appendLog(String message);
    void showInformation(String message);
}

